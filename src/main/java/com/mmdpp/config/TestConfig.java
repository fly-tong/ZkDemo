package com.mmdpp.config;

import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @Description //zk配置测试
 * @Author Mr_Tong
 * @Param 2020/11/20 0020 19:11
 * @Version 1.0
 **/
public class TestConfig {

    ZooKeeper zk;

    @Before
    public void conn(){
        zk = ZKUtil.getZk();
    }

    @After
    public void close(){
        try {
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getConf(){
        //工作的线程
        WatchCallBack watchCallBack = new WatchCallBack();
        watchCallBack.setZk(zk);
        MyConf myConf = new MyConf();
        watchCallBack.setMyConf(myConf);
        // "/AppConf"看似根目录，实则前面还有前缀/testConf
        zk.exists("/AppConf",watchCallBack,watchCallBack,"AppConf");

        watchCallBack.await();

        //两种情况：1、节点不存在，2、节点存在
        while (true){
            if (myConf.getConf().equals("")) {
                System.out.println("*********** config is null ************");

                watchCallBack.await();
            } else {
                System.out.println(myConf.getConf());
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(myConf.getConf());
        }

    }

}
