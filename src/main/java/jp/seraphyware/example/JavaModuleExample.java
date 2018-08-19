package jp.seraphyware.example;

import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.*;
import javafx.fxml.*;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.collections.FXCollections;

import java.net.*;
import java.io.*;
import java.util.*;

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

        Parent parent;
        try {
            parent = (Parent) ldr.load();

        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        // タイトルにモジュール名／クラス名を表示する。
        // 無名クラス(クラスパス指定による起動)の場合はモジュール名はnullとなる。
        Class<?> cls = JavaModuleExample.class;
        stage.setTitle(cls.getModule().getName() + "/" + cls.getSimpleName());

        stage.setScene(new Scene(parent));
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