#!/bin/bash
# ========================================
# RuoYi-Vue-Plus 包名修改器 - 打包脚本 (Linux/macOS)
# ========================================

echo "========================================"
echo "RuoYi-Vue-Plus 包名修改器 - 打包脚本"
echo "========================================"
echo

# 检查 JDK 版本（需要 JDK 14+ 使用 jpackage）
if ! command -v java &> /dev/null; then
    echo "[错误] 未找到 Java，请确保 JDK 已安装"
    exit 1
fi

# 编译项目
echo "[步骤 1] 编译项目..."
mvn clean package
if [ $? -ne 0 ]; then
    echo "[错误] Maven 编译失败"
    exit 1
fi
echo "[成功] 编译完成"
echo

# 清理旧的输出目录
if [ -d "installer" ]; then
    echo "[步骤 2] 清理旧的安装包目录..."
    rm -rf installer
fi

# 使用 jpackage 打包
echo "[步骤 3] 使用 jpackage 创建安装包..."
echo

# 检测操作系统
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    TYPE="rpm"
    EXT=".rpm"
elif [[ "$OSTYPE" == "darwin"* ]]; then
    TYPE="pkg"
    EXT=".pkg"
else
    echo "[错误] 不支持的操作系统: $OSTYPE"
    exit 1
fi

jpackage \
  --type $TYPE \
  --name "RuoYi包名修改器" \
  --dest installer \
  --input target/classes \
  --main-jar ruoyi-vue-plus-package-rename-1.0-SNAPSHOT.jar \
  --main-class cn.baruto.Main \
  --app-version 1.0.0 \
  --vendor "Baruto" \
  --description "RuoYi-Vue-Plus 包名批量修改工具"

if [ $? -ne 0 ]; then
    echo
    echo "[错误] jpackage 打包失败"
    echo
    echo 可能的原因：
    echo 1. JDK 版本低于 14（jpackage 需要 JDK 14+）
    echo
    echo 解决方案：
    echo 1. 升级到 JDK 14 或更高版本
    echo
    exit 1
fi

echo
echo "========================================"
echo "[成功] 打包完成！"
echo "========================================"
echo
echo 安装包位置: installer/RuoYi包名修改器-1.0.0$EXT
echo
