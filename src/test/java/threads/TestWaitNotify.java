package threads;

import org.junit.Test;

import java.util.Scanner;

public class TestWaitNotify {
    private Integer val = 1;
    private class MyThread implements Runnable {
        @Override
        public void run() {
            System.out.println("Waiting for notify.");
            try {
                System.out.println(val);
                synchronized (val) {
                    val.wait();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("I'm running.");
        }
    }
    @Test
    public void test() throws InterruptedException {
        Thread thread = new Thread(new MyThread());
        thread.start();
        Scanner scanner = new Scanner(System.in);

        Thread.sleep(2000);
        synchronized (val) {
            val.notify();
        }
    }
}
