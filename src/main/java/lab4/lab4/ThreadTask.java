package lab4.lab4;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class ThreadTask implements Callable<Long> {
    private final ThreadInfo threadInfo;
    private final long initialSize;
    private final Runnable onUpdate;

    public ThreadTask(ThreadInfo threadInfo, long initialSize, Runnable onUpdate) {
        this.threadInfo = threadInfo;
        this.initialSize = initialSize;
        this.onUpdate = onUpdate;
    }

    @Override
    public Long call() {
        try {
            threadInfo.setStatus("Running");
            onUpdate.run();

            long increment = (long) (initialSize * 0.1);
            increment += (int) (Math.random() * increment);

            long executionTime = initialSize + increment; // Використовуємо increment як час очікування

            Thread.sleep(increment);

            long newSize = initialSize + increment;
            threadInfo.setExecutionTime(increment);
            threadInfo.setResult(newSize);

            threadInfo.setStatus("Completed");
            onUpdate.run();
            return initialSize;
        } catch (InterruptedException e) {
            threadInfo.setStatus("Interrupted");
            onUpdate.run();
            Thread.currentThread().interrupt();
            return 0L;
        }
    }
}
