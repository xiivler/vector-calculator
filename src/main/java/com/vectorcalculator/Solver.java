package com.vectorcalculator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import com.vectorcalculator.Properties.GroundType;

//this class finds the optimal durations for each midair input, given the target vertical displacement
public class Solver {

    //static final double limit = 20; 
    //this limit takes a while for TT jumps
    static final double ERROR = .0001; //acceptable amount of error on double addition/subtraction
    static final int REFRESH_RATE = 5; //after how many iterations the values are recaluclated when getting a ballpark estimate

    double limit = 20; //if the final y height of the test is above this number, assume it can't be optimal

    double bestResultsRange = 5; //range of values worse than the current best to still test in full

    boolean singleThrowAllowed = true;
    boolean ttAllowed = false;

    boolean hasRCV;

    Properties p = Properties.p;

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
    int diveCapBounceIndex = -1;

    int iterations = 0;
    int innerCalls = 0;
    int badCalls = 0;

    int delta = 0;

    double bestYDisp = 0; //for debug
    
    double x;
    double y;
    double z;

    ArrayList<DoubleIntArray> bestResults;

    public double[] getFinalYHeights(VectorMaximizer maximizer) {
        maximizer.calcYDisps();
        SimpleMotion[] motions = maximizer.getMotions();
        //System.out.println(motions.length);
        double y = p.y0 /* + VectorCalculator.getMoonwalkDisp() */;
        final_y_heights = new double[motions.length];
        for (int i = 0; i < motions.length; i++) {
            y += motions[i].dispY;
            final_y_heights[i] = y;
        }
        return final_y_heights;
    }

