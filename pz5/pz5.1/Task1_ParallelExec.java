import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Task1_ParallelExec {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("=== Starting Parallel Tasks ===");

        CompletableFuture<String> task1 = CompletableFuture.supplyAsync(() -> performTask("Image Processing", 2));
        CompletableFuture<String> task2 = CompletableFuture.supplyAsync(() -> performTask("Data Aggregation", 1));
        CompletableFuture<String> task3 = CompletableFuture.supplyAsync(() -> performTask("Security Scan", 3));

        CompletableFuture<Object> firstCompleted = CompletableFuture.anyOf(task1, task2, task3);
        
        firstCompleted.thenAccept(result -> {
            System.out.println("\n>> FASTEST TASK FINISHED: " + result);
        });

        CompletableFuture<Void> allTasks = CompletableFuture.allOf(task1, task2, task3);

        allTasks.thenRun(() -> {
            System.out.println("\n>> ALL TASKS COMPLETED. COLLECTING RESULTS...");
            
            String result1 = task1.join();
            String result2 = task2.join();
            String result3 = task3.join();

            System.out.println("Result 1: " + result1);
            System.out.println("Result 2: " + result2);
            System.out.println("Result 3: " + result3);
        }).get();

        System.out.println("=== Main Program Finished ===");
    }

    private static String performTask(String name, int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return name + " Done (Time: " + seconds + "s)";
    }
}