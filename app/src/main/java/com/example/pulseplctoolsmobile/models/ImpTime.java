package com.example.pulseplctoolsmobile.models;

public class ImpTime {
    public ImpTime() {
        this.hours = 0;
        this.minutes = 0;
    }
    public ImpTime(byte hours, byte minutes) {
        this.hours = hours;
        this.minutes = minutes;
    }

    private byte hours;
    private byte minutes;

    public int getTimeInMinutes() { return hours * 60 + minutes; }
    public byte getHours() { return hours;}
    public void setHours(byte value) {
        hours = value;
        if (value > 23) hours = 23;
        if (value < 0) hours = 0;
    }

    public byte getMinutes() { return minutes; }
    public void setMinutes(byte value) {
        minutes = value;
        if (value > 59) minutes = 59;
        if (value < 0) minutes = 0;
    }
}
