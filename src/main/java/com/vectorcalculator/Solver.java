package com.vectorcalculator;

import java.util.Arrays;
import java.util.Vector;

//this class finds the optimal durations for each midair input, given the target vertical displacement
public class Solver {

    static final double LIMIT = 2; //if the final y height of the test is above this number, assume it can't be optimal
    //this limit takes a while for TT jumps
    static final double ERROR = .0001; //acceptable amount of error on double addition/subtraction

    Properties p = Properties.p;

    int[][] preset;
    int[] durations;
    int[] lastFrames;
    double[] y_vels;
    double[] efficiencies;

    int rainbowSpinIndex = -1;
    int homingMCCTIndex = -1;

    int iterations = 0;
    int innerCalls = 0;
    int badCalls = 0;

    double bestYDisp = 0; //for debug

    public boolean solve(int delta) {
        long startTime = System.currentTimeMillis();

        //custom presets not yet supported
        if (p.currentPresetIndex == 0) {
            return false;
        }
        else {
            //first, select midair durations for a jump that's flat or downward (which are the midair presets)
            VectorCalculator.addPreset(p.currentPresetIndex);
        }
        int[][] unmodifiedPreset = VectorCalculator.midairPresets[p.currentPresetIndex];
        preset = new int[unmodifiedPreset.length][unmodifiedPreset[0].length];
        for (int i = 0; i < preset.length; i++) {
            preset[i][0] = unmodifiedPreset[i][0];
            if (preset[i][0] == VectorCalculator.RS) {
                rainbowSpinIndex = i + 1;
                preset[i][1] = unmodifiedPreset[i][1];
            }
            else if (preset[i][0] == VectorCalculator.HMCCT) {
                homingMCCTIndex = i + 1;
                preset[i][1] = unmodifiedPreset[i][1];
            }
            else {
                preset[i][1] = unmodifiedPreset[i][1] + delta;
            }
        } //start with all of the movements as low as they might end up so we can calculate falling displacements easier later
        VectorCalculator.addPreset(preset);

        //now set the initial movement so the whole jump ends up lower than the target y position
        //the spinless preset results in 129.2 height gain and 200 is bigger so Mario will always start lower (could have different numbers for each preset)
        p.initialDispY = p.y1 - p.y0 - 1000; //could maybe be less, but want to make sure all movements can be shortened a lot
        p.durationFrames = false;

        VectorMaximizer initialMaximizer = VectorCalculator.calculate();
        
        //find last frames of each piece of the movement
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

        double x = p.x0;
        double y = p.y0;
        double z = p.z0;

        SimpleMotion[] simpleMotions = initialMaximizer.getMotions();

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
				if (info[i][4] < 0) { //how efficient the jump is
					double speedInTargetDirection = info[i][6] * Math.cos(Math.atan2(info[i][3], info[i][5]) - initialMaximizer.getTargetAngle());
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
		}

        double y_target = p.y1;

        //System.out.println(Arrays.toString(efficiencies));

        //remove extra frames from adding them before to help with later calculations
        for (int i = 0; i < delta; i++) {
            for (int j = 0; j < preset.length + 1; j++) {
                if (j != rainbowSpinIndex && j != homingMCCTIndex) {
                    y -= y_vels[lastFrames[j]];
                    lastFrames[j]--;
                    durations[j]--;
                }
            }
        }

        // System.out.println("Updated Durations: " + Arrays.toString(durations));
        // System.out.println("Updated Last Frames: " + Arrays.toString(lastFrames));

        //remove the frames with the weakest efficiencies until Mario's height is above the target y position
        //this gives a ballpark estimate of the optimal frames
        
        while (y < y_target - ERROR) {
            double worstEfficiency = 2;
            int worstEfficiencyIndex = 0;
            for (int i = 0; i < lastFrames.length; i++) {
                if (i != rainbowSpinIndex && i != homingMCCTIndex && efficiencies[lastFrames[i]] < worstEfficiency) {
                    worstEfficiency = efficiencies[lastFrames[i]];
                    worstEfficiencyIndex = i;
                }
            }
            // System.out.println("Worst Efficiency: " + worstEfficiency + " of movement index " + worstEfficiencyIndex);
            if (worstEfficiency == 2) { //we are now cutting positive y-velocity frames so the jump height is too high to make
                p.durationFrames = true;
                return false;
            }
            y -= y_vels[lastFrames[worstEfficiencyIndex]];
            durations[worstEfficiencyIndex]--;
            lastFrames[worstEfficiencyIndex]--;
            //we can keep the "removed" frames in the arrays, as they won't be considered anymore
        }

        // System.out.println("Ballpark Durations: " + Arrays.toString(durations));
        // System.out.println("Ballpark Last Frames: " + Arrays.toString(lastFrames));
        // System.out.println("Ballpark Y Height: " + y);

        //System.out.println(test(durations));

        //now test adding and subtracting some frames to get a better result
        p.durationFrames = true;
        DoubleIntArray best = test(durations, delta, 0, y);
        test(best.intArray);
        //System.out.println(test(best.intArray));

        //possible improvements: some of these combinations will obviously not work
        //every part of the motion should just have precalculated displacement tables so that the appropriate displacements can be added on
        //one way to do this is to create a second maximizer at the start that has every input with the delta added on
        //split this into however many separate arrays for easier use, one for each motion
        //and then only check combos that have positive displacement
        //might also be best just to pick the options that are closest to the target displacement, as those are likely to be best
        //another option is to run worse versions of maximize() to start off with, and then for the best options run it again more accurately

        int[] deltas = new int[durations.length];
        int maxDelta = 0;
        for (int i = 0; i < durations.length; i++) {
            maxDelta = Math.max(maxDelta, Math.abs(durations[i] - best.intArray[i]));
            deltas[i] = best.intArray[i] - durations[i];
        }
        test(best.intArray); //for bestydisp debug

        // System.out.println("Delta: " + delta);
        //System.out.println("Max Delta: " + maxDelta);
        //System.out.println("Deltas: " + Arrays.toString(deltas) + ", " + bestYDisp);
        System.out.println("Durations: " + Arrays.toString(best.intArray) + ", " + bestYDisp);
        // System.out.println("Iterations: " + iterations);
        // System.out.println("Inner Calls: " + innerCalls);
        // System.out.println("Bad Calls: " + badCalls);
        //System.out.println("Calculated in " + (System.currentTimeMillis() - startTime) + " ms");
        

        return true;
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
            return test(durations, delta, index + 1, y_pos);
        }
        
