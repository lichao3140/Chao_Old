package com.runvision.db;

/**
 * Created by Administrator on 2018/7/5.
 */

public class User {
    /**
     * 主键
     */
    private int id;
    /**
     * 姓名
     */
    private String name;
    /**
     * 类型
     */
    private String type;
    /**
     * 性别
     */
    private String sex;
    /**
     * 年龄
     */
    private int age;
    /**
     * 工号
     */
    private String wordNo;
    /**
     * 身份证号码
     */
    private String cardNo;

    /**
     * 模版保存的路径（文件名）
     */
    private String templateImageID;

    /**
     * 时间
     */
    private long time;

    private Record record;

    public User() {
    }

    public User(int id, String name, String type, String sex, int age, String wordNo, String cardNo, String templateImageID) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.sex = sex;
        this.age = age;
        this.wordNo = wordNo;
        this.cardNo = cardNo;
        this.templateImageID = templateImageID;
    }

    public User(String name, String type, String sex, int age, String wordNo, String cardNo, String templateImageID, long time) {
        this.name = name;
        this.type = type;
        this.sex = sex;
        this.age = age;
        this.wordNo = wordNo;
        this.cardNo = cardNo;
        this.templateImageID = templateImageID;
        this.time=time;
    }


    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getTemplateImageID() {
        return templateImageID;
    }

    public void setTemplateImageID(String templateImageID) {
        this.templateImageID = templateImageID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getWordNo() {
        return wordNo;
    }

    public void setWordNo(String wordNo) {
        this.wordNo = wordNo;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }
}
