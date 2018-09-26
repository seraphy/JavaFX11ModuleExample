package jp.seraphyware.example;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.TreeSet;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * システムプロパティを表示するJavaFX画面サンプル
 */
public class JavaModuleExample extends Application implements Initializable {

    /**
     * テーブルビューのモデル
     */
    private static class SysProp {

        private StringProperty name = new SimpleStringProperty();

        private StringProperty value = new SimpleStringProperty();

        public StringProperty name() {
            return name;
        }

        public StringProperty value() {
            return value;
        }
    }

    @FXML
    private TableView<SysProp> tblSysProps;

    @FXML
    private TableColumn<SysProp, String> colName;

    @FXML
    private TableColumn<SysProp, String> colValue;

    private ObservableList<SysProp> sysPropItems = FXCollections.observableArrayList();

    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        // FXMLからシーングラフを作成する
        // ※ FXMLLoaderはリフレクションによって、このクラスのプライベートフィードを書き込む。
        // そのためにはmodule-infoでopensしておく必要がある。
        var ldr = new FXMLLoader();
        ldr.setController(this);
        var fxmlLoc = JavaModuleExample.class.getResource("/MainWindow.fxml");
        System.out.println("fxml=" + fxmlLoc);
        ldr.setLocation(fxmlLoc);

        VBox parent;
        try {
            parent = (VBox) ldr.load();

        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        // タイトルにモジュール名／クラス名を表示する。
        // 無名クラス(クラスパス指定による起動)の場合はモジュール名はnullとなる。
        Class<?> cls = JavaModuleExample.class;
        stage.setTitle(cls.getModule().getName() + "/" + cls.getSimpleName());

        // WebViewの利用のテスト
        VBox box = new VBox();
        {
            WebView wv = new WebView();
            WebEngine engine = wv.getEngine();
            engine.loadContent("<title>t</title><h1>JavaFX Module Example</h1>", "text/html");

            wv.setPrefSize(100, 60);

            parent.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            box.getChildren().addAll(wv, parent);
            VBox.setVgrow(box, Priority.NEVER);
            VBox.setVgrow(parent, Priority.ALWAYS);
        }

        // シーンの設定とステージの表示
        stage.setScene(new Scene(box));
        stage.show();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colName.setCellValueFactory(feature -> feature.getValue().name());
        colValue.setCellValueFactory(feature -> feature.getValue().value());
        tblSysProps.setItems(sysPropItems);
        initSysPropList();
    }

    private void initSysPropList() {
        var sysProps = System.getProperties();
        var items = new ArrayList<SysProp>();
        for (var name : new TreeSet<>(sysProps.stringPropertyNames())) {
            var value = sysProps.getProperty(name);
            var item = new SysProp();
            item.name().set(name);
            item.value().set(value);
            items.add(item);
        }
        sysPropItems.setAll(items);
    }

    @FXML
    protected void onClose() {
        stage.close();
    }

    public static void main(String... args) throws Exception {
        launch(args);
    }
}