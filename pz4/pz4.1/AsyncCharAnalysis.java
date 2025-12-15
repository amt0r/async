import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class AsyncCharAnalysis {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("=== TASK 1 STARTED ===");
        
        CompletableFuture<Void> task = CompletableFuture.supplyAsync(() -> {
            long start = System.nanoTime();
            Character[] chars = new Character[20];
            Random random = new Random();
            String source = "abcde 12345\tXYZ!@#  ";
            
            for (int i = 0; i < 20; i++) {
                chars[i] = source.charAt(random.nextInt(source.length()));
            }
            
            long duration = System.nanoTime() - start;
            System.out.println("[Step 1] Array Generation Time: " + duration / 1000 + " us");
            return chars;

        }).thenApplyAsync(chars -> {
            long start = System.nanoTime();
            
            List<Character> letters = new ArrayList<>();
            List<Character> spaces = new ArrayList<>();
            List<Character> tabs = new ArrayList<>();
            List<Character> others = new ArrayList<>();

            for (Character c : chars) {
                if (Character.isLetter(c)) {
                    letters.add(c);
                } else if (c == ' ') {
                    spaces.add(c);
                } else if (c == '\t') {
                    tabs.add(c);
                } else {
                    others.add(c);
                }
            }

            CategorizedResult result = new CategorizedResult(chars, letters, spaces, tabs, others);
            
            long duration = System.nanoTime() - start;
            System.out.println("[Step 2] Categorization Time: " + duration / 1000 + " us");
            return result;

        }).thenAcceptAsync(result -> {
            long start = System.nanoTime();
            
            System.out.println("Original Array: " + Arrays.toString(result.original));
            System.out.println("Letters: " + result.letters);
            System.out.println("Spaces count: " + result.spaces.size());
            System.out.println("Tabs count: " + result.tabs.size());
            System.out.println("Others: " + result.others);
            
            long duration = System.nanoTime() - start;
            System.out.println("[Step 3] Output Time: " + duration / 1000 + " us");

        }).thenRunAsync(() -> {
            System.out.println("=== TASK 1 COMPLETED SUCCESSFULLY ===");
        });

        task.get();
    }

    static class CategorizedResult {
        Character[] original;
        List<Character> letters;
        List<Character> spaces;
        List<Character> tabs;
        List<Character> others;

        public CategorizedResult(Character[] original, List<Character> letters, List<Character> spaces, List<Character> tabs, List<Character> others) {
            this.original = original;
            this.letters = letters;
            this.spaces = spaces;
            this.tabs = tabs;
            this.others = others;
        }
    }
}