package com.vectorcalculator;

import java.util.Arrays;
import java.util.Vector;

import com.vectorcalculator.Properties.YNT;

public class Sandbox {

    private static double reduceAngle(double angle) {
		double d = Math.toDegrees(angle);
		while (d >= 360)
			d -= 360;
		while (d < 0)
			d += 360;
		return d;
	}

    public static void main(String[] args) {
        VectorCalculator.main(args);
        //System.out.println(VectorCalculator.round(-5.2, 1));
        //System.out.println("Testing Speedrun Solver");
        //DispData.initializeDispData();
        //SpeedrunSolver ss = new SpeedrunSolver();
        //ss.solve(0);
        //calcDispData(1, 500, -1, 0, "Spinless (No Final Cap Throw)"); //initial motion
        //calcDispData(1, 500, -1, 0, false, "Spinless (No Final Cap Throw)"); //initial motion
        //calcDispData(1, 50, -1, 0, false, "Spinless (No Final Cap Throw)"); //final dive
        calcDispData(1, 100, 2, 4, false, "Spinless (No Final Cap Throw)");
        //calcFinalCTDispData();
    }

    public static void calcDispData(int minFrames, int maxFrames, int presetIndex, int maximizerIndex, boolean solveCB, String preset) {
        Properties p = Properties.getInstance();
        VectorCalculator.addPreset(preset, false);
        double[] forwardDisps = new double[maxFrames + 1];
        double[] yDisps = new double[maxFrames + 1];
        for (int i = minFrames; i <= maxFrames; i++) {
            if (presetIndex == -1)
                p.initialFrames = i;
            else
                p.midairs[presetIndex][1] = i;
            VectorMaximizer maximizer;
            if (solveCB) {
                DiveSolver solver = new DiveSolver();
                solver.test();
                maximizer = solver.getMaximizer();
            }
            else
                maximizer = VectorCalculator.calculate();
            //int index = maximizer.listPreparer.initialMovementIndex;
            //int index = maximizer.motions.length - 3;
            SimpleMotion motion = maximizer.motions[maximizerIndex];
            double targetAngle = Math.atan(maximizer.bestDispX / maximizer.bestDispZ);
            double coordAngle = Math.atan(motion.dispX / motion.dispZ);
            double forwardDisp = Math.abs(Math.sqrt(motion.dispX * motion.dispX + motion.dispZ * motion.dispZ) * Math.cos(targetAngle - coordAngle));
            forwardDisps[i] = VectorCalculator.round(forwardDisp, 3);
            yDisps[i] = VectorCalculator.round(motion.calcDispY(), 0);
        }
        System.out.println(Arrays.toString(forwardDisps));
        System.out.println(Arrays.toString(yDisps));
    }

    public static DispData getIMDispData(int minFrames, int maxFrames) {
        Properties p = Properties.getInstance();
        VectorCalculator.addPreset(p.midairPreset, false);
        double[] forwardDisps = new double[maxFrames + 1];
        double[] yDisps = new double[maxFrames + 1];
        for (int i = minFrames; i <= maxFrames; i++) {
            p.initialFrames = i - p.framesCrouch - p.framesMoonwalk - p.framesRun;
            VectorMaximizer maximizer;
            maximizer = VectorCalculator.calculate();

            double dispX, dispY, dispZ;
            dispX = dispY = dispZ = 0;
            for (int j = 0; j < maximizer.listPreparer.initialMovementIndex; j++) {
                dispX += maximizer.motions[j].dispX;
                dispY += maximizer.motions[j].dispY;
                dispZ += maximizer.motions[j].dispZ;
            };
            double targetAngle = Math.atan(maximizer.bestDispX / maximizer.bestDispZ);
            double coordAngle = Math.atan(dispX / dispZ);
            double forwardDisp = Math.abs(Math.sqrt(dispX * dispX + dispZ * dispZ) * Math.cos(targetAngle - coordAngle));;
            forwardDisps[i] = forwardDisp;
            yDisps[i] = dispY;
        }
        return new DispData(p.initialMovementName, DispData.DEFAULT, forwardDisps, yDisps);
    }

    public static void calcFinalCTDispData() {
        Properties p = Properties.getInstance();
        VectorCalculator.addPreset("Spinless", false);
        p.midairs[0][1] = 28;
        p.midairs[1][1] = 26;
        int frames = 35;
        double[] forwardDisps = new double[frames + 1];
        double[] yDisps = new double[frames + 1];
        VectorMaximizer maximizer = null;
        for (int i = 8; i <= frames; i++) {
            p.midairs[p.midairs.length - 2][1] = i;
            //SolverInterface solver = VectorCalculator.runSolver(true, true);
            DiveSolver solver = new DiveSolver();
            solver.test();
            maximizer = solver.getMaximizer();
            //VectorMaximizer maximizer = VectorCalculator.calculate();
            //int index = maximizer.listPreparer.initialMovementIndex;
            int ctIndex = maximizer.variableMovement2Index;
            int ctFallIndex = maximizer.hasVariableMovement2Falling ? maximizer.variableMovement2Index + 1 : -1;
            double dispX = maximizer.motions[ctIndex].dispX + (ctFallIndex >= 0 ? maximizer.motions[ctFallIndex].dispX : 0);
            double dispZ = maximizer.motions[ctIndex].dispZ + (ctFallIndex >= 0 ? maximizer.motions[ctFallIndex].dispZ : 0);
            double dispY = maximizer.motions[ctIndex].calcDispY() + (ctFallIndex >= 0 ? maximizer.motions[ctFallIndex].calcDispY() : 0);
            double targetAngle = Math.atan(maximizer.bestDispX / maximizer.bestDispZ);
            double coordAngle = Math.atan(dispX / dispZ);
            double forwardDisp = Math.abs(Math.sqrt(dispX * dispX + dispZ * dispZ) * Math.cos(targetAngle - coordAngle));
            forwardDisps[i] = VectorCalculator.round(forwardDisp, 3);
            yDisps[i] = VectorCalculator.round(dispY, 1);
            System.out.println("Complete");
        }
        System.out.println(Arrays.toString(forwardDisps));
        System.out.println(Arrays.toString(yDisps));
        VectorDisplayWindow.generateData(maximizer);
        VectorDisplayWindow.display();
    }

