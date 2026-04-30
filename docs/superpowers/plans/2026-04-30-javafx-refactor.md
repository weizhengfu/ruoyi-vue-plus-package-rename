# Swing → JavaFX 重构实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 UI 层从 Swing 完全替换为 JavaFX 17+，采用 FXML + CSS 侧边栏导航架构。

**Architecture:** FXML 定义布局，Controller 处理逻辑，CSS 控制样式。MainController 协调侧边栏切换和各子控制器。RenameService（javafx.concurrent.Service）负责后台处理。ConfigMapper 收集表单数据构建 RenameConfig。

**Tech Stack:** JavaFX 17（javafx-controls + javafx-fxml）, FXML, CSS, Maven

---

## 文件结构总览

| 操作 | 文件路径 | 职责 |
|------|---------|------|
| 修改 | `pom.xml` | JDK 17 + JavaFX 依赖 + 插件 |
| 修改 | `src/main/java/cn/baruto/Main.java` | 移除 Swing，改用 App.launch() |
| 删除 | `src/main/java/cn/baruto/ui/ModernUI.java` | Swing 组件工厂 |
| 删除 | `src/main/java/cn/baruto/ui/RenameApp.java` | Swing 主窗口 |
| 创建 | `src/main/java/cn/baruto/ui/App.java` | JavaFX Application 入口 |
| 创建 | `src/main/java/cn/baruto/ui/controller/MainController.java` | 主窗口控制器 |
| 创建 | `src/main/java/cn/baruto/ui/controller/SourceController.java` | 源文件配置页 |
| 创建 | `src/main/java/cn/baruto/ui/controller/PackageController.java` | 包名配置页 |
| 创建 | `src/main/java/cn/baruto/ui/controller/ProjectController.java` | 项目配置页 |
| 创建 | `src/main/java/cn/baruto/ui/controller/ModuleController.java` | 模块映射配置页 |
| 创建 | `src/main/java/cn/baruto/ui/controller/LogController.java` | 日志/进度页 |
| 创建 | `src/main/java/cn/baruto/ui/service/RenameService.java` | 后台处理服务 |
| 创建 | `src/main/java/cn/baruto/ui/util/ConfigMapper.java` | 表单→RenameConfig 映射 |
| 创建 | `src/main/resources/fxml/main.fxml` | 主布局（侧边栏+内容区） |
| 创建 | `src/main/resources/fxml/source.fxml` | 源文件配置页 |
| 创建 | `src/main/resources/fxml/package.fxml` | 包名配置页 |
| 创建 | `src/main/resources/fxml/project.fxml` | 项目配置页 |
| 创建 | `src/main/resources/fxml/module.fxml` | 模块映射配置页 |
| 创建 | `src/main/resources/fxml/log.fxml` | 日志/进度页 |
| 创建 | `src/main/resources/css/style.css` | 全局样式 |

---

### Task 1: 构建配置变更

**Files:**
- Modify: `pom.xml`

- [ ] **Step 1: 修改 pom.xml — JDK 版本、JavaFX 依赖、插件**

将 `pom.xml` 中的 JDK 目标从 8 改为 17，添加 JavaFX 依赖和插件。

替换 `<properties>` 中的编译版本：

```xml
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

在 `<dependencies>` 中添加（放在 hutool 之后）：

```xml
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>17.0.13</version>
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
    <version>17.0.13</version>
</dependency>
```

在 `<build><plugins>` 中添加（放在 assembly plugin 之后）：

```xml
<plugin>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-maven-plugin</artifactId>
    <version>0.0.8</version>
    <configuration>
        <mainClass>cn.baruto.ui.App</mainClass>
    </configuration>
</plugin>
```

- [ ] **Step 2: 验证编译通过**

Run: `mvn clean compile`
Expected: BUILD SUCCESS（此时 JavaFX 类还未创建，但依赖已下载）

- [ ] **Step 3: Commit**

```bash
git add pom.xml
git commit -m "build: 升级 JDK 17 并添加 JavaFX 依赖"
```

---

### Task 2: 删除旧 Swing UI 文件

**Files:**
- Delete: `src/main/java/cn/baruto/ui/ModernUI.java`
- Delete: `src/main/java/cn/baruto/ui/RenameApp.java`

- [ ] **Step 1: 删除 Swing 文件**

```bash
rm src/main/java/cn/baruto/ui/ModernUI.java
rm src/main/java/cn/baruto/ui/RenameApp.java
```

- [ ] **Step 2: 验证编译通过（CLI 模式仍可用）**

Run: `mvn clean compile`
Expected: BUILD SUCCESS（Main.java 此时不引用任何 ui 类，CLI 路径不受影响）

- [ ] **Step 3: Commit**

```bash
git add -u src/main/java/cn/baruto/ui/
git commit -m "refactor: 删除旧 Swing UI 文件"
```

---

### Task 3: 全局 CSS 样式

**Files:**
- Create: `src/main/resources/css/style.css`

- [ ] **Step 1: 创建目录并写入样式文件**

```bash
mkdir -p src/main/resources/css
```

创建 `src/main/resources/css/style.css`：

```css
/* === CSS 变量 === */
.root {
    -sidebar-bg: #1B2336;
    -sidebar-width: 200px;
    -content-bg: #F0F4F8;
    -card-bg: #FFFFFF;
    -accent: #3B82F6;
    -accent-hover: #2563EB;
    -success: #22C55E;
    -danger: #EF4444;
    -text-primary: #1E293B;
    -text-secondary: #64748B;
    -text-sidebar: #94A3B8;
    -text-sidebar-active: #FFFFFF;
    -sidebar-item-active-bg: #253350;
    -border: #CBD5E1;
    -border-focus: #3B82F6;
    -log-bg: #1E293B;
    -log-text: #A5D6A7;
    -radius: 8px;
}

