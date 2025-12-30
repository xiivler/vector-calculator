package com.vectorcalculator;

import java.util.ArrayList;
import java.util.Vector;

import javax.swing.table.TableModel;

import com.vectorcalculator.Properties.AngleType;

public class VectorMaximizer {

	Properties p = Properties.p;
	
	public static final double RCV_ERROR = .001; //acceptable Z axis error when trying to make a RCV go straight
    public static final int RCV_MAX_ITERATIONS = 100; //stop after this many iterations no matter what when trying to make a RCV go straight

	public static final double FAST_TURNAROUND_VELOCITY = Math.toRadians(25);
	public static final double FAST_TURNAROUND_ACCEL = Math.toRadians(2.5);
	public static final double FAST_TURNAROUND_ANGLE = Math.toRadians(135.5); //only needs to be 135, but extra .5 degrees for safety

	public static final double TURN_COUNTERROTATION = Math.toRadians(.4); //really should be .3 but this produces inaccurate results
	public static final double TRUE_TURN_COUNTERROTATION = Math.toRadians(.3);

	public static final double FINAL_CT_ANGLE_REDUCTION_LIMIT = 5; //how many degrees you are willing to sacrifice off a perfect vector

	//currently calculates tenths of degrees, and maybe loses a hundredth of a unit over calculating to the thousandth
	//public static int numSteps = 901;
	
	boolean alwaysDiveTurn = false; //set to true to only test with dive turn, which is faster for Solver
	boolean neverDiveTurn = false;
	double maximize_HCT_limit = Math.toRadians(2); //binary search limit for hct fall vector angle

	SimpleVector[] vectors;
	double[] angles;
	SimpleMotion[] motions;
	int[] frames;

	double diveCapBounceAngle;
	int ctType = Movement.MCCTU;
	double firstFrameDecel = 0; //for the dive before the cap bounce
	
	double dispZ;
	double dispX;
	double disp;
	double angle;
	
	double givenAngle;
	boolean targetAngleGiven;
	double initialAngle;
	double targetAngle;
	double angleAdjustment = 0;
	
	boolean rightVector;
	boolean currentVectorRight;

	boolean switchHCTFallVectorDir = true;
	boolean bestSwitchHCTFallVectorDir = true;
	
	boolean hasVariableRollCancel = false;
	boolean hasVariableCapThrow1 = false;
	boolean hasVariableCapThrow2 = false;
	boolean hasVariableOtherMovement2 = false;
	boolean hasVariableCapThrow1Falling = false;
	boolean hasVariableMovement2Falling = false;
	boolean hasVariableHCTFallVector = false;
	boolean hasRainbowSpin = false;
	boolean simpleTech = false;
	boolean hasDiveCapBounce = true;

	boolean only_maximize_variableAngle2 = false;
	
	int variableCapThrow1Index;
	int variableMovement2Index;
	int motionGroup2Index;
	int variableHCTFallIndex;
	//int motionGroup3Index;
	int rainbowSpinFrames;
	int preCapBounceDiveIndex = -1;

	int maxRCVNudges = 20;
	int maxRCVFineNudges = 10;

	ComplexVector variableCapThrow1Vector;
	ComplexVector variableMovement2Vector;
	int variableCapThrow1Frames;
	int variableCapThrow1FallingFrames;
	double motionGroup1FinalAngle;
	boolean variableCapThrow1VectorRight;

	boolean motionGroup2VectorRight;
	double motionGroup2Angle;
	double motionGroup2FinalAngle;
	double motionGroup2FinalRotation;

	double dispZMotionGroup1;
	double dispXMotionGroup1;
	double dispMotionGroup2;
	
	double testDispZ1;
	double testDispX1;
	double bestDispZ1;
	double bestDispX1;
	double testDispZ2;
	double testDispX2;
	double variableAngle1Adjusted;
	double variableAngle2;
	double variableAngle2Adjusted;
	double variableHCTHoldingAngle;
	double[] rainbowSpinHoldingAngles; //number of frames of holding opposite the vector direction (half frame = holding straight)

	double rcTrueInitialAngleDiff;
	double rcFinalAngleDiff;
	double bestRCFinalAngleDiff;
	double[] bestRainbowSpinHoldingAngles;

	double once_bestDispZ;
	double once_bestDispX;
	double once_bestDisp;
	double once_bestAngle1;
	double once_bestAngle2;
	double once_bestAngle1Adjusted;
	double once_bestAngle2Adjusted;

	double bestDispZ;
	double bestDispX;
	double bestDisp;
	double bestAngle1;
	double bestAngle2;
	double bestAngle1Adjusted;
	double bestAngle2Adjusted;

	SimpleMotion[] motionGroup2 = null;
	
	ArrayList<String> movementNames;
	ArrayList<Integer> movementFrames;
	MovementNameListPreparer listPreparer;

	static double[] fastTurnarounds = {Math.toRadians(85), Math.toRadians(75), Math.toRadians(72.5), Math.toRadians(67.5), Math.toRadians(50), Math.toRadians(47.5), Math.toRadians(25), 0};
	static int[] fastTurnaroundFrames = {4, 3, 3, 3, 2, 2, 1, 0};
	static int[] maxVelocityFastTurnaroundFrames = {1, 3, 2, 1, 2, 1, 1, 0}; //how many frames you're rotating at 25 deg/fr
	
	static double[] homingMotionThrowHoldingAngles;

	static boolean diveTurn = true; //whether to turn on dives to optimize them further
	
	static {
		homingMotionThrowHoldingAngles = new double[24];
		homingMotionThrowHoldingAngles[0] = Math.PI / 3;
		for (int j = 1; j <= 18; j++)
			homingMotionThrowHoldingAngles[j] = SimpleMotion.NORMAL_ANGLE;
		homingMotionThrowHoldingAngles[19] = SimpleMotion.NO_ANGLE;
		for (int j = 20; j <= 23; j++)
			homingMotionThrowHoldingAngles[j] = SimpleMotion.NORMAL_ANGLE;
	}
	
	public VectorMaximizer(MovementNameListPreparer listPreparer) {
		
		this.listPreparer = listPreparer;
		
		//TableModel genPropertiesModel = p.genPropertiesModel;
		targetAngleGiven = p.angleType == AngleType.TARGET; //keep this logic; if it's both, you want to conform to the initial
		if (p.xAxisZeroDegrees) { //vector calculator cacluates as if the order is ZXY (i.e. 0 degrees is the positive Z axis, and 90 degrees is the positive X axis)
			initialAngle = Math.PI / 2 - Math.toRadians(p.initialAngle);
			targetAngle = Math.PI / 2 - Math.toRadians(p.targetAngle);
		}
		else {
			initialAngle = Math.toRadians(p.initialAngle);
			targetAngle = Math.toRadians(p.targetAngle);
		}
		//givenAngle = Math.toRadians(Double.parseDouble(genPropertiesModel.getValueAt(p.ANGLE_ROW, 1).toString()));
		//targetAngleGiven = genPropertiesModel.getValueAt(p.ANGLE_TYPE_ROW, 1).toString().equals("Target Angle");
		rightVector = p.rightVector;
		
		movementNames = listPreparer.movementNames;
		movementFrames = listPreparer.movementFrames;
		
		hasVariableRollCancel = movementNames.get(0).contains("RCV");

		variableCapThrow1Index = movementNames.size();
		variableMovement2Index = movementNames.size();
		motionGroup2Index = movementNames.size();
		//motionGroup3Index = movementNames.size();
		
		//determine where the cap throws / other movement types whose angles are variable are (if any), since they will partition the movement
		for (int i = 0; i < movementNames.size(); i++) {
			if (movementNames.get(i).equals("Dive")) {
				if (i - 2 >= 0 && movementNames.get(i - 2).contains("Throw")) {
					if (i == movementNames.size() - 1) {
						hasVariableCapThrow2 = true;
						variableMovement2Index = i - 2;
						//motionGroup3Index = i - 1;
					}
					else {
						hasVariableCapThrow1 = true;
						variableCapThrow1Index = i - 2;
						motionGroup2Index = i - 1;
					}
				}
				else if (i - 3 >= 0 && movementNames.get(i - 3).contains("Throw") && !movementNames.get(i - 3).contains("RCV") && movementNames.get(i - 2).equals("Falling")) {
					if (i == movementNames.size() - 1) {
						hasVariableCapThrow2 = true;
						hasVariableMovement2Falling = true;
						variableMovement2Index = i - 3;
					}
					else {
						hasVariableCapThrow1 = true;
						hasVariableCapThrow1Falling = true;
						variableCapThrow1Index = i - 3;
						motionGroup2Index = i - 1;
					}
				}
				else if (i - 2 >= 0 && i == movementNames.size() - 1) {
					if (i - 3 >= 0 && movementNames.get(i - 2).equals("Falling") && (new Movement(movementNames.get(i - 3)).vectorAccel > 0) && !movementNames.get(i - 3).contains("RCV")) {
						hasVariableOtherMovement2 = true;
						hasVariableMovement2Falling = true;
						variableMovement2Index = i - 3;
					}
					else if (new Movement(movementNames.get(i - 2)).vectorAccel > 0 && !(i - 3 >= 0 && movementNames.get(i - 3).contains("RCV"))) {
						hasVariableOtherMovement2 = true;
						variableMovement2Index = i - 2;
					}
				}
			}
			//we need to optimize the hct fall specifically
			else if (movementNames.get(i).contains("Homing") && i < movementNames.size() - 1 && movementNames.get(i + 1).equals("Falling")) {
				hasVariableHCTFallVector = true;
				variableHCTFallIndex = i + 1;
			}
			else if (movementNames.get(i).equals("Rainbow Spin")) {
				hasRainbowSpin = true;
				rainbowSpinFrames = movementFrames.get(i);
			}
			// else if (movementNames.get(i).equals("Dive Cap Bounce")) {
			// 	hasDiveCapBounce = true;
			// 	preCapBounceDiveIndex = i - 1;
			// }
		}
		
		motions = new SimpleMotion[movementNames.size()];
		
		Debug.println("Variable cap throw 1: " + hasVariableCapThrow1);
		Debug.println("Variable cap throw 2: " + hasVariableCapThrow2);
		Debug.println("Variable other movement 2: " + hasVariableOtherMovement2);
		Debug.println("Indices: " + variableCapThrow1Index + ", " + motionGroup2Index + ", " + variableMovement2Index);
	}

	public double maximize(int optID) {
		if (optID == 0) {
			return maximize_variableAngle1();
		}
		else return maximize_variableAngle1();
	}

	//applies the binary serach value based on what is being optimized
	public void applyBinarySearchValue(double value, int optID) {
		if (optID == 0) {
			if (value < 0) {
				switchHCTFallVectorDir = true;
				variableHCTHoldingAngle = -value;
			}
			else {
				switchHCTFallVectorDir = false;
				variableHCTHoldingAngle = value;
			}
		}
	}

	//performs a modified binary search assuming ascending numbers
	//optID is the optimization to perform
	//0 = simple tech rainbow spin
	//limit is the smallest increment; when reached it stops the search
	public double[] binarySearch(double low, double high, int optID, double limit) {
		double quarter = (high - low) / 4;
		double med = (high + low) / 2;
		applyBinarySearchValue(med, optID);
		double lowMed;
		double highMed;
		double medDisp = maximize(optID);
		double lowMedDisp;
		double highMedDisp;

		while (quarter > limit) {
			lowMed = med - quarter;
			highMed = med + quarter;
			Debug.println("BS vals: " + lowMed + ", " + highMed);
			applyBinarySearchValue(lowMed, optID);
			lowMedDisp = maximize(optID);
			applyBinarySearchValue(highMed, optID);
			highMedDisp = maximize(optID);
			Debug.println(lowMedDisp + ", " + highMedDisp);
			if (lowMedDisp > medDisp && lowMedDisp > highMedDisp) { //maximum is in the left half
				low = low;
				med = lowMed;
				high = med;
				medDisp = lowMedDisp;
			}
			else if (highMedDisp > medDisp && highMedDisp > lowMedDisp) { //maximum is in the right half
				low = med;
				med = highMed;
				high = high;
				medDisp = highMedDisp;
			}
			else { //maximum is in the middle half
				low = lowMed;
				med = med;
				high = highMed;
				medDisp = medDisp;
			}
			quarter /= 2;
		}
		double bestValue = med;
		applyBinarySearchValue(bestValue, optID);
		double bestDisp = maximize(optID);
		return new double[]{bestDisp, bestValue};
	}
	
