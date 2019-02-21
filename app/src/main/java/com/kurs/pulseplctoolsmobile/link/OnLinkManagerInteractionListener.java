package com.kurs.pulseplctoolsmobile.link;

import com.kurs.pulseplctoolsmobile.models.PulseBtDevice;

public interface OnLinkManagerInteractionListener
{
    void onDataReceived(byte[] data);
    void onDeviceFound(PulseBtDevice pulseBtDevice);
    void onConnectionSuccessful();
    void onDisconnect();
}
