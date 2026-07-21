package com.vectorcalculator;

import java.util.ArrayList;

import com.vectorcalculator.Properties.HctDirection;

public class Movement {
	
	Properties p = Properties.p;
	
	public static final int MCCTU = 0, MCCTD = 1, MCCTL = 2, MCCTR = 3, CT = 4, DT = 5,
							TT = 6, TTU = 7, TTD = 8, TTL = 9, TTR = 10, FT = 11;
	public static final int CT_COUNT = 11; //number of possibilities (excluding fakethrow)
	public static final double CT_DISP_F = 678;
	public static final double CT_DISP_V = 70;
	public static final double CT_DISP_S = 0;
	public static final double MCCTU_DISP_V = 70 - 2.181;
	public static final double MCCTU_DISP_S = -10.332;
	public static final double MCCTD_DISP_V = 70 + 2.181;
	public static final double MCCTD_DISP_S = -10.332;
	public static final double MCCTL_DISP_V = 70;
	public static final double MCCTL_DISP_S = -6.988;
	public static final double MCCTR_DISP_V = 70;
	public static final double MCCTR_DISP_S = 6.988;
	public static final double TT_DISP_F = 694;
	public static final double TT_DISP_V = 70;
	public static final double TT_DISP_S = 0;
	public static final double TTU_DISP_V = 70 - 8.78;
	public static final double TTU_DISP_S = -6.447;
	public static final double TTD_DISP_V = 70 + 8.78;
	public static final double TTD_DISP_S = -6.447;
	public static final double TTL_DISP_V = 70;
	public static final double TTL_DISP_S = -10.641;
	public static final double TTR_DISP_V = 70;
	public static final double TTR_DISP_S = 10.641;
	public static final int[] CT_FRAMES = {8, 8, 8, 8, 9, 8, 3, 3, 3, 3, 3};
	public static final int[] CT_FRAMES_UNTIL_FULLY_THROWN = {29, 29, 29, 29, 30, 29, 16, 16, 16, 16, 16};
	public static final int[] CT_INPUT = {Inputs.MU, Inputs.MD, Inputs.ML, Inputs.MR, Inputs.Y, Inputs.Y, Inputs.Y, Inputs.MU, Inputs.MD, Inputs.ML, Inputs.MR};
	public static final String[] CT_NAMES = {"Down MCCT", "Up MCCT", "Left MCCT", "Right MCCT", "Single Throw", "Button DT", "Button TT", "Down TT", "Up TT", "Left TT", "Right TT"};
	public static final double[][] CT_DISPS = 	{{CT_DISP_F, MCCTU_DISP_V, MCCTU_DISP_S},
												{CT_DISP_F, MCCTD_DISP_V, MCCTD_DISP_S},
												{CT_DISP_F, MCCTL_DISP_V, MCCTL_DISP_S},
												{CT_DISP_F, MCCTR_DISP_V, MCCTR_DISP_S},
												{CT_DISP_F, CT_DISP_V, CT_DISP_S},
												{CT_DISP_F, CT_DISP_V, CT_DISP_S},
												{TT_DISP_F, TT_DISP_V, TT_DISP_S},
												{TT_DISP_F, TTU_DISP_V, TTU_DISP_S},
												{TT_DISP_F, TTD_DISP_V, TTD_DISP_S},
												{TT_DISP_F, TTL_DISP_V, TTL_DISP_S},
												{TT_DISP_F, TTR_DISP_V, TTR_DISP_S}};
	//public static boolean onMoon = false;

	public static final double CAPPY_SPEED = 35; //how fast Cappy moves in 2P
	public static final double CAPPY_FAST_SPEED = 64; //Cappy's fast 2P speed (after triple throw)
	public static final double CAPPY_JUMP_V_SPEED = 18; //how fast Cappy moves in a 2P Cappy jump
	public static final int CAPPY_GP_FRAMES = 28; //how many frames it takes for Cappy to start moving downward after pressing ZL/ZR

	//no downthrow or fakethrow because these are equivalent to others
	public static final String[] RC_TYPES = {"Motion Cap Throw RCV", "Single Throw RCV", "Upthrow RCV", "Double Throw RCV", "Triple Throw RCV", "Spinthrow RCV"};
	
	public static boolean isTT(int ctType) {
		return (ctType == TT || ctType == TTU || ctType == TTD || ctType == TTL || ctType == TTR);
	}
	//boolean variableSpeed = true;
	
	//int variableSpeedRow = -1;
	//int variableJumpFramesRow = -1;
	//int vectorableRow = -1;
	
