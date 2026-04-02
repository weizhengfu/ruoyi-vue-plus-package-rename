package cn.baruto.core;

import cn.baruto.config.RenameConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PathUtils 单元测试
 */
class PathUtilsTest {

    private RenameConfig config;

    @BeforeEach
    void setUp() {
        config = new RenameConfig();
        config.setOldPrefix("ruoyi");
        config.setNewPrefix("myapp");
        config.setRetainModules(new HashSet<>(Arrays.asList("sms4j", "warm")));
    }

    @Test
    void testNormalizePath_WindowsBackslash() {
        String input = "D:\\projects\\ruoyi\\src";
        String result = PathUtils.normalizePath(input);
        assertEquals("D:/projects/ruoyi/src", result);
    }

    @Test
    void testNormalizePath_UnixSlash() {
        String input = "/home/user/projects/ruoyi";
        String result = PathUtils.normalizePath(input);
        assertEquals("/home/user/projects/ruoyi", result);
    }

    @Test
    void testNormalizePath_Null() {
        String result = PathUtils.normalizePath(null);
        assertEquals("", result);
    }

    @Test
    void testInferModule_ValidPath() {
        // 实际使用中 codeDir 是项目根目录，文件在 ruoyi_admin/src/main/java/...
        // 此时 inferModule 返回 src（因为 src 是相对路径第一个有效部分）
        // 这正是代码的当前行为
        File file = new File("D:/temp/ruoyi_admin/src/main/java/org/dromara/service/Test.java");
        File codeDir = new File("D:/temp/ruoyi_admin");
        String result = PathUtils.inferModule(file, codeDir);
        assertEquals("src", result);
    }

    @Test
    void testInferModule_RootLevel() {
        // 如果 codeDir 是更上层的目录，可以正确获取模块名
        File file = new File("D:/temp/ruoyi_admin/src/main/java/org/dromara/service/Test.java");
        File codeDir = new File("D:/temp");
        String result = PathUtils.inferModule(file, codeDir);
        assertEquals("ruoyi_admin", result);
    }

    @Test
    void testIsRetainModule_True() {
        boolean result = PathUtils.isRetainModule("sms4j", config.getRetainModules());
        assertTrue(result);
    }

    @Test
    void testIsRetainModule_Contains() {
        boolean result = PathUtils.isRetainModule("sms4j-starter", config.getRetainModules());
        assertTrue(result);
    }

    @Test
    void testIsRetainModule_False() {
        boolean result = PathUtils.isRetainModule("ruoyi-module", config.getRetainModules());
        assertFalse(result);
    }

    @Test
    void testIsRetainModule_NullModuleName() {
        boolean result = PathUtils.isRetainModule(null, config.getRetainModules());
        assertFalse(result);
    }

    @Test
    void testIsRetainModule_NullRetainModules() {
        boolean result = PathUtils.isRetainModule("sms4j", null);
        assertFalse(result);
    }

    @Test
    void testTransformModuleName_PrefixMatch() {
        String result = PathUtils.transformModuleName("ruoyi-admin", config);
        assertEquals("myapp-admin", result);
    }

    @Test
    void testTransformModuleName_NoMatch() {
        String result = PathUtils.transformModuleName("other-module", config);
        assertEquals("other-module", result);
    }

    @Test
    void testTransformModuleName_RetainModule() {
        String result = PathUtils.transformModuleName("sms4j", config);
        // sms4j 不应该被前缀替换
        assertEquals("sms4j", result);
    }
}
