package com.vectorcalculator;

public class Sandbox {
    
    public static void main(String[] args) {
        VectorCalculator.main(null);
        int[][] preset = VectorCalculator.midairPresets[1];
        preset[1][1] = 24;
        VectorCalculator.addPreset(preset);
        VectorMaximizer maximizer = VectorCalculator.calculate();
        if (maximizer != null) {
            System.out.println(maximizer.isDiveCapBouncePossible(true, true, false, false));
            System.out.println("Angle: " + Properties.p.diveCapBounceAngle);
            System.out.println("Type: " + maximizer.ctType);
            VectorDisplayWindow.generateData(maximizer.getMotions(), maximizer.getInitialAngle(), maximizer.getTargetAngle());
            VectorDisplayWindow.display();
            ((DiveTurn) maximizer.getMotions()[maximizer.preCapBounceDiveIndex]).getCapBounceFrame(((ComplexVector) maximizer.getMotions()[maximizer.variableCapThrow1Index]).getCappyPosition(0));
        }
    }
}
