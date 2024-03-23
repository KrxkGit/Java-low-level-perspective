package threads.pool;

import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestThreadPool {
    private class MyThread implements Runnable {
        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName());
        }
    }
    protected void createThreadPool() {
        try (ThreadPoolExecutor threadPoolExecutor =
                     new ThreadPoolExecutor(5, 50, 1000,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(50), Executors.defaultThreadFactory(),
        new ThreadPoolExecutor.CallerRunsPolicy())) {
            for (int i = 0; i < 100; i++) {
                threadPoolExecutor.execute(new MyThread());
//                threadPoolExecutor.shutdown();
            }
        } finally {
            System.out.println("End");
        }
    }

    @Test
    public void test() {
        createThreadPool();
    }
}
