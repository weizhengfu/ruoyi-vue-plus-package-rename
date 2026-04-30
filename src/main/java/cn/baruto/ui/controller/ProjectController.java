package cn.baruto.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class ProjectController {

    @FXML
    private TextField projectNameField;

    @FXML
    private TextField projectVersionField;

    public String getProjectName() {
        return projectNameField.getText().trim();
    }

    public String getProjectVersion() {
        return projectVersionField.getText().trim();
    }
}
