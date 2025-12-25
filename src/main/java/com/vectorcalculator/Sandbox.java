package com.vectorcalculator;

import java.util.Arrays;

public class Sandbox {
    
    public static void main(String[] args) {
        VectorCalculator.main(null);
        int[][] preset = VectorCalculator.midairPresets[1];
        
        boolean[][] possible = new boolean[35][35];
        double[][] firstFrameDecels = new double[35][35];

        /* for (int i = 24; i <= 32; i++) {
            for (int j = 18; j <= 27; j++) {
                preset[0][1] = i;
                preset[1][1] = j;
                VectorCalculator.addPreset(preset);
                VectorMaximizer maximizer = VectorCalculator.calculate();
                if (maximizer != null) {
                    possible[i][j] = maximizer.isDiveCapBouncePossible(true, true, false, false);
                    firstFrameDecels[i][j] = maximizer.firstFrameDecel;
                    //System.out.println(preset[0][1] + " " + preset[1][1] + " " + maximizer.isDiveCapBouncePossible(true, true, true, true) + " " + maximizer.ctType + " " + maximizer.firstFrameDecel);
                    //System.out.println("Angle: " + Properties.p.diveCapBounceAngle);
                    //System.out.println("Type: " + maximizer.ctType);
                    //VectorDisplayWindow.generateData(maximizer.getMotions(), maximizer.getInitialAngle(), maximizer.getTargetAngle());
                    //VectorDisplayWindow.display();
                    ((DiveTurn) maximizer.getMotions()[maximizer.preCapBounceDiveIndex]).getCapBounceFrame(((ComplexVector) maximizer.getMotions()[maximizer.variableCapThrow1Index]).getCappyPosition(0));
                }
            }
        }
        for (int i = 0; i < 34; i++) {
            //System.out.println(Arrays.toString(possible[i]) + ", ");
            System.out.println(Arrays.toString(firstFrameDecels[i]) + ", ");
        } */
         preset[0][1] = 28;
         preset[1][1] = 25;
         VectorCalculator.addPreset(preset);
         VectorMaximizer maximizer = VectorCalculator.getMaximizer();
         //maximizer.neverDiveTurn = true;
         maximizer.maximize();
         System.out.println(preset[0][1] + " " + preset[1][1] + " " + maximizer.isDiveCapBouncePossible(true, true, true, false) + " " + maximizer.ctType);
         //VectorDisplayWindow.generateData(maximizer.getMotions(), maximizer.getInitialAngle(), maximizer.getTargetAngle());
         VectorDisplayWindow.display();
    }
}
