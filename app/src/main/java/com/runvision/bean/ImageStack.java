package com.runvision.bean;

/**
 * Created by Administrator on 2018/8/3.
 */

public class ImageStack {
    private boolean isReading = false;
    private ImageInfo imageOne = null;
    private ImageInfo imageTwo = null;

    public ImageStack(int width, int height) {
        imageOne = new ImageInfo(width, height);
        imageTwo = new ImageInfo(width, height);
    }

    /**
     * 出堆
     *
     * @return
     */
    public ImageInfo pullImageInfo() {
        //log("pullImageInfo() isReading:" + isReading);
        isReading = true;
        if (imageOne.isNew()) {
            imageTwo.setData(imageOne.getData());
            imageTwo.setTime(imageOne.getTime());
            imageTwo.setNew(true);
            imageOne.setNew(false);
        } else {
            imageTwo.setNew(false);
        }
        isReading = false;
        return imageTwo;
    }

    /**
     * 入堆
     * @param
     */
    public void pushImageInfo(byte[] imgData, long time) {
        if (!isReading) {
            imageOne.setData(imgData);
            imageOne.setTime(time);
            imageOne.setNew(true);
        }
    }

    public void clearAll() {
        imageOne.setNew(false);
        imageTwo.setNew(false);
    }
}
