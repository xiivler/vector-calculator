package com.vectorcalculator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import com.vectorcalculator.Properties.TurnDuringDive;
import com.vectorcalculator.Properties.GroundType;
import com.vectorcalculator.Properties.TripleThrow;
import com.vectorcalculator.VectorCalculator.Parameter;

//this class finds the optimal durations for each midair input, given the target vertical displacement
public class Solver {

    long lastAlertTime = 0;
    //static final double limit = 20; 
    //this limit takes a while for TT jumps
    static final double ERROR = .0001; //acceptable amount of error on double addition/subtraction
    static final int REFRESH_RATE = 5; //after how many iterations the values are recaluclated when getting a ballpark estimate
    static final double HCT_CHANGE_ANGLE_HEIGHT = 150; //if hct is less than this height above the ground, angle will be reduced to HCT_SMALLER_ANGLE so it still comes back in time
    static final double HCT_SMALLER_ANGLE = 40;

    double limit = 20; //if the final y height of the test is above this number, assume it can't be optimal

    int cbDurationLimit = Integer.MAX_VALUE; //limit for how long cb duration can be

    double bestResultsRange = 5; //range of values worse than the current best to still test in full

    boolean singleThrowAllowed = true;
    TripleThrow ttAllowed;
    TurnDuringDive dtAllowed;

    boolean hasRCV;

    Properties p = Properties.p;

    VectorMaximizer maximizer;

    int ctType;
    double diveDecel;
    double edgeCBAngle;

    int[][] preset;
    int[] durations;
    int[] lastFrames;
    double[] y_vels;
    double[] efficiencies;
    double[] final_y_heights; //the end y height of each motion
    double[] y_disps;

    double[][] info;
    double firstFrameVelocityAngle;

    int[][] ctTypes;
    double[][] diveDecels;
    double[][] edgeCBAngles;
    boolean[][] diveTurns;

    double firstFrameDecel;

    int rainbowSpinIndex = -1;
    int homingMCCTIndex = -1;
    int homingTTIndex = -1;
    int firstDiveIndex = -1;
    int secondDiveIndex = -1;
    int finalCapThrowIndex = -1;
    int diveCapBounceIndex = -1;

    int iterations = 0;
    int innerCalls = 0;
    int badCalls = 0;

    int delta = 0;
    
    double x;
    double y;
    double z;

    double bestDisp = 0;
    int[] bestDurations;
    double bestYDisp = 0; //for debug

    ArrayList<DoubleIntArray> bestResults;

    public double[] getFinalYHeights(VectorMaximizer maximizer) {
        maximizer.calcYDisps();
        SimpleMotion[] motions = maximizer.getMotions();
        double y = p.y0;
        final_y_heights = new double[motions.length];
        for (int i = 0; i < motions.length; i++) {
            y += motions[i].dispY;
            final_y_heights[i] = y;
        }
        return final_y_heights;
    }

