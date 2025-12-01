import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.*;

public class TaskBalancingBenchmark {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== TASK BALANCING BENCHMARK ===");

        int rows = getValidInt(scanner, "Enter number of rows: ");
        int cols = getValidInt(scanner, "Enter number of columns: ");

        System.out.println("Generating matrix...");
        int[][] matrix = generateMatrix(rows, cols);
        
        if (rows * cols <= 100) {
            printMatrix(matrix);
        } else {
            System.out.println("Matrix is too large to display entirely.");
            System.out.println("First element (0,0): " + matrix[0][0]);
        }

        int firstElement = matrix[0][0];
        long thresholdValue = 2L * firstElement;
        System.out.println("Target: Find minimum element > " + thresholdValue);

        System.out.println("\n--- Starting Work Stealing (Fork/Join) ---");
        long startWS = System.nanoTime();
        int resultWS = runWorkStealing(matrix, thresholdValue);
        long endWS = System.nanoTime();
        double timeWS = (endWS - startWS) / 1_000_000_000.0;
        
        printResult("Work Stealing", resultWS, timeWS);

        System.out.println("\n--- Starting Work Dealing (Fixed Pool) ---");
        long startWD = System.nanoTime();
        int resultWD = runWorkDealing(matrix, thresholdValue);
        long endWD = System.nanoTime();
        double timeWD = (endWD - startWD) / 1_000_000.0;

        printResult("Work Dealing", resultWD, timeWD);

        scanner.close();
    }

    private static int getValidInt(Scanner scanner, String prompt) {
        int value;
        while (true) {
            System.out.print(prompt);
            if (scanner.hasNextInt()) {
                value = scanner.nextInt();
                if (value > 0) {
                    break;
                } else {
                    System.out.println("Please enter a positive integer.");
                }
            } else {
                System.out.println("Invalid input. Please enter an integer.");
                scanner.next();
            }
        }
        return value;
    }

    private static int[][] generateMatrix(int rows, int cols) {
        int[][] matrix = new int[rows][cols];
        Random random = new Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = random.nextInt(10000);
            }
        }
        return matrix;
    }

    private static void printMatrix(int[][] matrix) {
        System.out.println("Generated Matrix:");
        for (int[] row : matrix) {
            for (int val : row) {
                System.out.printf("%5d ", val);
            }
            System.out.println();
        }
    }

    private static void printResult(String method, int result, double timeMs) {
        System.out.println("Method: " + method);
        if (result == Integer.MAX_VALUE) {
            System.out.println("Result: No element found strictly greater than 2 * first element.");
        } else {
            System.out.println("Result: " + result);
        }
        System.out.printf("Time: %.4f ms%n", timeMs);
    }

    private static int runWorkStealing(int[][] matrix, long threshold) {
        ForkJoinPool pool = new ForkJoinPool();
        MinSearchRecursiveTask task = new MinSearchRecursiveTask(matrix, 0, matrix.length, threshold);
        return pool.invoke(task);
    }

    private static int runWorkDealing(int[][] matrix, long threshold) {
        int coreCount = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(coreCount);
        List<Future<Integer>> futures = new ArrayList<>();

        int totalRows = matrix.length;
        int chunkSize = (int) Math.ceil((double) totalRows / coreCount);

        for (int i = 0; i < totalRows; i += chunkSize) {
            int startRow = i;
            int endRow = Math.min(i + chunkSize, totalRows);
            
            Callable<Integer> task = () -> {
                int localMin = Integer.MAX_VALUE;
                for (int r = startRow; r < endRow; r++) {
                    for (int c = 0; c < matrix[r].length; c++) {
                        if (matrix[r][c] > threshold && matrix[r][c] < localMin) {
                            localMin = matrix[r][c];
                        }
                    }
                }
                return localMin;
            };
            futures.add(executor.submit(task));
        }

        int globalMin = Integer.MAX_VALUE;
        try {
            for (Future<Integer> f : futures) {
                int val = f.get();
                if (val < globalMin) {
                    globalMin = val;
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
        return globalMin;
    }

    static class MinSearchRecursiveTask extends RecursiveTask<Integer> {
        private final int[][] matrix;
        private final int startRow;
        private final int endRow;
        private final long threshold;
        private static final int SEQUENTIAL_THRESHOLD = 100;

        public MinSearchRecursiveTask(int[][] matrix, int startRow, int endRow, long threshold) {
            this.matrix = matrix;
            this.startRow = startRow;
            this.endRow = endRow;
            this.threshold = threshold;
        }

        @Override
        protected Integer compute() {
            if (endRow - startRow <= SEQUENTIAL_THRESHOLD) {
                return computeSequentially();
            } else {
                int mid = startRow + (endRow - startRow) / 2;
                MinSearchRecursiveTask leftTask = new MinSearchRecursiveTask(matrix, startRow, mid, threshold);
                MinSearchRecursiveTask rightTask = new MinSearchRecursiveTask(matrix, mid, endRow, threshold);
                
                leftTask.fork();
                int rightResult = rightTask.compute();
                int leftResult = leftTask.join();
                
                return Math.min(leftResult, rightResult);
            }
        }

        private Integer computeSequentially() {
            int min = Integer.MAX_VALUE;
            for (int i = startRow; i < endRow; i++) {
                for (int val : matrix[i]) {
                    if (val > threshold && val < min) {
                        min = val;
                    }
                }
            }
            return min;
        }
    }
}