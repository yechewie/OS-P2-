
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class ProducerConsumer {
    static final int BUFFER_SIZE = 5;
    static final int TOTAL_ITEMS = 10;

    static Queue<Integer> buffer = new LinkedList<>();
    static Semaphore empty = new Semaphore(BUFFER_SIZE);
    static Semaphore full = new Semaphore(0);
    static Semaphore mutex = new Semaphore(1);

    static class Producer extends Thread {
        public void run() {
            for (int item = 1; item <= TOTAL_ITEMS; item++) {
                try {
                    System.out.println("[Producer] Waiting to produce...");
                    empty.acquire();
                    mutex.acquire();

                    buffer.add(item);
                    System.out.println("[Producer] Produced item " + item);

                    mutex.release();
                    full.release();

                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("[Producer] Finished producing all items.");
        }
    }

    static class Consumer extends Thread {
        public void run() {
            for (int i = 1; i <= TOTAL_ITEMS; i++) {
                try {
                    System.out.println("[Consumer] Waiting to consume...");
                    full.acquire();
                    mutex.acquire();

                    int item = buffer.poll();
                    System.out.println("[Consumer] Consumed item " + item);

                    mutex.release();
                    empty.release();

                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("[Consumer] Finished consuming all items.");
        }
    }

    public static void main(String[] args) {
        Producer producer = new Producer();
        Consumer consumer = new Consumer();

        producer.start();
        consumer.start();

        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("[Main] Simulation complete.");
    }
}
