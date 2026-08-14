package com.vectorcalculator;

import java.util.ArrayList;
import com.vectorcalculator.Properties.TurnDuringDive;

public class VectorMaximizer {

	Properties p = Properties.p;
	
	public static final double RCV_ERROR = .001; //acceptable Z axis error when trying to make a RCV go straight
    public static final int RCV_MAX_ITERATIONS = 100; //stop after this many iterations no matter what when trying to make a RCV go straight

	public static final double FAST_TURNAROUND_VELOCITY = Math.toRadians(25);
	public static final double FAST_TURNAROUND_ACCEL = Math.toRadians(2.5);
	public static final double FAST_TURNAROUND_ANGLE = Math.toRadians(137); //only needs to be 135, but extra degrees for safety

	public static final double TURN_COUNTERROTATION = Math.toRadians(.35); //really should be .3 but this produces inaccurate results
	public static final double TRUE_TURN_COUNTERROTATION = Math.toRadians(.3);

	public static final double FINAL_CT_ANGLE_REDUCTION_LIMIT = 5; //how many degrees you are willing to sacrifice off a perfect vector

	public static final double MAX_DIVE_CAP_BOUNCE_ANGLE = 41.2;

	public static final int COMPLEX_HCT_FALL_MIN_FRAMES = 5; //for 28f and faster HTTs this method is not optimal

	double maximize_HCT_limit = Math.toRadians(2); //binary search limit for hct fall vector angle

	SimpleVector[] vectors;
	double[] angles;
	SimpleMotion[] motions;
	int[] frames;

	double diveCapBounceAngle; //in degrees
	double vectorAngle; //in radians
	int ctType = Movement.MCCTU;
	double firstFrameDecel = 0; //for the dive before the cap bounce
	
	double dispZ;
	double dispX;
	double disp;
	double angle;
	
	double givenAngle;
	double initialAngle;
	double targetAngle;
	double angleAdjustment = 0;

	double initialRotation = 0;
	
	boolean rightVector;
	boolean currentVectorRight;

	boolean complexHCTFallVector = false;
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
	boolean hasDiveCapBounce = false;
	boolean hasCapBounce = false;

	boolean only_maximize_variableAngle2 = false;

	boolean optimizeFCTFalling = false;
	boolean optimizeCT1Falling = false; //gains a little under 2 units in the best cases that have been found, only relevant in moon gravity currently
	
	boolean roughOptimizeFCTFalling = false; //if this is true, then the calculator does not worry about the turnaround at the end to get a rough value
	boolean roughOptimizeCT1Falling = false;
	boolean roughCTRotations = false; //assumes ct rotates to normal angle to make calculations faster

	boolean optimizeIMYank = true;
	boolean optimizeCBYank = true;
	boolean optimizeRSYank = true;

	int variableCapThrow1Index;
	int variableMovement2Index;
	int motionGroup2Index;
	int variableHCTFallIndex;
	int rainbowSpinFrames;
	int preCapBounceDiveIndex = Integer.MIN_VALUE;
	int rainbowSpinIndex = Integer.MIN_VALUE;
	int cbIndex = Integer.MIN_VALUE;

	int maxRCVNudges = 20;
	int maxRCVFineNudges = 10;

	SimpleVector variableCapThrow1Vector;
	SimpleVector variableMovement2Vector;
	int variableCapThrow1Frames;
	int variableCapThrow1FallingFrames;
	int variableMovement2Frames;
	int variableMovement2FallingFrames;
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
	double variableHCTCountervectorFrames;

	double rsYankFrames = 0;
	double imYankFrames = 0;
	double cbYankFrames = 0;
	
	double rcTrueInitialAngleDiff;
	double rcFinalAngleDiff;
	double bestRCFinalAngleDiff;

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

	String error = "";

	SimpleMotion[] motionGroup1 = null;
	SimpleMotion[] motionGroup2 = null;
	
	ArrayList<String> movementNames;
	ArrayList<Integer> movementFrames;
	MovementNameListPreparer listPreparer;

	static double[] fastTurnarounds = {0, Math.toRadians(25), Math.toRadians(25 + 22.5), Math.toRadians(25 + 22.5 + 20), Math.toRadians(25 + 22.5 + 20 + 17.5)};
	
