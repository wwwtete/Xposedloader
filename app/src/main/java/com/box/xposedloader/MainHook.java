package com.box.xposedloader;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 模板代码
 * Created by wangw on 2018/9/19.
 */
public class MainHook implements IXposedHookLoadPackage {

    private static final String TAG = "MainHook";

    //目标PackageName
    private static final String TAARGET_PACKAGENAME = "";

    /**
     * Xposed FrameWork入口方法
     * @param lpparam
     * @throws Throwable
     */
    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (TAARGET_PACKAGENAME.equals(lpparam.packageName)){
            //为防止加固代码Hook失败，Hook时机改为加载Application时才开始Hook
            XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Context context=(Context) param.args[0];
                    lpparam.classLoader = context.getClassLoader();
                    onHandleLoadPackage(lpparam.classLoader);
                    super.afterHookedMethod(param);
                }
            });
        }
    }

    /**
     * XposedLoader 入口方法
     * @param loader
     */
    public void onHandleLoadPackage(ClassLoader loader){

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
