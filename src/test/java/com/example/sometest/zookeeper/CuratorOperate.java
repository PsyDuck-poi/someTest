package com.example.sometest.zookeeper;

import com.example.sometest.SomeTestApplication;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.tomcat.util.net.AprEndpoint;
import org.apache.zookeeper.CreateMode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SomeTestApplication.class)
public class CuratorOperate {

    @Autowired
    CuratorFramework curatorFramework;

    /**
     * 创建节点
     *
     * @throws Exception
     */
    @Test
    public void createNode() throws Exception {
        // 添加持久节点
        String path = curatorFramework.create().forPath("/curator-node");
        System.out.println(String.format("curator create node :%s successfully.", path));

        // 添加临时序号节点,并赋值数据
        String path1 = curatorFramework.create()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath("/curator-node", "some-data".getBytes());
        System.out.println(String.format("curator create node :%s successfully.", path));

        // System.in.read()目的是阻塞客户端关闭，我们可以在这期间查看zk的临时序号节点
        // 当程序结束时候也就是客户端关闭的时候，临时序号节点会消失
        System.in.read();
    }

    /**
     * 获取节点
     *
     * @throws Exception
     */
    @Test
    public void testGetData() throws Exception {
        // 在上面的方法执行后，创建了curator-node节点，但是我们并没有显示的去赋值
        // 通过这个方法去获取节点的值会发现，当我们通过Java客户端创建节点不赋值的话默认就是存储的创建节点的ip
        byte[] bytes = curatorFramework.getData().forPath("/curator-node");
        System.out.println(new String("*******"+bytes));
    }

    /**
     * 修改节点数据
     *
     * @throws Exception
     */
    @Test
    public void testSetData() throws Exception {
        curatorFramework.setData().forPath("/curator-node", "changed!".getBytes());
        byte[] bytes = curatorFramework.getData().forPath("/curator-node");
        System.out.println(new String("*******"+bytes));
    }

    /**
     * 创建节点同时创建⽗节点
     *
     * @throws Exception
     */
    @Test
    public void testCreateWithParent() throws Exception {
        String pathWithParent = "/node-parent/sub-node-1";
        String path = curatorFramework.create().creatingParentsIfNeeded().forPath(pathWithParent);
        System.out.println(String.format("curator create node :%s successfully.", path));
    }

    /**
     * 删除节点(包含子节点)
     *
     * @throws Exception
     */
    @Test
    public void testDelete() throws Exception {
        String pathWithParent = "/node-parent";
        curatorFramework.delete().guaranteed().deletingChildrenIfNeeded().forPath(pathWithParent);
    }

    @Test
    public void testDemo() throws Exception {
        InterProcessMutex  processMutex=new InterProcessMutex(curatorFramework,"/processMutex");
        processMutex.acquire();
        InterProcessReadWriteLock readWriteLock=new InterProcessReadWriteLock(curatorFramework,"/readWriteLock");
        readWriteLock.readLock();
        readWriteLock.writeLock();

        try {
            // 执行需要保护的代码块
        } finally {
            processMutex.release();
            readWriteLock.readLock().release();
            readWriteLock.writeLock().release();
        }

    }


}
