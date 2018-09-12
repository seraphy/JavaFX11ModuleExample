cd /d %~dp0
call setenv.cmd
call mvn clean package
rem java -p target/mods -m javamoduleexample/jp.seraphyware.example.JavaModuleExample

if exist "release" rmdir /s /q release
jlink --module-path target/mods --add-modules javamoduleexample --no-man-pages --no-header-files --verbose --output release --launcher run=javamoduleexample/jp.seraphyware.example.JavaModuleExample

release\bin\run.bat

if "%1" == "" pause