/* === 侧边栏 === */
.sidebar {
    -fx-background-color: -sidebar-bg;
    -fx-pref-width: 200px;
    -fx-min-width: 200px;
}

.sidebar-logo-title {
    -fx-font-family: "Microsoft YaHei UI", "System";
    -fx-font-size: 16px;
    -fx-font-weight: bold;
    -fx-text-fill: white;
}

.sidebar-logo-subtitle {
    -fx-font-family: "Microsoft YaHei UI", "System";
    -fx-font-size: 11px;
    -fx-text-fill: #64748B;
}

.sidebar-nav-item {
    -fx-background-color: transparent;
    -fx-font-family: "Microsoft YaHei UI", "System";
    -fx-font-size: 13px;
    -fx-text-fill: -text-sidebar;
    -fx-cursor: hand;
    -fx-padding: 10 16 10 20;
    -fx-border-color: transparent transparent transparent transparent;
    -fx-border-width: 0 0 0 3px;
}

.sidebar-nav-item:hover {
    -fx-background-color: #1E2A3E;
    -fx-text-fill: white;
}

.sidebar-nav-item:selected {
    -fx-background-color: -sidebar-item-active-bg;
    -fx-text-fill: -text-sidebar-active;
    -fx-border-color: transparent transparent transparent -accent;
    -fx-border-width: 0 0 0 3px;
}

.sidebar-version {
    -fx-font-family: "Microsoft YaHei UI", "System";
    -fx-font-size: 11px;
    -fx-text-fill: #475569;
}

/* === 开始处理按钮 === */
.start-button {
    -fx-background-color: -accent;
    -fx-text-fill: white;
    -fx-font-family: "Microsoft YaHei UI", "System";
    -fx-font-size: 14px;
    -fx-font-weight: bold;
    -fx-background-radius: 6px;
    -fx-cursor: hand;
    -fx-padding: 10 0 10 0;
    -fx-pref-width: 160px;
}

.start-button:hover {
    -fx-background-color: -accent-hover;
}

.start-button:disabled {
    -fx-background-color: #94A3B8;
    -fx-opacity: 0.7;
}

/* === 内容区 === */
.content-area {
    -fx-background-color: -content-bg;
    -fx-padding: 24 30 24 30;
}

.page-title {
    -fx-font-family: "Microsoft YaHei UI", "System";
    -fx-font-size: 20px;
    -fx-font-weight: bold;
    -fx-text-fill: -accent;
}

.page-desc {
    -fx-font-family: "Microsoft YaHei UI", "System";
    -fx-font-size: 13px;
    -fx-text-fill: -text-secondary;
    -fx-padding: 4 0 16 0;
}

/* === 表单卡片 === */
.form-card {
    -fx-background-color: -card-bg;
    -fx-background-radius: -radius;
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);
    -fx-padding: 24;
    -fx-spacing: 16;
}

.form-label {
    -fx-font-family: "Microsoft YaHei UI", "System";
    -fx-font-size: 13px;
    -fx-font-weight: bold;
    -fx-text-fill: -text-primary;
}

.form-label-required {
    -fx-font-family: "Microsoft YaHei UI", "System";
    -fx-font-size: 13px;
    -fx-font-weight: bold;
    -fx-text-fill: -text-primary;
}

.form-input {
    -fx-font-family: "Microsoft YaHei UI", "System";
    -fx-font-size: 13px;
    -fx-text-fill: -text-primary;
    -fx-background-color: white;
    -fx-background-radius: 6px;
    -fx-border-color: -border;
    -fx-border-radius: 6px;
    -fx-border-width: 1px;
    -fx-padding: 8 12 8 12;
    -fx-pref-height: 36px;
}

.form-input:focused {
    -fx-border-color: -border-focus;
    -fx-effect: dropshadow(gaussian, rgba(59,130,246,0.15), 6, 0, 0, 0);
}

.form-hint {
    -fx-font-family: "Microsoft YaHei UI", "System";
    -fx-font-size: 11px;
    -fx-text-fill: #94A3B8;
}

.browse-button {
    -fx-background-color: white;
    -fx-text-fill: -text-primary;
    -fx-font-family: "Microsoft YaHei UI", "System";
    -fx-font-size: 13px;
    -fx-background-radius: 6px;
    -fx-border-color: -border;
    -fx-border-radius: 6px;
    -fx-border-width: 1px;
    -fx-padding: 8 16 8 16;
    -fx-cursor: hand;
    -fx-pref-height: 36px;
}

.browse-button:hover {
    -fx-background-color: #F8FAFC;
    -fx-border-color: -accent;
}

/* === 日志区域 === */
.log-area {
    -fx-font-family: "Consolas", "Monospace";
    -fx-font-size: 12px;
    -fx-text-fill: -log-text;
    -fx-background-color: -log-bg;
    -fx-background-radius: 6px;
    -fx-border-color: #334155;
    -fx-border-radius: 6px;
    -fx-border-width: 1px;
    -fx-padding: 12;
    -fx-highlight-fill: #334155;
    -fx-highlight-text-fill: -log-text;
}

