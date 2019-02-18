package com.example.pulseplctoolsmobile.fragments;

import com.example.pulseplctoolsmobile.models.PulseBtDevice;
import com.example.pulseplctoolsmobile.protocol.Commands;

//Events
public interface OnFragmentInteractionListener {
    void onScanBLERequest(boolean status);
    void onConnectToDeviceRequest(PulseBtDevice device);
    void onGoToPageRequest(Pages p);
    void onSendDataDirectly(byte[] data);
    void onSendCmdRequest(Commands cmd, Object param);
}

