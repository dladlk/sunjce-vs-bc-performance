@SET MAX_SIZE_MB=5

@SET JAVA_PATH="C:\Program Files\Java\jdk8u372-b07\bin\java.exe"
@%JAVA_PATH% -jar target\jca-performance-comp-1.0.0-shaded.jar %MAX_SIZE_MB%

@SET JAVA_PATH="C:\Program Files\Java\jdk-12\bin\java.exe"
@%JAVA_PATH% -jar target\jca-performance-comp-1.0.0-shaded.jar %MAX_SIZE_MB%

@SET JAVA_PATH="C:\Program Files\Java\jdk-17\bin\java.exe"
@%JAVA_PATH% -jar target\jca-performance-comp-1.0.0-shaded.jar %MAX_SIZE_MB%
