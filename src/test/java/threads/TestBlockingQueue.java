package threads;

import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TestBlockingQueue {
    @Test
    public void test() {
        BlockingQueue<Integer> blockingQueue = new ArrayBlockingQueue<>(10);
        blockingQueue.offer(2);
        blockingQueue.poll();
    }
}
