package com.vectorcalculator;

import com.vectorcalculator.Properties.TripleThrow;
import com.vectorcalculator.Properties.TurnDuringDive;
import com.vectorcalculator.VectorCalculator.Parameter;

//class that only solves for the dive lengths
public class DiveSolver implements SolverInterface {

    public static final int MIN_DIVE_DURATION = 10;
    public static final int MAX_DIVE_DURATION = 40;

    Properties p = Properties.p;

    boolean singleThrowAllowed = true;
    boolean mcctAllowed = true;
    TripleThrow ttAllowed;

    String error;

    boolean success;

    VectorMaximizer maximizer;

    int diveCapBounceIndex = -1;
    int firstDiveIndex = -1;
    int secondDiveIndex = -1;

    double bestDisp = 0;

    public String getError() {
        return error;
    }

    public boolean solveSuccess() {
        return success;
    }

    public double getBestDisp() {
        return bestDisp;
    }

    public TripleThrow ttAllowed() {
        return ttAllowed;
    }

    public boolean singleThrowAllowed() {
        return singleThrowAllowed;
    }

    public boolean mcctAllowed() {
        return mcctAllowed;
    }

    public VectorMaximizer getMaximizer() {
        return maximizer;
    }

    public boolean solve(int maxDelta) {
        long startTime = System.currentTimeMillis();

        boolean oldDurationFrames = p.durationFrames;

        if (!p.durationFrames) {
            p.initialFrames = VectorCalculator.initialMovement.getMotion(p.initialFrames, false, false).calcFrames(p.initialDispY - VectorCalculator.getMoonwalkDisp());
        }
        p.durationFrames = true;

        singleThrowAllowed = true;
        mcctAllowed = true;

        if (p.midairPreset.equals("Spinless") || p.midairPreset.equals("Simple Tech")) {
            ttAllowed = p.tripleThrow;
            if (ttAllowed == TripleThrow.YES) {
                singleThrowAllowed = false;
                mcctAllowed = false;
            }
        }
        else
            ttAllowed = TripleThrow.NO;

        boolean solveFirstDive = false;
        boolean solveSecondDive = false;

        int[][] midairs = p.midairs;

        //find locations of movements in the presets
        for (int i = 0; i < midairs.length; i++) {
            if (i >= 2 && midairs[i][0] == VectorCalculator.CB && midairs[i - 1][0] == VectorCalculator.DIVE &&
                (midairs[i - 2][0] == VectorCalculator.MCCT || midairs[i - 2][0] == VectorCalculator.CT || midairs[i - 2][0] == VectorCalculator.TT)) {
                diveCapBounceIndex = i;
                firstDiveIndex = i - 1;
                solveFirstDive = true;
                if (p.midairPreset.equals("Custom")) {
                    if (midairs[i - 2][0] == VectorCalculator.MCCT)
                        ttAllowed = TripleThrow.NO;
                    else if (midairs[i - 2][0] == VectorCalculator.CT) {
                        ttAllowed = TripleThrow.NO;
                        mcctAllowed = false;
                    }
                    else {
                        ttAllowed = TripleThrow.YES;
                        singleThrowAllowed = false;
                        mcctAllowed = false;
                    }
                }
            }
            else if (i == midairs.length - 1 && midairs[i][0] == VectorCalculator.DIVE) {
                secondDiveIndex = i;
                solveSecondDive = true;
            }
            else if (midairs[i][0] == VectorCalculator.HTT && solveSecondDive) //CBV first
                singleThrowAllowed = false;
        } //start with all of the movements as low as they might end up so we can calculate falling displacements easier later
        
        if (solveFirstDive) {
            int firstDiveDuration = midairs[firstDiveIndex][1];

            if (firstDiveDuration > MAX_DIVE_DURATION) {
                firstDiveDuration = MAX_DIVE_DURATION;
            }

            boolean add = true;
            int delta = 0;

            Debug.println("Solving for first dive");

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
                if (delta > maxDelta) { //different number for moon gravity?
                    error = "Error: Could not bounce on cappy"; 
                    p.durationFrames = oldDurationFrames;
                    success = false;
                    return false;
                }
                int testFirstDiveDuration = firstDiveDuration + delta;
                Debug.println("Testing " + testFirstDiveDuration);
                if (testFirstDiveDuration <= MIN_DIVE_DURATION || testFirstDiveDuration >= MAX_DIVE_DURATION)
                    continue;
                midairs[firstDiveIndex][1] = testFirstDiveDuration;
                VectorCalculator.addPreset(midairs);
                found = (testCT() != -1);
            }
        }

        System.out.println(p.initialFrames);

        if (solveSecondDive) {
            maximizer = VectorCalculator.getMaximizer();
            Debug.println(p.getUpwarpMinusError());
            Debug.println(getFinalYPos(maximizer));
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
                    p.durationFrames = oldDurationFrames;
                    success = false;
                    return false;
                }
                else {
                    midairs[secondDiveIndex][1]--;
                }
                maximizer.movementFrames.set(maximizer.movementFrames.size() - 1, midairs[secondDiveIndex][1]);
            }
        }

        VectorCalculator.addPreset(midairs);

        if (solveSecondDive) {
            maximizer = VectorCalculator.getMaximizer();
            maximizer.maximize();
            if (solveFirstDive)
                maximizer.isDiveCapBouncePossible(-1, singleThrowAllowed, false, mcctAllowed, !singleThrowAllowed && ttAllowed != TripleThrow.YES, p.tripleThrow != TripleThrow.NO); 
        }

        if (!solveFirstDive && !solveSecondDive) {
            maximizer = VectorCalculator.getMaximizer();
            maximizer.maximize();
        }

        maximizer.recalculateDisps();
        maximizer.adjustToGivenAngle();

        VectorCalculator.setProgressText("Solver: Calculated in " + (System.currentTimeMillis() - startTime) + " ms");

        p.durationFrames = oldDurationFrames;
        success = true;
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
        maximizer = VectorCalculator.getMaximizer();
        if (diveTurn) {
            VectorCalculator.setProperty(Parameter.dive_turn, "Yes");
            maximizer.edgeCBMin = 0;
            p.diveCapBounceAngle = 0;
        }
        else {
            VectorCalculator.setProperty(Parameter.dive_turn, "No");
            maximizer.edgeCBMin = 0;
            p.diveCapBounceAngle = 0;
            maximizer.edgeCBMax = 12;
        }
        maximizer.maximize_HCT_limit = Math.toRadians(8);
        maximizer.firstFrameDecelIncrement = firstFrameDecelIncrement;
        p.diveFirstFrameDecel = 0;
        maximizer.edgeCBAngleIncrement = edgeCBAngleIncrement;
        bestDisp = maximizer.maximize();
        int ctType = maximizer.isDiveCapBouncePossible(-1, singleThrowAllowed, false, mcctAllowed, !singleThrowAllowed && ttAllowed != TripleThrow.YES, p.tripleThrow != TripleThrow.NO);
        return ctType;
    }

    public double getFinalYPos(VectorMaximizer maximizer) {
        //maximizer.maximize();
        maximizer.calcYDisps();
        SimpleMotion[] motions = maximizer.getMotions();
        double y = p.y0;
        for (int i = 0; i < motions.length; i++) {
            y += motions[i].dispY;
            
            System.out.println(motions[i].movement.movementType + " " + motions[i].dispY);
        }
        return y;
    }
}
