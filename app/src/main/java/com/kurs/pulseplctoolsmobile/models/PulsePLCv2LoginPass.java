package com.kurs.pulseplctoolsmobile.models;

public class PulsePLCv2LoginPass {
    private PulsePLCv2Serial serial;
    private PulsePLCv2Pass pass;

    public PulsePLCv2Serial getSerial() { return serial; }
    public PulsePLCv2Pass getPass() { return pass; }

    public void setSerial(PulsePLCv2Serial serial) { this.serial = serial; }
    public void setPass(PulsePLCv2Pass pass) { this.pass = pass; }

    public PulsePLCv2LoginPass() {
        this.serial = new PulsePLCv2Serial();
        this.pass = new PulsePLCv2Pass();
    }
    public PulsePLCv2LoginPass(byte[] serial, byte[] pass) {
        this.serial = new PulsePLCv2Serial(serial);
        this.pass = new PulsePLCv2Pass(pass);
    }
    public PulsePLCv2LoginPass(String serial, String pass) {
        this.serial = new PulsePLCv2Serial(serial);
        this.pass = new PulsePLCv2Pass(pass);
    }
}
