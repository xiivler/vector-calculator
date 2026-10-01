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

    public double maxYDisp() {
        double maxYDisp = 0;
        for (int index = 0; index < data.length; index++) {
            maxYDisp = Math.max(maxYDisp, yDisp(index));
        }
        return maxYDisp;
    }

    public int minFrames() {
        int minFrames = Integer.MAX_VALUE;
        for (int index = 0; index < data.length; index++) {
            minFrames = Math.min(minFrames, frames(index));
        }
        return minFrames;
    }
}