	int minFrames = 1;
	int minRecommendedFrames = 1;
	int maxFrames = Integer.MAX_VALUE;

	double angularAccel = Math.toRadians(.3);
	double maxAngVel = Math.toRadians(6);
	boolean hasRotationalAccel = true;

	boolean canMoonwalk = false;
	boolean canVector = true;
	
	double initialHorizontalSpeed = 0;
	double initialVerticalSpeed = 0;
	double gravity = 1.5;
	double moonGravity = .4;
	double sidewaysAccel = .3;
	double forwardAccel = .5;
	double backwardAccel = 1;
	int framesAtMaxVerticalSpeed = 0;
	int framesAtInitialHorizontalSpeed = 0;
	//int jumpFramesOffset = 0; //for captures that have more frames of jumping than are held
	
	int frameOffset = 0; //for movement where the vertical motion starts after the horizontal

	int inputOffset = -1; //number of frames before the movement begins that the inputs begin (ignoring additional motion offset for Lunakit)
	
	//double minSpeedCap = 0; //triple jumps, for instance, require a speed of at least 14
	double defaultSpeedCap = 14; //speed cap only if you aren't traveling faster than it
	double trueSpeedCap = 24; //jumps are always capped to 24
	double recommendedInitialHorizontalSpeed = Double.MAX_VALUE; //only used for some movement types to suggest what the initial speed should be if it is less than their true speed cap
	double fallSpeedCap = -35;

	double defaultRotation = 0; //by default, what the initial rotation is relative to the movement
	boolean chooseInitialRotation = true;

	public static final double MIN_GP_HEIGHT = 40;
	
	String movementType;
	String displayName;
	
	ArrayList<Integer> inputs1 = new ArrayList<Integer>();
	ArrayList<Integer> inputs2 = new ArrayList<Integer>();
	//ArrayList<String> TSVInputs = inputs;
	
	boolean variableJumpFrames = false;
	boolean variableInitialHorizontalSpeed = true;

	public static boolean isMidairCapThrow(String str) {
		return str.contains("Throw") && !str.contains("RCV");
	}

	public static boolean isCapBounce(String str) {
		return str.contains("Cap Bounce") || str.contains("2P Midair Vault");
	}

	private void calcVerticalVelocity(double initialHorizontalSpeed, double verticalVelMin, double verticalVelMax, double forwardVelMin, double forwardVelMax) {
		if (!p.customInitialRotation)
			p.initialRotation = 0;
		initialHorizontalSpeed = Math.min(initialHorizontalSpeed, trueSpeedCap);
		double initialForwardSpeed = initialHorizontalSpeed * Math.cos(Math.toRadians(p.initialRotation));
		if (initialForwardSpeed <= forwardVelMin)
			initialVerticalSpeed = verticalVelMin;
		else if (initialForwardSpeed >= forwardVelMax)
			initialVerticalSpeed = verticalVelMax;
		else
			initialVerticalSpeed = verticalVelMin + (initialForwardSpeed - forwardVelMin) * (verticalVelMax - verticalVelMin) / (forwardVelMax - forwardVelMin);
	}
	
	//deprecated constructor
	public Movement(double initialHorizontalSpeed, double sidewaysAccel) {
		this.initialHorizontalSpeed = initialHorizontalSpeed;
		this.sidewaysAccel = sidewaysAccel;
	}
	
	public Movement(String movementType, double initialHorizontalSpeed) {
		this(movementType, initialHorizontalSpeed, 10);
	}
	
	public Movement(String movementType, int framesJump) {
		this(movementType, Double.MAX_VALUE, framesJump);
	}
	
	public Movement(String movementType) {
		this(movementType, Double.MAX_VALUE, 10);
	}
	