	private int booleanToPlusMinus(boolean b) {
		if (b)
			return 1;
		else
			return -1;
	}
	
	private void sumXDisps(SimpleMotion[] selectedMotions) {
		dispZ = 0;
		for (SimpleMotion m : selectedMotions)
			dispZ += m.dispZ;
	}
	
	private void sumYDisps(SimpleMotion[] selectedMotions) {
		dispX = 0;
		for (SimpleMotion m : selectedMotions)
			dispX += m.dispX;
	}
	
	private void calcDisp() {
		disp = Math.sqrt(Math.pow(dispZ, 2) + Math.pow(dispX, 2));
	}
	
	private void calcAngle() {
		angle = Math.atan(dispX / dispZ);
	}
	
	private void calcAll(SimpleMotion[] selectedMotions) {
		sumXDisps(selectedMotions);
		sumYDisps(selectedMotions);
		calcDisp();
		calcAngle();
	}
	
	
	private double calcFinalRotation(SimpleMotion[] motionGroup) {
		if (motionGroup.length == 0)
			return Math.PI/2;
		else {
			motionGroup[0].setInitialRotation(motionGroup[0].initialAngle);
			for (int i = 1; i < motionGroup.length; i++)
				motionGroup[i].setInitialRotation(motionGroup[i-1].calcFinalRotation());
			
			/*
			Debug.println("Rotation steps:");
			motionGroup[motionGroup.length - 1].calcFinalRotation();
			for (int i = 0; i < motionGroup.length; i++) {
				Debug.println(motionGroup[i].movement.movementType); 
				if (motionGroup[i].getClass().getSimpleName().contains("Vector"))
					Debug.println(((SimpleVector) motionGroup[i]).rightVector); 
				Debug.println(Math.toDegrees(motionGroup[i].finalRotation));
			}
			*/
			
			return motionGroup[motionGroup.length - 1].calcFinalRotation();
		}
	}

	//generates the holding angles for optimizing a rainbow spin within a simple tech jump and stores them in the variable rainbowSpinHoldingAngles
	//Don't actually need it though
	/* private void generateSimpleTechRainbowSpinHoldingAngles(double unvectorAmount) {
		rainbowSpinHoldingAngles = new double[rainbowSpinFrames];

		int fullUnvectorFrames = (int) unvectorAmount;
		double partialStrength = unvectorAmount - fullUnvectorFrames;
		int totalUnvectorFrames = (int) Math.ceil(unvectorAmount);

		for (int a = 0; a < rainbowSpinFrames - totalUnvectorFrames; a++) {
			rainbowSpinHoldingAngles[a] = SimpleMotion.NORMAL_ANGLE;
		}
		for (int a = rainbowSpinFrames - totalUnvectorFrames; a < rainbowSpinFrames - fullUnvectorFrames; a++) {
			rainbowSpinHoldingAngles[a] = SimpleMotion.NORMAL_ANGLE * (1 - 2*partialStrength);
		}

		for (int a = rainbowSpinFrames - fullUnvectorFrames; a < rainbowSpinFrames; a++) {
			rainbowSpinHoldingAngles[a] = -SimpleMotion.NORMAL_ANGLE;
		}
	} */
	
	//angle is the angle of the dive
	private void setCapThrowHoldingAngles(ComplexVector motion, double angle, int frames, int fallingFrames) {
		double throwAngle = angle;
		double diveAngle = angle;
		if (p.hyperoptimize)
			throwAngle += Math.toRadians(diveCapBounceAngle);
		double maxRotation = 0;
		double rotationalVelocity = 0;
		boolean standardTurnaround = false;
		for (int i = 0; i < frames - 2; i++) {
			rotationalVelocity += .3; //fix if really long?
			maxRotation += rotationalVelocity;
			//Debug.println("Max rotation: " + maxRotation);
			if (maxRotation + diveCapBounceAngle > 24.999) { //if we can get to the dive angle with at least 1f of fast turnaround
				standardTurnaround = true;
			}
		}
		double[] holdingAngles = new double[frames];
		holdingAngles[0] = throwAngle;
		//we need at least 6 frames to apply the non-standard turnaround
		//if the divecapbounceangle is 0 and the movement is not more than 10 frames, those have better solutions
		if (p.hyperoptimize && !(diveCapBounceAngle == 0 && frames <= 14) && !(frames <= 6 && !standardTurnaround)) { //we can rotate enough away from the dive angle that we can use 1 or 2 frames of fast turnaround to get there
			if (standardTurnaround) {
				if (maxRotation + diveCapBounceAngle < 25.001) { //shortcut if we can just hold one direction
					for (int i = 1; i < frames - 1; i++) {
						holdingAngles[i] = SimpleMotion.NORMAL_ANGLE;
					}
					holdingAngles[frames - 1] = throwAngle + maxRotation - 136 / 180.0 * Math.PI; //hold as little back as you can
					boolean[] holdingMinRadius = new boolean[frames];
					holdingMinRadius[frames - 1] = true;
					motion.setHolding(holdingAngles, holdingMinRadius);
					return;
				}
				else {
					int turnaroundFrames = 1;
					double minRotation = motion.rotationalAccel * (frames - 2); //first frame sets the cap throw angle, last frame (or two) is a fast turnaround
					Debug.println("Min Rotation: " + Math.toDegrees(minRotation));
					double unneededRotation = 0;
					//System.out.println(fallingFrames);
					if (fallingFrames >= 4) {
						unneededRotation = Math.toRadians(2.9); //this rotation can all happen during the falling
						//System.out.println("Whoa");
					}
					else {
						//System.out.println("Whoa No");
					}
					double additionalRotation = FAST_TURNAROUND_VELOCITY - (Math.toRadians(diveCapBounceAngle) - unneededRotation + minRotation);	
					if (additionalRotation < 0) {
						turnaroundFrames = 2;
						additionalRotation += FAST_TURNAROUND_VELOCITY - Math.toRadians(2.5) + Math.toRadians(.3); //add .3 because the minimum rotation is now .3 less
						minRotation = motion.rotationalAccel * (frames - 3);
					}
					Debug.println("Additional rotation: " + Math.toDegrees(additionalRotation));
					double rotationSum = 0;
					rotationalVelocity = 0;
					int additionalRotationFrames = 0;
					while (rotationSum < additionalRotation) {
						rotationalVelocity += motion.rotationalAccel;
						rotationSum += rotationalVelocity;
						additionalRotationFrames++;
					}
					double totalRotation = minRotation + additionalRotation;
					double overshoot = rotationSum - additionalRotation;
					Debug.println("DCBA: " + diveCapBounceAngle);
					Debug.println("Total rotation: " + Math.toDegrees(totalRotation));
					Debug.println("Overshoot: " + Math.toDegrees(overshoot));
					//how much counterrotation there should be on the first frame of acceleration
					double firstAdditionalRotationFrameCounterrotation = overshoot / additionalRotationFrames;
					int firstAdditionalRotationFrame = frames - turnaroundFrames - additionalRotationFrames;
					holdingAngles[1] = SimpleMotion.NORMAL_ANGLE - Math.toRadians(1.5); //shifting by 1.5 degrees makes fast turnarounds not reverse the wrong way
					for (int i = 2; i < firstAdditionalRotationFrame; i++) {
						holdingAngles[i] = holdingAngles[i - 1] - TURN_COUNTERROTATION;
					}
					holdingAngles[firstAdditionalRotationFrame] = holdingAngles[firstAdditionalRotationFrame - 1] - firstAdditionalRotationFrameCounterrotation;
					Debug.println(holdingAngles[firstAdditionalRotationFrame]);
					for (int i = firstAdditionalRotationFrame + 1; i < frames - turnaroundFrames; i++) {
						holdingAngles[i] = holdingAngles[i - 1];
					}
					if (turnaroundFrames == 1) {
						holdingAngles[frames - 1] = throwAngle + totalRotation - 136 / 180.0 * Math.PI; //hold as little back as you can
					}
					else { //2 turnaround frames; second should be forward, turnaround as much as you can so it accelerates mario forward
						holdingAngles[frames - 2] = holdingAngles[frames - turnaroundFrames - 1] - 179/180.0 * Math.PI;
						holdingAngles[frames - 1] = holdingAngles[frames - 2] - 179/180.0 * Math.PI;
					}
					boolean[] holdingMinRadius = new boolean[frames];
					holdingMinRadius[frames - turnaroundFrames] = true;
					Debug.println(holdingAngles[firstAdditionalRotationFrame]);
					motion.setHolding(holdingAngles, holdingMinRadius);
					return;
				}
			}
			else { //we need to fast turnaround both directions to get to the dive angle
				rotationalVelocity = 0;
				double rotation = 0;
				holdingAngles[1] = SimpleMotion.NORMAL_ANGLE;
				for (int i = 1; i < frames - 4; i++) {
					rotation += Math.toRadians(.3);
					if (i > 1) {
						holdingAngles[i] = holdingAngles[i - 1] - TURN_COUNTERROTATION;
					}
				}
				rotation -= Math.toRadians(.075); //rotation on frames - 4 frame
				// for (int i = 1; i < frames - 4; i++) {
					
				// }
				// holdingAngles[frames - 4] = diveAngle - FAST_TURNAROUND_VELOCITY;
				// holdingAngles[frames - 3] = diveAngle - FAST_TURNAROUND_VELOCITY - 179 / 180.0 * Math.PI;
				// holdingAngles[frames - 2] = diveAngle - FAST_TURNAROUND_VELOCITY;
				// holdingAngles[frames - 1] = holdingAngles[frames - 2] + 136 / 180.0 * Math.PI;
				holdingAngles[frames - 3] = throwAngle + rotation + 135.1 / 180.0 * Math.PI;
				holdingAngles[frames - 4] = holdingAngles[frames - 3] + 179 / 180.0 * Math.PI;
				holdingAngles[frames - 2] = diveAngle - FAST_TURNAROUND_VELOCITY;
				holdingAngles[frames - 1] = holdingAngles[frames - 2] + 135.1 / 180.0 * Math.PI;
				boolean[] holdingMinRadius = new boolean[frames];
				holdingMinRadius[frames - 4] = true;
				holdingMinRadius[frames - 3] = true;
				holdingMinRadius[frames - 1] = true;
				motion.setHolding(holdingAngles, holdingMinRadius);
				return;
			}
		}
		else { //not hyperoptimized, or edge cap bounce angle is 0 and we can use pre-determined holding angles to vector efficiently
			if (frames < 7) {
				for (int i = 1; i < frames; i++) {
					holdingAngles[i] = angle;
				}
			}
			else if (frames == 7) {
				holdingAngles[1] = SimpleMotion.NORMAL_ANGLE; //.3
				holdingAngles[2] = SimpleMotion.NORMAL_ANGLE; //.6
				holdingAngles[3] = SimpleMotion.NORMAL_ANGLE; //.9
				holdingAngles[4] = angle - Math.toRadians(.5); //1.5
				holdingAngles[5] = angle - Math.toRadians(.5); //.9
				holdingAngles[6] = angle - Math.toRadians(.5); //0 //this needs to be greater than 1 away so that we don't experience the deceleration
			}
			else if (frames == 8) {
				holdingAngles[1] = SimpleMotion.NORMAL_ANGLE; //.3
				holdingAngles[2] = SimpleMotion.NORMAL_ANGLE - TURN_COUNTERROTATION; //.3 //really should be .3 but this makes it slightly inaccurate
				holdingAngles[3] = SimpleMotion.NORMAL_ANGLE - TURN_COUNTERROTATION; //.6
				holdingAngles[4] = SimpleMotion.NORMAL_ANGLE - TURN_COUNTERROTATION - TRUE_TURN_COUNTERROTATION; //.6
				holdingAngles[5] = angle - Math.toRadians(.5); //1.5
				holdingAngles[6] = angle - Math.toRadians(.5); //.9
				holdingAngles[7] = angle - Math.toRadians(.5); //0 //this needs to be greater than 1 away so that we don't experience the deceleration
			}
			else if (frames <= 14 && p.hyperoptimize) {
				if (frames == 9) {
					holdingAngles[1] = SimpleMotion.NORMAL_ANGLE; //.3
					holdingAngles[2] = SimpleMotion.NORMAL_ANGLE - TURN_COUNTERROTATION; //.3
					holdingAngles[3] = SimpleMotion.NORMAL_ANGLE - 2 * TURN_COUNTERROTATION; //.3
					holdingAngles[4] = SimpleMotion.NORMAL_ANGLE - 3 * TURN_COUNTERROTATION; //.3
					holdingAngles[5] = SimpleMotion.NORMAL_ANGLE - 3 * TURN_COUNTERROTATION; //.6
					holdingAngles[6] = angle - Math.toRadians(.5); //1.5
					holdingAngles[7] = angle - Math.toRadians(.5); //.9
					holdingAngles[8] = angle - Math.toRadians(.5); //0
				}
				else if (frames == 10) {
					holdingAngles[1] = SimpleMotion.NORMAL_ANGLE; //.3
					holdingAngles[2] = SimpleMotion.NORMAL_ANGLE - TURN_COUNTERROTATION; //.3
					holdingAngles[3] = SimpleMotion.NORMAL_ANGLE - 2 * TURN_COUNTERROTATION; //.3
					holdingAngles[4] = SimpleMotion.NORMAL_ANGLE - 3 * TURN_COUNTERROTATION; //.3
					holdingAngles[5] = SimpleMotion.NORMAL_ANGLE - 4 * TURN_COUNTERROTATION; //.3
					holdingAngles[6] = SimpleMotion.NORMAL_ANGLE - 5 * TURN_COUNTERROTATION; //.3
					holdingAngles[7] = angle - Math.toRadians(.5); //1.5
					holdingAngles[8] = angle - Math.toRadians(.5); //.9
					holdingAngles[9] = angle - Math.toRadians(.5); //0
				}
				else if (frames == 11) {
					holdingAngles[1] = SimpleMotion.NORMAL_ANGLE; //.3
					holdingAngles[2] = SimpleMotion.NORMAL_ANGLE - TURN_COUNTERROTATION; //.3
					holdingAngles[3] = SimpleMotion.NORMAL_ANGLE - 2 * TURN_COUNTERROTATION; //.3
					holdingAngles[4] = SimpleMotion.NORMAL_ANGLE - 2 * TURN_COUNTERROTATION; //.6
					holdingAngles[5] = SimpleMotion.NORMAL_ANGLE - 2 * TURN_COUNTERROTATION - TRUE_TURN_COUNTERROTATION; //.6
					holdingAngles[6] = SimpleMotion.NORMAL_ANGLE - 2 * TURN_COUNTERROTATION - TRUE_TURN_COUNTERROTATION; //.9
					holdingAngles[7] = angle;
					holdingAngles[8] = angle;
					holdingAngles[9] = angle;
					holdingAngles[10] = angle;
				}
				else if (frames == 12) {
					holdingAngles[1] = SimpleMotion.NORMAL_ANGLE; //.3
					holdingAngles[2] = SimpleMotion.NORMAL_ANGLE - TURN_COUNTERROTATION; //.3
					holdingAngles[3] = SimpleMotion.NORMAL_ANGLE - 2 * TURN_COUNTERROTATION; //.3
					holdingAngles[4] = SimpleMotion.NORMAL_ANGLE - 3 * TURN_COUNTERROTATION; //.3
					holdingAngles[5] = SimpleMotion.NORMAL_ANGLE - 4 * TURN_COUNTERROTATION; //.3
					holdingAngles[6] = SimpleMotion.NORMAL_ANGLE - 4 * TURN_COUNTERROTATION; //.6
					holdingAngles[7] = SimpleMotion.NORMAL_ANGLE - 4 * TURN_COUNTERROTATION; //.9
					holdingAngles[8] = angle;
					holdingAngles[9] = angle;
					holdingAngles[10] = angle;
					holdingAngles[11] = angle;
				}
				else if (frames == 13) {
					holdingAngles[1] = SimpleMotion.NORMAL_ANGLE; //.3
					holdingAngles[2] = SimpleMotion.NORMAL_ANGLE - TURN_COUNTERROTATION; //.3
					holdingAngles[3] = SimpleMotion.NORMAL_ANGLE - 2 * TURN_COUNTERROTATION; //.3
					holdingAngles[4] = SimpleMotion.NORMAL_ANGLE - 3 * TURN_COUNTERROTATION; //.3
					holdingAngles[5] = SimpleMotion.NORMAL_ANGLE - 4 * TURN_COUNTERROTATION; //.3
					holdingAngles[6] = SimpleMotion.NORMAL_ANGLE - 5 * TURN_COUNTERROTATION; //.3
					holdingAngles[7] = SimpleMotion.NORMAL_ANGLE - 5 * TURN_COUNTERROTATION; //.6
					holdingAngles[8] = SimpleMotion.NORMAL_ANGLE - 5 * TURN_COUNTERROTATION - TRUE_TURN_COUNTERROTATION; //.6
					holdingAngles[9] = angle;
					holdingAngles[10] = angle;
					holdingAngles[11] = angle;
					holdingAngles[12] = angle;
				}
				else if (frames == 14) {
					holdingAngles[1] = SimpleMotion.NORMAL_ANGLE; //.3
					holdingAngles[2] = SimpleMotion.NORMAL_ANGLE - TURN_COUNTERROTATION; //.3
					holdingAngles[3] = SimpleMotion.NORMAL_ANGLE - 2 * TURN_COUNTERROTATION; //.3
					holdingAngles[4] = SimpleMotion.NORMAL_ANGLE - 3 * TURN_COUNTERROTATION; //.3
					holdingAngles[5] = SimpleMotion.NORMAL_ANGLE - 4 * TURN_COUNTERROTATION; //.3
					holdingAngles[6] = SimpleMotion.NORMAL_ANGLE - 5 * TURN_COUNTERROTATION; //.3
					holdingAngles[7] = SimpleMotion.NORMAL_ANGLE - 6 * TURN_COUNTERROTATION; //.3
					holdingAngles[8] = SimpleMotion.NORMAL_ANGLE - 7 * TURN_COUNTERROTATION; //.3
					holdingAngles[9] = SimpleMotion.NORMAL_ANGLE - 7 * TURN_COUNTERROTATION; //.6
					holdingAngles[10] = angle;
					holdingAngles[11] = angle;
					holdingAngles[12] = angle;
					holdingAngles[13] = angle;
				}
			}
			else {
				int lastNormalAngleFrame = (frames - 1) / 2;
				//int lastNormalAngleFrame = frames - 2;
				for (int i = 1; i <= lastNormalAngleFrame; i++)
					holdingAngles[i] = SimpleMotion.NORMAL_ANGLE;
				for (int i = lastNormalAngleFrame + 1; i < frames; i++)
					holdingAngles[i] = angle;
			}
			motion.setHoldingAngles(holdingAngles);
		}
	}

