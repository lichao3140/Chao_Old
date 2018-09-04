package com.runvision.bean;

public enum Sex {
    未知(0, "未知"),
    男(1, "男"),
    女(2, "女");

    private int code;
    private String desc;

    Sex(int code, String desc) {
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
    public static Sex getSex(int id) {
        for (Sex sex : Sex.values()) {
            if (sex.getCode() == id) {
                return sex;
            }
        }
        return 未知;
    }
}
