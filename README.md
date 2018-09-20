# JavaFXをJava11でビルドして使えるようにする方法

<ins>※ 2018/09/20修正: Java11のRC版(build 11+28) + OpenJFX11で試したバージョンに差し替え</ins>

## 要旨

Java11から、JavaFXは分離されるので、Java11(OpenJDK11)でJavaFXを使うにはどうしたらよいのか？

結論としては、以下の2点だけ注意すればよさそう。

- JavaFX(OpenJFX)はライブラリとして提供される(Java11以降)
  - MavenのCentral Repositoryに公開済みなので、pomの依存ライブラリとして書いておけばよい。
  - http://mail.openjdk.java.net/pipermail/openjfx-dev/2018-July/022088.html
- JavaFXはモジュールになっている(Java9以降)
  - java起動時に、 ```--add-modules javafx.controls,javafx.fxml``` のように指定するか、
  - 自分のjarにも```module-info.java```をつけて、単に ```--module modulename/FQCN``` のようにjavaを起動する。

## 実験コード

Windows上のOpenJDK11(RC 11+28)で、以下のようなJavaFXの簡単なコードで試す。

```shell
> java -version
openjdk version "11" 2018-09-25
OpenJDK Runtime Environment 18.9 (build 11+28)
OpenJDK 64-Bit Server VM 18.9 (build 11+28, mixed mode)
```

使用したMavenは、Apache Maven 3.5.4

```shell
> mvn -v
Apache Maven 3.5.4 (1edded0938998edf8bf061f1ceb3cfdeccf443fe; 2018-06-18T03:33:14+09:00)
Maven home: C:\Java\apache-maven-3.5.4\bin\..
Java version: 11, vendor: Oracle Corporation, runtime: C:\java\jdk-11
Default locale: ja_JP, platform encoding: MS932
OS name: "windows 7", version: "6.1", arch: "amd64", family: "windows"
```

### JavaModuleExample.java

```java
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
import java.text.*;
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
```

### MainWindow.fxml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.geometry.Insets?>
<?import javafx.scene.control.Button?>
<?import javafx.scene.control.ButtonBar?>
<?import javafx.scene.control.TableColumn?>
<?import javafx.scene.control.TableView?>
<?import javafx.scene.control.TitledPane?>
<?import javafx.scene.layout.VBox?>
<VBox maxHeight="-Infinity" maxWidth="-Infinity" minHeight="-Infinity" minWidth="-Infinity" prefHeight="400.0" prefWidth="600.0"
    xmlns="http://javafx.com/javafx/8.0.111"
    xmlns:fx="http://javafx.com/fxml/1">
    <children>
        <TitledPane animated="false" collapsible="false" maxHeight="1.7976931348623157E308" maxWidth="1.7976931348623157E308" text="システムプロパティ一覧" VBox.vgrow="ALWAYS">
            <content>
                <TableView fx:id="tblSysProps" maxHeight="1.7976931348623157E308" maxWidth="1.7976931348623157E308">
                    <columns>
                        <TableColumn fx:id="colName" prefWidth="150.0" text="プロパティ名" />
                        <TableColumn fx:id="colValue" minWidth="0.0" prefWidth="347.0" text="値" />
                    </columns>
                </TableView>
            </content>
        </TitledPane>
        <ButtonBar prefHeight="40.0" prefWidth="200.0">
            <buttons>
                <Button cancelButton="true" mnemonicParsing="false" onAction="#onClose" text="閉じる(終わる)" />
            </buttons>
            <VBox.margin>
                <Insets bottom="5.0" left="5.0" right="5.0" top="5.0" />
            </VBox.margin>
            <padding>
                <Insets right="20.0" />
            </padding>
        </ButtonBar>
    </children>
</VBox>
```

### module-info.java

このサンプルはモジュールにもするので、以下のように module-info.java もつける。

```java
module javamoduleexample {
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;

