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

    private static final String TAG = "MainHook";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        log("handleLoadPackage",lpparam.packageName);
    }

    public void e(String msg){
        Log.e(TAG, msg );
    }

    public void log(String msg){
        Log.d(TAG, msg);
    }

    public void log(String fun,String msg){
        Log.d(TAG, "["+fun+"] ==> "+msg);
    }
}
