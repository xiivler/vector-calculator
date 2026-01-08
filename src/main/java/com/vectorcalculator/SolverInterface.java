package com.vectorcalculator;

import com.vectorcalculator.Properties.TripleThrow;

public interface SolverInterface {
    boolean solve(int delta);
    String getError();
    double getBestDisp();
    boolean solveSuccess();
    boolean singleThrowAllowed();
    boolean mcctAllowed();
    TripleThrow ttAllowed();
    VectorMaximizer getMaximizer();
}