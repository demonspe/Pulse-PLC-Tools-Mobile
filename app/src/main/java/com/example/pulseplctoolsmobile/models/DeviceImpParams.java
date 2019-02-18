package com.example.pulseplctoolsmobile.models;

import com.example.pulseplctoolsmobile.Helper;
import com.example.pulseplctoolsmobile.enums.ImpAscueProtocolType;
import com.example.pulseplctoolsmobile.enums.ImpNum;
import com.example.pulseplctoolsmobile.enums.ImpNumOfTarifs;
import com.example.pulseplctoolsmobile.enums.ImpOverflowType;

public class DeviceImpParams {
    private ImpNum num;
    private byte isEnable;  //Включен/отключен
    private boolean isEnable_bool;  //Включен/отключен
    private byte adrs_PLC;  //Сетевой адрес
    private int a;          //A передаточное число
    ImpEnergyGroup e_Current;
    ImpEnergyGroup e_StartDay;
    private ImpOverflowType perepoln;   //число разрядов после которых происходит переполнение 0, 5 или 6
    private ImpNumOfTarifs t_qty;       //Количество тарифов
    private int ascue_adrs;             //Адрес для протокола
    private byte[] ascue_pass;          //Пароль для протокола
    private String ascue_pass_string;   //Пароль для протокола
    private ImpAscueProtocolType ascue_protocol;        //Тип протокола
    private int max_Power;              //Максимальная мощность нагрузки
    //Настройки времени тарифов
    private ImpTime t1_Time_1;
    private ImpTime t3_Time_1;
    private ImpTime t1_Time_2;
    private ImpTime t3_Time_2;
    private ImpTime t2_Time;


    public ImpNum getNum() { return num; }
    public void setNum(ImpNum value) { num = value; }

    public byte getIsEnable() { return isEnable; }
    public void setIsEnable(byte value) {
        if(value == 0){
            isEnable = 0;
            isEnable_bool = false;
        }
        if(value == 1){
            isEnable = 1;
            isEnable_bool = true;
        }
    }
    public boolean getIsEnable_bool() { return isEnable_bool; }
    public void setIsEnable_bool(boolean value) {
        isEnable_bool = value;
        if(value)
            isEnable = 1;
        else
            isEnable = 0;
    }

    public byte getAdrs_PLC() { return adrs_PLC; }
    public void setAdrs_PLC(byte value) { adrs_PLC = value; }

    public int getA() { return a; }
    public void setA(int value) { a = value; }

    public ImpEnergyGroup getE_Current() { return e_Current; }
    public void setE_Current(ImpEnergyGroup value) { e_Current = value; }

    public ImpEnergyGroup getE_StartDay() { return e_StartDay; }
    public void setE_StartDay(ImpEnergyGroup value) { e_StartDay = value; }

    public ImpOverflowType getPerepoln() { return perepoln; }
    public void setPerepoln(ImpOverflowType value) { perepoln = value; }

    public ImpNumOfTarifs getT_qty() { return t_qty; }
    public void setT_qty(ImpNumOfTarifs value) { t_qty = value; }

    public int getAscue_adrs() { return ascue_adrs; }
    public void setAscue_adrs(int value) { ascue_adrs = value; }

    public byte[] getAscue_pass() { return ascue_pass; }
    public void setAscue_pass(byte[] value) {
        for(byte b: value) { if(b < 0 || b > 9) return; }
        ascue_pass = value;
        ascue_pass_string = "";
        for (int i = 0; i < 6; i++) {
            if(i < ascue_pass.length)
                ascue_pass_string += ""+ascue_pass[i];
            else
                ascue_pass_string += "0";
        }
    }

    public String getAscue_pass_string() { return ascue_pass_string; }
    public void setAscue_pass_string(String value) {
        if(!Helper.isNumeric(value)) return;
        ascue_pass_string = value;
        ascue_pass = new byte[] { 0, 0, 0, 0, 0, 0 };
        for (int i = 0; i < 6; i++) {
            if (i < ascue_pass_string.length())
                ascue_pass[i] = Byte.parseByte(ascue_pass_string.substring(i, i + 1));
        }
    }

    public ImpAscueProtocolType getAscue_protocol() { return ascue_protocol; }
    public void setAscue_protocol(ImpAscueProtocolType value) { ascue_protocol = value; }

    public int getMax_Power() { return max_Power; }
    public void setMax_Power(int value) { max_Power = value; }

    public ImpTime getT1_Time_1() { return t1_Time_1; }
    public ImpTime getT3_Time_1() { return t3_Time_1; }
    public ImpTime getT1_Time_2() { return t1_Time_2; }
    public ImpTime getT3_Time_2() { return t3_Time_2; }
    public ImpTime getT2_Time() { return t2_Time; }
    public void setT1_Time_1(ImpTime value) { t1_Time_1 = value; }
    public void setT3_Time_1(ImpTime value) { t3_Time_1 = value; }
    public void setT1_Time_2(ImpTime value) { t1_Time_2 = value; }
    public void setT3_Time_2(ImpTime value) { t3_Time_2 = value; }
    public void setT2_Time(ImpTime value) { t2_Time = value; }


    public DeviceImpParams(ImpNum impNum) {
        num = impNum;
        adrs_PLC = impNum.getCode();
        SetDefaultParams();
    }

    public void SetDefaultParams() {
        setIsEnable_bool(false);
        setA(1600);

        setE_Current(new ImpEnergyGroup(true));
        setE_StartDay(new ImpEnergyGroup(true));

        setPerepoln(ImpOverflowType.Disable);
        setT_qty(ImpNumOfTarifs.One);
        setT1_Time_1(new ImpTime((byte)7, (byte)0));
        setT3_Time_1(new ImpTime((byte)10, (byte)0));
        setT1_Time_2(new ImpTime((byte)17, (byte)0));
        setT3_Time_2(new ImpTime((byte)21, (byte)0));
        setT2_Time(new ImpTime((byte)23, (byte)0));
        setAscue_adrs(0);
        setAscue_pass_string("111111");
        setAscue_protocol(ImpAscueProtocolType.PulsePLC);
        setMax_Power(0);
    }
}

