package cn.baruto.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * 现代化 UI 组件工具类
 */
public class ModernUI {

    // === 配色方案 ===
    public static final Color PRIMARY_COLOR = new Color(52, 152, 219);        // 亮蓝色
    public static final Color PRIMARY_DARK = new Color(41, 128, 185);         // 深蓝
    public static final Color PRIMARY_LIGHT = new Color(135, 206, 250);       // 浅蓝
    public static final Color SUCCESS_COLOR = new Color(46, 204, 113);        // 绿色
    public static final Color WARNING_COLOR = new Color(241, 196, 15);        // 黄色
    public static final Color DANGER_COLOR = new Color(231, 76, 60);          // 红色
    public static final Color INFO_COLOR = new Color(52, 152, 219);           // 信息蓝

    // === 背景色 ===
    public static final Color BG_MAIN = new Color(245, 247, 250);             // 主背景
    public static final Color BG_PANEL = Color.WHITE;                         // 面板背景
    public static final Color BG_CARD = new Color(255, 255, 255);             // 卡片背景
    public static final Color BG_INPUT = new Color(255, 255, 255);            // 输入框背景
    public static final Color BG_INPUT_DISABLED = new Color(240, 240, 240);   // 禁用输入框

    // === 文字颜色 ===
    public static final Color TEXT_PRIMARY = new Color(44, 62, 80);           // 主文字
    public static final Color TEXT_SECONDARY = new Color(127, 140, 141);      // 次要文字
    public static final Color TEXT_MUTED = new Color(189, 195, 199);          // 弱化文字
    public static final Color TEXT_WHITE = Color.WHITE;                       // 白色文字

    // === 边框和阴影 ===
    public static final Color BORDER_COLOR = new Color(226, 232, 240);        // 边框色
    public static final Color BORDER_FOCUS = PRIMARY_COLOR;                   // 焦点边框
    public static final Color SHADOW_COLOR = new Color(0, 0, 0, 30);          // 阴影色

    // === 尺寸规范 ===
    public static final int BORDER_RADIUS_SMALL = 6;                          // 小圆角
    public static final int BORDER_RADIUS = 8;                                // 标准圆角
    public static final int BORDER_RADIUS_LARGE = 12;                         // 大圆角
    public static final int PADDING_SMALL = 8;                                // 小间距
    public static final int PADDING = 12;                                     // 标准间距
    public static final int PADDING_LARGE = 20;                               // 大间距

    /**
     * 创建圆角边框
     */
    public static Border createRoundedBorder(int radius) {
        return new RoundedBorder(radius, BORDER_COLOR);
    }

    /**
     * 创建带阴影的圆角边框
     */
    public static Border createShadowBorder(int radius) {
        return new CompoundBorder(
            new RoundedBorder(radius, BORDER_COLOR),
            new EmptyBorder(2, 2, 2, 2)
        );
    }

    /**
     * 创建焦点边框
     */
    public static Border createFocusBorder(int radius) {
        return new RoundedBorder(radius, BORDER_FOCUS);
    }

    /**
     * 创建标题边框
     */
    public static Border createTitledBorder(String title) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                new RoundedBorder(BORDER_RADIUS, BORDER_COLOR),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Microsoft YaHei UI", Font.BOLD, 13),
                PRIMARY_COLOR
            ),
            new EmptyBorder(PADDING, PADDING, PADDING, PADDING)
        );
    }

    /**
     * 创建现代化按钮
     */
    public static JButton createButton(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (primary) {
            button.setBackground(PRIMARY_COLOR);
            button.setForeground(TEXT_WHITE);
            button.setBorder(new RoundedBorder(BORDER_RADIUS, PRIMARY_COLOR));
        } else {
            button.setBackground(BG_PANEL);
            button.setForeground(TEXT_PRIMARY);
            button.setBorder(new RoundedBorder(BORDER_RADIUS, BORDER_COLOR));
        }

        // 添加悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (primary) {
                    button.setBackground(PRIMARY_DARK);
                } else {
                    button.setBackground(new Color(248, 249, 250));
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (primary) {
                    button.setBackground(PRIMARY_COLOR);
                } else {
                    button.setBackground(BG_PANEL);
                }
            }
        });

        return button;
    }

    /**
     * 创建现代化输入框
     */
    public static JTextField createTextField() {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 13));
        textField.setBackground(BG_INPUT);
        textField.setForeground(TEXT_PRIMARY);
        textField.setBorder(new RoundedBorder(BORDER_RADIUS, BORDER_COLOR));
        textField.setCaretColor(PRIMARY_COLOR);
        textField.setSelectionColor(PRIMARY_LIGHT);
        textField.setSelectedTextColor(TEXT_PRIMARY);

        // 添加焦点监听
        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                textField.setBorder(new RoundedBorder(BORDER_RADIUS, BORDER_FOCUS));
            }

            public void focusLost(java.awt.event.FocusEvent e) {
                textField.setBorder(new RoundedBorder(BORDER_RADIUS, BORDER_COLOR));
            }
        });

        return textField;
    }

    /**
     * 创建标签
     */
    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 13));
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    /**
     * 创建次要标签
     */
    public static JLabel createSecondaryLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));
        label.setForeground(TEXT_SECONDARY);
        return label;
    }

    /**
     * 创建面板
     */
    public static JPanel createPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_PANEL);
        panel.setBorder(createShadowBorder(BORDER_RADIUS));
        return panel;
    }

    /**
     * 创建卡片面板
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_CARD);
        panel.setBorder(new RoundedBorder(BORDER_RADIUS_LARGE, BORDER_COLOR));
        return panel;
    }

    /**
     * 创建渐变面板
     */
    public static class GradientPanel extends JPanel {
        private final Color color1;
        private final Color color2;

        public GradientPanel(Color color1, Color color2) {
            this.color1 = color1;
            this.color2 = color2;
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.dispose();
        }
    }

    /**
     * 圆角边框类
     */
    public static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;

        public RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
}