    public boolean solve(int delta) {
        //System.out.println(ctDivePossible[29][23]);
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

        if (VectorCalculator.chooseJumpFrames) {
            p.framesJump = 10;
            VectorCalculator.genPropertiesModel.setValueAt(p.framesJump, VectorCalculator.HOLD_JUMP_FRAMES_ROW, 1);
        }

        //custom presets not yet supported
        if (p.currentPresetIndex == 0) {
            return true;
        }
        else {
            //first, select midair durations for a jump that's flat or downward (which are the midair presets)
            VectorCalculator.addPreset(p.currentPresetIndex);
        }
        if (VectorCalculator.midairPresetNames[p.currentPresetIndex].equals("CBV First (Triple Throw)"))
            singleThrowAllowed = false;
        else
            singleThrowAllowed = true;
        int[][] unmodifiedPreset = VectorCalculator.midairPresets[p.currentPresetIndex];
        preset = new int[unmodifiedPreset.length][unmodifiedPreset[0].length];

        //find locations of movements in the presets
        for (int i = 0; i < preset.length; i++) {
            preset[i][0] = unmodifiedPreset[i][0];
            preset[i][1] = unmodifiedPreset[i][1];
            if (preset[i][0] == VectorCalculator.RS)
                rainbowSpinIndex = i + 1;
            else if (preset[i][0] == VectorCalculator.HMCCT)
                homingMCCTIndex = i + 1;
            else if (preset[i][0] ==  VectorCalculator.HTT)
                homingTTIndex = i + 1;
            else if (preset[i][0] == VectorCalculator.CB && i > 0 && preset[i - 1][0] == VectorCalculator.DIVE) {
                diveCapBounceIndex = i + 1;
                firstDiveIndex = i;
            }
            else if (i == preset.length - 1 && preset[i][0] == VectorCalculator.DIVE)
                secondDiveIndex = i + 1;
        } //start with all of the movements as low as they might end up so we can calculate falling displacements easier later

        //first, fix the preset so that it makes sense with the height of the ground
        Movement.onMoon = p.onMoon;
		//MovementNameListPreparer movementPreparer = new MovementNameListPreparer();
        //presetMaximizer.maximize();
        
        p.initialDispY = p.y1 - p.y0 - 1000;
        p.initialFrames = VectorCalculator.initialMotion.calcFrames(p.initialDispY - VectorCalculator.getMoonwalkDisp());

        p.durationFrames = true;

        // p.hasGroundUnderFirstGP = true;
        // p.hasGroundUnderCB = true;
        // p.hasGroundUnderSecondGP = true;
        // p.groundTypeFirstGP = GroundType.GROUND;
        // p.groundTypeCB = GroundType.GROUND;
        // p.groundTypeSecondGP = GroundType.GROUND;
        // p.groundUnderFirstGP = 0;
        // p.groundUnderCB = 0;
        // p.groundUnderSecondGP = 0;

        preset[diveCapBounceIndex - 1][1] = p.initialFrames; //make cap bounce also big to start (will be shortened later)
        preset[secondDiveIndex - 1][1] = p.initialFrames; //make final dive also big to start
        VectorCalculator.addPreset(preset);
        VectorMaximizer presetMaximizer = VectorCalculator.getMaximizer();

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
        int maximizer_secondDiveIndex = maximizer_secondGPIndex + 1;

        //shorten first movement until first GP isn't too low
        double[] final_y_heights = getFinalYHeights(presetMaximizer);
        if (p.groundTypeFirstGP != GroundType.NONE) {
            while (final_y_heights[maximizer_firstGPIndex] < p.groundUnderFirstGP + Movement.MIN_GP_HEIGHT) {
                p.initialFrames--;
                presetMaximizer.movementFrames.set(0, p.initialFrames);
                final_y_heights = getFinalYHeights(presetMaximizer);
            }
        } 
        if (p.groundTypeCB != GroundType.NONE) {
            while (final_y_heights[maximizer_firstDiveIndex] <= p.groundUnderCB) {
                p.initialFrames--;
                presetMaximizer.movementFrames.set(0, p.initialFrames);
                final_y_heights = getFinalYHeights(presetMaximizer);
            }
        }
        //shorten either first movement or cap bounce until second GP isn't too low
        presetMaximizer = VectorCalculator.getMaximizer();
        calcFrameByFrame(presetMaximizer);
        final_y_heights = getFinalYHeights(presetMaximizer);
        System.out.println(Arrays.toString(final_y_heights));
        if (p.groundTypeSecondGP != GroundType.NONE) {
            int iterations = 0;
            while (final_y_heights[maximizer_secondGPIndex] < p.groundUnderSecondGP + Movement.MIN_GP_HEIGHT) {
                iterations++;
                //System.out.println("Initial: " + p.initialFrames + " " + efficiencies[lastFrames[0]]);
                //System.out.println("Dive Length: " + preset[diveCapBounceIndex - 1][1] + " " + efficiencies[lastFrames[diveCapBounceIndex]]);
                if (efficiencies[lastFrames[0]] < efficiencies[lastFrames[diveCapBounceIndex]]) {
                    p.initialFrames--;
                    lastFrames[0]--;
                }
                else {
                    preset[diveCapBounceIndex - 1][1]--;
                    lastFrames[diveCapBounceIndex]--;
                }
                presetMaximizer.movementFrames.set(0, p.initialFrames);
                presetMaximizer.movementFrames.set(maximizer_capBounceIndex, preset[diveCapBounceIndex - 1][1]);
                final_y_heights = getFinalYHeights(presetMaximizer);
                if (iterations % REFRESH_RATE == 0) {
                    VectorCalculator.addPreset(preset);
                    presetMaximizer = VectorCalculator.getMaximizer();
                    calcFrameByFrame(presetMaximizer);
                }
            }
        }

        System.out.println(Arrays.toString(final_y_heights));
        VectorCalculator.addPreset(preset);

        hasRCV = p.initialMovementName.contains("RCV");

        VectorMaximizer initialMaximizer = VectorCalculator.getMaximizer();
        calcFrameByFrame(initialMaximizer);

        System.out.println("Initial End Y Position: " + y);

        //System.out.println(Arrays.toString(final_y_heights));

        double y_target = p.y1;

        System.out.println("Initial Durations: " + Arrays.toString(durations));
        System.out.println("Initial Last Frames: " + Arrays.toString(lastFrames));

        //remove the frames with the weakest efficiencies until Mario's height is above the target y position
        //this gives a ballpark estimate of the optimal frames
        
        int iterations = 0;
        while (y < y_target - ERROR) {
            iterations++;
            double worstEfficiency = 2;
            int worstEfficiencyIndex = 0;
            for (int i = 0; i < lastFrames.length; i++) {
                if (i != rainbowSpinIndex && i != homingMCCTIndex && efficiencies[lastFrames[i]] < worstEfficiency) {
                    worstEfficiency = efficiencies[lastFrames[i]];
                    worstEfficiencyIndex = i;
                }
            }
            //System.out.println("Worst Efficiency: " + worstEfficiency + " of movement index " + worstEfficiencyIndex);
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
        System.out.println("Ballpark Y Disps: " + Arrays.toString(y_disps));

        System.out.println("Ballpark Durations: " + Arrays.toString(durations));
        System.out.println("Ballpark Last Frames: " + Arrays.toString(lastFrames));
        System.out.println("Ballpark Y Height: " + y);

        //int[] ballparkDurations = durations.clone();
        //double ballparkY = y;

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

        System.out.println("Test Start Durations: " + Arrays.toString(durations));
        System.out.println("Test Start Last Frames: " + Arrays.toString(lastFrames));
        System.out.println(Arrays.toString(y_vels));

        //durations = ballparkDurations; //use the old durations still as the basis for testing though

        for (int i = 0; i < delta; i++) { //the new last frames are different
            for (int j = 0; j < preset.length + 1; j++) {
                if (j != rainbowSpinIndex && j != homingMCCTIndex) {
                    y -= y_vels[lastFrames[j]];
                    lastFrames[j]--;
                    durations[j]--;
                }
            }
        }

        System.out.println(y_vels[200]);
        System.out.println("Test Durations: " + Arrays.toString(durations));
        System.out.println("Test Last Frames: " + Arrays.toString(lastFrames));
        System.out.println("Test Y Height: " + y);

        // if (final_y_heights[0] != 0) { //breakpoint
        //     return false;
        // }

        //System.out.println(test(durations));

        //test cap throw and dive combinations that will be used using the ballpark durations
        
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
                if (testCT(-1, .02, .1, true, true) >= 0) { //test quick and dirty first just to figure out if it is possible
                    //testCT(ctType, .01, .01, false); //only test with smaller increment if it's already possible with larger increment
                    ctTypes[ctDuration][diveDuration] = ctType;
                    diveDecels[ctDuration][diveDuration] = diveDecel;
                    edgeCBAngles[ctDuration][diveDuration] = edgeCBAngle;
                    diveTurns[ctDuration][diveDuration] = true;
                }
                else if (!hasRCV && testCT(-1, .1, 1, true, false) >= 0) { //now test without turning the dive (don't with RCVs because these can never be optimal for them)
                    ctTypes[ctDuration][diveDuration] = ctType;
                    diveDecels[ctDuration][diveDuration] = diveDecel;
                    edgeCBAngles[ctDuration][diveDuration] = edgeCBAngle;
                    diveTurns[ctDuration][diveDuration] = false;
                    //System.out.println("Wahoo");
                }
                else {
                    ctTypes[ctDuration][diveDuration] = -1;
                }
            }
        }
        //Sandbox.printArray(ctTypes);
        //Sandbox.printArray(diveTurns);
        //Sandbox.printArray(diveDecels);
        //System.out.println("28 20 possible: " + ctTypes[28][20]);
        //System.out.println("28 26 dive decel: " + diveDecels[28][26]);

