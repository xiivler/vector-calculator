package com.vectorcalculator;

import java.util.ArrayList;

import com.vectorcalculator.Properties.HctDirection;

public class Movement {
	
	Properties p = Properties.p;
	
	public static final int MCCTU = 0, MCCTD = 1, MCCTL = 2, MCCTR = 3, CT = 4, DT = 5,
							TT = 6, TTU = 7, TTD = 8, TTL = 9, TTR = 10;
	public static final int CT_COUNT = 11; //number of possibilities
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
	public static final int[] CT_INPUT = {Inputs.MU, Inputs.MD, Inputs.ML, Inputs.MR, Inputs.Y, Inputs.Y, Inputs.Y, Inputs.MU, Inputs.MD, Inputs.ML, Inputs.MR};
	public static final String[] CT_NAMES = {"Down MCCT", "Up MCCT", "Left MCCT", "Right MCCT", "Button Single Throw", "Button Double Throw", "Button TT", "Down TT", "Up TT", "Left TT", "Right TT"};
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

	//no downthrow or fakethrow because these are equivalent to others
	public static final String[] RC_TYPES = {"Motion Cap Throw RCV", "Single Throw RCV", "Upthrow RCV", "Double Throw RCV", "Triple Throw RCV", "Spinthrow RCV"};
	
	//boolean variableSpeed = true;
	
	//int variableSpeedRow = -1;
	//int variableJumpFramesRow = -1;
	//int vectorableRow = -1;
	
	int minFrames = 1;
	int minRecommendedFrames = 1;
	int maxFrames = Integer.MAX_VALUE;

	double rotationalAccel = Math.toRadians(.3);
	double maxRotationalSpeed = Math.toRadians(6);
	double rotationalSpeedAfterMax = Math.toRadians(3.5);
	boolean hasRotationalAccel = true;

	boolean canMoonwalk = false;
	
	double initialHorizontalSpeed = 0;
	double initialVerticalSpeed = 0;
	double gravity = 1.5;
	double moonGravity = .4;
	double vectorAccel = .3;
	double forwardAccel = .5;
	double backwardAccel = 1;
	int framesAtMaxVerticalSpeed = 0;
	int framesAtInitialHorizontalSpeed = 0;
	//int jumpFramesOffset = 0; //for captures that have more frames of jumping than are held
	
	int frameOffset = 0; //for movement where the vertical motion starts after the horizontal
	
	//double minSpeedCap = 0; //triple jumps, for instance, require a speed of at least 14
	double defaultSpeedCap = 14; //speed cap only if you aren't traveling faster than it
	double trueSpeedCap = 24; //jumps are always capped to 24
	double recommendedInitialHorizontalSpeed = Double.MAX_VALUE; //only used for some movement types to suggest what the initial speed should be if it is less than their true speed cap
	double fallSpeedCap = -35;

	public static final double MIN_GP_HEIGHT = 40;
	
	String movementType;
	String displayName;
	
	ArrayList<Integer> inputs1 = new ArrayList<Integer>();
	ArrayList<Integer> inputs2 = new ArrayList<Integer>();
	//ArrayList<String> TSVInputs = inputs;
	
	boolean variableJumpFrames = false;
	boolean variableInitialHorizontalSpeed = true;
	
