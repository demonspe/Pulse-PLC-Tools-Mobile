package com.example.pulseplctoolsmobile.enums;

public enum ImpOverflowType {
    Disable(0) ,
    Overflow_5_Digits(5),
    Overflow_6_Digits(6);
    byte code;
    ImpOverflowType(int code) {
        this.code = (byte)code;
    }
    public byte getCode() {
        return code;
    }
    public static ImpOverflowType getFromByte(byte b) {
        if(b == 0) return Disable;
        if(b == 5) return Overflow_5_Digits;
        if(b == 6) return Overflow_6_Digits;
        return Disable;
    }
}
