package com.xiaoxiao.qiaoba.interpreter.utils;

import com.xiaoxiao.qiaoba.protocol.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by wangfei on 2017/7/20.
 */

public class RouterUtils {
    public static String getRouterString(String domain, String pathName){
        StringBuilder sb = new StringBuilder();
        sb.append(domain).append("/").append(pathName);
        return sb.toString();
    }

    public static String getRouterPath(String domain, String pathName, String actionName){
        return getRouterPath(domain, pathName, actionName, null);
    }

    public static String getRouterPath(String domain, String pathName, String actionName, Map<String, String> params){
        // 判断空的处理， 抛出异常
        String routerString = getRouterString(domain, pathName);
        StringBuilder sb = new StringBuilder(routerString);
        sb.append("/").append(actionName);
        String queryString = toQueryString(params);
        sb.append(queryString);
        return sb.toString();
    }

    public static String toQueryString(Map<String, String> params) {
        if(params != null || params.keySet().size() <= 0){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String key : params.keySet()){
            sb.append(key).append("=").append(params.get(key)).append("&");
        }
        String result = sb.toString();
        if(StringUtils.isNotEmpty(result)){
            result = result.substring(0, result.length()-1);
        }
        return result;
    }

    /**
     * 从router中找到对应domain（domain/pathname/actionname）
     * @param router
     * @return
     */
    public static String getDomainFromRouter(String router){
        if(StringUtils.isEmpty(router) || router.indexOf("/") < 0){
            return null;
        }
        return router.split("/")[0];
    }

    public static String getPathnameFromRouter(String router){
        if(StringUtils.isNotEmpty(getDomainFromRouter(router))){
            return router.split("/")[1];
        }
        return null;
    }

    public static String getActionnameFromRouter(String router){
        if(StringUtils.isNotEmpty(getPathnameFromRouter(router))){
            String[] routerSplits = router.split("/");
            if (routerSplits.length > 1){
                String str = routerSplits[2];
                return str.substring(0, str.indexOf("?") > 0 ? str.indexOf("?") : str.length());
            }
            return null;
        }
        return null;
    }

    public static Map<String, String> parseQueryString(String router){
        Map<String, String> map = new HashMap<>();
        if(router.indexOf("/") >= 0){
            String queryString = router.substring(router.indexOf("?")+1);
            String[] queryArray = queryString.split("&");
            for (String query : queryArray){
                if(query.indexOf("=") > 0){
                    String[] split = query.split("=");
                    map.put(split[0], split[1]);
                }
            }
        }
        return map;
    }

    public static Map<String, String> deparamQueryString(String paramStr){
        Map<String, String> map = new HashMap<>();
        if(StringUtils.isEmpty(paramStr)){
            return map;
        }
        String[] queryArray = paramStr.split("&");
        for (String query : queryArray){
            if(query.indexOf("=") > 0){
                String[] split = query.split("=");
                map.put(split[0], split[1]);
            }
        }
        return map;
    }

    public static String generateJsonData(String router){
        Map<String, String> params = parseQueryString(router);
        return generateJsonData(params);
    }
    public static String generateJsonData(Map<String, String> params){
        if(params == null){
            return "";
        }
        JSONObject jsonObject = new JSONObject();
        for (String key : params.keySet()){
            if(StringUtils.isNotEmpty(key)){
                String val = params.get(key);
                try {
                    jsonObject.put(key, (StringUtils.isEmpty(val) ? "" : val));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonObject.toString();
    }

    public static String mergeJson(String source, String target){
        if(StringUtils.isEmpty(source)){
            return target;
        }else if(StringUtils.isEmpty(target)){
            return source;
        }
        try {
            JSONObject jsonSource = new JSONObject(source);
            JSONObject jsonTarget = new JSONObject(target);
            return deepMergeJson(jsonSource, jsonTarget).toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return source;
        }
    }

    private static JSONObject deepMergeJson(JSONObject source, JSONObject target){
        Iterator<String> keys = source.keys();
        while (keys.hasNext()){
            try {
                String key = keys.next();
                if (!target.has(key)) {
                    target.put(key, source.get(key));
                } else {
                    if (source.get(key) instanceof JSONObject && target.get(key) instanceof JSONObject) {
                        JSONObject val = (JSONObject) source.get(key);
                        deepMergeJson(val, (JSONObject) target.get(key));
                    } else {
                        target.put(key, source.get(key));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return target;
    }
}
