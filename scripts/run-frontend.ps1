$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$npm = Get-Command npm -ErrorAction SilentlyContinue
if ($npm) {
    $npmCmd = $npm.Source
} else {
    $npmCmd = Join-Path $root ".tools\node-v22.14.0-win-x64\npm.cmd"
}
if (-not (Test-Path $npmCmd)) { throw "未找到 npm。请安装 Node.js 20+，或保留项目中的 .tools 目录。" }
$nodeHome = Split-Path $npmCmd
$env:PATH = "$nodeHome;$env:PATH"
Set-Location (Join-Path $root "frontend")
if (-not (Test-Path "node_modules")) {
    & $npmCmd install --registry https://registry.npmmirror.com
}
& $npmCmd run dev
