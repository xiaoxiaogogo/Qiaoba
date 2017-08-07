package com.xiaoxiao.qiaoba.interpreter.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;

import com.xiaoxiao.qiaoba.protocol.utils.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

/**
 * Created by wangfei on 2017/7/23.
 */

public class ProcessUtils {

    public static final String UNKNOWN_PROCESS_NAME = "unknown_process_name";

    public static int getMyProcessId() {
        return android.os.Process.myPid();
    }

    public static String getProcessName(int pid) {
        String processName = UNKNOWN_PROCESS_NAME;
        try {
            File file = new File("/proc/" + pid + "/" + "cmdline");
            BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
            processName = mBufferedReader.readLine().trim();
            mBufferedReader.close();
            return processName;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!TextUtils.isEmpty(processName)) {
                return processName;
            }
        }
        return UNKNOWN_PROCESS_NAME;
    }

    public static String getProcessName(Context context, int pid) {
        String processName = getProcessName(pid);
        processName = processName.replace(context.getPackageName(), "");
        if (StringUtils.isEmpty(processName)){
            processName = "main";
        }else {
            processName = processName.substring(1);
        }
        if(UNKNOWN_PROCESS_NAME.equals(processName)){
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
            if (runningApps == null) {
                return UNKNOWN_PROCESS_NAME;
            }
            for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
                if (procInfo.pid == pid) {
                    return procInfo.processName;
                }
            }
        }else{
            return processName;
        }
        return UNKNOWN_PROCESS_NAME;
    }

}
