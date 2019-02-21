package com.kurs.pulseplctoolsmobile;

public class Helper {
    public static int toUInt(byte b)
    {
        int k = b;
        if(k<0) k += 256;
        return k;
    }

    public static String toStringUInt(byte [] data, String splitter)
    {
        String str = "";
        for (byte b: data)
        {
            str += Helper.toUInt(b) + splitter;
        }
        return str;
    }

    public static String toStringHEX(byte [] data, String splitter)
    {
        String str = "";
        for (byte b: data)
        {
            str += "0x"+ String.format("%02X", b) + splitter;
            //str += "0x"+ Integer.toHexString(Helper.toUInt(b)) + splitter;
        }
        return str;
    }

    public static long bytesToLong(byte[] bytes, int count_bytes) {
        long ret = 0;
        for (int i=0; i < count_bytes; i++) {
            ret <<= 8;
            ret |= (int)bytes[i] & 0xFF;
        }
        return ret;
    }

    public static boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }
}
