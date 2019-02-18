package com.example.pulseplctoolsmobile.models;

import java.nio.charset.StandardCharsets;

public class PulsePLCv2Pass {
    private byte[] pass_bytes;

    public byte[] getBytes() {
        return pass_bytes;
    }
    public void setBytes(byte[] value) {
        pass_bytes = new byte[]
                { (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF };
        for (int i = 0; i < 6; i++) if (i < value.length) pass_bytes[i] = value[i];
    }

    public String getString() {
        String passString = "";
        for (byte b: pass_bytes) {
            if(b != (byte)0xFF)
                passString += new String(new byte[] {b}, StandardCharsets.US_ASCII);
        }
        return  passString;
    }
    public void setString(String value) {
        pass_bytes = new byte[]
                { (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF };
        byte[] bytesFromString = value.getBytes(StandardCharsets.US_ASCII);
        for (int i = 0; i < 6; i++)
            if (i < bytesFromString.length) pass_bytes[i] = bytesFromString[i];
    }

    public PulsePLCv2Pass() {
        setString("");
    }
    public PulsePLCv2Pass(String pass) { setString(pass); }
    public PulsePLCv2Pass(byte[] pass) { setBytes(pass); }
}
