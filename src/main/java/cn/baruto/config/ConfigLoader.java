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

    public static RenameConfig load(String configFile) {
        Props props = PropsUtil.get(configFile);
        RenameConfig config = new RenameConfig();

        config.setZipFilePath(props.getProperty("zip.name"));
        config.setTargetPath(props.getProperty("target.path"));
        config.setTargetPackageName(props.getProperty("package.name"));
        config.setRetainModules(parseRetainModules(props.getProperty("package.retain")));
        config.setProjectName(props.getProperty("project.name"));
        config.setProjectVersion(props.getProperty("project.version", ""));
        config.setOldPrefix(props.getProperty("module.prefix.old"));
        config.setNewPrefix(props.getProperty("module.prefix.new"));
        config.setModuleMap(parseModuleMap(props.getProperty("module.map")));

        validate(config);
        return config;
    }

    private static Set<String> parseRetainModules(String value) {
        if (StrUtil.isBlank(value)) {
            return Collections.emptySet();
        }
        return new HashSet<>(Arrays.asList(value.split(",")));
    }

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

    private static void validate(RenameConfig config) {
        List<String> errors = new ArrayList<>();
        if (StrUtil.isBlank(config.getZipFilePath())) errors.add("zip.name 未配置");
        if (StrUtil.isBlank(config.getTargetPackageName())) errors.add("package.name 未配置");
        if (StrUtil.isBlank(config.getProjectName())) errors.add("project.name 未配置");
        if (StrUtil.isBlank(config.getTargetPath())) errors.add("target.path 未配置");
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("配置错误：\n" + String.join("\n", errors));
        }
        File zipFile = new File(config.getZipFilePath());
        if (!zipFile.exists()) {
            throw new IllegalArgumentException("ZIP 文件不存在: " + config.getZipFilePath());
        }
    }
}
