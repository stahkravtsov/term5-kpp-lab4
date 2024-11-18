package lab4.lab4;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MainController {

    @FXML
    private Spinner<Integer> threadCountSpinner;

    @FXML
    private Button startButton;

    @FXML
    private TableView<ThreadInfo> threadsTable;

    @FXML
    private TableColumn<ThreadInfo, String> nameColumn;

    @FXML
    private TableColumn<ThreadInfo, String> statusColumn;

    @FXML
    private TableColumn<ThreadInfo, Long> timeColumn;

    @FXML
    private TableColumn<ThreadInfo, Long> resultColumn;

    @FXML
    private Label mainThreadStatusLabel;

    private final ObservableList<ThreadInfo> threadInfoList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Налаштування колонок таблиці
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("executionTime"));
        resultColumn.setCellValueFactory(new PropertyValueFactory<>("result"));

        threadsTable.setItems(threadInfoList);

        startButton.setOnAction(event -> startProcessing());
    }

    @FXML
    public void onHelloButtonClick()
    {
        startProcessing();
    }

    private void startProcessing() {
        int threadCount = threadCountSpinner.getValue();
        threadInfoList.clear();
        mainThreadStatusLabel.setText("Main Thread Status: Running...");

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            ThreadInfo threadInfo = new ThreadInfo("Thread " + (i + 1));
            threadInfoList.add(threadInfo);

            // Завдання з поверненням результату
            Future<?> future = executorService.submit(() -> {
                ThreadTask task = new ThreadTask(threadInfo, () -> Platform.runLater(() -> threadsTable.refresh()));
                task.run(); // Запуск завдання
            });

            futures.add(future);
        }

        // Окремий потік для очікування завершення всіх потоків
        new Thread(() -> {
            try {
                for (Future<?> future : futures) {
                    future.get(); // Очікуємо завершення кожного потоку
                }
                long totalTime = System.currentTimeMillis() - startTime;

                Platform.runLater(() -> mainThreadStatusLabel.setText("Main Thread Status: Completed in " + totalTime + " ms"));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                executorService.shutdown();
            }
        }).start();
    }
}
