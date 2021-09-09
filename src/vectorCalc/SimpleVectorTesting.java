package vectorCalc;

import java.util.Arrays;

public class SimpleVectorTesting {
	
	public static void main(String[] args) {
		
		/*
		SimpleVector v1 = new SimpleVector(Math.toRadians(79.547), SimpleVector.NORMAL_ANGLE, 24, .3, 30, false);
		
		System.out.println(v1.dispForward);
		System.out.println(v1.dispSideways);
		System.out.println(v1.dispX);
		System.out.println(v1.dispZ);
		System.out.println(Math.toDegrees(v1.finalAngle));
		
		System.out.println();
		
		SimpleVector v2 = new SimpleVector(Math.toRadians(100.103), Math.toRadians(70), 7, .3, 20, true);
		
		System.out.println(v2.dispForward);
		System.out.println(v2.dispSideways);
		System.out.println(v2.dispX);
		System.out.println(v2.dispZ);
		System.out.println(Math.toDegrees(v2.finalAngle));
		*/
		
		/*
		Movement[] m = {new Movement("Vault", 24), new Movement("Cap Throw", 7), new Movement("Dive", 20), new Movement("Cap Bounce", 16), new Movement("Cap Throw", 7)};
		int[] f = {30, 20, 23, 40, 20};
		
		VectorMaximizer vm = new VectorMaximizer(m, f);
		//vm.maximizeJumpCapThrow();
		//vm.maximizeJumpCapThrowDiveCapBounceCapThrow();
		//vm.maximizeJumpCapThrowDiveCapBounceCapThrowFaster();
		vm.maximizeJumpCapThrowDiveCapBounceCapThrowEvenFaster();
		vm.maximizeJumpCapThrowDiveCapBounceCapThrowEfficientSearch();
		*/
		
		/*
		Movement m1 = new Movement("Triple Jump", 13.9, 10);
		for (int i = 1; i < 100; i++)
		System.out.println(m1.height(i));
		*/
		
		/*
		Movement m2 = new Movement("Single Jump", 10.1);
		for (int i = 1; i < 100; i++) {
			SimpleMotion sm = new SimpleMotion(m2, i);
			sm.calcDisp();
			System.out.println(sm.disp);
		}
		*/
		
		/*
		Movement m2 = new Movement("Single Jump", 13.4);
		for (int i = 1; i < 100; i++) {
			SimpleVector sv = new SimpleVector(m2, true, i);
			sv.calcDisp();
			System.out.println(sv.dispForward + ", " + sv.dispSideways);
		}
		*/
		
		/*
		Movement m = new Movement("Vault");
		SimpleVector v = (SimpleVector) m.getMotion(40, false, false);
		v.setInitialAngle(Math.PI / 2);
		v.calcAll();
		
		Movement m3 = new Movement("Motion Cap Throw");
		double holdingAngles[] = new double[24];
		holdingAngles[0] = Math.PI / 3;
		for (int i = 1; i <= 18; i++)
			holdingAngles[i] = SimpleMotion.NORMAL_ANGLE;
		holdingAngles[19] = SimpleVector.NO_ANGLE;
		for (int i = 20; i <= 23; i++)
			holdingAngles[i] = SimpleMotion.NORMAL_ANGLE;
		ComplexVector cv = new ComplexVector(m3, v.finalAngle, holdingAngles, true);
		cv.calcDisp();
		//System.out.println(cv.dispSideways + ", " + cv.dispForward);
		SimpleMotion sm1 = new SimpleMotion(m3, v.finalAngle, 24);
		sm1.calcDisp();
		
		Movement m4 = new Movement("Falling", cv.calcFinalSpeed());
		SimpleVector s1 = new SimpleVector(m4, cv.calcFinalAngle(), SimpleVector.NORMAL_ANGLE, false, 12);
		SimpleMotion sm2 = new SimpleMotion(m4, cv.calcFinalAngle(), 12);
		s1.calcDisp();
		sm2.calcDisp();
		
		//System.out.println(Math.toDegrees(s1.calcFinalAngle()));
		//System.out.println(s1.calcFinalSpeed());
		Movement m5 = new Movement("Rainbow Spin");
		SimpleVector s2 = new SimpleVector(m5, s1.calcFinalAngle(), SimpleVector.NORMAL_ANGLE, false, 31);
		s2.calcDisp();
		
		//System.out.println(Math.toDegrees(s2.calcFinalAngle()));
		Movement m6 = new Movement("Falling", s2.calcFinalSpeed());
		SimpleMotion sm3 = new SimpleMotion(m6, s2.calcFinalAngle(), 1);
		sm3.calcDisp();
		
		cv.calcDispCoords();
		sm1.calcDispCoords();
		s1.calcDispCoords();
		sm2.calcDispCoords();
		s2.calcDispCoords();
		sm3.calcDispCoords();
		
		System.out.println(v.dispX + ", " + v.dispZ);
		System.out.println(cv.dispX + ", " + cv.dispZ);
		System.out.println(s1.dispX + ", " + s1.dispZ);
		System.out.println(s2.dispX + ", " + s2.dispZ);
		System.out.println(sm3.dispX + ", " + sm3.dispZ);
		
		double dispX = v.dispX + cv.dispX + s1.dispX + s2.dispX + sm3.dispX;
		double dispZ = v.dispZ + cv.dispZ + s1.dispZ + s2.dispZ + sm3.dispZ;
		
		System.out.println(dispX + ", " + dispZ);
		System.out.println(Math.sqrt(Math.pow(dispX, 2) + Math.pow(dispZ,  2)));
		System.out.println(Math.toDegrees(Math.atan(dispZ / dispX)));
		*/
		
		/*
		Movement m = new Movement("Single Jump", 14);
		SimpleVector s = (SimpleVector) m.getMotion(30, true, false);
		s.setInitialAngle(Math.PI / 2);
		s.setInitialRotation(Math.PI / 2);
		double holdingAngles[] = new double[6];
		holdingAngles[0] = SimpleMotion.NO_ANGLE;
		holdingAngles[1] = SimpleMotion.NORMAL_ANGLE;
		holdingAngles[2] = Math.PI / 4;
		holdingAngles[3] = 0;
		holdingAngles[4] = SimpleMotion.NORMAL_ANGLE;
		holdingAngles[5] = SimpleMotion.NORMAL_ANGLE;
		//s.setHoldingAngles(holdingAngles);
		s.calcDisp();
		System.out.println(s.calcFramesToRotation(Math.PI / 2 - Math.toRadians(.3)));
		*/
		
		
		Movement m = new Movement("Single Jump", 24, 10);
		SimpleVector s = (SimpleVector) m.getMotion(60, false, false);
		//s.setInitialCoordinates(100, 100, 100);
		//s.setOptimalForwardAccel(false);
		s.setInitialAngle(Math.toRadians(-20.86934779483386));
		s.calcDisp();
		s.calcDispCoords();
		System.out.println(s.dispX + ", " + s.dispZ);
		double[][] info = s.calcFrameByFrame();
		for (double[] ds : info)
			System.out.println(Arrays.toString(ds));
		
		
		/*
		Movement m3 = new Movement("Motion Cap Throw", 5.0);
		double holdingAngles[] = new double[24];
		holdingAngles[0] = Math.PI / 3;
		for (int i = 1; i <= 18; i++)
			holdingAngles[i] = SimpleMotion.NORMAL_ANGLE;
		holdingAngles[19] = SimpleVector.NO_ANGLE;
		for (int i = 20; i <= 23; i++)
			holdingAngles[i] = SimpleMotion.NORMAL_ANGLE;
		ComplexVector cv = (ComplexVector) m3.getMotion(24, false, true);
		cv.setOptimalForwardAccel(false);
		cv.setInitialAngle(Math.PI / 2);
		cv.setHoldingAngles(holdingAngles);
		cv.calcDisp();
		cv.calcDispCoords();
		System.out.println(cv.dispX + ", " + cv.dispZ);
		double[][] info2 = cv.calcFrameByFrame();
		for (double[] ds : info2)
			System.out.println(Arrays.toString(ds));
		*/
	}
	
}
