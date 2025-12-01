import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;

public class AsyncAverageCalculator {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        long startTime = System.currentTimeMillis();

        System.out.println("=== ASYNCHRONOUS AVERAGE CALCULATOR ===");

        System.out.print("Enter minimum number (0-1000): ");
        if (!scanner.hasNextInt()) {
            System.out.println("Error: Input must be an integer.");
            scanner.close();
            return;
        }
        int min = scanner.nextInt();

        System.out.print("Enter maximum number (0-1000): ");
        if (!scanner.hasNextInt()) {
            System.out.println("Error: Input must be an integer.");
            scanner.close();
            return;
        }
        int max = scanner.nextInt();
        scanner.close();

        if (min < 0 || min > 1000 || max < 0 || max > 1000) {
            System.out.println("Error: Numbers must be within the range [0; 1000].");
            return;
        }

        if (min >= max) {
            System.out.println("Error: Minimum must be smaller than maximum.");
            return;
        }

        if (max - min + 1 < 60) {
            System.out.println("Error: The range must allow for at least 60 unique numbers.");
            return;
        }

        CopyOnWriteArraySet<Integer> dataSet = DataGenerator.generateData(min, max);
        
        List<Integer> dataList = new ArrayList<>(dataSet);
        System.out.println("Generated array: " + dataList);

        int chunkSize = 10;
        int numberOfThreads = (int) Math.ceil((double) dataList.size() / chunkSize);
        
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<Double>> futures = new ArrayList<>();

        System.out.println("Splitting into " + numberOfThreads + " parts for processing...");

        for (int i = 0; i < dataList.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, dataList.size());
            List<Integer> subList = dataList.subList(i, end);

            CalculationTask task = new CalculationTask(subList);
            futures.add(executor.submit(task));
        }

        double totalSum = 0;
        
        for (int i = 0; i < futures.size(); i++) {
            Future<Double> future = futures.get(i);
            try {
                while (!future.isDone()) {
                    Thread.sleep(10); 
                }

                if (future.isCancelled()) {
                    System.out.println("Task #" + (i + 1) + " was cancelled.");
                } else {
                    double partialSum = future.get();
                    totalSum += partialSum;
                    System.out.println("-> Part #" + (i + 1) + " calculated. Part sum: " + partialSum);
                }

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

        double average = totalSum / dataSet.size();

        System.out.println("------------------------------------------------");
        System.out.println("Total Sum: " + totalSum);
        System.out.println("Element Count: " + dataSet.size());
        System.out.printf("AVERAGE VALUE: %.2f%n", average);

        long endTime = System.currentTimeMillis();
        System.out.println("Execution time: " + (endTime - startTime) + " ms");
    }
}