package cn.baruto.ui.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;

public class LogController {

    @FXML
    private TextArea logArea;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label progressLabel;

    public void appendLog(String message) {
        if (Platform.isFxApplicationThread()) {
            logArea.appendText(message);
        } else {
            Platform.runLater(() -> logArea.appendText(message));
        }
    }

    public void clearLog() {
        logArea.clear();
    }

    public void setProgress(double progress) {
        if (Platform.isFxApplicationThread()) {
            progressBar.setProgress(progress);
            progressLabel.setText(String.format("%.0f%%", progress * 100));
        } else {
            Platform.runLater(() -> {
                progressBar.setProgress(progress);
                progressLabel.setText(String.format("%.0f%%", progress * 100));
            });
        }
    }

    public void showProgressBar(boolean visible) {
        progressBar.setVisible(visible);
    }

    public void setProgressLabel(String text) {
        progressLabel.setText(text);
    }
}
