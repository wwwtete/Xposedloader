package com.box.xposedloader;

import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 测试代码
 * Created by wangw on 2018/9/19.
 */
public class MainHook implements IXposedHookLoadPackage {


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if ("com.xmx.group".equals(lpparam.packageName)){
            XposedHelpers.findAndHookMethod("com.xmx.group.MainActivityV1", lpparam.classLoader, "onResume",  new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    L.d("onResume: ");
                }
            });
        }
    }
}
