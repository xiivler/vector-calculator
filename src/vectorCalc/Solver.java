package vectorCalc;

import java.util.Arrays;
import java.util.Vector;

//this class finds the optimal durations for each midair input, given the target vertical displacement
public class Solver {

    int[][] preset;
    int[] durations;

    int rainbowSpinIndex;
    int homingMCCTIndex;

    public void solve() {
        //custom presets not yet supported
        if (VectorCalculator.currentPresetIndex == 0) {
            return;
        }
        else {
            //first, select midair durations for a jump that's flat or downward (which are the midair presets)
            VectorCalculator.addPreset(VectorCalculator.currentPresetIndex);
        }
        preset = VectorCalculator.midairPresets[VectorCalculator.currentPresetIndex];
        VectorMaximizer initialMaximizer = VectorCalculator.calculate();
        
        //find last frames of each piece of the movement
        durations = new int[preset.length + 1]; //the durations that a user would enter
        int[] lastFrames = new int[preset.length + 1]; //the last frames of each motion taking into account added frames for ground pounds, crouches, moonwalks, etc.
        int currentMotionLastFrame = VectorCalculator.lastInitialMovementFrame;
        rainbowSpinIndex = -1;
        homingMCCTIndex = -1;
        durations[0] = VectorCalculator.initialFrames;
        lastFrames[0] = currentMotionLastFrame;
        for (int i = 0; i < preset.length; i++) {
            durations[i + 1] = preset[i][1];
            currentMotionLastFrame += preset[i][1];
            if (preset[i][0] == VectorCalculator.DIVE) {
                currentMotionLastFrame++; //the ground pound adds a frame
            }
            else if (preset[i][0] == VectorCalculator.RS) {
                rainbowSpinIndex = i;
            }
            else if (preset[i][0] == VectorCalculator.HMCCT) {
                homingMCCTIndex = i;
            }
            lastFrames[i + 1] = currentMotionLastFrame;
        }

        //System.out.println(Arrays.toString(lastFrames));

        double x = VectorCalculator.x0;
        double y = VectorCalculator.y0;
        double z = VectorCalculator.z0;

        double[][] info = null;

        //calculate efficiencies from every frame of the jump
        double[] y_vels = new double[currentMotionLastFrame + 1];
        double[] efficiencies = new double[currentMotionLastFrame + 1];

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
					double speedInTargetDirection = info[i][6] * Math.cos(Math.atan2(info[i][5], info[i][3]) - Math.toRadians(VectorCalculator.targetAngle));
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

        double y_target = VectorCalculator.y1;

        //System.out.println(Arrays.toString(efficiencies));

        //remove the frames with the weakest efficiencies until Mario's height is above the target y position
        //this gives a ballpark estimate of the optimal frames
        //TODO figure out if this needs a tiny bit of wiggle room ex .0001
        while (y < y_target) {
            double worstEfficiency = 2;
            int worstEfficiencyIndex = 0;
            for (int i = 0; i < lastFrames.length; i++) {
                if (i != rainbowSpinIndex && i != homingMCCTIndex && efficiencies[lastFrames[i]] < worstEfficiency) {
                    worstEfficiency = efficiencies[lastFrames[i]];
                    worstEfficiencyIndex = i;
                }
            }
            y -= y_vels[lastFrames[worstEfficiencyIndex]];
            durations[worstEfficiencyIndex]--;
            lastFrames[worstEfficiencyIndex]--;
            //we can keep the "removed" frames in the arrays, as they won't be considered anymore
        }

        //System.out.println(test(durations));

        //now test adding and subtracting some frames to get a better result
        DoubleIntArray best = test(durations, 2, 0, durations.length - 1);
        System.out.println(test(best.intArray));

        //possible improvements: some of these combinations will obviously not work
        //every part of the motion should just have precalculated displacement tables so that the appropriate displacements can be added on
        //one way to do this is to create a second maximizer at the start that has every input with the delta added on
        //split this into however many separate arrays for easier use, one for each motion
        //and then only check combos that have positive displacement
        //might also be best just to pick the options that are closest to the target displacement, as those are likely to be best
        //another option is to run worse versions of maximize() to start off with, and then for the best options run it again more accurately
    }

    public class DoubleIntArray {
        double d;
        int[] intArray;

        public DoubleIntArray(double d, int[] intArray) {
            this.d = d;
            this.intArray = intArray;
        }
    }

    public DoubleIntArray test(int[] durations, int delta, int index, int maxIndex) {
        DoubleIntArray best = new DoubleIntArray(0, durations);
        if (index == rainbowSpinIndex || index == homingMCCTIndex) {
            return test(durations, delta, index + 1, maxIndex);
        }
        for (int i = durations[index] - delta; i <= durations[index] + delta; i++) {
            int[] testDurations = durations.clone();
            testDurations[index] = i;
            DoubleIntArray result;
            if (index < maxIndex) {
                result = test(testDurations, delta, index + 1, maxIndex);
            }
            else {
                result = new DoubleIntArray(test(testDurations), testDurations);
            }
            if (result.d > best.d) {
                best = result;
            }
        }
        return best;
    }

    public double test(int[] testDurations) {
        VectorCalculator.initialFrames = testDurations[0];
        VectorCalculator.genPropertiesTable.setValueAt(testDurations[0], VectorCalculator.MOVEMENT_DURATION_ROW, 1);
        int[][] midairs = preset.clone();
        for (int i = 0; i < preset.length; i++) {
            midairs[i][1] = testDurations[i + 1];
        }
        VectorCalculator.addPreset(midairs);
        VectorMaximizer maximizer = VectorCalculator.calculate();
        double disp = maximizer.maximize();
        double y = VectorCalculator.y0;
        SimpleMotion[] motions = maximizer.getMotions();
        for (SimpleMotion m : motions) {
            y += m.calcDispY();
        }
        if (y < VectorCalculator.y1) { //too low so won't work
            return 0.0;
        }
        else {
            return disp;
        }
    }
}