    // FXMLLoaderがリフレクションを使うためにopensする必要がある
    opens jp.seraphyware.example;
    exports jp.seraphyware.example;
}
```

モジュール名は「javamoduleexample」としている。

また、```opens``` により、FXMLLoaderがコントローラクラスにリフレクション経由でアクセスできるようにしている。(@FXMLの部分)


## Mavenでのビルド

ビルド時にはOpenJDK11にパスを通しておくこと。

(JavaFXまわりのライブラリはMavenでダウンロードされるので、OpenJFXのSDKなどは入れておく必要はない。)

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId>jp.seraphyware.example</groupId>
	<artifactId>javamoduleexample</artifactId>
	<version>1.0-SNAPSHOT</version>

	<packaging>jar</packaging>

	<name>JavaModuleExample</name>
	<url>http://maven.apache.org</url>

	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<java.version>11</java.version>
		<openjfx.version>11</openjfx.version>
		<mainClass>jp.seraphyware.example.JavaModuleExample</mainClass>
	</properties>

	<dependencies>
		<dependency>
			<groupId>org.openjfx</groupId>
			<artifactId>javafx-controls</artifactId>
			<version>${openjfx.version}</version>
		</dependency>
		<dependency>
			<groupId>org.openjfx</groupId>
			<artifactId>javafx-fxml</artifactId>
			<version>${openjfx.version}</version>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<!-- java10以降のコンパイル -->
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<version>3.8.0</version>
				<configuration>
					<release>${java.version}</release>
				</configuration>
			</plugin>
			<!-- JAR -->
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-jar-plugin</artifactId>
				<version>3.1.0</version>
				<configuration>
					<!-- 依存jarと同じmodsファイルに出力する -->
					<outputDirectory>${project.build.directory}/mods</outputDirectory>
					<archive>
						<manifest>
							<!-- MEANIFESTによるメインクラスの指定は意味ないかも。
								 java -p mods -m javamoduleexample/jp.seraphyware.example.JavaModuleExampl
								で起動するため。 -->
							<mainClass>${mainClass}</mainClass>
						</manifest>
					</archive>
				</configuration>
			</plugin>
			<!-- 依存するJARのlibへの展開 -->
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-dependency-plugin</artifactId>
				<version>3.1.1</version>
				<executions>
					<execution>
						<id>copy-dependencies</id>
						<phase>prepare-package</phase>
						<goals>
							<goal>copy-dependencies</goal>
						</goals>
						<configuration>
							<outputDirectory>${project.build.directory}/mods</outputDirectory>
							<overWriteReleases>false</overWriteReleases>
							<overWriteSnapshots>false</overWriteSnapshots>
							<overWriteIfNewer>true</overWriteIfNewer>
							<includeScope>runtime</includeScope>
						</configuration>
					</execution>
				</executions>
			</plugin>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-antrun-plugin</artifactId>
				<version>1.8</version>
				<executions>
					<execution>
						<!-- 展開されたjavafxの依存jarのうち、-win.jar以外のものを消す。
							(-win.jarでないものは1kbの中身空jarであり、module-infoもMANIFESTによる名前指定もないので、
							ないので、ファイル名から自動モジュール名がつけられるが、これがよろしくない。)
						 -->
						<id>copy-dependencies</id>
						<phase>prepare-package</phase>
						<goals>
							<goal>run</goal>
						</goals>
						<configuration>
							<target>
								<delete>
									<fileset dir="${project.build.directory}/mods">
										<include name="javafx-*.jar" />
										<exclude name="javafx-*-win.jar" />
										<exclude name="javafx-*-mac.jar" />
									</fileset>
								</delete>
							</target>
						</configuration>
					</execution>
					<execution>
						<!--
							現時点(maven-jar-plugin:3.1.0)では、module-info.javaに対する、module-main-class, module-version属性を
							付与することができないので、java9以降のjarコマンドを直接使用して属性を更新させる。
							https://stackoverflow.com/questions/43671410/how-to-specify-main-class-and-module-version-in-maven-jar-plugin

							これにより、
							java -p target/mods -m javamoduleexample
							としてモジュール名を指定してモジュールのメインクラスを実行することができる。
						-->
						<id>module-info-attributes</id>
						<phase>package</phase>
						<goals>
							<goal>run</goal>
						</goals>
						<configuration>
							<target>
								<exec executable="${java.home}/bin/jar" failonerror="true">
									<arg value="--main-class" />
									<arg value="${mainClass}" />
									<arg value="--module-version" />
									<arg value="${project.version}" />
									<arg value="--update" />
									<arg value="--file" />
									<arg value="${project.build.directory}/mods/${project.artifactId}-${project.version}.jar" />
								</exec>
							</target>
						</configuration>
					</execution>
				</executions>
			</plugin>
			<!-- Mavenから実行できるようにする。(ただし、無名パッケージ扱いになる) mvn package exec:java -->
			<plugin>
				<groupId>org.codehaus.mojo</groupId>
				<artifactId>exec-maven-plugin</artifactId>
				<version>1.2.1</version>
				<executions>
					<execution>
						<goals>
							<goal>java</goal>
						</goals>
					</execution>
				</executions>
				<configuration>
					<mainClass>${mainClass}</mainClass>
				</configuration>
			</plugin>
		</plugins>
	</build>
</project>
```

