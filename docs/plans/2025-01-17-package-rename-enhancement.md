# 包名修改器增强功能实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 增强 RuoYi-Vue-Plus 包名修改工具，支持保留多个模块的原始包名、模块名映射、自定义项目名称配置。

**架构:** 重构现有单体 Main 类为分层架构：ConfigLoader 加载配置、ContentReplacer 处理内容替换、PathCalculator 计算路径、FileProcessor 统一处理文件。保持原有 Hutool 依赖，添加 Apache Commons Lang 用于字符串操作。

**Tech Stack:** Java 8, Hutool 5.8.28, Apache Commons Lang 3.x

---

## Task 1: 添加 Apache Commons Lang 依赖

**Files:**
- Modify: `pom.xml`

**Step 1: 添加 commons-lang3 依赖**

在 `dependencies` 节点中添加：

```xml
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
    <version>3.14.0</version>
</dependency>
```

**Step 2: 验证依赖添加成功**

Run: `mvn dependency:tree`
Expected: 输出中包含 `org.apache.commons:commons-lang3:3.14.0`

**Step 3: 提交**

```bash
git add pom.xml
git commit -m "deps: add apache commons-lang3 dependency"
```

---

## Task 2: 创建配置模型类

**Files:**
- Create: `src/main/java/cn/baruto/config/RenameConfig.java`

**Step 1: 创建 RenameConfig 类**

```java
package cn.baruto.config;

import java.util.Set;
import java.util.Map;

/**
 * 包名修改配置模型
 */
public class RenameConfig {

    // === 源文件配置 ===
    private String zipFilePath;

    // === 包名配置 ===
    private String targetPackageName;
    private Set<String> retainModules;

    // === 项目配置 ===
    private String projectName;
    private String projectVersion;
    private String targetPath;

    // === 模块名映射配置 ===
    private String oldPrefix;
    private String newPrefix;
    private Map<String, String> moduleMap;

    /**
     * 获取输出目录
     */
    public String getOutputDirectory() {
        String dir = targetPath + java.io.File.separator + projectName;
        if (projectVersion != null && !projectVersion.trim().isEmpty()) {
            dir = dir + java.io.File.separator + projectVersion;
        }
        return dir;
    }

    // Getters and Setters
    public String getZipFilePath() { return zipFilePath; }
    public void setZipFilePath(String zipFilePath) { this.zipFilePath = zipFilePath; }

    public String getTargetPackageName() { return targetPackageName; }
    public void setTargetPackageName(String targetPackageName) { this.targetPackageName = targetPackageName; }

    public Set<String> getRetainModules() { return retainModules; }
    public void setRetainModules(Set<String> retainModules) { this.retainModules = retainModules; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getProjectVersion() { return projectVersion; }
    public void setProjectVersion(String projectVersion) { this.projectVersion = projectVersion; }

    public String getTargetPath() { return targetPath; }
    public void setTargetPath(String targetPath) { this.targetPath = targetPath; }

    public String getOldPrefix() { return oldPrefix; }
    public void setOldPrefix(String oldPrefix) { this.oldPrefix = oldPrefix; }

    public String getNewPrefix() { return newPrefix; }
    public void setNewPrefix(String newPrefix) { this.newPrefix = newPrefix; }

    public Map<String, String> getModuleMap() { return moduleMap; }
    public void setModuleMap(Map<String, String> moduleMap) { this.moduleMap = moduleMap; }
}
```

**Step 2: 编译验证**

Run: `mvn compile`
Expected: BUILD SUCCESS

**Step 3: 提交**

```bash
git add src/main/java/cn/baruto/config/RenameConfig.java
git commit -m "feat: add RenameConfig model class"
```

---

## Task 3: 创建配置加载器

**Files:**
- Create: `src/main/java/cn/baruto/config/ConfigLoader.java`

**Step 1: 创建 ConfigLoader 类**

