package com.vectorcalculator;

public class DispDataCTDive extends DispData {
    double[][] data;

    public DispDataCTDive(String movementType, int context, double[][] data) {
        super(movementType, context, null, null);
        this.data = data;
    }
}