    public boolean solve(int delta) {
        Debug.println("Starting Solver");
        long startTime = System.currentTimeMillis();

        if (p.groundTypeFirstGP != GroundType.NONE || p.groundTypeCB != GroundType.NONE || p.groundTypeSecondGP != GroundType.NONE) {
            limit = 20;
        }
        else {
            limit = 4;
        }

        this.delta = delta;

        p.diveFirstFrameDecel = 0;
        p.diveCapBounceAngle = 18;

        if (p.solveForInitialAngle) {
            p.initialAngle = p.targetAngle;
        }

        if (p.chooseJumpFrames) {
            VectorCalculator.setProperty(Parameter.jump_button_frames, 10);
        }

        //custom presets not yet supported
        if (p.midairPreset.equals("Custom"))
            return true;

        VectorCalculator.addPreset(p.midairPreset, false);

        singleThrowAllowed = true;
        if (p.midairPreset.equals("CBV First") && p.tripleThrow != TripleThrow.NO) {
            singleThrowAllowed = false;
            cbDurationLimit = 36;
        }
        else if (p.midairPreset.equals("Simple Tech")) {
            cbDurationLimit = 36;
        }
        if (p.midairPreset.equals("Spinless") || p.midairPreset.equals("Simple Tech"))
            ttAllowed = p.tripleThrow;
        else
            ttAllowed = TripleThrow.NO;

        dtAllowed = p.diveTurn;

        int[][] unmodifiedPreset = VectorCalculator.getPreset(p.midairPreset);
        preset = new int[unmodifiedPreset.length][unmodifiedPreset[0].length];

        //find locations of movements in the presets
        for (int i = 0; i < preset.length; i++) {
            preset[i][0] = unmodifiedPreset[i][0];
            preset[i][1] = unmodifiedPreset[i][1];
            if (preset[i][0] == VectorCalculator.RS)
                rainbowSpinIndex = i + 1;
            else if (preset[i][0] == VectorCalculator.HMCCT) {
                homingMCCTIndex = i + 1;
                preset[i][1] = Math.max(30, p.hctCapReturnFrame);
            }
            else if (preset[i][0] ==  VectorCalculator.HTT)
                homingTTIndex = i + 1;
            else if (preset[i][0] == VectorCalculator.CB && i > 0 && preset[i - 1][0] == VectorCalculator.DIVE) {
                diveCapBounceIndex = i + 1;
                firstDiveIndex = i;
            }
            else if (i == preset.length - 1 && preset[i][0] == VectorCalculator.DIVE) {
                secondDiveIndex = i + 1;
                finalCapThrowIndex = i;
            }
        } //start with all of the movements as low as they might end up so we can calculate falling displacements easier later

        //first, fix the preset so that it makes sense with the height of the ground
        p.onMoon = p.onMoon;
        
        p.initialDispY = p.y1 - p.y0 - 1000;
        p.initialFrames = VectorCalculator.initialMotion.calcFrames(p.initialDispY - VectorCalculator.getMoonwalkDisp());

        p.durationFrames = true;

        Debug.println("Reached Preset Maximization");

        preset[diveCapBounceIndex - 1][1] = Math.min(p.initialFrames, cbDurationLimit); //make cap bounce also big to start (will be shortened later)
        preset[secondDiveIndex - 1][1] = p.initialFrames; //make final dive also big to start
        preset[finalCapThrowIndex - 1][1] = p.initialFrames;
        VectorCalculator.addPreset(preset);
        VectorMaximizer presetMaximizer = VectorCalculator.getMaximizer();

        Debug.println("Got Maximzer");

        int maximizer_initialMovementIndex = -1;
        for (int i = 1; i < presetMaximizer.movementNames.size(); i++) {
            if (Movement.isMidairCapThrow(presetMaximizer.movementNames.get(i))) {
                maximizer_initialMovementIndex = i - 1;
                break;
            }
        }
        int maximizer_firstGPIndex;
        if (presetMaximizer.hasVariableCapThrow1Falling)
            maximizer_firstGPIndex = presetMaximizer.variableCapThrow1Index + 2;
        else
            maximizer_firstGPIndex = presetMaximizer.variableCapThrow1Index + 1;
        int maximizer_firstDiveIndex = -1;
        int maximizer_capBounceIndex = -1;
        if (presetMaximizer.movementNames.get(maximizer_firstGPIndex + 1).equals("Dive")) {
            maximizer_firstDiveIndex = maximizer_firstGPIndex + 1; //different than later firstDiveIndex (do note)
            maximizer_capBounceIndex = maximizer_firstDiveIndex + 1;
        }
        int maximizer_secondGPIndex;
        if (presetMaximizer.hasVariableMovement2Falling)
            maximizer_secondGPIndex = presetMaximizer.variableMovement2Index + 2;
        else
            maximizer_secondGPIndex = presetMaximizer.variableMovement2Index + 1;

        //shorten first movement until first GP isn't too low
        double[] final_y_heights = getFinalYHeights(presetMaximizer);
        if (p.groundTypeFirstGP != GroundType.NONE) {
            while (final_y_heights[maximizer_firstGPIndex] < p.groundHeightFirstGP + Movement.MIN_GP_HEIGHT) {
                //Debug.println(final_y_heights[maximizer_firstGPIndex]);
                p.initialFrames--;
                if (p.initialFrames < VectorCalculator.initialMovement.getMinFrames())
                    return false;
                presetMaximizer.movementFrames.set(maximizer_initialMovementIndex, p.initialFrames);
                final_y_heights = getFinalYHeights(presetMaximizer);
            }
        } 
        if (p.groundTypeCB != GroundType.NONE) {
            while (final_y_heights[maximizer_firstDiveIndex] <= p.groundHeightCB) {
                p.initialFrames--;
                if (p.initialFrames < VectorCalculator.initialMovement.getMinFrames())
                    return false;
                presetMaximizer.movementFrames.set(maximizer_initialMovementIndex, p.initialFrames);
                final_y_heights = getFinalYHeights(presetMaximizer);
            }
        }
        //shorten either first movement or cap bounce until second GP isn't too low
        presetMaximizer = VectorCalculator.getMaximizer();
        calcFrameByFrame(presetMaximizer);
        final_y_heights = getFinalYHeights(presetMaximizer);
        //Debug.println(Arrays.toString(final_y_heights));
        if (p.groundTypeSecondGP != GroundType.NONE) {
            int iterations = 0;
            while (final_y_heights[maximizer_secondGPIndex] < p.groundHeightSecondGP + Movement.MIN_GP_HEIGHT) {
                iterations++;
                //Debug.println("Initial: " + p.initialFrames + " " + efficiencies[lastFrames[0]]);
                //Debug.println("Dive Length: " + preset[diveCapBounceIndex - 1][1] + " " + efficiencies[lastFrames[diveCapBounceIndex]]);
                double initialEfficiency = efficiencies[lastFrames[0]];
                if (p.initialFrames <= VectorCalculator.initialMovement.getMinFrames())
                    initialEfficiency = Double.MAX_VALUE;
                double diveCapBounceEfficiency = efficiencies[lastFrames[diveCapBounceIndex]];
                double finalCapThrowEfficiency = efficiencies[lastFrames[finalCapThrowIndex]];
                if (initialEfficiency < diveCapBounceEfficiency && initialEfficiency < finalCapThrowEfficiency) {
                    p.initialFrames--;
                    lastFrames[0]--;
                }
                else if (diveCapBounceEfficiency < initialEfficiency && diveCapBounceEfficiency < finalCapThrowEfficiency) {
                    preset[diveCapBounceIndex - 1][1]--;
                    lastFrames[diveCapBounceIndex]--;
                    if (preset[diveCapBounceIndex - 1][1] < 1)
                        return false;
                }
                else {
                    preset[finalCapThrowIndex - 1][1]--;
                    lastFrames[finalCapThrowIndex]--;
                    if (preset[finalCapThrowIndex - 1][1] < 8)
                        return false;
                }
                presetMaximizer.movementFrames.set(maximizer_initialMovementIndex, p.initialFrames);
                presetMaximizer.movementFrames.set(maximizer_capBounceIndex, preset[diveCapBounceIndex - 1][1]);
                final_y_heights = getFinalYHeights(presetMaximizer);
                if (iterations % REFRESH_RATE == 0) {
                    VectorCalculator.addPreset(preset);
                    presetMaximizer = VectorCalculator.getMaximizer();
                    calcFrameByFrame(presetMaximizer);
                }
            }
        }

        Debug.println(Arrays.toString(final_y_heights));
        VectorCalculator.addPreset(preset);

        hasRCV = p.initialMovementName.contains("RCV");

        VectorMaximizer initialMaximizer = VectorCalculator.getMaximizer();
        calcFrameByFrame(initialMaximizer);

        Debug.println("Initial End Y Position: " + y);

        Debug.println(Arrays.toString(final_y_heights));

        Debug.println("Initial Durations: " + Arrays.toString(durations));
        Debug.println("Initial Last Frames: " + Arrays.toString(lastFrames));

        //remove the frames with the weakest efficiencies until Mario's height is above the target y position
        //this gives a ballpark estimate of the optimal frames
        
        int iterations = 0;
        while (y + p.getUpwarpMinusError() < p.y1 - ERROR) {
            iterations++;
            double worstEfficiency = 2;
            int worstEfficiencyIndex = 0;
            for (int i = 0; i < lastFrames.length; i++) {
                if (i != rainbowSpinIndex && i != homingMCCTIndex && efficiencies[lastFrames[i]] < worstEfficiency) {
                    if (i != 0 || (i == 0 && durations[0] > VectorCalculator.initialMovement.getMinFrames())) //only shorten initial movement if it can be shortened
                    worstEfficiency = efficiencies[lastFrames[i]];
                    worstEfficiencyIndex = i;
                }
            }
            //Debug.println("Worst Efficiency: " + worstEfficiency + " of movement index " + worstEfficiencyIndex);
            if (worstEfficiency == 2) { //we are now cutting positive y-velocity frames so the jump height is too high to make
                p.durationFrames = true;
                return false;
            }
            y -= y_vels[lastFrames[worstEfficiencyIndex]];
            durations[worstEfficiencyIndex]--;
            lastFrames[worstEfficiencyIndex]--;
            //we can keep the "removed" frames in the arrays, as they won't be considered anymore
            p.initialFrames = durations[0];
            for (int i = 0; i < preset.length; i++) {
                preset[i][1] = durations[i + 1];
            }
            if (iterations % REFRESH_RATE == 0) {
                VectorCalculator.addPreset(preset);
                initialMaximizer = VectorCalculator.getMaximizer();
                calcFrameByFrame(initialMaximizer);
            }
        }

        VectorCalculator.addPreset(preset);
        initialMaximizer = VectorCalculator.getMaximizer();         
        calcFrameByFrame(initialMaximizer);

        //if the movement is an rcv and we are solving for initial angle, figure out what initial angle should be used
        if (hasRCV && p.solveForInitialAngle) {
            p.initialAngle += p.targetAngle - firstFrameVelocityAngle;
            Debug.println(firstFrameVelocityAngle);
        }
        initialMaximizer = VectorCalculator.getMaximizer();         
        calcFrameByFrame(initialMaximizer);

        //calculate the y displacement of each piece of movement
        y_disps = new double[preset.length + 1];
        for (int i = 0; i < preset.length + 1; i++) {
            y_disps[i] = 0;
            int firstFrame = 0;
            if (i > 0)
                firstFrame = lastFrames[i - 1] + 1; //sometimes it's + 2 if there is a ground pound but ground pounds have 0 vertical speed anyway
            int lastFrame = lastFrames[i];
            for (int j = firstFrame; j <= lastFrame; j++)
                y_disps[i] += y_vels[j];
        }
        Debug.println("Ballpark Y Disps: " + Arrays.toString(y_disps));

        Debug.println("Ballpark Durations: " + Arrays.toString(durations));
        Debug.println("Ballpark Last Frames: " + Arrays.toString(lastFrames));
        Debug.println("Ballpark Y Height: " + y);

        //create a new maximizer with some extra frames so the testing works
        p.initialFrames = durations[0] + delta;
        for (int i = 0; i < preset.length; i++) {
            preset[i][1] = durations[i + 1];
            if (!(preset[i][0] == VectorCalculator.RS || preset[i][0] == VectorCalculator.HMCCT)) {
                preset[i][1] += delta;
            }
        }
        VectorCalculator.addPreset(preset);
        VectorMaximizer testMaximizer = VectorCalculator.getMaximizer();         
        calcFrameByFrame(testMaximizer);

        Debug.println("Test Start Durations: " + Arrays.toString(durations));
        Debug.println("Test Start Last Frames: " + Arrays.toString(lastFrames));
        //Debug.println(Arrays.toString(y_vels));

        for (int i = 0; i < delta; i++) { //the new last frames are different
            for (int j = 0; j < preset.length + 1; j++) {
                if (j != rainbowSpinIndex && j != homingMCCTIndex) {
                    y -= y_vels[lastFrames[j]];
                    lastFrames[j]--;
                    durations[j]--;
                }
            }
        }

        Debug.println("Test Durations: " + Arrays.toString(durations));
        Debug.println("Test Last Frames: " + Arrays.toString(lastFrames));
        Debug.println("Test Y Height: " + y);

        // if (final_y_heights[0] != 0) { //breakpoint
        //     return false;
        // }

        //Debug.println(test(durations));

        //test cap throw and dive combinations that will be used using the ballpark durations

        System.out.println("Solver: Testing Dive Durations");
        
        int[] testDurations = durations.clone();
        int maxCTDuration = durations[diveCapBounceIndex - 2] + delta;
        int maxDiveDuration = durations[diveCapBounceIndex - 1] + delta;
        ctTypes = new int[maxCTDuration + 1][maxDiveDuration + 1];
        diveDecels = new double[maxCTDuration + 1][maxDiveDuration + 1];
        edgeCBAngles = new double[maxCTDuration + 1][maxDiveDuration + 1];
        diveTurns = new boolean[maxCTDuration + 1][maxDiveDuration + 1];
        for (int i = -delta; i <= delta; i++) {
            for (int j = -delta; j <= delta; j++) {
                int ctDuration = durations[diveCapBounceIndex - 2] + i;
                int diveDuration = durations[diveCapBounceIndex - 1] + j;
                testDurations[diveCapBounceIndex - 2] = ctDuration;
                testDurations[diveCapBounceIndex - 1] = diveDuration;
                setDurations(testDurations);
                boolean testNoDiveTurn = (dtAllowed == TurnDuringDive.NO || (dtAllowed == TurnDuringDive.TEST && !hasRCV));
                if (dtAllowed != TurnDuringDive.NO && testCT(-1, .02, .1, true, true) >= 0) { //test quick and dirty first just to figure out if it is possible
                    //testCT(ctType, .01, .01, false); //only test with smaller increment if it's already possible with larger increment
                    //System.out.println("Possible: " + ctDuration + " " + diveDuration);
                    ctTypes[ctDuration][diveDuration] = ctType;
                    diveDecels[ctDuration][diveDuration] = diveDecel;
                    edgeCBAngles[ctDuration][diveDuration] = edgeCBAngle;
                    diveTurns[ctDuration][diveDuration] = true;
                }
                else if (testNoDiveTurn && testCT(-1, .1, 1, true, false) >= 0) { //now test without turning the dive (don't with RCVs because these can never be optimal for them)
                    ctTypes[ctDuration][diveDuration] = ctType;
                    diveDecels[ctDuration][diveDuration] = diveDecel;
                    edgeCBAngles[ctDuration][diveDuration] = edgeCBAngle;
                    diveTurns[ctDuration][diveDuration] = false;
                    //Debug.println("Wahoo");
                }
                else {
                    ctTypes[ctDuration][diveDuration] = -1;
                }
            }
        }
        //Debug.printArray(ctTypes);
        //Debug.printArray(diveTurns);
        //Debug.printArray(diveDecels);

        System.out.println("Solver: Finding Optimal Duration Candidates");
        lastAlertTime = System.currentTimeMillis();

        bestResults = new ArrayList<DoubleIntArray>();
        bestResults.add(new DoubleIntArray(0, durations));

        //now test adding and subtracting some frames to get a better result
        p.durationFrames = true;
        bestDurations = test(durations, delta, 0, p.y0).intArray;
        bestDisp = test(bestDurations, true, false);
        //Debug.println(test(best.intArray));

        Debug.println("Best Results " + 0 + ": " + bestDisp);
        Debug.println(Arrays.toString(bestDurations));

        System.out.println("Solver: Testing Optimal Duration Candidates");

        //test the runner-ups in more detail to see if any are actually better
        for (int i = 1; i < bestResults.size(); i++) {
            testDurations = bestResults.get(i).intArray;
            double testDisp = test(testDurations, true, false);
            if (testDisp > bestDisp) {
                bestDisp = testDisp;
                bestDurations = testDurations;
            }
            Debug.println("Best Results " + i + ": " + testDisp);
            Debug.println(Arrays.toString(testDurations));
        }
        test(bestDurations, true, hasRCV); //run again to bring the best result to present and also to adjust the initial angle in the case of an RCV

        int[] deltas = new int[durations.length];
        int maxDelta = 0;
        for (int i = 0; i < durations.length; i++) {
            maxDelta = Math.max(maxDelta, Math.abs(durations[i] - bestDurations[i]));
            deltas[i] = bestDurations[i] - durations[i];
        }
        //double bestDisp = test(bestDurations, false); //for bestydisp debug

        Debug.println("Best Disp: " + bestDisp);
        if (bestDisp == 0) {
            return false;
        }
        // Debug.println("Delta: " + delta);
        //Debug.println("Max Delta: " + maxDelta);
        Debug.println("Deltas: " + Arrays.toString(deltas) + ", " + bestYDisp);
        //Debug.println("Durations: " + Arrays.toString(best.intArray) + ", " + bestYDisp);
        Debug.println("Iterations: " + iterations);
        Debug.println("Inner Calls: " + innerCalls);
        // Debug.println("Bad Calls: " + badCalls);
        System.out.println("Solver: Calculated in " + (System.currentTimeMillis() - startTime) + " ms");
    
        return true;
    }

