package com.box.xposedloader;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import java.io.File;
import java.lang.reflect.Method;

import dalvik.system.PathClassLoader;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by wangw on 2018/9/19.
 */
public class XposedLoader implements IXposedHookLoadPackage {

    XSharedPreferences mSp;

    public XposedLoader() {
        mSp = new XSharedPreferences("com.box.xposedloader",StrConstants.SP_NAME);
        mSp.reload();
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        mSp.reload();
        String target = mSp.getString(StrConstants.KEY_TARGET_APK,"");
        if (TextUtils.isEmpty(target)) {
            L.e("Hook目标包名 is Null");
            return;
        }
        if (!target.equals(lpparam.packageName)){
            return;
        }

        //将loadPackageParam的classloader替换为宿主程序Application的classloader,解决宿主程序存在多个.dex文件时,有时候ClassNotFound的问题
        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Context context=(Context) param.args[0];
                lpparam.classLoader = context.getClassLoader();
                mSp.reload();
                String xposedPlug = mSp.getString(StrConstants.KEY_XPOSED_APK,"");
                String className = mSp.getString(StrConstants.KEY_HOOK_CLASS, "");
                String method = mSp.getString(StrConstants.KEY_HOOK_MEHTOD, "");
                L.d("apkName = "+xposedPlug+" , className = "+className+" , Method = "+method);
                if (TextUtils.isEmpty(xposedPlug) || TextUtils.isEmpty(className) || TextUtils.isEmpty(method)) {
                    L.e("Xposed插件包名为空或Class名为空或Method为空");
                    return;
                }
                invokeHandleHookMethod(context,xposedPlug, className, method, lpparam);
            }
        });
    }

    private void invokeHandleHookMethod(Context context, String xposedPlug, String className, String handleHookMethod, XC_LoadPackage.LoadPackageParam lpparam)  throws Throwable  {
        //原来的两种方式不是很好,改用这种新的方式
        File apkFile=findApkFile(context,xposedPlug);
        if (apkFile==null){
            throw new RuntimeException("寻找模块apk失败");
        }
        //加载指定的hook逻辑处理类，并调用它的handleHook方法
        PathClassLoader pathClassLoader = new PathClassLoader(apkFile.getAbsolutePath(), ClassLoader.getSystemClassLoader());
        Class<?> cls = Class.forName(className, true, pathClassLoader);
        Object instance = cls.newInstance();
//        for (Method method : cls.getMethods()) {
//            L.e("[method] name = "+method.getName());
//            for (Class<?> aClass : method.getParameterTypes()) {
//                L.e("[method] p = "+aClass.getName());
//            }
//        }
//        L.e("cls = "+cls+" [handleHookMethod] = "+ handleHookMethod);
        Method method = cls.getDeclaredMethod(handleHookMethod, XC_LoadPackage.LoadPackageParam.class);
        method.invoke(instance, lpparam);
    }

    private File findApkFile(Context context, String xposedPlug) {
        if (context==null){
            return null;
        }
        try {
            Context moudleContext = context.createPackageContext(xposedPlug, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
            String apkPath=moudleContext.getPackageCodePath();
            return new File(apkPath);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
