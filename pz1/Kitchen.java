import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Kitchen {
    private final Semaphore stoves;
    private final BlockingQueue<Order> orderQueue;
    private final AtomicBoolean isOpen;

    public Kitchen(int stoveCount) {
        this.stoves = new Semaphore(stoveCount);
        this.orderQueue = new LinkedBlockingQueue<>();
        this.isOpen = new AtomicBoolean(true);
    }

    public void submitOrder(Order order) {
        if (isOpen.get()) {
            orderQueue.offer(order);
            System.out.println(">>> MANAGER: " + order + " accepted to queue.");
        } else {
            System.out.println("!!! MANAGER: Kitchen is closed. " + order + " rejected.");
        }
    }

    public Order getNextOrder() {
        try {
            return orderQueue.poll(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public void cookOrder(Order order, String chefName) {
        try {
            stoves.acquire();
            
            System.out.println("--- " + chefName + " started cooking " + order + ". Stoves available: " + (stoves.availablePermits()));
            Thread.sleep(2500);
            
            System.out.println("*** " + chefName + " finished " + order + ".");
        } catch (InterruptedException e) {
            System.err.println(chefName + " was interrupted while cooking.");
            Thread.currentThread().interrupt();
        } finally {
            stoves.release();
        }
    }

    public void closeKitchen() {
        isOpen.set(false);
        System.out.println("\n=== WORKDAY OVER. KITCHEN IS CLOSING ===\n");
    }

    public boolean isClosed() {
        return !isOpen.get();
    }

    public boolean isOrdersListEmpty() {
        return orderQueue.isEmpty();
    }
}