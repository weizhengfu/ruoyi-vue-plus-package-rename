# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

批量修改 RuoYi-Vue-Plus 项目包名的 Java 工具。从 ZIP 包提取代码，将 `org.dromara` 替换为自定义包名，重组目录结构，输出新项目。支持 GUI（默认）和 CLI 两种模式。

## 构建与运行

```bash
mvn clean compile                    # 编译
mvn clean package                    # 打包 fat JAR（assembly plugin）

# GUI 模式（默认，无参数启动）
mvn exec:java -Dexec.mainClass="cn.baruto.Main"

# CLI 模式
mvn exec:java -Dexec.mainClass="cn.baruto.Main" -Dexec.args="--cli"

# 运行打包后的 JAR
java -jar target/ruoyi-vue-plus-package-rename-1.0-SNAPSHOT-jar-with-dependencies.jar

# 测试
mvn test
mvn test -Dtest=ContentReplacerTest  # 单个测试类

# 打包 EXE（需要 JDK 14+ 和 WiX Toolset）
package.bat
```

## 配置

运行前修改 [setting.properties](src/main/resources/setting.properties)：

```properties
zip.name=D:\\code\\RuoYi-Vue-Plus-5.X.zip    # ZIP 包路径（反斜杠需转义）
package.name=com.yourcompany                   # 目标包名
package.retain=sms4j,warm                      # 保留原始包名的模块
project.name=MyProject                         # 项目名称
project.version=1.0.0                          # 可选版本号
target.path=D:\\output                         # 输出目录
module.prefix.old=ruoyi                        # 模块名前缀替换（旧）
module.prefix.new=myapp                        # 模块名前缀替换（新）
module.map=ruoyi-admin:myapp-server            # 精确映射（优先级更高）
```

## 架构

### 包结构

```
cn.baruto
├── config/     配置层：RenameConfig（POJO）、ConfigLoader（加载验证）
├── core/       核心层：ContentReplacer（替换引擎）、PathCalculator（路径计算）、PathUtils（路径工具）
├── processor/  处理层：FileProcessor（文件读写协调）
└── ui/         界面层：RenameApp（主窗口）、ModernUI（组件工厂）
```

### 数据流

```
ConfigLoader → RenameConfig
                    ↓
ContentReplacer ← config（包名/模块名替换）
PathCalculator  ← config（目录结构重组）
FileProcessor   ← replacer + pathCalculator（协调读写）
Main            → 解压ZIP → 遍历文件 → FileProcessor.processFile() → 输出
```

### 核心替换逻辑（ContentReplacer）

1. **包名替换**：用 `\borg\.dromara(?=\.)` 正则匹配，替换为目标包名
2. **保留还原**：将保留模块（sms4j、warm 等）的包名还原为 `org.dromara.xxx`
3. **模块名替换**：先精确映射（moduleMap），再前缀替换（oldPrefix→newPrefix）

### 模块名映射优先级

精确映射（`module.map`）> 前缀替换（`module.prefix.old/new`）。PathCalculator 在计算路径时也遵循此优先级。

### GUI 架构

RenameApp 使用 Swing + SwingWorker，ModernUI 提供统一的组件工厂方法（颜色、圆角边框、渐变面板）。GUI 配置直接映射到 RenameConfig 字段。

## 代码风格

- Java 8 兼容，Maven 构建
- 缩进 4 空格，左大括号同行
- 注释使用中文
- 导入顺序：项目内 → 外部库 → JDK 标准库，无通配符导入
- 依赖注入通过构造函数
- 异常使用 IllegalArgumentException 进行早期失败验证
- 文件操作统一 UTF-8 编码
- 路径统一通过 PathUtils.normalizePath 转为正斜杠

## 关键依赖

- Hutool 5.8.28：FileUtil、ZipUtil、Props
- Apache Commons Lang3 3.14.0：字符串工具
- SLF4J 2.0.9 + Logback 1.4.14：日志
- JUnit 5 + Mockito 5.7：测试

## gstack

Use the `/browse` skill from gstack for all web browsing. Never use `mcp__claude-in-chrome__*` tools.

Available skills:
- `/plan-ceo-review` - CEO/founder-mode plan review
- `/plan-eng-review` - Eng manager-mode plan review
- `/review` - Pre-landing PR review
- `/ship` - Ship workflow
- `/browse` - Fast headless browser for QA testing
- `/qa` - Systematically QA test and fix bugs
- `/setup-browser-cookies` - Import cookies from real browser
- `/retro` - Weekly engineering retrospective