    public void calcFrameByFrame(VectorMaximizer maximizer) {
        maximizer.maximize();
        maximizer.adjustToGivenAngle();

        durations = new int[preset.length + 1]; //the durations that a user would enter
        lastFrames = new int[preset.length + 1]; //the last frames of each motion taking into account added frames for ground pounds, crouches, moonwalks, etc.
        int currentMotionLastFrame = VectorCalculator.lastInitialMovementFrame;
        durations[0] = p.initialFrames;
        lastFrames[0] = currentMotionLastFrame;
        for (int i = 0; i < preset.length; i++) {
            durations[i + 1] = preset[i][1];
            currentMotionLastFrame += preset[i][1];
            if (preset[i][0] == VectorCalculator.DIVE) {
                currentMotionLastFrame++; //the ground pound adds a frame
            }
            lastFrames[i + 1] = currentMotionLastFrame;
        }

        // Debug.println("Initial Durations: " + Arrays.toString(durations));
        // Debug.println("Initial Frames: " + Arrays.toString(lastFrames));

        // Debug.println("Rainbow Spin Index: " + rainbowSpinIndex);
        // Debug.println("Homing MCCT Index: " + homingMCCTIndex);

        //calculate efficiencies from every frame of the jump
        y_vels = new double[currentMotionLastFrame + 1];
        efficiencies = new double[currentMotionLastFrame + 1];
        final_y_heights = new double[preset.length + 1];

        x = p.x0;
        y = p.y0;
        z = p.z0;

        SimpleMotion[] simpleMotions = maximizer.getMotions();

        int row = 0;
		for (int index = 0; index < simpleMotions.length; index++) {
			SimpleMotion motion = simpleMotions[index];
			if (motion.frames == 0) {
				continue;
			}
			motion.calcDisp();
			motion.setInitialCoordinates(x, y, z);
			info = motion.calcFrameByFrame(); //seems inefficient because not all these values are needed
			if (index == 0 && info.length > 0) {
                firstFrameVelocityAngle = Math.toDegrees(p.xAxisZeroDegrees ? Math.atan2(info[0][5], info[0][3]) : Math.atan2(info[0][3], info[0][5]));
            }
            for (int i = 0; i < info.length; i++, row++) {
                y_vels[row] = info[i][4];
                //y_heights[row] = info[i][1];
				if (info[i][4] < 0) { //how efficient the jump is
					double speedInTargetDirection = info[i][6] * Math.cos(Math.atan2(info[i][3], info[i][5]) - maximizer.targetAngle);
					efficiencies[row] = -1 / ((info[i][4] / speedInTargetDirection) - 1); //ranges from 0 to 1
                    //maybe a more efficient calculation?
				}
                else {
                    efficiencies[row] = 2; //very efficient since it doesn't lose any height
                }
			}
			x = info[info.length - 1][0];
			y = info[info.length - 1][1];
			z = info[info.length - 1][2];
            for (int i = 0; i < lastFrames.length; i++) {
                if (row == lastFrames[i] + 1) {
                    final_y_heights[i] = y;
                }
            }
		}
    }

