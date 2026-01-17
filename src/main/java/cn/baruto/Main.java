package cn.baruto;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.setting.dialect.Props;
import cn.hutool.setting.dialect.PropsUtil;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        Props props = PropsUtil.get("setting.properties");
        String zipFilePath = props.getProperty("zip.name");

        // 获取到解压之后的代码路径
        String codeDir = unzip(zipFilePath);

        File codeDirFile = new File(codeDir);
        String targetDir = props.getProperty("target.path") + File.separator
                + DateUtil.format(new Date(), "yyyyMMddHHmmss");

        String targetPackageName = props.getProperty("package.name");
        List<File> files = FileUtil.loopFiles(codeDirFile);
        for (File file : files) {
            if (file.getName().equals("pom.xml")) {
                // 修改pom.xml
                renamePomXml(file, codeDirFile, targetDir, targetPackageName);
            } else if (file.getName().endsWith(".java")) {
                // 修改java文件
                renameJava(file, codeDirFile, targetDir, targetPackageName);
            } else if (file.getName().endsWith("Mapper.xml")) {
                // 修改mapperXml文件
                renameMapperXml(file, codeDirFile, targetDir, targetPackageName);
            } else {
                // 复制其他文件
                renameOther(file, codeDirFile, targetDir, targetPackageName);
            }
        }

        System.out.println("改名完成，新的文件路径：" + targetDir);

        // 删除解压的文件
        FileUtil.del(codeDirFile);
    }

    private static void renamePomXml(File pomFile, File codeDirFile, String targetPath, String targetPackageName) {
        // 修改pom.xml
        String fileContent = FileUtil.readUtf8String(pomFile);
        String newContent = fileContent.replaceAll("org.dromara", targetPackageName);
        if (newContent.contains("sms4j")) {
            newContent = newContent.replaceAll(targetPackageName + ".sms4j", "org.dromara.sms4j");
        }

        String targetFileName = getTargetFileName(pomFile, codeDirFile, targetPath, targetPackageName);
        FileUtil.writeString(newContent, targetFileName, "UTF-8");
        System.out.println("写入文件：" + targetFileName);
    }

    private static void renameMapperXml(File mapperFile, File codeDirFile, String targetPath,
            String targetPackageName) {
        // 修改pom.xml
        String fileContent = FileUtil.readUtf8String(mapperFile);
        String newContent = fileContent.replaceAll("org.dromara", targetPackageName);

        String targetFileName = getTargetFileName(mapperFile, codeDirFile, targetPath, targetPackageName);
        FileUtil.writeString(newContent, targetFileName, "UTF-8");
        System.out.println("写入文件：" + targetFileName);
    }

    private static void renameJava(File javaFile, File codeDirFile, String targetPath, String targetPackageName) {
        // 修改pom.xml
        String fileContent = FileUtil.readUtf8String(javaFile);
        String newContent = fileContent.replaceAll("org.dromara", targetPackageName);
        if (newContent.contains("sms4j")) {
            newContent = newContent.replaceAll(targetPackageName + ".sms4j", "org.dromara.sms4j");
        }
        String targetFileName = getTargetFileName(javaFile, codeDirFile, targetPath, targetPackageName);
        FileUtil.writeString(newContent, targetFileName, "UTF-8");
        System.out.println("写入文件：" + targetFileName);
    }

    private static void renameOther(File file, File codeDirFile, String targetPath, String targetPackageName) {
        // 修改pom.xml
        String fileContent = FileUtil.readUtf8String(file);
        String newContent = fileContent.replaceAll("org.dromara", targetPackageName);

        String targetFileName = getTargetFileName(file, codeDirFile, targetPath, targetPackageName);
        FileUtil.writeString(newContent, targetFileName, "UTF-8");
        System.out.println("写入文件：" + targetFileName);
    }

    private static String getTargetFileName(File oldFile, File codeDirFile, String targetPath,
            String targetPackageName) {
        String codeDirFileAbsolutePath = getFilePath(codeDirFile);
        String oldFileAbsolutePath = getFilePath(oldFile);

        String keywords = Arrays.asList("src", "main", "java", "org", "dromara").stream()
                .collect(Collectors.joining("/"));

        List<String> targetKeywordList = ListUtil.toList("src", "main", "java");
        String[] split = targetPackageName.split("\\.");
        targetKeywordList.addAll(Arrays.asList(split));

        String keywordsTarget = String.join("/", targetKeywordList);
        if (oldFileAbsolutePath.contains(keywords)) {
            oldFileAbsolutePath = oldFileAbsolutePath.replaceAll(keywords, keywordsTarget);
        }

        String relativePath = oldFileAbsolutePath.replace(codeDirFileAbsolutePath, "");
        String result = targetPath + File.separator + relativePath;
        return result;
    }

    private static String unzip(String zipFilePath) {
        // 创建一个本地临时文件夹
        String tempFilePath = System.getProperty("user.dir") + "/temp/ruoyi_" + System.currentTimeMillis();

        File zipFile = new File(zipFilePath);

        // 获取文件名，不带后缀的

        String suffix = "." + FileUtil.extName(zipFilePath);
        String zipFileName = FileUtil.getName(zipFilePath).replaceAll(suffix, "");

        File targetDir = new File(tempFilePath);
        targetDir.mkdirs();
        ZipUtil.unzip(zipFile, targetDir);

        // 解压出来的代码路径
        return tempFilePath + "/" + zipFileName;
    }

    private static String getFilePath(File file) {
        String absolutePath = file.getAbsolutePath();
        return absolutePath.replaceAll("\\\\", "/");
    }

}