	//deprecated constructor
	public Movement(double initialHorizontalSpeed, double vectorAccel) {
		this.initialHorizontalSpeed = initialHorizontalSpeed;
		this.vectorAccel = vectorAccel;
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
		
		if (movementType.equals("Optimal Distance Motion")) {
			initialHorizontalSpeed = 29.94;
			recommendedInitialHorizontalSpeed = 29.94;
			trueSpeedCap = 100;
		}
		if (movementType.equals("Single Jump")) {
			if (initialHorizontalSpeed <= 3)
				initialVerticalSpeed = 17;
			else if (initialHorizontalSpeed >= 14)
				initialVerticalSpeed = 19.5;
			else
				initialVerticalSpeed = 17 + 5 * (initialHorizontalSpeed - 3) / 22;
			framesAtMaxVerticalSpeed = framesJump;
			variableJumpFrames = true;
			canMoonwalk = true;
			for (int i = 0; i < framesJump; i++)
				inputs1.add(Inputs.B);
		}
		
		else if (movementType.equals("Double Jump")) {
			if (initialHorizontalSpeed <= 3)
				initialVerticalSpeed = 19.5;
			else if (initialHorizontalSpeed >= 14)
				initialVerticalSpeed = 21;
			else
				initialVerticalSpeed = 19.5 + 3 * (initialHorizontalSpeed - 3) / 22;
			framesAtMaxVerticalSpeed = framesJump;
			variableJumpFrames = true;
			canMoonwalk = true;
			for (int i = 0; i < framesJump; i++)
				inputs1.add(Inputs.B);
		}
		
		else if (movementType.equals("Triple Jump")) {
			if (initialHorizontalSpeed <= 14)
				initialHorizontalSpeed = 14;
			initialVerticalSpeed = 25;
			framesAtMaxVerticalSpeed = framesJump;
			gravity = 1;
			moonGravity = .3;
			variableJumpFrames = true;
			canMoonwalk = true;
			for (int i = 0; i < framesJump; i++)
				inputs1.add(Inputs.B);
		}

		else if (movementType.equals("Moonwalk")) {
			forwardAccel = 0;
			vectorAccel = 0;
			defaultSpeedCap = initialHorizontalSpeed;
			trueSpeedCap = initialHorizontalSpeed;
			gravity = 3;
			moonGravity = 3;
		}
		
		else if (movementType.equals("Cap Return Jump")) {
			initialVerticalSpeed = 22;
			framesAtMaxVerticalSpeed = framesJump;
			gravity = 1.3;
			moonGravity = .6;
			variableJumpFrames = true;
			canMoonwalk = true;
			for (int i = 0; i < framesJump; i++)
				inputs1.add(Inputs.B);
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
		}
		
		else if (movementType.equals("Crouch")) {
			initialVerticalSpeed = 0;
			gravity = 0;
			vectorAccel = 0;
			defaultSpeedCap = 3.5;
			trueSpeedCap = 100;
			inputs1.add(Inputs.ZL);
		}
		
		else if (movementType.equals("Backflip")) {
			initialVerticalSpeed = 32;
			initialHorizontalSpeed = 5; //could have option for starting backwards as well
			recommendedInitialHorizontalSpeed = 0;
			forwardAccel = .2;
			vectorAccel = 0;
			defaultSpeedCap = 9;
			trueSpeedCap = 9;
			gravity = 1;
			moonGravity = .45;
			inputs1.add(Inputs.B);
		}
	
		else if (movementType.equals("Vault")) {
			if (p.onMoon)
				initialVerticalSpeed = 30;
			else
				initialVerticalSpeed = 32;
			gravity = 1;
			inputs1.add(Inputs.Y);
			inputs1.add(Inputs.Y);
		}
		
		else if (movementType.equals("Sideflip")) {
			initialVerticalSpeed = 32;
			initialHorizontalSpeed = 9;
			recommendedInitialHorizontalSpeed = 0;
			vectorAccel = .075;
			defaultSpeedCap = 9;
			trueSpeedCap = 9;
			gravity = 1;
			moonGravity = .45;
			inputs1.add(Inputs.B);
		}
		
		else if (movementType.equals("Long Jump")) {
			initialHorizontalSpeed += 4; //long jumps increase speed by 4
			if (initialHorizontalSpeed >= 14) //initial cap at 14 u/fr
				initialHorizontalSpeed = 14;
			else if (initialHorizontalSpeed <= 7.5) //you must be going at least 3.5 u/fr beforehand
				initialHorizontalSpeed = 7.5;
			initialVerticalSpeed = 12;
			forwardAccel = .25;
			vectorAccel = 0;
			defaultSpeedCap = 23;
			trueSpeedCap = 23;
			gravity = .48;
			moonGravity = .2;
			inputs1.add(Inputs.ZL);
			inputs2.add(Inputs.B);
		}
		
		//need to change rolls to falling to vector them, but falling may have different gravity
		else if (movementType.equals("Ground Pound Roll")) {
			displayName = "GP Roll";
			initialVerticalSpeed = 0;
			variableInitialHorizontalSpeed = false;
			initialHorizontalSpeed = 30;
			framesAtInitialHorizontalSpeed = 43;
			forwardAccel = 0;
			vectorAccel = 0;
			moonGravity = 1;
			defaultSpeedCap = 30;
			trueSpeedCap = 30;
			frameOffset = 1;
			inputs1.add(Inputs.Y);
		}
		
		//5% speed decay from the frame of crouching, which can be separated
		else if (movementType.equals("Crouch Roll")) {
			displayName = "Roll";
			initialVerticalSpeed = 12;
			if (initialHorizontalSpeed <= 20)
				initialHorizontalSpeed = 20;
			framesAtInitialHorizontalSpeed = 57;
			forwardAccel = 0;
			vectorAccel = 0;
			moonGravity = 1;
			defaultSpeedCap = 20;
			trueSpeedCap = 100; //no true speed cap known, using 100 to prevent breaking anything
			recommendedInitialHorizontalSpeed = 20;
			moonGravity = 1;
			frameOffset = 1;
			inputs1.add(Inputs.ZL);
			inputs2.add(Inputs.Y);
		}
		
		//technically there are 4 varieties, at speeds 20, 23, and 26
		else if (movementType.equals("Roll Boost")) {
			initialVerticalSpeed = 12;
			if (initialHorizontalSpeed <= 20)
				initialHorizontalSpeed = 20;
			framesAtInitialHorizontalSpeed = 56;
			forwardAccel = 0;
			vectorAccel = 0;
			moonGravity = 1;
			defaultSpeedCap = 35;
			trueSpeedCap = 35;
			recommendedInitialHorizontalSpeed = 29;
			moonGravity = 1;
			frameOffset = 1;
			inputs1.add(Inputs.ZL);
			inputs2.add(Inputs.M);
		}
		
		else if (movementType.equals("Roll Vector")) {
			displayName = "";
			initialVerticalSpeed = -35;
			trueSpeedCap = 30;
			recommendedInitialHorizontalSpeed = 29;
			moonGravity = 1;
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
			vectorAccel = 0;
			defaultSpeedCap = initialHorizontalSpeed;
			trueSpeedCap = initialHorizontalSpeed;
			gravity = 2;
			moonGravity = .8;
			inputs1.add(Inputs.Y);
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
			vectorAccel = 0;
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
			vectorAccel = 0;
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
			vectorAccel = 0;
			inputs1.add(Inputs.B);
		}

		else if (movementType.equals("Flip Forward Vector")) {
			displayName = "";
			initialVerticalSpeed = -.75;
			initialHorizontalSpeed = 8.6;
			defaultSpeedCap = 8.6;
			trueSpeedCap = 8.6;
			gravity = .95;
			moonGravity = .3;
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
			inputs1.add(Inputs.Y);
			inputs1.add(Inputs.Y);
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
		}
		
		else if (movementType.equals("2P Midair Vault")) {
			if (p.onMoon)
				initialVerticalSpeed = 25;
			else
				initialVerticalSpeed = 26;
			gravity = 1;
			inputs1.add(Inputs.B);
		}
		
		else if (movementType.contains("RCV")) {
			trueSpeedCap = 100;
			recommendedInitialHorizontalSpeed = 29.94;
			rotationalAccel = Math.toRadians(1.3);
			if (p.onMoon) {
				initialVerticalSpeed = -1;
			}
			else {
				initialVerticalSpeed = -1.5;
			}
			maxRotationalSpeed = Math.toRadians(1.3 * 5);
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
			inputs1.add(Inputs.MU);
			if (movementType.equals("Motion Cap Throw"))
				minFrames = 8;
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
		}
		
		else if (movementType.equals("Pre-Uncapture")) {
			displayName = "";
			initialVerticalSpeed = 0;
			initialHorizontalSpeed = 0;
			gravity = 0;
			vectorAccel = 0;
			defaultSpeedCap = 0;
			trueSpeedCap = 0;
		}
		
		else if (movementType.equals("Uncapture")) {
			initialVerticalSpeed = 20;
			recommendedInitialHorizontalSpeed = 0;
			//one frame of 0 motion beforehand
			initialHorizontalSpeed = 5;
			inputs1.add(Inputs.ZL);
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
			vectorAccel = 0;
			defaultSpeedCap = 9;
			trueSpeedCap = 9;
		}
		
		//lasts 80 frames
		else if (movementType.equals("Flower Bounce Part 2")) {
			displayName = "";
			initialVerticalSpeed = 0;
			forwardAccel = 1;
			vectorAccel = 0;
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
			vectorAccel = 0;
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
			vectorAccel = 0;
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
			vectorAccel = 0;
			gravity = 1;
			defaultSpeedCap = 15;
			trueSpeedCap = 15;
			inputs1.add(Inputs.NONE);
			inputs1.add(Inputs.B);
		}
		
		else if (movementType.equals("Swinging Jump Vector")) {
			displayName = "";
			initialHorizontalSpeed = 15;
			initialVerticalSpeed = 9;
			gravity = 1;
			defaultSpeedCap = 15;
			trueSpeedCap = 15;
		}
		
		else if (movementType.equals("Pole/Fork Pre-Flick")) {
			displayName = "";
			initialVerticalSpeed = 0;
			variableInitialHorizontalSpeed = false;
			vectorAccel = 0;
			gravity = 0;
			defaultSpeedCap = 0;
			trueSpeedCap = 0;
			inputs1.add(Inputs.M);
		}
		
		//could do custom angles in the future
		else if (movementType.equals("Horizontal Pole/Fork Flick")) {
			displayName = "Pole/Fork Flick";
			variableInitialHorizontalSpeed = false;
			initialHorizontalSpeed = 39.385849;
			initialVerticalSpeed = 39.385849;
			defaultSpeedCap = 39.385849;
			trueSpeedCap = 39.385849;

		}
		
		else if (movementType.equals("Motion Horizontal Pole/Fork Flick")) {
			displayName = "Pole/Fork Flick";
			variableInitialHorizontalSpeed = false;
			initialHorizontalSpeed = 43.324432;
			initialVerticalSpeed = 43.324432;
			defaultSpeedCap = 43.324432;
			trueSpeedCap = 43.324432;
		}
		
		else if (movementType.equals("Motion Vertical Pole/Fork Flick")) {
			displayName = "Pole/Fork Flick";
			variableInitialHorizontalSpeed = false;
			initialHorizontalSpeed = 8;
			initialVerticalSpeed = 61.27;
			defaultSpeedCap = 8;
			trueSpeedCap = 8;
		}
		
		this.initialHorizontalSpeed = Math.min(initialHorizontalSpeed, trueSpeedCap);
		
		/*
		if (initialHorizontalSpeed >= trueSpeedCap)
			this.initialHorizontalSpeed = trueSpeedCap;
		else if (initialHorizontalSpeed <= minSpeedCap)
			this.initialHorizontalSpeed = minSpeedCap;
		else
			this.initialHorizontalSpeed = initialHorizontalSpeed;
			*/
	}
	
