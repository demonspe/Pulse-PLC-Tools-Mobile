package com.example.pulseplctoolsmobile.link;

import com.example.pulseplctoolsmobile.models.PulseBtDevice;

public interface OnLinkManagerInteractionListener
{
    void onDataReceived(byte[] data);
    void onDeviceFound(PulseBtDevice pulseBtDevice);
    void onConnectionSuccessful();
    void onDisconnect();
}
