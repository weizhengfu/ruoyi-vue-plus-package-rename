package cn.baruto.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class PackageController {

    @FXML
    private TextField packageNameField;

    @FXML
    private TextField retainModulesField;

    public String getPackageName() {
        return packageNameField.getText().trim();
    }

    public String getRetainModules() {
        return retainModulesField.getText().trim();
    }
}