.log-title {
    -fx-font-family: "Microsoft YaHei UI", "System";
    -fx-font-size: 14px;
    -fx-font-weight: bold;
    -fx-text-fill: -accent;
}

.progress-bar {
    -fx-accent: -success;
}

.progress-bar .track {
    -fx-background-color: #E2E8F0;
    -fx-background-radius: 6px;
}

.progress-bar .bar {
    -fx-background-color: -success;
    -fx-background-radius: 6px;
    -fx-background-insets: 0;
}

.progress-label {
    -fx-font-family: "Microsoft YaHei UI", "System";
    -fx-font-size: 12px;
    -fx-font-weight: bold;
    -fx-text-fill: -text-secondary;
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/resources/css/style.css
git commit -m "style: 添加 JavaFX 全局 CSS 样式"
```

---

### Task 4: FXML 布局文件

**Files:**
- Create: `src/main/resources/fxml/main.fxml`
- Create: `src/main/resources/fxml/source.fxml`
- Create: `src/main/resources/fxml/package.fxml`
- Create: `src/main/resources/fxml/project.fxml`
- Create: `src/main/resources/fxml/module.fxml`
- Create: `src/main/resources/fxml/log.fxml`

- [ ] **Step 1: 创建目录**

```bash
mkdir -p src/main/resources/fxml
```

- [ ] **Step 2: 创建 main.fxml**

创建 `src/main/resources/fxml/main.fxml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.Button?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.layout.AnchorPane?>
<?import javafx.scene.layout.BorderPane?>
<?import javafx.scene.layout.HBox?>
<?import javafx.scene.layout.VBox?>

<BorderPane xmlns:fx="http://javafx.com/fxml"
            fx:controller="cn.baruto.ui.controller.MainController"
            stylesheets="@../css/style.css"
            prefWidth="960" prefHeight="650"
            minWidth="900" minHeight="650">

    <!-- 左侧边栏 -->
    <left>
        <VBox styleClass="sidebar" fx:id="sidebar">
            <!-- Logo 区域 -->
            <VBox styleClass="sidebar-logo" alignment="CENTER_LEFT"
                  VBox.vgrow="NEVER" style="-fx-padding: 20 0 10 20;">
                <Label styleClass="sidebar-logo-title" text="包名修改器"/>
                <Label styleClass="sidebar-logo-subtitle" text="RuoYi-Vue-Plus"/>
            </VBox>

            <!-- 分隔线 -->
            <HBox style="-fx-background-color: #2D3A4F; -fx-pref-height: 1px;
                         -fx-margin: 8 16 8 16;"/>

            <!-- 导航项 -->
            <VBox fx:id="navContainer" VBox.vgrow="ALWAYS" spacing="2"
                  style="-fx-padding: 8 0 0 0;">
                <Button fx:id="navSource" styleClass="sidebar-nav-item"
                        text="源文件" onAction="#onNavClick"
                        maxWidth="Infinity" alignment="BASELINE_LEFT"/>
                <Button fx:id="navPackage" styleClass="sidebar-nav-item"
                        text="包名" onAction="#onNavClick"
                        maxWidth="Infinity" alignment="BASELINE_LEFT"/>
                <Button fx:id="navProject" styleClass="sidebar-nav-item"
                        text="项目" onAction="#onNavClick"
                        maxWidth="Infinity" alignment="BASELINE_LEFT"/>
                <Button fx:id="navModule" styleClass="sidebar-nav-item"
                        text="模块映射" onAction="#onNavClick"
                        maxWidth="Infinity" alignment="BASELINE_LEFT"/>
                <Button fx:id="navLog" styleClass="sidebar-nav-item"
                        text="处理日志" onAction="#onNavClick"
                        maxWidth="Infinity" alignment="BASELINE_LEFT"/>
            </VBox>

            <!-- 底部区域 -->
            <VBox alignment="CENTER" spacing="8" VBox.vgrow="NEVER"
                  style="-fx-padding: 16 20 20 20;">
                <Button fx:id="startButton" styleClass="start-button"
                        text="开始处理" onAction="#onStartProcessing"/>
                <Label styleClass="sidebar-version" text="v1.0"/>
            </VBox>
        </VBox>
    </left>

    <!-- 右侧内容区 -->
    <center>
        <AnchorPane fx:id="contentArea" styleClass="content-area"/>
    </center>
</BorderPane>
```

- [ ] **Step 3: 创建 source.fxml**

创建 `src/main/resources/fxml/source.fxml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.Button?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.control.TextField?>
<?import javafx.scene.layout.HBox?>
<?import javafx.scene.layout.VBox?>

<VBox xmlns:fx="http://javafx.com/fxml"
      fx:controller="cn.baruto.ui.controller.SourceController"
      styleClass="content-area" spacing="0">

    <Label styleClass="page-title" text="源文件配置"/>
    <Label styleClass="page-desc" text="选择 RuoYi-Vue-Plus 的 ZIP 包和输出目录"/>

    <!-- 卡片 -->
    <VBox styleClass="form-card" spacing="16" maxWidth="Infinity" VBox.vgrow="ALWAYS">
        <!-- ZIP 路径 -->
        <VBox spacing="6">
            <Label styleClass="form-label-required" text="ZIP 文件 *"/>
            <HBox spacing="8" alignment="CENTER_LEFT">
                <TextField fx:id="zipPathField" styleClass="form-input"
                           HBox.hgrow="ALWAYS" promptText="选择 RuoYi-Vue-Plus ZIP 文件"/>
                <Button styleClass="browse-button" text="浏览..."
                        onAction="#onBrowseZip"/>
            </HBox>
            <Label styleClass="form-hint" text="从 Gitee 下载的 RuoYi-Vue-Plus ZIP 包"/>
        </VBox>

        <!-- 输出目录 -->
        <VBox spacing="6">
            <Label styleClass="form-label-required" text="输出目录 *"/>
            <HBox spacing="8" alignment="CENTER_LEFT">
                <TextField fx:id="targetPathField" styleClass="form-input"
                           HBox.hgrow="ALWAYS" promptText="选择输出目录"/>
                <Button styleClass="browse-button" text="浏览..."
                        onAction="#onBrowseTarget"/>
            </HBox>
            <Label styleClass="form-hint" text="输出路径: 输出目录/项目名/版本号/"/>
        </VBox>
    </VBox>
</VBox>
```

- [ ] **Step 4: 创建 package.fxml**

创建 `src/main/resources/fxml/package.fxml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.Label?>
<?import javafx.scene.control.TextField?>
<?import javafx.scene.layout.VBox?>

<VBox xmlns:fx="http://javafx.com/fxml"
      fx:controller="cn.baruto.ui.controller.PackageController"
      styleClass="content-area" spacing="0">

    <Label styleClass="page-title" text="包名配置"/>
    <Label styleClass="page-desc" text="设置目标包名和需要保留原始包名的模块"/>

    <VBox styleClass="form-card" spacing="16" maxWidth="Infinity" VBox.vgrow="ALWAYS">
        <!-- 目标包名 -->
        <VBox spacing="6">
            <Label styleClass="form-label-required" text="目标包名 *"/>
            <TextField fx:id="packageNameField" styleClass="form-input"
                       promptText="com.example" text="com.example"/>
            <Label styleClass="form-hint" text="将替换所有 org.dromara 包名"/>
        </VBox>

        <!-- 保留模块 -->
        <VBox spacing="6">
            <Label styleClass="form-label" text="保留模块"/>
            <TextField fx:id="retainModulesField" styleClass="form-input"
                       promptText="逗号分隔" text="sms4j,warm"/>
            <Label styleClass="form-hint" text="保留原始包名的模块，逗号分隔，如 sms4j,warm"/>
        </VBox>
    </VBox>
</VBox>
```

- [ ] **Step 5: 创建 project.fxml**

创建 `src/main/resources/fxml/project.fxml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.Label?>
<?import javafx.scene.control.TextField?>
<?import javafx.scene.layout.HBox?>
<?import javafx.scene.layout.VBox?>

<VBox xmlns:fx="http://javafx.com/fxml"
      fx:controller="cn.baruto.ui.controller.ProjectController"
      styleClass="content-area" spacing="0">

    <Label styleClass="page-title" text="项目配置"/>
    <Label styleClass="page-desc" text="设置项目名称和版本号"/>

    <VBox styleClass="form-card" spacing="16" maxWidth="Infinity" VBox.vgrow="ALWAYS">
        <!-- 项目名称 -->
        <VBox spacing="6">
            <Label styleClass="form-label-required" text="项目名称 *"/>
            <TextField fx:id="projectNameField" styleClass="form-input"
                       promptText="MyProject" text="MyProject"/>
            <Label styleClass="form-hint" text="输出目录的子文件夹名称"/>
        </VBox>

        <!-- 项目版本 -->
        <VBox spacing="6">
            <Label styleClass="form-label" text="项目版本"/>
            <TextField fx:id="projectVersionField" styleClass="form-input"
                       promptText="1.0.0（可选）" text="1.0.0"/>
            <Label styleClass="form-hint" text="留空则不创建版本子目录"/>
        </VBox>
    </VBox>
</VBox>
```

- [ ] **Step 6: 创建 module.fxml**

创建 `src/main/resources/fxml/module.fxml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.Label?>
<?import javafx.scene.control.TextField?>
<?import javafx.scene.layout.HBox?>
<?import javafx.scene.layout.VBox?>

<VBox xmlns:fx="http://javafx.com/fxml"
      fx:controller="cn.baruto.ui.controller.ModuleController"
      styleClass="content-area" spacing="0">

    <Label styleClass="page-title" text="模块名映射配置"/>
    <Label styleClass="page-desc" text="配置模块名的替换规则，用于重命名模块目录"/>

    <VBox styleClass="form-card" spacing="16" maxWidth="Infinity" VBox.vgrow="ALWAYS">
        <!-- 前缀替换 -->
        <VBox spacing="6">
            <Label styleClass="form-label" text="前缀替换"/>
            <HBox spacing="12" alignment="CENTER_LEFT">
                <TextField fx:id="oldPrefixField" styleClass="form-input"
                           promptText="旧前缀" text="ruoyi" prefWidth="180"/>
                <Label text="→" style="-fx-font-size: 16; -fx-text-fill: #94A3B8;
                       -fx-font-weight: bold;"/>
                <TextField fx:id="newPrefixField" styleClass="form-input"
                           promptText="新前缀" text="myapp" prefWidth="180"/>
            </HBox>
            <Label styleClass="form-hint" text="将所有匹配前缀的模块名替换为新前缀"/>
        </VBox>

        <!-- 精确映射 -->
        <VBox spacing="6">
            <Label styleClass="form-label" text="精确映射"/>
            <TextField fx:id="moduleMapField" styleClass="form-input"
                       promptText="旧名:新名,旧名:新名"/>
            <Label styleClass="form-hint" text="格式: 旧模块名:新模块名，逗号分隔。优先级高于前缀替换"/>
        </VBox>
    </VBox>
</VBox>
```

- [ ] **Step 7: 创建 log.fxml**

创建 `src/main/resources/fxml/log.fxml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.Label?>
<?import javafx.scene.control.ProgressBar?>
<?import javafx.scene.control.TextArea?>
<?import javafx.scene.layout.VBox?>

<VBox xmlns:fx="http://javafx.com/fxml"
      fx:controller="cn.baruto.ui.controller.LogController"
      styleClass="content-area" spacing="0">

    <Label styleClass="page-title" text="处理日志"/>
    <Label styleClass="page-desc" text="查看包名修改的实时处理进度"/>

    <VBox styleClass="form-card" spacing="12" maxWidth="Infinity" VBox.vgrow="ALWAYS">
        <!-- 日志标题 -->
        <Label styleClass="log-title" text="运行日志"/>

        <!-- 日志文本区域 -->
        <TextArea fx:id="logArea" styleClass="log-area"
                  editable="false" wrapText="true"
                  VBox.vgrow="ALWAYS" prefRowCount="18"/>

        <!-- 进度条 -->
        <VBox spacing="6">
            <ProgressBar fx:id="progressBar" styleClass="progress-bar"
                         prefWidth="Infinity" progress="0" visible="false"/>
            <Label fx:id="progressLabel" styleClass="progress-label" text="就绪"/>
        </VBox>
    </VBox>
</VBox>
```

- [ ] **Step 8: Commit**

```bash
git add src/main/resources/fxml/
git commit -m "feat: 添加所有 FXML 布局文件"
```

---

### Task 5: ConfigMapper 工具类

**Files:**
- Create: `src/main/java/cn/baruto/ui/util/ConfigMapper.java`

- [ ] **Step 1: 创建目录**

```bash
mkdir -p src/main/java/cn/baruto/ui/util
```

- [ ] **Step 2: 创建 ConfigMapper.java**

创建 `src/main/java/cn/baruto/ui/util/ConfigMapper.java`：

```java
package cn.baruto.ui.util;

import cn.baruto.config.RenameConfig;
import cn.baruto.ui.controller.ModuleController;
import cn.baruto.ui.controller.PackageController;
import cn.baruto.ui.controller.ProjectController;
import cn.baruto.ui.controller.SourceController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConfigMapper {

    public static RenameConfig buildConfig(SourceController source,
                                           PackageController pkg,
                                           ProjectController project,
                                           ModuleController module) {
        RenameConfig config = new RenameConfig();
        config.setZipFilePath(source.getZipPath());
        config.setTargetPath(source.getTargetPath());
        config.setTargetPackageName(pkg.getPackageName());
        config.setRetainModules(parseRetainModules(pkg.getRetainModules()));
        config.setProjectName(project.getProjectName());
        config.setProjectVersion(project.getProjectVersion());
        config.setOldPrefix(module.getOldPrefix());
        config.setNewPrefix(module.getNewPrefix());
        config.setModuleMap(parseModuleMap(module.getModuleMap()));
        return config;
    }

    private static Set<String> parseRetainModules(String value) {
        if (value == null || value.trim().isEmpty()) {
            return java.util.Collections.emptySet();
        }
        return new HashSet<>(Arrays.asList(value.trim().split(",")));
    }

    private static Map<String, String> parseModuleMap(String value) {
        Map<String, String> map = new HashMap<>();
        if (value == null || value.trim().isEmpty()) {
            return map;
        }
        String[] pairs = value.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":");
            if (kv.length == 2) {
                map.put(kv[0].trim(), kv[1].trim());
            }
        }
        return map;
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/cn/baruto/ui/util/ConfigMapper.java
git commit -m "feat: 添加 ConfigMapper 表单→配置映射工具"
```

---

### Task 6: 子控制器（Source、Package、Project、Module）

**Files:**
- Create: `src/main/java/cn/baruto/ui/controller/SourceController.java`
- Create: `src/main/java/cn/baruto/ui/controller/PackageController.java`
- Create: `src/main/java/cn/baruto/ui/controller/ProjectController.java`
- Create: `src/main/java/cn/baruto/ui/controller/ModuleController.java`

- [ ] **Step 1: 创建目录**

```bash
mkdir -p src/main/java/cn/baruto/ui/controller
```

- [ ] **Step 2: 创建 SourceController.java**

创建 `src/main/java/cn/baruto/ui/controller/SourceController.java`：

```java
package cn.baruto.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class SourceController {

    @FXML
    private TextField zipPathField;

    @FXML
    private TextField targetPathField;

    @FXML
    public void onBrowseZip() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("ZIP 文件 (*.zip)", "*.zip"));
        chooser.setTitle("选择 RuoYi-Vue-Plus ZIP 文件");
        File file = chooser.showOpenDialog(getStage());
        if (file != null) {
            zipPathField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    public void onBrowseTarget() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("选择输出目录");
        File dir = chooser.showDialog(getStage());
        if (dir != null) {
            targetPathField.setText(dir.getAbsolutePath());
        }
    }

    public String getZipPath() {
        return zipPathField.getText().trim();
    }

    public String getTargetPath() {
        return targetPathField.getText().trim();
    }

    private Stage getStage() {
        return (Stage) zipPathField.getScene().getWindow();
    }
}
```

- [ ] **Step 3: 创建 PackageController.java**

创建 `src/main/java/cn/baruto/ui/controller/PackageController.java`：

```java
package cn.baruto.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class PackageController {

    @FXML
    private TextField packageNameField;

    @FXML
    private TextField retainModulesField;

    public String getPackageName() {
        return packageNameField.getText().trim();
    }

    public String getRetainModules() {
        return retainModulesField.getText().trim();
    }
}
```

- [ ] **Step 4: 创建 ProjectController.java**

创建 `src/main/java/cn/baruto/ui/controller/ProjectController.java`：

```java
package cn.baruto.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class ProjectController {

    @FXML
    private TextField projectNameField;

    @FXML
    private TextField projectVersionField;

    public String getProjectName() {
        return projectNameField.getText().trim();
    }

    public String getProjectVersion() {
        return projectVersionField.getText().trim();
    }
}
```

- [ ] **Step 5: 创建 ModuleController.java**

创建 `src/main/java/cn/baruto/ui/controller/ModuleController.java`：

```java
package cn.baruto.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class ModuleController {

    @FXML
    private TextField oldPrefixField;

    @FXML
    private TextField newPrefixField;

    @FXML
    private TextField moduleMapField;

    public String getOldPrefix() {
        return oldPrefixField.getText().trim();
    }

    public String getNewPrefix() {
        return newPrefixField.getText().trim();
    }

    public String getModuleMap() {
        return moduleMapField.getText().trim();
    }
}
```

- [ ] **Step 6: Commit**

```bash
git add src/main/java/cn/baruto/ui/controller/
git commit -m "feat: 添加四个配置页控制器"
```

---

### Task 7: LogController

**Files:**
- Create: `src/main/java/cn/baruto/ui/controller/LogController.java`

- [ ] **Step 1: 创建 LogController.java**

创建 `src/main/java/cn/baruto/ui/controller/LogController.java`：

```java
package cn.baruto.ui.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;

