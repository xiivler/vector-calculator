package vectorCalc;

import java.util.ArrayList;
import java.util.Vector;

import javax.swing.table.TableModel;

import vectorCalc.VectorCalculator.AngleType;

public class VectorMaximizer {

	public static final double RCV_ERROR = .001; //acceptable Z axis error when trying to make a RCV go straight
    public static final int RCV_MAX_ITERATIONS = 100; //stop after this many iterations no matter what when trying to make a RCV go straight

	public static final double FAST_TURNAROUND_VELOCITY = Math.toRadians(25);
	public static final double FAST_TURNAROUND_ACCEL = Math.toRadians(2.5);
	public static final double FAST_TURNAROUND_ANGLE = Math.toRadians(135.5); //only needs to be 135, but extra .5 degrees for safety

	public static final double TURN_COUNTERROTATION = Math.toRadians(.4); //really should be .3 but this produces inaccurate results
	public static final double TRUE_TURN_COUNTERROTATION = Math.toRadians(.3);

	//currently calculates tenths of degrees, and maybe loses a hundredth of a unit over calculating to the thousandth
	//public static int numSteps = 901;
			
	SimpleVector[] vectors;
	double[] angles;
	SimpleMotion[] motions;
	int[] frames;
	
	double dispZ;
	double dispX;
	double disp;
	double angle;
	
	double givenAngle;
	boolean targetAngleGiven;
	double initialAngle;
	double targetAngle;
	
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
	
	int variableCapThrow1Index;
	int variableMovement2Index;
	int motionGroup2Index;
	int variableHCTFallIndex;
	//int motionGroup3Index;

	ComplexVector variableCapThrow1Vector;
	int variableCapThrow1Frames;
	double motionGroup1FinalAngle;
	boolean variableCapThrow1VectorRight;

	boolean motionGroup2VectorRight;
	double motionGroup2Angle;
	double motionGroup2FinalAngle;
	double motionGroup2FinalRotation;

	double dispZMotionGroup1;
	double dispYMotionGroup1;
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

	SimpleMotion[] motionGroup2 = null;
	
	ArrayList<String> movementNames;
	ArrayList<Integer> movementFrames;
	MovementNameListPreparer listPreparer;

	static double[] fastTurnarounds = {Math.toRadians(85), Math.toRadians(75), Math.toRadians(72.5), Math.toRadians(67.5), Math.toRadians(50), Math.toRadians(47.5), Math.toRadians(25), 0};
	static int[] fastTurnaroundFrames = {4, 3, 3, 3, 2, 2, 1, 0};
	static int[] maxVelocityFastTurnaroundFrames = {1, 3, 2, 1, 2, 1, 1, 0}; //how many frames you're rotating at 25 deg/fr
	
	static double[] homingMotionThrowHoldingAngles;
	
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
		
