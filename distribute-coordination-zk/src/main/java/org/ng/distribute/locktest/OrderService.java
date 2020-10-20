package org.ng.distribute.locktest;

import org.ng.distribute.lock.Lock;
import org.ng.distribute.lock.zklock.zksequence.ZkSequenceLock;
import org.ng.distribute.lock.zklock.zksimple.ZkPlainLock;

public class OrderService implements Runnable{

    private OrderNumGenerator orderNumGenerator = new OrderNumGenerator();

    //private Lock lock = new ZkPlainLock();
    private Lock lock = new ZkSequenceLock();

    public void run() {
        getNumber();
    }

    public void getNumber() {
        try {
            lock.getLock();
            String number = orderNumGenerator.getNumber();
            System.out.println(Thread.currentThread().getName() + ", generated the order: " + number);
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                lock.unlock();
            } catch(Exception ex) {
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("##generated order####");
        for(int i=0;i<50;i++){
            new Thread(new OrderService()).start();
        }
    }

}