```java
package cn.baruto.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import cn.hutool.setting.dialect.PropsUtil;

import java.io.File;
import java.util.*;

/**
 * 配置加载器
 */
public class ConfigLoader {

    /**
     * 从 setting.properties 加载配置
     */
    public static RenameConfig load(String configFile) {
        Props props = PropsUtil.get(configFile);
        RenameConfig config = new RenameConfig();

        // 基础配置
        config.setZipFilePath(props.getProperty("zip.name"));
        config.setTargetPath(props.getProperty("target.path"));

        // 包名配置
        config.setTargetPackageName(props.getProperty("package.name"));
        config.setRetainModules(parseRetainModules(props.getProperty("package.retain")));

        // 项目配置
        config.setProjectName(props.getProperty("project.name"));
        config.setProjectVersion(props.getProperty("project.version", ""));

        // 模块映射配置
        config.setOldPrefix(props.getProperty("module.prefix.old"));
        config.setNewPrefix(props.getProperty("module.prefix.new"));
        config.setModuleMap(parseModuleMap(props.getProperty("module.map")));

        // 验证配置
        validate(config);

        return config;
    }

    /**
     * 解析保留模块列表
     */
    private static Set<String> parseRetainModules(String value) {
        if (StrUtil.isBlank(value)) {
            return Collections.emptySet();
        }
        return new HashSet<>(Arrays.asList(value.split(",")));
    }

    /**
     * 解析模块映射
     */
    private static Map<String, String> parseModuleMap(String value) {
        Map<String, String> map = new HashMap<>();
        if (StrUtil.isBlank(value)) {
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

    /**
     * 验证配置完整性
     */
    private static void validate(RenameConfig config) {
        List<String> errors = new ArrayList<>();

        if (StrUtil.isBlank(config.getZipFilePath())) {
            errors.add("zip.name 未配置");
        }

        if (StrUtil.isBlank(config.getTargetPackageName())) {
            errors.add("package.name 未配置");
        }

        if (StrUtil.isBlank(config.getProjectName())) {
            errors.add("project.name 未配置");
        }

        if (StrUtil.isBlank(config.getTargetPath())) {
            errors.add("target.path 未配置");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("配置错误：\n" + String.join("\n", errors));
        }

        // 验证 ZIP 文件存在
        File zipFile = new File(config.getZipFilePath());
        if (!zipFile.exists()) {
            throw new IllegalArgumentException("ZIP 文件不存在: " + config.getZipFilePath());
        }
    }
}
```

**Step 2: 编译验证**

Run: `mvn compile`
Expected: BUILD SUCCESS

**Step 3: 提交**

```bash
git add src/main/java/cn/baruto/config/ConfigLoader.java
git commit -m "feat: add ConfigLoader class"
```

---

## Task 4: 创建路径工具类

**Files:**
- Create: `src/main/java/cn/baruto/core/PathUtils.java`

**Step 1: 创建 PathUtils 工具类**

```java
package cn.baruto.core;

import cn.hutool.core.util.StrUtil;
import cn.baruto.config.RenameConfig;

import java.io.File;
import java.util.Set;

/**
 * 路径工具类
 */
public class PathUtils {

    /**
     * 统一路径分隔符为 /
     */
    public static String normalizePath(String path) {
        if (path == null) {
            return "";
        }
        return path.replaceAll("\\\\", "/");
    }

    /**
     * 从文件路径推断所属模块
     */
    public static String inferModule(File file, File codeDir) {
        String path = normalizePath(file.getAbsolutePath());
        String basePath = normalizePath(codeDir.getAbsolutePath());

        String relativePath = path.replace(basePath, "");
        String[] parts = relativePath.split("/");

        // 第一级目录是模块名
        if (parts.length > 0) {
            String moduleName = parts[0].replaceFirst("^/", "");
            if (!moduleName.isEmpty()) {
                return moduleName;
            }
        }

        return "";
    }

    /**
     * 从模块名判断是否需要保留包名
     */
    public static boolean isRetainModule(String moduleName, Set<String> retainModules) {
        if (StrUtil.isBlank(moduleName) || retainModules == null || retainModules.isEmpty()) {
            return false;
        }
        for (String retain : retainModules) {
            if (moduleName.contains(retain)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 转换模块名（应用映射规则）
     */
    public static String transformModuleName(String moduleName, RenameConfig config) {
        // 1. 优先使用精确映射
        if (config.getModuleMap() != null && config.getModuleMap().containsKey(moduleName)) {
            return config.getModuleMap().get(moduleName);
        }

        // 2. 使用前后缀替换
        if (StrUtil.isNotBlank(config.getOldPrefix())
                && StrUtil.isNotBlank(config.getNewPrefix())
                && moduleName.startsWith(config.getOldPrefix())) {
            return config.getNewPrefix() + moduleName.substring(config.getOldPrefix().length());
        }

        return moduleName;
    }
}
```