    public int testCT(int throwType, double edgeCBAngleIncrement, double firstFrameDecelIncrement, boolean zeroAngleTolerance, boolean diveTurn) {
        double userTolerance = p.diveCapBounceTolerance;
        VectorMaximizer ballparkMaximizer = VectorCalculator.getMaximizer();
        if (diveTurn) {
            VectorCalculator.setProperty(Parameter.dive_turn, "Yes");
            p.diveCapBounceAngle = 18;
        }
        else {
            VectorCalculator.setProperty(Parameter.dive_turn, "No");
            ballparkMaximizer.edgeCBMin = 6;
            p.diveCapBounceAngle = 9;
            ballparkMaximizer.edgeCBMax = 12;
        }
        ballparkMaximizer.maximize_HCT_limit = Math.toRadians(8);
        //ballparkMaximizer.maxRCVNudges = 5;
        ballparkMaximizer.firstFrameDecelIncrement = firstFrameDecelIncrement;
        p.diveFirstFrameDecel = 0;
        if (zeroAngleTolerance)
            p.diveCapBounceTolerance = 0;
        ballparkMaximizer.edgeCBAngleIncrement = edgeCBAngleIncrement;
        ballparkMaximizer.maximize();
        ctType = ballparkMaximizer.isDiveCapBouncePossible(throwType, singleThrowAllowed, false, ttAllowed != TripleThrow.YES, !singleThrowAllowed, ttAllowed != TripleThrow.NO);
        diveDecel = ballparkMaximizer.firstFrameDecel;
        edgeCBAngle = ballparkMaximizer.diveCapBounceAngle;
        p.diveCapBounceTolerance = userTolerance;
        // if (ctType > -1) {
        //     Debug.println("CT Type: " + ctType + " " + preset[0][1] + " " + preset[1][1]);
        // }
        return ctType;
    }

