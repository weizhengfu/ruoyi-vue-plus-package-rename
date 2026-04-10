@echo off
chcp 65001 >nul
echo ========================================
echo RuoYi-Vue-Plus 包名修改器 - 打包脚本
echo ========================================
echo.

:: 检查 JDK 版本（需要 JDK 14+ 使用 jpackage）
java -version 2>&1 | findstr "version" >nul
if errorlevel 1 (
    echo [错误] 未找到 Java，请确保 JDK 已安装并配置到 PATH
    pause
    exit /b 1
)

:: 编译项目
echo [步骤 1] 编译项目...
call mvn clean package
if errorlevel 1 (
    echo [错误] Maven 编译失败
    pause
    exit /b 1
)
echo [成功] 编译完成
echo.

:: 清理旧的输出目录
if exist "installer" (
    echo [步骤 2] 清理旧的安装包目录...
    rmdir /s /q installer
)

:: 使用 jpackage 打包（需要 JDK 14+）
echo [步骤 3] 使用 jpackage 创建 EXE 安装包...
echo.

jpackage ^
  --type exe ^
  --name "RuoYi包名修改器" ^
  --dest installer ^
  --input target/classes ^
  --main-jar ruoyi-vue-plus-package-rename-1.0-SNAPSHOT.jar ^
  --main-class cn.baruto.Main ^
  --app-version 1.0.0 ^
  --vendor "Baruto" ^
  --description "RuoYi-Vue-Plus 包名批量修改工具" ^
  --win-console ^
  --win-dir-chooser ^
  --win-menu ^
  --win-shortcut

if errorlevel 1 (
    echo.
    echo [错误] jpackage 打包失败
    echo.
    echo 可能的原因：
    echo 1. JDK 版本低于 14（jpackage 需要 JDK 14+）
    echo 2. 找不到 Wix 工具集（Windows 上需要 WiX 3.0+）
    echo.
    echo 解决方案：
    echo 1. 升级到 JDK 14 或更高版本
    echo 2. 下载并安装 WiX: https://wixtoolset.org/
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo [成功] 打包完成！
echo ========================================
echo.
echo 安装包位置: installer\RuoYi包名修改器-1.0.0.exe
echo.
pause
