package cn.baruto.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        primaryStage.setTitle("RuoYi-Vue-Plus 包名修改器");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(650);
        primaryStage.setWidth(960);
        primaryStage.setHeight(680);
        primaryStage.centerOnScreen();

        try {
            Image icon = new Image(getClass().getResourceAsStream("/icon.png"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            // 忽略图标加载失败
        }

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
