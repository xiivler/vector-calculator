package com.vectorcalculator;

import com.vectorcalculator.Properties.TripleThrow;
import com.vectorcalculator.Properties.TurnDuringDive;
import com.vectorcalculator.VectorCalculator.Parameter;

//class that only solves for the dive lengths
public class DiveSolver {

    Properties p = Properties.p;

    boolean singleThrowAllowed = true;

    String error;

    int diveCapBounceIndex = -1;
    int firstDiveIndex = -1;
    int secondDiveIndex = -1;

    public boolean solve() {

        singleThrowAllowed = true;

        boolean solveFirstDive = false;
        boolean solveSecondDive = false;

        int[][] midairs = p.midairs;

        //find locations of movements in the presets
        for (int i = 0; i < midairs.length; i++) {
            if (i >= 2 && midairs[i][0] == VectorCalculator.CB && midairs[i - 1][0] == VectorCalculator.DIVE &&
                (midairs[i - 2][0] == VectorCalculator.MCCT || midairs[i - 2][0] == VectorCalculator.TT)) {
                diveCapBounceIndex = i;
                firstDiveIndex = i - 1;
                solveFirstDive = true;
            }
            else if (i == midairs.length - 1 && midairs[i][0] == VectorCalculator.DIVE) {
                secondDiveIndex = i;
                solveSecondDive = true;
            }
            else if (midairs[i][0] == VectorCalculator.HTT && solveSecondDive) //CBV first
                singleThrowAllowed = false;
        } //start with all of the movements as low as they might end up so we can calculate falling displacements easier later
        
        if (solveFirstDive) {
            int firstDiveDuration = midairs[1][firstDiveIndex];

            boolean add = true;
            int delta = 0;

            System.out.println("Solving for first dive");

            //test the given dive duration, then 1 more, than 1 less, than 2 more, then 2 less, etc. until a possible dive is found
            boolean found = false;
            found = (testCT() != -1);
            while (!found) {
                if (add) {
                    if (delta < 0)
                        delta--;
                    else
                        delta++;
                }
                else
                    delta *= -1;
                add = !add;
                if (delta > 20) { //different number for moon gravity?
                    error = "Error: Could not bounce on cappy"; 
                    return false;
                }
                int testFirstDiveDuration = firstDiveDuration + delta;
                System.out.println("Testing " + testFirstDiveDuration);
                if (testFirstDiveDuration <= 10 || testFirstDiveDuration >= 40)
                    continue;
                midairs[firstDiveIndex][1] = testFirstDiveDuration;
                VectorCalculator.addPreset(midairs);
                found = (testCT() != -1);
            }
        }

        if (solveSecondDive) {
            VectorMaximizer maximizer = VectorCalculator.getMaximizer();
            System.out.println(p.getUpwarpMinusError());
            System.out.println(getFinalYPos(maximizer));
            while (getFinalYPos(maximizer) + p.getUpwarpMinusError() > p.y1 - Solver.ERROR) {
                midairs[secondDiveIndex][1]++;
                maximizer.movementFrames.set(maximizer.movementFrames.size() - 1, midairs[secondDiveIndex][1]);
            }
            while (getFinalYPos(maximizer) + p.getUpwarpMinusError() < p.y1 - Solver.ERROR) {
                if (midairs[secondDiveIndex][1] < 14) {
                    midairs[secondDiveIndex][1]++; 
                }
                else if (midairs[secondDiveIndex][1] == 14) {
                    error = "Error: Could not reach target height"; 
                    return false;
                }
                else {
                    midairs[secondDiveIndex][1]--;
                }
                maximizer.movementFrames.set(maximizer.movementFrames.size() - 1, midairs[secondDiveIndex][1]);
            }
        }

        VectorCalculator.addPreset(midairs);
        return true;
    }

    public int testCT() {
        if (p.diveTurn == TurnDuringDive.TEST) {
            int testDiveTurn = testCT(.02, .1, true);
            if (testDiveTurn != -1)
                return testDiveTurn;
            else
                return testCT(.1, 1, false);
        }
        else if (p.diveTurn == TurnDuringDive.YES) {
            return testCT(.02, .1, true);
        }
        else
            return testCT(.1, 1, false);
    }

    public int testCT(double edgeCBAngleIncrement, double firstFrameDecelIncrement, boolean diveTurn) {
        VectorMaximizer maximizer = VectorCalculator.getMaximizer();
        if (diveTurn) {
            VectorCalculator.setProperty(Parameter.dive_turn, "Yes");
            p.diveCapBounceAngle = 18;
        }
        else {
            VectorCalculator.setProperty(Parameter.dive_turn, "No");
            maximizer.edgeCBMin = 6;
            p.diveCapBounceAngle = 9;
            maximizer.edgeCBMax = 12;
        }
        maximizer.maximize_HCT_limit = Math.toRadians(8);
        maximizer.firstFrameDecelIncrement = firstFrameDecelIncrement;
        p.diveFirstFrameDecel = 0;
        maximizer.edgeCBAngleIncrement = edgeCBAngleIncrement;
        maximizer.maximize();
        int ctType = maximizer.isDiveCapBouncePossible(-1, singleThrowAllowed, false, p.tripleThrow != TripleThrow.YES, false, p.tripleThrow != TripleThrow.NO);
        return ctType;
    }

    public double getFinalYPos(VectorMaximizer maximizer) {
        maximizer.calcYDisps();
        SimpleMotion[] motions = maximizer.getMotions();
        double y = p.y0 /* + VectorCalculator.getMoonwalkDisp() */;
        for (int i = 0; i < motions.length; i++) {
            y += motions[i].dispY;
        }
        return y;
    }
}
