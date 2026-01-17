package cn.baruto;

import cn.baruto.config.ConfigLoader;
import cn.baruto.config.RenameConfig;
import cn.baruto.core.ContentReplacer;
import cn.baruto.core.PathCalculator;
import cn.baruto.processor.FileProcessor;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;

import java.io.File;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        // 1. 加载并验证配置
        RenameConfig config = ConfigLoader.load("setting.properties");

        // 2. 初始化处理器
        ContentReplacer contentReplacer = new ContentReplacer(config);
        PathCalculator pathCalculator = new PathCalculator(config);
        FileProcessor fileProcessor = new FileProcessor(contentReplacer, pathCalculator, config);

        // 3. 解压源文件
        String codeDir = unzip(config.getZipFilePath());
        File codeDirFile = new File(codeDir);

        // 4. 遍历处理文件
        List<File> files = FileUtil.loopFiles(codeDirFile);
        for (File file : files) {
            fileProcessor.processFile(file, codeDirFile);
        }

        System.out.println("\n改名完成，输出目录：" + config.getOutputDirectory());

        // 5. 清理临时文件
        FileUtil.del(codeDirFile);
    }

    /**
     * 解压 ZIP 文件
     */
    private static String unzip(String zipFilePath) {
        // 创建临时文件夹
        String tempFilePath = System.getProperty("user.dir") + "/temp/ruoyi_" + System.currentTimeMillis();

        File zipFile = new File(zipFilePath);

        // 获取文件名（不带后缀）
        String suffix = "." + FileUtil.extName(zipFilePath);
        String zipFileName = FileUtil.getName(zipFilePath).replaceAll(suffix, "");

        File targetDir = new File(tempFilePath);
        targetDir.mkdirs();
        ZipUtil.unzip(zipFile, targetDir);

        // 返回解压后的代码路径
        return tempFilePath + "/" + zipFileName;
    }
}
