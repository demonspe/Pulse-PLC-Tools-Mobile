package com.kurs.pulseplctoolsmobile.protocol;

import com.kurs.pulseplctoolsmobile.Helper;

import java.nio.charset.StandardCharsets;

public class MyDataBuffer {

    private byte[] tmp;
    private byte[] buffer;
    private int count;

    public MyDataBuffer(int bufferSize)
    {
        buffer = new byte[bufferSize];
        count = 0;
    }

    public void add(byte b)
    {
        buffer[count] = b;
        count++;
    }
    public void add(int data) {
        add((byte)(data));
    }
    public void add(int data, int count_bytes) {
        if(count_bytes == 2) {
            add((byte)(data >> 8));
            add((byte)(data >> 0));
        }
        if(count_bytes == 3) {
            add((byte)(data >> 16));
            add((byte)(data >> 8));
            add((byte)(data >> 0));
        }
        if(count_bytes == 4) {
            add((byte)(data >> 24));
            add((byte)(data >> 16));
            add((byte)(data >> 8));
            add((byte)(data >> 0));
        }
    }
    public void add(long data, int count_bytes) {
        if(count_bytes == 2) {
            add((byte)(data >> 8));
            add((byte)(data >> 0));
        }
        if(count_bytes == 3) {
            add((byte)(data >> 16));
            add((byte)(data >> 8));
            add((byte)(data >> 0));
        }
        if(count_bytes == 4) {
            add((byte)(data >> 24));
            add((byte)(data >> 16));
            add((byte)(data >> 8));
            add((byte)(data >> 0));
        }
    }
    public void add(byte[] bytes) {
        for (byte b: bytes) { add(b); }
    }
    public void add(byte[] bytes, int count_bytes) {
        if(bytes.length < count_bytes) return;
        for(int i = 0; i < count_bytes; i++) {
            add(bytes[i]);
        }
    }
    public void add(String str) {
        add(str.getBytes(StandardCharsets.US_ASCII));
    }

    public void clear()
    {
        count = 0;
    }

    public byte[] getBytes()
    {
        tmp = new byte[count];
        for(int i = 0; i < count; i++)
        {

            tmp[i] = (byte)buffer[i];
        }
        return tmp;
    }

    public int getCount() {
        return count;
    }

    //Получить массив данных с контрольной суммой в конце
    public byte[] getBytesCRC16()
    {
        tmp = new byte[count+2];
        for(int i = 0; i < count; i++)
        {
            tmp[i] = (byte) buffer[i];
        }

        //Add crc16 bytes
        CRC16Modbus crc16Modbus = new CRC16Modbus();
        for (int i = 0; i < count; i ++) {
            crc16Modbus.update(tmp[i]);
        }
        tmp[count] = (byte) ((crc16Modbus.getValue() & 0x000000ff));
        tmp[count+1] = (byte) ((crc16Modbus.getValue() & 0x0000ff00) >>> 8);
        return tmp;
    }

    public boolean CheckCRC16()
    {
        CRC16Modbus crc16Modbus = new CRC16Modbus();
        crc16Modbus.reset();
        byte[] tmp = getBytes();
        crc16Modbus.update(tmp,0,tmp.length);
        return crc16Modbus.getValue() == 0;
    }

    public String getBytesStringUInt()
    {
        getBytesCRC16();
        return Helper.toStringUInt(tmp, ", ");
    }
    public String getBytesStringHEX()
    {
        getBytesCRC16();
        return Helper.toStringHEX(tmp, ", ");
    }
}
