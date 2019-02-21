package com.kurs.pulseplctoolsmobile.models;

public class ImpEnergyGroup {
    private boolean isCorrect;
    private ImpEnergyValue e_T1_Value;
    private ImpEnergyValue e_T2_Value;
    private ImpEnergyValue e_T3_Value;

    public boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(boolean value){ isCorrect = value; }

    public ImpEnergyValue getE_T1() { return e_T1_Value; }
    public void setE_T1(ImpEnergyValue value) { e_T1_Value = value; }

    public ImpEnergyValue getE_T2() { return e_T2_Value; }
    public void setE_T2(ImpEnergyValue value) { e_T2_Value = value; }

    public ImpEnergyValue getE_T3() { return e_T3_Value; }
    public void setE_T3(ImpEnergyValue value) { e_T3_Value = value; }

    public String getE_T1_View() {
        if(isCorrect && e_T1_Value.getValue_Wt() < 0xFFFFFFFFL)
            return "" + e_T1_Value.getValue_kWt();
        else
            return "-";
    }
    public String getE_T2_View() {
        if(isCorrect && e_T2_Value.getValue_Wt() < 0xFFFFFFFFL)
            return "" + e_T2_Value.getValue_kWt();
        else return "-";
    }
    public String getE_T3_View() {
        if(isCorrect && e_T3_Value.getValue_Wt() < 0xFFFFFFFFL)
            return "" + e_T3_Value.getValue_kWt();
        else return "-";
    }

    public String getE_Summ_View() {
        if (isCorrect)
        {
            double summ = 0;
            if (e_T1_Value.getValue_kWt() < 0xFFFFFFFFL) summ += e_T1_Value.getValue_kWt();
            if (e_T2_Value.getValue_kWt() < 0xFFFFFFFFL) summ += e_T2_Value.getValue_kWt();
            if (e_T3_Value.getValue_kWt() < 0xFFFFFFFFL) summ += e_T3_Value.getValue_kWt();
            return "" + summ;
        }
        else
            return "-";
    }

    public ImpEnergyGroup(boolean isCorrect) {
        setIsCorrect(isCorrect);
        setE_T1(new ImpEnergyValue(0));
        setE_T2(new ImpEnergyValue(0));
        setE_T3(new ImpEnergyValue(0));
    }
}
