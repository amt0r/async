import java.util.List;
import java.util.concurrent.Callable;

public class CalculationTask implements Callable<Double> {
    private final List<Integer> numbersToCheck;

    public CalculationTask(List<Integer> numbersToCheck) {
        this.numbersToCheck = numbersToCheck;
    }

    @Override
    public Double call() throws Exception {
        String threadName = Thread.currentThread().getName();
        System.out.println("Thread [" + threadName + "] processing part: " + numbersToCheck);
        
        double sum = 0;
        for (Integer num : numbersToCheck) {
            sum += num;
        }

        Thread.sleep(100); 
        
        return sum;
    }
}