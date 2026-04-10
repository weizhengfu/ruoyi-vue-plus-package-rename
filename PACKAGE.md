# RuoYi-Vue-Plus 包名修改器 - 打包指南

## 前置要求

### 必需软件

1. **JDK 14 或更高版本**
   - jpackage 工具需要 JDK 14+ 支持
   - 下载地址：https://adoptium.net/

2. **Maven 3.6+**
   - 下载地址：https://maven.apache.org/download.cgi

3. **Windows 用户（可选但推荐）**
   - WiX Toolset 3.0+（用于创建安装程序）
   - 下载地址：https://wixtoolset.org/
   - 安装后将 WiX 的 bin 目录添加到 PATH 环境变量

## 快速开始

### 方式一：使用打包脚本（推荐）

**Windows:**
```bash
# 双击运行
package.bat

# 或在命令行中运行
./package.bat
```

**Linux/macOS:**
```bash
# 赋予执行权限
chmod +x package.sh

# 运行
./package.sh
```

打包完成后，安装包位于：
- Windows: `installer/RuoYi包名修改器-1.0.0.exe`
- Linux: `installer/RuoYi包名修改器-1.0.0.rpm` (需要 WiX)
- macOS: `installer/RuoYi包名修改器-1.0.0.pkg`

### 方式二：手动打包

```bash
# 1. 编译并打包 JAR
mvn clean package

# 2. 使用 jpackage 创建 EXE（Windows 示例）
jpackage ^
  --type exe ^
  --name "RuoYi包名修改器" ^
  --dest installer ^
  --input target ^
  --main-jar ruoyi-vue-plus-package-rename-1.0-SNAPSHOT-jar-with-dependencies.jar ^
  --main-class cn.baruto.Main ^
  --app-version 1.0.0 ^
  --vendor "Baruto" ^
  --description "RuoYi-Vue-Plus 包名批量修改工具" ^
  --win-console
```

## 运行方式

### 开发运行

```bash
# GUI 模式（默认）
mvn exec:java -Dexec.mainClass="cn.baruto.Main"

# 或运行 JAR
java -jar target/ruoyi-vue-plus-package-rename-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### 命令行模式

```bash
# CLI 模式（使用配置文件）
java -jar target/ruoyi-vue-plus-package-rename-1.0-SNAPSHOT-jar-with-dependencies.jar --cli

# 或直接运行主类
mvn exec:java -Dexec.mainClass="cn.baruto.Main" -Dexec.args="--cli"
```

## 打包问题排查

### 问题 1：找不到 jpackage 命令

**原因**: JDK 版本低于 14

**解决方案**:
- 升级到 JDK 14 或更高版本
- 下载地址：https://adoptium.net/

### 问题 2：Windows 上 jpackage 报错 "wix tools are not found"

**原因**: 缺少 WiX Toolset

**解决方案**:
- 下载并安装 WiX Toolset: https://wixtoolset.org/
- 安装后确保 `candle.exe` 和 `light.exe` 在 PATH 中
- 临时解决：移除 `--type exe` 参数，jpackage 会创建目录结构而非 EXE

### 问题 3：Maven 编译失败

**解决方案**:
```bash
# 清理并重新编译
mvn clean
mvn install
```

### 问题 4：打包后的 EXE 运行报错

**可能原因**:
1. JAR 包没有包含所有依赖
2. JRE 版本不匹配

**解决方案**:
- 使用 maven-assembly-plugin 打包 fat JAR（已配置）
- 或者使用 --runtime-image 指定捆绑的 JRE

## 高级打包选项

### 捆绑 JRE（离线运行）

```bash
jpackage \
  --type exe \
  --name "RuoYi包名修改器" \
  --dest installer \
  --input target \
  --runtime-image <path-to-jre> \
  --main-jar ruoyi-vue-plus-package-rename-1.0-SNAPSHOT-jar-with-dependencies.jar \
  --main-class cn.baruto.Main \
  --app-version 1.0.0
```

### 自定义图标

```bash
jpackage \
  --type exe \
  --name "RuoYi包名修改器" \
  --dest installer \
  --input target \
  --icon src/main/resources/icon.ico \
  --main-jar ruoyi-vue-plus-package-rename-1.0-SNAPSHOT-jar-with-dependencies.jar \
  --main-class cn.baruto.Main \
  --app-version 1.0.0
```

### 更多 jpackage 参数

查看完整参数列表：
```bash
jpackage --help
```

## GUI 功能说明

### 界面组成

1. **源文件配置**
   - ZIP 文件路径：选择 RuoYi-Vue-Plus 源码的 ZIP 包
   - 输出目录：选择处理后的代码输出位置

2. **包名配置**
   - 目标包名：将 `org.dromara` 替换为目标包名（如 `com.example`）
   - 保留模块：逗号分隔，指定不替换包名的模块（如 `sms4j,warm`）

3. **项目配置**
   - 项目名称：新的项目名称
   - 项目版本：可选的项目版本号

4. **模块名映射配置**
   - 前缀替换：批量替换模块名前缀（如 `ruoyi` → `myapp`）
   - 精确映射：精确指定模块名映射（如 `ruoyi-admin:myapp-server`）

5. **处理日志**
   - 实时显示处理进度和日志
   - 进度条显示处理进度

### 使用流程

1. 点击"浏览..."选择 RuoYi-Vue-Plus ZIP 文件
2. 配置包名、项目名等参数
3. 点击"开始处理"按钮
4. 等待处理完成，查看输出目录

## 分发建议

### Windows 用户
- 直接分发 `installer/RuoYi包名修改器-1.0.0.exe`
- 用户双击安装即可使用

### Linux 用户
- 分发 RPM/DEB 包（需要相应 jpackage 支持）
- 或分发 tar.gz 包（包含 JRE）

### macOS 用户
- 分发 PKG 安装包
- 用户双击安装到应用程序文件夹

## 许可证

本工具基于 Apache 2.0 许可证开源。

## 技术支持

如有问题，请提交 Issue 或联系开发者。