    public static void calcCTDiveDispData() {
        Properties p = Properties.getInstance();
        VectorCalculator.addPreset("Spinless (No Final Cap Throw)", false);
        double[][] forwardDisps = new double[34][41];
        double[][] yDisps = new double[34][41];
        for (int i = 8; i <= 33; i++) {
            for (int j = 20; j <= 35; j++) {
                //p.initialFrames = i;
                p.midairs[p.firstCTIndex][1] = i;
                p.midairs[p.firstCTIndex + 1][1] = j;
                //SolverInterface solver = VectorCalculator.runSolver(true, true);
                DiveSolver solver = new DiveSolver();
                solver.ttAllowed = YNT.NO;
                double disp = solver.test();
                if (disp != 0) {
                    VectorMaximizer maximizer = solver.getMaximizer();
                    //VectorMaximizer maximizer = VectorCalculator.calculate();
                    //int index = maximizer.listPreparer.initialMovementIndex;
                    int ctIndex = maximizer.variableCapThrow1Index;
                    int ctFallIndex = maximizer.hasVariableCapThrow1Falling ? maximizer.variableCapThrow1Index + 1 : -1;
                    int diveIndex = maximizer.variableCapThrow1Index + (ctFallIndex >= 0 ? 3 : 2);
                    double dispX = maximizer.motions[ctIndex].dispX + (ctFallIndex >= 0 ? maximizer.motions[ctFallIndex].dispX : 0) + maximizer.motions[diveIndex].dispX;
                    double dispZ = maximizer.motions[ctIndex].dispZ + (ctFallIndex >= 0 ? maximizer.motions[ctFallIndex].dispZ : 0) + maximizer.motions[diveIndex].dispZ;
                    double dispY = maximizer.motions[ctIndex].calcDispY() + (ctFallIndex >= 0 ? maximizer.motions[ctFallIndex].calcDispY() : 0) + maximizer.motions[diveIndex].calcDispY();
                    //SimpleMotion motion = maximizer.motions[index];
                    double targetAngle = Math.atan(maximizer.bestDispX / maximizer.bestDispZ);
                    double coordAngle = Math.atan(dispX / dispZ);
                    double forwardDisp = Math.abs(Math.sqrt(dispX * dispX + dispZ * dispZ) * Math.cos(targetAngle - coordAngle));
                    forwardDisps[i][j] = VectorCalculator.round(forwardDisp, 3);
                    yDisps[i][j] = VectorCalculator.round(dispY, 1);
                    System.out.printf("%s\t%s\t%s\t%s\t%s\n", i + j, i, j, forwardDisps[i][j], yDisps[i][j]);
                }
            }
        }
        //System.out.println(Arrays.toString(forwardDisps));
        //System.out.println(Arrays.toString(yDisps));
    } 

    /* public static void main(String[] args) {
        Properties p = Properties.getInstance();
        Movement initialMovement = new Movement("Triple Jump", 24.0);
        double[] holdingAngles = new double[]{0, 90, 89.7, 89.4, 89, 89, 89, 90, 90, 89.9, 90, 70, 180, 180, 180, 180, 175, 264.4, -95.6, 75, 79, 83, 82, 82, SimpleMotion.NO_ANGLE, 83, 185.5, 185.5, 185.5, 185.5, 200};
        //double[] holdingAngles = new double[]{4.1, 4.1, 4.1, 4.1, 4.1, 4.1, 0};
        //double[] holdingAngles = new double[]{1, 180.4};
        //double[] holdingAngles = new double[]{170, 349, 353, 352};
        for (int i = 0; i < holdingAngles.length; i++) {
            if (holdingAngles[i] != SimpleMotion.NO_ANGLE)
                holdingAngles[i] = Math.toRadians(holdingAngles[i]);
        }
        ComplexVector cv = (ComplexVector) initialMovement.getMotion(holdingAngles.length, false, true);
        cv.setHoldingAngles(holdingAngles);
        cv.setInitialAngle(Math.toRadians(90));
        cv.setInitialRotation(Math.toRadians(90));
        cv.calcDispDispCoordsAngleSpeed();
        double[][] info = cv.calcFrameByFrame();
        double prevRotation = reduceAngle(info[0][9]);
        for (int i = 0; i < holdingAngles.length; i++) {
            if (info[i][7] == SimpleMotion.NO_ANGLE)
                info[i][7] = -0;
            System.out.printf("%2d: %6.3f  %6.3f  %5.3f\n", i, reduceAngle(info[i][7]), reduceAngle(info[i][9]), reduceAngle(info[i][9]) - prevRotation);
            prevRotation = reduceAngle(info[i][9]);
        }
    } */
}