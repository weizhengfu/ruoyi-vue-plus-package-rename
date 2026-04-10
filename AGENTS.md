# AGENTS.md

## 项目概述

这是一个用于批量修改 RuoYi-Vue-Plus 项目包名的 Java 工具。从 ZIP 包中提取代码，替换包名并重组目录结构。

支持两种运行模式：
1. **GUI 模式**（默认）：图形界面，可视化配置
2. **CLI 模式**：命令行模式，使用配置文件

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
# GUI 模式（默认）
mvn exec:java -Dexec.mainClass="cn.baruto.Main"

# CLI 模式（使用配置文件）
mvn exec:java -Dexec.mainClass="cn.baruto.Main" -Dexec.args="--cli"

# 运行打包后的 JAR
java -jar target/ruoyi-vue-plus-package-rename-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### 打包成 EXE
```bash
# Windows
package.bat

# Linux/macOS
./package.sh
```

详细打包说明请查看 [PACKAGE.md](./PACKAGE.md)

## 代码风格指南

### 包结构
- 主包名: `cn.baruto`
- 子包按功能分层:
  - `cn.baruto.config`: 配置加载和模型
  - `cn.baruto.core`: 核心逻辑（路径计算、内容替换、工具类）
  - `cn.baruto.processor`: 文件处理器
  - `cn.baruto.ui`: GUI 界面（Swing）

### 导入顺序
1. 内部导入（项目内包）
2. 外部库导入（Hutool, Apache Commons 等）
3. JDK 标准库导入
- 无通配符导入（不使用 `import java.util.*`）
- 必要时使用完全限定类名（如 `java.io.File.separator`）
- 导入组之间用空行分隔

### 格式规范
- 缩进: 4 个空格
- 左大括号与声明同行
- 方法之间用空行分隔
- 类内字段使用注释分组（如 `// === 源文件配置 ===`）
- 代码步骤用编号注释标注（如 `// 1. 加载并验证配置`）

### 命名约定
- 类名: PascalCase（RenameConfig, ConfigLoader, FileProcessor）
- 方法名: camelCase（load, processFile, calculateTargetPath）
- 字段: camelCase（zipFilePath, targetPackageName, contentReplacer）
- 常量: UPPER_SNAKE_CASE（如需使用）
- 局部变量: camelCase（tempFilePath, result, codeDir）
- 私有方法: camelCase，描述性名称（validate, parseRetainModules, isSpecialPath）

### 代码结构
- 类按功能组织：配置层、核心层、处理层
- 使用构造函数注入依赖
- 验证逻辑集中在加载器中
- 字符串操作和文件操作优先使用工具类方法
- 简单日志使用 System.out
- 每个方法单一职责

### 注释规范
- 类级别注释使用中文，简洁描述功能
- 方法注释使用中文，一句话说明用途
- 复杂逻辑添加内联注释
- 不需要详细的参数和返回值注释

### 异常处理
- 验证失败抛出 IllegalArgumentException
- 错误信息清晰明确
- 使用异常进行早期失败验证

## 依赖管理

### 核心依赖
- Hutool 5.8.28: 文件操作（FileUtil）、ZIP 解压（ZipUtil）、配置读取（Props）
- Apache Commons Lang3 3.14.0: 字符串工具

### 无测试依赖
项目当前无测试配置。

## 关键约定

1. **中文注释**: 所有注释使用中文
2. **UTF-8 编码**: 文件读写统一使用 UTF-8
3. **路径处理**: 使用 PathUtils.normalizePath 统一路径分隔符
4. **保留模块**: sms4j、warm 等模块保留原始包名
5. **配置驱动**: GUI 输入或通过 setting.properties 配置所有参数
6. **临时目录**: 使用 temp/ruoyi_<timestamp> 作为临时解压目录
7. **GUI 优先**: 优先使用 GUI 模式，CLI 模式通过 --cli 参数启动
