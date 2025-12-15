import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

public class AsyncSequenceAnalysis {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("=== TASK 2 STARTED ===");
        long globalStart = System.nanoTime();

        CompletableFuture<Void> task = CompletableFuture.supplyAsync(() -> {
            double[] numbers = new double[20];
            for (int i = 0; i < numbers.length; i++) {
                numbers[i] = ThreadLocalRandom.current().nextDouble(-100.0, 100.0);
            }
            return numbers;

        }).thenApplyAsync(numbers -> {
            double maxDiff = 0.0;
            if (numbers.length > 1) {
                maxDiff = Math.abs(numbers[0] - numbers[1]);
                for (int i = 1; i < numbers.length - 1; i++) {
                    double diff = Math.abs(numbers[i] - numbers[i + 1]);
                    if (diff > maxDiff) {
                        maxDiff = diff;
                    }
                }
            }
            return new CalculationResult(numbers, maxDiff);

        }).thenAcceptAsync(result -> {
            System.out.println("Generated Sequence:");
            System.out.println(Arrays.toString(result.sequence));
            System.out.printf("Calculated Max Difference (|a_i - a_i+1|): %.4f%n", result.maxDifference);

        }).thenRunAsync(() -> {
            long globalEnd = System.nanoTime();
            long totalDuration = globalEnd - globalStart;
            System.out.println("Total execution time for all async operations: " + totalDuration / 1_000_000.0 + " ms");
            System.out.println("=== TASK 2 COMPLETED SUCCESSFULLY ===");
        });

        task.get();
    }

    static class CalculationResult {
        double[] sequence;
        double maxDifference;

        public CalculationResult(double[] sequence, double maxDifference) {
            this.sequence = sequence;
            this.maxDifference = maxDifference;
        }
    }
}