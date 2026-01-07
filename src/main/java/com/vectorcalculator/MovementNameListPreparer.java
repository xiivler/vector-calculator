package com.vectorcalculator;

import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;

public class MovementNameListPreparer {

	Properties p = Properties.p;
	
	//TableModel genPropertiesModel;
	DefaultTableModel movementModel;
	
	ArrayList<String> movementNames = new ArrayList<String>();
	ArrayList<Integer> movementFrames = new ArrayList<Integer>();
	
	double initialVelocity = 0;
	int framesJump = 0;

	int lastInitialMovementFrame = 0;
	
	String invalidMessage = "";
	
	public MovementNameListPreparer() {
		//this.genPropertiesModel = VectorCalculator.genPropertiesModel;
		this.movementModel = VectorCalculator.movementModel;
	}
	
	public String prepareList() {

		//initial movement
		//String name = genPropertiesModel.getValueAt(VectorCalculator.INITIAL_MOVEMENT_TYPE_ROW, 1).toString();
		String name = p.initialMovementName;
		int frames = 0;
		if (p.durationFrames) {
			frames = p.initialFrames;
		}
		else {
			VectorCalculator.initialMotion = VectorCalculator.initialMovement.getMotion(p.initialFrames, false, false);
			frames = VectorCalculator.initialMotion.calcFrames(p.initialDispY - VectorCalculator.getMoonwalkDisp());
			p.initialFrames = frames;
		}

		//add moonwalk if one is present
		if (p.canMoonwalk && p.framesMoonwalk > 0) {
			movementNames.add("Moonwalk");
			movementFrames.add(p.framesMoonwalk);
		}
		
		//prepending movements if there are ones to prepend to the actual named movememnt
		//not crouch roll because it will be easier for people to just input the speed at the time of rolling
		if (name.equals("Backflip") || name.equals("Long Jump")) {
			movementNames.add("Crouch");
			movementFrames.add(1);
		}
		else if (name.equals("Crouch") && frames > 43) {
			movementNames.add("Ground Pound Roll");
			movementFrames.add(43);
			movementNames.add("Roll Vector");
			movementFrames.add(frames - 43);
		}
		else if (name.equals("Uncapture")) {
			movementNames.add("Pre-Uncapture");
			movementFrames.add(1);
		}
		else if (name.contains("Pole/Fork")) {
			movementNames.add("Pole/Fork Pre-Flick");
			movementFrames.add(1);
		}
		
		int flowerFrames = 40;
		if (p.onMoon)
			flowerFrames = 125;
		
		//special cases to say which movements should happen
		if (name.equals("Ground Pound Roll") && frames > 43) {
			movementNames.add("Ground Pound Roll");
			movementFrames.add(43);
			movementNames.add("Roll Vector");
			movementFrames.add(frames - 43);
		}
		else if (name.equals("Crouch Roll") && frames > 57) {
			movementNames.add("Crouch Roll");
			movementFrames.add(57);
			movementNames.add("Roll Vector");
			movementFrames.add(frames - 57);
		}
		else if (name.equals("Roll Boost") && frames > 56) {
			movementNames.add("Roll Boost");
			movementFrames.add(56);
			movementNames.add("Roll Vector");
			movementFrames.add(frames - 56);
		}
		else if (name.equals("Flower Bounce") && frames > flowerFrames) {
			movementNames.add("Flower Bounce");
			movementFrames.add(flowerFrames);
			frames -= flowerFrames;
			if (frames > 80) {
				movementNames.add("Flower Bounce Part 2");
				movementFrames.add(80);
				movementNames.add("Flower Bounce Part 3");
				movementFrames.add(frames - 80);
			}
			else {
				movementNames.add("Flower Bounce Part 2");
				movementFrames.add(frames);
			}
		}
		else if (name.equals("Swinging Jump") && frames > 11) {
			movementNames.add("Swinging Jump");
			movementFrames.add(11);
			movementNames.add("Swinging Jump Vector");
			movementFrames.add(frames - 11);
		}
		else if (name.equals("Flip Forward") && frames > VectorCalculator.initialMovement.framesAtInitialHorizontalSpeed) { //could standardize this for all motion that behaves like this, add the motion name + " Vector"
			movementNames.add("Flip Forward");
			movementFrames.add(VectorCalculator.initialMovement.framesAtInitialHorizontalSpeed);
			movementNames.add("Flip Forward Vector");
			movementFrames.add(frames - VectorCalculator.initialMovement.framesAtInitialHorizontalSpeed);
		}
		else if (name.contains("RCV")) {
			if (p.initialAndTargetGiven) {
				double difference = p.initialAngle - p.targetAngle;
				while (difference >= 180) {
					difference -= 360;
				}
				while (difference < -180) {
					difference += 360;
				}
				if (Math.abs(difference) > 65) {
					return "Cannot calculate RCV with angle difference greater than 65°";
				}
			}
			movementNames.add(name);
			int rcFrames = (new Movement(name)).minFrames;
			movementFrames.add(rcFrames);
			movementNames.add("Falling");
			movementFrames.add(frames - rcFrames);
		}
		else {
			movementNames.add(name);
			movementFrames.add(frames);
		}
		
		if (p.chooseInitialHorizontalSpeed) {
			//initialVelocity = Double.parseDouble(genPropertiesModel.getValueAt(VectorCalculator.INITIAL_HORIZONTAL_SPEED_ROW, 1).toString());
			initialVelocity = p.initialHorizontalSpeed;
		}
		if (p.chooseJumpFrames) {
			//framesJump = Integer.parseInt(genPropertiesModel.getValueAt(VectorCalculator.HOLD_JUMP_FRAMES_ROW, 1).toString());
			framesJump = p.framesJump;
		}

		//calculate last initial movement frame
		lastInitialMovementFrame = -1;
		for (int duration : movementFrames) {
			lastInitialMovementFrame += duration;
		}
		
		//midair movement
		String oldName = "";
		
		for (int i = 0; i < movementModel.getRowCount(); i++) {
			name = movementModel.getValueAt(i, 0).toString();
			frames = Integer.parseInt(movementModel.getValueAt(i, 1).toString());
			
			if (Movement.isMidairCapThrow(name)) {
				if (Movement.isMidairCapThrow(oldName))
					return "Cannot have two throws in immediate succession";
				else if (oldName.equals("Dive"))
					return "Cannot throw from a dive";
				else if (frames > 24) {
					movementNames.add(name);
					movementFrames.add(24);
					movementNames.add("Falling");
					movementFrames.add(frames - 24);
				}
				else {
					movementNames.add(name);
					movementFrames.add(frames);
				}
			}
			else if (name.equals("Rainbow Spin")) {
				if (movementNames.contains("Rainbow Spin"))
					return "Cannot have two rainbow spins";
				else if (oldName.equals("Dive"))
					return "Cannot rainbow spin from a dive";
				else {
					movementNames.add(name);
					movementFrames.add(31);
					movementNames.add("Falling");
					movementFrames.add(frames - 31);
				}
			}
			else {
				if (name.equals("Cap Bounce")) {
					if (movementNames.contains("Cap Bounce") || movementNames.contains("2P Midair Vault"))
						return "Cannot have two cap bounces in a jump";
					if (oldName.equals("Dive"))
						name = "Dive Cap Bounce";
				}
				else if (name.equals("Dive"))
					if  (oldName.equals("Dive"))
						return "Cannot dive twice in immediate succession";
					else {
						movementNames.add("Ground Pound");
						movementFrames.add(1);
					}
				else if (name.equals("2P Midair Vault")) {
					if (movementNames.contains("Cap Bounce") || movementNames.contains("2P Midair Vault"))
						return "Cannot have two cap bounces in a jump";
					else if (oldName.equals("Dive"))
						return "Use cap bounce instead for 2P bounce after dive";
				}
				
				movementNames.add(name);
				movementFrames.add(frames);
			}
			oldName = name;
		}
		return "";
	}
	
	public void print() {
		for (int i = 0; i < movementNames.size(); i++)
			Debug.println(movementNames.get(i) + ", " + movementFrames.get(i));
	}
}