    public class DoubleIntArray {
        double d;
        int[] intArray;

        public DoubleIntArray(double d, int[] intArray) {
            this.d = d;
            this.intArray = intArray;
        }
    }

    public DoubleIntArray test(int[] durations, int delta, int index, double y_pos) {
        if (System.currentTimeMillis() - lastAlertTime >= 2000) {
            System.out.println("Solver: Working...");
            lastAlertTime = System.currentTimeMillis();
        }
        if (index == durations.length - 1) {
            innerCalls++;
        }
        DoubleIntArray best = new DoubleIntArray(0, durations);
        if (index == rainbowSpinIndex || (index == homingMCCTIndex && p.hctCapReturnFrame >= 36)) {
            return test(durations, delta, index + 1, y_pos + y_disps[index]);
        }
        if (index < durations.length - 1) {
            for (int i = -delta; i <= delta; i++) {
                int testDuration = durations[index] + i;
                if (index == homingMCCTIndex && (testDuration < p.hctCapReturnFrame || testDuration > 36)) {
                    Debug.println("Skipping HMCCT duration " + testDuration);
                    continue;
                }
                if (index == diveCapBounceIndex && testDuration > cbDurationLimit) {
                    continue;
                }
                double test_y_pos = y_pos + y_disps[index];
                int[] testDurations = durations.clone();
                testDurations[index] = testDuration;
                DoubleIntArray result = new DoubleIntArray(0, testDurations);
                int lastFrame = lastFrames[index];
                if (i < 0) {
                    for (int j = 0; j > i; j--) {
                        test_y_pos -= y_vels[lastFrame + j];
                    }
                }
                else if (i > 0) {
                    for (int j = 1; j <= i; j++) {
                        test_y_pos += y_vels[lastFrame + j]; //something is broken here
                    }
                }
                test_y_pos = validateHeight(index, test_y_pos, y_vels[lastFrame + i], testDurations);
                //Debug.println("Index " + index + " Delta " + i + " YPos " + test_y_pos);
                if (test_y_pos == FALSE) { //we were too low with respect to the ground
                    result = new DoubleIntArray(0, testDurations);
                }
                else if (index == diveCapBounceIndex - 1) { //make sure ct dive into cb is possible
                    int ctType = ctTypes[testDurations[index - 1]][testDurations[index]];
                    if (ctType == -1) {
                        result = new DoubleIntArray(0, testDurations);
                    }
                    else {
                        //Debug.println("Passed: (" + testDurations[index - 1] + ", " + testDurations[index] + ")");
                        result = test(testDurations, delta, index + 1, test_y_pos);
                    }
                }
                else {
                    result = test(testDurations, delta, index + 1, test_y_pos);
                }
                // }
                if (result.d > best.d) {
                    best = result;
                }
            }
            return best;
        }
        else { //make the final movement as low as it can be
            //Debug.println("Start Y Pos: " + y_pos);
            double base_y_pos = y_pos + y_disps[index];
            double test_y_pos = base_y_pos;
            int test_delta = 0;
            int lastFrame = lastFrames[index];
            //Debug.println("Base Y Pos: " + base_y_pos);
            if (base_y_pos + p.getUpwarpMinusError() > p.y1) {
                // Debug.println();
                // Debug.println(test_y_pos);
                for (test_delta = 1; test_delta <= delta; test_delta++) {
                    test_y_pos += y_vels[lastFrame + test_delta];
                    // Debug.println(test_y_pos);
                    if (test_y_pos + p.getUpwarpMinusError() < p.y1 - ERROR) {
                        test_y_pos -= y_vels[lastFrame + test_delta];
                        break;
                    }
                }
                test_delta--;
            }
            else if (base_y_pos + p.getUpwarpMinusError() < p.y1 - ERROR) {
                for (test_delta = 1; test_delta <= delta; test_delta++) {
                    test_y_pos -= y_vels[lastFrame - test_delta + 1];
                    if (test_y_pos + p.getUpwarpMinusError() >= p.y1 - ERROR) {
                        break;
                    }
                }
                test_delta *= -1; //so we subtract the frames instead of adding them
                if (test_y_pos + p.getUpwarpMinusError() < p.y1 - ERROR) { //not possible to be high enough so just return 0
                    return new DoubleIntArray(0, durations);
                }
            }
            if (test_y_pos + p.getUpwarpMinusError() > p.y1 + limit) { //almost certainly won't be optimal
                return new DoubleIntArray(0, durations);
            }
            // Debug.println();
            
            int[] testDurations = durations.clone();
            testDurations[index] = durations[index] + test_delta;
            DoubleIntArray result = new DoubleIntArray(test(testDurations, false, false), testDurations);
            //Debug.println(Arrays.toString(testDurations) + ", " + test_y_pos + ", " + result.d);
            double currentBest = bestResults.get(0).d;
            if (result.d > 0) {
                if (result.d > currentBest) {
                    // if (result.d >= currentBest + 1 || currentBest == 0) { //clearly better
                    //     bestResults.clear();
                    // }
                    for (int i = bestResults.size() - 1; i >= 0; i--) { //remove ones it is clearly better than
                        if (result.d >= bestResults.get(i).d + bestResultsRange) {
                            bestResults.remove(i);
                        }
                    }
                    bestResults.add(0, result);
                }
                else if (result.d >= currentBest - bestResultsRange) {
                    bestResults.add(result);
                }
            }
            return result;
        }
    }

