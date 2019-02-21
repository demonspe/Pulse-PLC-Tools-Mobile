package com.kurs.pulseplctoolsmobile.enums;

public enum ImpNumOfTarifs {
    One(1),
    Two(2),
    Three(3);
    byte code;
    ImpNumOfTarifs(int code) {
        this.code = (byte)code;
    }
    public byte getCode() {
        return code;
    }
    public static ImpNumOfTarifs getFromByte(byte b) {
        if(b == 1) return One;
        if(b == 2) return Two;
        if(b == 3) return Three;
        return One;
    }
}
