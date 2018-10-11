package com.runvision.frament;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.runvision.core.Const;
import com.runvision.g68a_sn.R;
import com.runvision.utils.CameraHelp;
import com.runvision.utils.SPUtil;

import java.lang.reflect.Field;

public class DeviceSetFrament extends android.support.v4.app.Fragment implements View.OnClickListener {

    private View view;
    private TextView threshold_1,threshold_n,wait_for_time,open_time,device_ip,vms_ip,vms_port,vms_uername,vms_password;
    private CheckBox cb_choice;
    private Spinner Preservation_time;
    private Button btn_Sure,btn_Refresh;
    private Context mContext;
    private int preservation_day;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null == view) {
            view = inflater.inflate(R.layout.devicesetframent, container, false);

        }
        initView();
        initData();
        return view;
    }

    private void initView() {
        threshold_1=(TextView)view.findViewById(R.id.threshold_1);
        threshold_n=(TextView)view.findViewById(R.id.threshold_n);
        wait_for_time=(TextView)view.findViewById(R.id.wait_for_time);
        open_time=(TextView)view.findViewById(R.id.open_time);
        device_ip=(TextView)view.findViewById(R.id.device_ip);
        vms_ip=(TextView)view.findViewById(R.id.vms_ip);
        vms_port=(TextView)view.findViewById(R.id.vms_port);
        vms_uername=(TextView)view.findViewById(R.id.vms_uername);
        vms_password=(TextView)view.findViewById(R.id.vms_password);

        cb_choice=(CheckBox)view.findViewById(R.id.cb_choice);

        btn_Sure=(Button)view.findViewById(R.id.btn_Sure);
        btn_Sure.setOnClickListener(this);

        btn_Refresh=(Button)view.findViewById(R.id.btn_Refresh);
        btn_Refresh.setOnClickListener(this);

        Preservation_time = (Spinner) view.findViewById(R.id.Preservation_time);
        Preservation_time.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String sexNumber = DeviceSetFrament.this.getResources().getStringArray(R.array.user_time)[i];
                System.out.println(sexNumber);
                if (sexNumber.equals("90天")) {
                    preservation_day=90;
                } else if(sexNumber.equals("60天")) {
                    preservation_day=60;
                } else if(sexNumber.equals("30天")) {
                    preservation_day=30;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    private void initData() {
        threshold_1.setText(String.valueOf(SPUtil.getFloat(Const.KEY_CARDSCORE, Const.ONEVSONE_SCORE)));
        threshold_n.setText(String.valueOf(SPUtil.getFloat(Const.KEY_ONEVSMORESCORE, Const.ONEVSMORE_SCORE)));
        wait_for_time.setText(String.valueOf(SPUtil.getInt(Const.KEY_BACKHOME, Const.CLOSE_HOME_TIMEOUT)));
        open_time.setText(String.valueOf(SPUtil.getInt(Const.KEY_OPENDOOR, Const.CLOSE_DOOR_TIME)));
        device_ip.setText(CameraHelp.getIpAddress());

        //Preservation_time.set(SPUtil.getInt(Const.KEY_PRESERVATION_DAY,90));
        if (SPUtil.getInt(Const.KEY_PRESERVATION_DAY,90) == 90) {
            Preservation_time.setSelection(0);
        } else if(SPUtil.getInt(Const.KEY_PRESERVATION_DAY,90) == 60) {
            Preservation_time.setSelection(1);
        } else if(SPUtil.getInt(Const.KEY_PRESERVATION_DAY,90) == 30) {
            Preservation_time.setSelection(2);
        }
        if (SPUtil.getBoolean(Const.KEY_ISOPENLIVE, Const.OPEN_LIVE)==true) {
            cb_choice.setChecked(true);
        } else {
            cb_choice.setChecked(false);
        }
        vms_ip.setText(SPUtil.getString(Const.KEY_VMSIP, ""));
        vms_port.setText(Integer.toString(SPUtil.getInt(Const.KEY_VMSPROT,0)));
        vms_uername.setText(SPUtil.getString(Const.KEY_VMSUSERNAME, ""));
        vms_password.setText(SPUtil.getString(Const.KEY_VMSPASSWORD, ""));
    }

    private void setData() {
        SPUtil.addFloat(Const.KEY_CARDSCORE, Float.parseFloat(threshold_1.getText().toString().trim()));
        SPUtil.addFloat(Const.KEY_ONEVSMORESCORE, Float.parseFloat(threshold_n.getText().toString().trim()));
        SPUtil.putInt(Const.KEY_BACKHOME, Integer.parseInt(wait_for_time.getText().toString().trim()));
        SPUtil.putInt(Const.KEY_OPENDOOR, Integer.parseInt(open_time.getText().toString().trim()));
        //修改本地IP
        updateSetting(device_ip.getText().toString().trim(),getContext());


        SPUtil.putInt(Const.KEY_PRESERVATION_DAY, preservation_day);

        if (cb_choice.isChecked() == true) {
            SPUtil.putBoolean(Const.KEY_ISOPENLIVE, true);
        } else {
            SPUtil.putBoolean(Const.KEY_ISOPENLIVE, false);
        }

        SPUtil.putString(Const.KEY_VMSIP, vms_ip.getText().toString().trim());
        SPUtil.putInt(Const.KEY_VMSPROT, Integer.parseInt(vms_port.getText().toString().trim()));
        SPUtil.putString(Const.KEY_VMSUSERNAME, vms_uername.getText().toString().trim());
        SPUtil.putString(Const.KEY_VMSPASSWORD, vms_password.getText().toString().trim());

        Amendsuccess();
        Const.WEB_UPDATE=true;
    }

    private void Amendsuccess()
    {
        //修改成功
        DialogInterface.OnCancelListener onCancelListener ;
        AlertDialog dialog =new AlertDialog.Builder(getContext())
                .setTitle("修改成功")
                .setIcon(R.mipmap.timg)
                .show();
        try {
            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object mAlertController = mAlert.get(dialog);
            Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
            mMessage.setAccessible(true);
            TextView mMessageView = (TextView) mMessage.get(mAlertController);
            mMessageView.setTextColor(Color.GREEN);
            mMessageView.setTextSize(25);
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = 200;
            params.height = 100 ;
            dialog.getWindow().setAttributes(params);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }


    // 修改设置（IP�?????
    public static int updateSetting(String deviceip,Context context) {
        if (deviceip.equals(CameraHelp.getIpAddress())) {
            return 1;
        }
        if(deviceip.equals("")) {
            return 2;
        }
            String[] Sip = deviceip.split("\\.");

            String str = Sip[0] + "." + Sip[1] + "." + Sip[2] + ".1";
            Log.i("aa", str + "," + deviceip);

            String[] staticIP = new String[]{deviceip,
                    "255.255.255.0", str, "8.8.8.8"};
            Intent closeIntent = new Intent("com.snstar.networkparameters.ETH_CLOSE");
            context.sendBroadcast(closeIntent);

            Intent i = new Intent("com.snstar.networkparameters.ETHSETINGS");
            Bundle bundle = new Bundle();
            bundle.putSerializable("STATIC_IP", staticIP);
            i.putExtras(bundle);
            context.sendBroadcast(i);
            Intent iopen = new Intent("com.snstar.networkparameters.ETH_OPEN");
            context.sendBroadcast(iopen);

            //finish();

           // ((Activity) mContext).finish();
        return 3;
    }


    public void open() {
        initData();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_Sure:
                setData();
                break;
            case R.id.btn_Refresh:
                initData();
                break;
            default:
                break;
        }
    }



}