**Step 2: 编译验证**

Run: `mvn compile`
Expected: BUILD SUCCESS

**Step 3: 提交**

```bash
git add src/main/java/cn/baruto/core/PathUtils.java
git commit -m "feat: add PathUtils utility class"
```

---

## Task 5: 创建内容替换引擎

**Files:**
- Create: `src/main/java/cn/baruto/core/ContentReplacer.java`

**Step 1: 创建 ContentReplacer 类**

```java
package cn.baruto.core;

import cn.baruto.config.RenameConfig;
import cn.hutool.core.util.StrUtil;

/**
 * 内容替换引擎
 */
public class ContentReplacer {

    private final RenameConfig config;

    public ContentReplacer(RenameConfig config) {
        this.config = config;
    }

    /**
     * 统一的内容替换方法
     */
    public String replaceContent(String content, String moduleName) {
        String result = content;

        // 1. 包名替换
        result = replacePackageName(result, moduleName);

        // 2. 模块名替换
        result = replaceModuleName(result);

        return result;
    }

    /**
     * 替换包名
     */
    private String replacePackageName(String content, String moduleName) {
        String result = content;

        if (PathUtils.isRetainModule(moduleName, config.getRetainModules())) {
            // 保留模块：只替换模块名，不替换包名
            result = replaceModulePrefixOnly(result);
        } else {
            // 普通模块：替换包名
            result = result.replaceAll("org.dromara", config.getTargetPackageName());

            // 特殊处理：还原保留模块的包名引用
            if (config.getRetainModules() != null) {
                for (String retain : config.getRetainModules()) {
                    String wrongPattern = config.getTargetPackageName() + "." + retain;
                    String correctPattern = "org.dromara." + retain;
                    result = result.replaceAll(wrongPattern, correctPattern);
                }
            }
        }

        return result;
    }

    /**
     * 仅替换模块名前缀（用于保留包名的模块）
     */
    private String replaceModulePrefixOnly(String content) {
        String result = content;

        // 只替换模块名（artifactId、目录名等），不替换包名
        if (StrUtil.isNotBlank(config.getOldPrefix()) && StrUtil.isNotBlank(config.getNewPrefix())) {
            // 替换 artifactId 中的模块名（通常是 <artifactId>ruoyi-xxx</artifactId>）
            result = result.replaceAll(
                    "<artifactId>" + config.getOldPrefix() + "-",
                    "<artifactId>" + config.getNewPrefix() + "-"
            );
            result = result.replaceAll(
                    "</artifactId>",
                    "</artifactId>"
            );

            // 替换目录引用中的模块名
            result = result.replaceAll(
                    "<module>" + config.getOldPrefix() + "-",
                    "<module>" + config.getNewPrefix() + "-"
            );
            result = result.replaceAll(
                    "</module>",
                    "</module>"
            );
        }

        // 应用精确映射
        if (config.getModuleMap() != null) {
            for (java.util.Map.Entry<String, String> entry : config.getModuleMap().entrySet()) {
                result = result.replaceAll(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

    /**
     * 替换模块名
     */
    private String replaceModuleName(String content) {
        String result = content;

        // 1. 优先使用精确映射
        if (config.getModuleMap() != null) {
            for (java.util.Map.Entry<String, String> entry : config.getModuleMap().entrySet()) {
                result = result.replaceAll(entry.getKey(), entry.getValue());
            }
        }

        // 2. 前后缀替换
        if (StrUtil.isNotBlank(config.getOldPrefix()) && StrUtil.isNotBlank(config.getNewPrefix())) {
            result = result.replaceAll(config.getOldPrefix(), config.getNewPrefix());
        }

        return result;
    }
}
```

