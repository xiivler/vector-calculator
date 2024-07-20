package vectorCalc;
public class Sandbox {
    public static void main(String[] args) {
        Movement rcCapThrow = new Movement("MCCT Roll Cancel Spinpound", 29.94);
        GroundedCapThrow rcMotion = new StayGroundedCapThrow(rcCapThrow, 0, Math.toRadians(81), Math.toRadians(44.2), true);
        rcMotion.calcDispDispCoordsAngleSpeed();
        System.out.println("Displacement: " + rcMotion.dispX + ", " + rcMotion.dispZ);

        // double low = 0;
        // double high = Math.PI / 2; //strongest you can vector is 45 degrees
        // int i = 0;
        // double test = 0;
        // while (i < 100) {
        //     test = (high + low) / 2; //binary search for the correct angle
        //     System.out.println("Testing " + Math.toDegrees(test));
        //     Movement rcCapThrow = new Movement("MCCT Roll Cancel Spinpound", 29.94); //speed doesn't seem to matter
        //     GroundedCapThrow rcMotion = new StayGroundedCapThrow(rcCapThrow, 0, Math.toRadians(81), test, true);
        //     rcMotion.calcDispDispCoordsAngleSpeed();
        //     //System.out.println("Final Angle: " + Math.toDegrees(rcMotion.finalAngle));
        //     if (Math.abs(rcMotion.dispZ) < .01) {
        //         System.out.println("Hook angle: " + Math.toDegrees(test));
        //         System.out.println("Displacement: " + rcMotion.dispX + ", " + rcMotion.dispZ);
        //         break;
        //     }
        //     else if (rcMotion.dispZ > 0) { //we went too far left, increase rcv angle
        //         low = test;
        //     }
        //     else {
        //         high = test;
        //     }
        //     i++;
        // }

        // System.exit(0);
        
        // test = Math.toRadians(40);
        // double bestDisp = 0;
        // double bestAngle = 0;
        // for (i = 0; i <= 100; i++) {
        //     test += Math.toRadians(10) / 100;
        //     System.out.println("Testing " + Math.toDegrees(test));
        //     Movement rcCapThrow = new Movement("MCCT Roll Cancel Spinpound", 29.94);
        //     GroundedCapThrow rcMotion = new StayGroundedCapThrow(rcCapThrow, 0,  Math.toRadians(81), test, true);
        //     rcMotion.calcDispDispCoordsAngleSpeed();
        //     //System.out.println("Final Angle: " + Math.toDegrees(rcMotion.finalAngle));
        //     //double testDisp = Math.sqrt(rcMotion.dispX * rcMotion.dispX + rcMotion.dispZ * rcMotion.dispZ);
        //     double testDisp = rcMotion.dispX;
        //     if (testDisp > bestDisp) {
        //         bestDisp = testDisp;
        //         bestAngle = test;
        //     }
        // }
        // System.out.println("Hook angle: " + Math.toDegrees(bestAngle));
        // System.out.println("Displacement: " + bestDisp);
    }
}
