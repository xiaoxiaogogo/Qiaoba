package com.gavin.secondmodule.api;

import android.util.Log;
import android.widget.Toast;

import com.gavin.secondmodule.app.SecondApplicationProxy;
import com.xiaoxiao.qiaoba.interpreter.api.action.IAction;
import com.xiaoxiao.qiaoba.interpreter.api.callback.ActionCallback;
import com.xiaoxiao.qiaoba.interpreter.api.router.ActionResult;
import com.xiaoxiao.qiaoba.interpreter.utils.ProcessUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by wangfei on 2017/7/30.
 */

public class SayHiAction implements IAction {

    @Override
    public String invoke(String jsonData) {
        Log.e("mytest", "thread : " + Thread.currentThread().getName() + " : " + ProcessUtils.getProcessName(SecondApplicationProxy.getInstance().getApplication(), ProcessUtils.getMyProcessId()));
        Log.e("mytest", "json data : " + jsonData);
//        Toast.makeText(SecondApplicationProxy.getInstance().getApplication(), "say hi in second process", Toast.LENGTH_SHORT).show();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", "xiaoming");
            jsonObject.put("age", 123);
            Thread.sleep(5000);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
