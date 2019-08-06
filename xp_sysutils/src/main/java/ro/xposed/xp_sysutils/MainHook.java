package ro.xposed.xp_sysutils;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 模板代码
 * Created by wangw on 2018/9/19.
 */
public class MainHook implements IXposedHookLoadPackage {

    private static final String TAG = "MainHook";

    private XSharedPreferences mSp;

    public MainHook(){
         mSp = new XSharedPreferences("ro.xposed.xp_sysutils",MainActivity.SPNAME);
//         mSp.reload();
//         log("backup="+mSp.getBoolean(MainActivity.KEY_BACKUP,false)+", debug="+ mSp.getBoolean(MainActivity.KEY_DEBUG,true));
    }

    /**
     * Xposed FrameWork入口方法
     * @param lpparam
     * @throws Throwable
     */
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
//        mSp.reload();
//        log("backup="+mSp.getBoolean(MainActivity.KEY_BACKUP,false)+", debug="+ mSp.getBoolean(MainActivity.KEY_DEBUG,true));
//        if (!mSp.getBoolean(MainActivity.KEY_BACKUP,false) && !mSp.getBoolean(MainActivity.KEY_DEBUG,true))
//            return;
//        log("start find="+lpparam.packageName);
        if (!lpparam.packageName.equals("android")){
            return;
        }
        log("start Hook getPackageInfo");
        Class<?> clz = XposedHelpers.findClass("com.android.server.pm.PackageManagerService", lpparam.classLoader);
//        log("find PackageManagerService success!");
        XposedBridge.hookAllMethods(clz, "getPackageInfo", new XC_MethodHook() {

            public void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
//                log("afterHookedMethod ==>");
                PackageInfo packageInfo = (PackageInfo) param.getResult();
                if (packageInfo != null) {
                    ApplicationInfo appinfo = packageInfo.applicationInfo;
                    int flags = appinfo.flags;
                    mSp.reload();
                    boolean isDebug = mSp.getBoolean(MainActivity.KEY_DEBUG, false);
                    boolean isBackup = mSp.getBoolean(MainActivity.KEY_BACKUP, false);
                    log( "Load App : " + appinfo.packageName+" , isDebug="+isDebug+" , isBackup="+isBackup);
                    log( "==== After Hook ====");

                    if (isBackup && (flags & 32768) == 0) {
                        flags |= 32768;
                    }
                    if (isDebug && (flags & 2) == 0) {
                        flags |= 2;
                    }
                    appinfo.flags = flags;
                    param.setResult(packageInfo);
                    log( "flags = " + flags);
                    isDebugable(appinfo);
                    isBackup(appinfo);
                }else {
                    log( "packageInfo is Null  ");
                }
            }
        });
    }

    public boolean isDebugable(ApplicationInfo info) {
        try {
            if ((info.flags & 2) != 0) {
                log( "Open Debugable");
                return true;
            }
        } catch (Exception e) {
        }
        log( "Close Debugable");
        return false;
    }

    public boolean isBackup(ApplicationInfo info) {
        try {
            if ((info.flags & 32768) != 0) {
                log( "Open Backup");
                return true;
            }
        } catch (Exception e) {
        }
        log( "Close Backup");
        return false;
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
