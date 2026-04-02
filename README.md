## RuoYi-Vue-Plus包名修改器

> 支持一键修改RuoYi-Vue-Plus包名、模块名和项目配置

## 功能特性

- 批量替换包名（org.dromara → 自定义包名）
- 支持保留指定模块的原始包名（如 sms4j、warm）
- 支持模块名映射（前后缀替换或精确映射）
- 自定义项目名称和版本
- 自动处理 Java 文件、XML 文件、POM 文件等
- **新增**：图形用户界面（GUI），更易使用
- **新增**：支持打包成 EXE 可执行文件

## 使用方法

### 1. 下载 RuoYi-Vue-Plus 源码

访问：https://gitee.com/dromara/RuoYi-Vue-Plus
点击 `克隆/下载` 按钮，下载 ZIP 包

### 2. 修改配置文件

编辑 `src/main/resources/setting.properties`：

```properties
# === 源文件配置 ===
zip.name=D:\\code\\RuoYi-Vue-Plus-5.X.zip

# === 包名配置 ===
package.name=com.yourcompany
package.retain=sms4j,warm

# === 项目配置 ===
project.name=YourProject
project.version=1.0.0
target.path=D:\\test

# === 模块名映射配置 ===
# 前后缀替换
module.prefix.old=ruoyi
module.prefix.new=yourapp

# 精确映射（可选，优先级更高）
module.map=ruoyi-admin:yourapp-server,ruoyi-common:yourapp-core
```

### 3. 运行程序

#### GUI 模式（推荐）
```bash
# 直接运行，自动启动 GUI 界面
mvn exec:java -Dexec.mainClass="cn.baruto.Main"

# 或运行打包后的 JAR
java -jar target/ruoyi-vue-plus-package-rename-1.0-SNAPSHOT-jar-with-dependencies.jar
```

#### 命令行模式
```bash
# 使用 CLI 模式（需配置 setting.properties）
mvn exec:java -Dexec.mainClass="cn.baruto.Main" -Dexec.args="--cli"
```

### 4. 打包成 EXE（可选）

详细打包说明请查看 [PACKAGE.md](./PACKAGE.md)

快速打包（Windows）：
```bash
# 双击运行
package.bat
```

安装包生成位置：`installer/RuoYi包名修改器-1.0.0.exe`

**前置要求**：
- JDK 14+（jpackage 需要）
- Windows 用户推荐安装 WiX Toolset

### 5. 查看结果

输出目录结构：`target.path/project.name/version/`

## 配置说明

| 配置项 | 说明 | 示例 |
|-------|------|-----|
| zip.name | RuoYi-Vue-Plus ZIP 包路径 | D:\\code\\RuoYi-Vue-Plus-5.X.zip |
| package.name | 目标包名 | com.example |
| package.retain | 保留原始包名的模块 | sms4j,warm |
| project.name | 项目名称 | MyProject |
| project.version | 项目版本（可选） | 1.0.0 |
| target.path | 输出目录 | D:\\test |
| module.prefix.old | 旧模块名前缀 | ruoyi |
| module.prefix.new | 新模块名前缀 | myapp |
| module.map | 精确模块映射 | ruoyi-admin:myapp-server,ruoyi-common:myapp-core |
