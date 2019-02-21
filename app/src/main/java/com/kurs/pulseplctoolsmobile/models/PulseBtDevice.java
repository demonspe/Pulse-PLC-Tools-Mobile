package com.kurs.pulseplctoolsmobile.models;

import android.bluetooth.BluetoothDevice;

public class PulseBtDevice {

    private BluetoothDevice device;
    private String workMode;
    private String serialNum;

    {
        workMode = "";
        serialNum = "";
    }

    public PulseBtDevice(BluetoothDevice device)
    {
        this.device = device;
        String btSearchName = device.getName();

        if(btSearchName != null) {
            if(btSearchName.length() >= 11) {
                String typePulseDevice = btSearchName.substring(0,2);
                if(typePulseDevice.equals("TU")) workMode = "Счетчик";
                if(typePulseDevice.equals("FA")) workMode = "Фаза A";
                if(typePulseDevice.equals("FB")) workMode = "Фаза B";
                if(typePulseDevice.equals("FC")) workMode = "Фаза C";
                serialNum = btSearchName.substring(3,11);
            }
        }
    }

    public String getFullName()
    {
        if(device == null) return "Undefined";
        if(workMode.length() == 0) return device.getName() + " " + device.getAddress();
        return "[" + serialNum + "] " + workMode;
    }

    public String getWorkMode()
    {
        return workMode;
    }

    public String getSerialNum()
    {
        return serialNum;
    }

    public BluetoothDevice getBtDevice()
    {
        return device;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        PulseBtDevice d = (PulseBtDevice) obj;
        return (this.serialNum.equals(d.serialNum) && this.workMode.equals(d.workMode));
    }
}
