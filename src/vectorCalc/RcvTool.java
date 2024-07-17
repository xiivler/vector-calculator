package vectorCalc;

public class RcvTool {
    public static final double RCV_ERROR = .001; //acceptable Z axis error
    public static final int RCV_MAX_ITERATIONS = 100; //stop after this many iterations no matter what

    public static void main(String[] args) {
        /*
        Movement rcCapThrow = new Movement("Spinthrow Roll Cancel", 29.94);
        GroundedCapThrow rcMotion = new GroundedCapThrow(rcCapThrow, 0, Math.toRadians(19.5), true);
        Movement rcv = new Movement("Falling", rcMotion.finalSpeed);
        SimpleVector rcvMotion = new SimpleVector(rcv, rcMotion.finalAngle, SimpleMotion.NORMAL_ANGLE, false, 100);
        
        
        for (int i = 1; i < 720; i++) {
            //System.out.println("Testing RCs");
            double testAngle = ((Math.PI / 4) / 720) * i;
            rcCapThrow = new Movement("Spinthrow Roll Cancel", 29.94);
            rcMotion = new GroundedCapThrow(rcCapThrow, 0, testAngle, true);
            rcMotion.calcDispDispCoordsAngleSpeed();
            if (Math.abs(-rcMotion.finalAngle - testAngle) >= Math.toRadians(0.01)) {
                System.out.println("Failed with test angle " + Math.toDegrees(testAngle) + ", " + Math.toDegrees(-rcMotion.finalAngle));
            }
        }
        
        rcMotion = new GroundedCapThrow(rcCapThrow, 0, Math.toRadians(19.5), true);
        rcMotion.calcDispDispCoordsAngleSpeed();
        System.out.println("RC Info");
        System.out.println("Cap throw angle: " + Math.toDegrees(rcMotion.capThrowAngle));
        System.out.println("Final Speed: " + rcMotion.finalSpeed);
        System.out.println("Final Angle: " + Math.toDegrees(rcMotion.finalAngle));
        System.out.println("Disp X: " + rcMotion.dispX);
        System.out.println("Disp Z: " + rcMotion.dispZ);

        rcv = new Movement("Falling", rcMotion.finalSpeed);
        rcvMotion = new SimpleVector(rcv, rcMotion.finalAngle, SimpleMotion.NORMAL_ANGLE, false, 100);
        rcvMotion.calcDispDispCoordsAngleSpeed();
        System.out.println("\nRCV Info");
        System.out.println("Initial Angle: " + Math.toDegrees(rcvMotion.initialAngle));
        System.out.println("Disp X: " + rcvMotion.dispX);
        System.out.println("Disp Z: " + rcvMotion.dispZ);

        System.exit(1);
        */
        
        //loop to find best for every height
        for (int maxYDisp = 657; maxYDisp < 7000; maxYDisp += 7) {
            findBest(29.94, maxYDisp, false);
        }
        
        //5650
        findBest(29.94, 4650, true);
    }