public class LogController {

    @FXML
    private TextArea logArea;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label progressLabel;

    public void appendLog(String message) {
        if (Platform.isFxApplicationThread()) {
            logArea.appendText(message);
        } else {
            Platform.runLater(() -> logArea.appendText(message));
        }
    }

    public void clearLog() {
        logArea.clear();
    }

    public void setProgress(double progress) {
        if (Platform.isFxApplicationThread()) {
            progressBar.setProgress(progress);
            progressLabel.setText(String.format("%.0f%%", progress * 100));
        } else {
            Platform.runLater(() -> {
                progressBar.setProgress(progress);
                progressLabel.setText(String.format("%.0f%%", progress * 100));
            });
        }
    }

    public void showProgressBar(boolean visible) {
        progressBar.setVisible(visible);
    }

    public void setProgressLabel(String text) {
        progressLabel.setText(text);
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/cn/baruto/ui/controller/LogController.java
git commit -m "feat: 添加 LogController 日志和进度控制器"
```

---

### Task 8: RenameService

**Files:**
- Create: `src/main/java/cn/baruto/ui/service/RenameService.java`

- [ ] **Step 1: 创建目录**

```bash
mkdir -p src/main/java/cn/baruto/ui/service
```

- [ ] **Step 2: 创建 RenameService.java**

创建 `src/main/java/cn/baruto/ui/service/RenameService.java`：

```java
package cn.baruto.ui.service;

import cn.baruto.config.RenameConfig;
import cn.baruto.core.ContentReplacer;
import cn.baruto.core.PathCalculator;
import cn.baruto.processor.FileProcessor;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.File;
import java.util.List;

public class RenameService extends Service<Void> {

    private RenameConfig config;

    public void setConfig(RenameConfig config) {
        this.config = config;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                // 1. 初始化处理器
                updateMessage("正在初始化处理器...\n");
                ContentReplacer contentReplacer = new ContentReplacer(config);
                PathCalculator pathCalculator = new PathCalculator(config);
                FileProcessor fileProcessor = new FileProcessor(contentReplacer, pathCalculator, config);
                updateMessage("处理器初始化完成\n");

                // 2. 解压文件
                updateMessage("正在解压 ZIP 文件: " + config.getZipFilePath() + "\n");
                String codeDir = unzip(config.getZipFilePath());
                File codeDirFile = new File(codeDir);
                updateMessage("解压完成，临时目录: " + codeDir + "\n");

                // 3. 遍历处理文件
                List<File> files = FileUtil.loopFiles(codeDirFile);
                int total = files.size();
                updateMessage("共找到 " + total + " 个文件\n\n");

                int processed = 0;
                for (File file : files) {
                    fileProcessor.processFile(file, codeDirFile);
                    processed++;
                    if (processed % 50 == 0) {
                        updateMessage("  已处理 " + processed + "/" + total + " 个文件\n");
                        updateProgress(processed, total);
                    }
                }

                updateProgress(total, total);
                updateMessage("\n所有文件处理完成\n");
                updateMessage("输出目录: " + config.getOutputDirectory() + "\n");

                // 4. 清理临时文件
                updateMessage("正在清理临时文件...\n");
                FileUtil.del(codeDirFile);
                updateMessage("清理完成\n");

                return null;
            }
        };
    }

