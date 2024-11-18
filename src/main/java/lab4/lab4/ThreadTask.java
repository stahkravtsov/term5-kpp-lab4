package lab4.lab4;

import javafx.application.Platform;

public class ThreadTask implements Runnable {
    private final ThreadInfo threadInfo;
    private final Runnable runnable;


    public ThreadTask(ThreadInfo threadInfo, Runnable _runnable) {
        this.threadInfo = threadInfo;
        runnable = _runnable;
    }

    @Override
    public void run() {
        try {
            Platform.runLater(() -> threadInfo.setStatus("Running"));
            long startTime = System.currentTimeMillis();

            // Імітація ресурсозатратної операції
            Thread.sleep(1000 + (int) (Math.random() * 2000));
            long result = 1000 + (int) (Math.random() * 1000); // Результат обчислення

            long executionTime = System.currentTimeMillis() - startTime;

            Platform.runLater(() -> {
                threadInfo.setStatus("Completed");
                threadInfo.setExecutionTime(executionTime);
                threadInfo.setResult(result);
                runnable.run();
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}