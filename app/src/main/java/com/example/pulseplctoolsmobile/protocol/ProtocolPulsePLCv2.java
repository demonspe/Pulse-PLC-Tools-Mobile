package com.example.pulseplctoolsmobile.protocol;

import com.example.pulseplctoolsmobile.Helper;
import com.example.pulseplctoolsmobile.OnMessageListener;
import com.example.pulseplctoolsmobile.enums.BatteryMode;
import com.example.pulseplctoolsmobile.enums.ImpAscueProtocolType;
import com.example.pulseplctoolsmobile.enums.ImpNum;
import com.example.pulseplctoolsmobile.enums.ImpNumOfTarifs;
import com.example.pulseplctoolsmobile.enums.ImpOverflowType;
import com.example.pulseplctoolsmobile.enums.InterfaceMode;
import com.example.pulseplctoolsmobile.enums.WorkMode;
import com.example.pulseplctoolsmobile.link.LinkManager;
import com.example.pulseplctoolsmobile.models.DeviceImpExParams;
import com.example.pulseplctoolsmobile.models.DeviceImpParams;
import com.example.pulseplctoolsmobile.models.DeviceMainParams;
import com.example.pulseplctoolsmobile.models.ImpEnergyGroup;
import com.example.pulseplctoolsmobile.models.ImpEnergyValue;
import com.example.pulseplctoolsmobile.models.ImpTime;
import com.example.pulseplctoolsmobile.models.PulsePLCv2LoginPass;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ProtocolPulsePLCv2 {
    //Data
    private MyDataBuffer Rx_Bytes;
    private MyDataBuffer Tx_Data;
    private CRC16Modbus crc16;
    private ProtocolDataContainer DataContainer;
    private MyDataBuffer InputData;

    //Выполняемая сейчас команда
    private Commands CurrentCommand;
    //Доступ к выполнению команд на устройстве
    private AccessType Access;
    //Link
    private LinkManager CurrentLink;

    //region Таймеры
    Timer TimerTimeout;
    Timer TimerAccess;

    private void TimerTimeoutStart(int timeOutMs) {
        if(TimerTimeout != null) TimerTimeout.cancel();
        TimerTimeout = new Timer();
        TimerTimeout.schedule(new TimerTask() {
            @Override
            public void run() {
                    //Если ожидали данных но не дождались
                    if (CurrentCommand != Commands.None)
                    {
                        if (CurrentCommand == Commands.Close_Session) { RequestEnd(true); return; }
                        if (Rx_Bytes.CheckCRC16() || Rx_Bytes.getCount() == 0)
                            Message("Истекло время ожидания ответа");
                        else
                        {
                            //Отправим в Log окно
                            //Message(this, new MessageDataEventArgs() { Data = Rx_Bytes, Length = Rx_Bytes.Length, MessageType = MessageType.ReceiveBytes });
                            Message("Неверная контрольная сумма" );
                        }
                        //Комманда не выполнилась
                        RequestEnd(false);
                    }
            }
        }, timeOutMs+1000);
    }
    private void TimerTimeoutStop() {
        if(TimerTimeout != null) TimerTimeout.cancel();
    }
    private void TimerAccessStart() {
        if(TimerTimeout != null) TimerTimeout.cancel();
        TimerTimeout = new Timer();
        //Запускаем таймер на 30 сек
        TimerTimeout.schedule(new TimerTask() {
            @Override
            public void run() {
                Access = AccessType.No_Access;
                TimerAccessStop();
            }
        }, 30000);
    }
    private void TimerAccessStop() {
        if(TimerAccess != null) TimerAccess.cancel();
        if(onProtocolEvent != null) onProtocolEvent.onAccessEnd();
    }
    //endregion

    //Events
    //Message
    private OnMessageListener onMessageListener;
    public void setOnMessageListener(OnMessageListener onMessageListener) {
        this.onMessageListener = onMessageListener;
    }
    public void Message(String text) {
        if(onMessageListener != null) onMessageListener.onMessageShow(text);
    }
    //Command end
    private OnProtocolEvent onProtocolEvent;
    public  void setOnProtocolEvent(OnProtocolEvent onProtocolEvent) {
        this.onProtocolEvent = onProtocolEvent;
    }
    private void CommandEnd(ProtocolDataContainer dataContainer) {
        if(onProtocolEvent != null)
            onProtocolEvent.onCommandEnd(dataContainer);
    }

    //Свойства комманд
    class CommandProperties {
        public String Code;
        public int Timeout;
        public int MinLength;

        public CommandProperties(String code, int minLength, int timeOut) {
            Code = code;
            Timeout = timeOut;
            MinLength = minLength;
        }
    }
    //Словарь параметров комманд
    private Map<Commands, CommandProperties> commandProps;
    //Коды результата обработки пакета
    public enum HandleResult {
        Ok,
        Continue,
        Error
    }

    public ProtocolPulsePLCv2(LinkManager link) {
        CurrentLink = link;
        //Data
        Tx_Data = new MyDataBuffer(1024);
        Rx_Bytes =  new MyDataBuffer(1024);
        crc16 = new CRC16Modbus();
        //

        //Настроим параметры команд
        InitCommandProperties();
    }
    //Иниц команд
    void InitCommandProperties() {
        commandProps = new HashMap<>();
        //---Заполним коды команд---
        //Доступ
        commandProps.put(Commands.Check_Pass,       new CommandProperties("Ap", 0, 100));
        commandProps.put(Commands.Close_Session,    new CommandProperties("Ac", 0, 200));
        //Системные
        commandProps.put(Commands.Bootloader,       new CommandProperties("Su", 0, 100));
        commandProps.put(Commands.SerialWrite,      new CommandProperties("Ss", 0, 200));
        commandProps.put(Commands.EEPROM_Read_Byte, new CommandProperties("Se", 0, 100));
        commandProps.put(Commands.EEPROM_Burn,      new CommandProperties("Sb", 0, 100));
        commandProps.put(Commands.Clear_Errors,     new CommandProperties("Sc", 0, 100));
        commandProps.put(Commands.Reboot,           new CommandProperties("Sr", 0, 100));
        commandProps.put(Commands.Request_PLC,      new CommandProperties("SR", 0, 60000));
        //Чтение
        commandProps.put(Commands.Search_Devices,   new CommandProperties("RL", 0, 500));
        commandProps.put(Commands.Read_Journal,     new CommandProperties("RJ", 0, 200));
        commandProps.put(Commands.Read_DateTime,    new CommandProperties("RT", 0, 100));
        commandProps.put(Commands.Read_Main_Params, new CommandProperties("RM", 0, 100));
        commandProps.put(Commands.Read_IMP,         new CommandProperties("RI", 8, 100));
        commandProps.put(Commands.Read_IMP_extra,   new CommandProperties("Ri", 0, 100));
        commandProps.put(Commands.Read_PLC_Table,   new CommandProperties("RP", 0, 200));
        commandProps.put(Commands.Read_PLC_Table_En,new CommandProperties("RP", 0, 200));
        commandProps.put(Commands.Read_E_Current,   new CommandProperties("REc", 28, 100));
        commandProps.put(Commands.Read_E_Start_Day, new CommandProperties("REd", 28, 100));
        commandProps.put(Commands.Read_E_Month,     new CommandProperties("REm", 28, 100));
        //Запись
        commandProps.put(Commands.Write_DateTime,   new CommandProperties("WT", 0, 500));
        commandProps.put(Commands.Write_Main_Params,new CommandProperties("WM", 0, 500));
        commandProps.put(Commands.Write_IMP,        new CommandProperties("WI", 0, 500));
        commandProps.put(Commands.Write_PLC_Table,  new CommandProperties("WP", 0, 2000));
        commandProps.put(Commands.Pass_Write,       new CommandProperties("Wp", 0, 500));
        //-------------
    }

    //Отмена выполнения комманд, таймеров, сброс статусов
    public void cancel() {
        //if(TimerAccess != null) TimerAccess.cancel();
        if(TimerTimeout != null) TimerTimeout.cancel();
        CurrentCommand = Commands.None;
        Rx_Bytes.clear();
    }
    //Отправить команду
    public boolean Send(Commands cmdCode, Object param) {
        if (CurrentLink == null) return false;
        if (!CurrentLink.getIsConnected()) return false;
        //Установим команду
        CurrentCommand = cmdCode;

        if (CurrentCommand == Commands.Check_Pass)     return CMD_Check_Pass((PulsePLCv2LoginPass)param);
        //if (CurrentCommand == Commands.Close_Session)  return CMD_Close_Session();
        if (CurrentCommand == Commands.Search_Devices) return CMD_Search_Devices();
        //Доступ - Чтение
        if (Access != AccessType.Read && Access != AccessType.Write) {
            Message("Нет доступа к данным устройства. Сначала авторизуйтесь." );
            return false;
        }
        //if (CurrentCommand == Commands.Read_Journal)        return CMD_Read_Journal((Journal_type)param);
        //if (CurrentCommand == Commands.Read_DateTime)       return CMD_Read_DateTime();
        if (CurrentCommand == Commands.Read_Main_Params)    return CMD_Read_Main_Params();
        if (CurrentCommand == Commands.Read_IMP)            return CMD_Read_Imp_Params((ImpNum)param);
        if (CurrentCommand == Commands.Read_IMP_extra)      return CMD_Read_Imp_Extra_Params((ImpNum)param);
        //if (CurrentCommand == Commands.Read_PLC_Table_En)   return CMD_Read_PLC_Table(new List<DataGridRow_PLC>());
        //if (CurrentCommand == Commands.Read_PLC_Table)      return CMD_Read_PLC_Table((List<DataGridRow_PLC>)param);
        //if (CurrentCommand == Commands.Read_E_Current)      return CMD_Read_E_Current((byte)param);
        //if (CurrentCommand == Commands.Read_E_Start_Day)    return CMD_Read_E_Start_Day((byte)param);
        //Доступ - Запись
        if (Access != AccessType.Write) {
            Message("Нет доступа к записи параметров на устройство.");
            return false;
        }
        //if (CurrentCommand == Commands.Bootloader)         return CMD_BOOTLOADER();
        //if (CurrentCommand == Commands.SerialWrite)        return CMD_SerialWrite((PulsePLCv2Serial)param);
        //if (CurrentCommand == Commands.Pass_Write)         return CMD_Pass_Write((DeviceMainParams)param);
        //if (CurrentCommand == Commands.EEPROM_Burn)        return CMD_EEPROM_BURN();
        //if (CurrentCommand == Commands.EEPROM_Read_Byte)   return CMD_EEPROM_Read_Byte((UInt16)param);
        if (CurrentCommand == Commands.Reboot)             return CMD_Reboot();
        //if (CurrentCommand == Commands.Clear_Errors)       return CMD_Clear_Errors();
        //if (CurrentCommand == Commands.Write_DateTime)     return CMD_Write_DateTime((DateTime)param);
        if (CurrentCommand == Commands.Write_Main_Params)  return CMD_Write_Main_Params((DeviceMainParams)param);
        if (CurrentCommand == Commands.Write_IMP)          return CMD_Write_Imp_Params((DeviceImpParams)param);
        //if (CurrentCommand == Commands.Write_PLC_Table)    return CMD_Write_PLC_Table((List<DataGridRow_PLC>)param);
        //if (CurrentCommand == Commands.Request_PLC)        return CMD_Request_PLC((PLCRequestParamsForProtocol)param);
        return false;
    }
    //Обработчик получения данных из BT канала
    public void DataReceived(byte[] inData) {
        //Забираем данные
        for (int i = 0; i < inData.length; i++) {
            //Добавляет по одному байту из приходящего потока байт в пакет и проверяем на корректность
            Rx_Bytes.add(inData[i]);

            boolean crc = Rx_Bytes.CheckCRC16();
            crc = false;
            int minl = commandProps.get(CurrentCommand).MinLength;
            boolean l = Rx_Bytes.getCount() >= commandProps.get(CurrentCommand).MinLength;
            l = false;
            //Проверяем CRC16
            if (Rx_Bytes.CheckCRC16() && Rx_Bytes.getCount() >= commandProps.get(CurrentCommand).MinLength)
            {
                //Если сформирован корректный пакет то отправляем на обработку
                HandleResult handle_code = Handle(Rx_Bytes.getBytes(), Rx_Bytes.getCount());

                //Комманда выполнена успешно
                if (handle_code == HandleResult.Ok) {
                    RequestEnd(true);
                    //Обновляем таймер доступа
                    // (в устройстве он обновляется при получении команды по интерфейсу)
                    TimerAccessStart();
                    continue;
                }

                //Получилось обработать и ждем следующую часть сообщения
                if (handle_code == HandleResult.Continue) {
                    Rx_Bytes.clear();
                    continue;
                }

                //Не верный формат сообщения
                if (handle_code == HandleResult.Error) {
                    Message("Неверный формат ответа" );
                    RequestEnd(false);
                }
            }
        }
    }
    //Проверка соответствия получаемых данных и отпрвленной команды
    boolean Check(byte commandName, Commands checkCommand) {
        //Проверим ждем ли ответ на эту команду
        if (CurrentCommand != checkCommand) return false;
        //Если ждем ответ то проверяем символ - код команды в ответе
        String cmdCode = commandProps.get(checkCommand).Code;
        String cmdName = cmdCode.substring(cmdCode.length()-1);
        String cmdNameFromDevice = new String(new byte[] {commandName}, StandardCharsets.US_ASCII);
        if (cmdName.equals(cmdNameFromDevice))
            return true;
        return false;
    }

    HandleResult Handle(byte[] rxBytes, int length) {
        if(rxBytes[0] == 0 &&  rxBytes[1] == 'P' && rxBytes[2] == 'l' && rxBytes[3] == 's' )
        {
            byte CMD_Type = rxBytes[4];
            byte CMD_Name = rxBytes[5];

            //Доступ
            if (CMD_Type == 'A')
            {
                if (Check(CMD_Name, Commands.Check_Pass)) { CMD_Check_Pass(rxBytes);    return HandleResult.Ok; }
            }
            //Системные команды
            if (CMD_Type == 'S')
            {
                //if (Check(CMD_Name, Commands.Bootloader))       { CMD_BOOTLOADER(rxBytes);          return HandleResult.Ok; }
                //if (Check(CMD_Name, Commands.SerialWrite))      { CMD_SerialWrite(rxBytes);         return HandleResult.Ok; }
                if (Check(CMD_Name, Commands.Reboot))           { CMD_Reboot(rxBytes);              return HandleResult.Ok; }
                //if (Check(CMD_Name, Commands.EEPROM_Burn))      { CMD_EEPROM_BURN(rxBytes);         return HandleResult.Ok; }
                //if (Check(CMD_Name, Commands.EEPROM_Read_Byte)) { CMD_EEPROM_Read_Byte(rxBytes);    return HandleResult.Ok; }
                //if (Check(CMD_Name, Commands.Clear_Errors))     { CMD_Clear_Errors(rxBytes);        return HandleResult.Ok; }
                //if (Check(CMD_Name, Commands.Request_PLC))      { CMD_Request_PLC(rxBytes);         return HandleResult.Ok; }
            }
            //Команды чтения
            if (CMD_Type == 'R')
            {
                if (Check(CMD_Name, Commands.Search_Devices))   { CMD_Search_Devices(rxBytes);     return HandleResult.Ok; }
                //if (Check(CMD_Name, Commands.Read_Journal))     { CMD_Read_Journal(rxBytes);        return HandleResult.Ok; }
                //if (Check(CMD_Name, Commands.Read_DateTime))    { CMD_Read_DateTime(rxBytes);       return HandleResult.Ok; }
                if (Check(CMD_Name, Commands.Read_Main_Params)) { CMD_Read_Main_Params(rxBytes);    return HandleResult.Ok; }
                if (Check(CMD_Name, Commands.Read_IMP))         { CMD_Read_Imp_Params(rxBytes);     return HandleResult.Ok; }
                if (Check(CMD_Name, Commands.Read_IMP_extra))   { CMD_Read_Imp_Extra_Params(rxBytes); return HandleResult.Ok; }
                //if (CMD_Name == 'P' && (CurrentCommand == Commands.Read_PLC_Table ||
                //        CurrentCommand == Commands.Read_PLC_Table_En)) { CMD_Read_PLC_Table(rxBytes); return HandleResult.Ok; }
                //if (CMD_Name == 'E')
                //{
                //    if (Check(rxBytes[6], Commands.Read_E_Current)) { CMD_Read_E_Handle(rxBytes);      return HandleResult.Ok; }
                //    if (Check(rxBytes[6], Commands.Read_E_Start_Day)) { CMD_Read_E_Handle(rxBytes);    return HandleResult.Ok; }
                //}
            }
            //Команды записи
            if (CMD_Type == 'W')
            {
                //if (Check(CMD_Name, Commands.Pass_Write))       { CMD_Pass_Write(rxBytes);          return HandleResult.Ok; }
                //if (Check(CMD_Name, Commands.Write_DateTime))   { CMD_Write_DateTime(rxBytes);      return HandleResult.Ok; }
                if (Check(CMD_Name, Commands.Write_Main_Params)) { CMD_Write_Main_Params(rxBytes);  return HandleResult.Ok; }
                if (Check(CMD_Name, Commands.Write_IMP))        { CMD_Write_Imp_Params(rxBytes);    return HandleResult.Ok; }
                //if (Check(CMD_Name, Commands.Write_PLC_Table))  { CMD_Write_PLC_Table(rxBytes);     return HandleResult.Ok; }
            }
        }
        return HandleResult.Error;
    }

    //Выставить флаги о том что запрос отправлен
    private boolean Request_Start() { return Request_Start(null); }
    private boolean Request_Start(Object prepareData) {
        //Очищаем буффер приема
        Rx_Bytes.clear();

        //Запускаем таймер ожидания ответа
        TimerTimeoutStart(commandProps.get(CurrentCommand).Timeout);

        //Подготовим данные для передачи во View
        DataContainer = new ProtocolDataContainer(false, CurrentCommand, prepareData);

        //Пробуем отправить данные
        return CurrentLink.sendData(Tx_Data.getBytesCRC16());
    }

    //Сбросить флаги ожидания ответа на запрос
    private void RequestEnd(boolean status) {
        Rx_Bytes.clear();
        TimerTimeoutStop();
        if(DataContainer != null)
            DataContainer.Status = status;
        CommandEnd(DataContainer);
        CurrentCommand = Commands.None;
    }

    //Работа с буффером отправки
    private void Start_Add_Tx(Commands cmd) {
        Tx_Data.clear(); //Очистим буфер передачи
        Tx_Data.add((byte)0x00);  //Добавим первый байт сообщение
        Tx_Data.add("Pls" + commandProps.get(cmd).Code); //Добавим остальные байты начала сообщения
    }

    //region СЕРВИСНЫЕ КОМАНДЫ
    //Запрос ПЕРЕЗАГРУЗКА
    private boolean CMD_Reboot() {
        Start_Add_Tx(Commands.Reboot);
        Message("Перезагрузить" );
        return Request_Start();
    }
    //Обработка запроса
    private void CMD_Reboot(byte[] rxBytes) {
        Message("Устройство перезагружается..");
    }
    //endregion

    //region КАНАЛ (Поиск, пароль, закрытие сессии)
    //Запрос ПРОВЕРКА ПАРОЛЯ
    private boolean CMD_Check_Pass(PulsePLCv2LoginPass param) {
        Start_Add_Tx(Commands.Check_Pass);
        //Серийник
        byte[] serial = param.getSerial().getBytes();
        Tx_Data.add(serial);
        //Пароль
        byte[] pass_ = param.getPass().getBytes();
        Tx_Data.add(pass_);
        Message("Авторизация: [" +
                param.getSerial().getString() + "] [" +
                param.getPass().getString() + "]");
        //Отправляем запрос
        return Request_Start();
    }
    //Обработка запроса
    private void CMD_Check_Pass(byte[] rxBytes) {
        String accessStr = "_";
        String service_mode = rxBytes[6] == 1 ? "[Service mode]" : "";
        if (rxBytes[7] == 's') { accessStr = "Нет доступа "; Access = AccessType.Write; }
        if (rxBytes[7] == 'r') { accessStr = "Чтение "; Access = AccessType.Read; }
        if (rxBytes[7] == 'w') { accessStr = "Запись "; Access = AccessType.Write; }
        DataContainer.Data = Access;
        //Событие о получении доступа на чтение или запись
        if(onProtocolEvent != null) onProtocolEvent.onAccessGranted(Access);
        Message("Доступ открыт: " + accessStr + service_mode);
        TimerAccessStart();
    }

    //Запрос ЗАКРЫТИЕ СЕССИИ (закрывает доступ к данным)
    private boolean CMD_Close_Session() {
        //Первые байты по протоколу конфигурации
        Start_Add_Tx(Commands.Close_Session);
        Access = AccessType.No_Access;
        Message("Закрыть сессию");
        return Request_Start();
    }

    //Запрос ПОИСК УСТРОЙСТВ в канале (и режима работы)
    private boolean CMD_Search_Devices() {
        Start_Add_Tx(Commands.Search_Devices);
        Message("Поиск устройств");
        return Request_Start("");
    }
    //Обработка ответа
    private void CMD_Search_Devices(byte[] rxBytes) {
        if (rxBytes.length < 11) return;
        int mode = rxBytes[6];
        String mode_ = "";
        if (mode == 0) mode_ = " [Счетчик]";
        if (mode == 1) mode_ = " [Фаза А]";
        if (mode == 2) mode_ = " [Фаза B]";
        if (mode == 3) mode_ = " [Фаза C]";
        String serial_num =
                String.format("%02d", rxBytes[7]) +
                String.format("%02d", rxBytes[8]) +
                String.format("%02d", rxBytes[9]) +
                String.format("%02d", rxBytes[10]);
        DataContainer.Data = serial_num;
        Message("Ответил " + serial_num + mode_);
    }
    //endregion

    //region ОСНОВНЫЕ ПАРАМЕТРЫ (режимы работы, ошибки, пароли)
    private boolean CMD_Read_Main_Params() {
        Start_Add_Tx(Commands.Read_Main_Params);
        Message("Чтение основных параметров");
        return Request_Start();
    }
    //Обработка ответа
    private void CMD_Read_Main_Params(byte[] rxBytes) {
        DeviceMainParams device = new DeviceMainParams();
        //Версия прошивки
        device.setVersionFirmware("v2." + rxBytes[7] + "." + rxBytes[8]);
        device.setVersionEEPROM("v1." + rxBytes[6]);
        //Параметры
        device.setWorkMode(WorkMode.values()[rxBytes[9]]);
        device.setBatteryMode(BatteryMode.values()[rxBytes[10]]);
        device.setRS485_WorkMode(InterfaceMode.values()[rxBytes[11]]);
        device.setBluetooth_WorkMode(InterfaceMode.values()[rxBytes[12]]);
        //Ошибки
        device.setErrorsByte(rxBytes[13]);
        //Передаем данные
        DataContainer.Data = device;
        Message("Основные параметры успешно прочитаны");
    }

    //Запрос - ЗАПИСЬ ОСНОВНЫХ ПАРАМЕТРОВ
    private boolean CMD_Write_Main_Params(DeviceMainParams device) {
        Start_Add_Tx(Commands.Write_Main_Params);
        //Параметры
        Tx_Data.add((byte)device.getWorkMode().ordinal());
        Tx_Data.add((byte)device.getBatteryMode().ordinal());
        Tx_Data.add((byte)device.getRS485_WorkMode().ordinal());
        Tx_Data.add((byte)device.getBluetooth_WorkMode().ordinal());
        Message("Запись основных параметров");
        return Request_Start();
    }
    //Обработка ответа
    private void CMD_Write_Main_Params(byte[] rxBytes) {
        if (rxBytes[6] == 'O' && rxBytes[7] == 'K') Message("Основные параметры успешно записаны");
        if (rxBytes[6] == 'e' && rxBytes[7] == 'r') Message("Ошибка при записи.");
    }
    //endregion

    //region ИМУЛЬСНЫЕ ВХОДЫ
    //Запрос ЧТЕНИЕ ПАРАМЕТРОВ ИМПУЛЬСНЫХ ВХОДОВ
    private boolean CMD_Read_Imp_Params(ImpNum imp_num) {
        Start_Add_Tx(Commands.Read_IMP);
        Tx_Data.add((byte)imp_num.getChar());
        Message("Чтение настроек " + imp_num);
        return Request_Start();
    }
    //Обработка ответа
    private void CMD_Read_Imp_Params(byte[] rxBytes) {
        DeviceImpParams Imp = new DeviceImpParams(ImpNum.IMP1);
        if (rxBytes[6] != '1' && rxBytes[6] != '2') return;
        if(rxBytes[6] == '2') Imp.setNum(ImpNum.IMP2);
        int pntr = 7;
        Imp.setIsEnable(rxBytes[pntr++]);
        if (Imp.getIsEnable_bool())
        {
            Imp.setAdrs_PLC(rxBytes[pntr++]);
            //Тип протокола
            Imp.setAscue_protocol(ImpAscueProtocolType.getFromByte(rxBytes[pntr++]));
            //Адрес аскуэ
            Imp.setAscue_adrs((int) Helper.bytesToLong(new byte[] {rxBytes[pntr++], rxBytes[pntr++]}, 2));
            //Пароль для аскуэ (6)
            Imp.setAscue_pass(new byte[] { rxBytes[pntr++], rxBytes[pntr++], rxBytes[pntr++], rxBytes[pntr++], rxBytes[pntr++], rxBytes[pntr++] });
            //Эмуляция переполнения (1)
            Imp.setPerepoln(ImpOverflowType.getFromByte(rxBytes[pntr++]));
            //Передаточное число (2)
            Imp.setA((int)Helper.bytesToLong(new byte[] {rxBytes[pntr++], rxBytes[pntr++]}, 2));
            //Тарифы (11)
            Imp.setT_qty(ImpNumOfTarifs.getFromByte(rxBytes[pntr++]));
            Imp.setT1_Time_1(new ImpTime(rxBytes[pntr++], rxBytes[pntr++]));
            Imp.setT3_Time_1(new ImpTime(rxBytes[pntr++], rxBytes[pntr++]));
            Imp.setT1_Time_2(new ImpTime(rxBytes[pntr++], rxBytes[pntr++]));
            Imp.setT3_Time_2(new ImpTime(rxBytes[pntr++], rxBytes[pntr++]));
            Imp.setT2_Time(new ImpTime(rxBytes[pntr++], rxBytes[pntr++]));

            //Показания - Текущие (12)
            ImpEnergyGroup e_cur = new ImpEnergyGroup(true);
            //T1
            byte[] e_bytes = new byte[] { rxBytes[pntr++], rxBytes[pntr++], rxBytes[pntr++], rxBytes[pntr++] };
            long E = Helper.bytesToLong(e_bytes, 4);
            e_cur.setE_T1(new ImpEnergyValue(E));
            //T2
            e_bytes = new byte[] { rxBytes[pntr++], rxBytes[pntr++], rxBytes[pntr++], rxBytes[pntr++] };
            E = Helper.bytesToLong(e_bytes, 4);
            e_cur.setE_T2(new ImpEnergyValue(E));
            //T3
            e_bytes = new byte[] { rxBytes[pntr++], rxBytes[pntr++], rxBytes[pntr++], rxBytes[pntr++] };
            E = Helper.bytesToLong(e_bytes, 4);
            e_cur.setE_T3(new ImpEnergyValue(E));
            Imp.setE_Current(e_cur);

            //Показания - На начало суток (12)
            ImpEnergyGroup e_start = new ImpEnergyGroup(true);
            //T1
            e_bytes = new byte[] { rxBytes[pntr++], rxBytes[pntr++], rxBytes[pntr++], rxBytes[pntr++] };
            E = Helper.bytesToLong(e_bytes, 4);
            e_start.setE_T1(new ImpEnergyValue(E));
            //T2
            e_bytes = new byte[] { rxBytes[pntr++], rxBytes[pntr++], rxBytes[pntr++], rxBytes[pntr++] };
            E = Helper.bytesToLong(e_bytes, 4);
            e_start.setE_T2(new ImpEnergyValue(E));
            //T3
            e_bytes = new byte[] { rxBytes[pntr++], rxBytes[pntr++], rxBytes[pntr++], rxBytes[pntr++] };
            E = Helper.bytesToLong(e_bytes, 4);
            e_start.setE_T3(new ImpEnergyValue(E));
            Imp.setE_Current(e_start);

            //Максимальная мощность
            Imp.setMax_Power((int)Helper.bytesToLong(new byte[] { rxBytes[pntr++], rxBytes[pntr++] }, 2));

            //Резервные параметры (на будущее)
            //4 байта
            byte reserv_ = rxBytes[pntr++];
            reserv_ = rxBytes[pntr++];
            reserv_ = rxBytes[pntr++];
            reserv_ = rxBytes[pntr++];
        }
        DataContainer.Data = Imp;
        Message("Параметры " + Imp.getNum().getName() + " успешно прочитаны");
    }


    //Запрос ЧТЕНИЕ ПАРАМЕТРОВ ИМПУЛЬСНЫХ ВХОДОВ
    private boolean CMD_Read_Imp_Extra_Params(ImpNum imp_num)
    {
        Start_Add_Tx(Commands.Read_IMP_extra);
        byte imp_ = 0;
        if (imp_num == ImpNum.IMP1) imp_ = '1';
        if (imp_num == ImpNum.IMP2) imp_ = '2';
        //Параметры
        Tx_Data.add(imp_);
        //Отправляем запрос
        //Message("Чтение состояния " + imp_num);
        return Request_Start();
    }
    //Обработка ответа
    private void CMD_Read_Imp_Extra_Params(byte[] rxBytes)
    {
        DeviceImpExParams ImpEx = new DeviceImpExParams(ImpNum.IMP1);
        if (rxBytes[6] != '1' && rxBytes[6] != '2') return;
        if (rxBytes[6] == '2') ImpEx.setNum(ImpNum.IMP2);
        int pntr = 7;
        ImpEx.setCurrentTarif(rxBytes[pntr++]);
        ImpEx.setImpCounter((int)Helper.bytesToLong(new byte[] { rxBytes[pntr++], rxBytes[pntr++] },2));
        ImpEx.setTimeMsFromLastImp((int)Helper.bytesToLong(new byte[] { rxBytes[pntr++], rxBytes[pntr++], rxBytes[pntr++], rxBytes[pntr++] },4));
        ImpEx.setCurrentPower((int)Helper.bytesToLong(new byte[] { rxBytes[pntr++], rxBytes[pntr++], rxBytes[pntr++], rxBytes[pntr++] },4));
        //Отобразим
        DataContainer.Data = ImpEx;
        //Message("Мгновенные значения " + ImpEx.getNum().getName() + " считаны");
    }

    //Запрос ЗАПИСЬ ПАРАМЕТРОВ ИМПУЛЬСНЫХ ВХОДОВ
    private boolean CMD_Write_Imp_Params(DeviceImpParams imp) {
        Start_Add_Tx(Commands.Write_IMP);
        if (imp.getNum() == ImpNum.IMP1) Tx_Data.add("1");
        else if (imp.getNum() == ImpNum.IMP2) Tx_Data.add("2");
        else return false;
        DeviceImpParams Imp_ = imp;

        //Параметры
        Tx_Data.add(Imp_.getIsEnable());
        if (Imp_.getIsEnable_bool()) {
            //
            Tx_Data.add(Imp_.getAdrs_PLC());
            //Тип протокола
            Tx_Data.add(Imp_.getAscue_protocol().getCode());
            //Адрес аскуэ
            Tx_Data.add(Imp_.getAscue_adrs(), 2);
            //Пароль для аскуэ (6)
            Tx_Data.add(Imp_.getAscue_pass(), 6);
            //Эмуляция переполнения (1)
            Tx_Data.add(Imp_.getPerepoln().getCode());
            //Передаточное число (2)
            Tx_Data.add(Imp_.getA(), 2);
            //Тарифы (11)
            Tx_Data.add(Imp_.getT_qty().getCode());
            Tx_Data.add(Imp_.getT1_Time_1().getHours());
            Tx_Data.add(Imp_.getT1_Time_1().getMinutes());
            Tx_Data.add(Imp_.getT3_Time_1().getHours());
            Tx_Data.add(Imp_.getT3_Time_1().getMinutes());
            Tx_Data.add(Imp_.getT1_Time_2().getHours());
            Tx_Data.add(Imp_.getT1_Time_2().getMinutes());
            Tx_Data.add(Imp_.getT3_Time_2().getHours());
            Tx_Data.add(Imp_.getT3_Time_2().getMinutes());
            Tx_Data.add(Imp_.getT2_Time().getHours());
            Tx_Data.add(Imp_.getT2_Time().getMinutes());
            //Показания (12)
            Tx_Data.add(Imp_.getE_Current().getE_T1().getValue_Wt(), 4);
            Tx_Data.add(Imp_.getE_Current().getE_T2().getValue_Wt(), 4);
            Tx_Data.add(Imp_.getE_Current().getE_T3().getValue_Wt(), 4);
            //Максимальная нагрузка
            Tx_Data.add(Imp_.getMax_Power(), 2);
            //Резервные параметры (на будущее)
            Tx_Data.add(0);
            Tx_Data.add(0);
            Tx_Data.add(0);
            Tx_Data.add(0);
        }
        Message("Запись параметров " + imp.getNum().getName());
        return Request_Start();
    }
    //Обработка ответа
    private void CMD_Write_Imp_Params(byte[] bytes_buff) {
        if (bytes_buff[6] == 'O' && bytes_buff[7] == 'K') Message("Параметры успешно записаны");
        if (bytes_buff[6] == 'e' && bytes_buff[7] == 'r') Message("Ошибка при записи");
    }
    //endregion
}

