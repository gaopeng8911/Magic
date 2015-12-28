package com.jason.bluetooth.le;
/**
 * Created by P_Gao on 2015/12/9.
 */

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.List;

public class show_result extends Activity {
    public static final String EXTRAS_DEVICE_NAME_LEFTHAND = "DEVICE_NAME";//左手设备传输
    public static final String EXTRAS_DEVICE_ADDRESS_LEFTHAND = "DEVICE_ADDRESS";//左手设备传输
    public static final String EXTRAS_DEVICE_NAME_RIGHTHAND = "DEVICE_NAME";//右手设备传输
    public static final String EXTRAS_DEVICE_ADDRESS_RIGHTHAND = "DEVICE_ADDRESS";//右手设备传输
    public static final String EXTRAS_DEVICE_NAME_WAIST = "DEVICE_NAME";//腰部设备传输
    public static final String EXTRAS_DEVICE_ADDRESS_WAIST = "DEVICE_ADDRESS";//腰部设备传输

    private final static String UUID_KEY_DATA = "0000ffe1-0000-1000-8000-00805f9b34fb";//uuid
    private final static String UUID_KEY_DATA2 = "0000ffe0-0000-1000-8000-00805f9b34fb";

    private final static String TAG = show_result.class.getSimpleName();

    private BluetoothLeClass mBLELefthand;//左手设备服务
    private BluetoothLeClass mBLERighthand;//右手设备服务
    private BluetoothLeClass mBLEWaist;//腰部设备服务

    private boolean mScanning;

    private Handler mHandlerLefthand;//左手设备进程
    private Handler mHandlerRight;//右手设备进程
    private Handler mHandlerWaist;//腰部设备进程
    private Handler handlerMain;//控制界面进程


    private String mDeviceNameLefthand;//左手设备名称
    private String mDeviceNameRighthand;//右手设备名称
    private String mDeviceNameWaist;//腰部设备名称

    private String mDeviceAddressLefthand;//左手设备地址
    private String mDeviceAddressRighthand;//右手设备地址
    private String mDeviceAddressWaist;//腰部设备地址

    private TextView tvResultLefthand;//左手结果
    private TextView tvResultRighthand;//右手结果
    private TextView tvResultWaist;//腰部结果

    private String showResult;
    private VideoView vvResult;//演示视频

    private TextView tvResultRight;
    private TextView tvActionRighthand;
    private TextView tvActionWaist;
    String action = "ready";
    double roolForShowLefthand = 0;
    double pitchDifferenceLefthand = 0;//俯仰角差值
    double roolDifferenceLefthand = 0;//滚动角差值
    double yawDifferenceLefthand = 0;//航向角差值
    ArrayList pitchListLefthand = new ArrayList();
    ArrayList roolListLefthand = new ArrayList();
    ArrayList yawListhand = new ArrayList();


    double roolForShowRighthand = 0;
    double pitchDifferenceRightthand = 0;//俯仰角差值
    double roolDifferenceRighthand = 0;//滚动角差值
    double yawDifferenceRighthand = 0;//航向角差值
    ArrayList pitchListRighthand = new ArrayList();
    ArrayList roolListRighthand = new ArrayList();
    ArrayList yawListRighthand = new ArrayList();
    double[] iiAgoWaist = {0, 0, 0, 0, 0, 0};

    ArrayList moveListWaist = new ArrayList();
    ArrayList waistResultAXList = new ArrayList();//腰部位移AX,用于连续三组数判断最大值最小值差
    ArrayList waistResultAYList = new ArrayList();//腰部位移AY用于连续三组数判断最大值最小值差
    ArrayList waistResultAZList = new ArrayList();//腰部位移AZ用于连续三组数判断最大值最小值差


    private int righthandResultCount = 0;//右手挥拳计数，每次右手挥拳后，5次数据不判断，以免重复判断挥拳动作
    private int lefthandResultCount = 0;//左手挥拳计数，每次左手挥拳后，5次数据不判断，以免重复判断挥拳动作
    private int waistResultCount = 0;//腰部动作判断计数，用于防治重复判断


