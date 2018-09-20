package com.box.xposedloader;

/**
 * Created by wangw on 2018/9/19.
 */
public interface StrConstants {

    public static final String SP_NAME = "hookloader_config";

    String KEY_TARGET_APK = "key_target_apk";
    String KEY_XPOSED_APK = "key_xposed_apk";
    String KEY_HOOK_CLASS = "KEY_HOOK_CLASS";
    String KEY_HOOK_MEHTOD = "KEY_HOOK_MEHTOD";

    String DEFAULT_METHOD_NAME = "handleLoadPackage";
    String DEFAULT_CLASS_NAME = ".MainHook";
}
