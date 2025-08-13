#!/bin/bash
chmod 777 $0;

delWebViewPath="$(dirname $0)"
delWebViewTempPath=${delWebViewPath}/temp
libiPhoneLibPath=$1
new_libiPhoneLibPath=$1
URLUtilityPath=${delWebViewPath}/utils/URLUtility.mm
iPhoneOSSdkPath=/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/Developer/SDKs/iPhoneOS.sdk

# 查看 libiPhone-lib.a 结构
lipo -info $libiPhoneLibPath

URLUtilityArmv7o=${delWebViewTempPath}/URLUtility-armv7.o
URLUtilityArm64o=${delWebViewTempPath}/URLUtility-arm64.o

libiPhoneLibArmv7a=${delWebViewTempPath}/libiPhone-lib-armv7.a
libiPhoneLibArm64a=${delWebViewTempPath}/libiPhone-lib-arm64.a

# 创建 文件
mkdir ${delWebViewTempPath}

# 编译 URLUtilit.o
clang -c $URLUtilityPath -arch armv7 -isysroot $iPhoneOSSdkPath -o $URLUtilityArmv7o
clang -c $URLUtilityPath -arch arm64 -isysroot $iPhoneOSSdkPath -o $URLUtilityArm64o

# 拆分 libiPhone-lib.a 成对应结构文件存储
lipo $libiPhoneLibPath -thin armv7 -output $libiPhoneLibArmv7a
lipo $libiPhoneLibPath -thin arm64 -output $libiPhoneLibArm64a

# 分离 URLUtility.o
ar -v -d $libiPhoneLibArmv7a URLUtility.o
ar -v -d $libiPhoneLibArm64a URLUtility.o

# 增加 URLUtility.o
ar -q $libiPhoneLibArmv7a $URLUtilityArmv7o
ar -q $libiPhoneLibArm64a $URLUtilityArm64o

# 合并 结构库
lipo -create $libiPhoneLibArmv7a $libiPhoneLibArm64a -output $new_libiPhoneLibPath

# 查看 libiPhone-lib.a 是否包含 webView
strings -a -arch all $new_libiPhoneLibPath | grep -i webView

# 删除 冗余文件
rm -r ${delWebViewTempPath}
