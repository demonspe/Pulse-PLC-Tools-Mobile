package com.kurs.pulseplctoolsmobile.enums;

public enum ImpAscueProtocolType {
    PulsePLC(0),
    Mercury230ART(1);
    byte code;
    ImpAscueProtocolType(int code) {
        this.code = (byte)code;
    }
    public byte getCode() {
        return code;
    }
    public static ImpAscueProtocolType getFromByte(byte b) {
        if(b == 0) return PulsePLC;
        if(b == 1) return Mercury230ART;
        return PulsePLC;
    }
}