    double[] iiWaistAgo = {0, 0, 0, 0, 0, 0};//腰部输入结果集，前一时刻

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_show_result);

        mHandlerLefthand = new Handler();
        mHandlerRight = new Handler();
        mHandlerWaist = new Handler();
        handlerMain = new Handler();

        tvResultLefthand = (TextView) this.findViewById(R.id.tv_result_left);
        tvResultRight = (TextView) this.findViewById(R.id.tv_result_right);
        tvResultWaist = (TextView) this.findViewById(R.id.tv_result_waist);

        vvResult = (VideoView) this.findViewById(R.id.vv_result);

        tvResultLefthand.setMovementMethod(ScrollingMovementMethod.getInstance());

        mBLELefthand = new BluetoothLeClass(this);
        mBLERighthand = new BluetoothLeClass(this);
        mBLEWaist = new BluetoothLeClass(this);


        final Intent intent = getIntent();
        ArrayList<String> deviceArrayList = new ArrayList<String>();
        deviceArrayList = intent.getStringArrayListExtra("deviceArrayList");

        mDeviceNameLefthand = deviceArrayList.get(0);
        mDeviceAddressLefthand = deviceArrayList.get(1);
        mDeviceNameRighthand = deviceArrayList.get(2);
        mDeviceAddressRighthand = deviceArrayList.get(3);
        mDeviceNameWaist = deviceArrayList.get(4);
        mDeviceAddressWaist = deviceArrayList.get(5);


