package cn.baruto.ui;

import cn.baruto.config.RenameConfig;
import cn.baruto.config.ConfigLoader;
import cn.baruto.core.ContentReplacer;
import cn.baruto.core.PathCalculator;
import cn.baruto.processor.FileProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

import static cn.baruto.ui.ModernUI.*;

/**
 * 包名修改器 GUI 界面 - 现代化版本
 */
public class RenameApp extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(RenameApp.class);

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

    // === 主面板引用 ===
    private JPanel headerPanel;
    private JPanel contentPanel;

    public RenameApp() {
        initUI();
    }

    private void initUI() {
        setTitle("RuoYi-Vue-Plus 包名修改器");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 750);
        setLocationRelativeTo(null);
        setResizable(true);
        setMinimumSize(new Dimension(800, 600));
        getContentPane().setBackground(BG_MAIN);

        // 设置窗口图标（可选）
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/icon.png"));
            if (icon.getIconWidth() != -1) {
                setIconImage(icon.getImage());
            }
        } catch (Exception e) {
            // 忽略图标加载失败
        }

        // 主布局
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_MAIN);

        // 1. 顶部标题区域
        headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. 内容区域
        contentPanel = createContentPanel();
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setEnabled(false);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new GradientPanel(PRIMARY_COLOR, PRIMARY_DARK);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));
        panel.setPreferredSize(new Dimension(0, 140));

        // 左侧标题
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        // 主标题
        JLabel titleLabel = new JLabel("RuoYi-Vue-Plus 包名修改器");
        titleLabel.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_WHITE);
        titlePanel.add(titleLabel, BorderLayout.NORTH);

        // 副标题
        JLabel subtitleLabel = new JLabel("一键批量修改 RuoYi-Vue-Plus 项目包名，快速重构代码结构");
        subtitleLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));
        subtitleLabel.setBorder(new EmptyBorder(8, 0, 0, 0));
        titlePanel.add(subtitleLabel, BorderLayout.CENTER);

        panel.add(titlePanel, BorderLayout.CENTER);

        // 右侧版本信息
        JPanel versionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        versionPanel.setOpaque(false);
        JLabel versionLabel = new JLabel("v1.0");
        versionLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));
        versionLabel.setForeground(new Color(255, 255, 255, 150));
        versionPanel.add(versionLabel);
        panel.add(versionPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_MAIN);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 15, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;

        // 添加卡片式面板
        gbc.insets = new Insets(0, 20, 15, 20);
        
        // 1. 源文件配置
        panel.add(createSourceConfigPanel(), gbc);
        gbc.gridy++;

        // 2. 包名配置
        panel.add(createPackageConfigPanel(), gbc);
        gbc.gridy++;

        // 3. 项目配置
        panel.add(createProjectConfigPanel(), gbc);
        gbc.gridy++;

        // 4. 模块名映射配置
        panel.add(createModuleMapPanel(), gbc);
        gbc.gridy++;

        // 5. 操作和日志区域
        gbc.insets = new Insets(0, 20, 20, 20);
        panel.add(createActionAndLogPanel(), gbc);
        gbc.gridy++;

        return panel;
    }

    private JPanel createSourceConfigPanel() {
        JPanel panel = createCardPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(20, 25, 20, 25)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;

        // 标题
        JLabel titleLabel = new JLabel("源文件配置");
        titleLabel.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 15));
        titleLabel.setForeground(PRIMARY_COLOR);
        gbc.gridwidth = 3;
        panel.add(titleLabel, gbc);
        gbc.gridy++;

        // ZIP 文件路径
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        JLabel zipLabel = createLabel("ZIP 文件：");
        zipLabel.setPreferredSize(new Dimension(80, 30));
        panel.add(zipLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        zipPathField = createTextField();
        zipPathField.setPreferredSize(new Dimension(0, 36));
        panel.add(zipPathField, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton zipBrowseBtn = createButton("浏览...", false);
        zipBrowseBtn.setPreferredSize(new Dimension(90, 36));
        zipBrowseBtn.addActionListener(this::browseZipFile);
        panel.add(zipBrowseBtn, gbc);
        gbc.gridy++;

        // 输出目录
        gbc.gridx = 0;
        JLabel targetLabel = createLabel("输出目录：");
        targetLabel.setPreferredSize(new Dimension(80, 30));
        panel.add(targetLabel, gbc);
        gbc.gridx = 1;
        targetPathField = createTextField();
        targetPathField.setText("D:\\rename_output");
        targetPathField.setPreferredSize(new Dimension(0, 36));
        panel.add(targetPathField, gbc);
        gbc.gridx = 2;
        JButton targetBrowseBtn = createButton("浏览...", false);
        targetBrowseBtn.setPreferredSize(new Dimension(90, 36));
        targetBrowseBtn.addActionListener(this::browseTargetPath);
        panel.add(targetBrowseBtn, gbc);

        return panel;
    }

    private JPanel createPackageConfigPanel() {
        JPanel panel = createCardPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(20, 25, 20, 25)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;

        // 标题
        JLabel titleLabel = new JLabel("包名配置");
        titleLabel.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 15));
        titleLabel.setForeground(PRIMARY_COLOR);
        gbc.gridwidth = 3;
        panel.add(titleLabel, gbc);
        gbc.gridy++;

        // 目标包名
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        JLabel packageLabel = createLabel("目标包名：");
        packageLabel.setPreferredSize(new Dimension(80, 30));
        panel.add(packageLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.gridwidth = 2;
        packageNameField = createTextField();
        packageNameField.setText("com.example");
        packageNameField.setPreferredSize(new Dimension(0, 36));
        panel.add(packageNameField, gbc);
        gbc.gridy++;

        // 保留模块
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        JLabel retainLabel = createLabel("保留模块：");
        retainLabel.setPreferredSize(new Dimension(80, 30));
        panel.add(retainLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.gridwidth = 2;
        retainModulesField = createTextField();
        retainModulesField.setText("sms4j,warm");
        retainModulesField.setToolTipText("逗号分隔，如：sms4j,warm");
        retainModulesField.setPreferredSize(new Dimension(0, 36));
        panel.add(retainModulesField, gbc);

        return panel;
    }

    private JPanel createProjectConfigPanel() {
        JPanel panel = createCardPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(20, 25, 20, 25)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;

        // 标题
        JLabel titleLabel = new JLabel("项目配置");
        titleLabel.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 15));
        titleLabel.setForeground(PRIMARY_COLOR);
        gbc.gridwidth = 3;
        panel.add(titleLabel, gbc);
        gbc.gridy++;

        // 项目名称
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        JLabel projectLabel = createLabel("项目名称：");
        projectLabel.setPreferredSize(new Dimension(80, 30));
        panel.add(projectLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.gridwidth = 2;
        projectNameField = createTextField();
        projectNameField.setText("MyProject");
        projectNameField.setPreferredSize(new Dimension(0, 36));
        panel.add(projectNameField, gbc);
        gbc.gridy++;

        // 项目版本
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        JLabel versionLabel = createLabel("项目版本：");
        versionLabel.setPreferredSize(new Dimension(80, 30));
        panel.add(versionLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.gridwidth = 2;
        projectVersionField = createTextField();
        projectVersionField.setText("1.0.0");
        projectVersionField.setPreferredSize(new Dimension(0, 36));
        panel.add(projectVersionField, gbc);

        return panel;
    }

    private JPanel createModuleMapPanel() {
        JPanel panel = createCardPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(20, 25, 20, 25)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;

        // 标题
        JLabel titleLabel = new JLabel("模块名映射配置");
        titleLabel.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 15));
        titleLabel.setForeground(PRIMARY_COLOR);
        gbc.gridwidth = 3;
        panel.add(titleLabel, gbc);
        gbc.gridy++;

        // 前缀替换（一行显示）
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        JLabel prefixLabel = createLabel("前缀替换：");
        prefixLabel.setPreferredSize(new Dimension(80, 30));
        panel.add(prefixLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        oldPrefixField = createTextField();
        oldPrefixField.setText("ruoyi");
        oldPrefixField.setPreferredSize(new Dimension(0, 36));
        panel.add(oldPrefixField, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0.05;
        JLabel arrowLabel = new JLabel("→");
        arrowLabel.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 16));
        arrowLabel.setForeground(TEXT_SECONDARY);
        arrowLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(arrowLabel, gbc);
        
        gbc.gridx = 3;
        gbc.weightx = 0.35;
        newPrefixField = createTextField();
        newPrefixField.setText("myapp");
        newPrefixField.setPreferredSize(new Dimension(0, 36));
        panel.add(newPrefixField, gbc);
        
        gbc.gridy++;
        gbc.gridx = 0;

        // 精确映射
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        JLabel mapLabel = createLabel("精确映射：");
        mapLabel.setPreferredSize(new Dimension(80, 30));
        panel.add(mapLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.gridwidth = 3;
        moduleMapField = createTextField();
        moduleMapField.setToolTipText("格式：旧：新，旧：新");
        moduleMapField.setPreferredSize(new Dimension(0, 36));
        panel.add(moduleMapField, gbc);

        return panel;
    }

    private JPanel createActionAndLogPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_MAIN);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 15, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;

        // 操作按钮面板
        JPanel buttonPanel = createCardPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonPanel.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(20, 25, 20, 25)
        ));

        startButton = createButton("开始处理", true);
        startButton.setPreferredSize(new Dimension(180, 45));
        startButton.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 16));
        startButton.addActionListener(this::startProcessing);
        buttonPanel.add(startButton);

        panel.add(buttonPanel, gbc);
        gbc.gridy++;

        // 日志面板
        panel.add(createLogPanel(), gbc);

        return panel;
    }

    private JPanel createLogPanel() {
        JPanel panel = createCardPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(20, 25, 20, 25)
        ));
        panel.setPreferredSize(new Dimension(0, 280));

        // 日志标题
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(BG_PANEL);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("处理日志");
        titleLabel.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 15));
        titleLabel.setForeground(PRIMARY_COLOR);
        headerPanel.add(titleLabel);

        panel.add(headerPanel, BorderLayout.NORTH);

        // 日志区域
        logArea = new JTextArea(10, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));
        logArea.setBackground(new Color(40, 44, 52));
        logArea.setForeground(new Color(152, 195, 121));
        logArea.setCaretColor(new Color(152, 195, 121));
        logArea.setSelectionColor(new Color(61, 68, 85));
        logArea.setSelectedTextColor(new Color(152, 195, 121));
        logArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        logArea.setLineWrap(true);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(new RoundedBorder(BORDER_RADIUS, new Color(60, 63, 65)));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 进度条
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(0, 28));
        progressBar.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 12));
        progressBar.setForeground(SUCCESS_COLOR);
        progressBar.setBackground(new Color(236, 240, 241));
        progressBar.setBorder(new RoundedBorder(BORDER_RADIUS, BORDER_COLOR));
        progressBar.setString("就绪");
        progressBar.setVisible(false);

        panel.add(Box.createVerticalStrut(15), BorderLayout.SOUTH);
        panel.add(progressBar, BorderLayout.SOUTH);

        return panel;
    }

    private void browseZipFile(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("ZIP 文件 (*.zip)", "zip"));
        chooser.setDialogTitle("选择 RuoYi-Vue-Plus ZIP 文件");
        chooser.setBackground(BG_PANEL);
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            zipPathField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void browseTargetPath(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("选择输出目录");
        chooser.setBackground(BG_PANEL);
        
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
        progressBar.setString("正在处理...");
        logArea.setText("");
        logArea.append("════════════════════════════════════════\n");
        logArea.append("  开始处理任务\n");
        logArea.append("════════════════════════════════════════\n\n");

        // 使用 SwingWorker 在后台执行任务
        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // 1. 构建配置
                    RenameConfig config = buildConfig();
                    publish("✓ 配置加载完成\n");

                    // 2. 初始化处理器
                    ContentReplacer contentReplacer = new ContentReplacer(config);
                    PathCalculator pathCalculator = new PathCalculator(config);
                    FileProcessor fileProcessor = new FileProcessor(contentReplacer, pathCalculator, config);
                    publish("✓ 处理器初始化完成\n");

                    // 3. 解压文件
                    publish("→ 正在解压 ZIP 文件...\n");
                    String codeDir = unzip(config.getZipFilePath());
                    File codeDirFile = new File(codeDir);
                    publish("✓ 解压完成，临时目录：" + codeDir + "\n");

                    // 4. 遍历处理文件
                    publish("→ 开始处理文件...\n");
                    List<java.io.File> files = cn.hutool.core.io.FileUtil.loopFiles(codeDirFile);
                    int total = files.size();
                    int processed = 0;

                    for (java.io.File file : files) {
                        fileProcessor.processFile(file, codeDirFile);
                        processed++;
                        if (processed % 50 == 0) {
                            publish("  已处理 " + processed + "/" + total + " 个文件\n");
                        }
                    }

                    publish("\n✓ 所有文件处理完成！\n");
                    publish("→ 输出目录：" + config.getOutputDirectory() + "\n");

                    // 5. 清理临时文件
                    publish("→ 正在清理临时文件...\n");
                    cn.hutool.core.io.FileUtil.del(codeDirFile);
                    publish("✓ 清理完成！\n");

                } catch (Exception ex) {
                    publish("\n✗ 错误：" + ex.getMessage() + "\n");
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
                progressBar.setString("处理完成");
                startButton.setEnabled(true);
                try {
                    get();
                    logArea.append("\n════════════════════════════════════════\n");
                    logArea.append("  ✓ 任务成功完成\n");
                    logArea.append("════════════════════════════════════════\n");
                    JOptionPane.showMessageDialog(
                        RenameApp.this, 
                        "处理完成！", 
                        "成功", 
                        JOptionPane.INFORMATION_MESSAGE,
                        createSuccessIcon()
                    );
                } catch (Exception ex) {
                    logger.error("获取处理结果时发生错误", ex);
                    logArea.append("\n════════════════════════════════════════\n");
                    logArea.append("  ✗ 任务失败\n");
                    logArea.append("════════════════════════════════════════\n");
                    JOptionPane.showMessageDialog(
                        RenameApp.this, 
                        "处理失败：" + ex.getMessage(), 
                        "错误", 
                        JOptionPane.ERROR_MESSAGE,
                        createErrorIcon()
                    );
                }
            }
        }.execute();
    }

    private ImageIcon createSuccessIcon() {
        // 创建简单的成功图标
        int size = 48;
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(SUCCESS_COLOR);
        g2d.fillOval(4, 4, size - 8, size - 8);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(14, 24, 20, 30);
        g2d.drawLine(20, 30, 34, 16);
        g2d.dispose();
        return new ImageIcon(image);
    }

    private ImageIcon createErrorIcon() {
        // 创建简单的错误图标
        int size = 48;
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(DANGER_COLOR);
        g2d.fillOval(4, 4, size - 8, size - 8);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(16, 16, 32, 32);
        g2d.drawLine(32, 16, 16, 32);
        g2d.dispose();
        return new ImageIcon(image);
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
