package vectorCalc;

public class Sandbox {
    
    public static void main(String[] args) {
        System.out.println("Sandbox testing");

        DiveTurn dive = (DiveTurn) (new Movement("Dive")).getMotion(20, true, true);
        
        dive.calcDispDispCoordsAngleSpeed();

        double[][] data = dive.calcFrameByFrame();
        for(int i = 0; i < 20; i++) {
            System.out.println("Hold: " + Math.toDegrees(data[i][7]));
            System.out.println("x: " + data[i][0]);
            System.out.println("z: " + data[i][2]);
        }
    }
}