	public VectorMaximizer(MovementNameListPreparer listPreparer) {
		
		this.listPreparer = listPreparer;
		
		if (p.xAxisZeroDegrees) { //vector calculator cacluates as if the order is ZXY (i.e. 0 degrees is the positive Z axis, and 90 degrees is the positive X axis)
			initialAngle = Math.PI / 2 - Math.toRadians(p.initialAngle);
			targetAngle = Math.PI / 2 - Math.toRadians(p.targetAngle);
		}
		else {
			initialAngle = Math.toRadians(p.initialAngle);
			targetAngle = Math.toRadians(p.targetAngle);
		}

		rightVector = p.rightVector;

		if (p.twoPlayerMode)
			ctType = Movement.FT;
		
		movementNames = listPreparer.movementNames;
		movementFrames = listPreparer.movementFrames;
		
		hasVariableRollCancel = movementNames.get(0).contains("RCV");

		variableCapThrow1Index = movementNames.size();
		variableMovement2Index = movementNames.size();
		motionGroup2Index = movementNames.size();
		
		//determine where the cap throws / other movement types whose angles are variable are (if any), since they will partition the movement
		for (int i = 0; i < movementNames.size(); i++) {
			if (movementNames.get(i).equals("Dive")) {
				boolean isFinalDive = (i == movementNames.size() - (p.reverseBonk ? 2 : 1));
				if (i - 2 >= 0 && Movement.isMidairCapThrow(movementNames.get(i - 2))) {
					if (isFinalDive) {
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
				else if (i - 3 >= 0 && Movement.isMidairCapThrow(movementNames.get(i - 3)) && movementNames.get(i - 2).equals("Falling")) {
					if (isFinalDive) {
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
				else if (i - 2 >= 0 && isFinalDive) {
					if (i - 3 >= 0 && movementNames.get(i - 2).equals("Falling") && (new Movement(movementNames.get(i - 3)).canVector) && !movementNames.get(i - 3).contains("RCV")) {
						hasVariableOtherMovement2 = true;
						hasVariableMovement2Falling = true;
						variableMovement2Index = i - 3;
					}
					else if (new Movement(movementNames.get(i - 2)).canVector) {
						hasVariableOtherMovement2 = true;
						variableMovement2Index = i - 2;
					}
				}
			}
			else if (movementNames.get(i).equals("Rainbow Spin")) {
				hasRainbowSpin = true;
				rainbowSpinIndex = i;
				rainbowSpinFrames = movementFrames.get(i);
				if (i >= 2 && (movementNames.get(i - 2).equals("Fake Throw") || movementNames.get(i - 2).contains("Homing")) && movementNames.get(i - 1).equals("Falling")) {
					hasVariableHCTFallVector = true; //we optimize countervectoring it
					complexHCTFallVector = movementFrames.get(i - 1) >= COMPLEX_HCT_FALL_MIN_FRAMES; //this is just a rough idea
					variableHCTFallIndex = i - 1;
				}
			}
			else if (Movement.isCapBounce(movementNames.get(i))) {
				hasCapBounce = true;
				cbIndex = i;
				if (movementNames.get(i).equals("Dive Cap Bounce"))
					hasDiveCapBounce = true;
			}
		}
		
		motions = new SimpleMotion[movementNames.size()];

		optimizeCT1Falling = p.optimizeCT1Falling && p.turnarounds && hasVariableCapThrow1 && hasVariableCapThrow1Falling && movementFrames.get(variableCapThrow1Index + 1) >= 6; //TODO 6 might not always work, but it really seems like it does
		
		Debug.println("Variable cap throw 1: " + hasVariableCapThrow1);
		Debug.println("Variable cap throw 2: " + hasVariableCapThrow2);
		Debug.println("Variable other movement 2: " + hasVariableOtherMovement2);
		Debug.println("Indices: " + variableCapThrow1Index + ", " + motionGroup2Index + ", " + variableMovement2Index);
	}

	public boolean hasError() {
		return !error.equals("");
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
		disp = Math.sqrt(dispZ * dispZ + dispX * dispX);
	}
	
	private void calcAngle() {
		angle = Math.atan2(dispX, dispZ);
	}
	
	private void calcAll(SimpleMotion[] selectedMotions) {
		sumXDisps(selectedMotions);
		sumYDisps(selectedMotions);
		calcDisp();
		calcAngle();
	}
	
	
	private double calcFinalRotation(SimpleMotion[] motionGroup, boolean hasInitialMovement) {
		if (motionGroup.length == 0)
			return Math.PI / 2;
		else {
			if (hasInitialMovement) {
				if (hasVariableRollCancel)
					initialRotation = Math.toRadians(p.initialAngle);
				else
					initialRotation = motionGroup[0].initialAngle + (p.chooseInitialRotation ? Math.toRadians(p.initialRotation) : motionGroup[0].movement.defaultRotation);
				motionGroup[0].setInitialRotation(initialRotation);
			}
			else {
				motionGroup[0].setInitialRotation(motionGroup[0].initialAngle);
			}
			for (int i = 1; i < motionGroup.length; i++) {
				double prevRotation = motionGroup[i - 1].calcFinalRotation();
				if (Movement.isMidairCapThrow(motionGroup[i].movement.movementType)) {
					//System.out.println(i + " holding angle " + motionGroup[i].holdingAngle);
					motionGroup[i].setInitialRotation(motionGroup[i].initialAngle + (((SimpleVector) motionGroup[i]).rightVector ? -1 : 1) * motionGroup[i].holdingAngle);
				}
				else if (motionGroup[i].movement.movementType.equals("Reverse Bonk"))
					motionGroup[i].setInitialRotation(motionGroup[i].initialAngle + Math.PI);
				else {
					motionGroup[i].setInitialRotation(prevRotation);
				}
			}
			
			return motionGroup[motionGroup.length - 1].calcFinalRotation();
		}
	}

	private boolean canRocketFlower(int index) {
		return (index <= listPreparer.initialMovementIndex) || (index  == listPreparer.initialMovementIndex + 1) && (movementNames.get(index).equals("Cap Bounce") || (movementNames.get(index).equals("2P Midair Vault")));
	}

	private double[] generateHomingMotionThrowHoldingAngles() {
		double[] homingMotionThrowHoldingAngles = new double[24];
		homingMotionThrowHoldingAngles[0] = Math.toRadians(p.hctThrowAngle);
		for (int j = 1; j <= 23; j++)
			homingMotionThrowHoldingAngles[j] = SimpleMotion.NORMAL_ANGLE;
		if (p.hctNeutralHoming) {
			homingMotionThrowHoldingAngles[p.hctHomingFrame] = SimpleMotion.NO_ANGLE;
		}
		//homingMotionThrowHoldingAngles[23] = Math.toRadians(90 + p.debugValue); //yank here can help a slight amount, about .1 units, probably not worth calculating
		return homingMotionThrowHoldingAngles;
	}
	
	public static final double OPTIMAL_ANGLE_DIFF = Double.MAX_VALUE;
	//public static final int FIRST_VECTOR_ANGLE_FRAME = 7;

	//angle is the angle of the dive
	//angleDiff is how many radians to the side of the dive angle the throw angle is
	//vectorAngle is how many degrees to the side are being held in order to vector the cap throw
	private boolean setCapThrowHoldingAngles(ComplexVector motion, double angle, double angleDiff, double vectorAngle, int frames, int fallingFrames) {
		if (!optimizeCT1Falling && (angleDiff == OPTIMAL_ANGLE_DIFF || p.trySimplifyFirstThrowVector) && canSetOptimalHoldingAngles(frames, angle, angle + angleDiff, vectorAngle))
			return setOptimalHoldingAngles(motion, angle, angleDiff, vectorAngle, frames);
		else if (angleDiff == OPTIMAL_ANGLE_DIFF)
			angleDiff = Math.toRadians(diveCapBounceAngle);
		
		double angleDiffDeg = Math.toDegrees(angleDiff);
		double throwAngle = angle + angleDiff;
		double diveAngle = angle;
		double maxRotation = 0;
		double angularVelocity = 0;
		boolean standardTurnaround = false;

		double[] holdingAngles = new double[frames];
		holdingAngles[0] = throwAngle;

		if (optimizeCT1Falling) { //no turnaround is performed during the cap throw
			for (int i = 1; i < frames; i++) {
				holdingAngles[i] = vectorAngle;
				//holdingAngles[i] = vectorAngle - Math.toRadians(.3) * (i - 1);
			}
			// holdingAngles[22] = Math.toRadians(p.debugValue);
			// holdingAngles[23] = Math.toRadians(p.debugValue);
			//holdingAngles[22] = vectorAngle - 3.01 / 4 * Math.PI;
			//holdingAngles[23] = vectorAngle - 3.01 / 4 * Math.PI + Math.toRadians(p.debugValue);
			boolean[] holdingMinRadius = new boolean[frames];
			// holdingMinRadius[22] = true;
			// holdingMinRadius[23] = true;
			motion.setHolding(holdingAngles, holdingMinRadius);
			return true;
		}

		for (int i = 0; i < frames - 2; i++) {
			angularVelocity += .3;
			if (angularVelocity >= 6) {
				angularVelocity = 6;
			}
			maxRotation += angularVelocity;
			//Debug.println("Max rotation: " + maxRotation);
			if (maxRotation + angleDiffDeg > 24.999) { //if we can get to the dive angle with at least 1f of fast turnaround
				standardTurnaround = true;
			}
		}
		angularVelocity += .3;
		if (angularVelocity >= 6) {
			angularVelocity = 6;
		}

		double trueMaxRotation = maxRotation + angularVelocity; //maximum rotation without a turnaround
		//we need at least 6 frames to apply the non-standard turnaround
		//if the angleDiffDeg is 0 and the movement is not more than 10 frames, those have better solutions
		if (p.turnarounds && !(angleDiff == 0 && frames <= 14) && !(frames <= 6 && !standardTurnaround)) { //we can rotate enough away from the dive angle that we can use 1 or 2 frames of fast turnaround to get there
			if (standardTurnaround) {
				if (maxRotation + angleDiffDeg < 25.001) { //shortcut if we can just hold one direction
					for (int i = 1; i < frames - 1; i++) {
						holdingAngles[i] = vectorAngle;
					}
					holdingAngles[frames - 1] = throwAngle + maxRotation - 136 / 180.0 * Math.PI; //hold as little back as you can
					boolean[] holdingMinRadius = new boolean[frames];
					holdingMinRadius[frames - 1] = true;
					motion.setHolding(holdingAngles, holdingMinRadius);
					return true;
				}
				else {
					int turnaroundFrames = 1;
					double minRotation = motion.angularAccel * (frames - 2); //first frame sets the cap throw angle, last frame (or two) is a fast turnaround
					Debug.println("Min Rotation: " + Math.toDegrees(minRotation));
					double unneededRotation = 0;
					//Debug.println(fallingFrames);
					if (fallingFrames >= 4) {
						unneededRotation = Math.toRadians(2.9); //this rotation can all happen during the falling
					}
					double additionalRotation = FAST_TURNAROUND_VELOCITY - (angleDiff - unneededRotation + minRotation);	
					if (additionalRotation < 0) {
						turnaroundFrames = 2;
						additionalRotation += FAST_TURNAROUND_VELOCITY - Math.toRadians(2.5) + Math.toRadians(.3); //add .3 because the minimum rotation is now .3 less
						minRotation = motion.angularAccel * (frames - 3);
					}
					Debug.println("Additional rotation: " + Math.toDegrees(additionalRotation));
					double rotationSum = 0;
					angularVelocity = 0;
					int additionalRotationFrames = 0;
					while (rotationSum < additionalRotation) {
						angularVelocity += motion.angularAccel;
						rotationSum += angularVelocity;
						additionalRotationFrames++;
					}
					double totalRotation = minRotation + additionalRotation;
					double overshoot = rotationSum - additionalRotation;
					Debug.println("Angle diff (DCBA): " + angleDiffDeg);
					Debug.println("Total rotation: " + Math.toDegrees(totalRotation));
					Debug.println("Overshoot: " + Math.toDegrees(overshoot));
					//how much counterrotation there should be on the first frame of acceleration
					//double firstAdditionalRotationFrameCounterrotation = overshoot / additionalRotationFrames;
					int firstAdditionalRotationFrame = frames - turnaroundFrames - additionalRotationFrames;
					double currentRotation = throwAngle + Math.toRadians(0.3);
					holdingAngles[1] = SimpleMotion.NORMAL_ANGLE - Math.toRadians(1.5); //shifting by 1.5 degrees makes fast turnarounds not reverse the wrong way
					// if (p.debugValue != 0) {
					// 	holdingAngles[1] = Math.min(vectorAngle, SimpleMotion.NORMAL_ANGLE - Math.toRadians(1.5)); 
					// }
					boolean holdTargetRotation = false;
					boolean vectorAngleApplied = false;
					for (int i = 2; i < firstAdditionalRotationFrame; i++) {
						holdingAngles[i] = holdingAngles[i - 1] - TURN_COUNTERROTATION;
						currentRotation += Math.toRadians(0.3);
						if (i == motion.minFrames - 1/*  && p.debugValue == 0 */) {
							if (holdingAngles[i] > vectorAngle)
								holdingAngles[i] = vectorAngle;
							vectorAngleApplied = true; //TODO if this isn't applied, then it should get applied somehow
						}
						if (currentRotation > holdingAngles[i] - Math.toRadians(1)) //getting too close
							holdTargetRotation = true;
						if (holdTargetRotation)
							holdingAngles[i] = throwAngle + totalRotation;
					}
					//Debug.println(holdingAngles[firstAdditionalRotationFrame]);
					for (int i = firstAdditionalRotationFrame; i < frames - turnaroundFrames; i++) {
						holdingAngles[i] = holdTargetRotation ? throwAngle + totalRotation : holdingAngles[1];
					}
					if (!holdTargetRotation) { //counterrotate enough to mitigate all the overshoot in one frame
						holdingAngles[frames - turnaroundFrames - 1] = holdingAngles[frames - turnaroundFrames - 2] - overshoot;
					}
					// System.out.println("Total rotation: " + Math.toDegrees(totalRotation));
					// System.out.println("Throw Angle: " + Math.toDegrees(throwAngle));
					// System.out.println("Last Holding Angle: " + Math.toDegrees(holdingAngles[frames - turnaroundFrames - 1]));
					//here we see if there was a collision between the last angle and Mario's rotation; if that is the case, then we should not use this method of finding holding angles
					// if (throwAngle + totalRotation > holdingAngles[frames - turnaroundFrames - 1]) {
					// 	if (canSetOptimalCapThrowHoldingAngles(frames, diveAngle, diveAngle + angleDiff, vectorAngle)) {
					// 		setOptimalCapThrowHoldingAngles(motion, diveAngle, angleDiff, vectorAngle, frames);
					// 		return true;
					// 	}
					// 	else {
					// 		error = "Error: Issue with first cap throw vector angle";
					// 		return false;
					// 	}
					// }
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
					return true;
				}
			}
			else { //we need to fast turnaround both directions to get to the dive angle
				angularVelocity = 0;
				double rotation = 0;
				holdingAngles[1] = vectorAngle;
				for (int i = 1; i < frames - 4; i++) {
					rotation += Math.toRadians(.3);
					if (i > 1) {
						holdingAngles[i] = holdingAngles[i - 1] - TURN_COUNTERROTATION;
					}
				}
				rotation -= Math.toRadians(.075); //rotation on frames - 4 frame
				holdingAngles[frames - 3] = throwAngle + rotation + 135.1 / 180.0 * Math.PI;
				holdingAngles[frames - 4] = holdingAngles[frames - 3] + 179 / 180.0 * Math.PI;
				holdingAngles[frames - 2] = diveAngle - FAST_TURNAROUND_VELOCITY;
				holdingAngles[frames - 1] = holdingAngles[frames - 2] + 135.1 / 180.0 * Math.PI;
				boolean[] holdingMinRadius = new boolean[frames];
				holdingMinRadius[frames - 4] = true;
				holdingMinRadius[frames - 3] = true;
				holdingMinRadius[frames - 1] = true;
				motion.setHolding(holdingAngles, holdingMinRadius);
				return true;
			}
		}
		else if (angleDiff == 0) { //edge cap bounce angle is 0 and we can use pre-determined holding angles to vector efficiently
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
			else if (frames <= 14 /* && p.turnarounds */) {
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
			return true;
		}
		else { //no turnaround allowed, so we vector as long as we can before holding final angle
			int vectorFrames = 0;
			int remainingFrames = frames - 1;
			if (trueMaxRotation < angleDiffDeg) {
				error = "Error: Edge CB angle too large";
				return false;
			}
			double firstVelocity = 0; //velocity on the frame we've gotten back to the initial throw angle and are moving toward the dive bounce angle
			while (true) {
				angularVelocity = firstVelocity;
				double rotation = 0;
				for (int i = 0; i < remainingFrames; i++) {
					angularVelocity += .3;
					if (angularVelocity >= 6) {
						angularVelocity = 6;
					}
					rotation += angularVelocity;
				}
				if (remainingFrames < 0 || rotation < angleDiffDeg) {
					vectorFrames -= 1; //we overdid it
					remainingFrames += 2;
					break;
				}
				vectorFrames++;
				remainingFrames -= 2;
				firstVelocity += .3;
			}
			for (int i = 1; i <= vectorFrames; i++) {
				holdingAngles[i] = SimpleMotion.NORMAL_ANGLE;
			}
			for (int i = vectorFrames + 1; i <= 2 * vectorFrames; i++) {
				if (vectorFrames >= 7)
					holdingAngles[i] = throwAngle;
				else
					holdingAngles[i] = diveAngle;
			}
			for (int i = 2 * vectorFrames + 1; i < frames; i++) {
				holdingAngles[i] = diveAngle;
			}
			motion.setHoldingAngles(holdingAngles);
			return true;
		}
	}

	//vector angle is SimpleMotion.NORMAL_ANGLE if it's a max strength vector
	private boolean canSetOptimalHoldingAngles(int frames, double diveAngle, double relativeInitialRotation, double vectorAngle) {
		if (!p.turnarounds)
			return false;

		double ang_deg = Math.toDegrees(vectorAngle - diveAngle);

		int turnaroundFrames;
		if (ang_deg <= 25 + 22.5 + FINAL_CT_ANGLE_REDUCTION_LIMIT)
			return false;
		else if (ang_deg <= 25 + 22.5 + 20 + FINAL_CT_ANGLE_REDUCTION_LIMIT)
			turnaroundFrames = 3;
		else
			turnaroundFrames = 4;

		double maxRotation = 0;
		double angularVelocity = 0;
		for (int i = 0; i < frames - 1 - turnaroundFrames; i++) {
			angularVelocity += .3;
			if (angularVelocity >= 6) {
				angularVelocity = 6;
			}
			maxRotation += angularVelocity;
		}
		angularVelocity += .3;
		if (angularVelocity >= 6) {
			angularVelocity = 6;
		}
		// System.out.println("Max Rotation: " + maxRotation);
		// System.out.println("Vector angle: " + Math.toDegrees(vectorAngle));
		// System.out.println("Needed rotation: " + Math.toDegrees(vectorAngle - throwAngle));
		return (vectorAngle - relativeInitialRotation < Math.toRadians(maxRotation)); //is it possible to rotate enough to reach the vector angle from the throw angle? If so, then we can use an optimally vectored turnaround
	}

	//sets cap throw holding angles assuming Cappy is thrown at 90 degrees OR that Mario will rotate all the way to the vectorAngle so that he has enough time to do an optimal turnaround
	//idea: vector perfectly for as long as you can
	//then initiate a turnaround but then go back to vectoring afterward
	//the last frame hold the exact direction you want to go in
	//OR if turning around for x frames is almost enough, just vector more weakly to start with
	//angle is dive angle
	//it seems that turnaroundFrames is always 3
	private boolean setOptimalHoldingAngles(ComplexVector motion, double angle, double angleDiff, double vectorAngle, int frames) {
		double[] holdingAngles = new double[frames];
		boolean[] holdingMinRadius = new boolean[frames];
		if (p.turnarounds) {
			double initialHoldingAngle = angleDiff == OPTIMAL_ANGLE_DIFF ? SimpleMotion.NORMAL_ANGLE : angle + angleDiff;
			double ang_deg = Math.toDegrees(vectorAngle - angle);
			Debug.println("Final Cap Throw Dive Angle: " + ang_deg);
			int turnaroundFrames = 0;
			double difference = 0; //difference between exact turnaround and how much Mario needs to turn around
			if (ang_deg <= 25)
				return false;
			else if (ang_deg <= 25 + 22.5) {
				turnaroundFrames = 2;
				difference = ang_deg - 25 - 22.5;
			}
			else if (ang_deg <= 25 + 22.5 + 20 + (Movement.isMidairCapThrow(motion.movement.movementType) ? FINAL_CT_ANGLE_REDUCTION_LIMIT : 0)) {
				turnaroundFrames = 3;
				difference = ang_deg - 25 - 22.5 - 20;
			}
			else {
				turnaroundFrames = 4;
				difference = ang_deg - 25 - 22.5 - 20 - 17.5;
			}
			if (difference > 0 || (turnaroundFrames == 1 && difference < -0.001)) {
				if (angleDiff == OPTIMAL_ANGLE_DIFF)
					initialHoldingAngle -= Math.toRadians(difference);
				vectorAngle -= Math.toRadians(difference);
			}

			Movement movement = motion.movement;
			double forwardAccelFrames = Math.max((movement.defaultSpeedCap - movement.initialHorizontalSpeed) / movement.forwardAccel, 0);
			int totalForwardAccelFrames = (int) Math.ceil(forwardAccelFrames);

			int firstVectorAngleFrame = Math.max(totalForwardAccelFrames, 1);

			if (totalForwardAccelFrames == 0)
				holdingAngles[0] = initialHoldingAngle;
			for (int i = 0; i < (int) forwardAccelFrames; i++)
                holdingAngles[i] = 0;
            if (forwardAccelFrames != totalForwardAccelFrames && totalForwardAccelFrames > 0)
                holdingAngles[totalForwardAccelFrames - 1] = Math.acos(forwardAccelFrames - (int) forwardAccelFrames);
			for (int i = firstVectorAngleFrame; i < frames - turnaroundFrames; i++) {
				holdingAngles[i] = vectorAngle;
			}
			Debug.println("Turnaround Frames: " + turnaroundFrames);
			if (turnaroundFrames == 2) { //this is only optimal for non cap throws
				holdingAngles[frames - turnaroundFrames] = vectorAngle - Math.PI * 136/180.0;
				holdingAngles[frames - 1] = vectorAngle - Math.PI * (136 + difference)/180.0;
				holdingMinRadius[frames - 1] = true;
			}
			else {
				holdingAngles[frames - turnaroundFrames] = vectorAngle + Math.PI * 181/180.0;
				if (turnaroundFrames > 1)
					holdingAngles[frames - turnaroundFrames + 1] = vectorAngle + Math.PI * 2/180.0;
				if (turnaroundFrames > 2)
					holdingAngles[frames - turnaroundFrames + 2] = vectorAngle - Math.PI * 5/180.0;
				if (turnaroundFrames > 3)
					holdingAngles[frames - turnaroundFrames + 3] = vectorAngle - Math.PI * 9/180.0;
				if (difference < -0.001) {
					holdingAngles[frames - 1] = angle;
				}
			}
			holdingMinRadius[frames - turnaroundFrames] = true;
			motion.setHolding(holdingAngles, holdingMinRadius);
		}
		else {
			holdingAngles[0] = angle;
			int lastNormalAngleFrame = (frames - 1) / 2;
			for (int i = 1; i <= lastNormalAngleFrame; i++)
				holdingAngles[i] = SimpleMotion.NORMAL_ANGLE;
			for (int i = lastNormalAngleFrame + 1; i < frames; i++)
				holdingAngles[i] = angle;
			motion.setHoldingAngles(holdingAngles);
		}
		return true;
	}
	
	private void setOtherMovementHoldingAngles(ComplexVector motion, int index, double angle, double initialAngle, double initialRotation, boolean rightVector) {
		
		Movement movement;
		SimpleVector angleCalculator;
		int frames = movementFrames.get(index);

		if (index == 0) {
			movement = new Movement(movementNames.get(index), p.initialHorizontalSpeed, p.rocketFlower);
			angleCalculator = (SimpleVector) movement.getMotion(frames, rightVector, false);
			angleCalculator.setInitialAngle(Math.PI / 2);
			angleCalculator.setInitialRotation(Math.PI / 2 + (p.chooseInitialRotation ? Math.toRadians(p.initialRotation) : motion.movement.defaultRotation));
		}
		else {
			movement = new Movement(movementNames.get(index), motions[index - 1].finalSpeed, canRocketFlower(index) ? p.rocketFlower : false);
			angleCalculator = (SimpleVector) movement.getMotion(frames, rightVector, false);
			angleCalculator.setInitialAngle(initialAngle);
			angleCalculator.setInitialRotation(initialRotation);
		}

		Debug.println(1, "Initial Angle: " + Math.toDegrees(initialAngle));
		Debug.println(1, "Initial Rotation: " + Math.toDegrees(initialRotation));
		Debug.println(1, "Target Rotation: " + Math.toDegrees(initialAngle + (rightVector ? -angle : angle)));
		
		//System.out.println("Target Rotation (Relative): " + Math.toDegrees(angle));

		//first check if we can just use the optimal version
		boolean canUseOptimal = true;

		double forwardAccelFrames = VectorCalculator.clampDouble((movement.defaultSpeedCap - movement.initialHorizontalSpeed) / movement.forwardAccel, 0, frames);
		int totalForwardAccelFrames = (int) Math.ceil(forwardAccelFrames);

		int turnaroundFrames = 0;
		double ang_deg = Math.toDegrees(SimpleMotion.NORMAL_ANGLE - angle);
		if (ang_deg <= 25)
			canUseOptimal = false;
		else if (ang_deg <= 25 + 22.5)
			turnaroundFrames = 2;
		else if (ang_deg <= 25 + 22.5 + 20)
			turnaroundFrames = 3;
		else if (ang_deg <= 25 + 22.5 + 20 + 17.5)
			turnaroundFrames = 4;
		else
			canUseOptimal = false;

		//System.out.println("Turnaround Frames: " + turnaroundFrames);
		
		double[] holdingAngles = new double[frames];
		boolean[] holdingMinRadius = new boolean[frames];
		double[] rotations = angleCalculator.calcRelativeRotations();

		// for (int z = 0; z < frames; z++) {
		// 	if (Debug.debug == 0)
		// 		System.out.printf("Frame %d, Rotation %.3f\n", z, Math.toDegrees(initialAngle + (rightVector ? -rotations[z] : rotations[z])));
		// }
		// Debug.println(1, "");

		//int framesToFullRotation = angleCalculator.calcFramesToFullRotation();
		int framesToFullRotation = -1;
		int framesToTargetRotation = -1;

		double prevRotation = rotations[0];

		for (int i = 0; i < rotations.length; i++) {
			while (rotations[i] < -Math.PI)
				rotations[i] += Math.PI * 2;
			while (rotations[i] > Math.PI)
				rotations[i] -= Math.PI * 2;

			if ((prevRotation <= angle && angle <= rotations[i]) || (prevRotation >= angle && angle >= rotations[i])) {
				if (framesToTargetRotation == -1)
					framesToTargetRotation = i;
			}
			if (rotations[i] == SimpleMotion.NORMAL_ANGLE) {
				framesToFullRotation = i + 1;
				break;
			}
			prevRotation = rotations[i];
		}

		//System.out.println("Frames to Full Rotation: " + framesToFullRotation);

		if (framesToFullRotation < 0)
			canUseOptimal = false;
		if (framesToFullRotation + turnaroundFrames > frames)
			canUseOptimal = false;

		if (canUseOptimal) {
			Debug.println(1, "Using optimal");
			setOptimalHoldingAngles(motion, angle, OPTIMAL_ANGLE_DIFF, SimpleMotion.NORMAL_ANGLE, frames);
			return;
		}

		double targetRotation = initialAngle + (rightVector ? -angle : angle);
		//int framesToTargetRotation = angleCalculator.calcFramesToRotation(targetRotation);
		boolean counterrotationMethod = false;
		boolean quickturnAssistMethod = false;
		boolean counterQuickturn = false; //if quickturn is contrary to the direction of regular rotation

		double totalRotation = Math.abs(targetRotation - initialRotation); //TODO eliminate math.abs
		double rotationWithoutQuickturn = totalRotation;
		double minRotation = angleCalculator.calcMinRotation();
		double overshoot = 0;

		int neutralFrames = 0; //frames of neutral before quickturn (might need 1 to make the qt rotate in the correct direction)

		Debug.println(1, "Frames to Target Rotation: " + framesToTargetRotation);

		if (framesToTargetRotation != -1) {
			counterrotationMethod = true;
			if (minRotation <= totalRotation)
				turnaroundFrames = 0;
			else { //TODO still might not cover all bases
				turnaroundFrames = 1;
				neutralFrames = 1;
				minRotation -= (FAST_TURNAROUND_VELOCITY + motion.angularAccel + motion.angularAccel); //replace one frame of min rotation with quickturn the opposite way, and also replace a frame of min rotation with neutral
				counterQuickturn = true;
			}
		}

		//we can't rotate enough normally and need to add a quickturn to help
		if (framesToTargetRotation == -1) {
			double rotationWithQuickturn = -Double.MAX_VALUE;
			turnaroundFrames = 0; //which type of fast turnaround from the list that we're checking
			while (rotationWithQuickturn < angle && turnaroundFrames < fastTurnarounds.length - 1) {
				turnaroundFrames++;
				rotationWithoutQuickturn = rotations[frames - 1 - turnaroundFrames];
				rotationWithQuickturn = rotationWithoutQuickturn + fastTurnarounds[turnaroundFrames];
				overshoot = rotationWithQuickturn - angle;
			}

			if (turnaroundFrames == 1 && totalRotation > (FAST_TURNAROUND_VELOCITY + (minRotation - motion.angularAccel))) {
				counterrotationMethod = true;
				minRotation += FAST_TURNAROUND_VELOCITY - motion.angularAccel;
			}
			else if (turnaroundFrames <= 4) //TODO accept larger?
				quickturnAssistMethod = true;
		}

		//counterrotation method (turnaroundFrames is 0 or 1)
		if (counterrotationMethod) {
			double firstCounterrotation = motion.angularAccel * (totalForwardAccelFrames + 1); //how much counterrotation on the frame right after the first normal angle frame
			Debug.println(1, "Min Rotation: " + Math.toDegrees(minRotation));
			Debug.println(1, "Total Rotation: " + Math.toDegrees(totalRotation));
			double rotationSum = minRotation;
			double angularVelocity = motion.angularAccel;
			int additionalRotationFrames = 0;
			int maxAdditionalRotationFrames = frames - (totalForwardAccelFrames + 1) - turnaroundFrames - neutralFrames;
			double replacementAngularVelocity = motion.angularAccel; //angular velocity of the frame we're replacing
			while (rotationSum < totalRotation) { //swap out frames of min rotation for frames with regular rotation
				if (angularVelocity >= motion.maxAngVel)
					angularVelocity -= Math.toRadians(2.5);
				else
					angularVelocity += motion.angularAccel;
				rotationSum += angularVelocity - replacementAngularVelocity;
				additionalRotationFrames++;
				if (additionalRotationFrames >= maxAdditionalRotationFrames) { //this is really complicated but our first counterrotation frame's velocity can increase so we keep replacing it
					additionalRotationFrames = maxAdditionalRotationFrames;
					replacementAngularVelocity += motion.angularAccel;
					firstCounterrotation -= motion.angularAccel;
					if (firstCounterrotation < 0) //TODO this is a fail state, but this should not commonly occur
						firstCounterrotation = 0;
						break;
				}
				//System.out.println(Math.toDegrees(rotationSum));
			}
			overshoot = rotationSum - totalRotation;
			Debug.println(1, "Additional Rotation Frames: " + additionalRotationFrames);
			Debug.println(1, "Overshoot: " + Math.toDegrees(overshoot));

			int firstAdditionalRotationFrame = frames - additionalRotationFrames - turnaroundFrames - neutralFrames;
			
			for (int i = 0; i < (int) forwardAccelFrames; i++)
                holdingAngles[i] = 0;
            if (forwardAccelFrames != totalForwardAccelFrames && totalForwardAccelFrames > 0)
                holdingAngles[totalForwardAccelFrames - 1] = Math.acos(forwardAccelFrames - (int) forwardAccelFrames);
			if (totalForwardAccelFrames < frames)
				holdingAngles[totalForwardAccelFrames] = SimpleMotion.NORMAL_ANGLE;
			if (totalForwardAccelFrames + 1 < frames)
				holdingAngles[totalForwardAccelFrames + 1] = SimpleMotion.NORMAL_ANGLE - firstCounterrotation;
			for (int i = totalForwardAccelFrames + 2; i < firstAdditionalRotationFrame; i++) {
				holdingAngles[i] = holdingAngles[i - 1] - motion.angularAccel;
				//currentRotation += Math.toRadians(0.3);
			}
			for (int i = Math.max(totalForwardAccelFrames + 2, firstAdditionalRotationFrame); i < frames - turnaroundFrames - neutralFrames; i++) {
				holdingAngles[i] = SimpleMotion.NORMAL_ANGLE;
			}
			holdingAngles[frames - 1 - turnaroundFrames - neutralFrames] = holdingAngles[frames - 2 - turnaroundFrames - neutralFrames] - (additionalRotationFrames == 0 ? motion.angularAccel : overshoot);
			if (neutralFrames == 1)
				holdingAngles[frames - turnaroundFrames - neutralFrames] = SimpleMotion.NO_ANGLE;
			if (turnaroundFrames == 1) {
				if (counterQuickturn)
					holdingAngles[frames - 1] = angle + FAST_TURNAROUND_VELOCITY - Math.PI * (136 / 180.0);
				else
					holdingAngles[frames - 1] = angle - FAST_TURNAROUND_VELOCITY + Math.PI * (136 / 180.0);
				holdingMinRadius[frames - 1] = true;
			}
			motion.setHolding(holdingAngles, holdingMinRadius);
			return;
		}

		if (quickturnAssistMethod) {
			Debug.println(1, "Overshoot: " + overshoot);

			for (int i = 0; i < (int) forwardAccelFrames; i++)
                holdingAngles[i] = 0;
            if (forwardAccelFrames != totalForwardAccelFrames && totalForwardAccelFrames > 0)
                holdingAngles[totalForwardAccelFrames - 1] = Math.acos(forwardAccelFrames - (int) forwardAccelFrames);
			for (int i = totalForwardAccelFrames; i < frames - turnaroundFrames; i++) {
				holdingAngles[i] = SimpleMotion.NORMAL_ANGLE;
			}
			if (turnaroundFrames == 2) { //this is only optimal for non cap throws
				holdingAngles[frames - turnaroundFrames] = rotationWithoutQuickturn + Math.PI * 136/180.0;
				holdingAngles[frames - 1] = rotationWithoutQuickturn + Math.PI * 136/180.0 - overshoot;
				holdingMinRadius[frames - 1] = true;
			}
			else {
				holdingAngles[frames - turnaroundFrames] = rotationWithoutQuickturn - Math.PI * 181/180.0;
				if (turnaroundFrames > 1) {
					holdingAngles[frames - turnaroundFrames + 1] = rotationWithoutQuickturn - Math.PI * 2/180.0;
					holdingMinRadius[frames - turnaroundFrames + 1] = true;
				}
				if (turnaroundFrames > 2) {
					holdingAngles[frames - turnaroundFrames + 2] = rotationWithoutQuickturn + Math.PI * 5/180.0;
					holdingMinRadius[frames - turnaroundFrames + 2] = true;
				}
				if (turnaroundFrames > 3) {
					holdingAngles[frames - turnaroundFrames + 3] = rotationWithoutQuickturn + Math.PI * 9/180.0;
					holdingMinRadius[frames - turnaroundFrames + 3] = true;
				}
				if (overshoot > Math.toRadians(0.001)) {
					holdingAngles[frames - 1] = angle;
					holdingMinRadius[frames - 1] = false;
				}
			}
			holdingMinRadius[frames - turnaroundFrames] = true;
			motion.setHolding(holdingAngles, holdingMinRadius);
			return;
		}

		motion.setHolding(holdingAngles, holdingMinRadius);
	}

	private void setHCTFallingHoldingAngles(ComplexVector motion) {
		Movement movement = motion.movement;
		double[] holdingAngles = new double[motion.frames];

		double forwardAccelFrames = Math.max((movement.defaultSpeedCap - movement.initialHorizontalSpeed) / movement.forwardAccel, 0);
		int totalForwardAccelFrames = (int) Math.ceil(forwardAccelFrames);
		int totalCountervectorFrames = (int) Math.ceil(variableHCTCountervectorFrames);
		double partialCountervector = variableHCTCountervectorFrames - (int) variableHCTCountervectorFrames;

		int firstVectorAngleFrame = Math.max(totalForwardAccelFrames, 1);
		int firstCountervectorFrame = Math.max(motion.frames - totalCountervectorFrames, 0); //if totalHCTCountervectorFrames is large enough, it will overwrite the last forward accel frame which is intentional

		//number of frames the variable holding angle is applied for
		//TODO this is what should be calculated INSTEAD of the angle
		
		for (int i = 0; i < (int) forwardAccelFrames; i++)
			holdingAngles[i] = 0;
		if (forwardAccelFrames != totalForwardAccelFrames && totalForwardAccelFrames > 0) {
			if (firstCountervectorFrame == (int) forwardAccelFrames) //this might not run anymore
				holdingAngles[totalForwardAccelFrames - 1] = 0;
			else
				holdingAngles[totalForwardAccelFrames - 1] = Math.acos(forwardAccelFrames - (int) forwardAccelFrames);
		}
		for (int i = firstVectorAngleFrame; i < firstCountervectorFrame; i++)
			holdingAngles[i] = SimpleMotion.NORMAL_ANGLE;
		if (partialCountervector > 0)
			holdingAngles[firstCountervectorFrame] = SimpleMotion.NORMAL_ANGLE - (Math.PI * partialCountervector);
		for (int i = firstCountervectorFrame + (partialCountervector > 0 ? 1 : 0); i < motion.frames; i++) {
			holdingAngles[i] = -SimpleMotion.NORMAL_ANGLE; //really should test both directions
		}
		
		motion.setHoldingAngles(holdingAngles);
	}

	private void setYankHoldingAngles(SimpleMotion[] motionGroup, Movement movement, int motionIndex, int movementIndex, double yankFrames) {
		double forwardAccelFrames = Math.max((movement.defaultSpeedCap - movement.initialHorizontalSpeed) / movement.forwardAccel, 0);
		int firstMaxForwardSpeedFrame = (int) Math.ceil(forwardAccelFrames);
		if ((firstMaxForwardSpeedFrame == 0 && yankFrames == 0)) {
			motionGroup[motionIndex] = movement.getMotion(movementFrames.get(movementIndex), currentVectorRight, false);
			return;
		}
		motionGroup[motionIndex] = movement.getMotion(movementFrames.get(movementIndex), currentVectorRight, true);
		double[] holdingAngles = new double[movementFrames.get(movementIndex)];
		int fullYankFrames = (int) yankFrames;
		int allYankFrames = (int) Math.ceil(yankFrames);
		double partialYank = yankFrames - fullYankFrames;
		if (movement.canVector) {
			for (int a = 0; a < firstMaxForwardSpeedFrame; a++) {
				holdingAngles[a] = 0;
			}
			if (forwardAccelFrames != firstMaxForwardSpeedFrame) {
				holdingAngles[firstMaxForwardSpeedFrame - 1] = Math.acos(forwardAccelFrames - (int) forwardAccelFrames);
			}
			for (int a = firstMaxForwardSpeedFrame; a < holdingAngles.length - allYankFrames; a++) {
				holdingAngles[a] = SimpleMotion.NORMAL_ANGLE;
			}
			if (allYankFrames > fullYankFrames) {
				holdingAngles[holdingAngles.length - allYankFrames] = SimpleMotion.NORMAL_ANGLE + (partialYank * Math.PI / 2);
			}
			for (int a = holdingAngles.length - fullYankFrames; a < holdingAngles.length; a++) {
				holdingAngles[a] = SimpleMotion.BACK_ANGLE;
			}
			((ComplexVector) motionGroup[motionIndex]).setHoldingAngles(holdingAngles);
		}
		else {
			for (int a = 0; a < holdingAngles.length - allYankFrames; a++) {
				holdingAngles[a] = 0;
			}
			if (allYankFrames > fullYankFrames) {
				holdingAngles[holdingAngles.length - allYankFrames] = partialYank * Math.PI / 2;
			}
			double normalAngle = SimpleMotion.NORMAL_ANGLE;
			double deltaVelocityAngle = Math.atan(movement.forwardAccel / Math.max(movement.defaultSpeedCap, movement.initialHorizontalSpeed));
			boolean fullSpeed = (movement.defaultSpeedCap - movement.initialHorizontalSpeed) / movement.forwardAccel <= holdingAngles.length - allYankFrames;
			for (int a = holdingAngles.length - fullYankFrames; a < holdingAngles.length; a++) {
				if (fullSpeed) {
					holdingAngles[a] = normalAngle;
					normalAngle += deltaVelocityAngle;
				}
				else {
					holdingAngles[a] = Math.PI / 4; //this is a heuristic, it seems to be a good angle to hold to balance changing angle with building speed
				}
			}
			if (!fullSpeed) {
				holdingAngles[holdingAngles.length - 1] = normalAngle; //this is a heuristic, it seems to be a good angle to hold to balance changing angle with building speed
			}
			else if (fullYankFrames > 0) {
				holdingAngles[holdingAngles.length - fullYankFrames - 1] = Math.PI / 3; //another heuristic, seems to work well for long jumps
				holdingAngles[holdingAngles.length - fullYankFrames] = Math.PI / 3;
			}
			((ComplexNonvector) motionGroup[motionIndex]).setHoldingAngles(holdingAngles);
		}
	}
	
	private SimpleMotion[] calcMotionGroup(int startIndex, int endIndex, double initialVelocity, int framesJump) {
        SimpleMotion[] motionGroup = new SimpleMotion[endIndex - startIndex];
        if (motionGroup.length == 0)
            return motionGroup;
        
        //calculate the trajectory of the inital movement

        //case for roll cancel vectors (note it assumes at least 1 falling frame afterward)
        //if we're the first motion group and there's a variable roll cancel
        if (hasVariableRollCancel && startIndex == 0) {
            Movement rc = new Movement(movementNames.get(0), initialVelocity, p.rocketFlower);
            GroundedCapThrow rcMotion = new GroundedCapThrow(rc, Math.PI / 2, rcTrueInitialAngleDiff, rcFinalAngleDiff, !currentVectorRight);
            rcMotion.calcDispDispCoordsAngleSpeed();
            motionGroup[0] = rcMotion;
        }
        else {
            Movement initialMovement = new Movement(movementNames.get(startIndex), initialVelocity, framesJump, startIndex == 0 ? p.rocketFlower : false); //need to add frames jump if want to use that here
            if (startIndex == 0 && movementNames.get(1).equals("Backflip"))
                initialMovement.defaultRotation = Math.PI;
            if (startIndex == listPreparer.initialMovementIndex)
                setYankHoldingAngles(motionGroup, initialMovement, 0, startIndex, imYankFrames);
            else
                motionGroup[0] = initialMovement.getMotion(movementFrames.get(startIndex), currentVectorRight, false);
            motionGroup[0].setInitialAngle(Math.PI / 2);
            motionGroup[0].calcDispDispCoordsAngleSpeed();
            if (!(motionGroup[0].getClass().getSimpleName().equals("SimpleMotion") || motionGroup[0].getClass().getSimpleName().equals("CoyoteTime")) || movementNames.get(startIndex).equals("Ground Pound"))
                currentVectorRight = !currentVectorRight;
        }

        for (int i = 1; i < motionGroup.length; i++) {
            int j = i + startIndex;
            Movement currentMovement;
            if (movementNames.get(j - 1).equals("Moonwalk") || movementNames.get(j - 1).equals("Coyote Time"))
                currentMovement = new Movement(movementNames.get(j), motionGroup[i - 1].finalSpeed, framesJump, p.rocketFlower);
            else if (canRocketFlower(j))
				currentMovement = new Movement(movementNames.get(j), motionGroup[i - 1].finalSpeed, p.rocketFlower);
			else
                currentMovement = new Movement(movementNames.get(j), motionGroup[i - 1].finalSpeed);
            if (j == listPreparer.initialMovementIndex) {
                 setYankHoldingAngles(motionGroup, currentMovement, i, j, imYankFrames);
            }
            else if (movementNames.get(j).equals("Homing Motion Cap Throw")) {          
                motionGroup[i] = currentMovement.getMotion(movementFrames.get(j), currentVectorRight, true);
                ((ComplexVector) motionGroup[i]).setHoldingAngles(generateHomingMotionThrowHoldingAngles());
            }
            else if (movementNames.get(j).equals("Rainbow Spin") && rsYankFrames > 0) {
                setYankHoldingAngles(motionGroup, currentMovement, i, j, rsYankFrames);
            }
            else if (Movement.isCapBounce(movementNames.get(j)) && cbYankFrames > 0) {
                setYankHoldingAngles(motionGroup, currentMovement, i, j, cbYankFrames);
            }
            else if (movementNames.get(j).equals("Dive")) {
                preCapBounceDiveIndex = j;
                motionGroup[i] = currentMovement.getMotion(movementFrames.get(j), currentVectorRight, true);
                ((DiveTurn) motionGroup[i]).firstFrameDecel = firstFrameDecel;
                if (p.diveTurn == TurnDuringDive.NO || (p.reverseBonk && j == movementNames.size() - 2)) {
                    ((DiveTurn) motionGroup[i]).setHoldingAngle(0);
                }
            }
            else
                motionGroup[i] = currentMovement.getMotion(movementFrames.get(j), currentVectorRight, false);
            if (hasVariableHCTFallVector && j == variableHCTFallIndex) { //use the holding angle we are testing this iteration for optimizing the HCT fall  
				if (complexHCTFallVector) {
					motionGroup[i] = currentMovement.getMotion(movementFrames.get(j), currentVectorRight, true);
					setHCTFallingHoldingAngles((ComplexVector) motionGroup[i]);
				}
				else {
					variableHCTCountervectorFrames = 0;
					((SimpleVector) motionGroup[i]).setHoldingAngle(variableHCTHoldingAngle);
					// Debug.println("Testing: " + variableHCTHoldingAngle);
					if (movementFrames.get(j) <= 3) {
						((SimpleVector) motionGroup[i]).optimalForwardAccel = false; //may need to not hold straight ahead in the falling frames even though under max speed
					}
				}
                if (!switchHCTFallVectorDir) {
                    currentVectorRight = !currentVectorRight;
                }
                Debug.println("HCT Optimize Branch Activated!");
            }
            motionGroup[i].setInitialAngle(motionGroup[i - 1].finalAngle);
            motionGroup[i].calcDispDispCoordsAngleSpeed();
            //System.out.println("Previous angle: " + Math.toDegrees(motionGroup[i - 1].finalAngle));
            //System.out.println("It was a: " + movementNames.get(j - 1));
            //System.out.println("It is a: " + movementNames.get(j));
            //System.out.println(motionGroup[i].dispX);
            //System.out.println(motionGroup[i].dispZ);
            //if the movement is falling, switch the vector only if j is the index of the hct AND we are hct second OR the falling is part of an RCV/the initial movement
            //if the movement is an HCT, do not switch the vector if HCT second
            if (j == listPreparer.initialMovementIndex && movementNames.get(j).equals("Falling"))
                currentVectorRight = !currentVectorRight;
            else if (!(movementNames.get(j).equals("Falling") || motionGroup[i].getClass().getSimpleName().equals("SimpleMotion")))
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
            Movement rcCapThrow = new Movement(movementType, initialVelocity, p.rocketFlower);
            GroundedCapThrow rcMotion = new GroundedCapThrow(rcCapThrow, 0, rcTrueInitialAngleDiff, test, true);
            rcMotion.calcDispDispCoordsAngleSpeed();
            //Debug.println("Final Angle: " + Math.toDegrees(rcMotion.finalAngle));
            Movement rcv = new Movement("Falling", rcMotion.finalSpeed, p.rocketFlower);
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
	
	public double maximize() {
		if (Debug.debug >= 0)
			return maximize(MAX_TRY);
		try {
			return maximize(MAX_TRY);
		}
		catch (Exception ex) {
			bestDisp = 0;
			if (error.equals(""))
				error = "Error: Calculator failed";
			return 0;
		}
	}

	public static final int MAX_IM_YANK_FRAMES = 32;
	public static final int MAX_IM_YANK_FRAMES_NONVECTOR = 32;
	public static final double MAX_IM_LIMIT = 0.99;
	public static final double MAX_IM_NONVECTOR_LIMIT = 0.99;
	
	//the maximize functions are called in this order, each by the next so that all necessary permutations are tested
	public static final int MAX_TRY = 0, MAX_IM = 1, MAX_RS = 2, MAX_CB = 3, MAX_HCT = 4, MAX_VA1 = 5, MAX_VA2 = 6;
	public double maximize(int optID) {
		switch (optID) {
			case MAX_TRY:
				return maximize_try();
			case MAX_IM:
				if (p.maximizeYank && optimizeIMYank) {
					Movement initialMovement = new Movement(movementNames.get(listPreparer.initialMovementIndex));
					if (p.onMoon && p.midairVault)
						return binarySearch(0, MAX_IM_YANK_FRAMES, MAX_IM, MAX_IM_LIMIT)[0];
					else if (initialMovement.canVector)
						return linearSearch(0, MAX_IM_YANK_FRAMES, MAX_IM)[0];
					else if (initialMovement.sidewaysAccel > 0)
						return binarySearch(0, MAX_IM_YANK_FRAMES_NONVECTOR, MAX_IM, MAX_IM_NONVECTOR_LIMIT)[0];
				}
				else break;
			case MAX_RS: //holding back on last rainbow spin frame
				if (hasRainbowSpin && p.maximizeYank && optimizeRSYank)
					return linearSearch(0, 1, MAX_RS)[0];
				else break;
			case MAX_CB: //holding back on last cap bounce frame(s)
				if (hasCapBounce && p.maximizeYank && movementFrames.get(cbIndex) >= 50 && optimizeCBYank) //only test for long cbs
					return linearSearch(0, MAX_IM_YANK_FRAMES, MAX_CB)[0];
				else break;
			case MAX_HCT: //hct falling optimization
				if (hasVariableHCTFallVector)
					if (complexHCTFallVector)
						return binarySearch(0, 16, MAX_HCT, 0.49)[0];
					else
						return binarySearch(- Math.PI / 2, Math.PI / 2, MAX_HCT, maximize_HCT_limit)[0];
				else break;
			case MAX_VA1: //first cap throw angle optimization, and also finds second cap throw/last movement optimization via findVariableAngle2()
				return maximize_variableAngle1();
			case MAX_VA2:
				return maximize_variableAngle2();
			default:
				return 0;
		}
		return maximize(optID + 1);
	}

	//applies the serach value based on what is being optimized
	public void applySearchValue(double value, int optID) {
		switch (optID) {
			case MAX_HCT:
				if (complexHCTFallVector)
					variableHCTCountervectorFrames = value;
				else {
					if (value < 0) {
						switchHCTFallVectorDir = true;
						variableHCTHoldingAngle = -value;
					}
					else {
						switchHCTFallVectorDir = false;
						variableHCTHoldingAngle = value;
					}
				}
				break;
			case MAX_IM:
				imYankFrames = value;
				break;
			case MAX_RS:
				rsYankFrames = value;
				break;
			case MAX_CB:
				cbYankFrames = value;
				break;
		}
	}

	//performs a modified binary search assuming ascending numbers
	//optID is the optimization being performed
	//limit is the smallest increment; when reached it stops the search
	public double[] binarySearch(double low, double high, int optID, double limit) {
		double quarter = (high - low) / 4;
		double med = (high + low) / 2;
		applySearchValue(med, optID);
		double lowMed;
		double highMed;
		double medDisp = maximize(optID + 1);
		double lowMedDisp;
		double highMedDisp;

		while (quarter > limit) {
			lowMed = med - quarter;
			highMed = med + quarter;
			Debug.println("BS vals: " + lowMed + ", " + highMed);
			applySearchValue(lowMed, optID);
			lowMedDisp = maximize(optID + 1);
			applySearchValue(highMed, optID);
			highMedDisp = maximize(optID + 1);
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
		applySearchValue(bestValue, optID);
		double bestDisp = maximize(optID + 1);
		return new double[]{bestDisp, bestValue};
	}

	private double[] linearSearch(int low, int high, int optID) {
		int value = low;
		applySearchValue(value, optID);
		double bestDisp = maximize(optID + 1);
		for (value = low + 1; value <= high; value++) { //keep trying to yank for more frames until the result is worse
			applySearchValue(value, optID);
			double curDisp = maximize(optID + 1);
			if (curDisp <= bestDisp)
				break;
			bestDisp = curDisp;
			if (value == high)
				return new double[]{bestDisp, value};
		}
		value--;
		applySearchValue(value, optID);
		return new double[]{maximize(optID + 1), value};
	}

	//this function finds the correct/optimal RCV if applicable
	public double maximize_try() {
		long startTime = System.currentTimeMillis();

		diveCapBounceAngle = p.diveCapBounceAngle;
		vectorAngle = p.vectorAngle;
		firstFrameDecel = p.diveFirstFrameDecel;

		if (p.initialAndTargetGiven) {
			while (initialAngle - targetAngle > Math.PI) {
				initialAngle -= Math.PI * 2;
			}
			while (initialAngle - targetAngle < -Math.PI) {
				initialAngle += Math.PI * 2;
			}
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
			maximize(MAX_VA1); //TODO: update this to not check other later maximize functions
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
					Movement rc = new Movement(Movement.RC_TYPES[i], p.rocketFlower);
					GroundedCapThrow rcMotion = new GroundedCapThrow(rc, false);
					int totalFrames = rcMotion.calcFrames(p.initialDispY);
					movementFrames.set(0, rc.minFrames);
					movementFrames.set(1, totalFrames - rc.minFrames);
					rcFinalAngleDiff = calcRCFinalAngleDiff(movementNames.get(0), p.initialHorizontalSpeed, movementFrames.get(1));

					maximize(MAX_TRY + 1);

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
				rcFinalAngleDiff = calcRCFinalAngleDiff(movementNames.get(0), p.initialHorizontalSpeed, movementFrames.get(1));
			}
			
			//optimize the rc, then try to get the rc initial angle to be the same as the target angle
			double bestUnadjustedTargetAngle = Math.PI;
			double unadjustedTargetAngle = Math.PI;
			double increment = 0;
			//on the first iteration just maximize it and see how far off we are
			//then keep nudging it slightly
			for (int i = 1; i <= maxRCVNudges; i++) {
				maximize(MAX_TRY + 1);
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

				maximize(MAX_TRY + 1);

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
			maximize(MAX_TRY + 1);
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

		double totalDispX = 0;
		for (int i = 0; i < motions.length; i++) {
			totalDispX += motions[i].dispX;
			Debug.printf("%s: %.3f %.3f %.3f\n", movementNames.get(i), motions[i].dispX, motions[i].dispZ, totalDispX);
		}
		
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

		//calculate rotations properly
		calcFinalRotation(motions, true);


		//rotating motions to the right angle
		//adjustToGivenAngle();
		
		//Debug.println("Angle 1: " + Math.toDegrees(bestAngle1));
		//Debug.println("Angle 2: " + Math.toDegrees(bestAngle2));
		Debug.println("Calculated in " + (System.currentTimeMillis() - startTime) + " ms");

		return bestDisp;
	}
	
	private double[] calcFallingDisplacements(SimpleVector variableCapThrowVector, int variableCapThrowIndex, double variableAngleAdjusted, boolean vectorRight, boolean optimizeFalling, boolean roughOptimizeFalling) {
		double[] displacements = new double[2];
		variableCapThrowVector.calcFinalAngle();
		Movement variableCapThrowFalling = new Movement("Falling", variableCapThrowVector.calcFinalSpeed());
		SimpleVector variableCapThrowFallingVector;
		double holdingAngle = (vectorRight ? 1 : -1) * (variableCapThrowVector.finalAngle - variableAngleAdjusted);
		variableCapThrowFallingVector = (SimpleVector) variableCapThrowFalling.getMotion(movementFrames.get(variableCapThrowIndex + 1), vectorRight, (optimizeFalling && !roughOptimizeFalling) || (!optimizeFalling && holdingAngle < 0));
		//SimpleVector variableCapThrowFallingVector = (SimpleVector) variableCapThrowFalling.getMotion(movementFrames.get(variableCapThrowIndex + 1), vectorRight, false);
		motions[variableCapThrowIndex + 1] = variableCapThrowFallingVector;
		if (optimizeFalling) {
			if (roughOptimizeFalling) {
				variableCapThrowFallingVector.setInitialAngle(variableCapThrowVector.finalAngle);
				//variableCapThrowFallingVector.setHoldingAngle(holdingAngle);
			}
			else {
				ComplexVector variableCapThrowFallingVectorC = (ComplexVector) variableCapThrowFallingVector;
				variableCapThrowFallingVectorC.setOptimalForwardAccel(false); //not trying to be optimal, simply trying to end up in the right direction
				variableCapThrowFallingVectorC.setInitialAngle(variableCapThrowVector.finalAngle);
				//double ctFinalRotation = variableCapThrowVector.initialAngle + (variableCapThrowVector.rightVector ? -SimpleMotion.NORMAL_ANGLE : SimpleMotion.NORMAL_ANGLE);
				variableCapThrowVector.setInitialRotation(variableCapThrowVector.initialAngle + (variableCapThrowVector.rightVector ? -variableCapThrowVector.holdingAngle : variableCapThrowVector.holdingAngle)); //TODO breaks if NO_ANGLE?
				double ctFinalRotation;
				if (roughCTRotations) {
					if (variableCapThrowIndex == variableCapThrow1Index)
						ctFinalRotation = variableCapThrowVector.initialAngle + (variableCapThrowVector.rightVector ? -Math.toRadians(vectorAngle) : Math.toRadians(vectorAngle)); //TODO this doesn't seem perfect
					else
						ctFinalRotation = variableCapThrowVector.normalAngle;
				}
				else
					ctFinalRotation = variableCapThrowVector.calcFinalRotation();
				double fallingInitialAngle = variableCapThrowVector.finalAngle;
				double ctTargetRotation = vectorRight ? fallingInitialAngle - variableAngleAdjusted : variableAngleAdjusted - fallingInitialAngle;
				setOtherMovementHoldingAngles(variableCapThrowFallingVectorC, variableCapThrowIndex + 1, ctTargetRotation, fallingInitialAngle, ctFinalRotation, vectorRight);
				//setFinalFallingHoldingAngles(variableCapThrowFallingVectorC, variableAngleAdjusted - ctFinalRotation, holdingAngle, movementFrames.get(variableCapThrowIndex + 1));
				//variableCapThrowFallingVectorC.setHoldingAngle(0);
			}
		}
		else {
			variableCapThrowFallingVector.setOptimalForwardAccel(false); //not trying to be optimal, simply trying to end up in the right direction
			variableCapThrowFallingVector.setInitialAngle(variableCapThrowVector.finalAngle);
			//Debug.println(Math.toDegrees(variableCapThrowVector.finalAngle));
			variableCapThrowFallingVector.setHoldingAngle(holdingAngle);
		}
		
		variableCapThrowFallingVector.calcDisp();
		variableCapThrowFallingVector.calcDispCoords();
		displacements[0] = variableCapThrowFallingVector.dispZ;
		displacements[1] = variableCapThrowFallingVector.dispX;
		return displacements;
	}

	//one iteration of maximization of variable angles 1 and 2 if they exist
	private double maximize_variableAngle1() {
		currentVectorRight = rightVector;

		//calculate the total displacement of all the movement before the first cap throw whose angle can be variable
		motionGroup1 = calcMotionGroup(0, Math.min(variableCapThrow1Index, variableMovement2Index), p.initialHorizontalSpeed, p.framesJump);
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
		
		once_bestDisp = Math.sqrt(dispZMotionGroup1 * dispZMotionGroup1 + dispXMotionGroup1 * dispXMotionGroup1);
		
		//Debug.println(variableCapThrow1VectorRight);
		
		//SimpleMotion[] motionGroup2 = null;
		
		//first variable movement
		if (hasVariableCapThrow1) {
			//we need a motion group 2
			variableCapThrow1VectorRight = currentVectorRight;
			currentVectorRight = !currentVectorRight;
			
			motionGroup2VectorRight = currentVectorRight;

			calcMotionGroup2();
			
			Debug.println("motion group 2 disp: " + dispMotionGroup2);
			
			//optimize the first variable cap throw
			Movement variableCapThrow1 = new Movement(movementNames.get(variableCapThrow1Index), motions[variableCapThrow1Index - 1].finalSpeed);
			variableCapThrow1Frames = movementFrames.get(variableCapThrow1Index);
			//Debug.println("frames: " + variableCapThrow1Frames);
			variableCapThrow1Vector = (SimpleVector) variableCapThrow1.getMotion(variableCapThrow1Frames, variableCapThrow1VectorRight, true);
			motions[variableCapThrow1Index] = variableCapThrow1Vector;
			variableCapThrow1Vector.setInitialAngle(motionGroup1FinalAngle);
			
			double low = 0;
			double high = Math.PI / 2;
			double med = (high + low) / 2;
			// if (p.initialFrames < 10) {
			// 	high = Math.PI;
			// 	med = Math.PI / 2;
			// }
			double medDisp = calcDisp(med);
			double lowMed;
			double lowMedDisp;
			double highMed;
			double highMedDisp;
			double radius = (high + low) / 4;

			//skips this step
			if (only_maximize_variableAngle2) {
				med = bestAngle1;
				radius = 0;
			}

			//binary search-ish algorithm to find maximum
			//this works because the function is increasing/flat until the maximum, then decreasing/flat after
			while (radius > Math.toRadians(.05)) {
				//System.out.println("Med: " + Math.toDegrees(med));
				lowMed = med - radius;
				highMed = med + radius;
				lowMedDisp = calcDisp(lowMed);
				highMedDisp = calcDisp(highMed);
				//System.out.println("High Med Disp: " + highMedDisp);
				//System.out.println("Med Disp: " + medDisp);
				//System.out.println("Low Med Disp: " + lowMedDisp);
				//System.out.println();
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
			setCapThrowHoldingAngles((ComplexVector) variableCapThrow1Vector, once_bestAngle1, p.twoPlayerMode ? OPTIMAL_ANGLE_DIFF : Math.toRadians(diveCapBounceAngle), Math.toRadians(vectorAngle), variableCapThrow1Frames, variableCapThrow1FallingFrames);
			variableCapThrow1Vector.calcDisp();
			if (hasVariableCapThrow1Falling)
				calcFallingDisplacements(variableCapThrow1Vector, variableCapThrow1Index, once_bestAngle1Adjusted, !variableCapThrow1VectorRight, optimizeCT1Falling, roughOptimizeCT1Falling);
			//recalculate variable cap throw or movement 2 for the best angle 1
			if (hasVariableCapThrow2 || hasVariableOtherMovement2) {
				maximize(MAX_VA1 + 1);
			}	
		}
		//if we didn't have variableCapThrow1 but we do have a second variable movement (i.e. before the final dive)
		else if (hasVariableCapThrow2 || hasVariableOtherMovement2) {
			maximize(MAX_VA1 + 1);
			once_bestAngle2 = variableAngle2;
			once_bestAngle2Adjusted = variableAngle2Adjusted;
			once_bestDispZ = testDispZ2;
			once_bestDispX = testDispX2;
		}
		
		//if there was a variable 2nd movement, we need to calculate a motion group 3 consisting of the ground pound and dive after it, and possibly reverse bonk
		if (hasVariableCapThrow2 || hasVariableOtherMovement2) {	
			Movement groundPound = new Movement("Ground Pound");
			SimpleMotion gpMotion = groundPound.getMotion(movementFrames.get(motions.length - (p.reverseBonk ? 3 : 2)), false, false);
			gpMotion.setInitialAngle(once_bestAngle2Adjusted);
			motions[motions.length - (p.reverseBonk ? 3 : 2)] = gpMotion;
			
			Movement dive = new Movement("Dive");
			SimpleMotion diveMotion = dive.getMotion(movementFrames.get(motions.length - (p.reverseBonk ? 2 : 1)), false, false);
			diveMotion.setInitialAngle(once_bestAngle2Adjusted);
			motions[motions.length - (p.reverseBonk ? 2 : 1)] = diveMotion;
			diveMotion.calcDispDispCoordsAngleSpeed();

			once_bestDispZ += diveMotion.dispZ;
			once_bestDispX += diveMotion.dispX;

			if (p.reverseBonk) {
				Movement reverseBonk = new Movement("Reverse Bonk");
				SimpleMotion reverseBonkMotion = reverseBonk.getMotion(movementFrames.get(motions.length - 1), false, false);
				reverseBonkMotion.setInitialAngle(once_bestAngle2Adjusted + (p.rightVector ? -1 : 1) * Math.toRadians(p.reverseBonkAngle));
				reverseBonkMotion.calcDispDispCoordsAngleSpeed();
				motions[motions.length - 1] = reverseBonkMotion;

				once_bestDispZ += reverseBonkMotion.dispZ;
				once_bestDispX += reverseBonkMotion.dispX;
			}
		}
		
		once_bestDisp = Math.sqrt(once_bestDispZ * once_bestDispZ + once_bestDispX * once_bestDispX);

		return once_bestDisp;
	}

	private void calcMotionGroup2() {
		motionGroup2 = calcMotionGroup(motionGroup2Index, variableMovement2Index, 0, 0); //last velocity does not currently matter as there is a ground pound then dive
		Debug.println("Motion group 2 calculation:");
		calcAll(motionGroup2);
		dispMotionGroup2 = disp;
		
		//Debug.println("Motion group 2 final movement: " + motionGroup2[motionGroup2.length - 1].movement.movementType);
		motionGroup2Angle = angle - Math.PI / 2;
		motionGroup2FinalAngle = motionGroup2[motionGroup2.length - 1].finalAngle - Math.PI / 2;
		
		motionGroup2FinalRotation = calcFinalRotation(motionGroup2, false);
	}

	private double calcDisp(double variableAngle1) {
		setCapThrowHoldingAngles((ComplexVector) variableCapThrow1Vector, variableAngle1, p.twoPlayerMode ? OPTIMAL_ANGLE_DIFF : Math.toRadians(diveCapBounceAngle), Math.toRadians(vectorAngle), variableCapThrow1Frames, variableCapThrow1FallingFrames);

		variableCapThrow1Vector.calcDisp();
		variableCapThrow1Vector.calcDispCoords();
		
		double variableCapThrow1DispZ = variableCapThrow1Vector.dispZ;
		double variableCapThrow1DispX = variableCapThrow1Vector.dispX;
		
		//adjust the angles so we can see how much displacement has occurred
		double motionGroup2AdjustedAngle;
		//System.out.println("Motion Group 1 Final Angle: " + Math.toDegrees(motionGroup1FinalAngle));
		variableAngle1Adjusted = motionGroup1FinalAngle + (motionGroup2VectorRight ? 1 : -1) * variableAngle1;
		motionGroup2AdjustedAngle = variableAngle1Adjusted + motionGroup2Angle;
		
		//System.out.println("Motion group 2 adjusted angle: " + Math.toDegrees(motionGroup2AdjustedAngle));

		//if the cap throw is long enough, there's falling afterward
		if (hasVariableCapThrow1Falling) {
			double[] fallingDisplacements = calcFallingDisplacements(variableCapThrow1Vector, variableCapThrow1Index, variableAngle1Adjusted, !variableCapThrow1VectorRight, optimizeCT1Falling, roughOptimizeCT1Falling);
			variableCapThrow1DispZ += fallingDisplacements[0];
			variableCapThrow1DispX += fallingDisplacements[1];
		}
		
		// Debug.println(Math.toDegrees(motions[variableCapThrow1Index].finalAngle));
		
		// Debug.println(variableCapThrow1DispZ);
		// Debug.println(variableCapThrow1DispX);
		// Debug.println(dispMotionGroup2 * Math.cos(motionGroup2AdjustedAngle));
		// Debug.println(dispMotionGroup2 * Math.sin(motionGroup2AdjustedAngle));
		
		// Debug.println(Math.toDegrees(variableAngle1Adjusted));
		
		//sum the displacements so far
		testDispZ1 = dispZMotionGroup1 + variableCapThrow1DispZ + dispMotionGroup2 * Math.cos(motionGroup2AdjustedAngle);
		testDispX1 = dispXMotionGroup1 + variableCapThrow1DispX + dispMotionGroup2 * Math.sin(motionGroup2AdjustedAngle);
		
		return maximize(MAX_VA1 + 1);
		
		//Debug.println("Angle: " + Math.toDegrees(variableAngle1));
		//Debug.println("Group 2 Angle: " + Math.toDegrees(motionGroup2AdjustedAngle));
		//Debug.println("Displacement x, y: " + testDispZ1 + ", " + testDispX1);
		//Debug.println("Variable 1 displacement x, y: " + motions[variableCapThrow1Index].dispZ + ", " + motions[variableCapThrow1Index].dispX);
		//Debug.println("Group 2 displacement x, y: " + dispMotionGroup2 * Math.cos(motionGroup2AdjustedAngle) + ", " + dispMotionGroup2 * Math.sin(motionGroup2AdjustedAngle));
	}

	private double maximize_variableAngle2() {
		//recalculate variable cap throw or movement 2 for the best angle 1
		if (hasVariableCapThrow1) {
			//calcMotionGroup2(); //TODO only recalculate this sometimes

			if (hasVariableCapThrow2 || hasVariableOtherMovement2) {
				double motionGroup2AdjustedFinalAngle = variableAngle1Adjusted + motionGroup2FinalAngle;
				double motionGroup2FinalRotationAdjusted = motionGroup2FinalRotation + once_bestAngle1Adjusted - Math.PI / 2;
				if (hasVariableOtherMovement2) { //use more accurate calculation //TODO see how slow this is
					SimpleMotion[] motionGroups1and2 = new SimpleMotion[variableMovement2Index];
					for (int i = 0; i < variableMovement2Index; i++)
						motionGroups1and2[i] = motions[i];
					motionGroup2FinalRotationAdjusted = calcFinalRotation(motionGroups1and2, true);
				}
				
				//Debug.println("while optimizing mg2 final rotation adjusted: " + Math.toDegrees(motionGroup2FinalRotationAdjusted));
				//Debug.println("the final angle adjusted: " + Math.toDegrees(motionGroup2FinalRotationAdjusted));
				//Debug.println(Math.toDegrees(variableAngle2) + ": " + Math.toDegrees(variableAngle2Adjusted));
				//Debug.println("Testing 1st angle: " + Math.toDegrees(variableAngle1));
				if (findVariableAngle2(motionGroup2, motionGroup2AdjustedFinalAngle, motionGroup2FinalRotationAdjusted, testDispZ1, testDispX1)) {
					//if we're able to find a variable angle 2
					double testDisp = Math.sqrt(testDispZ2 * testDispZ2 + testDispX2 * testDispX2);
					//Debug.println("Test disp this calcDisp(): " + testDisp);
					return testDisp;
				}
				return 0;
			}
			else { //if there isn't one we just compare this choice of variableAngle1 to the ones we've tried before
				double testDisp = Math.sqrt(testDispZ1 * testDispZ1 + testDispX1 * testDispX1);
				// Debug.println("Test Disp X1: " + testDispZ1);
				// Debug.println("Test Disp Y1: " + testDispX1);
				return testDisp;
			}
		}
		//if we didn't have variableCapThrow1 but we do have a second variable movement (i.e. before the final dive)
		else if (hasVariableCapThrow2 || hasVariableOtherMovement2) {
			double motionGroup1FinalRotation = calcFinalRotation(motionGroup1, true);
			Debug.println("Rotation before variable movement 2:" + Math.toDegrees(motionGroup1FinalRotation));
			findVariableAngle2(motionGroup1, motionGroup1FinalAngle, motionGroup1FinalRotation, dispZMotionGroup1, dispXMotionGroup1);
			return Math.sqrt(testDispZ2 * testDispZ2 + testDispX2 * testDispX2);
		}
		else { //TODO is this right?
			return Math.sqrt(testDispZ1 * testDispZ1 + testDispX1 * testDispX1);
		}
	}
	
	private boolean findVariableAngle2(SimpleMotion[] motionGroup, double initialAngle, double initialRotation, double previousDispZ, double previousDispX) {
		double initialForwardVelocity;
		if (variableMovement2Index - 1 >= 0)
			initialForwardVelocity = motions[variableMovement2Index - 1].finalSpeed;
		else
			initialForwardVelocity = p.initialHorizontalSpeed;
		
		Movement variableMovement2 = new Movement(movementNames.get(variableMovement2Index), initialForwardVelocity, canRocketFlower(variableMovement2Index) ? p.rocketFlower : false);
		//this boolean signifies whether to rotate during the fall component of a final cap throw
		optimizeFCTFalling = p.optimizeFCTFalling && p.turnarounds && hasVariableCapThrow2 && hasVariableMovement2Falling && movementFrames.get(variableMovement2Index + 1) >= 6; //TODO 6 might not always work, but it really seems like it does
		variableMovement2Vector = (SimpleVector) variableMovement2.getMotion(movementFrames.get(variableMovement2Index), currentVectorRight, !optimizeFCTFalling);
		motions[variableMovement2Index] = variableMovement2Vector;
		variableMovement2Vector.setInitialAngle(initialAngle); 

		variableMovement2Frames = movementFrames.get(variableMovement2Index);
		variableMovement2FallingFrames = 0;
		if (hasVariableMovement2Falling) {
			variableMovement2FallingFrames = movementFrames.get(variableMovement2Index + 1);
		}
		
		//binary search to find variableAngle2
		double low = 0;
		double high = Math.PI / 4;
		variableAngle2 = Math.PI / 8;
		
		// Debug.println("Finding Variable Angle 2");
		// Debug.println("Initial Angle:" + Math.toDegrees(initialAngle));
		
		while(high - low > .00001) {
			if (optimizeFCTFalling) { //yank does not appear to be effective, neither is holding back
				// double[] holdingAngles = new double[24];
				// for (int i = 0; i <= 22; i++)
				// 	holdingAngles[i] = SimpleMotion.NORMAL_ANGLE;
				// holdingAngles[23] = Math.toRadians(p.debugValue);
				// ((ComplexVector) variableMovement2Vector).setHoldingAngles(holdingAngles);
				variableMovement2Vector.setHoldingAngle(SimpleMotion.NORMAL_ANGLE);
			}
			else if (hasVariableCapThrow2) {
				setCapThrowHoldingAngles((ComplexVector) variableMovement2Vector, variableAngle2, p.customFCTAngle ? Math.toRadians(p.fctAngle) : OPTIMAL_ANGLE_DIFF, SimpleMotion.NORMAL_ANGLE, variableMovement2Frames, variableMovement2FallingFrames);
			}
			else
				setOtherMovementHoldingAngles((ComplexVector) variableMovement2Vector, variableMovement2Index, variableAngle2, initialAngle, initialRotation, currentVectorRight);
			variableMovement2Vector.calcDisp();
			variableMovement2Vector.calcDispCoords();
			
			double variableMovement2DispZ = variableMovement2Vector.dispZ;
			double variableMovement2DispX = variableMovement2Vector.dispX;
			
			variableAngle2Adjusted = initialAngle - (currentVectorRight ? 1 : -1) * variableAngle2; //the absolute direction we're throwing in/trying to go in

			if (hasVariableMovement2Falling) {
				double[] fallingDisplacements = calcFallingDisplacements(variableMovement2Vector, variableMovement2Index, variableAngle2Adjusted, !currentVectorRight, optimizeFCTFalling, roughOptimizeFCTFalling);
				variableMovement2DispZ += fallingDisplacements[0];
				variableMovement2DispX += fallingDisplacements[1];
			}
			
			// testDispZ2 = 0;
			// testDispX2 = 0;
			// for (int i = 0; i < motions.length; i++) {
			// 	if (i <= variableMovement2Index || (hasVariableMovement2Falling && i <= variableMovement2Index + 1)) {
			// 		testDispZ2 += motions[i].dispZ;
			// 		testDispX2 += motions[i].dispX;
			// 	}
			// 	//System.out.printf("%s: %.3f %.3f %.3f\n", movementNames.get(i), motions[i].dispX, motions[i].dispZ, totalDispX);
			// }
			testDispZ2 = previousDispZ + variableMovement2DispZ;
			testDispX2 = previousDispX + variableMovement2DispX;
			double testDispAngle2 = Math.atan2(testDispX2, testDispZ2); //angle of the displacement of the 2nd variable movement
			if (testDispAngle2 <= 0)
			 	testDispAngle2 += Math.PI;		
			
			// Debug.println("High:" + Math.toDegrees(high));
			// Debug.println("Low:" + Math.toDegrees(low));
			// Debug.println("Test angle:" + Math.toDegrees(variableAngle2Adjusted));
			// Debug.println("Disp angle:" + Math.toDegrees(testDispAngle2));

			Debug.println("Variable angle 2 adjusted: " + Math.toDegrees(variableAngle2Adjusted));
			Debug.println("Test disp angle 2: " + Math.toDegrees(testDispAngle2));

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
		Debug.println("Failed");
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
				falling.setInitialForwardVelocity(ct.calcFinalSpeed());
				falling.setInitialAngle(ct.finalAngle);
				//bestAngle1Adjusted = ct.finalAngle + (motionGroup2VectorRight ? 1 : -1) * bestAngle1;
				//Debug.println("CT final angle: " + Math.toDegrees(ct.finalAngle));
				//Debug.println("Holding angle: " + Math.toDegrees(Math.abs(ct.finalAngle - bestAngle1Adjusted)));
				//falling.setHoldingAngle((ct.rightVector) ? -1 : 1) * (ct.finalAngle - bestAngle1Adjusted));
				if (!optimizeCT1Falling)
					falling.setHoldingAngle((falling.rightVector ? 1 : -1) * (ct.finalAngle - bestAngle1Adjusted));
				else {
					double ctFinalRotation;
					if (roughCTRotations)
						ctFinalRotation = ct.initialAngle + (ct.rightVector ? -Math.toRadians(vectorAngle) : Math.toRadians(vectorAngle)); //TODO this doesn't seem perfect
					else
						ctFinalRotation = ct.calcFinalRotation();
					double fallingInitialAngle = ct.finalAngle;
					boolean vectorRight = !ct.rightVector;
					double ctTargetRotation = vectorRight ? fallingInitialAngle - bestAngle1Adjusted : bestAngle1Adjusted - fallingInitialAngle;
					setOtherMovementHoldingAngles((ComplexVector) falling, variableCapThrow1Index + 1, ctTargetRotation, fallingInitialAngle, ctFinalRotation, vectorRight);
				}
				//falling.setHoldingAngle(Math.abs(ct.finalAngle - bestAngle1Adjusted));
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
	public double vectorAngleMin = 45; double vectorAngleMax = 90;
	//public double firstFrameDecelIncrement = 0.005;
	public double vectorAngleIncrement = 1;
	public double edgeCBAngleIncrement = 0.01;
	//public int edgeCBSteps = 30 * 101;
	//sees if the dive will actually bounce on cappy in the requested number of frames
	//throwType is the type of throw to check (set to -1 to check all throw types that satisfy the booleans afterward)
	//allowButtonST = can check for regular single throws
	//allowSideThrow = can check for MCCT/TT right and left (have not been found to be helpful as of yet)
	//allowMCCT = can check for motion single throws
	//allowDT = can check for double throws
	//allowTT = can check for triple throws
	//diveCapBounceAngle is now one that works (also the value in Properties is this)
	//ctType is the ct that worked;
	//currently does not recalculate rest of jump to be optimal, but maybe it should
	public int isDiveCapBouncePossible(int throwType, boolean allowButtonST, boolean allowSideThrow, boolean allowMCCT, boolean allowDT, boolean allowTT) {
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
			try {
				motions[i].calcDispDispCoordsAngleSpeed();
			}
			catch (Exception ex) {
				diveCapBounceAngle = 0;
				setCapThrowHoldingAngles((ComplexVector) variableCapThrow1Vector, bestAngle1, p.twoPlayerMode ? OPTIMAL_ANGLE_DIFF : Math.toRadians(diveCapBounceAngle), Math.toRadians(vectorAngle), variableCapThrow1Frames, variableCapThrow1FallingFrames);
				variableCapThrow1Vector.setHoldingAngle(SimpleMotion.NORMAL_ANGLE);
				if (optimizeCT1Falling)
					calcFallingDisplacements(variableCapThrow1Vector, variableCapThrow1Index, bestAngle1Adjusted, !variableCapThrow1VectorRight, optimizeCT1Falling, roughOptimizeCT1Falling);
				motions[i].calcDispDispCoordsAngleSpeed();
			}
		}

		double lowAngle = -Double.MAX_VALUE;
		double highAngle = -Double.MAX_VALUE;
		int targetCBFrame = motions[preCapBounceDiveIndex].frames;
		//Debug.println("Target CB Frame: " + targetCBFrame);
		//DiveTurn dive = (DiveTurn) motions[preCapBounceDiveIndex];
		//ComplexVector capThrow = (ComplexVector) motions[variableCapThrow1Index];
		for (vectorAngle = Math.min(vectorAngleMax, 90); vectorAngle >= vectorAngleMin; vectorAngle -= vectorAngleIncrement) {
		//for (firstFrameDecel = 0; firstFrameDecel <= .5; firstFrameDecel += firstFrameDecelIncrement) {
			// if (firstFrameDecel > 0 && firstFrameDecel / .5 <= .1) { //can't hold back this shallow
			// 	continue;
			// }
			// dive.firstFrameDecel = firstFrameDecel;
			for (int ct = 0; ct < Movement.CT_COUNT; ct++) {
				// Debug.println("Testing throw type " + ct);
				if (throwType != -1 && ct != throwType) {
					continue;
				}
				else if ((!allowButtonST || variableCapThrow1Frames < 9) && ct == Movement.CT) {
					continue;
				}
				else if (!allowSideThrow && (ct == Movement.MCCTL || ct == Movement.MCCTR || ct == Movement.TTL || ct == Movement.TTR)) {
					continue;
				}
				else if ((!allowMCCT || variableCapThrow1Frames < 8) && (ct == Movement.MCCTU || ct == Movement.MCCTD || ct == Movement.MCCTL || ct == Movement.MCCTR)) {
					continue;
				}
				else if ((!allowDT || variableCapThrow1Frames < 8) && ct == Movement.DT) {
					continue;
				}
				else if (!allowTT && Movement.isTT(ct)) {
					continue;
				}

				variableCapThrow1Vector.minFrames = Movement.isTT(ct) ? 3 : 8;
				
				boolean found = false;
				boolean overshot = false;
				for (double edgeCB = edgeCBMin; edgeCB <= edgeCBMax; edgeCB += edgeCBAngleIncrement) {
					diveCapBounceAngle = edgeCB;
					if (variableCapThrow1Frames <= 14 && edgeCB > 20) { //these cannot be turned as much without developing another method of turning
						break;
					}
					boolean possibleAngle = setCapThrowHoldingAngles((ComplexVector) variableCapThrow1Vector, bestAngle1, p.twoPlayerMode ? OPTIMAL_ANGLE_DIFF : Math.toRadians(diveCapBounceAngle), Math.toRadians(vectorAngle), variableCapThrow1Frames, variableCapThrow1FallingFrames);
					if (!possibleAngle) //TODO possible angle for the falling
						continue;

					int cbFrame = getCapBounceFrame(ct);
					//Debug.printf("%.3f° %df\n", diveCapBounceAngle, cbFrame);
					if (cbFrame == targetCBFrame) {
						if (!found || overshot) {
							found = true;
							overshot = false;
							lowAngle = diveCapBounceAngle;
						}
						if (!overshot) {
							highAngle = diveCapBounceAngle;
						}
						ctType = ct;
					}
					else if (highAngle != -Double.MAX_VALUE)
						overshot = true;
				}
				if (found && highAngle >= lowAngle + p.diveCapBounceTolerance) { //too high of a risk it won't actually work in game if they are the same
					//Debug.println("Decel: " + firstFrameDecel);
					Debug.println("Found low: " + lowAngle);
					Debug.println("Found high: " + highAngle);
					if (lowAngle == 0 && highAngle <= 5.5) { //safer to pick 0
						diveCapBounceAngle = 0;
					}
					else if (highAngle - lowAngle < 2) { //if high and low angles are close pick the middle for most reliable result
						diveCapBounceAngle = (highAngle + lowAngle) / 2;
					}
					else { //otherwise pick an angle close to the high for a better vector
						diveCapBounceAngle = highAngle - 1;
					}
					p.diveCapBounceAngle = diveCapBounceAngle;
					//p.diveFirstFrameDecel = firstFrameDecel;
					p.vectorAngle = vectorAngle;
					Debug.println(p.diveCapBounceAngle);
					setCapThrowHoldingAngles((ComplexVector) variableCapThrow1Vector, bestAngle1, p.twoPlayerMode ? OPTIMAL_ANGLE_DIFF : Math.toRadians(diveCapBounceAngle), Math.toRadians(vectorAngle), variableCapThrow1Frames, variableCapThrow1FallingFrames);
					if (optimizeCT1Falling)
						calcFallingDisplacements(variableCapThrow1Vector, variableCapThrow1Index, bestAngle1Adjusted, !variableCapThrow1VectorRight, optimizeCT1Falling, roughOptimizeCT1Falling);
					getCapBounceFrame(ct); //run again to adjust the falling vector to be correct
					return ctType;
				}
			}
		}
		return -1;
	}

	//recalculates displacement after calling isDiveCapBouncePossible()
	public void recalculateDisps(boolean setBestDisp) {
		double newDisp = 0;
		only_maximize_variableAngle2 = true;
		if (setBestDisp) {
			newDisp = maximize();
		}
		only_maximize_variableAngle2 = false;

		if (newDisp == 0) {
			return;
		}

		//now recalculate the displacement of the full jump with the new cap throw angle, dive decel, etc.
		//so that it can later be adjusted to the correct angle with a call to adjustToGivenAngle()
		for (int i = variableCapThrow1Index; i < motions.length; i++) {
			if ((i == variableCapThrow1Index + 1 || i == variableCapThrow1Index + 2) && motions[i].movement.movementType.equals("Ground Pound")) {
				motions[i].setInitialAngle(bestAngle1Adjusted);
			}
			else if ((i == variableMovement2Index + 1 || i == variableMovement2Index + 2) && motions[i].movement.movementType.equals("Ground Pound")) {
				motions[i].setInitialAngle(bestAngle2Adjusted);
			}
			else if (p.reverseBonk && i == motions.length - 1) {
				motions[i].setInitialAngle(motions[i - 1].finalAngle + (p.rightVector ? -1 : 1) * Math.toRadians(p.reverseBonkAngle));
			}
			else if (i > 0) {
				motions[i].setInitialAngle(motions[i - 1].finalAngle);
			}
			motions[i].calcDispDispCoordsAngleSpeed();
		}
		sumXDisps(motions);
		sumYDisps(motions);
		if (setBestDisp) {
			bestDispX = dispX;
			bestDispZ = dispZ;
			bestDisp = Math.sqrt(bestDispX * bestDispX + bestDispZ * bestDispZ);
		}
		//maximize_variableAngle1();					
		//calcDisp(bestAngle1);
		//adjustToGivenAngle();
	}

	//adjusts the angle of everything so it is in the direction of the given target or initial angle
	//can only call this once
	public void adjustToGivenAngle() {
		if (bestDisp == 0)
			return;

		double unadjustedTargetAngle = Math.atan(bestDispX / bestDispZ);
		if (unadjustedTargetAngle < 0)
			unadjustedTargetAngle += Math.PI;
		Debug.println("Unadjusted target angle:" + Math.toDegrees(unadjustedTargetAngle));
		if (!p.initialAndTargetGiven && (p.targetAngleGiven || p.targetCoordinatesGiven)) { //if we were just given a target angle or target coordinates, shift so motion is moving in that direction
			angleAdjustment = targetAngle - unadjustedTargetAngle;
			initialAngle = Math.PI / 2 + angleAdjustment;
		}
		else {
			angleAdjustment = initialAngle - Math.PI / 2;
			if (rightVector) {
				angleAdjustment -= rcTrueInitialAngleDiff;
			}
			else {
				angleAdjustment += rcTrueInitialAngleDiff;
			}
			targetAngle = unadjustedTargetAngle + angleAdjustment;
		}
		initialRotation += angleAdjustment;
		for (int i = 0; i < motions.length; i++) {
			motions[i].adjustInitialAngle(angleAdjustment);
			motions[i].adjustInitialRotation(angleAdjustment);
		}
		if (initialAngle < 0)
			initialAngle += 2 * Math.PI;
		if (targetAngle < 0)
			targetAngle += 2 * Math.PI;
		Debug.println("Initial angle:" + Math.toDegrees(initialAngle));
		Debug.println("Target angle:" + Math.toDegrees(targetAngle));
	}

	public double[] getFinalCapThrowPosition() {
		if (hasVariableCapThrow2)
			return ((ComplexVector) motions[variableMovement2Index]).getCappyPosition(p.fctType);
		else
			return null;
	}

	public void calcYDisps() { //calculates Y disps of every motion
		calcMotionGroup(0, movementNames.size(), p.initialHorizontalSpeed, p.framesJump);
		for (int i = 0; i < motions.length; i++) {
			if (motions[i].movement.movementType.equals("Falling") && i > 0)
				motions[i].movement.initialVerticalSpeed = motions[i - 1].calcFinalVerticalVelocity();
			motions[i].calcDispY();
		}
	}
}