    private String unzip(String zipFilePath) {
        String tempFilePath = System.getProperty("user.dir") + "/temp/ruoyi_" + System.currentTimeMillis();
        File zipFile = new File(zipFilePath);
        String suffix = "." + FileUtil.extName(zipFilePath);
        String zipFileName = FileUtil.getName(zipFilePath).replaceAll(suffix, "");
        File targetDir = new File(tempFilePath);
        targetDir.mkdirs();
        ZipUtil.unzip(zipFile, targetDir);
        return tempFilePath + "/" + zipFileName;
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/cn/baruto/ui/service/RenameService.java
git commit -m "feat: 添加 RenameService 后台处理服务"
```

---

### Task 9: MainController（核心控制器）

**Files:**
- Create: `src/main/java/cn/baruto/ui/controller/MainController.java`

- [ ] **Step 1: 创建 MainController.java**

创建 `src/main/java/cn/baruto/ui/controller/MainController.java`：

```java
package cn.baruto.ui.controller;

import cn.baruto.config.RenameConfig;
import cn.baruto.ui.service.RenameService;
import cn.baruto.ui.util.ConfigMapper;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainController {

    @FXML private AnchorPane contentArea;
    @FXML private Button navSource;
    @FXML private Button navPackage;
    @FXML private Button navProject;
    @FXML private Button navModule;
    @FXML private Button navLog;
    @FXML private Button startButton;

    private SourceController sourceController;
    private PackageController packageController;
    private ProjectController projectController;
    private ModuleController moduleController;
    private LogController logController;

    private final Map<Button, Pane> navPages = new HashMap<>();
    private Button currentNav;
    private final RenameService renameService = new RenameService();

    @FXML
    public void initialize() {
        // 加载所有子页面
        try {
            FXMLLoader sourceLoader = new FXMLLoader(
                getClass().getResource("/fxml/source.fxml"));
            Pane sourcePage = sourceLoader.load();
            sourceController = sourceLoader.getController();

            FXMLLoader packageLoader = new FXMLLoader(
                getClass().getResource("/fxml/package.fxml"));
            Pane packagePage = packageLoader.load();
            packageController = packageLoader.getController();

            FXMLLoader projectLoader = new FXMLLoader(
                getClass().getResource("/fxml/project.fxml"));
            Pane projectPage = projectLoader.load();
            projectController = projectLoader.getController();

            FXMLLoader moduleLoader = new FXMLLoader(
                getClass().getResource("/fxml/module.fxml"));
            Pane modulePage = moduleLoader.load();
            moduleController = moduleLoader.getController();

            FXMLLoader logLoader = new FXMLLoader(
                getClass().getResource("/fxml/log.fxml"));
            Pane logPage = logLoader.load();
            logController = logLoader.getController();

            navPages.put(navSource, sourcePage);
            navPages.put(navPackage, packagePage);
            navPages.put(navProject, projectPage);
            navPages.put(navModule, modulePage);
            navPages.put(navLog, logPage);

            // 绑定 RenameService 消息到日志区域
            renameService.messageProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    logController.appendLog(newVal);
                }
            });
            renameService.progressProperty().addListener((obs, oldVal, newVal) -> {
                logController.setProgress(newVal.doubleValue());
            });
            renameService.runningProperty().addListener((obs, oldVal, newVal) -> {
                startButton.setDisabled(newVal);
                startButton.setText(newVal ? "处理中..." : "开始处理");
            });
            renameService.setOnSucceeded(e -> {
                logController.setProgressLabel("处理完成");
                logController.appendLog("\n任务成功完成\n");
                showAlert(Alert.AlertType.INFORMATION, "成功", "处理完成！");
            });
            renameService.setOnFailed(e -> {
                logController.setProgressLabel("处理失败");
                String error = renameService.getException() != null
                    ? renameService.getException().getMessage() : "未知错误";
                logController.appendLog("\n错误: " + error + "\n");
                showAlert(Alert.AlertType.ERROR, "错误", "处理失败: " + error);
            });

            // 默认选中第一个导航项
            switchPage(navSource);

        } catch (Exception e) {
            throw new RuntimeException("加载 FXML 失败", e);
        }
    }

    @FXML
    public void onNavClick(javafx.event.ActionEvent event) {
        Button clicked = (Button) event.getSource();
        switchPage(clicked);
    }

    @FXML
    public void onStartProcessing(javafx.event.ActionEvent event) {
        if (!validateInputs()) {
            return;
        }

        RenameConfig config = ConfigMapper.buildConfig(
            sourceController, packageController, projectController, moduleController);
        renameService.setConfig(config);

        logController.clearLog();
        logController.showProgressBar(true);
        logController.setProgress(0);
        logController.setProgressLabel("正在处理...");
        logController.appendLog("════════════════════════════════════════\n");
        logController.appendLog("  开始处理任务\n");
        logController.appendLog("════════════════════════════════════════\n\n");

        // 自动切换到日志页
        switchPage(navLog);

        renameService.restart();
    }

    private void switchPage(Button nav) {
        if (currentNav == nav) return;

        // 取消旧选中
        if (currentNav != null) {
            currentNav.getStyleClass().remove("selected");
        }

        // 设置新选中
        nav.getStyleClass().add("selected");
        currentNav = nav;

        // 切换内容
        Pane page = navPages.get(nav);
        contentArea.getChildren().clear();

        // 淡入动画
        page.setOpacity(0);
        AnchorPane.setTopAnchor(page, 0.0);
        AnchorPane.setBottomAnchor(page, 0.0);
        AnchorPane.setLeftAnchor(page, 0.0);
        AnchorPane.setRightAnchor(page, 0.0);
        contentArea.getChildren().add(page);

        FadeTransition fade = new FadeTransition(Duration.millis(200), page);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private boolean validateInputs() {
        String zipPath = sourceController.getZipPath();
        if (zipPath.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "验证错误", "请选择 ZIP 文件");
            return false;
        }
        if (!new File(zipPath).exists()) {
            showAlert(Alert.AlertType.WARNING, "验证错误", "ZIP 文件不存在");
            return false;
        }
        if (packageController.getPackageName().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "验证错误", "请输入目标包名");
            return false;
        }
        if (projectController.getProjectName().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "验证错误", "请输入项目名称");
            return false;
        }
        if (sourceController.getTargetPath().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "验证错误", "请选择输出目录");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/cn/baruto/ui/controller/MainController.java
