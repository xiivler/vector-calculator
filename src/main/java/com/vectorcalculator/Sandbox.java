package com.vectorcalculator;

import java.util.Arrays;
import java.util.Vector;

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
        Properties p = Properties.getInstance();
        VectorCalculator.addPreset("Spinless (No Final Cap Throw)", false);
        int frames = 50;
        double[] forwardDisps = new double[frames + 1];
        double[] yDisps = new double[frames + 1];
        for (int i = 1; i <= frames; i++) {
            //p.initialFrames = i;
            p.midairs[p.midairs.length - 1][1] = i;
            VectorMaximizer maximizer = VectorCalculator.calculate();
            //int index = maximizer.listPreparer.initialMovementIndex;
            int index = maximizer.motions.length - 1;
            SimpleMotion motion = maximizer.motions[index];
            double targetAngle = Math.atan(maximizer.bestDispX / maximizer.bestDispZ);
            double coordAngle = Math.atan(motion.dispX / motion.dispZ);
            double forwardDisp = Math.abs(Math.sqrt(motion.dispX * motion.dispX + motion.dispZ * motion.dispZ) * Math.cos(targetAngle - coordAngle));
            forwardDisps[i] = VectorCalculator.round(forwardDisp, 0);
            yDisps[i] = VectorCalculator.round(motion.calcDispY(), 0);
        }
        System.out.println(Arrays.toString(forwardDisps));
        System.out.println(Arrays.toString(yDisps));
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