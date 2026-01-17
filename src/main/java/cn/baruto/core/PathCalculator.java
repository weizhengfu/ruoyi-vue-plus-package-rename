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

        if (pathParts.length > 0 && !pathParts[0].isEmpty()) {
            String moduleName = pathParts[0];
            pathParts[0] = PathUtils.transformModuleName(moduleName, config);
        }

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
