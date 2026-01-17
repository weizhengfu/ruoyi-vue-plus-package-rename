# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个用于批量修改 RuoYi-Vue-Plus 项目包名的工具。它从指定的 RuoYi-Vue-Plus ZIP 包中提取代码，将所有 `org.dromara` 包名替换为自定义包名，并重新组织目录结构。

## 构建与运行

### 构建项目
```bash
mvn clean compile
```

### 打包项目
```bash
mvn clean package
```

### 运行程序
```bash
# 直接运行 Main 类
mvn exec:java -Dexec.mainClass="cn.baruto.Main"

# 或先打包后运行 jar
java -jar target/ruoyi-vue-plus-package-rename-1.0-SNAPSHOT.jar
```

## 配置

在运行前，必须修改 [src/main/resources/setting.properties](src/main/resources/setting.properties) 文件：

```properties
# 要修改的目标包名
package.name=cn.xxx

# 输出目录路径（反斜杠需要转义）
target.path=D:\\test

# RuoYi-Vue-Plus 的 ZIP 包路径
zip.name=D:\\code\\RuoYi-Vue-Plus-5.X.zip
```

## 核心架构

### 核心组件架构

- **RenameConfig**: 配置模型类
- **ConfigLoader**: 配置加载与验证
- **ContentReplacer**: 内容替换引擎
- **PathCalculator**: 路径计算器
- **PathUtils**: 路径工具类
- **FileProcessor**: 文件处理器
- **Main**: 主入口

### 处理流程

1. ConfigLoader 加载并验证配置
2. ContentReplacer 根据模块类型处理内容替换
3. PathCalculator 计算目标文件路径
4. FileProcessor 执行文件读写操作

### 主要处理流程

程序的核心逻辑在 [Main.java](src/main/java/cn/baruto/Main.java) 中：

1. **解压阶段**：将 RuoYi-Vue-Plus ZIP 包解压到 `temp/ruoyi_<timestamp>` 临时目录
2. **遍历处理**：递归遍历所有文件，根据文件类型执行不同操作
3. **包名替换**：将所有文件中的 `org.dromara` 替换为目标包名
4. **目录重组**：重新构建符合新包名的目录结构（`src/main/java/<新包名>/`）
5. **输出清理**：将结果输出到带时间戳的目标目录，删除临时文件

### 文件处理策略

| 文件类型 | 处理方法 |
|---------|---------|
| `pom.xml` | 替换 `org.dromara`，但保留 `sms4j` 的原始包名 |
| `.java` | 替换包声明和 import 语句，保留 `sms4j` 原始包名 |
| `Mapper.xml` | 替换 namespace 和类型引用 |
| 其他文件 | 直接复制并替换内容中的包名引用 |

### 关键依赖

- **Hutool 5.8.28**：用于文件操作（`FileUtil`）、ZIP 解压（`ZipUtil`）、配置读取（`Props`）

### 特殊处理

`sms4j` 依赖需要保持原始包名 `org.dromara.sms4j`，代码中有特殊逻辑在替换后将 `sms4j` 的包名还原。

## 目录结构

```
src/
├── main/
│   ├── java/cn/baruto/
│   │   └── Main.java          # 主程序入口
│   └── resources/
│       └── setting.properties  # 配置文件
temp/                           # 临时解压目录（运行时生成）
target/                         # Maven 构建输出目录
```
