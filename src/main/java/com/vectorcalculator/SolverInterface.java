package com.vectorcalculator;

import com.vectorcalculator.Properties.YNT;

public interface SolverInterface {
    boolean solve(int delta);
    String getError();
    double getBestDisp();
    boolean solveSuccess();
    boolean singleThrowAllowed();
    boolean mcctAllowed();
    YNT ttAllowed();
    VectorMaximizer getMaximizer();
}