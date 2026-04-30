package cn.baruto.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class SourceController {

    @FXML
    private TextField zipPathField;

    @FXML
    private TextField targetPathField;

    @FXML
    public void onBrowseZip() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("ZIP 文件 (*.zip)", "*.zip"));
        chooser.setTitle("选择 RuoYi-Vue-Plus ZIP 文件");
        File file = chooser.showOpenDialog(getStage());
        if (file != null) {
            zipPathField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    public void onBrowseTarget() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("选择输出目录");
        File dir = chooser.showDialog(getStage());
        if (dir != null) {
            targetPathField.setText(dir.getAbsolutePath());
        }
    }

    public String getZipPath() {
        return zipPathField.getText().trim();
    }

    public String getTargetPath() {
        return targetPathField.getText().trim();
    }

    private Stage getStage() {
        return (Stage) zipPathField.getScene().getWindow();
    }
}