**Step 2: 编译验证**

Run: `mvn compile`
Expected: BUILD SUCCESS

**Step 3: 提交**

```bash
git add src/main/java/cn/baruto/core/ContentReplacer.java
git commit -m "feat: add ContentReplacer engine"
```

---

## Task 6: 创建路径计算器

**Files:**
- Create: `src/main/java/cn/baruto/core/PathCalculator.java`

**Step 1: 创建 PathCalculator 类**

```java
package cn.baruto.core;

import cn.baruto.config.RenameConfig;
import cn.hutool.core.util.StrUtil;

import java.io.File;
import java.util.Arrays;

/**
 * 路径计算器
 */
public class PathCalculator {

    private final RenameConfig config;

    public PathCalculator(RenameConfig config) {
        this.config = config;
    }

    /**
     * 计算目标文件路径
     */
    public String calculateTargetPath(File sourceFile, File codeDir) {
        String sourcePath = PathUtils.normalizePath(sourceFile.getAbsolutePath());
        String basePath = PathUtils.normalizePath(codeDir.getAbsolutePath());

        // 获取相对路径
        String relativePath = sourcePath.replace(basePath, "");

        // 分割路径段
        String[] pathParts = relativePath.split("/");

        // 处理第一级目录（模块名）
        if (pathParts.length > 0 && !pathParts[0].isEmpty()) {
            String moduleName = pathParts[0];
            pathParts[0] = PathUtils.transformModuleName(moduleName, config);
        }

        // 处理 Java 源码目录中的包名路径
        for (int i = 0; i < pathParts.length; i++) {
            if (isInJavaSourcePath(pathParts, i)
                    && "org".equals(pathParts[i])
                    && i + 1 < pathParts.length
                    && "dromara".equals(pathParts[i + 1])) {

                // 检查是否需要保留包名
                String currentModule = inferModuleFromPath(pathParts);
                if (!PathUtils.isRetainModule(currentModule, config.getRetainModules())) {
                    // 替换包名段 org.dromara.xxx → targetPackageName.xxx
                    String[] packageParts = config.getTargetPackageName().split("\\.");
                    if (packageParts.length >= 2) {
                        pathParts[i] = packageParts[0];
                        pathParts[i + 1] = packageParts[1];
                    }
                }
            }
        }

        // 重新组装路径
        String targetRelativePath = String.join("/", pathParts);
        return config.getOutputDirectory() + targetRelativePath;
    }

    /**
     * 判断当前位置是否在 Java 源码路径中
     */
    private boolean isInJavaSourcePath(String[] parts, int index) {
        // 查找前面是否有 src/main/java 模式
        for (int i = Math.max(0, index - 3); i < index; i++) {
            if (i + 2 < parts.length
                    && "src".equals(parts[i])
                    && "main".equals(parts[i + 1])
                    && "java".equals(parts[i + 2])) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从路径段推断模块名
     */
    private String inferModuleFromPath(String[] parts) {
        // 查找模块名（第一级目录）
        for (String part : parts) {
            if (!part.isEmpty() && !part.equals("src")) {
                return part;
            }
        }
        return "";
    }
}
```

**Step 2: 编译验证**

Run: `mvn compile`
Expected: BUILD SUCCESS

**Step 3: 提交**

```bash
git add src/main/java/cn/baruto/core/PathCalculator.java
git commit -m "feat: add PathCalculator class"
```

---

## Task 7: 创建文件处理器

**Files:**
- Create: `src/main/java/cn/baruto/processor/FileProcessor.java`

