package cn.baruto.core;

import cn.baruto.config.RenameConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ContentReplacer 单元测试
 */
class ContentReplacerTest {

    private ContentReplacer contentReplacer;
    private RenameConfig config;

    @BeforeEach
    void setUp() {
        config = new RenameConfig();
        config.setTargetPackageName("com.example");
        config.setRetainModules(new HashSet<>(Arrays.asList("sms4j", "warm")));
        contentReplacer = new ContentReplacer(config);
    }

    @Test
    void testReplacePackageName_Basic() {
        String input = "package org.dromara.ruoyi.module.service;";
        String expected = "package com.example.ruoyi.module.service;";
        String result = contentReplacer.replaceContent(input, "ruoyi-module");
        assertEquals(expected, result);
    }

    @Test
    void testReplacePackageName_MultipleOccurrences() {
        String input = "import org.dromara.common.service.OrgService;\nimport org.dromara.ruoyi.module.service;";
        String result = contentReplacer.replaceContent(input, "ruoyi-module");
        assertTrue(result.contains("com.example.common.service"));
        assertTrue(result.contains("com.example.ruoyi.module.service"));
    }

    @Test
    void testRetainModules_Sms4j() {
        // sms4j 应该保持 org.dromara.sms4j
        String input = "import org.dromara.sms4j.service.SmsService;";
        String result = contentReplacer.replaceContent(input, "ruoyi-module");
        assertEquals("import org.dromara.sms4j.service.SmsService;", result);
    }

    @Test
    void testRetainModules_Warm() {
        // warm 应该保持 org.dromara.warm
        String input = "package org.dromara.warm.config;";
        String result = contentReplacer.replaceContent(input, "ruoyi-module");
        assertEquals("package org.dromara.warm.config;", result);
    }

    @Test
    void testModuleNameReplacement_Prefix() {
        config.setOldPrefix("ruoyi");
        config.setNewPrefix("myapp");
        contentReplacer = new ContentReplacer(config);

        String input = "ruoyi-admin module";
        String result = contentReplacer.replaceContent(input, "ruoyi-admin");
        assertTrue(result.contains("myapp-admin"));
    }

    @Test
    void testModuleNameReplacement_ExactMap() {
        Map<String, String> moduleMap = new HashMap<>();
        moduleMap.put("ruoyi-admin", "myapp-server");
        config.setModuleMap(moduleMap);
        contentReplacer = new ContentReplacer(config);

        String input = "ruoyi-admin";
        String result = contentReplacer.replaceContent(input, "ruoyi-admin");
        assertTrue(result.contains("myapp-server"));
    }

    @Test
    void testXmlNamespaceReplacement() {
        String input = "namespace=\"org.dromara.ruoyi.mapper.UserMapper\"";
        String result = contentReplacer.replaceContent(input, "ruoyi-module");
        assertEquals("namespace=\"com.example.ruoyi.mapper.UserMapper\"", result);
    }

    @Test
    void testEmptyContent() {
        String result = contentReplacer.replaceContent("", "ruoyi-module");
        assertEquals("", result);
    }

    @Test
    void testNoMatchContent() {
        // 不包含 org.dromara 的内容应该保持不变
        String input = "package com.other.package;";
        String result = contentReplacer.replaceContent(input, "ruoyi-module");
        assertEquals("package com.other.package;", result);
    }
}