	//idea: vector perfectly for as long as you can
	//then initiate a turnaround but then go back to vectoring afterward
	//the last frame hold the exact direction you want to go in
	//OR if turning around for x frames is almost enough, just vector more weakly to start with
	//angle is dive angle
	//it seems that turnaroundFrames is always 3
	private void setFinalCapThrowHoldingAngles(ComplexVector motion, double angle, int frames) {
		double[] holdingAngles = new double[frames];
		if (p.hyperoptimize) {
			double initialHoldingAngle = SimpleMotion.NORMAL_ANGLE;
			double ang_deg = Math.toDegrees(SimpleMotion.NORMAL_ANGLE - angle);
			Debug.println("Final Cap Throw Dive Angle: " + ang_deg);
			int turnaroundFrames;
			double difference; //difference between exact turnaround and how much Mario needs to turn around
			if (ang_deg <= 25 + FINAL_CT_ANGLE_REDUCTION_LIMIT) {
				turnaroundFrames = 1;
				difference = ang_deg - 25;
			}
			else if (ang_deg <= 25 + 22.5 + FINAL_CT_ANGLE_REDUCTION_LIMIT) {
				turnaroundFrames = 2;
				difference = ang_deg - 25 - 22.5;
			}
			else if (ang_deg <= 25 + 22.5 + 20 + FINAL_CT_ANGLE_REDUCTION_LIMIT) {
				turnaroundFrames = 3;
				difference = ang_deg - 25 - 22.5 - 20;
			}
			else {
				turnaroundFrames = 4;
				difference = ang_deg - 25 - 22.5 - 20 - 17.5;
			}
			if (difference > 0) {
				initialHoldingAngle = SimpleMotion.NORMAL_ANGLE - Math.toRadians(difference);
			}
			for (int i = 0; i < frames - turnaroundFrames; i++) {
				holdingAngles[i] = initialHoldingAngle;
			}
			Debug.println("Turnaround Frames: " + turnaroundFrames);
			holdingAngles[frames - turnaroundFrames] = initialHoldingAngle + Math.PI * 181/180.0;
			if (turnaroundFrames > 1)
				holdingAngles[frames - turnaroundFrames + 1] = initialHoldingAngle + Math.PI * 2/180.0;
			if (turnaroundFrames > 2)
				holdingAngles[frames - turnaroundFrames + 2] = initialHoldingAngle - Math.PI * 5/180.0;
			if (turnaroundFrames > 3)
				holdingAngles[frames - turnaroundFrames + 3] = initialHoldingAngle - Math.PI * 9/180.0;
			if (difference < -0.001) {
				holdingAngles[frames - 1] = angle;
			}
			boolean[] holdingMinRadius = new boolean[frames];
			holdingMinRadius[frames - turnaroundFrames] = true;
			motion.setHolding(holdingAngles, holdingMinRadius);
		}
		else {
			int lastNormalAngleFrame = (frames - 1) / 2;
			//int lastNormalAngleFrame = frames - 2;
			for (int i = 1; i <= lastNormalAngleFrame; i++)
				holdingAngles[i] = SimpleMotion.NORMAL_ANGLE;
			for (int i = lastNormalAngleFrame + 1; i < frames; i++)
				holdingAngles[i] = angle;
			motion.setHoldingAngles(holdingAngles);
		}
	}

