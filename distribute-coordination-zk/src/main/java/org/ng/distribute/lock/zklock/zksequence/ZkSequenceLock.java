package org.ng.distribute.lock.zklock.zksequence;


import org.ng.distribute.lock.AbstractLock;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class ZkSequenceLock extends AbstractLock {
    private static final String zkServers = "127.0.0.1:2181";
    private static final int sessionTimeout = 8000;
    private static final int connectionTimeout = 5000;

    private static final String lockPath = "/lockPath";

    private String beforePath;
    private String currentPath;


    private ZkClient client;

    public ZkSequenceLock() {
        client = new ZkClient(zkServers);
        if (!client.exists(lockPath)) {
            client.createPersistent(lockPath);

        }
        log.info("zk client :{}", zkServers);

    }


    @Override
    protected void waitLock() {
        CountDownLatch latch = new CountDownLatch(1);
        IZkDataListener listener = new IZkDataListener() {
            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
                log.info("Data Deleted-{}", dataPath);
                latch.countDown();
            }

            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {
            }
        };
        client.subscribeDataChanges(beforePath, listener);

        if (client.exists(beforePath)) {
            try {
                log.info("Current_path:" + currentPath);
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        client.unsubscribeDataChanges(beforePath, listener);
    }

    @Override
    protected boolean tryLock() {
        if (currentPath == null) {
            currentPath = client.createEphemeralSequential(lockPath + "/", "lock-data");
            log.info("current:" + currentPath);
        }

        List<String> childrens = client.getChildren(lockPath);
        Collections.sort(childrens);
        System.out.println(currentPath + "--" + childrens.get(0));

        if (currentPath.equals("/lockPath9/0000000000")) {
            log.info("^^^" + beforePath + "^^" + childrens);
        }

        if (currentPath.equals(lockPath + "/" + childrens.get(0))) {
            log.info(Thread.currentThread().getName());
            return true;
        } else {
            int curIndex = childrens.indexOf(currentPath.substring(lockPath.length() + 1));
            beforePath = lockPath + "/" + childrens.get(curIndex - 1);
        }
        log.info("beforePath" + beforePath);
        return false;
    }

    @Override
    public void releaseLock() {
        log.info("delete:" + currentPath);
        client.delete(currentPath);
    }
}