    public double test(int[] testDurations, boolean fullAccuracy, boolean adjustInitialAngle) {
        iterations++;
        boolean possible = true;

        int ctDuration = testDurations[diveCapBounceIndex - 2];
        int diveDuration = testDurations[diveCapBounceIndex - 1];

        setDurations(testDurations);
        maximizer = VectorCalculator.getMaximizer();
        //if (p.groundTypeFirstGP != GroundType.NONE || p.groundTypeCB != GroundType.NONE || p.groundTypeSecondGP != GroundType.NONE) {
        double dispY = validateHeights(testDurations, maximizer);
        if (dispY == FALSE) {
            return 0.0;
        }
        //}
        bestYDisp = dispY; //for debugging

        if (diveTurns[ctDuration][diveDuration]) {
            VectorCalculator.setProperty(Parameter.dive_turn, "Yes");
        }
        else {
            VectorCalculator.setProperty(Parameter.dive_turn, "No");
            maximizer.edgeCBMin = 6;
            maximizer.edgeCBMax = 12;
        }

        if (!fullAccuracy) {
            maximizer.maximize_HCT_limit = Math.toRadians(8);
            maximizer.maxRCVNudges = 5;
            maximizer.maxRCVFineNudges = 1;
        }
        if (diveCapBounceIndex >= 0) {
            p.diveFirstFrameDecel = diveDecels[ctDuration][diveDuration];
            p.diveCapBounceAngle = edgeCBAngles[ctDuration][diveDuration];
        }
        double disp = maximizer.maximize();
        if (fullAccuracy) {
            if (maximizer.isDiveCapBouncePossible(-1, singleThrowAllowed, false, ttAllowed != TripleThrow.YES, !singleThrowAllowed, ttAllowed != TripleThrow.NO) > -1) { //also conforms the motion correctly
                maximizer.recalculateDisps();
                disp = maximizer.bestDisp;
            }
            else {
                Debug.println("Not actually possible: " + Arrays.toString(testDurations));
                return 0.0;
            }
        }
        if (adjustInitialAngle) {
            calcFrameByFrame(maximizer);
            if (hasRCV && p.solveForInitialAngle) {
                p.initialAngle += p.targetAngle - firstFrameVelocityAngle;
                Debug.println(firstFrameVelocityAngle);
            }
            return test(testDurations, fullAccuracy, false);
        }
        return disp;
    }

