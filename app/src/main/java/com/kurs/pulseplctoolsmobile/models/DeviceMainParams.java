package com.kurs.pulseplctoolsmobile.models;

import com.kurs.pulseplctoolsmobile.enums.BatteryMode;
import com.kurs.pulseplctoolsmobile.enums.InterfaceMode;
import com.kurs.pulseplctoolsmobile.enums.WorkMode;

import java.util.Date;

public class DeviceMainParams {
    private PulsePLCv2Pass passWrite; //Пароль доступа к данным устройства с которым идет общение
    private PulsePLCv2Pass passRead; //Пароль доступа к данным устройства с которым идет общение
    private PulsePLCv2Pass passCurrent; //Пароль текущий пароль который вводит пользователь

    public PulsePLCv2Pass getPassWrite() { return passWrite; }
    public void setPassWrite(PulsePLCv2Pass pass) { passWrite = pass; }

    public PulsePLCv2Pass getPassRead() { return passRead; }
    public void setPassRead(PulsePLCv2Pass pass) { passRead = pass; }

    public PulsePLCv2Pass getPassCurrent() { return passCurrent; }
    public void setPassCurrent(PulsePLCv2Pass pass) { passCurrent = pass; }


    private PulsePLCv2Serial serial;    //Серийный номер устройства с которым идет общение
    private byte errorsByte;
    private String errorsList;
    private String firmwareVersion; //Версия прошивки
    private String eepromVersion;   //Версия разметки памяти
    private byte work_mode;             //Режим устройства (Счетчик/УСПД)
    private byte mode_No_Battery;       //Режим работы без часов, тарифов и BKP (без батареи)
    private byte rs485_Work_Mode;       //Режим работы интерфейса (выкл, чтение, чтение/запись)
    private byte bluetooth_Work_Mode;   //Режим работы интерфейса (выкл, чтение, чтение/запись)
    private boolean newPassWrite;  //Флаг записи нового пароля
    private boolean newPassRead;   //Флаг записи нового пароля
    private Date deviceDateTime; //Время прочитанное из устройства

    public PulsePLCv2Serial getSerial() { return serial; }
    public void setSerial(PulsePLCv2Serial serial) { this.serial = serial; }

    public byte getErrorsByte() { return errorsByte; }
    public void setErrorsByte(byte errByte) {
        errorsByte = errByte;
        if (errorsByte == 0)
            errorsList = "Нет ошибок";
        else
        {
            errorsList = "";
            if ((errorsByte & 1) > 0) errorsList += "Проблема с батарейкой \n";
            if ((errorsByte & 2) > 0) errorsList += "Режим без батарейки \n";
            if ((errorsByte & 4) > 0) errorsList += "Переполнение IMP1 \n";
            if ((errorsByte & 8) > 0) errorsList += "Переполнение IMP2 \n";
            if ((errorsByte & 16) > 0) errorsList += "Проблема с памятью \n";
            if ((errorsByte & 32) > 0) errorsList += "Ошибка времени \n";
            //!! добавить ошибки ДОДЕЛАТЬ
        }
    }
    public String getErrorsList() { return errorsList; }

    public String getVersionFirmware() {return firmwareVersion; }
    public void setVersionFirmware(String version) { firmwareVersion = version; }

    public String getVersionEEPROM() { return eepromVersion; }
    public void setVersionEEPROM(String version) { eepromVersion = version;}

    public WorkMode getWorkMode() { return WorkMode.values()[work_mode]; }
    public void setWorkMode(WorkMode mode) { work_mode = (byte)mode.ordinal(); }

    public BatteryMode getBatteryMode() { return BatteryMode.values()[mode_No_Battery]; }
    public void setBatteryMode(BatteryMode mode) { mode_No_Battery = (byte)mode.ordinal(); }

    public InterfaceMode getRS485_WorkMode() { return InterfaceMode.values()[rs485_Work_Mode]; }
    public void setRS485_WorkMode(InterfaceMode mode) { rs485_Work_Mode = (byte)mode.ordinal(); }

    public InterfaceMode getBluetooth_WorkMode() { return InterfaceMode.values()[bluetooth_Work_Mode]; }
    public void setBluetooth_WorkMode(InterfaceMode mode) { bluetooth_Work_Mode = (byte)mode.ordinal(); }

    public boolean getNewPassWrite() { return newPassWrite; }
    public void setNewPassWrite(boolean newPassWrite) { this.newPassWrite = newPassWrite;  }
    public boolean getNewPassRead() { return newPassRead; }
    public void  setNewPassRead(boolean newPassRead) { this.newPassRead = newPassRead; }

    //DateTime
    public Date getDeviceDateTime() { return deviceDateTime; }
    public void setDeviceDateTime(Date deviceDateTime) { this.deviceDateTime = deviceDateTime; }
    public Date getPCDateTime() { return new Date(); } //Время компьютера
    public Date getTimeDifference() { //Разница
        return new Date(getPCDateTime().getTime() - getDeviceDateTime().getTime());
    }

    public DeviceMainParams()
    {
        SetDefaultParams();
    }

    public void SetDefaultParams()
    {
        setErrorsByte((byte)0);
        setNewPassWrite(false);
        setNewPassRead(false);
        setPassCurrent(new PulsePLCv2Pass());
        setPassRead(new PulsePLCv2Pass());
        setPassWrite(new PulsePLCv2Pass());
        setWorkMode(WorkMode.Counter);
        setBatteryMode(BatteryMode.Enable);
        setRS485_WorkMode(InterfaceMode.ReadOnly);
        setBluetooth_WorkMode(InterfaceMode.ReadOnly);
    }
}
