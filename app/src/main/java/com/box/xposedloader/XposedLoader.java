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

    private static final String VERSION = BuildConfig.VERSION_NAME;

    public XposedLoader() {
        mSp = new XSharedPreferences("com.box.xposedloader",StrConstants.SP_NAME);
        mSp.reload();
        L.d("XposedLoader Version="+VERSION);
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        mSp.reload();
        String target = mSp.getString(StrConstants.KEY_TARGET_APK,"");
//        L.d("packageName: "+lpparam.packageName);
        if (TextUtils.isEmpty(target)) {
            L.e("Hook目标包名 is Null");
            return;
        }
        if (!target.equals(lpparam.packageName)){
            return;
        }
        L.d("start Hook ==> "+lpparam.packageName);
        //将loadPackageParam的classloader替换为宿主程序Application的classloader,解决宿主程序存在多个.dex文件时,有时候ClassNotFound的问题
        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Context context=(Context) param.args[0];
//                lpparam.classLoader = context.getClassLoader();
                mSp.reload();
                L.d("Attach Hook Target => "+mSp.getString(StrConstants.KEY_TARGET_APK,""));
                String xposedPlug = mSp.getString(StrConstants.KEY_XPOSED_APK,"");
                String className = mSp.getString(StrConstants.KEY_HOOK_CLASS, StrConstants.DEFAULT_CLASS_NAME);
                String method = mSp.getString(StrConstants.KEY_HOOK_MEHTOD, StrConstants.DEFAULT_METHOD_NAME);
                L.d("Xposed Plugin Name = "+xposedPlug+" , className = "+className+" , Method = "+method);
                if (TextUtils.isEmpty(xposedPlug) || TextUtils.isEmpty(className) || TextUtils.isEmpty(method)) {
                    L.e("Xposed插件包名为空或Class名为空或Method为空");
                    return;
                }
                invokeHandleHookMethod(context,xposedPlug, className, method, lpparam,param);
            }
        });
    }

    private void invokeHandleHookMethod(Context context, String xposedPlug, String className, String handleHookMethod, XC_LoadPackage.LoadPackageParam lpparam, XC_MethodHook.MethodHookParam param)  throws Throwable  {
        L.d("[invokeHandleHookMethod] ==> "+lpparam.processName);
        //为方便打断点调试增加延时，真正使用时应去掉这个延时
        if (mSp.getBoolean(StrConstants.KEY_DEBUG_ISOPEN,false))
            Thread.sleep(3000);
        //1.获取Xposed插件Apk文件
        File apkFile=findApkFile(context,xposedPlug);
        L.d("[1]");
        if (apkFile==null){
            L.e("Not Find Xposed Plugin APK File");
            throw new RuntimeException("Not Find Xposed Plugin APK File");
        }
        //2.获取到Apk文件路径后初始化PathClassLoader对象
        //注意:第二个参数ClassLoader一定要选择LoadPackageParam对象的Classloader不能使用系统的ClassLoader，否则会抛出找不到Xposed相关类的异常
        PathClassLoader pathClassLoader = new PathClassLoader(apkFile.getAbsolutePath(), lpparam.getClass().getClassLoader());//ClassLoader.getSystemClassLoader());
        L.d("[2]");
        try {
            //3.加载Hook处理逻辑Class对象
            Class<?> cls = Class.forName(className, true, pathClassLoader);
            L.d("[3]");
//            Object instance = cls.newInstance();
//            L.d("[4]");
//        for (Method method : cls.getMethods()) {
//            L.e("[method] name = "+method.getName());
//            for (Class<?> aClass : method.getParameterTypes()) {
//                L.e("[method] p = "+aClass.getName());
//            }
//        }
//        L.e("cls = "+cls+" [handleHookMethod] = "+ handleHookMethod);
//            Method method = cls.getDeclaredMethod(handleHookMethod, ClassLoader.class,ClassLoader.class);
//            L.d("[5]");
//            method.invoke(instance, lpparam);//lpparam.classLoader,context.getClassLoader());
            //4.调用Hook处理入口方法
            XposedHelpers.callMethod(cls.newInstance(),handleHookMethod,lpparam,param);
            L.d("[invoke Method] <==");
        }catch (Exception e){
            L.e("invokeHandleHookMethod Error: "+e.getMessage());
            e.printStackTrace();
        }
    }

    private File findApkFile(Context context, String xposedPlug) {
        if (context==null){
            return null;
        }
        try {
            Context moudleContext = context.createPackageContext(xposedPlug, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
            String apkPath=moudleContext.getPackageCodePath();
            return new File(apkPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
