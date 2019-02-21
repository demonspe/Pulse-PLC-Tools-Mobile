package com.kurs.pulseplctoolsmobile.fragments;

import com.kurs.pulseplctoolsmobile.models.PulseBtDevice;
import com.kurs.pulseplctoolsmobile.protocol.Commands;

//Events
public interface OnFragmentInteractionListener {
    void onScanBLERequest(boolean status);
    void onConnectToDeviceRequest(PulseBtDevice device);
    void onGoToPageRequest(Pages p);
    void onSendDataDirectly(byte[] data);
    void onSendCmdRequest(Commands cmd, Object param);
}