		//TableModel genPropertiesModel = VectorCalculator.genPropertiesModel;
		targetAngleGiven = VectorCalculator.angleType == AngleType.TARGET; //keep this logic; if it's both, you want to conform to the initial
		if (VectorCalculator.xAxisZeroDegrees) { //vector calculator cacluates as if the order is ZXY (i.e. 0 degrees is the positive Z axis, and 90 degrees is the positive X axis)
			initialAngle = Math.PI / 2 - Math.toRadians(VectorCalculator.initialAngle);
			targetAngle = Math.PI / 2 - Math.toRadians(VectorCalculator.targetAngle);
		}
		else {
			initialAngle = Math.toRadians(VectorCalculator.initialAngle);
			targetAngle = Math.toRadians(VectorCalculator.targetAngle);
		}
		//givenAngle = Math.toRadians(Double.parseDouble(genPropertiesModel.getValueAt(VectorCalculator.ANGLE_ROW, 1).toString()));
		//targetAngleGiven = genPropertiesModel.getValueAt(VectorCalculator.ANGLE_TYPE_ROW, 1).toString().equals("Target Angle");
		rightVector = VectorCalculator.rightVector;
		
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
		}
		
		motions = new SimpleMotion[movementNames.size()];
		
		Debug.println("Variable cap throw 1: " + hasVariableCapThrow1);
		Debug.println("Variable cap throw 2: " + hasVariableCapThrow2);
		Debug.println("Variable other movement 2: " + hasVariableOtherMovement2);
		Debug.println("Indices: " + variableCapThrow1Index + ", " + motionGroup2Index + ", " + variableMovement2Index);
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
	
	private void setCapThrowHoldingAngles(ComplexVector motion, double angle, int frames) {
		double[] holdingAngles = new double[frames];
		holdingAngles[0] = angle;
		if (VectorCalculator.hyperoptimize || frames <= 8) {
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
			else if (frames == 9) {
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
			else {
				double minRotation = motion.rotationalAccel * (frames - 2); //first frame sets the cap throw angle, last frame is a fast turnaround
				//Debug.println("Min Rotation: " + Math.toDegrees(minRotation));
				double additionalRotation = FAST_TURNAROUND_VELOCITY - minRotation;
				double rotationSum = 0;
				double rotationalVelocity = 0;
				int additionalRotationFrames = 0;
				while (rotationSum < additionalRotation) {
					rotationalVelocity += motion.rotationalAccel;
					rotationSum += rotationalVelocity;
					additionalRotationFrames++;
				}
				double overshoot = rotationSum - additionalRotation;
				//how much counterrotation there should be on the first frame of acceleration
				double firstAdditionalRotationFrameCounterrotation = overshoot / additionalRotationFrames;
				int firstAdditionalRotationFrame = frames - 1 - additionalRotationFrames;
				holdingAngles[1] = SimpleMotion.NORMAL_ANGLE - Math.toRadians(1.5); //shifting by 1.5 degrees makes fast turnarounds not reverse the wrong way
				for (int i = 2; i < firstAdditionalRotationFrame; i++) {
					holdingAngles[i] = holdingAngles[i - 1] - TURN_COUNTERROTATION;
				}
				holdingAngles[firstAdditionalRotationFrame] = holdingAngles[firstAdditionalRotationFrame - 1] - firstAdditionalRotationFrameCounterrotation;
				for (int i = firstAdditionalRotationFrame + 1; i < frames - 1; i++) {
					holdingAngles[i] = holdingAngles[i - 1];
				}
				holdingAngles[frames - 1] = angle + FAST_TURNAROUND_VELOCITY - FAST_TURNAROUND_ANGLE;
				boolean[] holdingMinRadius = new boolean[frames];
				holdingMinRadius[frames - 1] = true;
				motion.setHolding(holdingAngles, holdingMinRadius);
				return;
			}
			motion.setHoldingAngles(holdingAngles);
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
			motionGroup[0] = initialMovement.getMotion(movementFrames.get(startIndex), currentVectorRight, false);
			motionGroup[0].setInitialAngle(Math.PI / 2);
			motionGroup[0].calcDispDispCoordsAngleSpeed();
			if (!motionGroup[0].getClass().getSimpleName().equals("SimpleMotion"))
				currentVectorRight = !currentVectorRight;
		}

		for (int i = nextIndex; i < motionGroup.length; i++) {
			int j = i + startIndex;
			Movement currentMovement = new Movement(movementNames.get(j), motionGroup[i - 1].finalSpeed);
			if (movementNames.get(j).equals("Homing Motion Cap Throw")) {			
				motionGroup[i] = currentMovement.getMotion(movementFrames.get(j), currentVectorRight, true);
				((ComplexVector) motionGroup[i]).setHoldingAngles(homingMotionThrowHoldingAngles);
			}
			else
				motionGroup[i] = currentMovement.getMotion(movementFrames.get(j), currentVectorRight, false);
			if (hasVariableHCTFallVector && j == variableHCTFallIndex) { //use the holding angle we are testing this iteration for optimizing the HCT fall
				((SimpleVector) motionGroup[i]).setHoldingAngle(variableHCTHoldingAngle);
				if (!switchHCTFallVectorDir) {
					currentVectorRight = !currentVectorRight;
				}
				Debug.println("HCT Optimize Branch Activated!");
			}
			motionGroup[i].setInitialAngle(motionGroup[i - 1].finalAngle);
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
		if (VectorCalculator.xAxisZeroDegrees) {
			return Math.PI / 2 - initialAngle;
		}
		else {
			return initialAngle;
		}
	}
	
	public double getTargetAngle() {
		if (VectorCalculator.xAxisZeroDegrees) {
			return Math.PI / 2 - targetAngle;
		}
		else {
			return targetAngle;
		}
	}
	
	//the actual maximization function
	public void maximize() {
		long startTime = System.currentTimeMillis();
		
		//SimpleMotion[] motionGroup1 = new SimpleMotion[variableCapThrow1Index];
		//SimpleMotion[] motionGroup2 = new SimpleMotion[variableMovement2Index - motionGroup2Index];
		//int motionGroup3Index;
		
		//currentVectorRight = rightVector;

		if (VectorCalculator.angleType == AngleType.BOTH) {
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
		if (hasVariableRollCancel) {
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
					int totalFrames = rcMotion.calcFrames(VectorCalculator.initialDispY);
					movementFrames.set(0, rc.minFrames);
					movementFrames.set(1, totalFrames - rc.minFrames);
					rcFinalAngleDiff = calcRCFinalAngleDiff(movementNames.get(0), listPreparer.initialVelocity, movementFrames.get(1));

					maximizeOnceHCT();

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
			int maxCount = 20;
			//on the first iteration just maximize it and see how far off we are
			//then keep nudging it slightly
			for (int i = 1; i <= maxCount; i++) {
				maximizeOnceHCT();
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
					increment = unadjustedTargetAngle * 2 / maxCount;
				}
				if (rightVector) {
					rcFinalAngleDiff -= increment;
				}
				else {
					rcFinalAngleDiff += increment;
				}
			}
			rcFinalAngleDiff = bestRCFinalAngleDiff;
			//maximizeOnce();

			//hopefully it's small by now; fine tune by nudging by the difference between the initial and target angles
			for (int i = 0; i < 10; i++) {
				if (Math.abs(unadjustedTargetAngle) < Math.toRadians(0.00005)) {
					break;
				}

				maximizeOnceHCT();

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
			maximizeOnceHCT();
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
		Debug.println("Calculated in " + (System.currentTimeMillis() - startTime) + " ms");

		
		//adjusting motions to the optimized values
		if (hasVariableCapThrow1) {
			//((ComplexVector) motions[variableCapThrow1Index].set
			double adjustment = bestAngle1Adjusted - Math.PI / 2;
			for (int i = 0; i < motionGroup2.length; i++)
				motionGroup2[i].adjustInitialAngle(adjustment);
		}
		
		//rotating motions to the right angle
		
		double unadjustedTargetAngle = Math.atan(bestDispX / bestDispZ);
		if (unadjustedTargetAngle < 0)
			unadjustedTargetAngle += Math.PI;
		Debug.println("Unadjusted target angle:" + Math.toDegrees(unadjustedTargetAngle));
		double adjustment;
		if (targetAngleGiven) {
			//adjustment = givenAngle - unadjustedTargetAngle;
			adjustment = targetAngle - unadjustedTargetAngle;
			initialAngle = Math.PI / 2 + adjustment;
			//targetAngle = givenAngle;
		}
		else {
			Debug.println("hi");
			//adjustment = givenAngle - Math.PI / 2;
			adjustment = initialAngle - Math.PI / 2;
			if (rightVector) {
				adjustment -= rcTrueInitialAngleDiff;
			}
			else {
				adjustment += rcTrueInitialAngleDiff;
			}
			//initialAngle = givenAngle;
			targetAngle = unadjustedTargetAngle + adjustment;
		}
		for (int i = 0; i < motions.length; i++) {
			motions[i].adjustInitialAngle(adjustment);
			if (motions[i].movement.movementType.equals("Falling") && i > 0)
				motions[i].movement.initialVerticalSpeed = motions[i - 1].calcFinalVerticalVelocity();
			Debug.println(motions[i].movement.movementType + " motion angle:" + Math.toDegrees(motions[i].initialAngle));
		}
		if (initialAngle < 0)
			initialAngle += 2 * Math.PI;
		if (targetAngle < 0)
			targetAngle += 2 * Math.PI;
		Debug.println("Initial angle:" + Math.toDegrees(initialAngle));
		Debug.println("Target angle:" + Math.toDegrees(targetAngle));
		
	}
	
	private double[] calcFallingDisplacements(ComplexVector variableCapThrowVector, int variableCapThrowIndex, double variableAngleAdjusted, boolean vectorRight) {
		double[] displacements = new double[2];
		variableCapThrowVector.calcFinalAngle();
		Movement variableCapThrowFalling = new Movement("Falling", variableCapThrowVector.calcFinalSpeed());
		SimpleVector variableCapThrowFallingVector = (SimpleVector) variableCapThrowFalling.getMotion(movementFrames.get(variableCapThrowIndex + 1), vectorRight, false);
		motions[variableCapThrowIndex + 1] = variableCapThrowFallingVector;
		variableCapThrowFallingVector.setOptimalForwardAccel(false); //not trying to be optimal, simply trying to end up in the right direction
		variableCapThrowFallingVector.setInitialAngle(variableCapThrowVector.finalAngle);
		variableCapThrowFallingVector.setHoldingAngle(Math.abs(variableCapThrowVector.finalAngle - variableAngleAdjusted));
		variableCapThrowFallingVector.calcDisp();
		variableCapThrowFallingVector.calcDispCoords();
		displacements[0] = variableCapThrowFallingVector.dispZ;
		displacements[1] = variableCapThrowFallingVector.dispX;
		return displacements;
	}

	//runs maximizeOnce() to find optimal variable angles 1 and 2 for different choices of holding angle for a HCT fall vector
	private void maximizeOnceHCT() {
		if (hasVariableHCTFallVector) {
			bestDisp = 0;
			double bestVariableHCTHoldingAngle = 0;

			int numSteps = 20; //do binary search instead?
			for (int i = -numSteps / 2; i <= numSteps / 2; i++) {
				variableHCTHoldingAngle = Math.PI / 2 / numSteps * i; //range from -90 degrees to 90 degrees
				if (variableHCTHoldingAngle < 0) {
					switchHCTFallVectorDir = true;
					variableHCTHoldingAngle = -variableHCTHoldingAngle;
				}
				else {
					switchHCTFallVectorDir = false;
				}
				Debug.println("Testing HCT fall holding angle " + Math.toDegrees(variableHCTHoldingAngle));

				maximizeOnce();

				if (once_bestDisp > bestDisp) {
					bestDisp = once_bestDisp;
					bestVariableHCTHoldingAngle = variableHCTHoldingAngle;
					bestSwitchHCTFallVectorDir = switchHCTFallVectorDir;
				}
			}

			variableHCTHoldingAngle = bestVariableHCTHoldingAngle;
			switchHCTFallVectorDir = bestSwitchHCTFallVectorDir;
			maximizeOnce();
		}
		else {
			maximizeOnce();
		}
	}

	//one iteration of maximization of variable angles 1 and 2 if they exist
	private void maximizeOnce() {
		currentVectorRight = rightVector;

		//calculate the total displacement of all the movement before the first cap throw whose angle can be variable
		SimpleMotion[] motionGroup1 = calcMotionGroup(0, Math.min(variableCapThrow1Index, variableMovement2Index), listPreparer.initialVelocity, VectorCalculator.framesJump);
		sumXDisps(motionGroup1);
		sumYDisps(motionGroup1);
		dispZMotionGroup1 = dispZ;
		dispYMotionGroup1 = dispX;
		motionGroup1FinalAngle = Math.PI / 2;
		if (motionGroup1.length > 0)
			motionGroup1FinalAngle = motionGroup1[motionGroup1.length - 1].finalAngle;
		
		Debug.println("Group 1 displacement x, y: " + dispZMotionGroup1 + ", " + dispYMotionGroup1);
		//Debug.println("Group 1 displacement: " + dispMotionGroup1);
		//Debug.println("Group 1 angle: " + Math.toDegrees(angleMotionGroup1));
		
		//the holding angle for the first variable cap throw
		
		once_bestAngle1 = 0;
		once_bestAngle2 = 0;
		
		once_bestAngle1Adjusted = 0;
		once_bestAngle2Adjusted = 0;
		
		once_bestDispZ = dispZMotionGroup1;
		once_bestDispX = dispYMotionGroup1;
		
		bestDispZ1 = once_bestDispZ;
		bestDispX1 = once_bestDispX;
		
		once_bestDisp = Math.sqrt(Math.pow(dispZMotionGroup1, 2) + Math.pow(dispYMotionGroup1, 2));
		
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
			motionGroup2FinalAngle = Math.abs(motionGroup2[motionGroup2.length - 1].finalAngle - Math.PI / 2);
			motionGroup2FinalRotation = calcFinalRotation(motionGroup2);
			
			Debug.println("motion group 2 disp: " + dispMotionGroup2);
			
			//optimize the first variable cap throw
			Movement variableCapThrow1 = new Movement(movementNames.get(variableCapThrow1Index), motions[variableCapThrow1Index - 1].finalSpeed);
			variableCapThrow1Frames = movementFrames.get(variableCapThrow1Index);
			Debug.println("frames: " + variableCapThrow1Frames);
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
			setCapThrowHoldingAngles(variableCapThrow1Vector, once_bestAngle1, variableCapThrow1Frames);
			variableCapThrow1Vector.calcDisp();
			if (hasVariableCapThrow1Falling)
				calcFallingDisplacements(variableCapThrow1Vector, variableCapThrow1Index, once_bestAngle1Adjusted, !variableCapThrow1VectorRight);
			//recalculate variable cap throw or movement 2 for the best angle 1
			if (hasVariableCapThrow2 || hasVariableOtherMovement2) {
				double motionGroup2AdjustedFinalAngle = once_bestAngle1Adjusted - booleanToPlusMinus(motionGroup2VectorRight) * motionGroup2FinalAngle;
				double motionGroup2FinalRotationAdjusted = motionGroup2FinalRotation + once_bestAngle1Adjusted - Math.PI / 2;
				//Debug.println("Adjusted mg2 final rotation: " + Math.toDegrees(motionGroup2FinalRotationAdjusted));
				findVariableAngle2(motionGroup2, motionGroup2AdjustedFinalAngle, motionGroup2FinalRotationAdjusted, bestDispZ1, bestDispX1); //will make bestDispZ2 and bestDispX2 wrong
			}	
		}
		//if we didn't have variableCapThrow1 but we do have a second variable movement (i.e. before the final dive)
		else if (hasVariableCapThrow2 || hasVariableOtherMovement2) {
			double motionGroup1FinalRotation = calcFinalRotation(motionGroup1);
			Debug.println("Rotation before variable movement 2:" + Math.toDegrees(motionGroup1FinalRotation));
			findVariableAngle2(motionGroup1, motionGroup1FinalAngle, motionGroup1FinalRotation, dispZMotionGroup1, dispYMotionGroup1);
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
	}

	private double calcDisp(double variableAngle1) {
		setCapThrowHoldingAngles(variableCapThrow1Vector, variableAngle1, variableCapThrow1Frames);
				
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
			double[] fallingDisplacements = calcFallingDisplacements(variableCapThrow1Vector, variableCapThrow1Index, variableAngle1Adjusted, !variableCapThrow1VectorRight);
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
		testDispX1 = dispYMotionGroup1 + variableCapThrow1DispX + dispMotionGroup2 * Math.sin(motionGroup2AdjustedAngle);
		
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
		ComplexVector variableMovement2Vector = (ComplexVector) variableMovement2.getMotion(movementFrames.get(variableMovement2Index), currentVectorRight, true);
		motions[variableMovement2Index] = variableMovement2Vector;
		variableMovement2Vector.setInitialAngle(initialAngle); 
		
		//binary search to find variableAngle2
		double low = 0;
		double high = Math.PI / 4;
		variableAngle2 = Math.PI / 8;
		
		//variableAngle2 = Math.toRadians(11.40380859375);
		
		//Debug.println("Initial Angle:" + Math.toDegrees(initialAngle));
		
		while(high - low > .00001) {
		
			if (hasVariableCapThrow2)
				setCapThrowHoldingAngles(variableMovement2Vector, variableAngle2, movementFrames.get(variableMovement2Index));
			else
				setOtherMovementHoldingAngles(variableMovement2Vector, motionGroup, variableMovement2Index, variableAngle2, initialAngle, initialRotation, currentVectorRight);
			variableMovement2Vector.calcDisp();
			variableMovement2Vector.calcDispCoords();
			
			double variableMovement2DispZ = variableMovement2Vector.dispZ;
			double variableMovement2DispX = variableMovement2Vector.dispX;
			
			variableAngle2Adjusted = initialAngle - booleanToPlusMinus(currentVectorRight) * variableAngle2; //the absolute direction we're throwing in/trying to go in
			
			if (hasVariableMovement2Falling) {
				double[] fallingDisplacements = calcFallingDisplacements(variableMovement2Vector, variableMovement2Index, variableAngle2Adjusted, !currentVectorRight);
				variableMovement2DispZ += fallingDisplacements[0];
				variableMovement2DispX += fallingDisplacements[1];
			}
			
			testDispZ2 = previousDispZ + variableMovement2DispZ;
			testDispX2 = previousDispX + variableMovement2DispX;
			double testDispAngle2 = Math.atan(testDispX2 / testDispZ2); //angle of the displacement of the 2nd variable movement
			if (testDispAngle2 <= 0)
				testDispAngle2 += Math.PI;		
			
		//	Debug.println("High:" + Math.toDegrees(high));
		//	Debug.println("Low:" + Math.toDegrees(low));
		//	Debug.println("Test angle:" + Math.toDegrees(testFinalAngle));
		//	Debug.println("Disp angle:" + Math.toDegrees(testDispAngle));

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
				//Debug.println("angle 2 found");
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
	
}