**Step 1: 创建 FileProcessor 类**

```java
package cn.baruto.processor;

import cn.baruto.config.RenameConfig;
import cn.baruto.core.ContentReplacer;
import cn.baruto.core.PathCalculator;
import cn.baruto.core.PathUtils;
import cn.hutool.core.io.FileUtil;

import java.io.File;

/**
 * 文件处理器
 */
public class FileProcessor {

    private final ContentReplacer contentReplacer;
    private final PathCalculator pathCalculator;
    private final RenameConfig config;

    public FileProcessor(ContentReplacer contentReplacer, PathCalculator pathCalculator, RenameConfig config) {
        this.contentReplacer = contentReplacer;
        this.pathCalculator = pathCalculator;
        this.config = config;
    }

    /**
     * 处理单个文件
     */
    public void processFile(File file, File codeDir) {
        // 1. 推断模块名
        String moduleName = PathUtils.inferModule(file, codeDir);

        // 2. 计算目标路径
        String targetPath = pathCalculator.calculateTargetPath(file, codeDir);

        // 3. 确保目标目录存在
        File targetFile = new File(targetPath);
        File parentDir = targetFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        // 4. 读取并替换内容
        String content = FileUtil.readUtf8String(file);
        String newContent = contentReplacer.replaceContent(content, moduleName);

        // 5. 写入文件
        FileUtil.writeString(newContent, targetPath, "UTF-8");
        System.out.println("写入文件：" + targetPath);
    }
}
```

**Step 2: 编译验证**

Run: `mvn compile`
Expected: BUILD SUCCESS

**Step 3: 提交**

```bash
git add src/main/java/cn/baruto/processor/FileProcessor.java
git commit -m "feat: add FileProcessor class"
```

---

## Task 8: 更新 setting.properties 配置文件

**Files:**
- Modify: `src/main/resources/setting.properties`

**Step 1: 更新配置文件格式**

将现有配置替换为：

```properties
# === 源文件配置 ===
# RuoYi-Vue-Plus 的 ZIP 包路径
zip.name=E:\\33597\\ABDM\\Compressed\\RuoYi-Vue-Plus-5.X.zip

# === 包名配置 ===
# 目标包名（替换 org.dromara）
package.name=com.molu

# 保留原始包名的模块（逗号分隔）
package.retain=sms4j,warm

# === 项目配置 ===
# 项目名称
project.name=MESCore

# 项目版本（可选，为空则不添加）
project.version=1.0.0

# 输出目录
target.path=D:\\Development\\Projects\\MESCore

# === 模块名映射配置 ===
# 方式1: 前后缀替换（处理所有匹配的模块）
module.prefix.old=ruoyi
module.prefix.new=mes

# 方式2: 精确映射（优先级更高，逗号分隔）
# 格式: 旧模块名:新模块名
module.map=ruoyi-admin:mes-server,ruoyi-common-bom:mes-dependencies
```

**Step 2: 验证配置文件可被正确加载**

Run: `mvn compile`
Expected: BUILD SUCCESS

**Step 3: 提交**

```bash
git add src/main/resources/setting.properties
git commit -m "feat: enhance setting.properties with new configuration options"
```

---

## Task 9: 重构 Main 类

**Files:**
- Modify: `src/main/java/cn/baruto/Main.java`

**Step 1: 重写 Main 类**

完全替换 [Main.java](src/main/java/cn/baruto/Main.java) 内容为：

