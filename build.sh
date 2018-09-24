#! /bin/bash
export JAVA_HOME=$(/usr/libexec/java_home)

mvn clean package

rm -fr release
$JAVA_HOME/bin/jlink --module-path target/mods --add-modules javamoduleexample --no-man-pages --no-header-files --verbose --output release --launcher run=javamoduleexample/jp.seraphyware.example.JavaModuleExample

release/bin/run

