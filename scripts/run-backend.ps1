$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
if (-not $env:JAVA_HOME) {
    $java = Get-Command java -ErrorAction SilentlyContinue
    if (-not $java) { throw "未找到 java，请安装 JDK 17 或更高版本并设置 JAVA_HOME" }
    $env:JAVA_HOME = (Resolve-Path (Join-Path (Split-Path $java.Source) "..")).Path
}
$mvn = Get-Command mvn -ErrorAction SilentlyContinue
if ($mvn) {
    $mvnCmd = $mvn.Source
} else {
    $mvnCmd = Join-Path $root ".tools\apache-maven-3.9.11\bin\mvn.cmd"
}
if (-not (Test-Path $mvnCmd)) { throw "未找到 mvn。请安装 Maven 3.6.3+，或保留项目中的 .tools 目录。" }
& $mvnCmd -f (Join-Path $root "backend\pom.xml") spring-boot:run @args
