package cn.baruto.core;

import cn.baruto.config.RenameConfig;
import cn.hutool.core.util.StrUtil;

public class ContentReplacer {
    private final RenameConfig config;

    public ContentReplacer(RenameConfig config) {
        this.config = config;
    }

    public String replaceContent(String content, String moduleName) {
        String result = content;
        result = replacePackageName(result, moduleName);
        result = replaceModuleName(result);
        return result;
    }

    private String replacePackageName(String content, String moduleName) {
        String result = content;
        // 先进行包名替换
        result = result.replaceAll("org.dromara", config.getTargetPackageName());
        // 还原保留模块的包名
        if (config.getRetainModules() != null) {
            for (String retain : config.getRetainModules()) {
                String wrongPattern = config.getTargetPackageName() + "." + retain;
                String correctPattern = "org.dromara." + retain;
                result = result.replaceAll(wrongPattern, correctPattern);
            }
        }
        return result;
    }

    private String replaceModuleName(String content) {
        String result = content;
        if (config.getModuleMap() != null) {
            for (java.util.Map.Entry<String, String> entry : config.getModuleMap().entrySet()) {
                result = result.replaceAll(entry.getKey(), entry.getValue());
            }
        }
        if (StrUtil.isNotBlank(config.getOldPrefix()) && StrUtil.isNotBlank(config.getNewPrefix())) {
            result = result.replaceAll(config.getOldPrefix(), config.getNewPrefix());
        }
        return result;
    }
}
