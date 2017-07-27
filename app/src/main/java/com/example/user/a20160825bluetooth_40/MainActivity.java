package com.example.user.a20160825bluetooth_40;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice mBluetoothDevice = null;
    private BluetoothGatt mBluetoothGatt = null;
    private BluetoothGattService mBluetoothSelectedService = null;
    private BluetoothGattCharacteristic ch = null;
    ArrayList<String> deviceAddressList=new ArrayList<>();
    ArrayList<String>  deviceNameList=new ArrayList<>();
    private Context mParent;
    Button bt1,bt2;
    TextView tv,tv2;
    String showValue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mParent = getApplicationContext();
        mBluetoothManager = (BluetoothManager) mParent.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothAdapter.enable();
       // mBluetoothAdapter.startLeScan(leScanDeviceCallback);

        bt1= (Button) findViewById(R.id.button);
        bt2 = (Button) findViewById(R.id.button2);
        bt1.setOnClickListener(btn1);
        bt2.setOnClickListener(btn2);
        tv=(TextView)findViewById(R.id.textView);
        tv=(TextView)findViewById(R.id.textView2);

    }

    View.OnClickListener btn1=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mBluetoothAdapter.startLeScan(leScanDeviceCallback);
        }
    };
    View.OnClickListener btn2=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mBluetoothAdapter.stopLeScan(leScanDeviceCallback);
        }
    };
        private BluetoothAdapter.LeScanCallback leScanDeviceCallback = new BluetoothAdapter.LeScanCallback() {
            @Override    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                final String deviceName = device.getName();
                if (deviceName.contains("Thermo") || deviceName.substring(0, 4).equals("(SP)")) {
                    if (!deviceAddressList.contains(device.getAddress())                   ) {
                        deviceAddressList.add(device.getAddress());
                        deviceNameList.add(device.getName());
                        tv.setText(device.getName());
                        Log.e("device.getName",device.getName());

                        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(device.getAddress());
                        mBluetoothAdapter = mBluetoothManager.getAdapter();
                        mBluetoothGatt = mBluetoothDevice.connectGatt(mParent, false, mBleCallback);


                    }
                }
            }
        };

       // mBluetoothAdapter.stopLeScan(leScanDeviceCallback);



    final BluetoothGattCallback mBleCallback=new BluetoothGattCallback() {


        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // 讀RSSI        ///mBluetoothGatt.readRemoteRssi();        // 發現服務//
                mBluetoothGatt.discoverServices();
                // isConnectGatt = true;
                tv.setText("成功");
                Log.e("log", "成功");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //showToast("連線失敗");
                tv.setText("失敗");
                Log.e("log", "失敗");

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            openINDICATION(gatt);
        }

        //  <--openINDICATION不屬於GattCallback-->
        private void openINDICATION(BluetoothGatt gatt) {

            // 取特定服務
            mBluetoothSelectedService = mBluetoothGatt.getService(UUID.fromString("00001810-0000-1000-8000-00805f9b34fb"));
            //UUID.fromString("00001810-0000-1000-8000-00805f9b34fb");    // 取特徵
            ch = mBluetoothSelectedService.getCharacteristic(UUID.fromString("00001810-0000-1000-8000-00805f9b34fb"));
            //UUID.fromString("00002a36-0000-1000-8000-00805f9b34fb");    // 接收Characteristic被寫的通知,收到藍芽模組的數據後會觸發onCharacteristicWrite
            setNotificationForCharacteristic(ch, true);
        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            getBloodPressureValue(characteristic);

        }

        public void setNotificationForCharacteristic(BluetoothGattCharacteristic ch, boolean enabled) {
            BluetoothGattDescriptor descriptor = ch.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
            mBluetoothGatt.setCharacteristicNotification(ch, enabled);


            if (descriptor != null) {
                byte[] val = enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                descriptor.setValue(val);
                mBluetoothGatt.writeDescriptor(descriptor);
            }
        }

        public void getBloodPressureValue(BluetoothGattCharacteristic ch) {
            byte[] rawValue = ch.getValue();
            int intValue = 0;
            if (rawValue.length == 19) {
                intValue = (int) (rawValue[1] & 0xFF);
                showValue += "高壓：" + String.valueOf(intValue) + "\n";
                intValue = (int) (rawValue[3] & 0xFF);
                showValue += "低壓：" + String.valueOf(intValue) + "\n";
                intValue = (int) (rawValue[14] & 0xFF);
                showValue += "心律：" + String.valueOf(intValue) + "\n";
                //showToast("收到19byte資料");    } else {        //showToast("沒收到19byte資料");*/
tv2.setText(showValue);
            }


        }


    };
    }
