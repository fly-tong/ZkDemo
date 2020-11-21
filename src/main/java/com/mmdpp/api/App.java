package com.mmdpp.api;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) throws Exception {

        //zk是有session概念的，没有连接池的概念
        //watch：1、观察 2、回调
        //注册只会发生 读类型调用如：get exeits
        //第一类：new zk 的时候传入的watch，这个watch时是session级别的，只限path,与node没有关系
        CountDownLatch cdl = new CountDownLatch(1);
        ZooKeeper zooKeeper = new ZooKeeper("192.168.73.125:2181,192.168.73.126:2181,192.168.73.127:2181,192.168.73.128:2181",
                3000, new Watcher() {
            @Override//回调方法
            public void process(WatchedEvent event) {

                Event.KeeperState state = event.getState();
                Event.EventType type = event.getType();
                String path = event.getPath();

                System.out.println("new zk watch: "+event.toString());

                switch (state) {
                    case Unknown:
                        break;
                    case Disconnected:
                        break;
                    case NoSyncConnected:
                        break;
                    case SyncConnected:
                        System.out.println("node connected");
                        //回调之后再执行wait下面的代码
                        cdl.countDown();
                        break;
                    case AuthFailed:
                        break;
                    case ConnectedReadOnly:
                        break;
                    case SaslAuthenticated:
                        break;
                    case Expired:
                        break;
                    case Closed:
                        break;
                }

                switch (type) {
                    case None:
                        break;
                    case NodeCreated:
                        break;
                    case NodeDeleted:
                        break;
                    case NodeDataChanged:
                        break;
                    case NodeChildrenChanged:
                        break;
                    case DataWatchRemoved:
                        break;
                    case ChildWatchRemoved:
                        break;
                }
            }
        });

        cdl.await();
        ZooKeeper.States state = zooKeeper.getState();
        switch (state) {
            case CONNECTING:
                System.out.println("zk connecting......");
                break;
            case ASSOCIATING:
                break;
            case CONNECTED:
                System.out.println("zk connected.....");
                break;
            case CONNECTEDREADONLY:
                break;
            case CLOSED:
                break;
            case AUTH_FAILED:
                break;
            case NOT_CONNECTED:
                break;
        };

        //节点增删改查

        //阻塞模型
        String pathName = zooKeeper.create("/ooxx", "hello".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

        Stat stat = new Stat();
        byte[] data = zooKeeper.getData("/ooxx", new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                //注册watch
                System.out.println("getData watch: " + event.toString());

                try {
                    //处理完之后继续监控，后面才会stat1/stat2都会监控
                    //true为default watch（new zk的那个watch）被重新注册
                    zooKeeper.getData("/ooxx",true, stat);
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }, stat);

        System.out.println(new String(data));
        //触发回调事件
        Stat stat1 = zooKeeper.setData("/ooxx", "test01".getBytes(), 0);
        //下面还会触发吗？--> 不会
        Stat stat2 = zooKeeper.setData("/ooxx", "test02".getBytes(), stat1.getVersion());

        System.out.println("before callback...");
        //异步回调验证
        zooKeeper.getData("/ooxx", false, new AsyncCallback.DataCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                //异步执行的顺序,取到数据之后再回调
                System.out.println("data callback....");
                System.out.println("ctx: " + ctx.toString());
                System.out.println("data: " + new String(data));
                System.out.println("stat: " + stat.toString());
            }
        },"callback");//这个内容即为ctx

        System.out.println("after callback....");
        Thread.sleep(100000);


    }
}
