package com.mmdpp.config;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;

/**
 * @Description //TODO
 * @Author Mr_Tong
 * @Param 2020/11/21 0021 10:12
 * @Version 1.0
 **/
public class WatchCallBack implements Watcher, AsyncCallback, AsyncCallback.StatCallback, AsyncCallback.DataCallback {

    ZooKeeper zooKeeper;
    MyConf myConf;
    CountDownLatch cdl = new CountDownLatch(1);

    public MyConf getMyConf() {
        return myConf;
    }

    public void setMyConf(MyConf myConf) {
        this.myConf = myConf;
    }

    public ZooKeeper getZk(){
        return zooKeeper;
    }
    public void setZk(ZooKeeper zooKeeper){
        this.zooKeeper = zooKeeper;
    }

    public void await(){
        /*
        /exists会回调，第一个this watch注册，第二个watch stat回调
         */
        zooKeeper.exists("/AppConf",this,this,"AppConf");
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
        //数据的回调
        if (data != null) {
            String dataString = new String(data);
            myConf.setConf(dataString);
            cdl.countDown();
        }

    }

    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        //状态的回调
        if (stat != null) {
            zooKeeper.getData("/AppConf",this,this,"AppConf");
        }

    }

    @Override
    public void process(WatchedEvent event) {
        //事件的回调
        switch (event.getType()) {
            case None:
                break;
            case NodeCreated:
                zooKeeper.getData("/AppConf",this,this,"AppConf");
                break;
            case NodeDeleted:
                //删除时操作，容忍性
                //清空配置
                myConf.setConf("");
                //重新赋值，阻塞
                cdl = new CountDownLatch(1);
                break;
            case NodeDataChanged:
                zooKeeper.getData("/AppConf",this,this,"AppConf");
                break;
            case NodeChildrenChanged:
                break;
            case DataWatchRemoved:
                break;
            case ChildWatchRemoved:
                break;
        }

    }
}
