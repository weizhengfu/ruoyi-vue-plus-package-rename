# Swing → JavaFX 重构设计

## 概述

将 UI 层从 Java Swing 重构为 JavaFX 17+，采用 FXML + CSS 架构，侧边栏导航布局。完全替换 Swing 代码，不保留兼容层。

## 方案选择

**方案 A：完全替换**（已选定）

- 删除 `ui/` 下所有 Swing 文件，用 JavaFX 重写
- 编译目标从 JDK 8 升级到 JDK 17
- CLI 模式不依赖 JavaFX runtime，仍可独立运行
- 优势：代码干净，无遗留依赖

## 构建变更

### JDK 目标

`maven.compiler.source/target`: 8 → 17

### 新增依赖

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

### 新增插件

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

### 运行方式

```bash
mvn javafx:run                                                      # GUI 模式
mvn exec:java -Dexec.mainClass="cn.baruto.Main" -Dexec.args="--cli" # CLI 模式
```

## 文件结构

### 删除

- `ui/ModernUI.java`
- `ui/RenameApp.java`

### 新增

```
ui/
├── App.java                      # Application 子类，JavaFX 入口
├── controller/
│   ├── MainController.java       # 主窗口控制器（侧边栏切换、启动处理）
│   ├── SourceController.java     # 源文件配置页
│   ├── PackageController.java    # 包名配置页
│   ├── ProjectController.java    # 项目配置页
│   ├── ModuleController.java     # 模块映射配置页
│   └── LogController.java        # 日志/进度页
├── service/
│   └── RenameService.java        # 后台处理（javafx.concurrent.Service）
└── util/
    └── ConfigMapper.java         # UI 字段 → RenameConfig 映射

resources/
├── fxml/
│   ├── main.fxml                 # 主布局（侧边栏 + 内容区）
│   ├── source.fxml               # 源文件配置页
│   ├── package.fxml              # 包名配置页
│   ├── project.fxml              # 项目配置页
│   ├── module.fxml               # 模块映射配置页
│   └── log.fxml                  # 日志/进度页
└── css/
    └── style.css                 # 全局样式
```

### 修改

- `Main.java`: GUI 模式改为 `App.launch()`，移除 Nimbus Look & Feel 代码
- `pom.xml`: JDK 17 + JavaFX 依赖 + javafx-maven-plugin

## 架构设计

### 数据流

```
FXML (布局) ←→ Controller (逻辑) → ConfigMapper → RenameConfig
                                              ↓
RenameService (javafx.concurrent.Service)
    ├── 内部创建 Task<Void>
    ├── updateMessage() → LogController 实时日志
    ├── updateProgress() → LogController 进度条
    └── 完成后 → Alert 弹窗通知
```

### 控制器协调

- `MainController` 持有所有子控制器的引用（通过 `@FXML` 注入）
- 侧边栏切换由 `MainController` 管理，替换内容区的子场景
- 各子控制器只负责各自页面的表单数据
- `ConfigMapper` 从各子控制器收集数据构建 `RenameConfig`

### 后台处理

- `RenameService` 继承 `javafx.concurrent.Service<Void>`
- 每次点击"开始处理"调用 `service.restart()`
- Task 内部执行：解压 → 遍历 → ContentReplacer + PathCalculator → FileProcessor → 清理
- 通过 `updateMessage()` 推送日志文本，`updateProgress()` 推送进度
- Controller 监听 `messageProperty` 和 `progressProperty` 更新 UI

## 布局设计

### 主窗口

```
┌──────────────────────────────────────────────┐
│  RuoYi-Vue-Plus 包名修改器  v1.0       [─][□][×]│
├────────────┬─────────────────────────────────┤
│ [R] 修改器  │                                 │
│ RuoYi-Plus │                                 │
│────────────│                                 │
│ ▎源文件     │   当前选中页面内容                │
│   包名      │   （由 fx:include 加载）         │
│   项目      │                                 │
│   模块映射   │                                 │
│   处理日志   │                                 │
│            │                                 │
│            │                                 │
│────────────│                                 │
│ [开始处理]  │                                 │
│ v1.0       │                                 │
└────────────┴─────────────────────────────────┘
```

侧边栏宽度 200px，固定在左侧。内容区自适应填充。窗口最小尺寸 900×650。

### 侧边栏行为

- 默认选中「源文件」
- 选中项：3px 蓝色左边框 + 背景 `#253350` + 白色文字
- 未选中项：无左边线 + 背景 `#1B2336` + 文字 `#94A3B8`
- 「开始处理」按钮固定在侧边栏底部
- 处理中按钮禁用

### 页面内容

| 页面 | 控件 |
|------|------|
| 源文件 | ZIP 路径（TextField + 浏览按钮）、输出目录（TextField + 浏览按钮） |
| 包名 | 目标包名、保留模块（TextField + 提示文字） |
| 项目 | 项目名称、项目版本 |
| 模块映射 | 前缀替换（旧→新两个 TextField）、精确映射（TextField + 格式提示） |
| 处理日志 | 深色背景 TextArea、进度条、完成/失败 Alert |

### 表单卡片设计

每个页面统一结构：
- 页面标题（蓝色，18px Bold）+ 描述（灰色，13px）
- 白色卡片容器，8px 圆角，轻微阴影
- 标签在输入框上方，必填项标 `*`
- 输入框：白底，36px 高，8px 圆角，聚焦蓝色边框
- 提示文字在输入框下方（小灰字）

### 日志页面

- 深色终端区域（背景 `#1E293B`，文字 `#A5D6A7`）
- 等宽字体 Consolas
- 进度条：绿色 `#22C55E`，圆角，显示百分比

## 视觉设计系统

### 配色

| 用途 | 色值 |
|------|------|
| 侧边栏背景 | `#1B2336` |
| 内容区背景 | `#F0F4F8` |
| 卡片背景 | `#FFFFFF` |
| 主色 Accent | `#3B82F6` |
| 主色悬停 | `#2563EB` |
| 成功色 | `#22C55E` |
| 危险色 | `#EF4444` |
| 侧边栏文字 | `#94A3B8` |
| 侧边栏选中文字 | `#FFFFFF` |
| 内容区主文字 | `#1E293B` |
| 内容区次文字 | `#64748B` |
| 输入框边框 | `#CBD5E1` |
| 输入框聚焦边框 | `#3B82F6` |
| 日志区背景 | `#1E293B` |
| 日志区文字 | `#A5D6A7` |

### 字体

- 标题：Microsoft YaHei UI / System Bold，18-28px
- 正文/标签：Microsoft YaHei UI / System Regular，13-14px
- 日志区：Consolas / Monospace，12-13px

### CSS 变量

```css
.root {
    -sidebar-bg: #1B2336;
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
    -border: #CBD5E1;
    -border-focus: #3B82F6;
    -log-bg: #1E293B;
    -log-text: #A5D6A7;
    -radius: 8px;
}
```

## 交互细节

- 侧边栏导航切换：淡入淡出 200ms
- 输入框聚焦：边框蓝色 + 轻微阴影
- 按钮悬停：颜色加深 + 手型光标
- 处理中：按钮禁用 + 文字变为"处理中..."
- 完成：Alert 弹窗（成功绿色/失败红色图标）
- 进度条：平滑动画，每 50 文件更新
- 文件选择：FileChooser（ZIP）+ DirectoryChooser（目录）

## Main.java 变更

- GUI 模式：`App.launch(App.class, args)`
- CLI 模式：不变
- 移除所有 Swing/Nimbus 代码
