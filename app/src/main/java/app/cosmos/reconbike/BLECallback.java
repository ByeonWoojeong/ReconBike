package app.cosmos.reconbike;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.lang.Thread.sleep;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BLECallback extends BluetoothGattCallback {

    Context context;
    MainActivity mainActivity = null;
    UnlockActivity unlockActivity = null;
    BluetoothGatt bluetoothGatt = null;
    BluetoothLeScanner bluetoothLeScanner;
    String command, device_id;
    int deviceIndex = 0;
    String TAG = "aaa";
    private BluetoothGattCharacteristic readCharacteristic, writeCharacteristic;
    private static final UUID BLUETOOTH_LE_CCCD           = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final UUID BLUETOOTH_LE_CC254X_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    private static final UUID BLUETOOTH_LE_CC254X_CHAR_RW = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    private static final UUID BLUETOOTH_LE_NRF_SERVICE    = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID BLUETOOTH_LE_NRF_CHAR_RW2   = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e"); // read on microbit, write on adafruit
    private static final UUID BLUETOOTH_LE_NRF_CHAR_RW3   = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID BLUETOOTH_LE_RN4870_SERVICE = UUID.fromString("49535343-FE7D-4AE5-8FA9-9FAFD205E455");
    private static final UUID BLUETOOTH_LE_RN4870_CHAR_RW = UUID.fromString("49535343-1E4D-4BD9-BA61-23C647249616");
    ArrayList<BluetoothDevice> devicesDiscovered = new ArrayList<>();
    static BLECallback getBLECallback;

    public static BLECallback getBLECallback() {
        if (getBLECallback == null) {
            throw new IllegalStateException("BLECallback is NULL");
        }
        return getBLECallback;
    }

    public BLECallback(Context context, BluetoothLeScanner bluetoothLeScanner) {
        getBLECallback = this;
        this.context = context;
        this.bluetoothLeScanner = bluetoothLeScanner;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void setUnlockActivity(UnlockActivity unlockActivity) {
        this.unlockActivity = unlockActivity;
    }

    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (device_id != null) {
                devicesDiscovered.add(result.getDevice());
                deviceIndex = devicesDiscovered.size() - 1;
                if (device_id.equals(result.getDevice().getName())) {
                    Log.d("aaa", "onScanResult: "+result.getDevice().getName()+"  "+result.getDevice().getAddress());
                    bluetoothGatt = devicesDiscovered.get(deviceIndex).connectGatt(context, false, BLECallback.this);
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            deviceIndex = 0;
                            devicesDiscovered.clear();
                            bluetoothLeScanner.stopScan(scanCallback);
                        }
                    });
                }
            }
        }
    };

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
        final String get_response_command = new String(characteristic.getValue());
        Log.d("aaa", "onCharacteristicChanged: "+get_response_command);
        switch (get_response_command){
            case "L_OP_ALR":
                if (unlockActivity != null){
                    unlockActivity.l_op_alrdy();
                }
                break;
            case "L_OP_OK":
                if (unlockActivity != null){
                    unlockActivity.l_op_ok();
                }
                break;
            case "L_OP_ERR":
                if (unlockActivity != null){
                    unlockActivity.l_op_err();
                }
                break;
            case "L_ST_OP":
                if (mainActivity != null) {
                    mainActivity.l_st_op();
                }
                break;
            case "L_ST_CL":
                if (mainActivity != null) {
                    mainActivity.l_st_cl();
                }
                break;
            case "L_ST_ERR":
                if (mainActivity != null) {
                    mainActivity.l_st_err();
                }
                break;
        }
        bluetoothGatt.disconnect();
        bluetoothGatt.close();
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if(characteristic == writeCharacteristic) {
            Log.d(TAG, "onCharacteristicWrite: "+status);
        }
    }

    @Override
    public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
        Log.d("aaa", "onConnectionStateChange: "+newState);
        switch (newState) {
            case 0:
                if (unlockActivity != null){
                    unlockActivity.l_op_err();
                    bluetoothGatt.disconnect();
                    bluetoothGatt.close();
                }
                break;
            case 1:
                break;
            case 2:
                bluetoothGatt.discoverServices();
                break;
            default:
                break;
        }
    }

    @Override
    public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
        Log.d(TAG, "onServicesDiscovered: "+status);
        for (BluetoothGattService gattService : gatt.getServices()) {
            Log.d(TAG, "onServicesDiscovered: "+gattService.getUuid());
            if (gattService.getUuid().equals(BLUETOOTH_LE_CC254X_SERVICE)) {
                readCharacteristic = gattService.getCharacteristic(BLUETOOTH_LE_CC254X_CHAR_RW);
                writeCharacteristic = gattService.getCharacteristic(BLUETOOTH_LE_CC254X_CHAR_RW);
            }
            if (gattService.getUuid().equals(BLUETOOTH_LE_RN4870_SERVICE)) {
                readCharacteristic = gattService.getCharacteristic(BLUETOOTH_LE_RN4870_CHAR_RW);
                writeCharacteristic = gattService.getCharacteristic(BLUETOOTH_LE_RN4870_CHAR_RW);
            }
            if (gattService.getUuid().equals(BLUETOOTH_LE_NRF_SERVICE)) {
                BluetoothGattCharacteristic rw2 = gattService.getCharacteristic(BLUETOOTH_LE_NRF_CHAR_RW2);
                BluetoothGattCharacteristic rw3 = gattService.getCharacteristic(BLUETOOTH_LE_NRF_CHAR_RW3);
                if (rw2 != null && rw3 != null) {
                    int rw2prop = rw2.getProperties();
                    int rw3prop = rw3.getProperties();
                    boolean rw2write = (rw2prop & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0;
                    boolean rw3write = (rw3prop & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0;
                    if (rw2write) { // some devices use this ...
                        writeCharacteristic = rw2;
                        readCharacteristic = rw3;
                    } else if (rw3write) { // ... and other devices use this characteristic
                        writeCharacteristic = rw3;
                        readCharacteristic = rw2;
                    }
                }
                bluetoothGatt.setCharacteristicNotification(readCharacteristic,true);
                BluetoothGattDescriptor readDescriptor = readCharacteristic.getDescriptor(BLUETOOTH_LE_CCCD);
                readDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                bluetoothGatt.writeDescriptor(readDescriptor);
            }
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        Log.d(TAG, "onDescriptorWrite: "+status);

        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        writeCharacteristic.setValue(command);
        if(!bluetoothGatt.writeCharacteristic(writeCharacteristic)){
            if (unlockActivity != null){
                unlockActivity.l_op_err();
                bluetoothGatt.disconnect();
                bluetoothGatt.close();
            }
        }
    }
}
