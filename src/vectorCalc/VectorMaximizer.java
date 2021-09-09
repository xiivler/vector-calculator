package vectorCalc;

import java.util.ArrayList;

import javax.swing.table.TableModel;

public class VectorMaximizer {

	//currently calculates tenths of degrees, and maybe loses a hundredth of a unit over calculating to the thousandth
	public static int numSteps = 901;
			
	SimpleVector[] vectors;
	double[] angles;
	SimpleMotion[] motions;
	int[] frames;
	
	double dispX;
	double dispZ;
	double disp;
	double angle;
	
	double givenAngle;
	boolean targetAngleGiven;
	double initialAngle;
	double targetAngle;
	
	boolean rightVector;
	boolean currentVectorRight;
	
	boolean hasVariableCapThrow1 = false;
	boolean hasVariableCapThrow2 = false;
	boolean hasVariableOtherMovement2 = false;
	boolean hasVariableCapThrow1Falling = false;
	boolean hasVariableMovement2Falling = false;
	
	int variableCapThrow1Index;
	int variableMovement2Index;
	int motionGroup2Index;
	//int motionGroup3Index;
	
	double variableAngle1;
	double variableAngle2;
	double testDispX1;
	double testDispY1;
	double testDispX2;
	double testDispY2;
	double variableAngle2Adjusted;
	
	ArrayList<String> movementNames;
	ArrayList<Integer> movementFrames;
	MovementNameListPreparer listPreparer;
	
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
		
		TableModel genPropertiesModel = listPreparer.genPropertiesModel;
		givenAngle = Math.toRadians(Double.parseDouble(genPropertiesModel.getValueAt(VectorCalculator.ANGLE_ROW, 1).toString()));
		targetAngleGiven = genPropertiesModel.getValueAt(VectorCalculator.ANGLE_TYPE_ROW, 1).toString().equals("Target Angle");
		rightVector = genPropertiesModel.getValueAt(VectorCalculator.VECTOR_DIRECTION_ROW, 1).toString().equals("Right");
		
		movementNames = listPreparer.movementNames;
		movementFrames = listPreparer.movementFrames;
		
		variableCapThrow1Index = movementNames.size();
		variableMovement2Index = movementNames.size();
		motionGroup2Index = movementNames.size();
		//motionGroup3Index = movementNames.size();
		
		for (int i = 0; i < movementNames.size(); i++)	
			if (movementNames.get(i).equals("Dive"))
				if (i - 2 >= 0 && movementNames.get(i - 2).contains("Throw"))
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
				else if (i - 3 >= 0 && movementNames.get(i - 3).contains("Throw") && movementNames.get(i - 2).equals("Falling")) {
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
					if (i - 3 >= 0 && movementNames.get(i - 2).equals("Falling") && (new Movement(movementNames.get(i - 3)).vectorAccel > 0)) {
						hasVariableOtherMovement2 = true;
						hasVariableMovement2Falling = true;
						variableMovement2Index = i - 3;
					}
					else if (new Movement(movementNames.get(i - 2)).vectorAccel > 0) {
						hasVariableOtherMovement2 = true;
						variableMovement2Index = i - 2;
					}
				}
		
		motions = new SimpleMotion[movementNames.size()];
		
