# MultiDex

apk 打包时，会将 .class 文件转换成 dex 文件。在 Android 5.0 以下，`ClassLoader` 只会从 `class.dex` 中加载类，所以当项目方法数超过 `65536` 时，需要使用 **MultiDex** 来进行分包处理。

## 使用方法

build.gradle 中引入 MultiDex

```groovy
implementation "com.android.support:multidex:1.0.3"
```

Application 的 `attachBaseContext` 中初始化

```java
protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    MultiDex.install(this);
}
```

## 工作流程

通过系统日志可以缕出 MultiDex 的大致工作流程：

```java
MultiDex: VM with version 1.6.0 does not have multidex support
MultiDex: Installing application
MultiDex: MultiDexExtractor(/system/priv-app/PrebuiltGmsCore.apk, /data/data/com.google.android.gms/code_cache/secondary-dexes)
MultiDex: Blocking on lock /data/data/com.google.android.gms/code_cache/secondary-dexes/MultiDex.lock
MultiDex: /data/data/com.google.android.gms/code_cache/secondary-dexes/MultiDex.lock locked
MultiDex: MultiDexExtractor.load(/system/priv-app/PrebuiltGmsCore.apk, false, )
MultiDex: loading existing secondary dex files
MultiDex: load found 5 secondary dex files
MultiDex: install done
```

上面是 MultiDex 在 Android 4.4 中的系统日志，因为 Android 5.0 及以上手机默认支持 MultiDex，所以不需要进行处理。

### $install

```java
public static void install(Context context) {
	Log.i("MultiDex", "Installing application");
	··· 省略部分代码
    try {
        ApplicationInfo applicationInfo = getApplicationInfo(context);
        if (applicationInfo == null) {
        	Log.i("MultiDex", "No ApplicationInfo available, i.e. running on a test Context: MultiDex support library is disabled.");
			return;
		}
		doInstallation(context, new File(applicationInfo.sourceDir), new File(applicationInfo.dataDir), "secondary-dexes", "", true);
	} catch (Exception var2) {
		Log.e("MultiDex", "MultiDex installation failure", var2);
		throw new RuntimeException("MultiDex installation failed (" + var2.getMessage() + ").");
	}
    Log.i("MultiDex", "install done");
}
```

