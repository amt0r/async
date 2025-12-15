import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Task2_ReviewAnalysis {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("=== Starting Product Review Analysis ===");

        CompletableFuture<ReviewData> platform1 = fetchAndParse("Amazon", 85);
        CompletableFuture<ReviewData> platform2 = fetchAndParse("eBay", 60);
        CompletableFuture<ReviewData> platform3 = fetchAndParse("BestBuy", 95);

        CompletableFuture<Double> combinedScore = platform1
                .thenCombine(platform2, (r1, r2) -> {
                    System.out.printf("Combining %s (%.1f) and %s (%.1f)%n", 
                        r1.source, r1.score, r2.source, r2.score);
                    return (r1.score + r2.score);
                })
                .thenCombine(platform3, (sumPrevious, r3) -> {
                    System.out.printf("Adding %s (%.1f) to current sum%n", 
                        r3.source, r3.score);
                    return (sumPrevious + r3.score) / 3.0; 
                });

        combinedScore.thenAccept(average -> {
            System.out.println("\n--- FINAL REPORT ---");
            System.out.printf("Average Product Rating: %.2f / 100.0%n", average);
            
            if (average > 80) {
                System.out.println("Conclusion: Highly Recommended!");
            } else if (average > 50) {
                System.out.println("Conclusion: Mixed Reviews. Buy with caution.");
            } else {
                System.out.println("Conclusion: Not Recommended.");
            }
        }).get();
    }

    private static CompletableFuture<ReviewData> fetchAndParse(String source, double baseScore) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Fetching raw data from " + source + "...");
            sleepRandom();
            return "RAW_JSON_" + source + "_" + baseScore;
        }).thenCompose(rawData -> CompletableFuture.supplyAsync(() -> {
            System.out.println("Parsing data from " + source + "...");
            sleepRandom();
            double finalScore = baseScore + ThreadLocalRandom.current().nextDouble(-5.0, 5.0);
            return new ReviewData(source, finalScore);
        }));
    }

    private static void sleepRandom() {
        try {
            TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(500, 1500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static class ReviewData {
        String source;
        double score;

        public ReviewData(String source, double score) {
            this.source = source;
            this.score = score;
        }
    }
}