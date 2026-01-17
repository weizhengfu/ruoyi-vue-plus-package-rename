package cn.baruto.core;

import cn.baruto.config.RenameConfig;
import cn.hutool.core.util.StrUtil;
import java.io.File;

public class PathCalculator {
    private final RenameConfig config;

    public PathCalculator(RenameConfig config) {
        this.config = config;
    }

    public String calculateTargetPath(File sourceFile, File codeDir) {
        String sourcePath = PathUtils.normalizePath(sourceFile.getAbsolutePath());
        String basePath = PathUtils.normalizePath(codeDir.getAbsolutePath());
        String relativePath = sourcePath.replace(basePath, "");
        String[] pathParts = relativePath.split("/");

        // 处理所有路径段中的模块名替换
        for (int i = 0; i < pathParts.length; i++) {
            // 跳过空字符串和已知的特殊路径
            if (pathParts[i].isEmpty() || isSpecialPath(pathParts[i])) {
                continue;
            }

            // 对每个路径段尝试进行模块名转换
            pathParts[i] = PathUtils.transformModuleName(pathParts[i], config);
        }

        // 处理 Java 源码路径中的包名替换
        for (int i = 0; i < pathParts.length; i++) {
            if (isInJavaSourcePath(pathParts, i) && "org".equals(pathParts[i])
                    && i + 1 < pathParts.length && "dromara".equals(pathParts[i + 1])) {
                String currentModule = inferModuleFromPath(pathParts);
                if (!PathUtils.isRetainModule(currentModule, config.getRetainModules())) {
                    String[] packageParts = config.getTargetPackageName().split("\\.");
                    if (packageParts.length >= 2) {
                        pathParts[i] = packageParts[0];
                        pathParts[i + 1] = packageParts[1];
                    }
                }
            }
        }

        String targetRelativePath = String.join("/", pathParts);
        return config.getOutputDirectory() + targetRelativePath;
    }

    /**
     * 判断是否为特殊路径（不需要进行模块名替换）
     */
    private boolean isSpecialPath(String pathPart) {
        return "src".equals(pathPart)
                || "main".equals(pathPart)
                || "java".equals(pathPart)
                || "resources".equals(pathPart)
                || "test".equals(pathPart)
                || "webapp".equals(pathPart);
    }

    private boolean isInJavaSourcePath(String[] parts, int index) {
        for (int i = Math.max(0, index - 3); i < index; i++) {
            if (i + 2 < parts.length && "src".equals(parts[i])
                    && "main".equals(parts[i + 1]) && "java".equals(parts[i + 2])) {
                return true;
            }
        }
        return false;
    }

    private String inferModuleFromPath(String[] parts) {
        for (String part : parts) {
            if (!part.isEmpty() && !part.equals("src")) {
                return part;
            }
        }
        return "";
    }
}
