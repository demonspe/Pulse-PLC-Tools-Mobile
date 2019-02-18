package com.example.pulseplctoolsmobile.enums;

public enum ImpNum {
    IMP1(1), IMP2(2);
    byte code;
    ImpNum(int code) {
        this.code = (byte)code;
    }
    public byte getCode() {
        return code;
    }
    public String getName() {
        if(code == 1) return "IMP1";
        if(code == 2) return "IMP2";
        return "";
    }
    public char getChar() {
        if(code == 1) return '1';
        if(code == 2) return '2';
        return ' ';
    }
}
