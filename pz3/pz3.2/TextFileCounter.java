import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TextFileCounter {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== TEXT FILE CHARACTER COUNTER (FORK/JOIN) ===");

        File directory = null;
        while (directory == null || !directory.exists() || !directory.isDirectory()) {
            System.out.print("Enter a valid directory path: ");
            String input = scanner.nextLine();
            File candidate = new File(input);
            if (candidate.exists() && candidate.isDirectory()) {
                directory = candidate;
            } else {
                System.out.println("Error: Invalid path or not a directory. Please try again.");
            }
        }

        System.out.println("Processing directory: " + directory.getAbsolutePath());
        System.out.println("Please wait...");

        long startTime = System.currentTimeMillis();

        ForkJoinPool pool = new ForkJoinPool();
        FolderProcessor task = new FolderProcessor(directory);
        List<String> results = pool.invoke(task);

        long endTime = System.currentTimeMillis();

        System.out.println("\n=== RESULTS ===");
        if (results.isEmpty()) {
            System.out.println("No text files found.");
        } else {
            for (String result : results) {
                System.out.println(result);
            }
        }

        System.out.println("---------------------------");
        System.out.println("Total files processed: " + results.size());
        System.out.println("Time taken: " + (endTime - startTime) + " ms");
        
        scanner.close();
    }
}

class FolderProcessor extends RecursiveTask<List<String>> {
    private final File folder;

    public FolderProcessor(File folder) {
        this.folder = folder;
    }

    @Override
    protected List<String> compute() {
        List<String> resultList = new ArrayList<>();
        List<FolderProcessor> subTasks = new ArrayList<>();

        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    FolderProcessor subTask = new FolderProcessor(file);
                    subTask.fork();
                    subTasks.add(subTask);
                } else {
                    if (isTextFile(file)) {
                        long charCount = countCharacters(file);
                        resultList.add("File: " + file.getName() + " | Characters: " + charCount);
                    }
                }
            }
        }

        for (FolderProcessor subTask : subTasks) {
            resultList.addAll(subTask.join());
        }

        return resultList;
    }

    private boolean isTextFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".txt") || name.endsWith(".log") || name.endsWith(".java") 
            || name.endsWith(".xml") || name.endsWith(".json") || name.endsWith(".md");
    }

    private long countCharacters(File file) {
        long count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            int c;
            while ((c = reader.read()) != -1) {
                if (!Character.isWhitespace(c)) { 
                    count++;
                }
            }
        } catch (IOException e) {
            return -1; 
        }
        return count;
    }
}