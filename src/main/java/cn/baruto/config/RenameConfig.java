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
