module javamoduleexample {
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.web;

    // FXMLLoaderがリフレクションを使うためにopensする必要がある
    opens jp.seraphyware.example;
    exports jp.seraphyware.example;
}