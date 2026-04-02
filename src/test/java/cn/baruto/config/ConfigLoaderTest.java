package cn.baruto.config;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConfigLoader 单元测试
 */
class ConfigLoaderTest {

    @Test
    void testLoad_ValidConfig() {
        RenameConfig config = ConfigLoader.load("setting.properties");

        assertNotNull(config);
        assertEquals("com.ml", config.getTargetPackageName());
        assertEquals("ForgeFlowServer", config.getProjectName());
        assertEquals("1.0.0", config.getProjectVersion());
        assertEquals("ruoyi", config.getOldPrefix());
        assertEquals("ml", config.getNewPrefix());
    }

    @Test
    void testLoad_RetainModules() {
        RenameConfig config = ConfigLoader.load("setting.properties");
        Set<String> retainModules = config.getRetainModules();

        assertNotNull(retainModules);
        assertTrue(retainModules.contains("sms4j"));
        assertTrue(retainModules.contains("warm"));
    }

    @Test
    void testLoad_ModuleMap() {
        RenameConfig config = ConfigLoader.load("setting.properties");
        Map<String, String> moduleMap = config.getModuleMap();

        // setting.properties 中 module.map 为空，所以应该是空 map
        assertNotNull(moduleMap);
    }

    @Test
    void testGetOutputDirectory_WithVersion() {
        RenameConfig config = new RenameConfig();
        config.setTargetPath("D:\\test");
        config.setProjectName("MyProject");
        config.setProjectVersion("1.0.0");

        String outputDir = config.getOutputDirectory();
        assertEquals("D:\\test" + java.io.File.separator + "MyProject" + java.io.File.separator + "1.0.0", outputDir);
    }

    @Test
    void testGetOutputDirectory_WithoutVersion() {
        RenameConfig config = new RenameConfig();
        config.setTargetPath("D:\\test");
        config.setProjectName("MyProject");
        config.setProjectVersion("");

        String outputDir = config.getOutputDirectory();
        assertEquals("D:\\test" + java.io.File.separator + "MyProject", outputDir);
    }

    @Test
    void testGetOutputDirectory_NullVersion() {
        RenameConfig config = new RenameConfig();
        config.setTargetPath("D:\\test");
        config.setProjectName("MyProject");
        config.setProjectVersion(null);

        String outputDir = config.getOutputDirectory();
        assertEquals("D:\\test" + java.io.File.separator + "MyProject", outputDir);
    }
}
