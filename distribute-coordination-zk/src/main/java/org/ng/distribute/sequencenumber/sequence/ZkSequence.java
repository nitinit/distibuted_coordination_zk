package org.ng.distribute.sequencenumber.sequence;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class ZkSequence {

    private static final int baseSleepTimeMs = 500;
    private static final int maxRetries = 3;

    private RetryPolicy retryPolicy;
    private DistributedAtomicLong atomicLong;

    public ZkSequence(CuratorFramework client, String counterPath) {
        this.retryPolicy = new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries);
        this.atomicLong = new DistributedAtomicLong(client, counterPath, retryPolicy);
    }

    public Long sequence() throws Exception{
        AtomicValue<Long> atomicValue = atomicLong.increment();
        return atomicValue.succeeded()?atomicValue.postValue():null;
    }
}
