package cn.baruto.ui.util;

import cn.baruto.config.RenameConfig;
import cn.baruto.ui.controller.ModuleController;
import cn.baruto.ui.controller.PackageController;
import cn.baruto.ui.controller.ProjectController;
import cn.baruto.ui.controller.SourceController;

import java.util.Arrays;
import java.util.Collections;
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
            return Collections.emptySet();
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
