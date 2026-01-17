package cn.baruto.processor;

import cn.baruto.config.RenameConfig;
import cn.baruto.core.ContentReplacer;
import cn.baruto.core.PathCalculator;
import cn.baruto.core.PathUtils;
import cn.hutool.core.io.FileUtil;
import java.io.File;

public class FileProcessor {
    private final ContentReplacer contentReplacer;
    private final PathCalculator pathCalculator;
    private final RenameConfig config;

    public FileProcessor(ContentReplacer contentReplacer, PathCalculator pathCalculator, RenameConfig config) {
        this.contentReplacer = contentReplacer;
        this.pathCalculator = pathCalculator;
        this.config = config;
    }

    public void processFile(File file, File codeDir) {
        String moduleName = PathUtils.inferModule(file, codeDir);
        String targetPath = pathCalculator.calculateTargetPath(file, codeDir);
        File targetFile = new File(targetPath);
        File parentDir = targetFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        String content = FileUtil.readUtf8String(file);
        String newContent = contentReplacer.replaceContent(content, moduleName);
        FileUtil.writeString(newContent, targetPath, "UTF-8");
        System.out.println("写入文件：" + targetPath);
    }
}