	private void setFinalFallingHoldingAngles(ComplexVector motion, double neededRotation, double initialHoldingAngle, int frames) {
		Debug.println("Needed Rotation: " + Math.toDegrees(neededRotation));
		Debug.println("Initial Holding Angle: " + Math.toDegrees(initialHoldingAngle));
		double[] holdingAngles = new double[frames];
		for (int i = 0; i < holdingAngles.length; i++) {
			holdingAngles[i] = initialHoldingAngle;
		}
		//holdingAngles[0] = 
		double ang_deg = Math.toDegrees(neededRotation);
		double rotationalVelocity = 0;
		int turnaroundFrames = 0;
		//double rotation = 0;
		for (int remainingFrames = frames; remainingFrames >= 0; remainingFrames--) {
			if (ang_deg > 67.5 && remainingFrames == 4) {
			 	turnaroundFrames = 4;
			}
			if (ang_deg > 47.5 && remainingFrames == 3) {
				turnaroundFrames = 3;
			}
			else if (remainingFrames == 2) {
				turnaroundFrames = 2;
			}
			else {
				rotationalVelocity += .3; //fix if really long?
				ang_deg -= rotationalVelocity;
			}
		}
		Debug.println("Turanround Frames: " + turnaroundFrames);
		holdingAngles[frames - turnaroundFrames] = initialHoldingAngle - Math.toRadians(ang_deg) + Math.PI * 136/180.0;
		if (turnaroundFrames > 1)
			holdingAngles[frames - turnaroundFrames + 1] = initialHoldingAngle + Math.PI * 2/180.0;
		if (turnaroundFrames > 2)
			holdingAngles[frames - turnaroundFrames + 2] = initialHoldingAngle - Math.PI * 5/180.0;
		if (turnaroundFrames > 3)
			holdingAngles[frames - turnaroundFrames + 3] = initialHoldingAngle - Math.PI * 9/180.0;
		holdingAngles[frames - 1] = initialHoldingAngle;
		boolean[] holdingMinRadius = new boolean[frames];
		holdingMinRadius[frames - turnaroundFrames] = true;
		motion.setHolding(holdingAngles, holdingMinRadius);

	}
	
	private void setOtherMovementHoldingAngles(ComplexVector motion, SimpleMotion[] motionGroup, int index, double angle, double initialAngle, double initialRotation, boolean rightVector) {
		Movement movement;
		SimpleVector angleCalculator;
		int frames = movementFrames.get(index);
		if (motionGroup.length > 0) {
			movement = new Movement(movementNames.get(index), motionGroup[motionGroup.length - 1].finalSpeed);
			angleCalculator = (SimpleVector) movement.getMotion(frames, rightVector, false);
			angleCalculator.setInitialAngle(initialAngle);
			//Debug.println("Initial Angle: " + Math.toDegrees(initialAngle));
			angleCalculator.setInitialRotation(initialRotation);
			//Debug.println("Initial Rotation: " + Math.toDegrees(initialRotation));
		}
		else {
			movement = new Movement(movementNames.get(index), listPreparer.initialVelocity);
			angleCalculator = (SimpleVector) movement.getMotion(frames, rightVector, false);
			angleCalculator.setInitialAngle(Math.PI / 2);
			angleCalculator.setInitialRotation(Math.PI / 2);
		}
		
		double[] rotations = angleCalculator.calcRelativeRotations();
		double[] holdingAngles = new double[frames];
		boolean[] holdingMinRadius = new boolean[frames];

		 for (int z = 0; z < frames; z++) {
			if (Debug.debug) {
				System.out.printf("Frame %d, Rotation %.3f\n", z, Math.toDegrees(rotations[z]));
			}
		}
		Debug.println(); 

		double rotationWithFastTurnaround = -Double.MAX_VALUE;
		int listIndex = -1; //which type of fast turnaround from the list that we're checking
		while (rotationWithFastTurnaround < angle && listIndex < fastTurnarounds.length - 1) {
			listIndex++;
			rotationWithFastTurnaround = rotations[frames - 1 - fastTurnaroundFrames[listIndex]] - fastTurnarounds[listIndex];
		}
		if (listIndex >= fastTurnarounds.length) {
			listIndex = fastTurnarounds.length - 1;
		}
		double turnaroundRotation = fastTurnarounds[listIndex];
		int turnaroundFrames = fastTurnaroundFrames[listIndex];
		int firstTurnaroundFrame = frames - turnaroundFrames;
		int i = 0;
		while (i < rotations.length && rotations[i] < turnaroundRotation + angle) {
			holdingAngles[i] = angleCalculator.holdingAngle; //NORMAL_ANGLE unless dive cap bounce where it's slightly less to account for error
			i++;
		}
		boolean overshot = false;
		if (i < rotations.length && i > 1 && rotations[i] != turnaroundRotation + angle) { //we overshoot on this frame if we don't adjust the holding angle
			double previousVelocity = rotations[i - 1] - rotations[i - 2];
			double neededVelocity = (turnaroundRotation + angle) - rotations[i - 1];
			if (neededVelocity < angleCalculator.rotationalAccel) { //we can't rotate this small, so overshoot by .3 degrees (the rotational acceleration)
				neededVelocity += angleCalculator.rotationalAccel;
				overshot = true;
			}
			holdingAngles[i] = angleCalculator.holdingAngle - (previousVelocity + angleCalculator.rotationalAccel - neededVelocity);
			rotations[i] = rotations[i - 1] + neededVelocity;
			i++;
		}
		for (; i < firstTurnaroundFrame && i < rotations.length; i++) {
			if (overshot) {
				holdingAngles[i] = turnaroundRotation + angle - Math.toRadians(1);
				overshot = false;
			}
			else {
				holdingAngles[i] = turnaroundRotation + angle;
			}
			rotations[i] = turnaroundRotation + angle;
		}
		//finally, apply the turnaround so we end up at the desired angle
		double fastTurnaroundVelocity = FAST_TURNAROUND_VELOCITY;
		for (; i < frames; i++) {
			if (i == firstTurnaroundFrame) {
				holdingAngles[i] = rotations[i - 1] - FAST_TURNAROUND_ANGLE;
				rotations[i] = rotations[i - 1] - FAST_TURNAROUND_VELOCITY;
			}
			else if (i < firstTurnaroundFrame + maxVelocityFastTurnaroundFrames[listIndex]) {
				holdingAngles[i] = holdingAngles[i - 1] - FAST_TURNAROUND_VELOCITY; //need to rotate by another 25 degrees
				rotations[i] = rotations[i - 1] - FAST_TURNAROUND_VELOCITY;
			}
			else { //fast turnaround decelerates as we keep holding the same angle
				fastTurnaroundVelocity -= FAST_TURNAROUND_ACCEL;
				holdingAngles[i] = holdingAngles[i - 1];
				rotations[i] = rotations[i - 1] - fastTurnaroundVelocity;
			}
			holdingMinRadius[i] = true;
		}
		 for (int z = 0; z < frames; z++) {
			if (Debug.debug) {
				System.out.printf("Frame %d, Rotation %.3f\n", z, Math.toDegrees(rotations[z]));
			}
		}
		Debug.println(); 
		motion.setHolding(holdingAngles, holdingMinRadius);
	}
	
	private SimpleMotion[] calcMotionGroup(int startIndex, int endIndex, double initialVelocity, int framesJump) {
		SimpleMotion[] motionGroup = new SimpleMotion[endIndex - startIndex];
		if (motionGroup.length == 0)
			return motionGroup;
		
		//calculate the trajectory of the inital movement

		//case for roll cancel vectors (note it assumes at least 1 falling frame afterward)
		int nextIndex;
		//if we're the first motion group and there's a variable roll cancel
		if (hasVariableRollCancel && startIndex == 0) {
			nextIndex = 2;
			Movement rc = new Movement(movementNames.get(0), initialVelocity);
			GroundedCapThrow rcMotion = new GroundedCapThrow(rc, Math.PI / 2, rcTrueInitialAngleDiff, rcFinalAngleDiff, !currentVectorRight);
			//GroundedCapThrow rcMotion = new GroundedCapThrow(rc, Math.PI / 2, RcvTool.calcRCFinalAngleDiff(rc.movementType, initialVelocity, movementFrames.get(startIndex + 1)), !currentVectorRight);
			rcMotion.calcDispDispCoordsAngleSpeed();
			Movement rcv = new Movement("Falling", rcMotion.finalSpeed);
			rcv.initialVerticalSpeed = -7;
			SimpleVector rcvMotion = new SimpleVector(rcv, rcMotion.finalAngle, SimpleMotion.NORMAL_ANGLE, currentVectorRight, movementFrames.get(startIndex + 1));
			rcvMotion.calcDispDispCoordsAngleSpeed();
			motionGroup[0] = rcMotion;
			motionGroup[1] = rcvMotion;
			currentVectorRight = !currentVectorRight;
		}
		else {
			nextIndex = 1;
			Movement initialMovement = new Movement(movementNames.get(startIndex), initialVelocity, framesJump); //need to add frames jump if want to use that here
			// if (movementNames.get(startIndex).equals("Triple Jump") && true) {
			// 	motionGroup[0] = initialMovement.getMotion(movementFrames.get(startIndex), currentVectorRight, true);
			// 	double[] holdingAngles = new double[movementFrames.get(startIndex)];
			// 	int framesCountervector = 1;
			// 	// for (int a = 0; a < 5; a++) {
			// 	// 	holdingAngles[a] = SimpleMotion.NO_ANGLE;
			// 	// }
			// 	for (int a = 0; a < holdingAngles.length - framesCountervector; a++) {
			// 		holdingAngles[a] = SimpleMotion.NORMAL_ANGLE;
			// 	}
			// 	for (int a = holdingAngles.length - framesCountervector; a < holdingAngles.length; a++) {
			// 		holdingAngles[a] = SimpleMotion.NORMAL_ANGLE;
			// 	}
			// 	((ComplexVector) motionGroup[0]).setHoldingAngles(holdingAngles);
			// }
			//else
			motionGroup[0] = initialMovement.getMotion(movementFrames.get(startIndex), currentVectorRight, false);
			motionGroup[0].setInitialAngle(Math.PI / 2);
			motionGroup[0].calcDispDispCoordsAngleSpeed();
			if (!motionGroup[0].getClass().getSimpleName().equals("SimpleMotion") || (diveTurn && movementNames.get(startIndex).equals("Ground Pound")))
				currentVectorRight = !currentVectorRight;
		}

		for (int i = nextIndex; i < motionGroup.length; i++) {
			int j = i + startIndex;
			Movement currentMovement = new Movement(movementNames.get(j), motionGroup[i - 1].finalSpeed);
			if (movementNames.get(j).equals("Homing Motion Cap Throw")) {			
				motionGroup[i] = currentMovement.getMotion(movementFrames.get(j), currentVectorRight, true);
				((ComplexVector) motionGroup[i]).setHoldingAngles(homingMotionThrowHoldingAngles);
			}
			// else if (movementNames.get(j).equals("Rainbow Spin") && simpleTech) {
			// 	motionGroup[i] = currentMovement.getMotion(movementFrames.get(j), currentVectorRight, true);
			// 	Debug.println("Simple tech!");
			// 	((ComplexVector) motionGroup[i]).setHoldingAngles(rainbowSpinHoldingAngles);
			// }
			// else if (movementNames.get(j).equals("Rainbow Spin")) {
			// 	motionGroup[i] = currentMovement.getMotion(movementFrames.get(j), currentVectorRight, true);
			// 	double[] holdingAngles = new double[movementFrames.get(j)];
			// 	int framesCountervector = 1;
			// 	for (int a = 0; a < holdingAngles.length - framesCountervector; a++) {
			// 		holdingAngles[a] = SimpleMotion.NORMAL_ANGLE;
			// 	}
			// 	for (int a = holdingAngles.length - framesCountervector; a < holdingAngles.length; a++) {
			// 		holdingAngles[a] = -.5*SimpleMotion.NORMAL_ANGLE;
			// 	}
			// 	((ComplexVector) motionGroup[i]).setHoldingAngles(holdingAngles);
			// }
			// else if (movementNames.get(j).equals("Dive Cap Bounce")) {
			// 	motionGroup[i] = currentMovement.getMotion(movementFrames.get(j), currentVectorRight, true);
			// 	double[] holdingAngles = new double[movementFrames.get(j)];
			// 	int framesCountervector = 1;
			// 	for (int a = 0; a < holdingAngles.length - framesCountervector; a++) {
			// 		holdingAngles[a] = SimpleMotion.NORMAL_ANGLE;
			// 	}
			// 	for (int a = holdingAngles.length - framesCountervector; a < holdingAngles.length; a++) {
			// 		holdingAngles[a] = SimpleMotion.NORMAL_ANGLE;
			// 	}
			// 	((ComplexVector) motionGroup[i]).setHoldingAngles(holdingAngles);
			// }
			else if (movementNames.get(j).equals("Dive")) {
				preCapBounceDiveIndex = j;
				motionGroup[i] = currentMovement.getMotion(movementFrames.get(j), currentVectorRight, true);
				((DiveTurn) motionGroup[i]).firstFrameDecel = firstFrameDecel;
				if (!diveTurn) {
					((DiveTurn) motionGroup[i]).setHoldingAngle(0);
				}
			}
			else
				motionGroup[i] = currentMovement.getMotion(movementFrames.get(j), currentVectorRight, false);
			if (hasVariableHCTFallVector && j == variableHCTFallIndex) { //use the holding angle we are testing this iteration for optimizing the HCT fall	
				((SimpleVector) motionGroup[i]).setHoldingAngle(variableHCTHoldingAngle);
				// Debug.println("Testing: " + variableHCTHoldingAngle);
				if (movementFrames.get(j) <= 3) {
					((SimpleVector) motionGroup[i]).optimalForwardAccel = false; //may need to not hold straight ahead in the falling frames even though under max speed
				}
				if (!switchHCTFallVectorDir) {
					currentVectorRight = !currentVectorRight;
				}
				Debug.println("HCT Optimize Branch Activated!");
			}
			motionGroup[i].setInitialAngle(motionGroup[i - 1].finalAngle);
			// Debug.println("Previous angle: " + Math.toDegrees(motionGroup[i - 1].finalAngle));
			// Debug.println("It was a: " + movementNames.get(j - 1));
			// Debug.println("It is a: " + movementNames.get(j));
			motionGroup[i].calcDispDispCoordsAngleSpeed();
			//if the movement is falling, switch the vector only if j is the index of the hct AND we are hct second
			//if the movement is an HCT, do not switch the vector if HCT second
			if (!(movementNames.get(j).equals("Falling") || motionGroup[i].getClass().getSimpleName().equals("SimpleMotion")))
				if (!(!switchHCTFallVectorDir && j == variableHCTFallIndex - 1))
					currentVectorRight = !currentVectorRight;
		}
		
		for (SimpleMotion m : motionGroup)
			Debug.println(m.dispZ + ", " + m.dispX);
		
		System.arraycopy(motionGroup, 0, motions, startIndex, motionGroup.length);
		
		return motionGroup;
	}

