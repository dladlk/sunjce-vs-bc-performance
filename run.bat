@SET MAX_SIZE_MB=5
@SET JAR_PATH=target\sunjce-vs-bc-performance-1.0.jar
@REM goto j21

@SET JAVA_PATH="C:\Program Files\Java\jdk8u372-b07\bin\java.exe"
@%JAVA_PATH% -jar %JAR_PATH% %MAX_SIZE_MB%

:j12
@SET JAVA_PATH="C:\Program Files\Java\jdk-12\bin\java.exe"
@%JAVA_PATH% -jar %JAR_PATH% %MAX_SIZE_MB%

:j21

@SET JAVA_PATH="C:\Program Files\Java\jdk-21\bin\java.exe"
%JAVA_PATH% -jar %JAR_PATH% %MAX_SIZE_MB%