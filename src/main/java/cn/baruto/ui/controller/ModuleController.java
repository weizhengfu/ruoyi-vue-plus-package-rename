package cn.baruto.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class ModuleController {

    @FXML
    private TextField oldPrefixField;

    @FXML
    private TextField newPrefixField;

    @FXML
    private TextField moduleMapField;

    public String getOldPrefix() {
        return oldPrefixField.getText().trim();
    }

    public String getNewPrefix() {
        return newPrefixField.getText().trim();
    }

    public String getModuleMap() {
        return moduleMapField.getText().trim();
    }
}
