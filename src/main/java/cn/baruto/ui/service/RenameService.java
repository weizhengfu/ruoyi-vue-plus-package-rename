package cn.baruto.ui.service;

import cn.baruto.config.RenameConfig;
import cn.baruto.core.ContentReplacer;
import cn.baruto.core.PathCalculator;
import cn.baruto.processor.FileProcessor;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.File;
import java.util.List;

public class RenameService extends Service<Void> {

    private RenameConfig config;

    public void setConfig(RenameConfig config) {
        this.config = config;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("正在初始化处理器...\n");
                ContentReplacer contentReplacer = new ContentReplacer(config);
                PathCalculator pathCalculator = new PathCalculator(config);
                FileProcessor fileProcessor = new FileProcessor(contentReplacer, pathCalculator, config);
                updateMessage("处理器初始化完成\n");

                updateMessage("正在解压 ZIP 文件: " + config.getZipFilePath() + "\n");
                String codeDir = unzip(config.getZipFilePath());
                File codeDirFile = new File(codeDir);
                updateMessage("解压完成，临时目录: " + codeDir + "\n");

                List<File> files = FileUtil.loopFiles(codeDirFile);
                int total = files.size();
                updateMessage("共找到 " + total + " 个文件\n\n");

                int processed = 0;
                for (File file : files) {
                    fileProcessor.processFile(file, codeDirFile);
                    processed++;
                    if (processed % 50 == 0) {
                        updateMessage("  已处理 " + processed + "/" + total + " 个文件\n");
                        updateProgress(processed, total);
                    }
                }

                updateProgress(total, total);
                updateMessage("\n所有文件处理完成\n");
                updateMessage("输出目录: " + config.getOutputDirectory() + "\n");

                updateMessage("正在清理临时文件...\n");
                FileUtil.del(codeDirFile);
                updateMessage("清理完成\n");

                return null;
            }
        };
    }

    private String unzip(String zipFilePath) {
        String tempFilePath = System.getProperty("user.dir") + "/temp/ruoyi_" + System.currentTimeMillis();
        File zipFile = new File(zipFilePath);
        String suffix = "." + FileUtil.extName(zipFilePath);
        String zipFileName = FileUtil.getName(zipFilePath).replaceAll(suffix, "");
        File targetDir = new File(tempFilePath);
        targetDir.mkdirs();
        ZipUtil.unzip(zipFile, targetDir);
        return tempFilePath + "/" + zipFileName;
    }
}
