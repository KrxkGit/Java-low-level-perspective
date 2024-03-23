package threads;

import org.junit.Test;

import java.util.concurrent.locks.ReentrantLock;

public class TestThreads implements Runnable {
    static private Integer val;
    static volatile Integer val1;
    static ThreadLocal<Integer> val2 = new ThreadLocal<>();
    static {
        val2.set(Integer.valueOf(2));
    }
    synchronized protected void updateVal() {
        synchronized (this) {
            val = this.hashCode();
        }
    }
    protected Integer readVal() {
        val1 = 100;
        return val1;
    }
    protected void useLock() {
        ReentrantLock reentrantLock = new ReentrantLock();
        reentrantLock.lock();
//        reentrantLock.lockInterruptibly();
        val = 1;
        reentrantLock.unlock();
    }
    protected void useLocal() {
        val2.set(1);
        val2.set(val2.get() + 1);
    }
    @Override
    public void run() {
        updateVal();
        readVal();
        useLock();
        useLocal();
        System.out.println("I'm running.");
        System.out.printf("%d %d %d\n", val, val1, val2.get());
    }
    @Test
    public void test() {
        Thread thread = new Thread(new TestThreads());
        thread.start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        // 创建虚拟线程
//        Thread thread1 = Thread.ofVirtual().start(new TestThreads());
//        System.out.println(thread1.isVirtual());
//        System.out.println(thread.isVirtual());
    }
}