//        mDeviceNameLefthand = intent.getStringExtra(EXTRAS_DEVICE_NAME_LEFTHAND);
//        mDeviceAddressLefthand = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS_LEFTHAND);
//        mDeviceNameRighthand = intent.getStringExtra(EXTRAS_DEVICE_NAME_RIGHTHAND);
//        mDeviceAddressRighthand = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS_RIGHTHAND);
//        mDeviceNameWaist= intent.getStringExtra(EXTRAS_DEVICE_NAME_WAIST);
//        mDeviceAddressWaist = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS_WAIST);


        mBLELefthand = new BluetoothLeClass(this);
        if (!mBLELefthand.initialize()) {
            //Log.e(TAG, "Unable to initialize Bluetooth");
            finish();
        }

        mBLERighthand = new BluetoothLeClass(this);
        if (!mBLERighthand.initialize()) {
            //Log.e(TAG, "Unable to initialize Bluetooth");
            finish();
        }

        mBLEWaist = new BluetoothLeClass(this);
        if (!mBLEWaist.initialize()) {
            //Log.e(TAG, "Unable to initialize Bluetooth");
            finish();
        }


        //发现BLE终端的Service时回调
        if (!mDeviceAddressLefthand.contains("no device")) {
            mBLELefthand.setOnServiceDiscoverListener(mOnServiceDiscoverLefthand);
            mBLELefthand.setOnDataAvailableListener(mOnDataAvailableLefthand);
            mBLELefthand.connect(mDeviceAddressLefthand);
        }

        if (!mDeviceAddressRighthand.contains("no device")) {
            mBLERighthand.setOnServiceDiscoverListener(mOnServiceDiscoverRighthand);
            mBLERighthand.setOnDataAvailableListener(mOnDataAvailableRighthand);
            mBLERighthand.connect(mDeviceAddressRighthand);
        }
        if (!mDeviceAddressWaist.contains("no device")) {
            mBLEWaist.setOnServiceDiscoverListener(mOnServiceDiscoverWaist);
            mBLEWaist.setOnDataAvailableListener(mOnDataAvailableWaist);
            mBLEWaist.connect(mDeviceAddressWaist);
        }


        String ready_uri = "android.resource://" + getPackageName() + "/" + R.raw.ready;
        final String rightHookUri = "android.resource://" + getPackageName() + "/" + R.raw.right_hook;
        //String leftHookUri = "android.resource://" + getPackageName() + "/" + R.raw.right_hook;
        //String rightSwingUri = "android.resource://" + getPackageName() + "/" + R.raw.right_swing;
        final String leftSwingUri = "android.resource://" + getPackageName() + "/" + R.raw.right_swing;
        final String leftStraightUri = "android.resource://" + getPackageName() + "/" + R.raw.left_straight;
        final String moveLeftUri = "android.resource://" + getPackageName() + "/" + R.raw.move_left;
        final String moveRightUri = "android.resource://" + getPackageName() + "/" + R.raw.move_right;
        final String forwardUri = "android.resource://" + getPackageName() + "/" + R.raw.forward;
        final String backwardsUri = "android.resource://" + getPackageName() + "/" + R.raw.backwards;
        final String rightSwingUri = "android.resource://" + getPackageName() + "/" + R.raw.right_swing;


        // vvResult.setVideoPath("/sdcard/initial_state.avi");
        vvResult.setVideoPath(ready_uri);

        MediaController mc = new MediaController(this);
        mc.setVisibility(View.INVISIBLE);
        vvResult.setMediaController(mc);
        vvResult.requestFocus();
        vvResult.start();

        Runnable myRunnable = new Runnable() {
            public void run() {
                handlerMain.postDelayed(this, 100);


                if (action == "right_hook") {
                    Log.e(TAG, "播放动画" + action);
                    vvResult.setVideoPath(rightHookUri);
                    vvResult.start();
                    tvResultRight.setText("右手手环上一动作为： 右勾拳");
                } else if (action == "right_swing") {
                    Log.e(TAG, "播放动画" + action);
                    vvResult.setVideoPath(rightSwingUri);
                    vvResult.start();
                    tvResultRight.setText("右手手环上一动作为： 右摆拳");

                } else if (action == "left_straight") {
                    Log.e(TAG, "播放动画" + action);
                    vvResult.setVideoPath(leftStraightUri);
                    vvResult.start();
                    tvResultLefthand.setText("左手手环上一动作为： 左直拳");
                } else if (action == "move_left") {
                    Log.e(TAG, "播放动画" + action);
                    vvResult.setVideoPath(moveLeftUri);
                    vvResult.start();
                    tvResultWaist.setText("向左移动");
                    //tvResultRight.setText("上一动作为：向左移动");
                } else if (action == "move_right") {
                    Log.e(TAG, "播放动画" + action);
                    vvResult.setVideoPath(moveRightUri);
                    vvResult.start();
                    tvResultWaist.setText("躲避动作");
                    //tvResultRight.setText("上一动作为：向右移动");
                } else if (action == "forward") {
                    Log.e(TAG, "播放动画" + action);
                    vvResult.setVideoPath(forwardUri);
                    vvResult.start();
                    tvResultWaist.setText("向前移动");
                    //tvResultRight.setText("上一动作为：向前移动");
                } else if (action == "backwards") {
                    Log.e(TAG, "播放动画" + action);
                    vvResult.setVideoPath(backwardsUri);
                    vvResult.start();
                    tvResultWaist.setText("向后移动");
                    //tvResultRight.setText("上一动作为：向后移动");
                }
                action = "";

            }


        };
        handlerMain.post(myRunnable);


    }

    /**
     * 收到BLE终端数据交互的事件
     */
    private BluetoothLeClass.OnDataAvailableListener mOnDataAvailableLefthand = new BluetoothLeClass.OnDataAvailableListener() {

        /**
         * BLE终端数据被读的事件
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS)
                Log.e(TAG, "onCharRead " + gatt.getDevice().getName()
                        + " read "
                        + characteristic.getUuid().toString()
                        + " -> "
                        + Utils.bytesToHexString(characteristic.getValue()));
        }

        String result = "";
        int count = 1;
        String resultCalc = "";
        double q0 = 1;
        double q1 = 0;
        double q2 = 0;
        double q3 = 0;


        double[] calcResultFist = {0, 0, 0, 0, 0, 0, 0};//计算出拳累加数组
        ArrayList resultList = new ArrayList();
        double[] calcResultMove = {1, 0, 0, 0, 0, 0};//计算位移累加


        /**
         * 收到BLE终端写入数据回调
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic) {


            String ss = characteristic.getStringValue(0);
            double[] iiAgo = {0, 0, 0, 0, 0, 0};
            String ss2 = "";
            result = result + ss;
            resultCalc = resultCalc + ss;
            if (ss.contains("\r\n")) {
                result = result.toString().replace("\r\n", ", ");
                resultCalc = resultCalc.toString().replace("\r\n", ", ");
                CalculateData cd = new CalculateData();
                String deviceNametemp = gatt.getDevice().getName().toString();
                double[] ii = cd.DataCleaningForDevice(result, deviceNametemp);


                //计算出拳
                if (count == 1) {
                    if (ii[1] != 0) {
                        double[] in = {ii[0], ii[1], ii[2], ii[3], ii[4], ii[5], 1, 0, 0, 0};//ax，ay,az,gx,gy,gz,q0,q1,q2,q3,pitch,rool,yaw
                        calcResultFist = cd.CalculateAx(in);
                    }

                } else {
                    if (calcResultFist != null) {
                        if (ii[1] != 0) {
//                            double in[] = {ii[0], ii[1], ii[2], ii[3], ii[4], ii[5], calcResultFist[3], calcResultFist[4], calcResultFist[5], calcResultFist[6]};
//                            calcResultFist = cd.CalculateAx2(in);
//
//
////                            Log.e(TAG, "俯仰角：----------------" + calcResultFist[0] + "," + calcResultFist[1] + "," + calcResultFist[2] + ",");
//                            roolForShowLefthand = calcResultFist[1];
//                            pitchDifferenceLefthand = calcResultFist[0];
//                            roolDifferenceLefthand = calcResultFist[1];
//                            yawDifferenceLefthand = calcResultFist[2];
//                            pitchListLefthand.add(calcResultFist[0]);
//                            roolListLefthand.add(calcResultFist[1]);
//                            yawListhand.add(calcResultFist[2]);
//
//                            if (pitchListLefthand.size() != 0 && pitchListLefthand.size() % 3 == 0) {
//                                for (int i = 0; i < pitchListLefthand.size(); i++) {
//                                    double pitchMax = cd.ArrayListMax(pitchListLefthand);
//                                    double pitchMin = cd.ArrayListMin(pitchListLefthand);
//                                    resultList.add(pitchMax - pitchMin);
//
//                                    double roolMax = cd.ArrayListMax(roolListLefthand);
//                                    double roolMin = cd.ArrayListMin(roolListLefthand);
//                                    resultList.add(roolMax - roolMin);
//
//                                    double yawMax = cd.ArrayListMax(yawListhand);
//                                    double yawMin = cd.ArrayListMin(yawListhand);
//                                    resultList.add(yawMax - yawMin);
//
//                                    double maxResult = cd.ArrayListMax(resultList);
//
//                                    if (maxResult > 60) {
//
//                                        if ((pitchMax - pitchMin) == maxResult) {
//                                            Log.e(TAG, gatt.getDevice().getName() + ":" + maxResult + "----------------------------左手勾拳----------------------------------");
//                                            action = "left_hook";
//                                        } else if ((roolMax - roolMin) == maxResult) {
//                                            Log.e(TAG, gatt.getDevice().getName() + ":" + maxResult + "----------------------------左手直拳----------------------------------");
//                                            action = "left_straight";
//                                        } else if ((yawMax - yawMin) == maxResult) {
//                                            Log.e(TAG, gatt.getDevice().getName() + ":" + maxResult + "----------------------------左手摆拳----------------------------------");
//                                            action = "right_swing";
//                                        }
//
//
//                                    } else {
//                                        action = "ready";
//                                    }
//                                    pitchListLefthand = new ArrayList();
//                                    roolListLefthand = new ArrayList();
//                                    yawListhand = new ArrayList();
//                                    resultList = new ArrayList();
//
//
//                                }
//                            }
//
//
//                            if (isNaN(pitchDifferenceLefthand)) {
//                                count = 0;
//                            }

                            ii[3] = cd.getAngular(ii[3]);
                            ii[4] = cd.getAngular(ii[4]);
                            ii[5] = cd.getAngular(ii[5]);
                            if (lefthandResultCount <= 0) {
                                if (Math.abs(iiAgo[3]) < 80 && Math.abs(iiAgo[4]) > 100 && Math.abs(ii[3]) > 80) {
                                    Log.e(TAG, gatt.getDevice().getName() + ":" + "----------------------------左手直拳----------------------------------");
                                    Log.e(TAG, "lefthandResultCount" + lefthandResultCount);
                                    action = "left_straight";
                                    lefthandResultCount = 6;
                                } else if (Math.abs(ii[3]) > 80 && Math.abs(ii[4]) > 80) {
                                    Log.e(TAG, gatt.getDevice().getName() + ":" + "----------------------------左手直拳-----------------------------------");
                                    Log.e(TAG, "lefthandResultCount" + lefthandResultCount);
                                    action = "left_straight";
                                    lefthandResultCount = 6;
                                } else if (Math.abs(ii[3]) > 100 && Math.abs(ii[5]) > 100) {
                                    Log.e(TAG, gatt.getDevice().getName() + ":" + "----------------------------左手勾拳----------------------------------");
                                    Log.e(TAG, "lefthandResultCount" + lefthandResultCount);
                                    action = "left_straight";
                                    lefthandResultCount = 6;
                                }
                                //Log.e(TAG, "address:" + ii[0]+","+ii[1]+","+ii[2]+","+ii[3]+","+ii[4]+","+ii[5]);}
                            }
                            lefthandResultCount = lefthandResultCount - 1;

                        }
                    }
                }


                resultCalc = "";
//                Log.e(TAG, "address:" + gatt.getDevice().getAddress() + "result:" + count + ":" + result);
                // Log.e(TAG, "" + ii[0] + "," + ii[1] + "," + ii[2] + "," + ii[3] + "," + ii[4] + "," + ii[5]);
//                showResult = tvResultLefthand.getText().toString() + "address:" + gatt.getDevice().getAddress() + "result:" + count + ":" + result;
                result = "";
                count++;
                iiAgo = ii;

                Log.e(TAG, "address:" + ii[0] + "," + ii[1] + "," + ii[2] + "," + ii[3] + "," + ii[4] + "," + ii[5]);
                Log.e(TAG, "lefthandResultCount" + lefthandResultCount);


            }
            //Log.e(TAG,"result:"+result);
            // mLeDeviceListAdapter.writeResult(ss);
//            Log.e(TAG, "onCharWrite " + gatt.getDevice().getName()
//                    + " write "
//                    + characteristic.getUuid().toString()
//                    + " -> "
//                    +ss);
        }
    };
    /**
     * 搜索到BLE终端服务的事件
     */
    private BluetoothLeClass.OnServiceDiscoverListener mOnServiceDiscoverLefthand = new BluetoothLeClass.OnServiceDiscoverListener() {

        @Override
        public void onServiceDiscover(BluetoothGatt gatt) {
            displayGattServicesLefthand(mBLELefthand.getSupportedGattServices());
        }

    };

    private void displayGattServicesLefthand(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        for (BluetoothGattService gattService : gattServices) {
            int type = gattService.getType();
            //-----Characteristics的字段信息-----//
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                int permission = gattCharacteristic.getPermissions();
                int property = gattCharacteristic.getProperties();
                byte[] data = gattCharacteristic.getValue();
                if (data != null && data.length > 0) {
                }
                //UUID_KEY_DATA是可以跟蓝牙模块串口通信的Characteristic
                if (gattCharacteristic.getUuid().toString().equals(UUID_KEY_DATA)) {
                    Runnable myRunnable = new Runnable() {
                        public void run() {
                            mHandlerLefthand.postDelayed(this, 100);
                            mBLELefthand.setCharacteristicNotification(gattCharacteristic, true);
                            //设置数据内容
                            gattCharacteristic.setValue("0");
                            //往蓝牙模块写入数据
                            mBLELefthand.writeCharacteristic(gattCharacteristic);
                            //tvResultLefthand.setText("手环滚动角为：" + roolForShowLefthand + "\n手环俯仰角为:" + pitchDifferenceLefthand + "\n手环航向角为：" + yawDifferenceLefthand);
//                            if (action == "hook_right") {
//                                vvResult.setVideoPath("/sdcard/hook_right.avi");
//
//                                vvResult.start();
//                                tvResultRight.setText("左勾拳");
//                            } else {
//                                tvResultRight.setText("初始状态");
//                            }
                        }


                    };
                    mHandlerLefthand.post(myRunnable);

                    //接受Characteristic被写的通知,收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
                    mBLELefthand.setCharacteristicNotification(gattCharacteristic, true);
                    //设置数据内容
                    gattCharacteristic.setValue("0");
                    //往蓝牙模块写入数据
                    mBLELefthand.writeCharacteristic(gattCharacteristic);
                }

                //-----Descriptors的字段信息-----//
                List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();
                for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
                    //Log.e(TAG, "-------->desc uuid:" + gattDescriptor.getUuid());
                    int descPermission = gattDescriptor.getPermissions();
                    //Log.e(TAG,"-------->desc permission:"+ Utils.getDescPermission(descPermission));

                    byte[] desData = gattDescriptor.getValue();
                    if (desData != null && desData.length > 0) {
                        //Log.e(TAG, "-------->desc value:"+ new String(desData));
                    }
                }
            }
        }

    }


    /**
     * 收到BLE终端数据交互的事件
     */
    private BluetoothLeClass.OnDataAvailableListener mOnDataAvailableRighthand = new BluetoothLeClass.OnDataAvailableListener() {

        /**
         * BLE终端数据被读的事件
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS)
                Log.e(TAG, "onCharRead " + gatt.getDevice().getName()
                        + " read "
                        + characteristic.getUuid().toString()
                        + " -> "
                        + Utils.bytesToHexString(characteristic.getValue()));
        }

        double[] iiAgo = {0, 0, 0, 0, 0, 0};
        String result = "";
        int count = 1;
        String resultCalc = "";
        double q0 = 1;
        double q1 = 0;
        double q2 = 0;
        double q3 = 0;


        double[] calcResultFist = {0, 0, 0, 0, 0, 0, 0};//计算出拳累加数组
        ArrayList resultList = new ArrayList();
        double[] calcResultMove = {1, 0, 0, 0, 0, 0};//计算位移累加


        /**
         * 收到BLE终端写入数据回调
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic) {


            String ss = characteristic.getStringValue(0);

            String ss2 = "";
            result = result + ss;
            resultCalc = resultCalc + ss;
            if (ss.contains("\r\n")) {
                result = result.toString().replace("\r\n", ", ");
                resultCalc = resultCalc.toString().replace("\r\n", ", ");
                resultCalc = "";

                CalculateData cd = new CalculateData();
                String deviceNametemp = gatt.getDevice().getName().toString();
                double[] ii = cd.DataCleaningForDevice(result, deviceNametemp);
                // Log.e(TAG, "address:" + gatt.getDevice().getAddress() + "result:" + count + ":" + result);
                //计算出拳
                if (count == 1) {
                    if (ii[1] != 0) {
                        double[] in = {ii[0], ii[1], ii[2], ii[3], ii[4], ii[5], 1, 0, 0, 0};//ax，ay,az,gx,gy,gz,q0,q1,q2,q3,pitch,rool,yaw
                        calcResultFist = cd.CalculateAx(in);
                    }

                } else {
                    if (calcResultFist != null) {
                        if (ii[1] != 0) {
//                            double in[] = {ii[0], ii[1], ii[2], ii[3], ii[4], ii[5], calcResultFist[3], calcResultFist[4], calcResultFist[5], calcResultFist[6]};
//                            calcResultFist = cd.CalculateAx2(in);
//
//
////                            Log.e(TAG, "俯仰角：----------------" + calcResultFist[0] + "," + calcResultFist[1] + "," + calcResultFist[2] + ",");
//                            roolForShowRighthand = calcResultFist[1];
//                            pitchDifferenceRightthand = calcResultFist[0];
//                            roolDifferenceRighthand = calcResultFist[1];
//                            yawDifferenceRighthand = calcResultFist[2];
//                            pitchListRighthand.add(calcResultFist[0]);
//                            roolListRighthand.add(calcResultFist[1]);
//                            yawListRighthand.add(calcResultFist[2]);
//                            Log.e(TAG, pitchListRighthand.size() + "");
//                            if (pitchListRighthand.size() >=3 ) {
//                                for (int i = 0; i < pitchListRighthand.size(); i++) {
//
//                                    //俯仰角
//                                    double pitchMax = cd.ArrayListMax(pitchListRighthand);
//                                    double pitchMin = cd.ArrayListMin(pitchListRighthand);
//                                    resultList.add(pitchMax - pitchMin);
//
//                                    //滚动
//
//                                    double roolMax = cd.ArrayListMax(roolListRighthand);
//                                    double roolMin = cd.ArrayListMin(roolListRighthand);
//                                    resultList.add(roolMax - roolMin);
//
//                                    //航向
//                                    double yawMax = cd.ArrayListMax(yawListRighthand);
//                                    double yawMin = cd.ArrayListMin(yawListRighthand);
//                                    resultList.add(yawMax - yawMin);
//
//                                    double maxResult = cd.ArrayListMax(resultList);
//                                    if (maxResult > 40) {
//
//                                        if ((pitchMax - pitchMin) == maxResult) {
//                                            Log.e(TAG, gatt.getDevice().getName() + ":"+pitchListRighthand.size() + maxResult + "----------------------------右手右勾拳----------------------------------");
//                                            action = "right_hook";
//                                        } else if ((roolMax - roolMin) == maxResult) {
//                                            Log.e(TAG, gatt.getDevice().getName() + ":" +pitchListRighthand.size() + maxResult + "----------------------------右手直拳----------------------------------");
//                                          action = "right_straight";
//                                        } else if ((yawMax - yawMin) == maxResult) {
//                                            Log.e(TAG, gatt.getDevice().getName() + ":" + pitchListRighthand.size() +maxResult + "----------------------------右手摆拳----------------------------------");
////                                            action = "right_swing";cm
//                                        }
//                                        //Log.e(TAG, gatt.getDevice().getName() + ":" + maxResult + "----------------------------右手右勾拳----------------------------------");
//                                        //action = "right_hook";
//
//                                    } else {
//                                        action = "ready";
//                                    }
//
//                                    pitchListRighthand.remove(0);
//                                    roolListRighthand.remove(0);
//                                    yawListRighthand.remove(0);
//                                    resultList.remove(0);
//
//
//                                }
//                            }
//
//
//                            if (isNaN(pitchDifferenceLefthand)) {
//                                count = 1;
//                            }
//                            if (count == 600) {
//                                count = 1;
//                            }


                            ii[3] = cd.getAngular(ii[3]);
                            ii[4] = cd.getAngular(ii[4]);
                            ii[5] = cd.getAngular(ii[5]);
                            if (righthandResultCount <= 0) {
                                if (iiAgo[3] < 80 && iiAgo[4] > 100 && ii[3] > 80) {
                                    Log.e(TAG, gatt.getDevice().getName() + ":" + "----------------------------右手摆拳----------------------------------");
                                    Log.e(TAG, "righthandResultCount" + righthandResultCount);
                                    action = "right_swing";
                                    righthandResultCount = 6;
                                } else if (ii[3] > 80 && ii[4] > 80) {
                                    Log.e(TAG, gatt.getDevice().getName() + ":" + "----------------------------右手摆拳-----------------------------------");
                                    Log.e(TAG, "righthandResultCount" + righthandResultCount);
                                    action = "right_swing";
                                    righthandResultCount = 6;
                                } else if (ii[3] > 100 && ii[5] > 100) {
                                    Log.e(TAG, gatt.getDevice().getName() + ":" + "----------------------------右手右勾拳----------------------------------");
                                    Log.e(TAG, "righthandResultCount" + righthandResultCount);
                                    action = "right_hook";
                                    righthandResultCount = 6;
                                }
                                //Log.e(TAG, "address:" + ii[0]+","+ii[1]+","+ii[2]+","+ii[3]+","+ii[4]+","+ii[5]);}
                            }
                            righthandResultCount = righthandResultCount - 1;


                        }
                    }
                }
                Log.e(TAG, "address:" + ii[0] + "," + ii[1] + "," + ii[2] + "," + ii[3] + "," + ii[4] + "," + ii[5]);
                Log.e(TAG, "righthandResultCount" + righthandResultCount);
                //  Log.e(TAG, "address:" + gatt.getDevice().getAddress() + "result:" + count + ":" + result);

                result = "";
                count++;
                iiAgo = ii;


            }

        }
    };
    /**
     * 搜索到BLE终端服务的事件
     */
    private BluetoothLeClass.OnServiceDiscoverListener mOnServiceDiscoverRighthand = new BluetoothLeClass.OnServiceDiscoverListener() {

        @Override
        public void onServiceDiscover(BluetoothGatt gatt) {
            displayGattServicesRighthand(mBLERighthand.getSupportedGattServices());
        }

    };

    private void displayGattServicesRighthand(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        for (BluetoothGattService gattService : gattServices) {
            //-----Service的字段信息-----//
            int type = gattService.getType();
//            Log.e(TAG,"-->service type:"+Utils.getServiceType(type));
//            Log.e(TAG,"-->includedServices size:"+gattService.getIncludedServices().size());
//            Log.e(TAG,"-->service uuid:"+gattService.getUuid());

            //-----Characteristics的字段信息-----//
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                //Log.e(TAG,"---->char uuid:"+gattCharacteristic.getUuid());

                int permission = gattCharacteristic.getPermissions();
                //Log.e(TAG,"---->char permission:"+Utils.getCharPermission(permission));

                int property = gattCharacteristic.getProperties();
                //Log.e(TAG,"---->char property:"+Utils.getCharPropertie(property));

                byte[] data = gattCharacteristic.getValue();
                if (data != null && data.length > 0) {
                    //Log.e(TAG,"---->char value:"+new String(data));
                }

                //UUID_KEY_DATA是可以跟蓝牙模块串口通信的Characteristic
                if (gattCharacteristic.getUuid().toString().equals(UUID_KEY_DATA)) {


                    Runnable myRunnable = new Runnable() {
                        public void run() {
                            mHandlerRight.postDelayed(this, 100);
                            mBLERighthand.setCharacteristicNotification(gattCharacteristic, true);
                            gattCharacteristic.setValue("0");
                            mBLERighthand.writeCharacteristic(gattCharacteristic);

                        }


                    };
                    mHandlerRight.post(myRunnable);
                    mBLERighthand.setCharacteristicNotification(gattCharacteristic, true);
                    gattCharacteristic.setValue("0");
                    mBLERighthand.writeCharacteristic(gattCharacteristic);
                }

                //-----Descriptors的字段信息-----//
                List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();
                for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
                    //Log.e(TAG, "-------->desc uuid:" + gattDescriptor.getUuid());
                    int descPermission = gattDescriptor.getPermissions();
                    //Log.e(TAG,"-------->desc permission:"+ Utils.getDescPermission(descPermission));

                    byte[] desData = gattDescriptor.getValue();
                    if (desData != null && desData.length > 0) {
                        //Log.e(TAG, "-------->desc value:"+ new String(desData));
                    }
                }
            }
        }

    }


    /**
     * 收到BLE终端数据交互的事件
     */
    private BluetoothLeClass.OnDataAvailableListener mOnDataAvailableWaist = new BluetoothLeClass.OnDataAvailableListener() {

        /**
         * BLE终端数据被读的事件
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS)
                Log.e(TAG, "onCharRead " + gatt.getDevice().getName()
                        + " read "
                        + characteristic.getUuid().toString()
                        + " -> "
                        + Utils.bytesToHexString(characteristic.getValue()));
        }

        String result = "";
        int count = 1;
        String resultCalc = "";
        double q0 = 1;
        double q1 = 0;
        double q2 = 0;
        double q3 = 0;


        double[] calcResultFist = {0, 0, 0, 0, 0, 0, 0};//计算出拳累加数组
        ArrayList resultList = new ArrayList();
        double[] calcResultMove = {1, 0, 0, 0, 0, 0};//计算位移累加


        /**
         * 收到BLE终端写入数据回调
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic) {


            String ss = characteristic.getStringValue(0);
            String ss2 = "";
            result = result + ss;
            resultCalc = resultCalc + ss;
            if (ss.contains("\r\n")) {
                result = result.toString().replace("\r\n", ", ");
                resultCalc = resultCalc.toString().replace("\r\n", ", ");
                CalculateData cd = new CalculateData();
                String deviceNametemp = gatt.getDevice().getName().toString();
                double[] ii = cd.DataCleaningForDevice(result, deviceNametemp);
                ii[0] = cd.getComplement(ii[0]);
                ii[1] = cd.getComplement(ii[1]);
                ii[2] = cd.getComplement(ii[2]);
                ii[3] = cd.getComplement(ii[3]);
                ii[4] = cd.getComplement(ii[4]);
                ii[5] = cd.getComplement(ii[5]);
                if (waistResultCount <= 0) {
                    if (ii[0] != 0) {
                        if (Math.abs(ii[2] - iiAgoWaist[2]) > 2500) {
                            action = "move_right";
                            Log.e(TAG, "=============================================躲避==============================================================");
                            waistResultCount = 6;
                        }
                    }
                }
                waistResultCount = waistResultCount - 1;
                iiAgoWaist = ii;
                Log.e(TAG, "address:ax=" + ii[0] + ",ay=" + ii[1] + ",az=" + ii[2] + ",gx=" + ii[3] + ",gy=" + ii[4] + ",gz=" + ii[5]);
                result = "";
                count++;


            }


        }


    };
    /**
     * 搜索到BLE终端服务的事件
     */
    private BluetoothLeClass.OnServiceDiscoverListener mOnServiceDiscoverWaist = new BluetoothLeClass.OnServiceDiscoverListener() {

        @Override
        public void onServiceDiscover(BluetoothGatt gatt) {
            displayGattServicesWaist(mBLEWaist.getSupportedGattServices());
        }

    };

    private void displayGattServicesWaist(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        for (BluetoothGattService gattService : gattServices) {
            //-----Service的字段信息-----//
            int type = gattService.getType();
//            Log.e(TAG,"-->service type:"+Utils.getServiceType(type));
//            Log.e(TAG,"-->includedServices size:"+gattService.getIncludedServices().size());
//            Log.e(TAG,"-->service uuid:"+gattService.getUuid());

            //-----Characteristics的字段信息-----//
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                //Log.e(TAG,"---->char uuid:"+gattCharacteristic.getUuid());

                int permission = gattCharacteristic.getPermissions();
                //Log.e(TAG,"---->char permission:"+Utils.getCharPermission(permission));

                int property = gattCharacteristic.getProperties();
                //Log.e(TAG,"---->char property:"+Utils.getCharPropertie(property));

                byte[] data = gattCharacteristic.getValue();
                if (data != null && data.length > 0) {
                    //Log.e(TAG,"---->char value:"+new String(data));
                }

                //UUID_KEY_DATA是可以跟蓝牙模块串口通信的Characteristic
                if (gattCharacteristic.getUuid().toString().equals(UUID_KEY_DATA)) {
                    Runnable myRunnable = new Runnable() {
                        public void run() {
                            mHandlerWaist.postDelayed(this, 100);
                            mBLEWaist.setCharacteristicNotification(gattCharacteristic, true);
                            gattCharacteristic.setValue("0");
                            mBLEWaist.writeCharacteristic(gattCharacteristic);
                        }


                    };
                    mHandlerWaist.post(myRunnable);
                    mBLEWaist.setCharacteristicNotification(gattCharacteristic, true);
                    gattCharacteristic.setValue("0");
                    mBLEWaist.writeCharacteristic(gattCharacteristic);

                }

                //-----Descriptors的字段信息-----//
                List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();
                for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
                    //Log.e(TAG, "-------->desc uuid:" + gattDescriptor.getUuid());
                    int descPermission = gattDescriptor.getPermissions();
                    //Log.e(TAG,"-------->desc permission:"+ Utils.getDescPermission(descPermission));

                    byte[] desData = gattDescriptor.getValue();
                    if (desData != null && desData.length > 0) {
                        //Log.e(TAG, "-------->desc value:"+ new String(desData));
                    }
                }
            }
        }

    }


    public boolean isNaN(double v) {
        return (v != v);
    }

}