### ビルド設定の要点

- [OpenJFX 11](https://mvnrepository.com/artifact/org.openjfx/javafx-controls/11) のものを使用する
- コンパイルでは ```release=11``` を指定した。
  - java10以前ではASMを差し替える等の細工が必要だったが、```maven-compiler-plugin:3.8.0``` では指定はいらないようだ。(むしろ不味い？)

### ビルドと無名モジュールとしての実行

```shell
mvn package exec:java
```

このようにすると、ビルドして、実際にJavaFXのアプリケーションを起動してくれる。

ただし、この場合、```module-info.java``` を指定していても、メインクラスは無名モジュールとして読み込まれているようである。


### JavaFX関連jarの抜き出し

```maven-dependency-plugin``` で依存jarをmodsフォルダに出力するようにしているので、 ```package``` コマンドを実行すると、以下のファイルができる。

- javafx-base-11-ea+19-win.jar
- javafx-base-11-ea+19.jar
- javafx-controls-11-ea+19-win.jar
- javafx-controls-11-ea+19.jar
- javafx-fxml-11-ea+19-win.jar
- javafx-fxml-11-ea+19.jar
- javafx-graphics-11-ea+19-win.jar
- javafx-graphics-11-ea+19.jar
- javamoduleexample-1.0-SNAPSHOT.jar

対象とするプラットフォームごとに依存jarが異なるようで、Windowsの場合は ``` javafx-*-win.jar``` という名前がつけられている。

(ちなみに、Macの場合には、```javafx-*-mac.jar``` という名前が付けられている。)

```win.jar``` でないものは、中身が空で、マニフェストもmodule-infoも入っていないので、いらないものである。

というか、あると不味い。

たとえば、modsフォルダをjavaのモジュールパスとして指定すると、この空のjarにはマニフェストもmodule-infoもないので、モジュールの規則に従い、ファイル名から「自動モジュール名」として認識されるが、その名前が不味いようなのだ。

以下のようなエラーになる。

```
Caused by: java.lang.IllegalArgumentException: javafx.base.11.ea.19: Invalid module name: '11' is not a Java identifier
```

(名前が問題ないとしても、衝突の可能性もあるので、いらない名なしのモジュールはロードしないに越したことは無い。)

なので、この中身空のjarは消しておく。

(このpom.xmlでは、maven-antrun-plugin で消している。)

なお、最後の「javamoduleexample-1.0-SNAPSHOT.jar」は、このサンプルのモジュールjarである。

## javaコマンドからの実行

Mavenの ```exec:java``` から実行した場合は、メインクラスはクラスパス指定されているようで、モジュールではなく無名モジュール扱いとなっているようである。

モジュールとして扱うには以下のようにする。

```shell
java -p target/mods -m javamoduleexample/jp.seraphyware.example.JavaModuleExample
```

ウィンドウタイトルは「モジュール名／クラス名」を表示するようにしているので、今度は、モジュール名が表示されている。

### モジュールの実行クラスを指定する場合

モジュールには「module-main-class」や「module-version」という属性をmodule-info.classに付与することができ、その場合は、

```shell
java -p target/mods -m javamoduleexample
```

このようにモジュール名を指定するだけでメインクラスを実行することができる。


ただし、現時点ではソースコードや、Mavenのmaven-jar-plugin(3.1.0)からは、これらの属性は設定できない。(将来的にはjarプラグインが対応するものと思われる)


現在この属性は jdkの ```jar``` コマンドによって付与する。

mavenでは ```maven-antrun-plugin``` を使って、JAVA_HOME/bin/jar コマンドを起動して直接書き換えることになる。

```xml
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-antrun-plugin</artifactId>
	<version>1.8</version>
	<executions>
		<execution>
			<phase>package</phase>
			<goals>
				<goal>run</goal>
			</goals>
			<configuration>
				<target>
					<exec executable="${java.home}/bin/jar" failonerror="true">
						<arg value="--main-class" />
						<arg value="${mainClass}" />
						<arg value="--module-version" />
						<arg value="${project.version}" />
						<arg value="--update" />
						<arg value="--file" />
						<arg value="${project.build.directory}/mods/${project.artifactId}-${project.version}.jar" />
					</exec>
				</target>
			</configuration>
		</execution>
	</executions>
</plugin>
```

こうして設定された属性の内容は

```shell
jar --describe-module --file=target\mods\javamoduleexample-1.0-SNAPSHOT.jar
```

のようにして確認することができる。


## jlinkで配備用パッケージを作成してみる

jlinkを使うことにより、使用しているモジュールだけに絞り込んだ配備用のJREを作成できる。

また、そのときランチャ用のスクリプトも指定できる。

```shell
jlink --module-path target/mods --add-modules javamoduleexample --no-man-pages --no-header-files --verbose --output release --launcher run=javamoduleexample/jp.seraphyware.example.JavaModuleExample
```

モジュールパスを指定して、メインとなるモジュールを指定すれば、それが依存するモジュールに絞り込んだJREが作成される。

```---launcher シェル名=モジュール名／FQCN``` とすることで、起動用のシェルも作成してくれる。(中身をみると、たいしたことはやってないのだが。)

これでできあがった実行環境は

```
release\bin\run.bat
```

のようにして起動できる。

<font color="red">● openjfxの ```11-ea+19``` の場合はネイティブライブラリのロードに失敗するため、以下のような小細工が必要だったが、 **```11-ea+25```では修正された** ようである。</font>

### 実行時にNoSuchMethodErrorエラーが発生する場合

起動時に

```
Exception in thread "WindowsNativeRunloopThread" java.lang.NoSuchMethodError: <init>
        at javafx.graphics/com.sun.glass.ui.win.WinApplication.staticScreen_getScreens(Native Method)
        at javafx.graphics/com.sun.glass.ui.Screen.initScreens(Screen.java:412)
        at javafx.graphics/com.sun.glass.ui.Application.lambda$run$1(Application.java:152)
        at javafx.graphics/com.sun.glass.ui.win.WinApplication._runLoop(Native Method)
        at javafx.graphics/com.sun.glass.ui.win.WinApplication.lambda$runLoop$3(WinApplication.java:174)
        at java.base/java.lang.Thread.run(Thread.java:834)
```
のようなエラーがでる場合は、環境変数PATHの中で古いJavaFXを参照して、そのDLLが読み込まれている可能性がある。

(Pleiades標準のEclipseのプラグインでパッケージエクスプローラからコマンドプロンプトを開くものがあるが、
これを使うとEclipseまわりのパスが環境変数PATHに設定されるので不味いことになるようだ。)

対策として、Java関連のPATHをないようにしてする。

たとえば、

```
set PATH=C:\Windows\system32;C:\Windows
release\bin\run.bat
```

のようなシンプルな最小限のパスにしてから起動してみると良いかもしれない。


## まとめ

MavenでJavaFXを普通のライブラリとしてビルドできるようにしてもらったおかげで、JavaFXアプリのOpenJDK11対応も、それほど不自由なくゆけそうな感じである。

Java9以降のモジュール仕組みに不慣れであったため戸惑うところもあるが、これは本質とは、あまり関係ない。

挙動がおかしなところもあるけれど、まだ正式リリースされたものでないので、そのうち直るのではないか。

それほど案ずる必要はなかったという実感を得た。

## 参考

今回お世話になった数々のページ。順不同

- [Getting Started with JavaFX 11](http://docs.gluonhq.com/javafx11/) MavenでJavaFX11をビルドする手順が親切です
- [OpenJFX11 + OpenJDK11 + Maven で JavaFX を動かす](http://skrb.hatenablog.com/entry/2018/07/25/220530)
- [Java 8 で作成した JavaFX アプリケーションを Java 9 で動かす Again](https://www.coppermine.jp/docs/notepad/2017/12/javafx8-to-javafx9-again.html)
- [JDK9でのjavapackagerについて](https://aoe-tk.hatenablog.com/entry/2017/10/09/001222)
- [AnalogClockプログラムをJava SE 9のモジュール化](http://d.hatena.ne.jp/torutk/20171015/p1)
- [JDK9のモジュールとjlinkでアプリ配布向けのJVMを作る](https://qiita.com/koduki/items/5a1b5e5da95a21935d18)
- [Using Java 9 Modularization to Ship Zero-Dependency Native Apps](https://steveperkins.com/using-java-9-modularization-to-ship-zero-dependency-native-apps/)

以上、メモ終了。
