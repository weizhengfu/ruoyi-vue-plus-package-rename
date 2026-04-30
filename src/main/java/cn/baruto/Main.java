package cn.baruto;

import cn.baruto.config.ConfigLoader;
import cn.baruto.config.RenameConfig;
import cn.baruto.core.ContentReplacer;
import cn.baruto.core.PathCalculator;
import cn.baruto.processor.FileProcessor;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        if (args.length == 0 || !args[0].equals("--cli")) {
            cn.baruto.ui.App.launch(cn.baruto.ui.App.class, args);
            return;
        }
        startCLIMode();
    }

    /**
     * 启动命令行模式
     */
    private static void startCLIMode() {
        logger.info("开始 RuoYi-Vue-Plus 包名修改工具...");

        // 1. 加载并验证配置
        RenameConfig config = ConfigLoader.load("setting.properties");
        logger.info("配置加载完成");

        // 2. 初始化处理器
        ContentReplacer contentReplacer = new ContentReplacer(config);
        PathCalculator pathCalculator = new PathCalculator(config);
        FileProcessor fileProcessor = new FileProcessor(contentReplacer, pathCalculator, config);
        logger.info("处理器初始化完成");

        // 3. 解压源文件
        logger.info("正在解压 ZIP 文件: {}", config.getZipFilePath());
        String codeDir = unzip(config.getZipFilePath());
        File codeDirFile = new File(codeDir);
        logger.info("解压完成，临时目录: {}", codeDir);

        // 4. 遍历处理文件
        List<File> files = FileUtil.loopFiles(codeDirFile);
        logger.info("共找到 {} 个文件需要处理", files.size());

        int total = files.size();
        int processed = 0;
        int lastPercent = 0;
        for (File file : files) {
            fileProcessor.processFile(file, codeDirFile);
            processed++;
            int percent = (int) ((processed * 100.0) / total);
            // 每处理 1% 或每 50 个文件输出一次进度
            if (percent > lastPercent || processed % 50 == 0) {
                System.out.print("\r进度: " + processed + "/" + total + " (" + percent + "%)");
                lastPercent = percent;
            }
        }
        System.out.println();  // 换行

        logger.info("改名完成，输出目录：{}", config.getOutputDirectory());

        // 5. 清理临时文件
        logger.info("正在清理临时文件...");
        try {
            FileUtil.del(codeDirFile);
            logger.info("清理完成");
        } catch (Exception e) {
            logger.warn("清理临时文件失败: {} - 请手动删除", codeDirFile.getAbsolutePath(), e);
        }
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