        bestResults = new ArrayList<DoubleIntArray>();
        bestResults.add(new DoubleIntArray(0, durations));

        //now test adding and subtracting some frames to get a better result
        p.durationFrames = true;
        int[] bestDurations = test(durations, delta, 0, p.y0).intArray;
        double bestDisp = test(bestDurations, true);
        //System.out.println(test(best.intArray));

        //test the runner-ups in more detail to see if any are actually better
        for (int i = 1; i < bestResults.size(); i++) {
            testDurations = bestResults.get(i).intArray;
            double testDisp = test(testDurations, true);
            if (testDisp > bestDisp) {
                bestDisp = testDisp;
                bestDurations = testDurations;
            }
            //System.out.println("Best Results " + i + ": " + testDisp);
            //System.out.println(Arrays.toString(testDurations));
        }
        test(bestDurations, true);

        int[] deltas = new int[durations.length];
        int maxDelta = 0;
        for (int i = 0; i < durations.length; i++) {
            maxDelta = Math.max(maxDelta, Math.abs(durations[i] - bestDurations[i]));
            deltas[i] = bestDurations[i] - durations[i];
        }
        //double bestDisp = test(bestDurations, false); //for bestydisp debug

        System.out.println("Best Disp: " + bestDisp);
        // System.out.println("Delta: " + delta);
        //System.out.println("Max Delta: " + maxDelta);
        System.out.println("Deltas: " + Arrays.toString(deltas) + ", " + bestYDisp);
        //System.out.println("Durations: " + Arrays.toString(best.intArray) + ", " + bestYDisp);
        // System.out.println("Iterations: " + iterations);
        // System.out.println("Inner Calls: " + innerCalls);
        // System.out.println("Bad Calls: " + badCalls);
        System.out.println("Calculated in " + (System.currentTimeMillis() - startTime) + " ms");
    
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

