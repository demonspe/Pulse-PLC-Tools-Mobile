package com.kurs.pulseplctoolsmobile.protocol;

//Events
public interface OnProtocolEvent {
    void onCommandEnd(ProtocolDataContainer dataContainer);
    void onAccessEnd();
    void onAccessGranted(AccessType accessType);
}