git commit -m "feat: 添加 MainController 主控制器（侧边栏切换+处理协调）"
```

---

### Task 10: App 入口 + Main.java 修改

**Files:**
- Create: `src/main/java/cn/baruto/ui/App.java`
- Modify: `src/main/java/cn/baruto/Main.java`

- [ ] **Step 1: 创建 App.java**

创建 `src/main/java/cn/baruto/ui/App.java`：

```java
package cn.baruto.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        primaryStage.setTitle("RuoYi-Vue-Plus 包名修改器");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(650);
        primaryStage.setWidth(960);
        primaryStage.setHeight(680);
        primaryStage.centerOnScreen();

        try {
            Image icon = new Image(getClass().getResourceAsStream("/icon.png"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            // 忽略图标加载失败
        }

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
```

- [ ] **Step 2: 修改 Main.java**

将整个 Main.java 替换为：

```java
package cn.baruto;

import cn.baruto.config.ConfigLoader;
import cn.baruto.config.RenameConfig;
import cn.baruto.core.ContentReplacer;
import cn.baruto.core.PathCalculator;
import cn.baruto.processor.FileProcessor;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        if (args.length == 0 || !args[0].equals("--cli")) {
            cn.baruto.ui.App.launch(cn.baruto.ui.App.class, args);
            return;
        }
        startCLIMode();
    }

    private static void startCLIMode() {
        logger.info("开始 RuoYi-Vue-Plus 包名修改工具...");

        RenameConfig config = ConfigLoader.load("setting.properties");
        logger.info("配置加载完成");

        ContentReplacer contentReplacer = new ContentReplacer(config);
        PathCalculator pathCalculator = new PathCalculator(config);
        FileProcessor fileProcessor = new FileProcessor(contentReplacer, pathCalculator, config);
        logger.info("处理器初始化完成");

        logger.info("正在解压 ZIP 文件: {}", config.getZipFilePath());
        String codeDir = unzip(config.getZipFilePath());
        File codeDirFile = new File(codeDir);
        logger.info("解压完成，临时目录: {}", codeDir);

        List<File> files = FileUtil.loopFiles(codeDirFile);
        logger.info("共找到 {} 个文件需要处理", files.size());

        int total = files.size();
        int processed = 0;
        int lastPercent = 0;
        for (File file : files) {
            fileProcessor.processFile(file, codeDirFile);
            processed++;
            int percent = (int) ((processed * 100.0) / total);
            if (percent > lastPercent || processed % 50 == 0) {
                System.out.print("\r进度: " + processed + "/" + total + " (" + percent + "%)");
                lastPercent = percent;
            }
        }
        System.out.println();

        logger.info("改名完成，输出目录：{}", config.getOutputDirectory());

        logger.info("正在清理临时文件...");
        try {
            FileUtil.del(codeDirFile);
            logger.info("清理完成");
        } catch (Exception e) {
            logger.warn("清理临时文件失败: {} - 请手动删除", codeDirFile.getAbsolutePath(), e);
        }
    }

    private static String unzip(String zipFilePath) {
        String tempFilePath = System.getProperty("user.dir") + "/temp/ruoyi_" + System.currentTimeMillis();
        File zipFile = new File(zipFilePath);
        String suffix = "." + FileUtil.extName(zipFilePath);
        String zipFileName = FileUtil.getName(zipFilePath).replaceAll(suffix, "");
        File targetDir = new File(tempFilePath);
        targetDir.mkdirs();
        ZipUtil.unzip(zipFile, targetDir);
        return tempFilePath + "/" + zipFileName;
    }
}
```

- [ ] **Step 3: 验证编译通过**

Run: `mvn clean compile`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/cn/baruto/ui/App.java src/main/java/cn/baruto/Main.java
git commit -m "feat: 添加 JavaFX App 入口，修改 Main.java 移除 Swing"
```

---

### Task 11: 编译验证与冒烟测试

**Files:**
- 无新增/修改

- [ ] **Step 1: 完整编译**

Run: `mvn clean compile`
Expected: BUILD SUCCESS

- [ ] **Step 2: 运行测试**

Run: `mvn test`
Expected: 所有测试通过（现有测试不涉及 UI 层）

- [ ] **Step 3: 打包验证**

Run: `mvn clean package -DskipTests`
Expected: BUILD SUCCESS，JAR 生成在 target/

- [ ] **Step 4: GUI 启动验证**

Run: `mvn javafx:run`
Expected: 窗口正常显示，侧边栏导航可切换，表单可输入

- [ ] **Step 5: Commit（如有修复）**

```bash
git add -A
git commit -m "fix: 修复编译和运行问题"
```