    //if height is not possible because of the ground, returns FALSE constant
    //otherwise returns the height, adjusted if the ground was touched
    public static final double FALSE = -Double.MAX_VALUE;
    public double validateHeight(int motionIndex, double y_pos, double y_vel, int[] durations) {
        double yDiff = -Double.MAX_VALUE;
        if (motionIndex == 0) { //initial movement
            //Debug.println(y_pos);
            if (p.groundTypeFirstGP == GroundType.NONE)
                return y_pos;
            else if (p.groundTypeFirstGP == GroundType.DAMAGING && y_pos <= p.groundHeightFirstGP)
                return FALSE;
            else
                yDiff = p.groundHeightFirstGP - y_pos;
            //Debug.println(yDiff);
        }
        else if (motionIndex == homingMCCTIndex || motionIndex == homingTTIndex) { //failures here will be caught by the rainbow spin because it is even lower
            return y_pos;
        }
        else if (motionIndex == rainbowSpinIndex) {
            if (rainbowSpinIndex < diveCapBounceIndex) { //rainbow spin first
                if (p.groundTypeFirstGP == GroundType.NONE)
                    return y_pos;
                else if (p.groundTypeFirstGP == GroundType.DAMAGING && y_pos <= p.groundHeightFirstGP)
                    return FALSE;
                else
                    yDiff = p.groundHeightFirstGP - y_pos;
            }
            else { //rainbow spin second
                if (p.groundTypeSecondGP == GroundType.NONE)
                    return y_pos;
                else if (p.groundTypeSecondGP == GroundType.DAMAGING && y_pos <= p.groundHeightSecondGP)
                    return FALSE;
                else
                    yDiff = p.groundHeightSecondGP - y_pos;
            }
        }
        else if (motionIndex == firstDiveIndex - 1) { //first CT
            if (p.groundTypeFirstGP == GroundType.NONE || y_pos - y_vel >= p.groundHeightFirstGP + Movement.MIN_GP_HEIGHT)
                return y_pos;
            else
                return FALSE;
        }
        else if (motionIndex == firstDiveIndex) {
            if (p.groundTypeCB == GroundType.NONE || y_pos > p.groundHeightCB)
                return y_pos;
            else
                return FALSE;
        }
        else if (motionIndex == diveCapBounceIndex) {
            if (p.groundTypeSecondGP == GroundType.NONE)
                return y_pos;
            else if (p.groundTypeSecondGP == GroundType.DAMAGING && y_pos <= p.groundHeightSecondGP)
                return FALSE;
            else
                yDiff = p.groundHeightSecondGP - y_pos;
        }
        else if (motionIndex == secondDiveIndex - 1) { //second CT
            if (p.groundTypeSecondGP == GroundType.NONE || y_pos - y_vel >= p.groundHeightSecondGP + Movement.MIN_GP_HEIGHT) {
                return y_pos;
            }
            else
                return FALSE;
        }
        else if (motionIndex == secondDiveIndex) {
            if (y_pos + p.getUpwarpMinusError() >= p.y1 - ERROR)
                return y_pos;
            else
                return FALSE;
        }
        //handle cases above ground where you collided with it on the last frame
        if (yDiff < 0)
            return y_pos;
        if (yDiff < -y_vel) {
            return y_pos + yDiff;
        }
        else
            return FALSE;
    }
    
