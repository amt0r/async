import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ThreadLocalRandom;

public class DataGenerator {

    public static CopyOnWriteArraySet<Integer> generateData(int min, int max) {
        int count = ThreadLocalRandom.current().nextInt(40, 61);
        CopyOnWriteArraySet<Integer> dataSet = new CopyOnWriteArraySet<>();

        System.out.println("Generating " + count + " unique numbers...");

        while (dataSet.size() < count) {
            int randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);
            dataSet.add(randomNum);
        }
        
        return dataSet;
    }
}