# NotiDo

您是否有非要把手机通知清理干净的强迫症？如果是，那么 NotiDo 非常合适您。  
NotiDo 可以使用一条不可直接移除的通知（Notification），记录您的 TODO。

Notification + TODO = **NotiDo**

## App 使用说明

NotiDo 的安装包大小不到 20 kB，安装后存储占用不到 200 kB。  
仅有的功能是展示一条无法直接移除的通知，可用作备忘。  
后台运行几乎不耗电。  
建议（特别针对国内厂商的系统）：  
1. 在多任务页面锁定任务防止被清理；  
2. 打开自启动和电池策略无限制等；  
3. 打开通知相关权限（过滤规则设置为重要，打开锁屏通知权限等）；  

## 编译下载

### 编译

- Gradle 环境下在项目根目录执行命令 `./gradlew assembleRelease`
- 使用 Android Studio G+，运行 Gradle 任务 `assembleRelease` 或 `assemble`

输出目录 `app/build/outputs/apk/release/`

### 下载

点击右侧 [Release](https://github.com/zjmok/NotiDo/releases)，点击 apk 安装包文件进行下载

## 安装包瘦身

- 不使用任何其它依赖，仅使用原生 SDK
- 直接用 Java，不用 Kotlin，不用 Lambda
- 删除多余资源文件，图标使用 android 包里提供的图标
- 开启 minifyEnabled shrinkResources