    //make sure the durations are actually possible
    public double validateHeights(int[] testDurations, VectorMaximizer maximizer) {
        double[] final_y_heights = getFinalYHeights(maximizer);
        if (final_y_heights[final_y_heights.length - 1] + p.getUpwarpMinusError() < p.y1 - ERROR) {
            return FALSE;
        }
        SimpleMotion[] motions = maximizer.getMotions();

        int maximizer_initialMovementIndex = -1;
        for (int i = 1; i < motions.length; i++) {
            if (Movement.isMidairCapThrow(motions[i].movement.movementType)) {
                maximizer_initialMovementIndex = i - 1;
                break;
            }
        }

        int maximizer_hmcctIndex = maximizer.variableHCTFallIndex;
        int maximizer_firstGPIndex;
        if (maximizer.hasVariableCapThrow1Falling)
            maximizer_firstGPIndex = maximizer.variableCapThrow1Index + 2;
        else
            maximizer_firstGPIndex = maximizer.variableCapThrow1Index + 1;
        int maximizer_firstDiveIndex = -1;
        int maximizer_capBounceIndex = -1;
        if (maximizer.movementNames.get(maximizer_firstGPIndex + 1).equals("Dive")) {
            maximizer_firstDiveIndex = maximizer_firstGPIndex + 1; //different than later firstDiveIndex (do note)
            maximizer_capBounceIndex = maximizer_firstDiveIndex + 1;
        }
        int maximizer_secondGPIndex;
        if (maximizer.hasVariableMovement2Falling)
            maximizer_secondGPIndex = maximizer.variableMovement2Index + 2;
        else
            maximizer_secondGPIndex = maximizer.variableMovement2Index + 1;
        int maximizer_rainbowSpinIndex = maximizer.rainbowSpinIndex;
        boolean rainbowSpinFirst = (rainbowSpinIndex < diveCapBounceIndex);

        double[] penultimate_y_heights = new double[final_y_heights.length]; //calculate penultimate heights
        for (int i = 0; i < final_y_heights.length; i++) {
            penultimate_y_heights[i] = final_y_heights[i] - motions[i].finalVerticalVelocity;
            double yDiff = 0;
            if (i == maximizer_initialMovementIndex && p.groundTypeFirstGP == GroundType.GROUND) { //account for cap throws executed very low to the ground, in which case all motion after is actaully higher than it would have been
                yDiff = p.groundHeightFirstGP - final_y_heights[i];
            }
            else if (i == maximizer_capBounceIndex && p.groundTypeSecondGP == GroundType.GROUND) { //account for cap throws executed very low to the ground, in which case all motion after is actaully higher than it would have been
                yDiff = p.groundHeightSecondGP - final_y_heights[i];
            }
            else if (maximizer.hasRainbowSpin && i == maximizer_rainbowSpinIndex + 1) { //account for cap throws executed very low to the ground, in which case all motion after is actaully higher than it would have been
                double groundHeightRS = -Double.MAX_VALUE;
                if (rainbowSpinFirst && p.groundTypeFirstGP == GroundType.GROUND)
                    groundHeightRS = p.groundHeightFirstGP;
                else if (p.groundTypeSecondGP == GroundType.GROUND)
                    groundHeightRS = p.groundHeightSecondGP;
                yDiff = groundHeightRS - final_y_heights[i];
            }
            if (yDiff > 0 && yDiff < -motions[i].finalVerticalVelocity) {
                //Debug.println("Before: " + Arrays.toString(final_y_heights));
                for (int j = i; j < final_y_heights.length; j++) {
                    final_y_heights[j] += yDiff;
                }
                //Debug.println("After: " + Arrays.toString(final_y_heights));
            }
        }
        if (p.hct) {
            double groundHeightRS = -Double.MAX_VALUE;
            if (rainbowSpinFirst && p.groundTypeFirstGP == GroundType.GROUND)
                groundHeightRS = p.groundHeightFirstGP;
            else if (p.groundTypeSecondGP == GroundType.GROUND)
                groundHeightRS = p.groundHeightSecondGP;
            if (final_y_heights[maximizer_hmcctIndex] < groundHeightRS + HCT_CHANGE_ANGLE_HEIGHT) {
                //Debug.println("Reducing HMCCT Angle");
                p.hctThrowAngle = HCT_SMALLER_ANGLE;
            }
            else {
                p.hctThrowAngle = 60;
            }
        }
        if (p.groundTypeFirstGP == GroundType.GROUND && penultimate_y_heights[maximizer_initialMovementIndex] <= p.groundHeightFirstGP) //maybe should be <
            return FALSE;
        if (p.groundTypeFirstGP == GroundType.DAMAGING && final_y_heights[maximizer_initialMovementIndex] <= p.groundHeightFirstGP) //maybe should be <
            return FALSE;
        if (maximizer.hasRainbowSpin && p.groundTypeFirstGP == GroundType.GROUND && rainbowSpinFirst && final_y_heights[maximizer_rainbowSpinIndex] <= p.groundHeightFirstGP) //actually the penultimate frame of the whole action but the final frame is a dive
            return FALSE;
        if (maximizer.hasRainbowSpin && p.groundTypeSecondGP == GroundType.GROUND && !rainbowSpinFirst && final_y_heights[maximizer_rainbowSpinIndex] <= p.groundHeightSecondGP)
            return FALSE;
        if (maximizer.hasRainbowSpin && p.groundTypeFirstGP == GroundType.DAMAGING && rainbowSpinFirst && final_y_heights[maximizer_rainbowSpinIndex + 1] <= p.groundHeightFirstGP) //actually the penultimate frame of the whole action but the final frame is a dive
            return FALSE;
        if (maximizer.hasRainbowSpin && p.groundTypeSecondGP == GroundType.DAMAGING && !rainbowSpinFirst && final_y_heights[maximizer_rainbowSpinIndex + 1] <= p.groundHeightSecondGP)
            return FALSE;
        if (p.groundTypeFirstGP != GroundType.NONE && penultimate_y_heights[maximizer_firstGPIndex - 1] < p.groundHeightFirstGP + Movement.MIN_GP_HEIGHT)
            return FALSE;
        if (p.groundTypeCB != GroundType.NONE && final_y_heights[maximizer_firstDiveIndex] <= p.groundHeightCB)
            return FALSE;
        if (p.groundTypeSecondGP == GroundType.GROUND && penultimate_y_heights[maximizer_capBounceIndex] <= p.groundHeightSecondGP)
            return FALSE;
        if (p.groundTypeSecondGP == GroundType.DAMAGING && final_y_heights[maximizer_capBounceIndex] <= p.groundHeightSecondGP)
            return FALSE;
        if (p.groundTypeSecondGP != GroundType.NONE && penultimate_y_heights[maximizer_secondGPIndex - 1] < p.groundHeightSecondGP + Movement.MIN_GP_HEIGHT)
            return FALSE;
        if (final_y_heights[final_y_heights.length - 1] + p.getUpwarpMinusError() < p.y1 - ERROR)
            return FALSE;
        return (final_y_heights[final_y_heights.length - 1] + p.getUpwarpMinusError() - p.y1);
    }

    //sets Vector Calculator to be using the current durations
    public void setDurations(int[] testDurations) {
        p.initialFrames = testDurations[0];
        VectorCalculator.setProperty(Parameter.initial_frames, testDurations[0]);
        int[][] midairs = preset.clone();
        for (int i = 0; i < preset.length; i++) {
            midairs[i][1] = testDurations[i + 1];
        }
        //Debug.printArray(midairs);
        VectorCalculator.addPreset(midairs);
    }
}