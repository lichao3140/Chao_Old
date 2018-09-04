package com.runvision.bean;

/**
 * @author sulin
 * @data 2018/8/6 16:22
 */
public enum Type {
    未知(0, "未知"),
    黑名单(1, "黑名单"),
    白名单(56, "白名单"),
    顾客(112, "顾客"),
    业主(113, "业主"),
    访客(114, "访客");


    private int code;
    private String desc;


    Type(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static Type getType(int id) {
        for (Type type : Type.values()) {
            if (type.getCode() == id) {
                return type;
            }
        }
        return 未知;
    }

}
