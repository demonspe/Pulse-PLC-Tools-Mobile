package com.example.pulseplctoolsmobile.models;

public class ImpEnergyValue {
    public ImpEnergyValue() {
        setValue_Wt(0);
    }
    public ImpEnergyValue(long energyInWth) {
        setValue_Wt(energyInWth);
    }
    public ImpEnergyValue(String e_kWth) {
        String eStr = e_kWth.replace(',', '.');
        double tmp = Double.parseDouble(eStr);
        setValue_Wt((int)(tmp*1000));
    }
    private long e; //Энергия в ваттах
    private double e_kWt; //Энергия в киловатах

    public long getValue_Wt(){ return e; }
    public void setValue_Wt(long value) {
        if (value > 3999999999L) {
            e = 3999999999L;
        }
        else {
            e = value;
        }
        e_kWt = (double)e / 1000;
    }

    public double getValue_kWt() { return e_kWt; }
    public void setValue_kWt(double value) {
        if (value > (double)0xFFFFFFFF / 1000) {
            e_kWt = 3999999.999;
        }
        else {
            e_kWt = value;
        }
        e = (long)(e_kWt * 1000);
    }
}
