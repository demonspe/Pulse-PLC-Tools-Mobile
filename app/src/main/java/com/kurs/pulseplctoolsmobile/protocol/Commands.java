package com.kurs.pulseplctoolsmobile.protocol;

//Комманды посылаемые на устройство
public enum Commands {
    None,
    Check_Pass,
    Close_Session,
    EEPROM_Burn,
    Reboot,
    Clear_Errors,
    Search_Devices,
    Read_Journal,
    Read_DateTime,
    Write_DateTime,
    Read_Main_Params,
    Write_Main_Params,
    Read_IMP,
    Read_IMP_extra,
    Write_IMP,
    Read_PLC_Table,
    Read_PLC_Table_En,
    Write_PLC_Table,
    Read_E_Current,
    Read_E_Start_Day,
    Read_E_Month,
    Request_PLC,
    EEPROM_Read_Byte,
    Pass_Write,
    SerialWrite,
    Bootloader
}
