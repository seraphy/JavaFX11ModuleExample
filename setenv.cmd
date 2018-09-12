if /I "%JAVA_HOME%" neq "" goto setmvn
set JAVA_HOME=C:\java\jdk-11
set PATH=%JAVA_HOME%\bin;%PATH%
:setmvn
if /I "%MAVEN_HOME%" neq "" exit /b
set MAVEN_HOME=C:\Java\apache-maven-3.5.4
set PATH=%MAVEN_HOME%\bin;%PATH%
