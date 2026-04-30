package cn.baruto.ui.controller;

import cn.baruto.config.RenameConfig;
import cn.baruto.ui.service.RenameService;
import cn.baruto.ui.util.ConfigMapper;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainController {

    @FXML private AnchorPane contentArea;
    @FXML private Button navSource;
    @FXML private Button navPackage;
    @FXML private Button navProject;
    @FXML private Button navModule;
    @FXML private Button navLog;
    @FXML private Button startButton;

    private SourceController sourceController;
    private PackageController packageController;
    private ProjectController projectController;
    private ModuleController moduleController;
    private LogController logController;

    private final Map<Button, Pane> navPages = new HashMap<>();
    private Button currentNav;
    private final RenameService renameService = new RenameService();

    @FXML
    public void initialize() {
        try {
            FXMLLoader sourceLoader = new FXMLLoader(getClass().getResource("/fxml/source.fxml"));
            Pane sourcePage = sourceLoader.load();
            sourceController = sourceLoader.getController();

            FXMLLoader packageLoader = new FXMLLoader(getClass().getResource("/fxml/package.fxml"));
            Pane packagePage = packageLoader.load();
            packageController = packageLoader.getController();

            FXMLLoader projectLoader = new FXMLLoader(getClass().getResource("/fxml/project.fxml"));
            Pane projectPage = projectLoader.load();
            projectController = projectLoader.getController();

            FXMLLoader moduleLoader = new FXMLLoader(getClass().getResource("/fxml/module.fxml"));
            Pane modulePage = moduleLoader.load();
            moduleController = moduleLoader.getController();

            FXMLLoader logLoader = new FXMLLoader(getClass().getResource("/fxml/log.fxml"));
            Pane logPage = logLoader.load();
            logController = logLoader.getController();

            navPages.put(navSource, sourcePage);
            navPages.put(navPackage, packagePage);
            navPages.put(navProject, projectPage);
            navPages.put(navModule, modulePage);
            navPages.put(navLog, logPage);

            renameService.messageProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    logController.appendLog(newVal);
                }
            });
            renameService.progressProperty().addListener((obs, oldVal, newVal) -> {
                logController.setProgress(newVal.doubleValue());
            });
            renameService.runningProperty().addListener((obs, oldVal, newVal) -> {
                startButton.setDisable(newVal);
                startButton.setText(newVal ? "处理中..." : "开始处理");
            });
            renameService.setOnSucceeded(e -> {
                logController.setProgressLabel("处理完成");
                logController.appendLog("\n任务成功完成\n");
                showAlert(Alert.AlertType.INFORMATION, "成功", "处理完成！");
            });
            renameService.setOnFailed(e -> {
                logController.setProgressLabel("处理失败");
                String error = renameService.getException() != null
                    ? renameService.getException().getMessage() : "未知错误";
                logController.appendLog("\n错误: " + error + "\n");
                showAlert(Alert.AlertType.ERROR, "错误", "处理失败: " + error);
            });

            switchPage(navSource);

        } catch (Exception e) {
            throw new RuntimeException("加载 FXML 失败", e);
        }
    }

    @FXML
    public void onNavClick(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        switchPage(clicked);
    }

    @FXML
    public void onStartProcessing(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }

        RenameConfig config = ConfigMapper.buildConfig(
            sourceController, packageController, projectController, moduleController);
        renameService.setConfig(config);

        logController.clearLog();
        logController.showProgressBar(true);
        logController.setProgress(0);
        logController.setProgressLabel("正在处理...");
        logController.appendLog("════════════════════════════════════════\n");
        logController.appendLog("  开始处理任务\n");
        logController.appendLog("════════════════════════════════════════\n\n");

        switchPage(navLog);

        renameService.restart();
    }

    private void switchPage(Button nav) {
        if (currentNav == nav) return;

        if (currentNav != null) {
            currentNav.getStyleClass().remove("selected");
        }

        nav.getStyleClass().add("selected");
        currentNav = nav;

        Pane page = navPages.get(nav);
        contentArea.getChildren().clear();

        page.setOpacity(0);
        AnchorPane.setTopAnchor(page, 0.0);
        AnchorPane.setBottomAnchor(page, 0.0);
        AnchorPane.setLeftAnchor(page, 0.0);
        AnchorPane.setRightAnchor(page, 0.0);
        contentArea.getChildren().add(page);

        FadeTransition fade = new FadeTransition(Duration.millis(200), page);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private boolean validateInputs() {
        String zipPath = sourceController.getZipPath();
        if (zipPath.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "验证错误", "请选择 ZIP 文件");
            return false;
        }
        if (!new File(zipPath).exists()) {
            showAlert(Alert.AlertType.WARNING, "验证错误", "ZIP 文件不存在");
            return false;
        }
        if (packageController.getPackageName().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "验证错误", "请输入目标包名");
            return false;
        }
        if (projectController.getProjectName().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "验证错误", "请输入项目名称");
            return false;
        }
        if (sourceController.getTargetPath().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "验证错误", "请选择输出目录");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
