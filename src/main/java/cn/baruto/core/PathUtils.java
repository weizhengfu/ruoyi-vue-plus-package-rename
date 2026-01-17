package cn.baruto.core;

import cn.hutool.core.util.StrUtil;
import cn.baruto.config.RenameConfig;
import java.io.File;
import java.util.Set;

public class PathUtils {
    public static String normalizePath(String path) {
        if (path == null) return "";
        return path.replaceAll("\\\\", "/");
    }

    public static String inferModule(File file, File codeDir) {
        String path = normalizePath(file.getAbsolutePath());
        String basePath = normalizePath(codeDir.getAbsolutePath());
        String relativePath = path.replace(basePath, "");
        String[] parts = relativePath.split("/");
        if (parts.length > 0) {
            String moduleName = parts[0].replaceFirst("^/", "");
            if (!moduleName.isEmpty()) return moduleName;
        }
        return "";
    }

    public static boolean isRetainModule(String moduleName, Set<String> retainModules) {
        if (StrUtil.isBlank(moduleName) || retainModules == null || retainModules.isEmpty()) {
            return false;
        }
        for (String retain : retainModules) {
            if (moduleName.contains(retain)) return true;
        }
        return false;
    }

    public static String transformModuleName(String moduleName, RenameConfig config) {
        if (config.getModuleMap() != null && config.getModuleMap().containsKey(moduleName)) {
            return config.getModuleMap().get(moduleName);
        }
        if (StrUtil.isNotBlank(config.getOldPrefix()) && StrUtil.isNotBlank(config.getNewPrefix())
                && moduleName.startsWith(config.getOldPrefix())) {
            return config.getNewPrefix() + moduleName.substring(config.getOldPrefix().length());
        }
        return moduleName;
    }
}
