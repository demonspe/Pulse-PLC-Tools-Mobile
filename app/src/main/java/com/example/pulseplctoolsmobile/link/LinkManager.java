package com.example.pulseplctoolsmobile.link;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.example.pulseplctoolsmobile.Helper;
import com.example.pulseplctoolsmobile.OnMessageListener;
import com.example.pulseplctoolsmobile.models.PulseBtDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LinkManager {

    //Тэг для логов
    private static final String TAG = "LinkManager";
    private static final String HC_08_SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private static final String HC_08_CHARACTERISTIC_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";

    //BLE
    BluetoothAdapter bt;
    private BluetoothGatt mGatt;
    BluetoothGattCharacteristic btCharacteristic;
    private boolean mScanning;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000;
    ArrayList<PulseBtDevice> listBtDevices; //Список найденных устройств

    //Statuses
    private boolean isConnected;
    private boolean isConnecting;

    //Events
    private OnLinkManagerInteractionListener mDataReceivedListener;
    public void setListener(OnLinkManagerInteractionListener mDataReceivedListener) {
        this.mDataReceivedListener = mDataReceivedListener;
    }
    private OnMessageListener onMessageListener;
    public void setMessageListener(OnMessageListener onMessageListener)
    {
        this.onMessageListener = onMessageListener;
    }

    public LinkManager()
    {
        //Список устройств
        listBtDevices = new ArrayList<>();
        //Bt adapter
        bt = BluetoothAdapter.getDefaultAdapter();
        mHandler = new Handler(); //Для остановки поиска через определенное время
    }

    //Flag
    public boolean getIsConnected() { return isConnected; }
    public boolean getIsConnecting() { return isConnecting; }

    //Search Devices
    //Сканировать устройства вокруг
    public void scanLeDevice(final boolean enable) {
        if (enable) {
            //Stop scan if was started
            if(mScanning) bt.stopLeScan(mLeScanCallback);
            //
            if(isConnected) disconnect();
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bt.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            listBtDevices.clear();
            bt.startLeScan(mLeScanCallback);
        } else {
            mHandler.removeCallbacksAndMessages(null);
            mScanning = false;
            bt.stopLeScan(mLeScanCallback);
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    if (device != null) {
                        PulseBtDevice d = new PulseBtDevice(device);
                        if (d.getWorkMode().length() != 0) //If it's PulsePLC device type
                        {
                            for (PulseBtDevice item : listBtDevices)
                                if (item.equals(d)) return;
                            listBtDevices.add(d);
                            if(mDataReceivedListener != null)
                                mDataReceivedListener.onDeviceFound(d);
                            if(onMessageListener != null)
                                onMessageListener.onMessageShow("Найден: " + d.getFullName());
                        }
                    }
                }
            };

    //Data
    public boolean sendData(byte[] data) {
        if(btCharacteristic != null) {
            int num_of_packages = data.length/20 + 1; //Пакеты по 20 байт
            int allBytesCounter = 0;
            for(int i = 0; i < num_of_packages; i++) {
                int packageLength = 20;
                int lengthMath = data.length-packageLength*i;
                if(lengthMath < packageLength) packageLength = lengthMath;
                byte[] packBytes = new byte[packageLength];
                for(int k = 0; k < packageLength; k++) {
                    packBytes[k] = data[allBytesCounter++];
                    if(allBytesCounter >= data.length) break;
                }
                //Лог
                Log.d("SEND_DATA", Helper.toStringHEX(packBytes, ", "));
                btCharacteristic.setValue(packBytes);
                if(!mGatt.writeCharacteristic(btCharacteristic)) return false;
            }
            return true;
        }
        else
            return false;
    }

    //Connection
    public void connect(Context context, PulseBtDevice pulse) {
        if(pulse == null) return;
        if (mGatt == null && pulse.getBtDevice().getName() != null) {
            //Флаг о том что идет процесс подключения
            isConnecting = true;
            //Подключание
            mGatt = pulse.getBtDevice().connectGatt(context, false, gattCallback);
            //if(onMessageListener != null)
            //    onMessageListener.onMessageShow("Попытка подключения к " + pulse.getFullName());
        }
    }
    //Отключиться от устройства
    public void disconnect() {
        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;
        btCharacteristic = null;
        isConnected = false;
        isConnecting = false;
    }
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(TAG, "ConnectionState: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i(TAG, "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e(TAG, "STATE_DISCONNECTED");
                    if(mDataReceivedListener != null)
                        mDataReceivedListener.onDisconnect();
                    isConnected = false;
                    isConnecting = false;
                    break;
                default:
                    Log.e(TAG, "STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            Log.i(TAG, "Services discovered: " + services.toString());
            for (BluetoothGattService service : services){
                /*Log.d(TAG, "Service: " + service.getUuid().toStringUInt());
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()){
                    Log.d(TAG, " - characteristic:" +characteristic.getUuid().toStringUInt());
                }*/
                //Получаем нужный сервис
                if (service.getUuid().equals(UUID.fromString(HC_08_SERVICE_UUID))){
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()){
                        //Получаем нужную характеристику
                        if (characteristic.getUuid().equals(UUID.fromString(HC_08_CHARACTERISTIC_UUID))){
                            //Флаг о том что успешно подключено (нужен для меню)
                            isConnected = true;
                            isConnecting = false;
                            Log.d(TAG, "Получили характеристику, подписываемся на уведомления");
                            btCharacteristic = characteristic;
                            gatt.setCharacteristicNotification(characteristic, true);
                            //Сообщение об успешном подключении
                            if(onMessageListener != null)
                                onMessageListener.onMessageShow("Успешно подключено");
                            //Событие (Обязательно в самом конце!!!)
                            if(mDataReceivedListener != null)
                                mDataReceivedListener.onConnectionSuccessful();
                        }
                    }
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            //Получим данные
            byte[] inputData = characteristic.getValue();
            //Лог
            Log.d("INPUT_DATA", Helper.toStringHEX(inputData, ", "));

            //Вызываем событие "Пришли данные"
            if (mDataReceivedListener != null)
                mDataReceivedListener.onDataReceived(inputData);
        }
    };
}
