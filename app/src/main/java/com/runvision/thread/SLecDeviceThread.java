package com.runvision.thread;

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import android.os.Handler;
import com.runvision.gpio.SlecProtocol;
import com.wits.serialport.SerialPort;
import com.wits.serialport.SerialPortManager;

public class SLecDeviceThread extends Thread {

    private SerialPortManager mSerialPortManager;
    private SerialPort serialPort4;
    private InputStream mInputStream4;
    private OutputStream mOutputStream4;
    private Handler handler = new Handler();
    private String icCard = "";

    @Override
    public void run() {
        super.run();
        try {
            //串口4，继电器控制
            mSerialPortManager = new SerialPortManager();
            serialPort4 = mSerialPortManager.getSerialPort4();
            mInputStream4 = serialPort4.getInputStream();
            mOutputStream4 = serialPort4.getOutputStream();

            sleep(500);
            while (true) {
                try {
                    sleep(50);
                    byte[] buffer = new byte[64];
                    if (mInputStream4 == null) {
                        continue;
                    }
                    int size = mInputStream4.read(buffer);

                    if (size < 1) {
                        continue;
                    }

                    int len = icCard.length();
                    Log.e("gzy", "run: " + size + "--" + icCard);
                    if (len == 0) {
                        //第一条数据
                        icCard = SlecProtocol.bytesToHexString2(buffer, size);
                    } else {
                        //之前已经有数据
                        icCard = icCard + SlecProtocol.bytesToHexString2(buffer, size);
                    }
                    handler.removeCallbacks(cancelCardRunnable);
                    //200ms没有新的数据就发送
                    handler.postDelayed(cancelCardRunnable, 200);

                } catch (SecurityException e) {
                    Log.e("SerialPort", "-----------------SecurityException");
                } catch (IOException e) {
                    Log.e("SerialPort", "-----------------IOException" + e.toString());
                } catch (InvalidParameterException e) {
                    Log.e("SerialPort", "-----------------InvalidParameterException");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 退出读卡状态
     */
    private Runnable cancelCardRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                //处理icCard
                Log.e("gzy", "接收到的串口数据为: " + icCard);

                final byte[] bytes = SlecProtocol.asciiToHex(SlecProtocol.hexToByteArray(icCard));
                if (bytes.length > 5) {
                    Log.e("gzy", "接收转换: " + SlecProtocol.bytesToHexString2(bytes, bytes.length) +
                            "--命令：" + bytes[3] +
                            "--数据长度：" + bytes[5] +
                            "--数据：" + (bytes[5] == 0 ? "没有数据" : bytes[6])
                    );
                    switch (bytes[3]) {
                        case 1:
                            if (bytes[6] == 0) {
                                Log.e("gzy", "run: 发送开门指令成功");
                            } else {
                                Log.e("gzy", "run: 发送开门指令失败");
                            }
                            break;
                        case 2:
                            //刷卡
                            //6-9是用户id，10-13是卡号
                            if (bytes.length > 13) {
                                final byte[] card = new byte[4];
                                for (int i = 0; i < card.length; i++) {
                                    card[i] = bytes[10 + i];
                                }
                            }
                            break;
                        default:
                    }
                }
                //重置
                icCard = "";
            } catch (Exception e2) {
                e2.printStackTrace();
                //重置
                icCard = "";
            }
            //重置
            icCard = "";
        }
    };


}
