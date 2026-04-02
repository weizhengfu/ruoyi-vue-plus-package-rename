package cn.baruto.ui;

import cn.baruto.config.RenameConfig;
import cn.baruto.config.ConfigLoader;
import cn.baruto.core.ContentReplacer;
import cn.baruto.core.PathCalculator;
import cn.baruto.processor.FileProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

/**
 * 包名修改器 GUI 界面
 */
public class RenameApp extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(RenameApp.class);

    // === 配色方案 ===
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);      // 蓝色
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);    // 绿色
    private static final Color BG_COLOR = new Color(236, 240, 241);        // 浅灰背景
    private static final Color PANEL_BG = Color.WHITE;                      // 面板白色
    private static final Color TEXT_DARK = new Color(44, 62, 80);          // 深色文字
    private static final Color TEXT_MUTED = new Color(127, 140, 141);      // 次要文字

    // === 源文件配置 ===
    private JTextField zipPathField;
    private JTextField targetPathField;

    // === 包名配置 ===
    private JTextField packageNameField;
    private JTextField retainModulesField;

    // === 项目配置 ===
    private JTextField projectNameField;
    private JTextField projectVersionField;

    // === 模块名映射配置 ===
    private JTextField oldPrefixField;
    private JTextField newPrefixField;
    private JTextField moduleMapField;

    // === 进度显示 ===
    private JTextArea logArea;
    private JProgressBar progressBar;
    private JButton startButton;

    public RenameApp() {
        initUI();
    }

    private void initUI() {
        setTitle("RuoYi-Vue-Plus 包名修改器");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 720);
        setLocationRelativeTo(null);
        setResizable(true);
        getContentPane().setBackground(BG_COLOR);

        // 主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(BG_COLOR);

        // 标题
        JLabel titleLabel = new JLabel("RuoYi-Vue-Plus 包名修改器");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(5));

        // 副标题
        JLabel subtitleLabel = new JLabel("一键批量修改 RuoYi-Vue-Plus 项目包名");
        subtitleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        // 1. 源文件配置
        mainPanel.add(createSourceConfigPanel());
        mainPanel.add(Box.createVerticalStrut(12));

        // 2. 包名配置
        mainPanel.add(createPackageConfigPanel());
        mainPanel.add(Box.createVerticalStrut(12));

        // 3. 项目配置
        mainPanel.add(createProjectConfigPanel());
        mainPanel.add(Box.createVerticalStrut(12));

        // 4. 模块名映射配置
        mainPanel.add(createModuleMapPanel());
        mainPanel.add(Box.createVerticalStrut(15));

        // 5. 操作面板
        mainPanel.add(createActionPanel());
        mainPanel.add(Box.createVerticalStrut(10));

        // 6. 日志和进度
        mainPanel.add(createLogPanel());

        // 添加滚动面板
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        setContentPane(scrollPane);
    }

    private JPanel createSourceConfigPanel() {
        JPanel panel = createTitledPanel("源文件配置");

        // ZIP 文件路径
        zipPathField = new JTextField(40);
        JButton zipBrowseBtn = new JButton("浏览...");
        zipBrowseBtn.addActionListener(this::browseZipFile);
        createLabelRow(panel, "ZIP 文件路径：", zipPathField, zipBrowseBtn);

        // 输出目录
        targetPathField = new JTextField(40);
        targetPathField.setText("D:\\rename_output");
        JButton targetBrowseBtn = new JButton("浏览...");
        targetBrowseBtn.addActionListener(this::browseTargetPath);
        createLabelRow(panel, "输出目录：", targetPathField, targetBrowseBtn);

        return panel;
    }

    private JPanel createPackageConfigPanel() {
        JPanel panel = createTitledPanel("包名配置");

        // 目标包名
        packageNameField = new JTextField(40);
        packageNameField.setText("com.example");
        createLabelRow(panel, "目标包名：", packageNameField, null);

        // 保留模块
        retainModulesField = new JTextField(40);
        retainModulesField.setToolTipText("逗号分隔，如：sms4j,warm");
        createLabelRow(panel, "保留模块：", retainModulesField, null);

        return panel;
    }

    private JPanel createProjectConfigPanel() {
        JPanel panel = createTitledPanel("项目配置");

        // 项目名称
        projectNameField = new JTextField(40);
        projectNameField.setText("MyProject");
        createLabelRow(panel, "项目名称：", projectNameField, null);

        // 项目版本
        projectVersionField = new JTextField(40);
        projectVersionField.setText("1.0.0");
        createLabelRow(panel, "项目版本：", projectVersionField, null);

        return panel;
    }

    private JPanel createModuleMapPanel() {
        JPanel panel = createTitledPanel("模块名映射配置");

        // 前缀替换
        oldPrefixField = new JTextField(18);
        oldPrefixField.setText("ruoyi");
        newPrefixField = new JTextField(18);
        newPrefixField.setText("myapp");
        createDualLabelRow(panel, "模块前缀替换：", "旧前缀", oldPrefixField, "新前缀", newPrefixField);

        // 精确映射
        moduleMapField = new JTextField(40);
        moduleMapField.setToolTipText("格式：旧模块:新模块,旧模块:新模块，如：ruoyi-admin:myapp-server");
        createLabelRow(panel, "精确模块映射：", moduleMapField, null);

        return panel;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(BG_COLOR);

        startButton = new JButton("开始处理");
        startButton.setPreferredSize(new Dimension(150, 45));
        startButton.setFont(new Font("微软雅黑", Font.BOLD, 15));
        startButton.setBackground(PRIMARY_COLOR);
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setBorderPainted(false);
        startButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startButton.addActionListener(this::startProcessing);
        panel.add(startButton);

        return panel;
    }

    private JPanel createLogPanel() {
        JPanel panel = createTitledPanel("处理日志");
        panel.setBackground(PANEL_BG);

        logArea = new JTextArea(10, 70);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(44, 62, 80));
        logArea.setForeground(new Color(46, 204, 113));  // 绿色文字
        logArea.setCaretColor(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(750, 180));
        panel.add(scrollPane);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(750, 20));
        progressBar.setVisible(false);
        progressBar.setForeground(PRIMARY_COLOR);
        progressBar.setBackground(new Color(200, 200, 200));
        panel.add(Box.createVerticalStrut(5));
        panel.add(progressBar);

        return panel;
    }

    private JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // 标题标签
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        titleLabel.setForeground(PRIMARY_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(titleLabel, gbc);

        // 分隔线
        JSeparator separator = new JSeparator();
        separator.setPreferredSize(new Dimension(700, 1));
        separator.setForeground(new Color(200, 200, 200));
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 0, 5, 0);
        panel.add(separator, gbc);

        return panel;
    }

    private JPanel createLabelRow(JPanel parent, String labelText, JTextField textField, JButton button) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int nextGridy = parent.getComponentCount() / 3;  // 每行最多3个组件：label + textField + button

        gbc.gridx = 0;
        gbc.gridy = nextGridy;
        gbc.weightx = 0;
        parent.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        parent.add(textField, gbc);

        if (button != null) {
            gbc.gridx = 2;
            gbc.weightx = 0;
            parent.add(button, gbc);
        }

        return parent;
    }

    private JPanel createDualLabelRow(JPanel parent, String labelText, String label1, JTextField field1, String label2, JTextField field2) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int nextGridy = parent.getComponentCount();

        // 左上角标签（跨两行）
        gbc.gridx = 0;
        gbc.gridy = nextGridy;
        gbc.gridheight = 2;
        gbc.weightx = 0;
        parent.add(new JLabel(labelText), gbc);

        // 第一行标签和输入框
        gbc.gridx = 1;
        gbc.gridy = nextGridy;
        gbc.gridheight = 1;
        gbc.weightx = 0.3;
        parent.add(new JLabel(label1), gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.7;
        parent.add(field1, gbc);

        // 第二行标签和输入框
        gbc.gridx = 1;
        gbc.gridy = nextGridy + 1;
        gbc.weightx = 0.3;
        parent.add(new JLabel(label2), gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.7;
        parent.add(field2, gbc);

        return parent;
    }

    private void browseZipFile(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("ZIP 文件 (*.zip)", "zip"));
        chooser.setDialogTitle("选择 RuoYi-Vue-Plus ZIP 文件");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            zipPathField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void browseTargetPath(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("选择输出目录");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            targetPathField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void startProcessing(ActionEvent e) {
        if (!validateInputs()) {
            return;
        }

        startButton.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setValue(0);
        progressBar.setIndeterminate(true);
        logArea.setText("开始处理...\n");

        // 使用 SwingWorker 在后台执行任务
        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // 1. 构建配置
                    RenameConfig config = buildConfig();
                    publish("配置加载完成\n");

                    // 2. 初始化处理器
                    ContentReplacer contentReplacer = new ContentReplacer(config);
                    PathCalculator pathCalculator = new PathCalculator(config);
                    FileProcessor fileProcessor = new FileProcessor(contentReplacer, pathCalculator, config);
                    publish("处理器初始化完成\n");

                    // 3. 解压文件
                    publish("正在解压 ZIP 文件...\n");
                    String codeDir = unzip(config.getZipFilePath());
                    File codeDirFile = new File(codeDir);
                    publish("解压完成，临时目录：" + codeDir + "\n");

                    // 4. 遍历处理文件
                    publish("开始处理文件...\n");
                    List<java.io.File> files = cn.hutool.core.io.FileUtil.loopFiles(codeDirFile);
                    int total = files.size();
                    int processed = 0;

                    for (java.io.File file : files) {
                        fileProcessor.processFile(file, codeDirFile);
                        processed++;
                        if (processed % 50 == 0) {
                            publish("已处理 " + processed + "/" + total + " 个文件\n");
                        }
                    }

                    publish("\n所有文件处理完成！\n");
                    publish("输出目录：" + config.getOutputDirectory() + "\n");

                    // 5. 清理临时文件
                    publish("正在清理临时文件...\n");
                    cn.hutool.core.io.FileUtil.del(codeDirFile);
                    publish("清理完成！\n");

                } catch (Exception ex) {
                    publish("\n错误：" + ex.getMessage() + "\n");
                    logger.error("处理文件时发生错误", ex);
                    throw ex;
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    logArea.append(message);
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                }
            }

            @Override
            protected void done() {
                progressBar.setIndeterminate(false);
                progressBar.setValue(100);
                startButton.setEnabled(true);
                try {
                    get();
                    JOptionPane.showMessageDialog(RenameApp.this, "处理完成！", "成功", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    logger.error("获取处理结果时发生错误", ex);
                    JOptionPane.showMessageDialog(RenameApp.this, "处理失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private boolean validateInputs() {
        if (zipPathField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择 ZIP 文件", "验证错误", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        File zipFile = new File(zipPathField.getText());
        if (!zipFile.exists()) {
            JOptionPane.showMessageDialog(this, "ZIP 文件不存在", "验证错误", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (packageNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入目标包名", "验证错误", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (projectNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入项目名称", "验证错误", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (targetPathField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择输出目录", "验证错误", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private RenameConfig buildConfig() {
        RenameConfig config = new RenameConfig();
        config.setZipFilePath(zipPathField.getText().trim());
        config.setTargetPath(targetPathField.getText().trim());
        config.setTargetPackageName(packageNameField.getText().trim());
        config.setRetainModules(parseRetainModules(retainModulesField.getText()));
        config.setProjectName(projectNameField.getText().trim());
        config.setProjectVersion(projectVersionField.getText().trim());
        config.setOldPrefix(oldPrefixField.getText().trim());
        config.setNewPrefix(newPrefixField.getText().trim());
        config.setModuleMap(parseModuleMap(moduleMapField.getText()));
        return config;
    }

    private java.util.Set<String> parseRetainModules(String value) {
        if (value == null || value.trim().isEmpty()) {
            return java.util.Collections.emptySet();
        }
        return new java.util.HashSet<>(java.util.Arrays.asList(value.trim().split(",")));
    }

    private java.util.Map<String, String> parseModuleMap(String value) {
        java.util.Map<String, String> map = new java.util.HashMap<>();
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

    private String unzip(String zipFilePath) {
        String tempFilePath = System.getProperty("user.dir") + "/temp/ruoyi_" + System.currentTimeMillis();
        java.io.File zipFile = new java.io.File(zipFilePath);

        String suffix = "." + cn.hutool.core.io.FileUtil.extName(zipFilePath);
        String zipFileName = cn.hutool.core.io.FileUtil.getName(zipFilePath).replaceAll(suffix, "");

        java.io.File targetDir = new java.io.File(tempFilePath);
        targetDir.mkdirs();
        cn.hutool.core.util.ZipUtil.unzip(zipFile, targetDir);

        return tempFilePath + "/" + zipFileName;
    }
}
