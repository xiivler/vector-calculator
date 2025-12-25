package com.vectorcalculator;

public class Sandbox {
    
    public static void main(String[] args) {
        VectorCalculator.main(null);
        int[][] preset = VectorCalculator.midairPresets[1];
        
        for (int i = 20; i <= 32; i++) {
            for (int j = 20; j <= 32; j++) {
                preset[0][1] = i;
                preset[1][1] = j;
                VectorCalculator.addPreset(preset);
                VectorMaximizer maximizer = VectorCalculator.calculate();
                if (maximizer != null) {
                    System.out.println(preset[0][1] + " " + preset[1][1] + " " + maximizer.isDiveCapBouncePossible(true, true, true, true));
                    //System.out.println("Angle: " + Properties.p.diveCapBounceAngle);
                    //System.out.println("Type: " + maximizer.ctType);
                    //VectorDisplayWindow.generateData(maximizer.getMotions(), maximizer.getInitialAngle(), maximizer.getTargetAngle());
                    //VectorDisplayWindow.display();
                    ((DiveTurn) maximizer.getMotions()[maximizer.preCapBounceDiveIndex]).getCapBounceFrame(((ComplexVector) maximizer.getMotions()[maximizer.variableCapThrow1Index]).getCappyPosition(0));
                }
            }
        }
        preset[0][1] = 30;
        preset[1][1] = 25;
        VectorCalculator.addPreset(preset);
        VectorMaximizer maximizer = VectorCalculator.calculate();
        System.out.println(preset[0][1] + " " + preset[1][1] + " " + maximizer.isDiveCapBouncePossible(true, true, true, false));
        VectorDisplayWindow.generateData(maximizer.getMotions(), maximizer.getInitialAngle(), maximizer.getTargetAngle());
        VectorDisplayWindow.display();
    }
}
