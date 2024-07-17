package vectorCalc;

import java.util.ArrayList;

public class Movement {
	
	public static boolean onMoon = false;

	//no downthrow or fakethrow because these are equivalent to others
	public static final String[] RC_TYPES = {"Motion Cap Throw Roll Cancel", "Single Throw Roll Cancel", "Upthrow Roll Cancel", "Double Throw Roll Cancel", "Triple Throw Roll Cancel", "Spinthrow Roll Cancel"};
	
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
	
	double initialHorizontalSpeed = 0;
	double initialVerticalSpeed = 0;
	double gravity = 1.5;
	double moonGravity = .4;
	double vectorAccel = .3;
	double forwardAccel = .5;
	int framesAtMaxVerticalSpeed = 0;
	int framesAtInitialHorizontalSpeed = 0;
	//int jumpFramesOffset = 0; //for captures that have more frames of jumping than are held
	
	int frameOffset = 0; //for movement where the vertical motion starts after the horizontal
	
	//double minSpeedCap = 0; //triple jumps, for instance, require a speed of at least 14
	double defaultSpeedCap = 14; //speed cap only if you aren't traveling faster than it
	double trueSpeedCap = 24; //jumps are always capped to 24
	double recommendedInitialHorizontalSpeed = Double.MAX_VALUE; //only used for some movement types to suggest what the initial speed should be if it is less than their true speed cap
	double fallSpeedCap = -35;
	
	String movementType;
	String displayName;
	
	ArrayList<String> inputs = new ArrayList<String>();
	
	boolean variableJumpFrames = false;
	boolean variableInitialHorizontalSpeed = true;
	
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
		
		if (movementType.equals("Single Jump")) {
			if (initialHorizontalSpeed <= 3)
				initialVerticalSpeed = 17;
			else if (initialHorizontalSpeed >= 14)
				initialVerticalSpeed = 19.5;
			else
				initialVerticalSpeed = 17 + 5 * (initialHorizontalSpeed - 3) / 22;
			framesAtMaxVerticalSpeed = framesJump;
			variableJumpFrames = true;
			for (int i = 0; i < framesJump; i++)
				inputs.add("B");
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
			for (int i = 0; i < framesJump; i++)
				inputs.add("B");
		}
		
		else if (movementType.equals("Triple Jump")) {
			if (initialHorizontalSpeed <= 14)
				initialHorizontalSpeed = 14;
			initialVerticalSpeed = 25;
			framesAtMaxVerticalSpeed = framesJump;
			gravity = 1;
			moonGravity = .3;
			variableJumpFrames = true;
			for (int i = 0; i < framesJump; i++)
				inputs.add("B");
		}
		
		else if (movementType.equals("Cap Return Jump")) {
			initialVerticalSpeed = 22;
			framesAtMaxVerticalSpeed = framesJump;
			gravity = 1.3;
			moonGravity = .6;
			variableJumpFrames = true;
			for (int i = 0; i < framesJump; i++)
				inputs.add("B");
		}
		
		else if (movementType.equals("Ground Pound Jump")) {
			displayName = "GP Jump";
			if (onMoon)
				initialVerticalSpeed = 32;
			else
				initialVerticalSpeed = 40;
			initialHorizontalSpeed = 0;
			variableInitialHorizontalSpeed = false;
			inputs.add("B");
		}
		
		else if (movementType.equals("Crouch")) {
			initialVerticalSpeed = 0;
			gravity = 0;
			vectorAccel = 0;
			defaultSpeedCap = 3.5;
			trueSpeedCap = 100;
			inputs.add("ZL");
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
			inputs.add("B");
		}
	
		else if (movementType.equals("Vault")) {
			if (onMoon)
				initialVerticalSpeed = 30;
			else
				initialVerticalSpeed = 32;
			gravity = 1;
			inputs.add("Y");
			inputs.add("Y");
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
			inputs.add("B");
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
			inputs.add("ZL, B");
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
			inputs.add("Y");
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
			inputs.add("ZL, Y");
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
			inputs.add("ZL, shake");
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
			if (onMoon) {
				initialVerticalSpeed = 17;
				initialHorizontalSpeed = 18;
			}
			else {
				initialVerticalSpeed = 28;
				initialHorizontalSpeed = 20;
			}
			forwardAccel = 0;
			vectorAccel = 0;
			defaultSpeedCap = initialHorizontalSpeed;
			trueSpeedCap = initialHorizontalSpeed;
			gravity = 2;
			moonGravity = .8;
			inputs.add("Y");
		}
		
		else if (movementType.equals("Spin Jump")) {
			initialVerticalSpeed = 20;
			defaultSpeedCap = 8;
			trueSpeedCap = 8;
			gravity = .4;
			moonGravity = .18;
			inputs.add("B");
		}
		
		else if (movementType.equals("Spinpound")) {
			if (onMoon)
				initialVerticalSpeed = 0;
			else
				initialVerticalSpeed = -35;
			recommendedInitialHorizontalSpeed = 0;
			vectorAccel = 0;
			moonGravity = 1.5;
			defaultSpeedCap = 0;
			trueSpeedCap = 0;
			fallSpeedCap = -45;
			inputs.add("ZL"); //technically need to loop for how long it is
		}
		
		else if (movementType.equals("Ground Pound")) {
			displayName = "GP";
			if (onMoon)
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
			inputs.add("ZL");
		}
		
		//will need wall slide beforehand
		else if (movementType.equals("Wall Jump")) {
			initialVerticalSpeed = 23;
			initialHorizontalSpeed = 8.6;
			recommendedInitialHorizontalSpeed = 0;
			if (onMoon)
				framesAtInitialHorizontalSpeed = 30;
			else
				framesAtInitialHorizontalSpeed = 25;
			defaultSpeedCap = 8.6;
			trueSpeedCap = 8.6;
			gravity = .95;
			moonGravity = .3;
			inputs.add("B");
		}
		
