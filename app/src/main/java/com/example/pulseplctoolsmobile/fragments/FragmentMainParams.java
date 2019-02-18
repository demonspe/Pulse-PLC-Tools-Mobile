package com.example.pulseplctoolsmobile.fragments;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.pulseplctoolsmobile.R;
import com.example.pulseplctoolsmobile.enums.BatteryMode;
import com.example.pulseplctoolsmobile.enums.InterfaceMode;
import com.example.pulseplctoolsmobile.enums.WorkMode;
import com.example.pulseplctoolsmobile.models.DeviceMainParams;
import com.example.pulseplctoolsmobile.models.PulseBtDevice;
import com.example.pulseplctoolsmobile.protocol.Commands;

public class FragmentMainParams extends Fragment {

    //UI
    //
    boolean firstRead;
    //Data
    private DeviceMainParams mainParams;
    //UI
    private Button bReadMainParams, bWriteMainParams;
    private TextView txtDeviceName, txtVersionFirmware, txtErrors;
    private Spinner sWorkMode, sBatteryMode, sRS485Mode, sBluetoothMode;

    private String deviceName;

    //Events
    OnFragmentInteractionListener listener;
    public void setListener(OnFragmentInteractionListener listener) {
        this.listener = listener;
    }

    public FragmentMainParams() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_params, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        //UI get
        txtVersionFirmware = view.findViewById(R.id.txtVersionFirmware);
        txtErrors = view.findViewById(R.id.txtErrors);
        txtDeviceName = view.findViewById(R.id.txtDeviceName);

        //Спиннеры
        sWorkMode = view.findViewById(R.id.sWorkMode);
        sBatteryMode = view.findViewById(R.id.sBatteryMode);
        sRS485Mode = view.findViewById(R.id.sRS485Mode);
        sBluetoothMode = view.findViewById(R.id.sBluetoothMode);

        //Кнопка "Перезагрузить"
        /*btnReboot = view.findViewById(R.id.btnCmdReboot);
        btnReboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(v.getContext())
                        .setMessage("Часть данных может быть потеряна. " +
                                "Уверены что хотите перезагрузить устройство?")
                        .setPositiveButton("Запись", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(listener != null)
                                    listener
                                            .onSendCmdRequest(ProtocolPulsePLCv2.Commands.Reboot, null);
                            }
                        })
                        .setNegativeButton("Отмена", null)
                        .create()
                        .show();
            }
        });*/
        //Кнопка "Прочитать основные параметры"
        bReadMainParams = view.findViewById(R.id.bReadMainParams);
        bReadMainParams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null)
                    listener.onSendCmdRequest(Commands.Read_Main_Params, null);
            }
        });

        //Кнопка "Записать основные параметры
        bWriteMainParams = view.findViewById(R.id.bWriteMainParams);
        bWriteMainParams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(v.getContext())
                        .setMessage("Уверены что хотите записать текущие параметры?")
                        .setPositiveButton("Запись", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(listener != null)
                                    listener.
                                            onSendCmdRequest(Commands.Write_Main_Params, getMainParams());
                            }
                        })
                        .setNegativeButton("Отмена", null)
                        .create()
                        .show();
            }
        });
        //Установим значения по умолчанию
        setMainParams(mainParams);
        //Запрос на чтение параметров
        if(!firstRead) {
            firstRead = true;
            if(listener != null)
                listener.onSendCmdRequest(Commands.Read_Main_Params, null);
        }
    }

    //Сбросим флаг первого чтения,
    //чтобы при открытии фрагмента отправлялась команда на чтение параметров
    public void resetFirstRead() {
        firstRead = false;
    }

    public void setCurrentDevice(PulseBtDevice device) {
        deviceName = device.getFullName();
        if(txtDeviceName != null) txtDeviceName.setText(deviceName);
    }

    public void setMainParams(DeviceMainParams params) {
        if(params == null) return;
        mainParams = params;
        //Имя устройства
        txtDeviceName.setText(deviceName);
        //Версия прошивки
        txtVersionFirmware.setText("Версия прошивки: " + params.getVersionFirmware());
        //Список ошибок
        txtErrors.setText(params.getErrorsList());
        //Режим работы
        sWorkMode.setSelection(params.getWorkMode().ordinal());
        //Режим батареи
        sBatteryMode.setSelection(params.getBatteryMode().ordinal());
        //Режим работы RS485
        sRS485Mode.setSelection(params.getRS485_WorkMode().ordinal());
        //Режим работы Bluetooth
        sBluetoothMode.setSelection(params.getBluetooth_WorkMode().ordinal());
    }

    public DeviceMainParams getMainParams() {
        //Режим работы
        mainParams.setWorkMode(WorkMode.values()[sWorkMode.getSelectedItemPosition()]);
        //Режим батареи
        mainParams.setBatteryMode(BatteryMode.values()[sBatteryMode.getSelectedItemPosition()]);
        //Режим работы RS485
        mainParams.setRS485_WorkMode(InterfaceMode.values()[sRS485Mode.getSelectedItemPosition()]);
        //Режим работы Bluetooth
        mainParams.setBluetooth_WorkMode(InterfaceMode.values()[sBluetoothMode.getSelectedItemPosition()]);

        return mainParams;
    }

}
