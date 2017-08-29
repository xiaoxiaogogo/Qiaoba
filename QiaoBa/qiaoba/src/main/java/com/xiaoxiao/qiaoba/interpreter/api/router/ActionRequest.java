package com.xiaoxiao.qiaoba.interpreter.api.router;

import android.os.Parcel;
import android.os.Parcelable;

import com.xiaoxiao.qiaoba.interpreter.utils.RouterUtils;
import com.xiaoxiao.qiaoba.interpreter.utils.UUIDUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangfei on 2017/8/6.
 */

public class ActionRequest implements Parcelable{

    public static final int TYPE_CALL_NORMAL = 0;
    public static final int TYPE_CALL_ONEWAY = 1;
    public static final int TYPE_CALL_NO_CALLBACK = 2;

    private String uuid;
    private String router;
    private String originDomain;
    private String jsonData;
    private int callType;
    private Map<String, String> paramData = new HashMap<>();

    public ActionRequest(String router, String originDomain) {
        this(router, originDomain, null, 0);
    }

    public ActionRequest(String router, String originDomain, String jsonData, int callType) {
        this(router, originDomain, jsonData, RouterUtils.parseQueryString(router), callType);
    }

    public ActionRequest(String router, String originDomain, String jsonData, Map<String, String> paramData, int callType) {
        this.router = router;
        this.originDomain = originDomain;
        // 将get参数合并到json中， 因为这样才可以在进程间传递
        this.jsonData = RouterUtils.mergeJson(jsonData, RouterUtils.generateJsonData(paramData));
        this.callType = callType;
        this.uuid = UUIDUtils.generateUUID();
    }

    protected ActionRequest(Parcel in) {
        uuid = in.readString();
        router = in.readString();
        originDomain = in.readString();
        jsonData = in.readString();
        callType = in.readInt();
    }

    public static final Creator<ActionRequest> CREATOR = new Creator<ActionRequest>() {
        @Override
        public ActionRequest createFromParcel(Parcel in) {
            return new ActionRequest(in);
        }

        @Override
        public ActionRequest[] newArray(int size) {
            return new ActionRequest[size];
        }
    };

    public String getUuid() {
        return uuid;
    }

    public String getRouter() {
        return router;
    }

    public String getOriginDomain() {
        return originDomain;
    }

    public String getJsonData() {
        return jsonData;
    }

    public int getCallType() {
        return callType;
    }

    public Map<String, String> getParamData() {
        return paramData;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uuid);
        dest.writeString(router);
        dest.writeString(originDomain);
        dest.writeString(jsonData);
        dest.writeInt(callType);
    }

    public static class Builder{
        private String originDomain;
        private String router;
        private String jsonData;
        private int callType;
        public Builder originDomain(String originDomain){
            this.originDomain = originDomain;
            return this;
        }
        public Builder router(String router){
            this.router = router;
            return this;
        }
        public Builder json(String jsonStr){
            this.jsonData = jsonStr;
            return this;
        }
        public Builder callType(int callType){
            this.callType = callType;
            return this;
        }
        public ActionRequest build(){
            return new ActionRequest(this.router, this.originDomain, this.jsonData, this.callType);
        }
    }
}