		else if (movementType.equals("Flip Forward")) {
			initialVerticalSpeed = 23;
			initialHorizontalSpeed = 8.6;
			recommendedInitialHorizontalSpeed = 0;
			if (onMoon)
				framesAtInitialHorizontalSpeed = 30;
			else
				framesAtInitialHorizontalSpeed = 25;
			variableInitialHorizontalSpeed = false;
			defaultSpeedCap = 8.6;
			trueSpeedCap = 8.6;
			gravity = .95;
			moonGravity = .3;
			inputs.add("B");
		}
		
		else if (movementType.equals("Cap Bounce")) {
			initialVerticalSpeed = 25;
			gravity = 1;
			inputs.add("Y");
			inputs.add("Y");
		}
		
		else if (movementType.equals("Dive Cap Bounce")) {
			displayName = "Cap Bounce";
			initialVerticalSpeed = 22;
			trueSpeedCap = 16;
			gravity = 1;
			moonGravity = .6;
			inputs.add("Y");
			inputs.add("Y");
		}
		
		else if (movementType.equals("Ground Pound Cap Bounce")) {
			displayName = "GP Cap Bounce";
			if (onMoon)
				initialVerticalSpeed = 35;
			else
				initialVerticalSpeed = 30;
			initialHorizontalSpeed = 0;
			variableInitialHorizontalSpeed = false;
			inputs.add("Y");
			inputs.add("Y");
		}
		
		else if (movementType.equals("Large NPC Bounce")) {
			initialVerticalSpeed = 25;
			gravity = 1;
			inputs.add("");
			inputs.add("B");
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
		
		else if (movementType.equals("Ground Pound NPC Bounce")) {
			displayName = "GP NPC Bounce";
			if (onMoon)
				initialVerticalSpeed = 35;
			else
				initialVerticalSpeed = 30;
			initialHorizontalSpeed = 0;
			variableInitialHorizontalSpeed = false;
		}
		
		else if (movementType.equals("2P Midair Vault")) {
			if (onMoon)
				initialVerticalSpeed = 25;
			else
				initialVerticalSpeed = 26;
			gravity = 1;
			inputs.add("B");
		}
		
		else if (movementType.contains("Roll Cancel")) {
			trueSpeedCap = 100;
			recommendedInitialHorizontalSpeed = 29.94;
			rotationalAccel = Math.toRadians(1.3);
			maxRotationalSpeed = Math.toRadians(1.3 * 5);
			if (movementType.equals("Motion Cap Throw Roll Cancel")) {
				minFrames = 19;
				inputs.add("Shake");
			}
			else if (movementType.equals("Single Throw Roll Cancel")) {
				minFrames = 24;
				inputs.add("Y");
			}
			else if (movementType.equals("Upthrow Roll Cancel")) {
				minFrames = 28;
				inputs.add("Up shake");
			}
			else if (movementType.equals("Downthrow Roll Cancel")) {
				minFrames = 28;
				inputs.add("Down shake");
			}
			else if (movementType.equals("Double Throw Roll Cancel")) {
				minFrames = 34;
				inputs.add("Y");
			}
			else if (movementType.equals("Fakethrow Roll Cancel")) {
				minFrames = 34;
				inputs.add("Y");
			}
			else if (movementType.equals("Triple Throw Roll Cancel")) {
				minFrames = 44;
				inputs.add("Y");
			}
			else if (movementType.equals("Spinthrow Roll Cancel")) {
				minFrames = 46;
				inputs.add("Side shake");
			}
			maxFrames = minFrames;
		}

		//only lasts 24 frames, should be split into falling after this
		//add min frames to this and rainbow spin?
		else if (movementType.contains("Throw")) {
			inputs.add("Shake");
			if (movementType.equals("Motion Cap Throw"))
				minFrames = 8;
			else if (movementType.equals("Triple Throw"))
				minFrames = 3;
			else if (movementType.equals("Homing Motion Cap Throw")) {
				displayName = "Homing MCCT";
				minFrames = 36; //potentially as small as 23 when throwing against a wall but program would need to know frame and maybe angle of homing
				for (int i = 0; i < 19; i++)
					inputs.add("");
				inputs.add("Shake");
			}
			else if (movementType.equals("Homing Triple Throw")) {
				displayName = "Homing TT";
				minFrames = 23;
				for (int i = 0; i < 6; i++)
					inputs.add("");
				inputs.add("Shake");
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
			inputs.add("B");
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
			inputs.add("ZL");
		}
		
		else if (movementType.equals("Bouncy Object Bounce")) {
			initialVerticalSpeed = 57;
			initialHorizontalSpeed = 0;
		}
		
		//lasts only until the speed is 0
		else if (movementType.equals("Flower Bounce")) {
			if (onMoon)
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
			if (onMoon)
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
			inputs.add("ZL"); //technically more than one of these
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
			inputs.add("");
			inputs.add("B");
		}
		
		else if (movementType.equals("Swinging Jump Vector")) {
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
			inputs.add("Shake");
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
		return Math.min(recommendedInitialHorizontalSpeed, initialHorizontalSpeed);
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
		if (onMoon)
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
		if (movementType.contains("Roll Cancel")) {
			return new GroundedCapThrow(this, !rightVector);
		}
		else if (vectorAccel == 0)
			return new SimpleMotion(this, frames);
		else if (complex)
			return new ComplexVector(this, rightVector, frames);
		else
			return new SimpleVector(this, rightVector, frames);
	}
}