	public Movement(String movementType, double initialHorizontalSpeed, int framesJump) {

		//this check will not work for some capture movement
		framesJump = Math.min(framesJump, 10);
		
		this.movementType = movementType;
		displayName = movementType;
		
		if (movementType.equals("None")) {
			//variableInitialHorizontalSpeed = false;
			//initialHorizontalSpeed = 0;
			minFrames = 0;
			maxFrames = 0;
			minRecommendedFrames = 0;
			gravity = 0;
			moonGravity = 0;
			sidewaysAccel = 0;
			canVector = false;
			forwardAccel = 0;
			backwardAccel = 0;
			recommendedInitialHorizontalSpeed = 0;
			//defaultSpeedCap = 0;
			//trueSpeedCap = 0;
		}

		else if (movementType.equals("Optimal Distance Motion")) {
			initialHorizontalSpeed = 29.94;
			recommendedInitialHorizontalSpeed = 29.94;
			trueSpeedCap = 100;
		}
		
		else if (movementType.equals("Single Jump")) {
			calcVerticalVelocity(initialHorizontalSpeed, 17, 19.5, 3, 14);
			framesAtMaxVerticalSpeed = framesJump;
			variableJumpFrames = true;
			canMoonwalk = true;
			for (int i = 0; i < framesJump; i++)
				inputs1.add(Inputs.B);
		}

		else if (movementType.equals("Double Jump")) {
			calcVerticalVelocity(initialHorizontalSpeed, 19.5, 21, 3, 14);
			framesAtMaxVerticalSpeed = framesJump;
			variableJumpFrames = true;
			canMoonwalk = true;
			for (int i = 0; i < framesJump; i++)
				inputs1.add(Inputs.B);
		}

		else if (movementType.equals("Triple Jump")) {
			calcVerticalVelocity(initialHorizontalSpeed, 19.5, 25, 3, 14);
			framesAtMaxVerticalSpeed = framesJump;
			gravity = 1;
			moonGravity = .3;
			variableJumpFrames = true;
			canMoonwalk = true;
			for (int i = 0; i < framesJump; i++)
				inputs1.add(Inputs.B);
		}
		
		else if (movementType.equals("Rocket Flower Jump")) {
			variableInitialHorizontalSpeed = false;
			initialHorizontalSpeed = 38;
			defaultSpeedCap = 38;
			trueSpeedCap = 38;
			initialVerticalSpeed = 18;
			framesAtMaxVerticalSpeed = framesJump;
			gravity = 1;
			moonGravity = .35;
			variableJumpFrames = true;
			canMoonwalk = true;
			displayName = "RF Jump";
			for (int i = 0; i < framesJump; i++)
				inputs1.add(Inputs.B);
		}

		else if (movementType.equals("Moonwalk")) {
			forwardAccel = 0;
			sidewaysAccel = 0;
			canVector = false;
			defaultSpeedCap = initialHorizontalSpeed;
			trueSpeedCap = initialHorizontalSpeed;
			gravity = 3;
			moonGravity = 3;
		}

		else if (movementType.equals("Coyote Time")) {
			forwardAccel = 0;
			sidewaysAccel = 0;
			canVector = false;
			defaultSpeedCap = initialHorizontalSpeed;
			trueSpeedCap = initialHorizontalSpeed;
			initialVerticalSpeed = -7;
			gravity = 0;
			moonGravity = 0;
			chooseInitialRotation = false;
		}
		
		else if (movementType.equals("Cap Return Jump") || movementType.equals("Rocket Flower Cap Return Jump")) {
			initialVerticalSpeed = 22;
			framesAtMaxVerticalSpeed = framesJump;
			gravity = 1.3;
			moonGravity = .6;
			variableJumpFrames = true;
			canMoonwalk = true;
			for (int i = 0; i < framesJump; i++)
				inputs1.add(Inputs.B);
			if (movementType.equals("Rocket Flower Cap Return Jump")) {
				displayName = "RF Cap Return Jump";
				variableInitialHorizontalSpeed = false;
				initialHorizontalSpeed = 38;
				defaultSpeedCap = 38;
				trueSpeedCap = 38;
			}
		}
		
		else if (movementType.equals("Ground Pound Jump")) {
			displayName = "GP Jump";
			if (p.onMoon)
				initialVerticalSpeed = 32;
			else
				initialVerticalSpeed = 40;
			initialHorizontalSpeed = 0;
			variableInitialHorizontalSpeed = false;
			inputs1.add(Inputs.B);
			chooseInitialRotation = false;
		}
		
		else if (movementType.equals("Crouch")) {
			if (p.framesRun > 0)
				initialVerticalSpeed = -7;
			else if (p.framesMoonwalk > 0)
				initialVerticalSpeed = -3 * p.framesMoonwalk;
			else {
				initialVerticalSpeed = 0;
				gravity = 0;
			}
			forwardAccel = 0;
			sidewaysAccel = 0;
			canVector = false;
			defaultSpeedCap = 3.5;
			trueSpeedCap = 100;
			canMoonwalk = true;
			inputs1.add(Inputs.ZL);
			chooseInitialRotation = false;
		}
		
		else if (movementType.equals("Backflip")) {
			initialVerticalSpeed = 32;
			initialHorizontalSpeed = 5; //could have option for starting backwards as well
			recommendedInitialHorizontalSpeed = 0;
			forwardAccel = .2;
			sidewaysAccel = .2;
			backwardAccel = .2;
			canVector = false;
			defaultSpeedCap = 9;
			trueSpeedCap = 9;
			gravity = 1;
			moonGravity = .45;
			inputs1.add(Inputs.B);
			chooseInitialRotation = false;
			defaultRotation = 180; //probably will get ignored
		}
	
		else if (movementType.equals("Vault") || movementType.equals("Rocket Flower Vault")) {
			if (p.onMoon)
				initialVerticalSpeed = 30;
			else
				initialVerticalSpeed = 32;
			gravity = 1;
			if (p.twoPlayerMode) {
				inputs1.add(Inputs.P2B);
			}
			else {
				inputs1.add(Inputs.Y);
				inputs1.add(Inputs.Y);
			}
			if (movementType.equals("Rocket Flower Vault")) {
				displayName = "RF Vault";
				variableInitialHorizontalSpeed = false;
				initialHorizontalSpeed = 38;
				defaultSpeedCap = 38;
				trueSpeedCap = 38;
			}
		}
		
		else if (movementType.equals("Sideflip")) {
			initialVerticalSpeed = 32;
			initialHorizontalSpeed = 9;
			recommendedInitialHorizontalSpeed = 0;
			forwardAccel = .25;
			sidewaysAccel = .075;
			backwardAccel = .5;
			defaultSpeedCap = 9;
			trueSpeedCap = 9;
			gravity = 1;
			moonGravity = .45;
			inputs1.add(Inputs.B);
			chooseInitialRotation = false;
		}
		
		else if (movementType.equals("Long Jump")) {
			initialHorizontalSpeed += 4; //long jumps increase speed by 4
			if (initialHorizontalSpeed >= 14) //initial cap at 14 u/fr
				initialHorizontalSpeed = 14;
			else if (initialHorizontalSpeed <= 7.5) //you must be going at least 3.5 u/fr beforehand
				initialHorizontalSpeed = 7.5;
			initialVerticalSpeed = 12;
			forwardAccel = .25;
			sidewaysAccel = .25;
			backwardAccel = .5;
			canVector = false;
			defaultSpeedCap = 23;
			trueSpeedCap = 23;
			gravity = .48;
			moonGravity = .2;
			inputs1.add(Inputs.ZL);
			inputs2.add(Inputs.B);
			canMoonwalk = true;
			chooseInitialRotation = false;
		}
		
		//need to change rolls to falling to vector them, but falling may have different gravity
		else if (movementType.contains("Ground Pound Roll")) {
			displayName = "GP Roll";
			initialVerticalSpeed = -7;
			framesAtMaxVerticalSpeed = 1;
			variableInitialHorizontalSpeed = false;
			initialHorizontalSpeed = 30;
			framesAtInitialHorizontalSpeed = 43;
			minFrames = 44;
			forwardAccel = 0;
			sidewaysAccel = 0;
			canVector = false;
			moonGravity = 1;
			defaultSpeedCap = 30;
			trueSpeedCap = 30;
			//frameOffset = 1;
			inputs1.add(Inputs.Y);
			chooseInitialRotation = false;
		}
		
		//5% speed decay from the frame of crouching, which can be separated
		else if (movementType.contains("Crouch Roll")) {
			displayName = "Roll";
			initialVerticalSpeed = 12;
			if (initialHorizontalSpeed <= 20)
				initialHorizontalSpeed = 20;
			framesAtInitialHorizontalSpeed = 57;
			minFrames = 58;
			forwardAccel = 0;
			sidewaysAccel = 0;
			canVector = false;
			moonGravity = 1;
			defaultSpeedCap = 20;
			trueSpeedCap = 100; //no true speed cap known, using 100 to prevent breaking anything
			recommendedInitialHorizontalSpeed = 20;
			moonGravity = 1;
			frameOffset = 1;
			inputs1.add(Inputs.ZL);
			inputs2.add(Inputs.Y);
			chooseInitialRotation = false;
		}
		
		//technically there are 4 varieties, at speeds 20, 23, and 26
		else if (movementType.contains("Roll Boost")) {
			displayName = "Roll Boost";
			initialVerticalSpeed = 12;
			if (initialHorizontalSpeed <= 20)
				initialHorizontalSpeed = 20;
			framesAtInitialHorizontalSpeed = 56;
			minFrames = 57;
			forwardAccel = 0;
			sidewaysAccel = 0;
			canVector = false;
			moonGravity = 1;
			defaultSpeedCap = 35;
			trueSpeedCap = 100; //sometimes you can get faster than 35 on slopes, so set this high to avoid that being an issue
			recommendedInitialHorizontalSpeed = 29;
			moonGravity = 1;
			frameOffset = 1;
			inputs1.add(Inputs.ZL);
			inputs2.add(Inputs.M);
			chooseInitialRotation = false;
		}
		
		else if (movementType.equals("Roll Vector")) {
			displayName = "";
			initialVerticalSpeed = -35;
			trueSpeedCap = 30;
			recommendedInitialHorizontalSpeed = 29;
			moonGravity = 1;
			chooseInitialRotation = false;
		}
		
		else if (movementType.equals("Dive")) {
			variableInitialHorizontalSpeed = false;
			if (p.onMoon) {
				initialVerticalSpeed = 17;
				initialHorizontalSpeed = 18;
			}
			else {
				initialVerticalSpeed = 28;
				initialHorizontalSpeed = 20;
			}
			forwardAccel = 0;
			backwardAccel = .5;
			sidewaysAccel = 0.125;
			canVector = false;
			defaultSpeedCap = initialHorizontalSpeed;
			trueSpeedCap = initialHorizontalSpeed;
			gravity = 2;
			moonGravity = .8;
			inputs1.add(Inputs.Y);
			chooseInitialRotation = false;
		}
		
		else if (movementType.equals("Spin Jump")) {
			initialVerticalSpeed = 20;
			defaultSpeedCap = 8;
			trueSpeedCap = 8;
			gravity = .4;
			moonGravity = .18;
			inputs1.add(Inputs.B);
		}
		
		else if (movementType.equals("Spinpound")) {
			if (p.onMoon)
				initialVerticalSpeed = 0;
			else
				initialVerticalSpeed = -35;
			recommendedInitialHorizontalSpeed = 0;
			sidewaysAccel = 0;
			canVector = false;
			moonGravity = 1.5;
			defaultSpeedCap = 0;
			trueSpeedCap = 0;
			fallSpeedCap = -45;
			inputs1.add(Inputs.ZL); //technically need to loop for how long it is
		}
		
		else if (movementType.equals("Ground Pound")) {
			displayName = "GP";
			if (p.onMoon)
				initialVerticalSpeed = 0;
			else
				initialVerticalSpeed = -45;
			recommendedInitialHorizontalSpeed = 0;
			forwardAccel = 0;
			sidewaysAccel = 0;
			canVector = false;
			moonGravity = 1.5;
			defaultSpeedCap = 0;
			trueSpeedCap = 0;
			fallSpeedCap = -45;
			frameOffset = 25;
			inputs1.add(Inputs.ZL);
		}
		
		//will need wall slide beforehand
		else if (movementType.equals("Wall Jump")) {
			initialVerticalSpeed = 23;
			initialHorizontalSpeed = 8.6;
			recommendedInitialHorizontalSpeed = 0;
			if (p.onMoon)
				framesAtInitialHorizontalSpeed = 30;
			else
				framesAtInitialHorizontalSpeed = 25;
			defaultSpeedCap = 8.6;
			trueSpeedCap = 8.6;
			gravity = .95;
			moonGravity = .3;
			inputs1.add(Inputs.B);
			chooseInitialRotation = false;
		}
		
		else if (movementType.equals("Flip Forward")) {
			initialVerticalSpeed = 23;
			initialHorizontalSpeed = 8.6;
			recommendedInitialHorizontalSpeed = 0;
			if (p.onMoon)
				framesAtInitialHorizontalSpeed = 30;
			else
				framesAtInitialHorizontalSpeed = 25;
			variableInitialHorizontalSpeed = false;
			defaultSpeedCap = 8.6;
			trueSpeedCap = 8.6;
			gravity = .95;
			moonGravity = .3;
			sidewaysAccel = 0;
			canVector = false;
			inputs1.add(Inputs.B);
			chooseInitialRotation = false;
		}

		else if (movementType.equals("Flip Forward Vector")) {
			displayName = "";
			initialVerticalSpeed = -.75;
			initialHorizontalSpeed = 8.6;
			defaultSpeedCap = 8.6;
			trueSpeedCap = 8.6;
			gravity = .95;
			moonGravity = .3;
			chooseInitialRotation = false;
		}
		
		else if (movementType.equals("Cap Bounce")) {
			initialVerticalSpeed = 25;
			gravity = 1;
			inputs1.add(Inputs.Y);
			inputs1.add(Inputs.Y);
		}
		
		else if (movementType.equals("Dive Cap Bounce")) {
			displayName = "Cap Bounce";
			initialVerticalSpeed = 22;
			trueSpeedCap = 16;
			gravity = 1;
			moonGravity = .6;
			if (p.twoPlayerMode) {
				inputs1.add(Inputs.P2B);
			}
			else {
				inputs1.add(Inputs.Y);
				inputs1.add(Inputs.Y);
			}
		}
		
		else if (movementType.equals("Ground Pound Cap Bounce")) {
			displayName = "GP Cap Bounce";
			if (p.onMoon)
				initialVerticalSpeed = 35;
			else
				initialVerticalSpeed = 30;
			initialHorizontalSpeed = 0;
			variableInitialHorizontalSpeed = false;
			inputs1.add(Inputs.Y);
			inputs1.add(Inputs.Y);
			chooseInitialRotation = false;
		}
		
		else if (movementType.equals("Large NPC Bounce")) {
			initialVerticalSpeed = 25;
			gravity = 1;
			inputs1.add(Inputs.NONE);
			inputs1.add(Inputs.B);
		}
		
		else if (movementType.equals("Small NPC Bounce")) {
			initialVerticalSpeed = 20;
			gravity = 1.75;
			moonGravity = .5;
		}
		
		else if (movementType.equals("Dive NPC Bounce")) {
			initialVerticalSpeed = 22;
			trueSpeedCap = 16;
			gravity = 1;
			moonGravity = .6;
		}
		
		else if (movementType.equals("Ground Pound Object/Enemy Bounce")) {
			displayName = "GP Bounce";
			initialVerticalSpeed = 35;
			initialHorizontalSpeed = 0;
			variableInitialHorizontalSpeed = false;
			chooseInitialRotation = false;
		}
		
		else if (movementType.equals("2P Midair Vault")) {
			if (p.onMoon)
				initialVerticalSpeed = 25;
			else
				initialVerticalSpeed = 26;
			gravity = 1;
			inputs1.add(Inputs.P2B);
		}
		
		else if (movementType.contains("RCV")) {
			trueSpeedCap = 100;
			recommendedInitialHorizontalSpeed = 29.94;
			chooseInitialRotation = false;
			angularAccel = Math.toRadians(1.3);
			if (p.onMoon) {
				initialVerticalSpeed = -1;
			}
			else {
				initialVerticalSpeed = -1.5;
			}
			maxAngVel = Math.toRadians(1.3 * 5);
			if (movementType.equals("Motion Cap Throw RCV")) {
				displayName = "MCCTRCV";
				minFrames = 19;
				inputs1.add(Inputs.MU);
				inputs2.add(Inputs.B);
			}
			else if (movementType.equals("Single Throw RCV")) {
				displayName = "Single Throw RCV";
				minFrames = 24;
				inputs1.add(Inputs.X);
				inputs2.add(Inputs.B);
			}
			else if (movementType.equals("Upthrow RCV")) {
				displayName = "UTRCV";
				minFrames = 28;
				inputs1.add(Inputs.MUU);
				inputs2.add(Inputs.B);
			}
			else if (movementType.equals("Downthrow RCV")) {
				displayName = "Downthrow RCV";
				minFrames = 28;
				inputs1.add(Inputs.MDD);
				inputs2.add(Inputs.B);
			}
			else if (movementType.equals("Double Throw RCV")) {
				displayName = "Double Throw RCV";
				minFrames = 34;
				inputs1.add(Inputs.X);
				inputs2.add(Inputs.B);
			}
			else if (movementType.equals("Fakethrow RCV")) {
				displayName = "Fakethrow RCV";
				minFrames = 34;
				inputs1.add(Inputs.X);
				inputs2.add(Inputs.B);
			}
			else if (movementType.equals("Triple Throw RCV")) {
				displayName = "TTRCV";
				minFrames = 44;
				inputs1.add(Inputs.X);
				inputs2.add(Inputs.B);
			}
			else if (movementType.equals("Spinthrow RCV")) {
				displayName = "STRCV";
				minFrames = 46;
				inputs1.add(Inputs.MLL);
				inputs2.add(Inputs.B);
			}
			/*
			else if (movementType.equals("MCCT Roll Cancel Spinpound")) {
				displayName = "MCCTRC to Spin";
				minFrames = 21;
				inputs1.add(Inputs.MU);
				inputs2.add(Inputs.B);
			}
				*/
			maxFrames = minFrames;
		}

		//only lasts 24 frames, should be split into falling after this
		//add min frames to this and rainbow spin?
		else if (movementType.contains("Throw")) {
			if (movementType.equals("Single Throw")) {
				minFrames = 9;
				inputs1.add(Inputs.Y);
			}
			else if (movementType.equals("Fake Throw")) {
				minFrames = 11;
				inputOffset = -2;
				inputs1.add(Inputs.P2Y);
				inputs1.add(Inputs.Y);
			}
			else {
				inputs1.add(Inputs.MU);
			}
			if (movementType.equals("Motion Cap Throw")) {
				minFrames = 8;
				displayName = "MCCT";
			}
			else if (movementType.equals("Triple Throw"))
				minFrames = 3;
			else if (movementType.equals("Homing Motion Cap Throw")) {
				displayName = "Homing MCCT";
				minFrames = Math.max(p.hctCapReturnFrame, 23); //potentially as small as 23 when throwing against a wall but program would need to know frame and maybe angle of homing
				for (int i = 0; i < p.hctHomingFrame; i++) {
					inputs1.add(Inputs.NONE);
				}
				if (p.hctDirection == HctDirection.UP)
					inputs1.add(Inputs.MU);
				else if (p.hctDirection == HctDirection.DOWN)
					inputs1.add(Inputs.MD);
				else if (p.hctDirection == HctDirection.LEFT)
					inputs1.add(Inputs.ML);
				else if (p.hctDirection == HctDirection.RIGHT)
					inputs1.add(Inputs.MR);
			}
			else if (movementType.equals("Homing Triple Throw")) {
				displayName = "Homing TT";
				minFrames = 23;
				for (int i = 0; i < 6; i++) {
					inputs1.add(Inputs.NONE);
				}
				inputs1.add(Inputs.MD);
			}
			initialVerticalSpeed = 6;
			defaultSpeedCap = 7;
			trueSpeedCap = 7;
			gravity = .3;
			moonGravity = .3;
		}
		
		//only lasts 31 frames, should be split into falling after this
		else if (movementType.equals("Rainbow Spin")) {
			minFrames = 32;
			initialVerticalSpeed = 10;
			defaultSpeedCap = 7;
			trueSpeedCap = 7;
			gravity = .8;
			moonGravity = .6;
			inputs1.add(Inputs.B);
		}
		
		else if (movementType.equals("Falling")) {
			displayName = "";
			defaultSpeedCap = 11;
			trueSpeedCap = 30;
			initialVerticalSpeed = -7;
			canMoonwalk = true;
		}

		else if (movementType.equals("Reverse Bonk")) {
			displayName = "Reverse Bonk";
			defaultSpeedCap = 2;
			trueSpeedCap = 2;
			gravity = .95;
			initialVerticalSpeed = 12;
			forwardAccel = 0;
			backwardAccel = 0;
			sidewaysAccel = 0;
			canVector = false;
		}
		
		else if (movementType.equals("Pre-Uncapture")) {
			displayName = "";
			initialVerticalSpeed = 0;
			initialHorizontalSpeed = 0;
			gravity = 0;
			moonGravity = 0;
			sidewaysAccel = 0;
			canVector = false;
			defaultSpeedCap = 0;
			trueSpeedCap = 0;
			chooseInitialRotation = false;
		}
		
		else if (movementType.equals("Uncapture")) {
			initialVerticalSpeed = 20;
			recommendedInitialHorizontalSpeed = 5;
			//one frame of 0 motion beforehand
			initialHorizontalSpeed = p.initialHorizontalSpeed;
			inputs1.add(Inputs.ZL);
			chooseInitialRotation = false;
		}
		
		else if (movementType.equals("Bouncy Object Bounce")) {
			initialVerticalSpeed = 57;
			initialHorizontalSpeed = 0;
		}
		
		//lasts only until the speed is 0
		else if (movementType.equals("Flower Bounce")) {
			if (p.onMoon)
				initialVerticalSpeed = 50;
			else
				initialVerticalSpeed = 60;
			forwardAccel = 1;
			sidewaysAccel = 0;
			canVector = false;
			defaultSpeedCap = 9;
			trueSpeedCap = 9;
		}
		
		//lasts 80 frames
		else if (movementType.equals("Flower Bounce Part 2")) {
			displayName = "";
			initialVerticalSpeed = 0;
			forwardAccel = 1;
			sidewaysAccel = 0;
			canVector = false;
			gravity = .1;
			moonGravity = .1;
			defaultSpeedCap = 9;
			trueSpeedCap = 9;
			fallSpeedCap = -1;
			
		}
		
		else if (movementType.equals("Flower Bounce Part 3")) {
			displayName = "";
			initialVerticalSpeed = -1;
			forwardAccel = 1;
			sidewaysAccel = 0;
			canVector = false;
			gravity = .1;
			moonGravity = .1;
			defaultSpeedCap = 9;
			trueSpeedCap = 9;
			fallSpeedCap = -8;
		}
		
		else if (movementType.equals("Flower Spinpound")) {
			if (p.onMoon)
				initialVerticalSpeed = 0;
			else
				initialVerticalSpeed = 15;
			recommendedInitialHorizontalSpeed = 9;
			sidewaysAccel = 0;
			canVector = false;
			defaultSpeedCap = 0;
			gravity = 2;
			moonGravity = 1.5;
			trueSpeedCap = 0;
			fallSpeedCap = -30;
			inputs1.add(Inputs.ZL); //technically more than one of these
		}
		
		else if (movementType.equals("Swinging Jump")) {
			initialHorizontalSpeed = 15;
			framesAtInitialHorizontalSpeed = 11;
			variableInitialHorizontalSpeed = false;
			initialVerticalSpeed = 20;
			framesAtMaxVerticalSpeed = 1;
			sidewaysAccel = 0;
			canVector = false;
			gravity = 1;
			defaultSpeedCap = 15;
			trueSpeedCap = 15;
			inputs1.add(Inputs.NONE);
			inputs1.add(Inputs.B);
			chooseInitialRotation = false;
		}
		
		else if (movementType.equals("Swinging Jump Vector")) {
			displayName = "";
			initialHorizontalSpeed = 15;
			initialVerticalSpeed = 9;
			gravity = 1;
			defaultSpeedCap = 15;
			trueSpeedCap = 15;
			chooseInitialRotation = false;
		}
		
		else if (movementType.equals("Pole/Fork Pre-Flick")) {
			displayName = "";
			initialVerticalSpeed = 0;
			variableInitialHorizontalSpeed = false;
			sidewaysAccel = 0;
			canVector = false;
			gravity = 0;
			defaultSpeedCap = 0;
			trueSpeedCap = 0;
			inputs1.add(Inputs.M);
			chooseInitialRotation = false;
		}
		
		//could do custom angles in the future
		else if (movementType.equals("Horizontal Pole/Fork Flick")) {
			displayName = "Pole/Fork Flick";
			variableInitialHorizontalSpeed = false;
			initialHorizontalSpeed = 39.385849;
			initialVerticalSpeed = 39.385849;
			defaultSpeedCap = 39.385849;
			trueSpeedCap = 39.385849;
			chooseInitialRotation = false;

		}
		
		else if (movementType.equals("Motion Horizontal Pole/Fork Flick")) {
			displayName = "Pole/Fork Flick";
			variableInitialHorizontalSpeed = false;
			initialHorizontalSpeed = 43.324432;
			initialVerticalSpeed = 43.324432;
			defaultSpeedCap = 43.324432;
			trueSpeedCap = 43.324432;
			chooseInitialRotation = false;
		}
		
		else if (movementType.equals("Motion Vertical Pole/Fork Flick")) {
			displayName = "Pole/Fork Flick";
			variableInitialHorizontalSpeed = false;
			initialHorizontalSpeed = 8;
			initialVerticalSpeed = 61.27;
			defaultSpeedCap = 8;
			trueSpeedCap = 8;
			chooseInitialRotation = false;
		}
		
		this.initialHorizontalSpeed = Math.min(initialHorizontalSpeed, trueSpeedCap);
	}
	
