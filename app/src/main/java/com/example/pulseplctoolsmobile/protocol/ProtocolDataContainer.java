package com.example.pulseplctoolsmobile.protocol;

public class ProtocolDataContainer {
    public Commands CommandCode;
    public Object Data;
    public boolean Status;

    public ProtocolDataContainer(boolean status, Commands commandCode, Object data) {
        Status = status;
        CommandCode = commandCode;
        Data = data;
    }
}