    public static void findBest(double initialVelocity, double maxYDisp, boolean verbose) {
        //placeholders
        Movement rcCapThrow = new Movement("");
        GroundedCapThrow rcMotion = new GroundedCapThrow(rcCapThrow, true);
        Movement rcv = new Movement("Falling");
        SimpleVector rcvMotion = new SimpleVector(rcv, false, 0);

        String[] rcTypes = {"Motion Cap Throw Roll Cancel", "Single Throw Roll Cancel", "Upthrow Roll Cancel", "Double Throw Roll Cancel", "Spinthrow Roll Cancel", "Triple Throw Roll Cancel"};
        double bestDisp = 0;
        String bestThrow = "";
        //test each throw and see which is the best
        for (int q = 0; q < rcTypes.length; q++) {
            rcCapThrow = new Movement(rcTypes[q], initialVelocity);
            double fallAmount = 1.5 * 15 + 7 * (rcCapThrow.minFrames - 15);
            double yVel = 7;
            int framesRCV = -1;
            while (fallAmount <= maxYDisp) {
                if (yVel < 35) {
                    yVel += 1.5;
                }
                if (yVel > 35) {
                    yVel = 35;
                }
                fallAmount += yVel;
                framesRCV++;
            }
            double low = 0;
            double high = Math.PI / 4; //strongest you can vector is 45 degrees
            int i = 0;
            double test = 0;
            while (i < RCV_MAX_ITERATIONS) {
                test = (high + low) / 2; //binary search for the correct angle
                //System.out.println("Testing " + Math.toDegrees(test));
                rcCapThrow = new Movement(rcTypes[q], initialVelocity);
                rcMotion = new GroundedCapThrow(rcCapThrow, 0, test, true);
                rcMotion.calcDispDispCoordsAngleSpeed();
                //System.out.println("Final Angle: " + Math.toDegrees(rcMotion.finalAngle));
                rcv = new Movement("Falling", rcMotion.finalSpeed);
                rcv.initialVerticalSpeed = -7;
                rcvMotion = new SimpleVector(rcv, rcMotion.finalAngle, SimpleMotion.NORMAL_ANGLE, false, framesRCV);
                rcvMotion.calcDispDispCoordsAngleSpeed();
                double sumDispZ = rcMotion.dispZ + rcvMotion.dispZ;
                //System.out.println("Disp Z sum: " + sumDispZ);
                if (Math.abs(sumDispZ) < RCV_ERROR) {
                    break;
                }
                else if (sumDispZ > 0) { //we went too far left, increase rcv angle
                    low = test;
                }
                else {
                    high = test;
                }
                i++;
            }
            double xDisp = rcMotion.dispX + rcvMotion.dispX;
            if (xDisp > bestDisp) {
                bestDisp = xDisp;
                bestThrow = rcCapThrow.movementType;
            }
            if (verbose) {
                System.out.println("\n" + rcCapThrow.movementType);
                System.out.println("Correct angle is " + Math.toDegrees(test));
                System.out.println("Frames turn: " + rcMotion.turningFrames + " (" + Math.toDegrees(rcMotion.overshoot) + " overshoot)");
                System.out.println("Holding angles:");
                for (int n = rcMotion.frames - rcMotion.turningFrames; n < rcMotion.frames; n++) {
                    System.out.printf("%d: %.3f (%.3f)\n", n, Math.toDegrees(rcMotion.holdingAngles[n]), Math.toDegrees(Math.PI/2 - rcMotion.velocityAngles[n]));
                }
                System.out.println("X-Displacement is " + (rcMotion.dispX + rcvMotion.dispX));
                System.out.println("Z-Displacement is " + (rcMotion.dispZ));
                double[][] rcInfo = rcMotion.calcFrameByFrame();
                double[][] rcvInfo = rcvMotion.calcFrameByFrame();
                System.out.println("Y-Displacement is " + (rcMotion.dispY + rcvMotion.dispY));
                System.out.println("Total Frames: " + (rcMotion.frames + rcvMotion.frames));

                System.out.println("Final speed: " + rcvMotion.finalSpeed + ", " + rcvInfo[rcvInfo.length - 1][4]);
            }
        }
        System.out.println(maxYDisp + "," + bestThrow);
    }

    public static double calcRCCapThrowAngle(String movementType, double initialVelocity, int framesRCV) {
        double low = 0;
        double high = Math.PI / 4; //strongest you can vector is 45 degrees
        int i = 0;
        double test = 0;
        while (i < RCV_MAX_ITERATIONS) {
            test = (high + low) / 2; //binary search for the correct angle
            //System.out.println("Testing " + Math.toDegrees(test));
            Movement rcCapThrow = new Movement(movementType, initialVelocity);
            GroundedCapThrow rcMotion = new GroundedCapThrow(rcCapThrow, 0, test, true);
            rcMotion.calcDispDispCoordsAngleSpeed();
            //System.out.println("Final Angle: " + Math.toDegrees(rcMotion.finalAngle));
            Movement rcv = new Movement("Falling", rcMotion.finalSpeed);
            rcv.initialVerticalSpeed = -7;
            SimpleVector rcvMotion = new SimpleVector(rcv, rcMotion.finalAngle, SimpleMotion.NORMAL_ANGLE, false, framesRCV);
            rcvMotion.calcDispDispCoordsAngleSpeed();
            double sumDispZ = rcMotion.dispZ + rcvMotion.dispZ;
            //System.out.println("Disp Z sum: " + sumDispZ);
            if (Math.abs(sumDispZ) < RCV_ERROR) {
                break;
            }
            else if (sumDispZ > 0) { //we went too far left, increase rcv angle
                low = test;
            }
            else {
                high = test;
            }
            i++;
        }
        return test;
    }
}