	public double getSuggestedSpeed() {
		if (!variableInitialHorizontalSpeed)
			return initialHorizontalSpeed;
		else
			return Math.min(recommendedInitialHorizontalSpeed, trueSpeedCap);
	}
	
	public double getTrueSpeedCap() {
		return trueSpeedCap;
	}
	
	public boolean variableJumpFrames() {
		return variableJumpFrames;
	}
	
	public boolean variableInitialHorizontalSpeed() {
		return variableInitialHorizontalSpeed;
	}
	
	public int getSuggestedFrames() {
		return Math.max(minRecommendedFrames, minFrames);
	}
	
	public int getMinFrames() {
		return minFrames;
	}
	
	public void setFramesJump(int framesJump) {
		framesJump = Math.min(framesJump, 10);
	}
	
	public SimpleMotion getMotion(int frames, boolean rightVector, boolean complex) {
		if (movementType.contains("RCV")) {
			return new GroundedCapThrow(this, !rightVector);
		}
		if (movementType.equals("Coyote Time")) {
			return new CoyoteTime(this, frames);
		}
		else if (!canVector)
			if (complex && movementType.equals("Dive"))
				return new DiveTurn(this, rightVector, frames);
			else if (complex)
				return new ComplexNonvector(this, rightVector, frames);
			else
				return new SimpleMotion(this, frames);
		else if (complex)
			return new ComplexVector(this, rightVector, frames);
		else
			return new SimpleVector(this, rightVector, frames);
	}
}
