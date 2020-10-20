package org.ng.distribute;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class DistributedCounter {
    private int COUNT = 10;
    private int TIME = 50;

    public static void main(String[] args) {
        // initialize log4j, zookeeper otherwise an error.
        //org.apache.log4j.BasicConfigurator.configure();

        try {
            DistributedCounter app = new DistributedCounter();
            app.zk();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void zk() throws Exception {
        String path = "/path/count";

        CuratorFramework client = initClient(path);

        // open COUNT threads simulate assignment to different nodes distributed in the SharedCount.
        ExecutorService service = Executors.newFixedThreadPool(COUNT);
        for (int i = 0; i < COUNT; i++) {
            DistributedAtomicLong count = new DistributedAtomicLong(client, path, new RetryNTimes(10, 10));

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        TimeUnit.SECONDS.sleep((long) (Math.random() * TIME));

                        setValue(count);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            service.submit(runnable);
        }

        service.shutdown();
        service.awaitTermination(TIME, TimeUnit.SECONDS);
    }

    private synchronized void setValue(DistributedAtomicLong count) throws Exception {
        System.out.println("-------------");

        System.out.println("current value =" + count.get().preValue());

        long l = (long) (Math.random() * 1000);
        System.out.println("Trying to write:" + l);

        AtomicValue<Long> result = count.trySet(l);
        if (result.succeeded()) {
            System.out.println(result.postValue() + "set successfully");
            System.out.println(result.preValue() + "->" + result.postValue());
        } else {
            System.out.println(result.postValue() + "setting failed");
            System.out.println("Counter still old values:" + result.preValue());
        }
    }

    private CuratorFramework initClient(String path) throws Exception {
        CuratorFramework client = makeClient();
        client.start();

        boolean b = isPathExist(client, path);

        // If this path does not exist, stat is null, create a new node path.
        if (!b) {
            String s = client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(path);

            System.out.println("create" + s);
        } else {
            System.out.println("already exists:" + path + ", without duplication created");
        }

        return client;
    }

    // detect the presence of the path.
    private boolean isPathExist(CuratorFramework client, String path) {
        boolean b = false;

        // detect the presence of the path.
        try {
            Stat stat = client.checkExists().forPath(path);
            b = stat == null ? false : true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return b;
    }

    private CuratorFramework makeClient() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(getAddress())
                .sessionTimeoutMs(10 * 1000)
                .connectionTimeoutMs(20 * 1000)
                .retryPolicy(retryPolicy)
                .build();

        return client;
    }

    private String getAddress() {
        String ip = "127.0.0.1";
        return ip + ":2181," + ip + ":2182," + ip + ":2183";
    }
}
