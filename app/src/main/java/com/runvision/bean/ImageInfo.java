package com.runvision.bean;

/**
 * Created by Administrator on 2018/8/3.
 */

public class ImageInfo {
    private int size = 0;
    private int width;
    private int height;
    private byte[] data;
    private long time;
    private boolean isNew;

    public ImageInfo(int width, int height) {
        this.width = width;
        this.height = height;
        size = width * height * 3 / 2;
        data = new byte[size];
        isNew = false;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }
}