        // System.out.println("Initial Durations: " + Arrays.toString(durations));
        // System.out.println("Initial Frames: " + Arrays.toString(lastFrames));

        // System.out.println("Rainbow Spin Index: " + rainbowSpinIndex);
        // System.out.println("Homing MCCT Index: " + homingMCCTIndex);

        double[][] info = null;

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
			for (int i = 0; i < info.length; i++, row++) {
                y_vels[row] = info[i][4];
                //y_heights[row] = info[i][1];
				if (info[i][4] < 0) { //how efficient the jump is
					double speedInTargetDirection = info[i][6] * Math.cos(Math.atan2(info[i][3], info[i][5]) - maximizer.getTargetAngle());
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
            ballparkMaximizer.alwaysDiveTurn = true;
        }
        else {
            ballparkMaximizer.neverDiveTurn = true;
            ballparkMaximizer.edgeCBMin = 6;
            ballparkMaximizer.edgeCBMax = 12;
        }
        ballparkMaximizer.maximize_HCT_limit = Math.toRadians(8);
        //ballparkMaximizer.maxRCVNudges = 5;
        ballparkMaximizer.firstFrameDecelIncrement = firstFrameDecelIncrement;
        p.diveFirstFrameDecel = 0;
        p.diveCapBounceAngle = 18;
        if (zeroAngleTolerance)
            p.diveCapBounceTolerance = 0;
        ballparkMaximizer.edgeCBAngleIncrement = edgeCBAngleIncrement;
        ballparkMaximizer.maximize();
        ctType = ballparkMaximizer.isDiveCapBouncePossible(throwType, singleThrowAllowed, false, true, false, ttAllowed);
        diveDecel = ballparkMaximizer.firstFrameDecel;
        edgeCBAngle = ballparkMaximizer.diveCapBounceAngle;
        p.diveCapBounceTolerance = userTolerance;
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
        if (index == durations.length - 1) {
            innerCalls++;
        }
        DoubleIntArray best = new DoubleIntArray(0, durations);
        if (index == rainbowSpinIndex || index == homingMCCTIndex) {
            return test(durations, delta, index + 1, y_pos + y_disps[index]);
        }
        if (index < durations.length - 1) {
            for (int i = -delta; i <= delta; i++) {
                double test_y_pos = y_pos + y_disps[index];
                int[] testDurations = durations.clone();
                testDurations[index] = durations[index] + i;
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
                //System.out.println("Index " + index + " Delta " + i + " YPos " + test_y_pos);
                if (test_y_pos == FALSE) { //we were too low with respect to the ground
                    result = new DoubleIntArray(0, testDurations);
                }
                //System.out.println(lastFrames[index] + ", " + y_vels[lastFrame + 1]);
                //
                // if (index == durations.length - 1) {
                //     result = new DoubleIntArray(test(testDurations), testDurations);
                // }
                // else {
                else if (index == diveCapBounceIndex - 1) { //make sure ct dive into cb is possible
                    int ctType = ctTypes[testDurations[index - 1]][testDurations[index]];
                    //if (ctType == -1 || (!singleThrowAllowed && ctType == Movement.CT) ||
                    //    (!ttAllowed && (ctType == Movement.TT || ctType == Movement.TTU || ctType == Movement.TTD || ctType == Movement.TTL || ctType == Movement.TTR))) {
                    //    result = new DoubleIntArray(0, testDurations);
                    //}
                    if (ctType == -1) {
                        result = new DoubleIntArray(0, testDurations);
                    }
                    else {
                        //System.out.println("Passed: (" + testDurations[index - 1] + ", " + testDurations[index] + ")");
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
            //System.out.println("Start Y Pos: " + y_pos);
            double base_y_pos = y_pos + y_disps[index];
            double test_y_pos = base_y_pos;
            int test_delta = 0;
            int lastFrame = lastFrames[index];
            //System.out.println("Base Y Pos: " + base_y_pos);
            if (base_y_pos + p.upwarp > p.y1) {
                // System.out.println();
                // System.out.println(test_y_pos);
                for (test_delta = 1; test_delta <= delta; test_delta++) {
                    test_y_pos += y_vels[lastFrame + test_delta];
                    // System.out.println(test_y_pos);
                    if (test_y_pos + p.upwarp < p.y1 - ERROR) {
                        test_y_pos -= y_vels[lastFrame + test_delta];
                        break;
                    }
                }
                test_delta--;
            }
            else if (base_y_pos + p.upwarp < p.y1 - ERROR) {
                for (test_delta = 1; test_delta <= delta; test_delta++) {
                    test_y_pos -= y_vels[lastFrame - test_delta + 1];
                    if (test_y_pos + p.upwarp >= p.y1) {
                        break;
                    }
                }
                test_delta *= -1; //so we subtract the frames instead of adding them
                if (test_y_pos + p.upwarp < p.y1 - ERROR) { //not possible to be high enough so just return 0
                    return new DoubleIntArray(0, durations);
                }
            }
            if (test_y_pos + p.upwarp > p.y1 + limit) { //almost certainly won't be optimal
                return new DoubleIntArray(0, durations);
            }
            // System.out.println();
            
            int[] testDurations = durations.clone();
            testDurations[index] = durations[index] + test_delta;
            DoubleIntArray result = new DoubleIntArray(test(testDurations, false), testDurations);
            //System.out.println(Arrays.toString(testDurations) + ", " + test_y_pos + ", " + result.d);
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
        /* else {
            for (int test_delta = -delta; test_delta <= delta; test_delta++) {
                int[] testDurations = durations.clone();
                testDurations[index] = durations[index] + test_delta;
                DoubleIntArray result = new DoubleIntArray(test(testDurations, false), testDurations);
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
            }
            return bestResults.get(0);
        } */
    }

    public double test(int[] testDurations, boolean fullAccuracy) {
        iterations++;
        boolean possible = true;

        //System.out.println(Arrays.toString(testDurations));
        // p.initialFrames = testDurations[0];
        // VectorCalculator.genPropertiesTable.setValueAt(testDurations[0], VectorCalculator.MOVEMENT_DURATION_ROW, 1);
        // int[][] midairs = preset.clone();
        // for (int i = 0; i < preset.length; i++) {
        //     midairs[i][1] = testDurations[i + 1];
        // }
        // VectorCalculator.addPreset(midairs);
        int ctDuration = testDurations[diveCapBounceIndex - 2];
        int diveDuration = testDurations[diveCapBounceIndex - 1];

        setDurations(testDurations);
        VectorMaximizer maximizer = VectorCalculator.getMaximizer();
        //if (p.groundTypeFirstGP != GroundType.NONE || p.groundTypeCB != GroundType.NONE || p.groundTypeSecondGP != GroundType.NONE) {
        double dispY = validateHeights(testDurations, maximizer);
        if (dispY == FALSE) {
            return 0.0;
        }
        //}
        bestYDisp = dispY; //for debugging

        if (diveTurns[ctDuration][diveDuration]) {
            maximizer.alwaysDiveTurn = true;
        }
        else {
            maximizer.neverDiveTurn = true;
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
            if (maximizer.isDiveCapBouncePossible(ctTypes[ctDuration][diveDuration], singleThrowAllowed, false, true, false, ttAllowed) > -1) { //also conforms the motion correctly
                maximizer.recalculateDisps();
                disp = maximizer.bestDisp;
            }
            else {
                System.out.println("Not actually possible: " + Arrays.toString(testDurations));
                return 0.0;
            }
        }
        // double y = p.y0;
        // SimpleMotion[] motions = maximizer.getMotions();
        // if (VectorCalculator.stop) {
        //     System.out.println(Arrays.toString(testDurations));
        //     System.exit(-1);
        // }
        // for (SimpleMotion m : motions) {
        //     y += m.calcDispY();
        // }
        // // System.out.println(y);
        // if (y < p.y1 - ERROR || possible == false) { //too low so won't work
        //     badCalls++;
        //     System.out.println(Arrays.toString(testDurations) + ", " + y);
        //     return 0.0;
        // }
        // else {
        //bestYDisp = y - p.y1;
        return disp;
        // }
    }

    //if height is not possible because of the ground, returns FALSE constant
    //otherwise returns the height, adjusted if the ground was touched
    public static final double FALSE = -Double.MAX_VALUE;
    public double validateHeight(int motionIndex, double y_pos, double y_vel, int[] durations) {
        // if (durations[0] == 68) {
        //     System.out.println("68 made to index " + motionIndex);
        // }
        double yDiff = -Double.MAX_VALUE;
        if (motionIndex == 0) { //initial movement
            //System.out.println(y_pos);
            if (p.groundTypeFirstGP == GroundType.NONE)
                return y_pos;
            else if (p.groundTypeFirstGP == GroundType.DAMAGING && y_pos <= p.groundUnderFirstGP)
                return FALSE;
            else
                yDiff = p.groundUnderFirstGP - y_pos;
            //System.out.println(yDiff);
        }
        else if (motionIndex == homingMCCTIndex || motionIndex == homingTTIndex) { //failures here will be caught by the rainbow spin because it is even lower
            return y_pos;
        }
        else if (motionIndex == rainbowSpinIndex) {
            if (rainbowSpinIndex < diveCapBounceIndex) { //rainbow spin first
                if (p.groundTypeFirstGP == GroundType.NONE)
                    return y_pos;
                else if (p.groundTypeFirstGP == GroundType.DAMAGING && y_pos <= p.groundUnderFirstGP)
                    return FALSE;
                else
                    yDiff = p.groundUnderFirstGP - y_pos;
            }
            else { //rainbow spin second
                if (p.groundTypeSecondGP == GroundType.NONE)
                    return y_pos;
                else if (p.groundTypeSecondGP == GroundType.DAMAGING && y_pos <= p.groundUnderSecondGP)
                    return FALSE;
                else
                    yDiff = p.groundUnderSecondGP - y_pos;
            }
        }
        else if (motionIndex == firstDiveIndex - 1) { //first CT
            if (p.groundTypeFirstGP == GroundType.NONE || y_pos - y_vel >= p.groundUnderFirstGP + Movement.MIN_GP_HEIGHT)
                return y_pos;
            else
                return FALSE;
        }
        else if (motionIndex == firstDiveIndex) {
            if (p.groundTypeCB == GroundType.NONE || y_pos > p.groundUnderCB)
                return y_pos;
            else
                return FALSE;
        }
        else if (motionIndex == diveCapBounceIndex) {
            if (p.groundTypeSecondGP == GroundType.NONE)
                return y_pos;
            else if (p.groundTypeSecondGP == GroundType.DAMAGING && y_pos <= p.groundUnderSecondGP)
                return FALSE;
            else
                yDiff = p.groundUnderSecondGP - y_pos;
        }
        else if (motionIndex == secondDiveIndex - 1) { //second CT
            if (p.groundTypeSecondGP == GroundType.NONE || y_pos - y_vel >= p.groundUnderSecondGP + Movement.MIN_GP_HEIGHT) {
                // if (durations[0] == 68) {
                //     System.out.println("68 Passed index " + motionIndex);
                //     System.out.println(y_pos);
                // }
                return y_pos;
            }
            else
                return FALSE;
        }
        else if (motionIndex == secondDiveIndex) {
            if (y_pos + p.upwarp >= p.y1 - ERROR)
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
        if (final_y_heights[final_y_heights.length - 1] < p.y1 - p.upwarp) {
            return FALSE;
        }
        SimpleMotion[] motions = maximizer.getMotions();

        int maximizer_initialMovementIndex = -1;
        for (int i = 0; i < motions.length; i++) {
            if (motions[i].movement.movementType.contains("Cap Throw")) {
                maximizer_initialMovementIndex = i - 1;
                break;
            }
        }

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
                yDiff = p.groundUnderFirstGP - final_y_heights[i];
            }
            else if (i == maximizer_capBounceIndex && p.groundTypeSecondGP == GroundType.GROUND) { //account for cap throws executed very low to the ground, in which case all motion after is actaully higher than it would have been
                yDiff = p.groundUnderSecondGP - final_y_heights[i];
            }
            else if (maximizer.hasRainbowSpin && i == maximizer_rainbowSpinIndex + 1) { //account for cap throws executed very low to the ground, in which case all motion after is actaully higher than it would have been
                double groundUnderRS = -Double.MAX_VALUE;
                if (rainbowSpinFirst && p.groundTypeFirstGP == GroundType.GROUND)
                    groundUnderRS = p.groundUnderFirstGP;
                else if (p.groundTypeSecondGP == GroundType.GROUND)
                    groundUnderRS = p.groundUnderSecondGP;
                yDiff = groundUnderRS - final_y_heights[i];
            }
            if (yDiff > 0 && yDiff < -motions[i].finalVerticalVelocity) {
                //System.out.println("Before: " + Arrays.toString(final_y_heights));
                for (int j = i; j < final_y_heights.length; j++) {
                    final_y_heights[j] += yDiff;
                }
                //System.out.println("After: " + Arrays.toString(final_y_heights));
            }
        }

        // if (p.initialFrames == 68 || testDurations[0] == 68) {
        //     System.out.println("68 height w/ " + final_y_heights[maximizer_initialMovementIndex]);
        // }
        if (p.groundTypeFirstGP == GroundType.GROUND && penultimate_y_heights[maximizer_initialMovementIndex] <= p.groundUnderFirstGP) //maybe should be <
            return FALSE;
        if (p.groundTypeFirstGP == GroundType.DAMAGING && final_y_heights[maximizer_initialMovementIndex] <= p.groundUnderFirstGP) //maybe should be <
            return FALSE;
        if (maximizer.hasRainbowSpin && p.groundTypeFirstGP == GroundType.GROUND && rainbowSpinFirst && final_y_heights[maximizer_rainbowSpinIndex] <= p.groundUnderFirstGP) //actually the penultimate frame of the whole action but the final frame is a dive
            return FALSE;
        if (maximizer.hasRainbowSpin && p.groundTypeSecondGP == GroundType.GROUND && !rainbowSpinFirst && final_y_heights[maximizer_rainbowSpinIndex] <= p.groundUnderSecondGP)
            return FALSE;
        if (maximizer.hasRainbowSpin && p.groundTypeFirstGP == GroundType.DAMAGING && rainbowSpinFirst && final_y_heights[maximizer_rainbowSpinIndex + 1] <= p.groundUnderFirstGP) //actually the penultimate frame of the whole action but the final frame is a dive
            return FALSE;
        if (maximizer.hasRainbowSpin && p.groundTypeSecondGP == GroundType.DAMAGING && !rainbowSpinFirst && final_y_heights[maximizer_rainbowSpinIndex + 1] <= p.groundUnderSecondGP)
            return FALSE;
        if (p.groundTypeFirstGP != GroundType.NONE && penultimate_y_heights[maximizer_firstGPIndex - 1] < p.groundUnderFirstGP + Movement.MIN_GP_HEIGHT)
            return FALSE;
        if (p.groundTypeCB != GroundType.NONE && final_y_heights[maximizer_firstDiveIndex] <= p.groundUnderCB)
            return FALSE;
        if (p.groundTypeSecondGP == GroundType.GROUND && penultimate_y_heights[maximizer_capBounceIndex] <= p.groundUnderSecondGP)
            return FALSE;
        if (p.groundTypeSecondGP == GroundType.DAMAGING && final_y_heights[maximizer_capBounceIndex] <= p.groundUnderSecondGP)
            return FALSE;
        if (p.groundTypeSecondGP != GroundType.NONE && penultimate_y_heights[maximizer_secondGPIndex - 1] < p.groundUnderSecondGP + Movement.MIN_GP_HEIGHT)
            return FALSE;
        if (final_y_heights[final_y_heights.length - 1] + p.upwarp < p.y1 - ERROR)
            return FALSE;
        //System.out.println(Arrays.toString(final_y_heights) + " " + Arrays.toString(penultimate_y_heights));
        // if (p.initialFrames == 64) {
        //     System.out.println(Arrays.toString(testDurations));
        // }
        return (final_y_heights[final_y_heights.length - 1] + p.upwarp - p.y1);
    }

    //sets Vector Calculator to be using the current durations
    public void setDurations(int[] testDurations) {
        p.initialFrames = testDurations[0];
        VectorCalculator.genPropertiesTable.setValueAt(testDurations[0], VectorCalculator.MOVEMENT_DURATION_ROW, 1);
        int[][] midairs = preset.clone();
        for (int i = 0; i < preset.length; i++) {
            midairs[i][1] = testDurations[i + 1];
        }
        //Sandbox.printArray(midairs);
        VectorCalculator.addPreset(midairs);
    }
    
    public int[][] ctDivePossible = {{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, 6, 6, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, 6, 6, 6, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, 6, 6, 6, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, 6, 6, 6, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, 6, 6, 6, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, -1, -1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 7, -1, 0, 0, 0, 0, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 8, 7, -1, 0, 0, 0, 0, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 8, 6, -1, 0, 0, 0, 0, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 8, 6, 1, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 6, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 6, 1, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 6, 1, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 6, 1, 0, 0, 0, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 6, 4, 0, 0, 0, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 6, 7, 1, 0, 0, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 6, 7, 1, 0, 0, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 6, 7, 1, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 6, 4, 1, 1, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 6, 4, 1, 1, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 6, 4, 1, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 0, 6, -1, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 6, 7, -1, -1, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 4, 0, 6, -1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 6, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 6, -1, -1, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 0, 6, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 4, 0, 6, 6, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 0, -1, 6, 6, 4, 1, 1, 1, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 8, 8, 8, 0, 0, 1, 0, 0, 0, 0, 0, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 7, 6, 6, 6, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}};

    public double[][] firstFrameDecels = {{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.0, 0.5000000000000001, 0.19999999999999998, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.0, 0.5000000000000001, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.0, 0.0, 0.5000000000000001, 0.5000000000000001, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.0, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.4000000000000001, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.0, 0.0, 0.5000000000000001, 0.5000000000000001, 0.07500000000000001, 0.0, 0.5000000000000001, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.0, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.5000000000000001, 0.5000000000000001, 0.0, 0.0, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.4250000000000001, 0.47500000000000014, 0.5000000000000001, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.5000000000000001, 0.0, 0.0, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.5000000000000001, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}};
}