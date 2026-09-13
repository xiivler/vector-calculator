package com.vectorcalculator;

public class DispDataCTDive extends DispData {
    double[][] data;

    public DispDataCTDive(String movementType, int context, double[][] data) {
        super(movementType, context, null, null);
        this.data = data;
    }

    public int frames(int index) {
        return (int) data[index][0];
    }

    public int ctFrames(int index) {
        return (int) data[index][1];
    }

    public int diveFrames(int index) {
        return (int) data[index][2];
    }

    public double forwardDisp(int index) {
        return data[index][3];
    }

    public double yDisp(int index) {
        return data[index][4];
    }
}
