package com.xiaoxiao.qiaoba.interpreter.api.router;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by wangfei on 2017/7/20.
 */

public class ActionResult implements Parcelable{
    public static final int CODE_SUCCESS = 0;
    public static final int CODE_PROVIDER_NOT_FOUND = 1;
    public static final int CODE_ACTION_NOT_FOUND = 2;
    public static final int CODE_ORIGIN_LOCAL_ROUTER_SERVICE_NOT_REGIESTED = 3;
    public static final int CODE_ROUTER_ERROR = 4;
    public static final int CODE_INVOKE_ROUTER_ROUTER_SERVICE_NOT_REGIESTED = 5;
    public static final int CODE_ACTION_REQUEST_NULL = 9; // 一般不应该出现， 除非后面支持取消操作
    public static final int CODE_ERROR_NOT_NORMAL = -1;

    private int code;
    private String uuid;
    private String router;
    private String data;
    private String jsonData;

    private ActionResult(int code, String uuid, String router, String data, String jsonData) {
        this.code = code;
        this.uuid = uuid;
        this.router = router;
        this.data = data;
        this.jsonData = jsonData;
    }

    protected ActionResult(Parcel in) {
        this.code = in.readInt();
        this.uuid = in.readString();
        this.router = in.readString();
        this.data = in.readString();
        this.jsonData = in.readString();
    }

    public static final Creator<ActionResult> CREATOR = new Creator<ActionResult>() {
        @Override
        public ActionResult createFromParcel(Parcel in) {
            return new ActionResult(in);
        }

        @Override
        public ActionResult[] newArray(int size) {
            return new ActionResult[size];
        }
    };

    public int getCode() {
        return code;
    }

    public String getUUID() {
        return uuid;
    }

    public String getUri() {
        return router;
    }

    public String getData() {
        return data;
    }

    public String getJsonData() {
        return jsonData;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(code);
        dest.writeString(uuid);
        dest.writeString(router);
        dest.writeString(data);
        dest.writeString(jsonData);
    }

    public static class Builder{
        private int code;
        private String uuid;
        private String router;
        private String data;
        private String jsonData;

        public Builder code(int code){
            this.code = code;
            return this;
        }
        public Builder uuid(String uuid){
            this.uuid = uuid;
            return this;
        }
        public Builder router(String router){
            this.router = router;
            return this;
        }
        public Builder data(String data){
            this.data = data;
            return this;
        }
        public Builder jsonData(String jsonData){
            this.jsonData = jsonData;
            return this;
        }
        public ActionResult build(){
            return new ActionResult(this.code, this.uuid, this.router, this.data, this.jsonData);
        }
    }

}
