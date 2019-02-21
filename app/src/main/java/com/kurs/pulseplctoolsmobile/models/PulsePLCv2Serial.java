package com.kurs.pulseplctoolsmobile.models;

import com.kurs.pulseplctoolsmobile.Helper;

//Для упрощения преобразований данных и валидации пароля и серийника
public class PulsePLCv2Serial {
    private byte[] serial_bytes;  //Серийный номер устройства с которым идет общение
    private String serial_string;
    //In Bytes
    public byte[] getBytes() {
        return serial_bytes;
    }
    public void setBytes(byte[] value) {
        if (value.length >= 4)
        {
            serial_bytes = value;
            setSerialStringFromBytes(serial_bytes);
            //Подгоняем длину массива под 4
            setSerialBytesFromString(serial_string);
        }
    }
    //In String
    public String getString() {
        if (serial_string == "00000000") return ""; else return serial_string;
    }
    public void setString(String value) {
        if (value == null || value == "" || value == "0")
        {
            serial_string = "00000000";
            serial_bytes = new byte[4];
            serial_bytes[0] = 0;
            serial_bytes[1] = 0;
            serial_bytes[2] = 0;
            serial_bytes[3] = 0;
            return;
        }
        String tmpStr = value;
        if (tmpStr.length() > 8) tmpStr = tmpStr.substring(0, 8);
        if (Helper.isNumeric(tmpStr))
        {
            if (tmpStr.length() == 8)
            {
                serial_string = tmpStr;
                //Подгоняем длину массива под 4
                setSerialBytesFromString(serial_string);
            }
        }
    }
    //Helpers
    private void setSerialBytesFromString(String str) {
        serial_bytes = new byte[4];
        serial_bytes[0] = Byte.parseByte(str.substring(0, 2));
        serial_bytes[1] = Byte.parseByte(str.substring(2, 4));
        serial_bytes[2] = Byte.parseByte(str.substring(4, 6));
        serial_bytes[3] = Byte.parseByte(str.substring(6, 8));
    }
    private void setSerialStringFromBytes(byte[] bytes) {
        serial_string = String.format("%02d", bytes[0]) +
                String.format("%02d", bytes[1]) +
                String.format("%02d", bytes[2]) +
                String.format("%02d", bytes[3]);
    }

    public PulsePLCv2Serial() {
        setString("0");
    }
    public PulsePLCv2Serial(String serialString) {
        setString(serialString);
    }
    public PulsePLCv2Serial(byte[] serialBytes) {
        setBytes(serialBytes);
    }
}
