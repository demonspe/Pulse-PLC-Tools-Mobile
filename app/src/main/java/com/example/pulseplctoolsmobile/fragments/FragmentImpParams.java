package com.example.pulseplctoolsmobile.fragments;


import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.pulseplctoolsmobile.Helper;
import com.example.pulseplctoolsmobile.R;
import com.example.pulseplctoolsmobile.enums.ImpAscueProtocolType;
import com.example.pulseplctoolsmobile.enums.ImpNum;
import com.example.pulseplctoolsmobile.enums.ImpNumOfTarifs;
import com.example.pulseplctoolsmobile.enums.ImpOverflowType;
import com.example.pulseplctoolsmobile.models.DeviceImpParams;
import com.example.pulseplctoolsmobile.models.ImpEnergyGroup;
import com.example.pulseplctoolsmobile.models.ImpEnergyValue;
import com.example.pulseplctoolsmobile.models.ImpTime;
import com.example.pulseplctoolsmobile.protocol.Commands;

import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentImpParams extends Fragment {

    //Data
    DeviceImpParams imp;
    //
    boolean firstRead;

    //Buttons
    private Button bReadImpParams, bWriteImpParams;

    //UI
    TextView txtTitleImpNum;
    Switch swEnable;        //Включен ли импульсный вход
    EditText tbAdrsPLC;     //Адрес PLC
    EditText tbA;           //Передаточное число
    EditText tbMaxLoad;     //Ограничение нагрузки
    Spinner sCapacityMode;  //Разрядность индикатора

    //Показания
    EditText tbECurrentT1, tbECurrentT2, tbECurrentT3;  //Текущие
    TextView txtEStartDayT1,txtEStartDayT2,txtEStartDayT3, txtEStartDaySumm; //Начало суток

    //Для протокола RS485
    Spinner sProtocolType;
    EditText tbNetAdrs, tbPass;

    //Тарифы
    Spinner sTariffsMode;
    Button bT1_1, bT3_1, bT1_2, bT3_2, bT2;

    //Events
    OnFragmentInteractionListener listener;
    public void setListener(OnFragmentInteractionListener listener) {
        this.listener = listener;
    }

    public FragmentImpParams() {
        // Required empty public constructor

    }

    public void resetFirstRead() {
        firstRead = false;
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_imps, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        //Get UI
        txtTitleImpNum = view.findViewById(R.id.txtTitleImpNum);
        swEnable = view.findViewById(R.id.swEnable);
        swEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                tbAdrsPLC.setEnabled(isChecked);
                tbA.setEnabled(isChecked);
                tbMaxLoad.setEnabled(isChecked);
                sCapacityMode.setEnabled(isChecked);
                tbECurrentT1.setEnabled(isChecked);
                tbECurrentT2.setEnabled(isChecked);
                tbECurrentT3.setEnabled(isChecked);
                txtEStartDayT1.setEnabled(isChecked);
                txtEStartDayT2.setEnabled(isChecked);
                txtEStartDayT3.setEnabled(isChecked);
                txtEStartDaySumm.setEnabled(isChecked);
                sProtocolType.setEnabled(isChecked);
                tbNetAdrs.setEnabled(isChecked);
                tbPass.setEnabled(isChecked);
                sTariffsMode.setEnabled(isChecked);
                if(isChecked)
                    setEnabledDisabledTariffsTime(sTariffsMode.getSelectedItemPosition());
                else
                    setEnabledDisabledTariffsTime(0);
            }
        });
        tbAdrsPLC = view.findViewById(R.id.tbAdrsPLC);
        tbA = view.findViewById(R.id.tbA);
        tbMaxLoad = view.findViewById(R.id.tbMaxLoad);
        sCapacityMode = view.findViewById(R.id.sCapacityMode);

        tbECurrentT1 = view.findViewById(R.id.tbECurrentT1);
        tbECurrentT2 = view.findViewById(R.id.tbECurrentT2);
        tbECurrentT3 = view.findViewById(R.id.tbECurrentT3);

        txtEStartDayT1 = view.findViewById(R.id.txtEStartDayT1);
        txtEStartDayT2 = view.findViewById(R.id.txtEStartDayT2);
        txtEStartDayT3 = view.findViewById(R.id.txtEStartDayT3);
        txtEStartDaySumm = view.findViewById(R.id.txtEStartDaySumm);

        sProtocolType = view.findViewById(R.id.sProtocolType);
        tbNetAdrs = view.findViewById(R.id.tbNetAdrs);
        tbPass = view.findViewById(R.id.tbPass);

        sTariffsMode = view.findViewById(R.id.sTariffsMode);
        sTariffsMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setEnabledDisabledTariffsTime(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        bT1_1 = view.findViewById(R.id.bT1_1);
        bT1_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        imp.setT1_Time_1(new ImpTime((byte)hourOfDay, (byte)minute));
                        bT1_1.setText(String.format("%02d", imp.getT1_Time_1().getHours()) +
                                " : " +
                                String.format("%02d", imp.getT1_Time_1().getMinutes()));
                    }
                }, imp.getT1_Time_1().getHours(), imp.getT1_Time_1().getMinutes(), true).show();
            }
        });
        bT3_1 = view.findViewById(R.id.bT3_1);
        bT3_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        imp.setT3_Time_1(new ImpTime((byte)hourOfDay, (byte)minute));
                        bT3_1.setText(String.format("%02d", imp.getT3_Time_1().getHours()) +
                                " : " +
                                String.format("%02d", imp.getT3_Time_1().getMinutes()));
                    }
                }, imp.getT3_Time_1().getHours(), imp.getT3_Time_1().getMinutes(), true).show();
            }
        });
        bT1_2 = view.findViewById(R.id.bT1_2);
        bT1_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        imp.setT1_Time_2(new ImpTime((byte)hourOfDay, (byte)minute));
                        bT1_2.setText(String.format("%02d", imp.getT1_Time_2().getHours()) +
                                " : " +
                                String.format("%02d", imp.getT1_Time_2().getMinutes()));
                    }
                }, imp.getT1_Time_2().getHours(), imp.getT1_Time_2().getMinutes(), true).show();
            }
        });
        bT3_2 = view.findViewById(R.id.bT3_2);
        bT3_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        imp.setT3_Time_2(new ImpTime((byte)hourOfDay, (byte)minute));
                        bT3_2.setText(String.format("%02d", imp.getT3_Time_2().getHours()) +
                                " : " +
                                String.format("%02d", imp.getT3_Time_2().getMinutes()));
                    }
                }, imp.getT3_Time_2().getHours(), imp.getT3_Time_2().getMinutes(), true).show();
            }
        });
        bT2 = view.findViewById(R.id.bT2);
        bT2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        imp.setT2_Time(new ImpTime((byte)hourOfDay, (byte)minute));
                        bT2.setText(String.format("%02d", imp.getT2_Time().getHours()) +
                                " : " +
                                String.format("%02d", imp.getT2_Time().getMinutes()));
                    }
                }, imp.getT2_Time().getHours(), imp.getT2_Time().getMinutes(), true).show();
            }
        });

        //Buttons
        //Кнопка "Прочитать"
        bReadImpParams = view.findViewById(R.id.bReadImpParams);
        bReadImpParams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null)
                    listener.onSendCmdRequest(Commands.Read_IMP, imp.getNum());
            }
        });
        //Кнопка "Записать"
        bWriteImpParams = view.findViewById(R.id.bWriteImpParams);
        bWriteImpParams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(v.getContext())
                        .setMessage("Уверены что хотите записать текущие параметры?")
                        .setPositiveButton("Запись", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(listener != null)
                                    listener.onSendCmdRequest(Commands.Write_IMP, getImpParams());
                            }
                        })
                        .setNegativeButton("Отмена", null)
                        .create()
                        .show();
            }
        });

        setCurrentImp(imp.getNum());
        //Прочитаем параметры если не прочитано
        firstRead();


    }

    private void firstRead() {
        if(!firstRead && imp != null) {
            if(listener != null){
                listener.onSendCmdRequest(Commands.Read_IMP, imp.getNum());
                firstRead = true;
            }
        }
    }

    public void setCurrentImp(ImpNum impNum) {
        imp = new DeviceImpParams(impNum);
        resetFirstRead();
        setImpParams(imp);
    }

    public void setImpParams(DeviceImpParams impParams) {
        if(impParams == null) return;
        //Текущие настройки
        imp = impParams;

        if(txtTitleImpNum == null) return;
        txtTitleImpNum.setText(imp.getNum().getName());

        //Выводим на экран
        swEnable.setChecked(false);
        swEnable.setChecked(true);
        swEnable.setChecked(imp.getIsEnable_bool());
        tbAdrsPLC.setText("" + Helper.toUInt(imp.getAdrs_PLC()));
        tbA.setText("" + imp.getA());
        tbMaxLoad.setText("" + imp.getMax_Power());
        sCapacityMode.setSelection(imp.getPerepoln().ordinal());

        tbECurrentT1.setText("" + imp.getE_Current().getE_T1().getValue_kWt());
        tbECurrentT2.setText("" + imp.getE_Current().getE_T2().getValue_kWt());
        tbECurrentT3.setText("" + imp.getE_Current().getE_T3().getValue_kWt());

        txtEStartDayT1.setText(imp.getE_StartDay().getE_T1_View());
        txtEStartDayT2.setText(imp.getE_StartDay().getE_T2_View());
        txtEStartDayT3.setText(imp.getE_StartDay().getE_T3_View());
        txtEStartDaySumm.setText(imp.getE_StartDay().getE_Summ_View());

        sProtocolType.setSelection(imp.getAscue_protocol().ordinal());
        tbNetAdrs.setText("" + imp.getAscue_adrs());
        tbPass.setText(imp.getAscue_pass_string());

        sTariffsMode.setSelection(imp.getT_qty().ordinal());
        bT1_1.setText(String.format("%02d", imp.getT1_Time_1().getHours()) +
                " : " +
                String.format("%02d", imp.getT1_Time_1().getMinutes()));
        bT3_1.setText(String.format("%02d", imp.getT3_Time_1().getHours()) +
                " : " +
                String.format("%02d", imp.getT3_Time_1().getMinutes()));
        bT1_2.setText(String.format("%02d", imp.getT1_Time_2().getHours()) +
                " : " +
                String.format("%02d", imp.getT1_Time_2().getMinutes()));
        bT3_2.setText(String.format("%02d", imp.getT3_Time_2().getHours()) +
                " : " +
                String.format("%02d", imp.getT3_Time_2().getMinutes()));
        bT2.setText(String.format("%02d", imp.getT2_Time().getHours()) +
                " : " +
                String.format("%02d", imp.getT2_Time().getMinutes()));
    }

    public DeviceImpParams getImpParams() {
        imp.setIsEnable_bool(swEnable.isChecked());
        imp.setAdrs_PLC((byte)Integer.parseInt(tbAdrsPLC.getText().toString()));
        imp.setA(Integer.parseInt(tbA.getText().toString()));
        imp.setMax_Power(Integer.parseInt(tbMaxLoad.getText().toString()));
        imp.setPerepoln(ImpOverflowType.values()[sCapacityMode.getSelectedItemPosition()]);

        ImpEnergyGroup e_cur = new ImpEnergyGroup(true);
        e_cur.setE_T1(new ImpEnergyValue(tbECurrentT1.getText().toString()));
        e_cur.setE_T2(new ImpEnergyValue(tbECurrentT2.getText().toString()));
        e_cur.setE_T3(new ImpEnergyValue(tbECurrentT3.getText().toString()));
        imp.setE_Current(e_cur);

        imp.setAscue_protocol(ImpAscueProtocolType.values()[sProtocolType.getSelectedItemPosition()]);
        imp.setAscue_adrs(Integer.parseInt(tbNetAdrs.getText().toString()));
        imp.setAscue_pass_string(tbPass.getText().toString());

        imp.setT_qty(ImpNumOfTarifs.values()[sTariffsMode.getSelectedItemPosition()]);

        return imp;
    }

    private void setEnabledDisabledTariffsTime(int numOfTariffsIndex){
        if(numOfTariffsIndex == 0){ //Один тариф
            bT1_1.setEnabled(false);
            bT3_1.setEnabled(false);
            bT1_2.setEnabled(false);
            bT3_2.setEnabled(false);
            bT2.setEnabled(false);
        } else if(numOfTariffsIndex == 1){ //Два
            bT1_1.setEnabled(true);
            bT3_1.setEnabled(false);
            bT1_2.setEnabled(false);
            bT3_2.setEnabled(false);
            bT2.setEnabled(true);
        } else if(numOfTariffsIndex == 2){ //Три
            bT1_1.setEnabled(true);
            bT3_1.setEnabled(true);
            bT1_2.setEnabled(true);
            bT3_2.setEnabled(true);
            bT2.setEnabled(true);
        }
    }
}