	public double getSuggestedSpeed() {
		//return Math.min(recommendedInitialHorizontalSpeed, initialHorizontalSpeed);
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
	
	/*
	public double height(int frames) {
		
		frames -= frameOffset;
		if (frames <= 0)
			return 0;
		
		double height;
		
		double currentGravity;
		if (p.onMoon)
			currentGravity = moonGravity;
		else
			currentGravity = gravity;
		if (frames <= framesAtMaxVerticalSpeed) {
			height = frames * initialVerticalSpeed;
			return height;
		}
		else
			height = framesAtMaxVerticalSpeed * initialVerticalSpeed;
		frames -= framesAtMaxVerticalSpeed;
		if (frames <= 0)
			return height;
		int framesToMaxSpeed = (int) ((initialVerticalSpeed - fallSpeedCap) / currentGravity);
		if (frames <= framesToMaxSpeed)
			height += (2 * initialVerticalSpeed - currentGravity * (frames + 1)) / 2 * frames;
		else
			height += ((2 * initialVerticalSpeed - currentGravity * (framesToMaxSpeed + 1)) / 2) * framesToMaxSpeed + fallSpeedCap * (frames - framesToMaxSpeed);
		
		return height;
	}
	*/
	
	public SimpleMotion getMotion(int frames, boolean rightVector, boolean complex) {
		if (movementType.contains("RCV")) {
			return new GroundedCapThrow(this, !rightVector);
		}
		else if (vectorAccel == 0)
			if (complex && movementType.equals("Dive"))
				return new DiveTurn(this, rightVector, frames);
			else
				return new SimpleMotion(this, frames);
		else if (complex)
			return new ComplexVector(this, rightVector, frames);
		else
			return new SimpleVector(this, rightVector, frames);
	}
}