		System.out.println("Variable cap throw 1: " + hasVariableCapThrow1);
		System.out.println("Variable cap throw 2: " + hasVariableCapThrow2);
		System.out.println("Variable other movement 2: " + hasVariableOtherMovement2);
	}
	
	private int booleanToPlusMinus(boolean b) {
		if (b)
			return 1;
		else
			return -1;
	}
	
	private void sumXDisps(SimpleMotion[] selectedMotions) {
		dispX = 0;
		for (SimpleMotion m : selectedMotions)
			dispX += m.dispX;
	}
	
	private void sumYDisps(SimpleMotion[] selectedMotions) {
		dispZ = 0;
		for (SimpleMotion m : selectedMotions)
			dispZ += m.dispZ;
	}
	
	private void calcDisp() {
		disp = Math.sqrt(Math.pow(dispX, 2) + Math.pow(dispZ, 2));
	}
	
	private void calcAngle() {
		angle = Math.atan(dispZ / dispX);
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
			System.out.println("Rotation steps:");
			for (int i = 0; i < motionGroup.length; i++) {
				System.out.println(motionGroup[i]); 
				if (motionGroup[i].getClass().getSimpleName().contains("Vector"))
					System.out.println(((SimpleVector) motionGroup[i]).rightVector); 
				System.out.println(Math.toDegrees(motionGroup[i].finalRotation));
			}
			*/
			return motionGroup[motionGroup.length - 1].calcFinalRotation();
		}
	}
	
	private double[] generateCapThrowHoldingAngles(double angle, int frames) {
		double[] holdingAngles = new double[frames];
		holdingAngles[0] = angle;
		int lastNormalAngleFrame = (frames - 1) / 2;
		for (int i = 1; i <= lastNormalAngleFrame; i++)
			holdingAngles[i] = SimpleMotion.NORMAL_ANGLE;
		for (int i = lastNormalAngleFrame + 1; i < frames; i++)
			holdingAngles[i] = angle;
		return holdingAngles;
	}
	
	private double[] generateOtherMovementHoldingAngles(SimpleMotion[] motionGroup, int index, double angle, double initialRotation, boolean rightVector) {
		
		Movement movement;
		SimpleVector angleCalculator;
		int frames = movementFrames.get(index);
		if (motionGroup.length > 0) {
			movement = new Movement(movementNames.get(index), motionGroup[motionGroup.length - 1].finalSpeed);
			angleCalculator = (SimpleVector) movement.getMotion(frames, rightVector, false);
			angleCalculator.setInitialAngle(motionGroup[motionGroup.length - 1].finalAngle);
			angleCalculator.setInitialRotation(initialRotation);
		}
		else {
			movement = new Movement(movementNames.get(index), listPreparer.initialVelocity);
			angleCalculator = (SimpleVector) movement.getMotion(frames, rightVector, false);
			angleCalculator.setInitialAngle(Math.PI / 2);
			angleCalculator.setInitialRotation(Math.PI / 2);
		}

		int framesTurnBeforeAngle = (int) angleCalculator.calcFramesToRotation(angleCalculator.initialAngle - booleanToPlusMinus(rightVector) * angle);
		double holdingAngles[] = new double[frames];
		if (framesTurnBeforeAngle == -1)
			for (int i = 0; i < frames; i++)
				holdingAngles[i] = SimpleMotion.NORMAL_ANGLE;
		else {
			for (int i = 0; i < framesTurnBeforeAngle; i++)
				holdingAngles[i] = SimpleMotion.NORMAL_ANGLE;
			holdingAngles[framesTurnBeforeAngle] = angle;
			for (int i = framesTurnBeforeAngle + 1; i < (framesTurnBeforeAngle + 1 + frames) / 2; i++)
				holdingAngles[i] = SimpleMotion.NORMAL_ANGLE;
			for (int i = (framesTurnBeforeAngle + frames) / 2; i < frames; i++)
				holdingAngles[i] = angle;
		}
		
		/*
		for (int i = 0; i < holdingAngles.length; i++)
			System.out.println(i + ": " + Math.toDegrees(holdingAngles[i]));
			*/
		
		return holdingAngles;
	}
	
	private SimpleMotion[] calcMotionGroup(int startIndex, int endIndex, double initialVelocity, int framesJump) {
		SimpleMotion[] motionGroup = new SimpleMotion[endIndex - startIndex];
		if (motionGroup.length == 0)
			return motionGroup;
		
		Movement initialMovement = new Movement(movementNames.get(startIndex), initialVelocity, framesJump); //need to add frames jump if want to use that here
		motionGroup[0] = initialMovement.getMotion(movementFrames.get(startIndex), currentVectorRight, false);
		motionGroup[0].setInitialAngle(Math.PI / 2);
		motionGroup[0].calcDispDispCoordsAngleSpeed();
		if (!motionGroup[0].getClass().getSimpleName().equals("SimpleMotion"))
			currentVectorRight = !currentVectorRight;

		for (int i = 1; i < motionGroup.length; i++) {
			int j = i + startIndex;
			Movement currentMovement = new Movement(movementNames.get(j), motionGroup[i - 1].finalSpeed);
			if (movementNames.get(j).equals("Homing Motion Cap Throw")) {			
				motionGroup[i] = currentMovement.getMotion(movementFrames.get(j), currentVectorRight, true);
				((ComplexVector) motionGroup[i]).setHoldingAngles(homingMotionThrowHoldingAngles);
			}
			else
				motionGroup[i] = currentMovement.getMotion(movementFrames.get(j), currentVectorRight, false);
			motionGroup[i].setInitialAngle(motionGroup[i - 1].finalAngle);
			motionGroup[i].calcDispDispCoordsAngleSpeed();
			if (!(movementNames.get(j).equals("Falling") || motionGroup[i].getClass().getSimpleName().equals("SimpleMotion")))
				currentVectorRight = !currentVectorRight;
		}
		
		for (SimpleMotion m : motionGroup)
			System.out.println(m.dispX + ", " + m.dispZ);
		
		System.arraycopy(motionGroup, 0, motions, startIndex, motionGroup.length);
		
		return motionGroup;
	}
	
	public SimpleMotion[] getMotions() {
		return motions;
	}
	
	public double getInitialAngle() {
		return initialAngle;
	}
	
	public double getTargetAngle() {
		return targetAngle;
	}
	
	public void maximize() {
		long startTime = System.currentTimeMillis();
		
		//SimpleMotion[] motionGroup1 = new SimpleMotion[variableCapThrow1Index];
		//SimpleMotion[] motionGroup2 = new SimpleMotion[variableMovement2Index - motionGroup2Index];
		//int motionGroup3Index;
		
		currentVectorRight = rightVector;
		
		SimpleMotion[] motionGroup1 = calcMotionGroup(0, Math.min(variableCapThrow1Index, variableMovement2Index), listPreparer.initialVelocity, VectorCalculator.framesJump);
		sumXDisps(motionGroup1);
		sumYDisps(motionGroup1);
		double dispXMotionGroup1 = dispX;
		double dispYMotionGroup1 = dispZ;
		double motionGroup1FinalAngle = Math.PI / 2;
		if (motionGroup1.length > 0)
			motionGroup1FinalAngle = motionGroup1[motionGroup1.length - 1].finalAngle;
		
		System.out.println("Group 1 displacement x, y: " + dispXMotionGroup1 + ", " + dispYMotionGroup1);
		//System.out.println("Group 1 displacement: " + dispMotionGroup1);
		//System.out.println("Group 1 angle: " + Math.toDegrees(angleMotionGroup1));
		
		//the holding angle for the first variable cap throw
		
		double bestAngle1 = 0;
		double bestAngle2 = 0;
		
		double bestAngle1Adjusted = 0;
		double bestAngle2Adjusted = 0;
		
		double bestDispX = dispXMotionGroup1;
		double bestDispY = dispYMotionGroup1;
		
		double bestDispX1 = bestDispX;
		double bestDispY1 = bestDispY;
		
		double bestDisp = Math.sqrt(Math.pow(dispXMotionGroup1, 2) + Math.pow(dispYMotionGroup1, 2));
		
		//System.out.println(variableCapThrow1VectorRight);
		
		SimpleMotion[] motionGroup2 = null;
		
		//first variable movement
		if (hasVariableCapThrow1) {
			//we need a motion group 2
			boolean variableCapThrow1VectorRight = currentVectorRight;
			currentVectorRight = !currentVectorRight;
			
			boolean motionGroup2VectorRight = currentVectorRight;
			motionGroup2 = calcMotionGroup(motionGroup2Index, variableMovement2Index, 0, 0); //last velocity does not currently matter as there is a ground pound then dive
			System.out.println("Motion group 2 calculation:");
			calcAll(motionGroup2);
			double dispMotionGroup2 = disp;
			double motionGroup2Angle = Math.PI / 2 - Math.abs(angle);
			double motionGroup2FinalAngle = Math.abs(motionGroup2[motionGroup2.length - 1].finalAngle - Math.PI / 2);
			double motionGroup2FinalRotation = calcFinalRotation(motionGroup2);
			
			System.out.println("motion group 2 disp: " + dispMotionGroup2);
			
			//optimize the cap throw
			Movement variableCapThrow1 = new Movement(movementNames.get(variableCapThrow1Index), motions[variableCapThrow1Index - 1].finalSpeed);
			int variableCapThrow1Frames = movementFrames.get(variableCapThrow1Index);
			System.out.println("frames: " + variableCapThrow1Frames);
			ComplexVector variableCapThrow1Vector = (ComplexVector) variableCapThrow1.getMotion(variableCapThrow1Frames, variableCapThrow1VectorRight, true);
			motions[variableCapThrow1Index] = variableCapThrow1Vector;
			variableCapThrow1Vector.setInitialAngle(motionGroup1FinalAngle);
			
			for (int i = 0; i < numSteps; i++) {
		//	for (int i = 0; i < 1; i++) {
				variableAngle1 =  i / ((double) numSteps - 1) * Math.PI / 2;
		//		variableAngle1 = Math.toRadians(26.126126126126128);

				variableCapThrow1Vector.setHoldingAngles(generateCapThrowHoldingAngles(variableAngle1, variableCapThrow1Frames));
				variableCapThrow1Vector.calcDisp();
				variableCapThrow1Vector.calcDispCoords();
				
				double variableCapThrow1DispX = variableCapThrow1Vector.dispX;
				double variableCapThrow1DispY = variableCapThrow1Vector.dispZ;
				
				double variableAngle1Adjusted;
				double motionGroup2AdjustedAngle;
				variableAngle1Adjusted = motionGroup1FinalAngle + booleanToPlusMinus(motionGroup2VectorRight) * variableAngle1;
				motionGroup2AdjustedAngle =  variableAngle1Adjusted - booleanToPlusMinus(motionGroup2VectorRight) * motionGroup2Angle;
				
				if (hasVariableCapThrow1Falling) {
					double[] fallingDisplacements = calcFallingDisplacements(variableCapThrow1Vector, variableCapThrow1Index, variableAngle1Adjusted, !variableCapThrow1VectorRight);
					variableCapThrow1DispX += fallingDisplacements[0];
					variableCapThrow1DispY += fallingDisplacements[1];
				}
				
				//System.out.println(Math.toDegrees(motions[variableCapThrow1Index].finalAngle));
				
		//		System.out.println(variableCapThrow1DispX);
		//		System.out.println(variableCapThrow1DispY);
		//		System.out.println(dispMotionGroup2 * Math.cos(motionGroup2AdjustedAngle));
		//		System.out.println(dispMotionGroup2 * Math.sin(motionGroup2AdjustedAngle));
				
				//System.out.println(Math.toDegrees(variableAngle1Adjusted));
				
				testDispX1 = dispXMotionGroup1 + variableCapThrow1DispX + dispMotionGroup2 * Math.cos(motionGroup2AdjustedAngle);
				testDispY1 = dispYMotionGroup1 + variableCapThrow1DispY + dispMotionGroup2 * Math.sin(motionGroup2AdjustedAngle);
				
				//find correct cap throw 2 angle and add that on
				if (hasVariableCapThrow2 || hasVariableOtherMovement2) {
					double motionGroup2AdjustedFinalAngle = variableAngle1Adjusted - booleanToPlusMinus(motionGroup2VectorRight) * motionGroup2FinalAngle;
					
					double motionGroup2FinalRotationAdjusted = motionGroup2FinalRotation + variableAngle1Adjusted - Math.PI / 2;
					//System.out.println(Math.toDegrees(variableAngle2) + ": " + Math.toDegrees(variableAngle2Adjusted));
					if (findVariableAngle2(motionGroup2, motionGroup2AdjustedFinalAngle, motionGroup2FinalRotationAdjusted, testDispX1, testDispY1)) {
						
						double testDisp = Math.sqrt(Math.pow(testDispX2, 2) + Math.pow(testDispY2, 2));
						if (testDisp > bestDisp) {
							bestAngle1 = variableAngle1;
							bestAngle2 = variableAngle2;
							bestDisp = testDisp;
							bestDispX = testDispX2;
							bestDispY = testDispY2;
							bestDispX1 = testDispX1;
							bestDispY1 = testDispY1;
							
							//for testing the vector
							bestAngle1Adjusted = variableAngle1Adjusted;
							bestAngle2Adjusted = variableAngle2Adjusted;
						}
					}
				}
				else {
					double testDisp = Math.sqrt(Math.pow(testDispX1, 2) + Math.pow(testDispY1, 2));
					if (testDisp > bestDisp) {
						bestAngle1 = variableAngle1;
						bestDisp = testDisp;
						bestDispX = testDispX1;
						bestDispY = testDispY1;
						
						//for testing the vector
						bestAngle1Adjusted = variableAngle1Adjusted;
					}
				}
				
				//System.out.println("Angle: " + Math.toDegrees(variableAngle1));
				//System.out.println("Group 2 Angle: " + Math.toDegrees(motionGroup2AdjustedAngle));
				//System.out.println("Displacement x, y: " + testDispX1 + ", " + testDispY1);
				//System.out.println("Variable 1 displacement x, y: " + motions[variableCapThrow1Index].dispX + ", " + motions[variableCapThrow1Index].dispZ);
				//System.out.println("Group 2 displacement x, y: " + dispMotionGroup2 * Math.cos(motionGroup2AdjustedAngle) + ", " + dispMotionGroup2 * Math.sin(motionGroup2AdjustedAngle));
			}
			
			//set cap throw 1 vector and falling vector to the correct angles
			variableCapThrow1Vector.setHoldingAngles(generateCapThrowHoldingAngles(bestAngle1, variableCapThrow1Frames));
			variableCapThrow1Vector.calcDisp();
			if (hasVariableCapThrow1Falling)
				calcFallingDisplacements(variableCapThrow1Vector, variableCapThrow1Index, bestAngle1Adjusted, !variableCapThrow1VectorRight);
			//recalculate variable cap throw or movement 2 for the best angle 1
			if (hasVariableCapThrow2 || hasVariableOtherMovement2) {
				double motionGroup2AdjustedFinalAngle = bestAngle1Adjusted - booleanToPlusMinus(motionGroup2VectorRight) * motionGroup2FinalAngle;
				double motionGroup2FinalRotationAdjusted = motionGroup2FinalRotation + bestAngle1Adjusted - Math.PI / 2;
				findVariableAngle2(motionGroup2, motionGroup2AdjustedFinalAngle, motionGroup2FinalRotationAdjusted, bestDispX1, bestDispY1); //will make bestDispX2 and bestDispY2 wrong
			}	
		}
		else if (hasVariableCapThrow2 || hasVariableOtherMovement2) {
			double motionGroup1FinalRotation = calcFinalRotation(motionGroup1);
			System.out.println("Rotation before variable movement 2:" + Math.toDegrees(motionGroup1FinalRotation));
			findVariableAngle2(motionGroup1, motionGroup1FinalAngle, motionGroup1FinalRotation, dispXMotionGroup1, dispYMotionGroup1);
			bestAngle2 = variableAngle2;
			bestAngle2Adjusted = variableAngle2Adjusted;
			bestDispX = testDispX2;
			bestDispY = testDispY2;
		}
		
		if (hasVariableCapThrow2 || hasVariableOtherMovement2) {
			double dispMotionGroup3 = 0;
			
			Movement groundPound = new Movement("Ground Pound");
			SimpleMotion gpMotion = groundPound.getMotion(1, false, false);
			gpMotion.setInitialAngle(bestAngle2Adjusted);
			motions[motions.length - 2] = gpMotion;
			
			Movement dive = new Movement("Dive");
			SimpleMotion diveMotion = dive.getMotion(movementFrames.get(motions.length - 1), false, false);
			diveMotion.setInitialAngle(bestAngle2Adjusted);
			motions[motions.length - 1] = diveMotion;
			
			dispMotionGroup3 = diveMotion.calcDispForward();
			
			bestDispX += dispMotionGroup3 * Math.cos(bestAngle2Adjusted);
			bestDispY += dispMotionGroup3 * Math.sin(bestAngle2Adjusted);
		}
		
		bestDisp = Math.sqrt(Math.pow(bestDispX, 2) + Math.pow(bestDispY, 2));
		System.out.println("Displacement x, y: " + bestDispX + ", " + bestDispY);
		System.out.println("Maximum displacement: " + bestDisp);
		System.out.println("Angle 1: " + Math.toDegrees(bestAngle1));
		System.out.println("Angle 2: " + Math.toDegrees(bestAngle2));
		System.out.println("Angle 1 Adjusted: " + Math.toDegrees(bestAngle1Adjusted));
		System.out.println("Angle 2 Adjusted: " + Math.toDegrees(bestAngle2Adjusted));
		System.out.println("Calculated in " + (System.currentTimeMillis() - startTime) + " ms");
		
		//adjusting motions to the optimized values
		if (hasVariableCapThrow1) {
			//((ComplexVector) motions[variableCapThrow1Index].set
			double adjustment = bestAngle1Adjusted - Math.PI / 2;
			for (int i = 0; i < motionGroup2.length; i++)
				motionGroup2[i].adjustInitialAngle(adjustment);
		}
		
		//rotating motions to the right angle
		
		double unadjustedTargetAngle = Math.atan(bestDispY / bestDispX);
		if (unadjustedTargetAngle < 0)
			unadjustedTargetAngle += Math.PI;
		System.out.println("Unadjusted target angle:" + Math.toDegrees(unadjustedTargetAngle));
		double adjustment;
		if (targetAngleGiven) {
			adjustment = givenAngle - unadjustedTargetAngle;
			initialAngle = Math.PI / 2 + adjustment;
			targetAngle = givenAngle;
		}
		else {
			System.out.println("hi");
			adjustment = givenAngle - Math.PI / 2;
			initialAngle = givenAngle;
			targetAngle = unadjustedTargetAngle + adjustment;
		}
		for (int i = 0; i < motions.length; i++) {
			motions[i].adjustInitialAngle(adjustment);
			if (motions[i].movement.movementType.equals("Falling") && i > 0)
				motions[i].movement.initialVerticalSpeed = motions[i - 1].calcFinalVerticalVelocity();
			System.out.println(motions[i].movement.movementType + " motion angle:" + Math.toDegrees(motions[i].initialAngle));
		}
		if (initialAngle < 0)
			initialAngle += 2 * Math.PI;
		if (targetAngle < 0)
			targetAngle += 2 * Math.PI;
		System.out.println("Initial angle:" + Math.toDegrees(initialAngle));
		System.out.println("Target angle:" + Math.toDegrees(targetAngle));
		
	}
	
	private double[] calcFallingDisplacements(ComplexVector variableCapThrowVector, int variableCapThrowIndex, double variableAngleAdjusted, boolean vectorRight) {
		double[] displacements = new double[2];
		variableCapThrowVector.calcFinalAngle();
		Movement variableCapThrowFalling = new Movement("Falling", variableCapThrowVector.calcFinalSpeed());
		SimpleVector variableCapThrowFallingVector = (SimpleVector) variableCapThrowFalling.getMotion(movementFrames.get(variableCapThrowIndex + 1), vectorRight, false);
		motions[variableCapThrowIndex + 1] = variableCapThrowFallingVector;
		variableCapThrowFallingVector.setOptimalForwardAccel(false);
		variableCapThrowFallingVector.setInitialAngle(variableCapThrowVector.finalAngle);
		variableCapThrowFallingVector.setHoldingAngle(Math.abs(variableCapThrowVector.finalAngle - variableAngleAdjusted));
		variableCapThrowFallingVector.calcDisp();
		variableCapThrowFallingVector.calcDispCoords();
		displacements[0] = variableCapThrowFallingVector.dispX;
		displacements[1] = variableCapThrowFallingVector.dispZ;
		return displacements;
	}
	
	private boolean findVariableAngle2(SimpleMotion[] motionGroup, double initialAngle, double initialRotation, double previousDispX, double previousDispY) {
		double initialForwardVelocity;
		if (variableMovement2Index - 1 >= 0)
			initialForwardVelocity = motions[variableMovement2Index - 1].finalSpeed;
		else
			initialForwardVelocity = listPreparer.initialVelocity;
		
		Movement variableMovement2 = new Movement(movementNames.get(variableMovement2Index), initialForwardVelocity);
		ComplexVector variableMovement2Vector = (ComplexVector) variableMovement2.getMotion(movementFrames.get(variableMovement2Index), currentVectorRight, true);
		motions[variableMovement2Index] = variableMovement2Vector;
		variableMovement2Vector.setInitialAngle(initialAngle); 
		
		double low = 0;
		double high = Math.PI / 4;
		variableAngle2 = Math.PI / 8;
		
		//variableAngle2 = Math.toRadians(11.40380859375);
		
		//System.out.println("Initial Angle:" + Math.toDegrees(initialAngle));
		
		while(high - low > .00001) {
		
			if (hasVariableCapThrow2)
				variableMovement2Vector.setHoldingAngles(generateCapThrowHoldingAngles(variableAngle2, movementFrames.get(variableMovement2Index)));
			else
				variableMovement2Vector.setHoldingAngles(generateOtherMovementHoldingAngles(motionGroup, variableMovement2Index, variableAngle2, initialRotation, currentVectorRight));
			variableMovement2Vector.calcDisp();
			variableMovement2Vector.calcDispCoords();
			
			double variableMovement2DispX = variableMovement2Vector.dispX;
			double variableMovement2DispY = variableMovement2Vector.dispZ;
			
			variableAngle2Adjusted = initialAngle - booleanToPlusMinus(currentVectorRight) * variableAngle2;
			
			if (hasVariableMovement2Falling) {
				double[] fallingDisplacements = calcFallingDisplacements(variableMovement2Vector, variableMovement2Index, variableAngle2Adjusted, !currentVectorRight);
				variableMovement2DispX += fallingDisplacements[0];
				variableMovement2DispY += fallingDisplacements[1];
			}
			
			testDispX2 = previousDispX + variableMovement2DispX;
			testDispY2 = previousDispY + variableMovement2DispY;
			double testDispAngle2 = Math.atan(testDispY2 / testDispX2);
			if (testDispAngle2 <= 0)
				testDispAngle2 += Math.PI;		
			
		//	System.out.println("High:" + Math.toDegrees(high));
		//	System.out.println("Low:" + Math.toDegrees(low));
		//	System.out.println("Test angle:" + Math.toDegrees(testFinalAngle));
		//	System.out.println("Disp angle:" + Math.toDegrees(testDispAngle));
			
			if (Math.abs(variableAngle2Adjusted - testDispAngle2) < .0001) {
				//System.out.println("angle 2 found");
				return true;
			}
			else if ((currentVectorRight && variableAngle2Adjusted < testDispAngle2) || (!currentVectorRight && variableAngle2Adjusted > testDispAngle2))
				high = variableAngle2;
			else
				low = variableAngle2;
			variableAngle2 = (high + low) / 2;
		}
		return false;
	}
	
}
