package cn.baruto.core;

import cn.baruto.config.RenameConfig;
import cn.hutool.core.util.StrUtil;

public class ContentReplacer {
    // 使用词边界正则表达式避免误匹配
    // 例如：不匹配 "notorg.dromara.xyz" 中的 "org.dromara"
    private static final String PACKAGE_PATTERN = "\\borg\\.dromara(?=\\.)";
    private static final String PACKAGE_ESCAPE = "___PKG_REPLACED___";

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
        // 第一步：使用词边界匹配进行包名替换（仅匹配后面跟着点号的 org.dromara）
        result = result.replaceAll(PACKAGE_PATTERN, config.getTargetPackageName());
        // 第二步：还原保留模块的包名
        if (config.getRetainModules() != null) {
            for (String retain : config.getRetainModules()) {
                // 替换被误改的保留模块包名
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
