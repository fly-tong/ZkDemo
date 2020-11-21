package com.mmdpp.config;

import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @Description //zk基本配置
 * @Author Mr_Tong
 * @Param 2020/11/20 0020 19:11
 * @Version 1.0
 **/
public class ZKUtil {

    private static ZooKeeper zooKeeper;

    private static String address = "192.168.73.125:2181,192.168.73.126:2181,192.168.73.127:2181,192.168.73.128:2181/testConf";

    private static DefaultWatch defaultWatch = new DefaultWatch();

    private static CountDownLatch cdl = new CountDownLatch(1);

    public static ZooKeeper getZk(){

        try {
            zooKeeper = new ZooKeeper(address,3000,defaultWatch);
            //watch里面连接完成之后得cdl.countDown()下面才能执行
            defaultWatch.setCdl(cdl);
            cdl.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return zooKeeper;
    }

}
