package com.xiaoxiao.qiaoba.interpreter.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dalvik.system.DexFile;

/**
 * Created by wangfei on 2017/3/2.
 */

public class ClassUtils {


    private static final int VM_WITH_MULTIDEX_VERSION_MAJOR = 2;
    private static final int VM_WITH_MULTIDEX_VERSION_MINOR = 1;
    private static final String EXTRACTED_NAME = ".classes";
    private static final String EXTRACTED_SUFFIX = ".zip";
    private static final String MULTIDEX_VERSION = "multidex.version";
    private static final String KEY_DEX_NUMBER = "dex.number";
    private static final String SECONDARY_FOLDER_NAME = "code_cache" + File.separator + "secondary-dexes";

    /**
     * 通过包名 获取当前apk中的在此包名下面的所有的类
     * @param context
     * @param packageName 需要扫描的报名
     * @return 包名下的类
     */
    public static List<String> getClassNameByPackageName(Context context, String packageName) throws PackageManager.NameNotFoundException, IOException {
        List<String> classNames = new ArrayList<>();
        for (String path : getSourcePaths(context)){
            DexFile dexFile;
            if(path.endsWith(EXTRACTED_SUFFIX)){
                dexFile = DexFile.loadDex(path, path + ".tmp", 0);//zip文件，需要加载成dex文件(DexClassLoader核心代码)
            }else {
                dexFile = new DexFile(path);//dex文件
            }
            Enumeration<String> entries = dexFile.entries();
            while (entries.hasMoreElements()){
                String className = entries.nextElement();
                if(className.contains(packageName)){
                    classNames.add(className);
                }
            }
        }
        return classNames;
    }

    private static List<String> getSourcePaths(Context context) throws PackageManager.NameNotFoundException, IOException {
        ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
        List<String> sourcePaths = new ArrayList<>();
        sourcePaths.add(applicationInfo.sourceDir);
        File file = new File(applicationInfo.sourceDir);
        String extractedFilePrefix = file.getName() + EXTRACTED_NAME;

        //如果VM已经支持MultiDex， 就不要去Secondary Folder加载Classesx.zip了，因为已经没有了
        //通过是否存在sp红的multidex.version是不准确的，因为低版本升级上来的用户是包含这个sp配置的
        if(!isVMMultidexCapable()){
            int totalDexNum = getMultiDexPreferences(context).getInt(KEY_DEX_NUMBER, 1);
            File dexFile = new File(applicationInfo.dataDir, SECONDARY_FOLDER_NAME);
            for (int i = 2 ; i <= totalDexNum; i++){
                //for each dex file, ie: test.classes2.zip, test.classes3.zip...
                String fileName = extractedFilePrefix + i + EXTRACTED_SUFFIX;
                File extractedFile = new File(dexFile, fileName);
                if(extractedFile.isFile()){
                    sourcePaths.add(extractedFile.getAbsolutePath());
                    //we ignore the verify zip part
                }else {
                    throw new IOException("Missing extracted secondary dex file '" + extractedFile.getPath() + "'");
                }
            }
        }

        return sourcePaths;
    }

    private static SharedPreferences getMultiDexPreferences(Context context) {
        return context.getSharedPreferences(MULTIDEX_VERSION, Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ?
            Context.MODE_PRIVATE : Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
    }

    /**
     * 判断当前系统是否已经支持multidex
     * Identifies if the current VM has a native support for multidex, meaning there is no need for
     * additional installation by this library.
     *
     * @return true if the VM handles multidex
     */
    private static boolean isVMMultidexCapable() {
        boolean isMultidexCapable = false;
        String vmName = null;

        try {
            if (isYunOS()) {    // YunOS需要特殊判断
                vmName = "'YunOS'";
                isMultidexCapable = Integer.valueOf(System.getProperty("ro.build.version.sdk")) >= 21;
            } else {    // 非YunOS原生Android
                vmName = "'Android'";
                String versionString = System.getProperty("java.vm.version");
                if (versionString != null) {
                    Matcher matcher = Pattern.compile("(\\d+)\\.(\\d+)(\\.\\d+)?").matcher(versionString);
                    if (matcher.matches()) {
                        try {
                            int major = Integer.parseInt(matcher.group(1));
                            int minor = Integer.parseInt(matcher.group(2));
                            isMultidexCapable = (major > VM_WITH_MULTIDEX_VERSION_MAJOR)
                                    || ((major == VM_WITH_MULTIDEX_VERSION_MAJOR)
                                    && (minor >= VM_WITH_MULTIDEX_VERSION_MINOR));
                        } catch (NumberFormatException ignore) {
                            // let isMultidexCapable be false
                        }
                    }
                }
            }
        } catch (Exception ignore) {

        }

        Log.i("galaxy", "VM with name " + vmName + (isMultidexCapable ? " has multidex support" : " does not have multidex support"));
        return isMultidexCapable;
    }

    /**
     * 判断系统是否为YunOS系统
     */
    private static boolean isYunOS() {
        try {
            String version = System.getProperty("ro.yunos.version");
            String vmName = System.getProperty("java.vm.name");
            return (vmName != null && vmName.toLowerCase().contains("lemur"))
                    || (version != null && version.trim().length() > 0);
        } catch (Exception ignore) {
            return false;
        }
    }

}