        if (index < durations.length - 1) {
            for (int i = -delta; i <= delta; i++) {
                double test_y_pos = y_pos;
                int[] testDurations = durations.clone();
                testDurations[index] = durations[index] + i;
                DoubleIntArray result;
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
                //System.out.println(lastFrames[index] + ", " + y_vels[lastFrame + 1]);
                //System.out.println("Index " + index + " Delta " + i + " YPos " + test_y_pos);
                // if (index == durations.length - 1) {
                //     result = new DoubleIntArray(test(testDurations), testDurations);
                // }
                // else {
                result = test(testDurations, delta, index + 1, test_y_pos);
                // }
                if (result.d > best.d) {
                    best = result;
                }
            }
            return best;
        }
        else { //make the final movement as low as it can be
            double test_y_pos = y_pos;
            int test_delta = 0;
            int lastFrame = lastFrames[index];

            if (y_pos > p.y1) {
                // System.out.println();
                // System.out.println(test_y_pos);
                for (test_delta = 1; test_delta <= delta; test_delta++) {
                    test_y_pos += y_vels[lastFrame + test_delta];
                    // System.out.println(test_y_pos);
                    if (test_y_pos < p.y1 - ERROR) {
                        test_y_pos -= y_vels[lastFrame + test_delta];
                        break;
                    }
                }
                test_delta--;
            }
            else if (y_pos < p.y1 - ERROR) {
                for (test_delta = 1; test_delta <= delta; test_delta++) {
                    test_y_pos -= y_vels[lastFrame - test_delta + 1];
                    if (test_y_pos >= p.y1) {
                        break;
                    }
                }
                test_delta *= -1; //so we subtract the frames instead of adding them
                if (test_y_pos < p.y1 - ERROR) { //not possible to be high enough so just return 0
                    return new DoubleIntArray(0, durations);
                }
            }
            if (test_y_pos > p.y1 + LIMIT) { //almost certainly won't be optimal
                return new DoubleIntArray(0, durations);
            }
            // System.out.println();
            // System.out.println(test_y_pos);
            int[] testDurations = durations.clone();
            testDurations[index] = durations[index] + test_delta;
            // if (test_y_pos >= .19 && test_y_pos <= .21) {
            //      System.out.println(Arrays.toString(testDurations));
            //      System.out.println(test(testDurations));
            // }
            return new DoubleIntArray(test(testDurations), testDurations);
        }
    }

    public double test(int[] testDurations) {
        iterations++;
        p.initialFrames = testDurations[0];
        VectorCalculator.genPropertiesTable.setValueAt(testDurations[0], VectorCalculator.MOVEMENT_DURATION_ROW, 1);
        int[][] midairs = preset.clone();
        for (int i = 0; i < preset.length; i++) {
            midairs[i][1] = testDurations[i + 1];
        }
        VectorCalculator.addPreset(midairs);
        VectorMaximizer maximizer = VectorCalculator.getMaximizer();
        maximizer.alwaysDiveTurn = true;
        maximizer.maximize_HCT_limit = Math.toRadians(8);
        double disp = maximizer.maximize();
        double y = p.y0;
        SimpleMotion[] motions = maximizer.getMotions();
        if (VectorCalculator.stop) {
            System.out.println(Arrays.toString(testDurations));
            System.exit(-1);
        }
        for (SimpleMotion m : motions) {
            y += m.calcDispY();
        }
        // System.out.println(y);
        if (y < p.y1 - ERROR) { //too low so won't work
            badCalls++;
            System.out.println(Arrays.toString(testDurations) + ", " + y);
            return 0.0;
        }
        else {
            bestYDisp = y - p.y1;
            return disp;
        }
    }
}