	public double calcRCFinalAngleDiff(String movementType, double initialVelocity, int framesRCV) {
        double low = 0;
        double high = Math.PI / 4; //strongest you can vector is 45 degrees
        int i = 0;
        double test = 0;
        while (i < RCV_MAX_ITERATIONS) {
            test = (high + low) / 2; //binary search for the correct angle
            //Debug.println("Testing " + Math.toDegrees(test));
            Movement rcCapThrow = new Movement(movementType, initialVelocity);
            GroundedCapThrow rcMotion = new GroundedCapThrow(rcCapThrow, 0, rcTrueInitialAngleDiff, test, true);
            rcMotion.calcDispDispCoordsAngleSpeed();
            //Debug.println("Final Angle: " + Math.toDegrees(rcMotion.finalAngle));
            Movement rcv = new Movement("Falling", rcMotion.finalSpeed);
            SimpleVector rcvMotion = new SimpleVector(rcv, rcMotion.finalAngle, SimpleMotion.NORMAL_ANGLE, false, framesRCV);
            rcvMotion.calcDispDispCoordsAngleSpeed();
            double sumDispZ = rcMotion.dispX + rcvMotion.dispX;
            Debug.println("Disp Z sum: " + sumDispZ);
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
	
	public SimpleMotion[] getMotions() {
		return motions;
	}
	
	public double getInitialAngle() {
		if (p.xAxisZeroDegrees) {
			return Math.PI / 2 - initialAngle;
		}
		else {
			return initialAngle;
		}
	}
	
	public double getTargetAngle() {
		if (p.xAxisZeroDegrees) {
			return Math.PI / 2 - targetAngle;
		}
		else {
			return targetAngle;
		}
	}
	
	//the actual maximization function
	//calls more maximization functions for each step
	//this function finds the correct/optimal RCV if applicable
	//then calls maximize_dive (whether or not to turn dive before cbv)
	//which calls maximize_hct (hct falling vectoring)
	//which calls maximize_variableAngle1 (first cap throw angle)
	public double maximize() {
		long startTime = System.currentTimeMillis();
		
		//SimpleMotion[] motionGroup1 = new SimpleMotion[variableCapThrow1Index];
		//SimpleMotion[] motionGroup2 = new SimpleMotion[variableMovement2Index - motionGroup2Index];
		//int motionGroup3Index;
		
		//currentVectorRight = rightVector;

		diveCapBounceAngle = p.diveCapBounceAngle;
		firstFrameDecel = p.diveFirstFrameDecel;

		if (p.angleType == AngleType.BOTH) {
			if (rightVector) {
				rcTrueInitialAngleDiff = initialAngle - targetAngle;
				//initialAngle -= rcTrueInitialAngleDiff;
			}
			else {
				rcTrueInitialAngleDiff = targetAngle - initialAngle;
				//initialAngle += rcTrueInitialAngleDiff;
			}
			Debug.println("True Diff: " + Math.toDegrees(rcTrueInitialAngleDiff));
		}
		else {
			rcTrueInitialAngleDiff = 0;
		}

		//rcTrueInitialAngleDiff = Math.toRadians(30); //target - initial if initially left vector, initial - target if initially right vector
		if (only_maximize_variableAngle2) {
			maximize_variableAngle1();
		}
		else if (hasVariableRollCancel) {
			if (movementNames.get(0).equals("Optimal Distance RCV")) {
				String bestRCName = "";
				int bestRCFrames = 0;
				int bestRCVFrames = 0;
				double bestRCFinalAngleDiff = 0;
				bestDisp = 0;
				
				//iterate through the RC types and see which is best
				for (int i = 0; i < Movement.RC_TYPES.length; i++) {

					movementNames.set(0, Movement.RC_TYPES[i]);
					Movement rc = new Movement(Movement.RC_TYPES[i]);
					GroundedCapThrow rcMotion = new GroundedCapThrow(rc, false);
					int totalFrames = rcMotion.calcFrames(p.initialDispY);
					movementFrames.set(0, rc.minFrames);
					movementFrames.set(1, totalFrames - rc.minFrames);
					rcFinalAngleDiff = calcRCFinalAngleDiff(movementNames.get(0), listPreparer.initialVelocity, movementFrames.get(1));

					maximize_dive();

					if (once_bestDisp > bestDisp) {
						bestRCName = Movement.RC_TYPES[i];
						bestRCFrames = rc.minFrames;
						bestRCVFrames = totalFrames - rc.minFrames;
						bestDisp = once_bestDisp;
						bestRCFinalAngleDiff = rcFinalAngleDiff;
					}
				}

				//now set up the maximizer to use the roll cancel type we found was best
				movementNames.set(0, bestRCName);
				movementFrames.set(0, bestRCFrames);
				movementFrames.set(1, bestRCVFrames);
				rcFinalAngleDiff = bestRCFinalAngleDiff;
			}
			else {
				rcFinalAngleDiff = calcRCFinalAngleDiff(movementNames.get(0), listPreparer.initialVelocity, movementFrames.get(1));
			}
			
			//optimize the rc, then try to get the rc initial angle to be the same as the target angle
			double bestUnadjustedTargetAngle = Math.PI;
			double unadjustedTargetAngle = Math.PI;
			double increment = 0;
			//on the first iteration just maximize it and see how far off we are
			//then keep nudging it slightly
			for (int i = 1; i <= maxRCVNudges; i++) {
				maximize_dive();
				unadjustedTargetAngle = Math.atan(once_bestDispX / once_bestDispZ);
				if (unadjustedTargetAngle < 0)
					unadjustedTargetAngle += Math.PI;
				unadjustedTargetAngle -= Math.PI / 2;
				if (Math.abs(unadjustedTargetAngle) < Math.abs(bestUnadjustedTargetAngle)) {
					bestUnadjustedTargetAngle = unadjustedTargetAngle;
					bestRCFinalAngleDiff = rcFinalAngleDiff;
					if (Math.abs(unadjustedTargetAngle) < Math.toRadians(0.001)) {
						break;
					}
				}
				if (i == 1) {
					increment = unadjustedTargetAngle * 2 / maxRCVNudges;
				}
				if (rightVector) {
					rcFinalAngleDiff -= increment;
				}
				else {
					rcFinalAngleDiff += increment;
				}
			}
			rcFinalAngleDiff = bestRCFinalAngleDiff;
			//maximize_variableAngle1();

			//hopefully it's small by now; fine tune by nudging by the difference between the initial and target angles
			for (int i = 0; i < maxRCVFineNudges; i++) {
				if (Math.abs(unadjustedTargetAngle) < Math.toRadians(0.00005)) {
					break;
				}

				maximize_dive();

				unadjustedTargetAngle = Math.atan(once_bestDispX / once_bestDispZ);
				if (unadjustedTargetAngle < 0)
					unadjustedTargetAngle += Math.PI;
				unadjustedTargetAngle -= Math.PI / 2;

				//Debug.println("RC Cap Throw Angle Change: " + Math.toDegrees(unadjustedTargetAngle));

				if (rightVector) {
					rcFinalAngleDiff -= unadjustedTargetAngle;
				}
				else {
					rcFinalAngleDiff += unadjustedTargetAngle;
				}
			}
		}
		else {
			maximize_dive();
		}

		bestDispZ = once_bestDispZ;
		bestDispX = once_bestDispX;
		bestDisp = once_bestDisp;
		bestAngle1 = once_bestAngle1;
		bestAngle2 = once_bestAngle2;
		bestAngle1Adjusted = once_bestAngle1Adjusted;
		bestAngle2Adjusted = once_bestAngle2Adjusted;

		Debug.println("Displacement x, y: " + bestDispZ + ", " + bestDispX);
		Debug.println("Maximum displacement: " + bestDisp);
		Debug.println("Angle 1: " + Math.toDegrees(bestAngle1));
		Debug.println("Angle 2: " + Math.toDegrees(bestAngle2));
		Debug.println("Angle 1 Adjusted: " + Math.toDegrees(bestAngle1Adjusted));
		Debug.println("Angle 2 Adjusted: " + Math.toDegrees(bestAngle2Adjusted));
		
		//adjusting motions to the optimized values
		if (hasVariableCapThrow1) {
			//((ComplexVector) motions[variableCapThrow1Index].set
			double adjustment = bestAngle1Adjusted - Math.PI / 2;
			for (int i = 0; i < motionGroup2.length; i++)
				motionGroup2[i].adjustInitialAngle(adjustment);
		}
		
		//set up for calculating vertical velocity
		for (int i = 0; i < motions.length; i++) {
			if (motions[i].movement.movementType.equals("Falling") && i > 0)
				motions[i].movement.initialVerticalSpeed = motions[i - 1].calcFinalVerticalVelocity();
		}

		//rotating motions to the right angle
		//adjustToGivenAngle();
		
		//System.out.println("Angle 1: " + Math.toDegrees(bestAngle1));
		//System.out.println("Angle 2: " + Math.toDegrees(bestAngle2));
		Debug.println("Calculated in " + (System.currentTimeMillis() - startTime) + " ms");

		return bestDisp;
	}
	
	private double[] calcFallingDisplacements(ComplexVector variableCapThrowVector, int variableCapThrowIndex, double variableAngleAdjusted, boolean vectorRight, boolean rotateDuringFall) {
		double[] displacements = new double[2];
		variableCapThrowVector.calcFinalAngle();
		Movement variableCapThrowFalling = new Movement("Falling", variableCapThrowVector.calcFinalSpeed());
		SimpleMotion variableCapThrowFallingVector;
		if (rotateDuringFall) {
			variableCapThrowFallingVector = variableCapThrowFalling.getMotion(movementFrames.get(variableCapThrowIndex + 1), vectorRight, true);
		}
		else {
			variableCapThrowFallingVector = variableCapThrowFalling.getMotion(movementFrames.get(variableCapThrowIndex + 1), vectorRight, false);
		}
		//SimpleVector variableCapThrowFallingVector = (SimpleVector) variableCapThrowFalling.getMotion(movementFrames.get(variableCapThrowIndex + 1), vectorRight, false);
		motions[variableCapThrowIndex + 1] = variableCapThrowFallingVector;
		if (rotateDuringFall) {
			ComplexVector variableCapThrowFallingVectorC = (ComplexVector) variableCapThrowFallingVector;
			variableCapThrowFallingVectorC.setOptimalForwardAccel(false); //not trying to be optimal, simply trying to end up in the right direction
			variableCapThrowFallingVectorC.setInitialAngle(variableCapThrowVector.finalAngle);
			double ctFinalRotation;
			if (variableCapThrowVector.rightVector) {
				ctFinalRotation = variableCapThrowVector.initialAngle - SimpleMotion.NORMAL_ANGLE;
			}
			else {
				ctFinalRotation = -variableCapThrowVector.initialAngle + SimpleMotion.NORMAL_ANGLE;
			}
			setFinalFallingHoldingAngles(variableCapThrowFallingVectorC, variableAngleAdjusted - ctFinalRotation, Math.abs(variableCapThrowVector.finalAngle - variableAngleAdjusted), movementFrames.get(variableCapThrowIndex + 1));
			//variableCapThrowFallingVectorC.setHoldingAngle(0);
		}
		else {
			SimpleVector variableCapThrowFallingVectorS = (SimpleVector) variableCapThrowFallingVector;
			variableCapThrowFallingVectorS.setOptimalForwardAccel(false); //not trying to be optimal, simply trying to end up in the right direction
			variableCapThrowFallingVectorS.setInitialAngle(variableCapThrowVector.finalAngle);
			variableCapThrowFallingVectorS.setHoldingAngle(Math.abs(variableCapThrowVector.finalAngle - variableAngleAdjusted));
		}
		
		variableCapThrowFallingVector.calcDisp();
		variableCapThrowFallingVector.calcDispCoords();
		displacements[0] = variableCapThrowFallingVector.dispZ;
		displacements[1] = variableCapThrowFallingVector.dispX;
		return displacements;
	}

	//optimization step that maximizes the dive turn (on or off)
	private void maximize_dive() {
		if (alwaysDiveTurn) { //only test with dive turn if this boolean is true
			diveTurn = true;
			maximize_HCT();
		}
		else if (neverDiveTurn) {
			diveTurn = false;
			maximize_HCT();
		}
		else {
			diveTurn = false;
			double disp_noDiveTurn = maximize_HCT();
			diveTurn = true;
			double disp_diveTurn = maximize_HCT();
			//Debug.println("With dive turn %.3f\n: " + disp_diveTurn);
			//Debug.println("Without dive turn %.3f\n: " + disp_noDiveTurn);
			if (disp_noDiveTurn > disp_diveTurn) {
				diveTurn = false;
				maximize_HCT();
			}
		}
	}
	//runs maximize_variableAngle1() to find optimal variable angles 1 and 2 for different choices of holding angle for a HCT fall vector OR for a simple tech
	private double maximize_HCT() {
		if (hasVariableHCTFallVector) {
			//Debug.println(maximize_HCT_limit);
			double[] results = binarySearch(-Math.PI / 2, Math.PI / 2, 0, maximize_HCT_limit);
			//Debug.println("Best HCT fall hold: " + Math.toDegrees(results[1]));
			return results[0];
		}
		//not needed anymore since simple tech doesn't actually have this optimization
		/* else if (hasRainbowSpin) {
			simpleTech = true;

			double simple_bestDisp = 0;

			int high = rainbowSpinFrames / 2;

			//double[] results = binarySearch(0, rainbowSpinFrames / 2, 0, .05);
			//Debug.println("Best RS: " + results[1]);

			for (double i = 0; i <= high; i += .1) {
				generateSimpleTechRainbowSpinHoldingAngles(i);

				maximize_variableAngle1();

				if (once_bestDisp > simple_bestDisp) {
					simple_bestDisp = once_bestDisp;
					bestRainbowSpinHoldingAngles = rainbowSpinHoldingAngles;
				}

				rainbowSpinHoldingAngles = bestRainbowSpinHoldingAngles;
				maximize_variableAngle1();
			}
		} */
		else {
			return maximize_variableAngle1();
		}
	}

	//one iteration of maximization of variable angles 1 and 2 if they exist
	private double maximize_variableAngle1() {
		currentVectorRight = rightVector;

		//calculate the total displacement of all the movement before the first cap throw whose angle can be variable
		SimpleMotion[] motionGroup1 = calcMotionGroup(0, Math.min(variableCapThrow1Index, variableMovement2Index), listPreparer.initialVelocity, p.framesJump);
		sumXDisps(motionGroup1);
		sumYDisps(motionGroup1);
		dispZMotionGroup1 = dispZ;
		dispXMotionGroup1 = dispX;
		motionGroup1FinalAngle = Math.PI / 2;
		if (motionGroup1.length > 0) {
			motionGroup1FinalAngle = motionGroup1[motionGroup1.length - 1].finalAngle;
		}
		
		Debug.println("Group 1 displacement x, y: " + dispZMotionGroup1 + ", " + dispXMotionGroup1);
		//Debug.println("Group 1 displacement: " + dispMotionGroup1);
		//Debug.println("Group 1 angle: " + Math.toDegrees(angleMotionGroup1));
		
		//the holding angle for the first variable cap throw
		
		once_bestAngle1 = 0;
		once_bestAngle2 = 0;
		
		once_bestAngle1Adjusted = 0;
		once_bestAngle2Adjusted = 0;
		
		once_bestDispZ = dispZMotionGroup1;
		once_bestDispX = dispXMotionGroup1;
		
		bestDispZ1 = once_bestDispZ;
		bestDispX1 = once_bestDispX;
		
		once_bestDisp = Math.sqrt(Math.pow(dispZMotionGroup1, 2) + Math.pow(dispXMotionGroup1, 2));
		
		//Debug.println(variableCapThrow1VectorRight);
		
		//SimpleMotion[] motionGroup2 = null;
		
		//first variable movement
		if (hasVariableCapThrow1) {
			//we need a motion group 2
			variableCapThrow1VectorRight = currentVectorRight;
			currentVectorRight = !currentVectorRight;
			
			motionGroup2VectorRight = currentVectorRight;
			motionGroup2 = calcMotionGroup(motionGroup2Index, variableMovement2Index, 0, 0); //last velocity does not currently matter as there is a ground pound then dive
			Debug.println("Motion group 2 calculation:");
			calcAll(motionGroup2);
			dispMotionGroup2 = disp;
			motionGroup2Angle = Math.PI / 2 - Math.abs(angle);
			//Debug.println("Motion group 2 final movement: " + motionGroup2[motionGroup2.length - 1].movement.movementType);
			if (motionGroup2VectorRight)
				motionGroup2FinalAngle = -(motionGroup2[motionGroup2.length - 1].finalAngle - Math.PI / 2);
			else
				motionGroup2FinalAngle = motionGroup2[motionGroup2.length - 1].finalAngle - Math.PI / 2;
				
			motionGroup2FinalRotation = calcFinalRotation(motionGroup2);
			// Debug.println("MG2 final angle: " + Math.toDegrees(motionGroup2FinalAngle));
			// Debug.println("MG2 final rotation: " + Math.toDegrees(motionGroup2FinalRotation));
			
			Debug.println("motion group 2 disp: " + dispMotionGroup2);
			
			//optimize the first variable cap throw
			Movement variableCapThrow1 = new Movement(movementNames.get(variableCapThrow1Index), motions[variableCapThrow1Index - 1].finalSpeed);
			variableCapThrow1Frames = movementFrames.get(variableCapThrow1Index);
			//System.out.println("frames: " + variableCapThrow1Frames);
			variableCapThrow1Vector = (ComplexVector) variableCapThrow1.getMotion(variableCapThrow1Frames, variableCapThrow1VectorRight, true);
			motions[variableCapThrow1Index] = variableCapThrow1Vector;
			variableCapThrow1Vector.setInitialAngle(motionGroup1FinalAngle);
			
			//double oldTestDisp = 0;

			//test various angles for the first cap throw
			//for (int i = 0; i < numSteps; i++) {
		//	for (int i = 0; i < 1; i++) {
		
			double low = 0;
			double high = Math.PI / 2;
			double med = Math.PI / 4;
			double medDisp = calcDisp(med); 
			double lowMed;
			double lowMedDisp;
			double highMed;
			double highMedDisp;
			double radius = Math.PI / 8;

			//skips this step
			if (only_maximize_variableAngle2) {
				med = bestAngle1;
				radius = 0;
			}

			//binary search-ish algorithm to find maximum
			//this works because the function is increasing/flat until the maximum, then decreasing/flat after
			while (radius > Math.toRadians(.05)) {
				//Debug.println("Med: " + Math.toDegrees(med));
				lowMed = med - radius;
				highMed = med + radius;
				lowMedDisp = calcDisp(lowMed);
				highMedDisp = calcDisp(highMed);
				if (lowMedDisp > medDisp && lowMedDisp > highMedDisp) { //maximum is in the left half
					low = low;
					med = lowMed;
					high = med;
					medDisp = lowMedDisp;
				}
				else if (highMedDisp > medDisp && highMedDisp > lowMedDisp) { //maximum is in the right half
					low = med;
					med = highMed;
					high = high;
					medDisp = highMedDisp;
				}
				else { //maximum is in the middle half
					low = lowMed;
					med = med;
					high = highMed;
					medDisp = medDisp;
				}
				radius /= 2;
			}
			once_bestAngle1 = med;
			once_bestDisp = calcDisp(once_bestAngle1);
			//the variables on the right in these assignments are set by the previous call
			once_bestAngle2 = variableAngle2;
			if (hasVariableCapThrow2 || hasVariableOtherMovement2) {
				once_bestDispZ = testDispZ2;
				once_bestDispX = testDispX2;
			}
			else {
				once_bestDispZ = testDispZ1;
				once_bestDispX = testDispX1;
			}
			bestDispZ1 = testDispZ1;
			bestDispX1 = testDispX1;
			once_bestAngle1Adjusted = variableAngle1Adjusted;
			once_bestAngle2Adjusted = variableAngle2Adjusted;
				//variableAngle1 =  i / ((double) numSteps - 1) * Math.PI / 2;
		//		variableAngle1 = Math.toRadians(26.126126126126128);
			//}
			
			//set cap throw 1 vector and falling vector to the correct angles
			variableCapThrow1FallingFrames = 0;
			if (hasVariableCapThrow1Falling) {
				variableCapThrow1FallingFrames = movementFrames.get(variableCapThrow1Index + 1);
			}
			setCapThrowHoldingAngles(variableCapThrow1Vector, once_bestAngle1, variableCapThrow1Frames, variableCapThrow1FallingFrames);
			variableCapThrow1Vector.calcDisp();
			if (hasVariableCapThrow1Falling)
				calcFallingDisplacements(variableCapThrow1Vector, variableCapThrow1Index, once_bestAngle1Adjusted, !variableCapThrow1VectorRight, false);
			//recalculate variable cap throw or movement 2 for the best angle 1
			if (hasVariableCapThrow2 || hasVariableOtherMovement2) {
				double motionGroup2AdjustedFinalAngle = once_bestAngle1Adjusted - booleanToPlusMinus(motionGroup2VectorRight) * motionGroup2FinalAngle;
				double motionGroup2FinalRotationAdjusted = motionGroup2FinalRotation + once_bestAngle1Adjusted - Math.PI / 2;
				Debug.println("Adjusted mg2 final rotation: " + Math.toDegrees(motionGroup2FinalRotationAdjusted));
				Debug.println("Adjusted mg2 final angle: " + Math.toDegrees(motionGroup2AdjustedFinalAngle));
				findVariableAngle2(motionGroup2, motionGroup2AdjustedFinalAngle, motionGroup2FinalRotationAdjusted, bestDispZ1, bestDispX1); //will make bestDispZ2 and bestDispX2 wrong
			}	
		}
		//if we didn't have variableCapThrow1 but we do have a second variable movement (i.e. before the final dive)
		else if (hasVariableCapThrow2 || hasVariableOtherMovement2) {
			double motionGroup1FinalRotation = calcFinalRotation(motionGroup1);
			Debug.println("Rotation before variable movement 2:" + Math.toDegrees(motionGroup1FinalRotation));
			findVariableAngle2(motionGroup1, motionGroup1FinalAngle, motionGroup1FinalRotation, dispZMotionGroup1, dispXMotionGroup1);
			once_bestAngle2 = variableAngle2;
			once_bestAngle2Adjusted = variableAngle2Adjusted;
			once_bestDispZ = testDispZ2;
			once_bestDispX = testDispX2;
		}
		
		//if there was a variable 2nd movement, we need to calculate a motion group 3 consisting of the ground pound and dive after it
		if (hasVariableCapThrow2 || hasVariableOtherMovement2) {
			double dispMotionGroup3 = 0;
			
			Movement groundPound = new Movement("Ground Pound");
			SimpleMotion gpMotion = groundPound.getMotion(1, false, false);
			gpMotion.setInitialAngle(once_bestAngle2Adjusted);
			motions[motions.length - 2] = gpMotion;
			
			Movement dive = new Movement("Dive");
			SimpleMotion diveMotion = dive.getMotion(movementFrames.get(motions.length - 1), false, false);
			diveMotion.setInitialAngle(once_bestAngle2Adjusted);
			motions[motions.length - 1] = diveMotion;
			
			dispMotionGroup3 = diveMotion.calcDispForward();
			
			once_bestDispZ += dispMotionGroup3 * Math.cos(once_bestAngle2Adjusted);
			once_bestDispX += dispMotionGroup3 * Math.sin(once_bestAngle2Adjusted);
		}
		
		once_bestDisp = Math.sqrt(Math.pow(once_bestDispZ, 2) + Math.pow(once_bestDispX, 2));

		return once_bestDisp;
	}

	private double calcDisp(double variableAngle1) {
		setCapThrowHoldingAngles(variableCapThrow1Vector, variableAngle1, variableCapThrow1Frames, variableCapThrow1FallingFrames);
				
		variableCapThrow1Vector.calcDisp();
		variableCapThrow1Vector.calcDispCoords();
		
		double variableCapThrow1DispZ = variableCapThrow1Vector.dispZ;
		double variableCapThrow1DispX = variableCapThrow1Vector.dispX;
		
		//adjust the angles so we can see how much displacement has occurred
		double motionGroup2AdjustedAngle;
		variableAngle1Adjusted = motionGroup1FinalAngle + booleanToPlusMinus(motionGroup2VectorRight) * variableAngle1;
		motionGroup2AdjustedAngle = variableAngle1Adjusted - booleanToPlusMinus(motionGroup2VectorRight) * motionGroup2Angle;
		
		//if the cap throw is long enough, there's falling afterward
		if (hasVariableCapThrow1Falling) {
			double[] fallingDisplacements = calcFallingDisplacements(variableCapThrow1Vector, variableCapThrow1Index, variableAngle1Adjusted, !variableCapThrow1VectorRight, false);
			variableCapThrow1DispZ += fallingDisplacements[0];
			variableCapThrow1DispX += fallingDisplacements[1];
		}
		
		//Debug.println(Math.toDegrees(motions[variableCapThrow1Index].finalAngle));
		
//		Debug.println(variableCapThrow1DispZ);
//		Debug.println(variableCapThrow1DispX);
//		Debug.println(dispMotionGroup2 * Math.cos(motionGroup2AdjustedAngle));
//		Debug.println(dispMotionGroup2 * Math.sin(motionGroup2AdjustedAngle));
		
		//Debug.println(Math.toDegrees(variableAngle1Adjusted));
		
		//sum the displacements so far
		testDispZ1 = dispZMotionGroup1 + variableCapThrow1DispZ + dispMotionGroup2 * Math.cos(motionGroup2AdjustedAngle);
		testDispX1 = dispXMotionGroup1 + variableCapThrow1DispX + dispMotionGroup2 * Math.sin(motionGroup2AdjustedAngle);
		
		//find correct cap throw 2 angle and add that on
		if (hasVariableCapThrow2 || hasVariableOtherMovement2) {
			double motionGroup2AdjustedFinalAngle = variableAngle1Adjusted - booleanToPlusMinus(motionGroup2VectorRight) * motionGroup2FinalAngle;
			
			//need to know the final rotation so that we can get the right rotation before a final dive if we're rotating, say, a cap bounce
			double motionGroup2FinalRotationAdjusted = motionGroup2FinalRotation + variableAngle1Adjusted - Math.PI / 2;
			//Debug.println("while optimizing mg2 final rotation adjusted: " + Math.toDegrees(motionGroup2FinalRotationAdjusted));
			//Debug.println("the final angle adjusted: " + Math.toDegrees(motionGroup2FinalRotationAdjusted));
			//Debug.println(Math.toDegrees(variableAngle2) + ": " + Math.toDegrees(variableAngle2Adjusted));
			if (findVariableAngle2(motionGroup2, motionGroup2AdjustedFinalAngle, motionGroup2FinalRotationAdjusted, testDispZ1, testDispX1)) {
				//if we're able to find a variable angle 2
				double testDisp = Math.sqrt(Math.pow(testDispZ2, 2) + Math.pow(testDispX2, 2));
				/* if (testDisp > oldTestDisp) {
					Debug.println("+");
				}
				else {
					Debug.println("-");
				}
				oldTestDisp = testDisp; */
				return testDisp;
			}

			return 0;
		}
		else { //if there isn't one we just compare this choice of variableAngle1 to the ones we've tried before
			double testDisp = Math.sqrt(Math.pow(testDispZ1, 2) + Math.pow(testDispX1, 2));
			/* if (testDisp > oldTestDisp) {
				Debug.println("+");
			}
			else {
				Debug.println("-");
			}
			oldTestDisp = testDisp; */
			Debug.println("Test Disp X1: " + testDispZ1);
			Debug.println("Test Disp Y1: " + testDispX1);
			return testDisp;
		}
		
		//Debug.println("Angle: " + Math.toDegrees(variableAngle1));
		//Debug.println("Group 2 Angle: " + Math.toDegrees(motionGroup2AdjustedAngle));
		//Debug.println("Displacement x, y: " + testDispZ1 + ", " + testDispX1);
		//Debug.println("Variable 1 displacement x, y: " + motions[variableCapThrow1Index].dispZ + ", " + motions[variableCapThrow1Index].dispX);
		//Debug.println("Group 2 displacement x, y: " + dispMotionGroup2 * Math.cos(motionGroup2AdjustedAngle) + ", " + dispMotionGroup2 * Math.sin(motionGroup2AdjustedAngle));
	}
	
	private boolean findVariableAngle2(SimpleMotion[] motionGroup, double initialAngle, double initialRotation, double previousDispZ, double previousDispX) {
		double initialForwardVelocity;
		if (variableMovement2Index - 1 >= 0)
			initialForwardVelocity = motions[variableMovement2Index - 1].finalSpeed;
		else
			initialForwardVelocity = listPreparer.initialVelocity;
		
		Movement variableMovement2 = new Movement(movementNames.get(variableMovement2Index), initialForwardVelocity);
		variableMovement2Vector = (ComplexVector) variableMovement2.getMotion(movementFrames.get(variableMovement2Index), currentVectorRight, true);
		motions[variableMovement2Index] = variableMovement2Vector;
		variableMovement2Vector.setInitialAngle(initialAngle); 
		
		//binary search to find variableAngle2
		double low = -Math.PI / 16;
		double high = Math.PI / 4;
		variableAngle2 = Math.PI / 8;
		
		//variableAngle2 = Math.toRadians(11.40380859375);
		
		// Debug.println("Finding Variable Angle 2");
		// Debug.println("Initial Angle:" + Math.toDegrees(initialAngle));
		
		while(high - low > .00001) {
			//boolean rotateDuringFall = p.hyperoptimize && hasVariableCapThrow2 && hasVariableMovement2Falling && movementFrames.get(variableCapThrow1Index + 1) >= 4;
			boolean rotateDuringFall = false;
			//rotateDuringFall = false; //commnet to actually run stuff
			if (rotateDuringFall)
				variableMovement2Vector.setHoldingAngle(SimpleMotion.NORMAL_ANGLE);
			else if (hasVariableCapThrow2)
				setFinalCapThrowHoldingAngles(variableMovement2Vector, variableAngle2, movementFrames.get(variableMovement2Index));
			else
				setOtherMovementHoldingAngles(variableMovement2Vector, motionGroup, variableMovement2Index, variableAngle2, initialAngle, initialRotation, currentVectorRight);
			variableMovement2Vector.calcDisp();
			variableMovement2Vector.calcDispCoords();
			
			double variableMovement2DispZ = variableMovement2Vector.dispZ;
			double variableMovement2DispX = variableMovement2Vector.dispX;
			
			variableAngle2Adjusted = initialAngle - booleanToPlusMinus(currentVectorRight) * variableAngle2; //the absolute direction we're throwing in/trying to go in
			
			if (hasVariableMovement2Falling) {
				double[] fallingDisplacements = calcFallingDisplacements(variableMovement2Vector, variableMovement2Index, variableAngle2Adjusted, !currentVectorRight, rotateDuringFall);
				variableMovement2DispZ += fallingDisplacements[0];
				variableMovement2DispX += fallingDisplacements[1];
			}
			
			testDispZ2 = previousDispZ + variableMovement2DispZ;
			testDispX2 = previousDispX + variableMovement2DispX;
			double testDispAngle2 = Math.atan(testDispX2 / testDispZ2); //angle of the displacement of the 2nd variable movement
			if (testDispAngle2 <= 0)
				testDispAngle2 += Math.PI;		
			
			// Debug.println("High:" + Math.toDegrees(high));
			// Debug.println("Low:" + Math.toDegrees(low));
			// Debug.println("Test angle:" + Math.toDegrees(variableAngle2Adjusted));
			// Debug.println("Disp angle:" + Math.toDegrees(testDispAngle2));

			/*if (hasVariableRollCancel) {
				if (Math.abs(testDispZ2) < .01) {
					Debug.println("angle 2 found");
					return true;
				}
				else if ((currentVectorRight && testDispZ2 > 0) || (!currentVectorRight && testDispZ2 < 0))
					high = variableAngle2;
				else
					low = variableAngle2;
			}
			else {*/
			if (Math.abs(variableAngle2Adjusted - testDispAngle2) < .0001) {
				// Debug.println("angle 2 found");
				return true;
			}
			else if ((currentVectorRight && variableAngle2Adjusted < testDispAngle2) || (!currentVectorRight && variableAngle2Adjusted > testDispAngle2))
				high = variableAngle2;
			else
				low = variableAngle2;
			//}
			variableAngle2 = (high + low) / 2;
		}
		return false;
	}

	//calculates how many frames of the dive there really are before the cap bounce
	public int getCapBounceFrame(int throwType) {
		if (hasVariableCapThrow1 && hasDiveCapBounce) {
			ComplexVector ct = (ComplexVector) motions[variableCapThrow1Index];
			DiveTurn dive = (DiveTurn) motions[preCapBounceDiveIndex];
			dive.firstFrameDecel = firstFrameDecel;
			ct.calcDispDispCoordsAngleSpeed();
			ct.calcDispY();
			if (hasVariableCapThrow1Falling) {
				SimpleVector falling = (SimpleVector) motions[variableCapThrow1Index + 1];
				falling.setInitialAngle(ct.finalAngle);
				//bestAngle1Adjusted = ct.finalAngle + booleanToPlusMinus(motionGroup2VectorRight) * bestAngle1;
				//System.out.println("CT final angle: " + Math.toDegrees(ct.finalAngle));
				//System.out.println("Holding angle: " + Math.toDegrees(Math.abs(ct.finalAngle - bestAngle1Adjusted)));
				falling.setHoldingAngle(Math.abs(ct.finalAngle - bestAngle1Adjusted));
				falling.setInitialCoordinates(ct.x0 + ct.dispX, ct.y0 + ct.dispY, ct.z0 + ct.dispZ);
				falling.calcDispDispCoordsAngleSpeed();
				falling.calcDispY();
				//Debug.println(falling.dispX + ", " + falling.dispY + ", " + falling.dispZ);
				//dive.setInitialAngle(bestAngle1Adjusted);
				dive.setInitialCoordinates(falling.x0 + falling.dispX, falling.y0 + falling.dispY, falling.z0 + falling.dispZ);
			}
			else {
				dive.setInitialCoordinates(ct.x0 + ct.dispX, ct.y0 + ct.dispY, ct.z0 + ct.dispZ);
			}
			return dive.getCapBounceFrame(ct.getCappyPosition(throwType));
		}
		else return -1;
	}

	public double edgeCBMin = 12, edgeCBMax = 26;
	public double firstFrameDecelIncrement = 0.005;
	public double edgeCBAngleIncrement = 0.01;
	//public int edgeCBSteps = 30 * 101;
	//sees if the dive will actually bounce on cappy in the requested number of frames
	//throwType is the type of throw to check (set to -1 to check all throw types that satisfy the booleans afterward)
	//allowButtonST = can check for regular single throws
	//allowST = can check for motion or regular single throws
	//allowDT = can check for double throws
	//allowTT = can check for triple throws
	//diveCapBounceAngle is now one that works (also the value in Properties is this)
	//ctType is the ct that worked;
	//currently does not recalculate rest of jump to be optimal, but maybe it should
	public int isDiveCapBouncePossible(int throwType, boolean allowButtonST, boolean allowSideThrow, boolean allowST, boolean allowDT, boolean allowTT) {
		//motions[0].setInitialAngle(Math.PI / 2); //undo any previous angle adjustment
		for (int i = variableCapThrow1Index; i < motions.length; i++) {
			if ((i == variableCapThrow1Index + 1 || i == variableCapThrow1Index + 2) && motions[i].movement.movementType.equals("Ground Pound")) {
				motions[i].setInitialAngle(bestAngle1Adjusted);
			}
			else if ((i == variableMovement2Index + 1 || i == variableMovement2Index + 2) && motions[i].movement.movementType.equals("Ground Pound")) {
				motions[i].setInitialAngle(bestAngle2Adjusted);
			}
			else if (i > 0) {
				motions[i].setInitialAngle(motions[i - 1].finalAngle);
			}
			motions[i].calcDispDispCoordsAngleSpeed();
		}

		double lowAngle = Double.MIN_VALUE;
		double highAngle = Double.MIN_VALUE;
		int targetCBFrame = motions[preCapBounceDiveIndex].frames;
		DiveTurn dive = (DiveTurn) motions[preCapBounceDiveIndex];
		//ComplexVector capThrow = (ComplexVector) motions[variableCapThrow1Index];
		for (firstFrameDecel = 0; firstFrameDecel <= .5; firstFrameDecel += firstFrameDecelIncrement) {
		//for (double endDecel = 0; endDecel <= 15; endDecel += .5) {
			if (firstFrameDecel > 0 && firstFrameDecel / .5 <= .1) { //can't hold back this shallow
				continue;
			}
			//Debug.println("hi");
			//System.out.print(preCapBounceDiveIndex);
			dive.firstFrameDecel = firstFrameDecel;
			//((DiveTurn) motions[preCapBounceDiveIndex]).endDecel = endDecel;
			for (int ct = 0; ct < Movement.CT_COUNT; ct++) {
				// System.out.println("Testing throw type " + ct);
				if (throwType != -1 && ct != throwType) {
					continue;
				}
				if ((!allowButtonST || variableCapThrow1Frames < 9) && ct == Movement.CT) {
					continue;
				}
				else if (!allowSideThrow && (ct == Movement.MCCTL || ct == Movement.MCCTR || ct == Movement.TTL || ct == Movement.TTR)) {
					continue;
				}
				else if ((!allowST || variableCapThrow1Frames < 8) && (ct == Movement.CT || ct == Movement.MCCTU || ct == Movement.MCCTD || ct == Movement.MCCTL || ct == Movement.MCCTR)) {
					continue;
				}
				else if ((!allowDT || variableCapThrow1Frames < 8) && ct == Movement.DT) {
					continue;
				}
				else if (!allowTT && (ct == Movement.TT || ct == Movement.TTU || ct == Movement.TTD || ct == Movement.TTL || ct == Movement.TTR)) {
					continue;
				}
				
				//double edgeCBIncrement = (edgeCBMax - edgeCBMin) / (edgeCBSteps - 1);
				
				boolean found = false;
				boolean overshot = false;
				for (double edgeCB = edgeCBMin; edgeCB <= edgeCBMax; edgeCB += edgeCBAngleIncrement) {
					diveCapBounceAngle = edgeCB;
					if (variableCapThrow1Frames <= 14 && edgeCB > 20) { //these cannot be turned as much without developing another method of turning
						break;
					}
					setCapThrowHoldingAngles(variableCapThrow1Vector, bestAngle1, variableCapThrow1Frames, variableCapThrow1FallingFrames);

					int cbFrame = getCapBounceFrame(ct);
					//System.out.printf("%.3f° %df\n", diveCapBounceAngle, cbFrame);
					if (cbFrame == targetCBFrame) {
						if (!found) {
							found = true;
							lowAngle = diveCapBounceAngle;
						}
						if (!overshot) {
							highAngle = diveCapBounceAngle;
						}
						ctType = ct;
					}
					else if (highAngle != Double.MIN_VALUE)
						overshot = true;
					//diveCapBounceAngle += edgeCBIncrement;
				}
				if (found && highAngle >= lowAngle + p.diveCapBounceTolerance) { //too high of a risk it won't actually work in game if they are the same
					 Debug.println("Decel: " + firstFrameDecel);
					 Debug.println("Found low: " + lowAngle);
					 Debug.println("Found high: " + highAngle);
					if (highAngle - lowAngle < 2) { //if high and low angles are close pick the middle for most reliable result
						diveCapBounceAngle = (highAngle + lowAngle) / 2;
					}
					else { //otherwise pick an angle close to the high for a better vector
						diveCapBounceAngle = highAngle - 1;
					}
					p.diveCapBounceAngle = diveCapBounceAngle;
					p.diveFirstFrameDecel = firstFrameDecel;
					Debug.println(p.diveCapBounceAngle);
					setCapThrowHoldingAngles(variableCapThrow1Vector, bestAngle1, variableCapThrow1Frames, variableCapThrow1FallingFrames);
					getCapBounceFrame(ct); //run again to adjust the falling vector to be correct
					return ctType;
				}
			}
		}
		// System.out.println("NO!");
		return -1;
	}

	//recalculates displacement after calling isDiveCapBouncePossible()
	public void recalculateDisps() {
		only_maximize_variableAngle2 = true;
		maximize();
		only_maximize_variableAngle2 = false;

		//now recalculate the displacement of the full jump with the new cap throw angle, dive decel, etc.
		//so that it can later be adjusted to the correct angle with a call to adjustToGivenAngle()
		for (int i = variableCapThrow1Index; i < motions.length; i++) {
			if ((i == variableCapThrow1Index + 1 || i == variableCapThrow1Index + 2) && motions[i].movement.movementType.equals("Ground Pound")) {
				motions[i].setInitialAngle(bestAngle1Adjusted);
			}
			else if ((i == variableMovement2Index + 1 || i == variableMovement2Index + 2) && motions[i].movement.movementType.equals("Ground Pound")) {
				motions[i].setInitialAngle(bestAngle2Adjusted);
			}
			else if (i > 0) {
				motions[i].setInitialAngle(motions[i - 1].finalAngle);
			}
			motions[i].calcDispDispCoordsAngleSpeed();
		}
		sumXDisps(motions);
		sumYDisps(motions);
		bestDispX = dispX;
		bestDispZ = dispZ;
		//maximize_variableAngle1();					
		//calcDisp(bestAngle1);
		//adjustToGivenAngle();
	}

	//adjusts the angle of everything so it is in the direction of the given target or initial angle
	public void adjustToGivenAngle() {
		// motions[0].setInitialAngle(Math.PI / 2); //undo any previous angle adjustment
		// for (int i = 0; i < motions.length; i++) {
		// 	if ((i == variableCapThrow1Index + 1 || i == variableCapThrow1Index + 2) && motions[i].movement.movementType.equals("Ground Pound")) {
		// 		motions[i].setInitialAngle(bestAngle1Adjusted);
		// 	}
		// 	else if ((i == variableMovement2Index + 1 || i == variableMovement2Index + 2) && motions[i].movement.movementType.equals("Ground Pound")) {
		// 		motions[i].setInitialAngle(bestAngle2Adjusted);
		// 	}
		// 	else if (i > 0) {
		// 		motions[i].setInitialAngle(motions[i - 1].finalAngle);
		// 	}
		// 	motions[i].calcDispDispCoordsAngleSpeed();
		// }
		double unadjustedTargetAngle = Math.atan(bestDispX / bestDispZ);
		if (unadjustedTargetAngle < 0)
			unadjustedTargetAngle += Math.PI;
		Debug.println("Unadjusted target angle:" + Math.toDegrees(unadjustedTargetAngle));
		if (targetAngleGiven) {
			angleAdjustment = targetAngle - unadjustedTargetAngle;
			initialAngle = Math.PI / 2 + angleAdjustment;
		}
		else {
			Debug.println("hi");
			angleAdjustment = initialAngle - Math.PI / 2;
			if (rightVector) {
				angleAdjustment -= rcTrueInitialAngleDiff;
			}
			else {
				angleAdjustment += rcTrueInitialAngleDiff;
			}
			targetAngle = unadjustedTargetAngle + angleAdjustment;
		}
		for (int i = 0; i < motions.length; i++) {
			motions[i].adjustInitialAngle(angleAdjustment);
		}
		if (initialAngle < 0)
			initialAngle += 2 * Math.PI;
		if (targetAngle < 0)
			targetAngle += 2 * Math.PI;
		Debug.println("Initial angle:" + Math.toDegrees(initialAngle));
		Debug.println("Target angle:" + Math.toDegrees(targetAngle));
	}

	public void calcYDisps() { //calculates Y disps of every motion
		calcMotionGroup(0, movementNames.size(), listPreparer.initialVelocity, p.framesJump);
		for (int i = 0; i < motions.length; i++) {
			if (motions[i].movement.movementType.equals("Falling") && i > 0)
				motions[i].movement.initialVerticalSpeed = motions[i - 1].calcFinalVerticalVelocity();
			motions[i].calcDispY();
		}
	}
}
