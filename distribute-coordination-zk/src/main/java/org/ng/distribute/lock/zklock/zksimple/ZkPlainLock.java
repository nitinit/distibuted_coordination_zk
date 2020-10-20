package org.ng.distribute.lock.zklock.zksimple;

import org.ng.distribute.lock.AbstractLock;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.concurrent.CountDownLatch;

@Slf4j
public class ZkPlainLock extends AbstractLock {
    private static final String zkServers = "127.0.0.1:2181";
    private static final int sessionTimeout = 8000;
    private static final int connectionTimeout = 5000;

    private static final String lockPath = "/lockPath";


    private ZkClient client;

    public ZkPlainLock() {
        client = new ZkClient(zkServers, sessionTimeout, connectionTimeout);
        log.info("zk client :{}", zkServers);
    }


    @Override
    protected void waitLock() {
        CountDownLatch latch = new CountDownLatch(1);

        IZkDataListener listener = new IZkDataListener() {
            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
                log.info("Data Deleted :{}", dataPath);
                latch.countDown();
            }

            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {
            }
        };
        client.subscribeDataChanges(lockPath, listener);

        if (client.exists(lockPath)) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        client.unsubscribeDataChanges(lockPath, listener);
    }

    @Override
    protected boolean tryLock() {
        try {
            client.createEphemeral(lockPath);
            System.out.println(Thread.currentThread().getName() + "");
        } catch (Exception e) {
            log.error("Error occured in try Lock", e);
            return false;
        }
        return true;
    }

    @Override
    public void releaseLock() {
        client.delete(this.lockPath);
    }
}
