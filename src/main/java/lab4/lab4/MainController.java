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

    private ObservableList<ThreadInfo> threadInfoList = FXCollections.observableArrayList();
    long totalSize = 10_000;
    long tempTotalSize = 0;

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

    private void startProcessing() {
        int threadCount = threadCountSpinner.getValue();

        mainThreadStatusLabel.setText("Main Thread Status: Running...");

        processIteration(threadCount);
        totalSize = threadInfoList.stream().mapToLong(ThreadInfo::getResult).sum();
    }

    private void processIteration(int threadCount) {
        threadInfoList.clear();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        long sizePerThread = totalSize / threadCount;
        long startTime = System.currentTimeMillis();
        tempTotalSize = 0;

        for (int i = 0; i < threadCount; i++) {
            ThreadInfo threadInfo = new ThreadInfo("Thread " + (i + 1));
            threadInfoList.add(threadInfo);

            CompletableFuture<Void> feature = CompletableFuture.supplyAsync(() -> {
                ThreadTask task = new ThreadTask(threadInfo, sizePerThread, () -> Platform.runLater(threadsTable::refresh));
                return task.call();
            }, executorService).thenApply(result -> {
                increment(result);
                threadInfo.setResult(result);
                return null;
            });

            futures.add(feature);
        }

        CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> Platform.runLater(() -> {
                    long endTime = -startTime + System.currentTimeMillis();
                    totalSize = tempTotalSize;
                    mainThreadStatusLabel.setText("Main Thread Status: Completed in " + endTime + "ms with total size " + totalSize + " byte");
                    threadsTable.refresh();
                    executorService.shutdownNow();
                }));
    }

    private synchronized void increment(long var) {
        tempTotalSize += var;
    }
}

