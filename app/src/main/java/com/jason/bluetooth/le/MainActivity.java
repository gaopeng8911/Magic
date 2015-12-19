package com.jason.bluetooth.le;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {


    private final static String TAG = DeviceScanActivity.class.getSimpleName();
    private final static String UUID_KEY_DATA = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private static final long SCAN_PERIOD = 10000;
    private Context c;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    /**
     * 搜索BLE终端
     */
    private BluetoothAdapter mBluetoothAdapter;
    /**
     * 读写BLE终端
     */
    private BluetoothLeClass mBLE;//蓝牙连接服务
    private boolean mScanning;
    private Handler mHandler;
    private TextView tv;
    private String lefthandDeviceAddress = "";//左手设备地址
    private String righthandDeviceAddress = "";//右手设备地址
    private String waistDeviceAddress = "";//腰部设备地址
    private String lefthandDeviceName = "";//左手设备地址
    private String righthandDeviceName = "";//右手设备地址
    private String waistDeviceName = "";//腰部设备地址


    /**
     * Called when the activity is first created.
     */
    private Spinner lefthandDeviceSpinner;//左手设备下拉列表
    private Spinner righthandDeviceSpinner;//右手设备下拉列表
    private Spinner waistDeviceSpinner;//腰部设备下拉列表
    private Button btnConfirm;
    private ArrayList<BluetoothDevice> mLeDevices = new ArrayList<BluetoothDevice>();//蓝牙设备列表
    private List<String> mLeDeviceNameList = new ArrayList<String>();//蓝牙设备名称列表
    private String noDevice = "no device";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getActionBar().setTitle(R.string.title_devices);
        mHandler = new Handler();
        btnConfirm = (Button) findViewById(R.id.btn_confirm);
        mLeDeviceNameList.add(noDevice);
        c = this;
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        //开启蓝牙
        mBluetoothAdapter.enable();

        mBLE = new BluetoothLeClass(this);
        if (!mBLE.initialize()) {
            //Log.e(TAG, "Unable to initialize Bluetooth");
            finish();
        }


        //左手设备下拉列表设置
        lefthandDeviceSpinner = (Spinner) findViewById(R.id.spinner_left_hand);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mLeDeviceNameList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lefthandDeviceSpinner.setAdapter(adapter);
        lefthandDeviceSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                /* 将所选mySpinner 的值带入myTextView 中*/
                //    myTextView.setText("您选择的是："+ adapter.getItem(arg2));
                /* 将mySpinner 显示*/

                if (mLeDeviceNameList.get(arg2) == noDevice) {
                    lefthandDeviceAddress = noDevice;
                    lefthandDeviceName = noDevice;
                } else {
                    for (int i = 0; i < mLeDevices.size(); i++) {
                        if (mLeDevices.get(i).getName() != null && mLeDeviceNameList.get(arg2) != null) {
                            if (mLeDevices.get(i).getName().toString().equals(mLeDeviceNameList.get(arg2).toString())) {
                                lefthandDeviceAddress = mLeDevices.get(i).getAddress().toString();
                                lefthandDeviceName = mLeDevices.get(i).getName().toString();
                            }
                        }
                    }
                }
//                 arg1.setVisibility(View.INVISIBLE);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });


        //右手设备下拉列表设置
        righthandDeviceSpinner = (Spinner) findViewById(R.id.spinner_right_hand);
        ArrayAdapter<String> adapterRight = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mLeDeviceNameList);
        adapterRight.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        righthandDeviceSpinner.setAdapter(adapterRight);
        righthandDeviceSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                /* 将所选mySpinner 的值带入myTextView 中*/
                //    myTextView.setText("您选择的是："+ adapter.getItem(arg2));
                /* 将mySpinner 显示*/
                if (mLeDeviceNameList.get(arg2) == noDevice) {
                    righthandDeviceAddress = noDevice;
                    righthandDeviceName = noDevice;
                } else {
                    for (int i = 0; i < mLeDevices.size(); i++) {
                        if (mLeDevices.get(i).getName() != null && mLeDeviceNameList.get(arg2) != null) {
                            if (mLeDevices.get(i).getName().toString().equals(mLeDeviceNameList.get(arg2).toString())) {
                                righthandDeviceAddress = mLeDevices.get(i).getAddress().toString();
                                righthandDeviceName = mLeDevices.get(i).getName().toString();
                            }
                        }
                    }
                }
                // arg1.setVisibility(View.INVISIBLE);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });


        //腰部设备下拉列表设置
        waistDeviceSpinner = (Spinner) findViewById(R.id.spinner_waist);
        ArrayAdapter<String> adapterWaist = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mLeDeviceNameList);
        adapterWaist.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        waistDeviceSpinner.setAdapter(adapterWaist);
        waistDeviceSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                /* 将所选mySpinner 的值带入myTextView 中*/
                //    myTextView.setText("您选择的是："+ adapter.getItem(arg2));
                /* 将mySpinner 显示*/
                if (mLeDeviceNameList.get(arg2) == noDevice) {
                    waistDeviceAddress = noDevice;
                    waistDeviceName = noDevice;
                } else {
                    for (int i = 0; i < mLeDevices.size(); i++) {
                        if (mLeDevices.get(i).getName() != null && mLeDeviceNameList.get(arg2) != null) {
                            if (mLeDevices.get(i).getName().toString().equals(mLeDeviceNameList.get(arg2))) {
                                waistDeviceAddress = mLeDevices.get(i).getAddress().toString();
                                waistDeviceName = mLeDevices.get(i).getName().toString();
                            }
                        }
                    }
                }
                // arg1.setVisibility(View.INVISIBLE);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });


        //确认按键
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lefthandDeviceAddress == righthandDeviceAddress && righthandDeviceAddress == waistDeviceAddress && waistDeviceAddress == noDevice) {
                    new AlertDialog.Builder(c)
                            .setTitle("提示")
                            .setMessage("不能所有设备均为空")
                            .setPositiveButton("确定", null)
                            .show();
                } else if (isSameDevice()) {
                    new AlertDialog.Builder(c)
                            .setTitle("提示")
                            .setMessage("不能为不同部位选择相同的设备")
                            .setPositiveButton("确定", null)
                            .show();
                } else {
                    final Intent intent = new Intent(c, show_result.class);

                    ArrayList<String> deviceArrayList = new ArrayList<String>();
                    deviceArrayList.add(lefthandDeviceName);
                    deviceArrayList.add(lefthandDeviceAddress);
                    deviceArrayList.add(righthandDeviceName);
                    deviceArrayList.add(righthandDeviceAddress);
                    deviceArrayList.add(waistDeviceName);
                    deviceArrayList.add(waistDeviceAddress);
//                    intent.putExtra(show_result.EXTRAS_DEVICE_NAME_LEFTHAND, lefthandDeviceName);
//                    intent.putExtra(show_result.EXTRAS_DEVICE_ADDRESS_LEFTHAND, lefthandDeviceAddress);
//                    intent.putExtra(show_result.EXTRAS_DEVICE_NAME_RIGHTHAND, righthandDeviceName);
//                    intent.putExtra(show_result.EXTRAS_DEVICE_ADDRESS_RIGHTHAND, righthandDeviceAddress);
//                    intent.putExtra(show_result.EXTRAS_DEVICE_NAME_WAIST, waistDeviceName);
//                    intent.putExtra(show_result.EXTRAS_DEVICE_ADDRESS_WAIST, waistDeviceAddress);

                    intent.putStringArrayListExtra("deviceArrayList", deviceArrayList);
                    startActivity(intent);


                }


            }
        });


    }

    public boolean isSameDevice() {
        if (lefthandDeviceAddress == righthandDeviceAddress && righthandDeviceAddress == waistDeviceAddress && waistDeviceAddress == noDevice) {
            return false;
        } else if (lefthandDeviceAddress == noDevice && righthandDeviceAddress == waistDeviceAddress) {
            return true;
        } else if (righthandDeviceAddress == noDevice && lefthandDeviceAddress == waistDeviceAddress) {
            return true;
        } else if (waistDeviceAddress == noDevice && lefthandDeviceAddress == righthandDeviceAddress) {
            return true;
        } else if (lefthandDeviceAddress == righthandDeviceAddress&&righthandDeviceAddress == noDevice) {
            return false;
        } else if (lefthandDeviceAddress == waistDeviceAddress&&waistDeviceAddress == noDevice) {
            return false;
        }else if (righthandDeviceAddress == waistDeviceAddress&&waistDeviceAddress == noDevice) {
            return false;
        }else if (lefthandDeviceAddress == righthandDeviceAddress || lefthandDeviceAddress == waistDeviceAddress || righthandDeviceAddress == waistDeviceAddress) {
            return true;
        } else {
            return false;
        }


    }

    //添加蓝牙设备
    public void addDevice(BluetoothDevice device) {
        if (mLeDevices != null) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
                mLeDeviceNameList.add(device.getName());

                ArrayAdapter newAadpter = new ArrayAdapter<String>(c, android.R.layout.simple_spinner_item, mLeDeviceNameList);
                newAadpter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                lefthandDeviceSpinner.setAdapter(newAadpter);

                ArrayAdapter newAadpter2 = new ArrayAdapter<String>(c, android.R.layout.simple_spinner_item, mLeDeviceNameList);
                newAadpter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                righthandDeviceSpinner.setAdapter(newAadpter2);

                ArrayAdapter newAadpter3 = new ArrayAdapter<String>(c, android.R.layout.simple_spinner_item, mLeDeviceNameList);
                newAadpter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                waistDeviceSpinner.setAdapter(newAadpter3);


            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Initializes list view adapter.
        //mLeDeviceListAdapter = new LeDeviceListAdapter(this);
        //  setListAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        //mLeDeviceListAdapter.clear();
        mBLE.disconnect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBLE.close();
    }


    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }


    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addDevice(device);
//                            mLeDeviceListAdapter.addDevice(device);
//                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

}
