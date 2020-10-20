package org.ng.distribute.lock;

import com.oracle.tools.packager.Log;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractLock implements Lock {
    @Override
    public void getLock() {
        if (tryLock()) {
            Log.info("Current Thread:"+Thread.currentThread().getName());
        } else {
            waitLock();
            getLock();
        }
    }

    protected abstract void waitLock();

    protected abstract boolean tryLock();

    protected abstract void releaseLock();

    @Override
    public void unlock() {
        Log.info("Current Thread:"+Thread.currentThread().getName());
        releaseLock();
    }
}
