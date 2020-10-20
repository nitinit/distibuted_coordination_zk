package org.ng.distribute.lock;

public interface Lock {

    void getLock() throws Exception;

    void unlock() throws Exception;
}
