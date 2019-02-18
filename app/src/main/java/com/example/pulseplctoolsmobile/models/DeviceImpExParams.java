package com.example.pulseplctoolsmobile.models;

import com.example.pulseplctoolsmobile.enums.ImpNum;

import java.util.Calendar;
import java.util.Date;

public class DeviceImpExParams {
    private ImpNum num;
    private byte currentTarif;
    private int impCounter;
    private int timeMsFromLastImp;
    private int currentPower;
    private Date timeActualData;
    public ImpNum getNum() { return num; }
    public void setNum(ImpNum num) { this.num = num; }

    public byte getCurrentTarif() { return currentTarif; }
    public void setCurrentTarif(byte currentTarif) { this.currentTarif = currentTarif; }

    public int getImpCounter() { return impCounter; }
    public void setImpCounter(int impCounter) { this.impCounter = impCounter; }

    public int getTimeMsFromLastImp() { return timeMsFromLastImp; }
    public void setTimeMsFromLastImp(int timeMsFromLastImp) { this.timeMsFromLastImp = timeMsFromLastImp; }

    public int getCurrentPower() { return currentPower; }
    public void setCurrentPower(int currentPower) { this.currentPower = currentPower; }

    public Date getTimeActualData() { return timeActualData; }
    public void setTimeActualData(Date timeActualData) { this.timeActualData = timeActualData; }

    public DeviceImpExParams() { setDefault(); }
    public DeviceImpExParams(ImpNum num) {
        setDefault();
        setNum(num);
    }

    private void setDefault() {
        setNum(ImpNum.IMP1);
        setCurrentTarif((byte)1);
        setImpCounter(0);
        setTimeMsFromLastImp(0);
        setCurrentPower(0);
        setTimeActualData(Calendar.getInstance().getTime());
    }
}
