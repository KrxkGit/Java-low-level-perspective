package threads;

import org.junit.Test;

/**
 * 测试 synchronized 关键字
 * 该关键字只对加关键字的代码段有效
 */
public class TestSynchronized {
    private Integer val = 0;
    class MyThread implements Runnable {
        public boolean toRead = true;

        public MyThread(boolean toRead) {
            this.toRead = toRead;
        }

        @Override
        public void run() {
            if (toRead) {
                try {
                    Read();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    Write();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        protected void Read() throws InterruptedException {
            Thread.sleep(10 * 1000);
            synchronized (val) {
//                Thread.sleep(10 * 1000);
                System.out.printf("Read: %d\n", val);
            }
        }

        protected void Write() throws InterruptedException {
            System.out.printf("Get lock\n");
            synchronized (val) {
                System.out.printf("Get lock owner\n");
                Thread.sleep(20 * 1000);
                val++;
                System.out.printf("Write: %d\n", val);
            }
        }
    }
    @Test
    public void test() throws InterruptedException {
        Runnable runnable = new MyThread(true);
        Thread threadRead = new Thread(runnable);
        Runnable runnable1 = new MyThread(false);
        Thread threadWrite = new Thread(runnable1);

        threadWrite.start();
        threadRead.start();

        threadWrite.join();
//        threadRead.join();
    }

}