```java
package cn.baruto;

import cn.baruto.config.ConfigLoader;
import cn.baruto.config.RenameConfig;
import cn.baruto.core.ContentReplacer;
import cn.baruto.core.PathCalculator;
import cn.baruto.processor.FileProcessor;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;

import java.io.File;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        // 1. 加载并验证配置
        RenameConfig config = ConfigLoader.load("setting.properties");

        // 2. 初始化处理器
        ContentReplacer contentReplacer = new ContentReplacer(config);
        PathCalculator pathCalculator = new PathCalculator(config);
        FileProcessor fileProcessor = new FileProcessor(contentReplacer, pathCalculator, config);

        // 3. 解压源文件
        String codeDir = unzip(config.getZipFilePath());
        File codeDirFile = new File(codeDir);

        // 4. 遍历处理文件
        List<File> files = FileUtil.loopFiles(codeDirFile);
        for (File file : files) {
            fileProcessor.processFile(file, codeDirFile);
        }

        System.out.println("\n改名完成，输出目录：" + config.getOutputDirectory());

        // 5. 清理临时文件
        FileUtil.del(codeDirFile);
    }

    /**
     * 解压 ZIP 文件
     */
    private static String unzip(String zipFilePath) {
        // 创建临时文件夹
        String tempFilePath = System.getProperty("user.dir") + "/temp/ruoyi_" + System.currentTimeMillis();

        File zipFile = new File(zipFilePath);

        // 获取文件名（不带后缀）
        String suffix = "." + FileUtil.extName(zipFilePath);
        String zipFileName = FileUtil.getName(zipFilePath).replaceAll(suffix, "");

        File targetDir = new File(tempFilePath);
        targetDir.mkdirs();
        ZipUtil.unzip(zipFile, targetDir);

        // 返回解压后的代码路径
        return tempFilePath + "/" + zipFileName;
    }
}
```

**Step 2: 编译验证**

Run: `mvn clean compile`
Expected: BUILD SUCCESS

**Step 3: 提交**

```bash
git add src/main/java/cn/baruto/Main.java
git commit -m "refactor: rewrite Main class with new architecture"
```

---

## Task 10: 更新 README 文档

**Files:**
- Modify: `README.md`

**Step 1: 更新 README 内容**

替换为：

```markdown
## RuoYi-Vue-Plus包名修改器

> 支持一键修改RuoYi-Vue-Plus包名、模块名和项目配置

## 功能特性

- 批量替换包名（org.dromara → 自定义包名）
- 支持保留指定模块的原始包名（如 sms4j、warm）
- 支持模块名映射（前后缀替换或精确映射）
- 自定义项目名称和版本
- 自动处理 Java 文件、XML 文件、POM 文件等

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
module.map=ruoyi-admin:yourapp-server
```

### 3. 运行程序

```bash
mvn clean compile exec:java -Dexec.mainClass="cn.baruto.Main"
```

### 4. 查看结果

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
```

**Step 2: 提交**

```bash
git add README.md
git commit -m "docs: update README with new features and configuration"
```

---

## Task 11: 集成测试

**Files:**
- N/A (运行验证)

**Step 1: 准备测试环境**

确认 `setting.properties` 中的路径配置正确：
- ZIP 文件存在
- 输出目录可写

**Step 2: 运行程序**

Run: `mvn clean compile exec:java -Dexec.mainClass="cn.baruto.Main"`
Expected:
- 看到解压日志
- 看到一系列 "写入文件：" 日志
- 最后输出 "改名完成，输出目录：..."

**Step 3: 验证输出结果**

检查输出目录：
1. 目录结构正确（模块名已替换）
2. Java 文件包名已正确替换
3. pom.xml 中包名和模块名已替换
4. sms4j、warm 模块的 org.dromara 包名已保留
5. 模块名映射已生效

**Step 4: 提交（如果测试通过）**

```bash
git add -A
git commit -m "test: verify complete functionality of enhanced package rename tool"
```

---

## Task 12: 更新 CLAUDE.md 文档

**Files:**
- Modify: `CLAUDE.md`

**Step 1: 更新架构描述**

在"核心架构"部分添加新类和配置说明，更新为：

```
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
```

**Step 2: 提交**

```bash
git add CLAUDE.md
git commit -m "docs: update CLAUDE.md with new architecture"
```

---

## 完成清单

- [ ] 所有新类已创建
- [ ] 配置文件已更新
- [ ] Main 类已重构
- [ ] 文档已更新
- [ ] 编译通过
- [ ] 功能测试通过
