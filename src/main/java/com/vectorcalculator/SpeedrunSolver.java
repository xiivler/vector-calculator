package com.vectorcalculator;

import java.util.ArrayList;
import java.util.Arrays;

import com.vectorcalculator.Properties.CoyoteType;
import com.vectorcalculator.Properties.HctType;
import com.vectorcalculator.Properties.YNT;

//this class finds the optimal durations for each midair input, given the target vertical displacement
public class SpeedrunSolver implements SolverInterface {
	public static final double IM_MIN_Y_DISP = 100;


	Properties p = Properties.p;
	VectorMaximizer maximizer;
	boolean diveTurn = true;
	boolean singleThrowAllowed = true;
	YNT ttAllowed = YNT.NO;
	int minTotalFrames = 0;
	boolean success = false;
	double bestDisp;
	String error = "";
	boolean mcctAllowed = true; //TODO logic for this

	double imForwardDisp = 0;
	double imYDisp = 0;

	public static final double RANGE = 10; //how much less far should still be considered as a candidate

	@Override
	public boolean solve(int delta) {
		if (!DispData.initialized) {
			DispData.initializeDispData();
		}

		long startTime = System.currentTimeMillis();

		double xDiff = p.x1 - p.x0;
		double zDiff = p.z1 - p.z0;
		double targetDisp = Math.sqrt(xDiff * xDiff + zDiff * zDiff);

		VectorCalculator.setProgressText("Solver: Generating Data");

		//DispData imData = DispData.getDispData("Vault", DispData.DEFAULT);
		DispDataCTDive ctDiveData = (DispDataCTDive) DispData.getDispData("MCCT Dive", DispData.DEFAULT);
		DispData cbData = getDispData(Movement.CB, 1, 50); //TODO: these values are just guesses and are only appropriate for Dive CB in regular gravity
		DispData diveData = getDispData(Movement.DIVE2, 1, 30); //TODO: similar
		DispData fctData = new DispData();
		if (p.fct == YNT.YES || p.fct == YNT.TEST)
			fctData = getDispData(Movement.CT2, 8, 35); //TODO: similar
		DispData hctData = new DispData();
		if (p.componentIndices[Movement.HCT] >= 0) {
			int hctType = p.getHCTType();
			int hctMinFrames = 23;
			if (hctType == VectorCalculator.HMCCT)
				hctMinFrames = Math.max(p.hctType == HctType.OPTIMAL ? 36 : p.hctCapReturnFrame, 23);
			hctData = getDispDataHCT(hctMinFrames, 36);
		}
		//DispData cbData = DispData.getDispData("Dive Cap Bounce", DispData.DEFAULT);
		//DispData diveData = DispData.getDispData("Final Dive", DispData.DEFAULT);
		//DispData fctData = DispData.getDispData("Final Cap Throw", DispData.DEFAULT);

		ArrayList<Candidate> candidates = new ArrayList<Candidate>();

		VectorCalculator.setProgressText("Solver: Testing Permutations");
		
		int maxFrames = Integer.MAX_VALUE;

		double ctDiveDataMaxYDisp = ctDiveData.maxYDisp();
		double cbDataMaxYDisp = cbData.maxYDisp();
		double diveDataMaxYDisp = diveData.maxYDisp();
		double fctDataMaxYDisp = fctData.maxYDisp();

		double lowestYDisp = p.y1 - Solver.ERROR - p.getUpwarpMinusError() - p.y0; //lowest yDisp that will make the movement land
		double highestYDisp = p.y1 - p.y0 + Solver.ERROR; //highest yDisp that will make the movement land

		p.initialDispY = p.y1 - p.y0 + IM_MIN_Y_DISP;

        int imMinFrames = VectorCalculator.initialMovement.getMotion(p.initialFrames, false, false).calcFrames(p.initialDispY - VectorCalculator.getCoyoteDisp()) + p.framesCrouch + p.framesMoonwalk + p.framesRun;
		int cbMinFrames = 1;
		int diveMinFrames = 14;
		int fctMinFrames = p.fct == YNT.YES ? 8 : 0; //TODO: handle for TT FCT
		int ctDiveMinFrames = ctDiveData.minFrames();

		int cbMaxFrames = cbData.maxFrames();
		int diveMaxFrames = diveData.maxFrames();
		int fctMaxFrames = fctData.maxFrames();

		double[] cbYDisps = cbData.yDisps;
		double[] diveYDisps = diveData.yDisps;
		double[] fctYDisps = fctData.yDisps;
		double[] ctDiveYDisps = new double[ctDiveData.data.length];
		for (int i = 0; i < ctDiveData.data.length; i++) {
			ctDiveYDisps[i] = ctDiveData.yDisp(i);
		}

		double cbForwardDisps[] = cbData.forwardDisps;
		//double diveForwardDisps[] = diveData.forwardDisps;
		double fctForwardDisps[] = fctData.forwardDisps;
		double ctDiveForwardDisps[] = new double[ctDiveData.data.length];
		for (int i = 0; i < ctDiveData.data.length; i++) {
			ctDiveForwardDisps[i] = ctDiveData.forwardDisp(i);
		}

		//the minimum possible number of frames
		int minFrames = imMinFrames + cbMinFrames + diveMinFrames + fctMinFrames + ctDiveMinFrames + 1 + 1; //2 ground pounds of 1 frame each
		int totalFrames1 = minFrames;

		System.out.println("Min Frames: " + minFrames);

		int minCoyoteFrames = 0;
		int maxCoyoteFrames = p.getCoyoteTimeMaxFrames();

		int[] frames = new int[Movement.COMPONENT_COUNT];

		for (int framesJump = 1; framesJump <= (VectorCalculator.initialMovement.variableJumpFrames ? 10 : 1); framesJump++) { //tried flipping to count down but it was actually slower that way
			p.framesJump = framesJump;
			for (int coyoteFrames = minCoyoteFrames; coyoteFrames <= maxCoyoteFrames; coyoteFrames++) {
				//reset values
				imForwardDisp = 0;
				imYDisp = 0;
				totalFrames1 = minFrames;
				for (frames[Movement.IM] = imMinFrames; totalFrames1 <= maxFrames; frames[Movement.IM]++, totalFrames1++) { //TODO: start at a more reasonable value (max height probably)
					System.out.println("Total / Max Frames: " + totalFrames1 + ", " + maxFrames);
					double prevImForwardDisp = imForwardDisp;
					double prevImYDisp = imYDisp;
					calcIMDisps(frames[Movement.IM], coyoteFrames);
					double maxYDisp = imYDisp + ctDiveDataMaxYDisp + cbDataMaxYDisp + diveDataMaxYDisp + fctDataMaxYDisp;
					if (maxYDisp < lowestYDisp)
						break; //if it is impossible to get high enough, break now
					double maxYDisp2 = maxYDisp - cbDataMaxYDisp;
					int totalFrames2 = totalFrames1;
					double forwardDisp2 = imForwardDisp;

					//calculate efficiency of last frame of IM
					double yVel = imYDisp - prevImYDisp;
					double forwardDelta = imForwardDisp - prevImForwardDisp;
					double imEfficiency;
					if (yVel >= 0)
						imEfficiency = 2;
					else
						imEfficiency = -1 / ((yVel / forwardDelta) - 1);

					System.out.println("IM: " + frames[Movement.IM] + ", " + imForwardDisp + ", " + imYDisp + ", " + imEfficiency);

					cbMaxFrames = cbData.maxFrames(imEfficiency);
					diveMaxFrames = diveData.maxFrames(imEfficiency);
					//fctMaxFrames = fctData.maxFrames(imEfficiency);
					//System.out.println(" CB Max Frames: " + cbMaxFrames);
					//System.out.println(" Dive Max Frames: " + diveMaxFrames);

					for (frames[Movement.CB] = cbMinFrames; frames[Movement.CB] <= cbMaxFrames && totalFrames2 <= maxFrames; frames[Movement.CB]++, totalFrames2++) {
						fctMaxFrames = fctData.maxFrames(cbData.efficiencies[frames[Movement.CB]]); //doesn't really help because it doesn't tend to get this long
						//System.out.println(" FCT Max Frames: " + fctMaxFrames);

						if (maxYDisp2 + cbYDisps[frames[Movement.CB]] < lowestYDisp)
							break;
						double maxYDisp3 = maxYDisp2 + cbYDisps[frames[Movement.CB]] - diveDataMaxYDisp;
						int totalFrames3 = totalFrames2;
						double forwardDisp3 = forwardDisp2 + cbForwardDisps[frames[Movement.CB]];

						for (frames[Movement.DIVE2] = diveMinFrames; frames[Movement.DIVE2] <= diveMaxFrames && totalFrames3 <= maxFrames; frames[Movement.DIVE2]++, totalFrames3++) {
							if (maxYDisp3 + diveYDisps[frames[Movement.DIVE2]] < lowestYDisp)
								break;
							double maxYDisp4 = maxYDisp3 + diveYDisps[frames[Movement.DIVE2]] - fctDataMaxYDisp;
							int totalFrames4 = totalFrames3;
							double forwardDisp4 = forwardDisp3 + frames[Movement.DIVE2] * 20; //TODO allow for diveangle

							for (frames[Movement.CT2] = fctMinFrames; frames[Movement.CT2] <= fctMaxFrames && totalFrames4 <= maxFrames; frames[Movement.CT2]++, totalFrames4++) {
								if (frames[Movement.CT2] > 0 && frames[Movement.CB] < p.cbCapReturnFrame) //cannot have a fct //TODO account for CB first hct case
									break;
								if (frames[Movement.CT2] > 0 && frames[Movement.CT2] < 8)
									continue;
								if (maxYDisp4 + fctYDisps[frames[Movement.CT2]] < lowestYDisp)
									break;
								double maxYDisp5 = maxYDisp4 + fctYDisps[frames[Movement.CT2]] - ctDiveDataMaxYDisp;
								double forwardDisp5 = forwardDisp4 + fctForwardDisps[frames[Movement.CT2]];

								// for (frames[Movement.CT2] = hctMinFrames; frames[Movement.CT2] <= hctMaxFrames && totalFrames5 <= maxFrames; frames[Movement.CT2]++, totalFrames5++) {
								// 	if (frames[Movement.CT2] > 0 && frames[Movement.CB] < p.cbCapReturnFrame) //cannot have a fct //TODO account for CB first hct case
								// 		break;
								// 	if (frames[Movement.CT2] > 0 && frames[Movement.CT2] < 8)
								// 		continue;
								// 	if (maxYDisp4 + fctYDisps[frames[Movement.CT2]] < lowestYDisp)
								// 		break;
								// 	double maxYDisp6 = maxYDisp5 + hctYDisps[frames[Movement.CT2]] - hctDataMaxYDisp;
								// 	double forwardDisp6 = forwardDisp5 + fctForwardDisps[frames[Movement.CT2]];

								for (int ctDiveDataIndex = 0; ctDiveDataIndex < ctDiveData.data.length; ctDiveDataIndex++) {
									int totalFrames = totalFrames4 + ctDiveData.frames(ctDiveDataIndex) - ctDiveMinFrames;
									if (totalFrames > maxFrames) {
										break;
									}
									double yDisp = maxYDisp5 + ctDiveYDisps[ctDiveDataIndex];
									if (yDisp > highestYDisp || yDisp < lowestYDisp) {
										continue;
									}
									double forwardDisp = forwardDisp5 + ctDiveForwardDisps[ctDiveDataIndex];
									if (forwardDisp >= targetDisp - RANGE) {
										frames[Movement.CT1] = ctDiveData.ctFrames(ctDiveDataIndex);
										frames[Movement.DIVE1] = ctDiveData.diveFrames(ctDiveDataIndex);
										int[][] midairs = new int[p.midairs.length][2];
										for (int i = 0; i < midairs.length; i++) {
											midairs[i][0] = p.midairs[i][0];
										}
										for (int component = 1; component < Movement.COMPONENT_COUNT; component++) {
											if (p.componentIndices[component] >= 0) {
												midairs[p.componentIndices[component]][1] = frames[component];
											}
										}
										//int midairs[] = {ctDiveData.ctFrames(ctDiveDataIndex), ctDiveData.diveFrames(ctDiveDataIndex), frames[Movement.CB], fctFrames, diveFrames};
										candidates.add(new Candidate(totalFrames, frames[Movement.IM] - p.framesCrouch - p.framesMoonwalk - p.framesRun, midairs, coyoteFrames, framesJump, forwardDisp, yDisp));
										if (forwardDisp >= targetDisp + RANGE)
											maxFrames = totalFrames;
									}
								}
							}
						}
					}
				}
			}
		}
		if (candidates.size() == 0) {
			error = "No Solution Found";
			success = false;
			return false;
		}
		candidates.sort(null);
		System.out.println("Permutations tested in " + (System.currentTimeMillis() - startTime) + " ms");
		VectorCalculator.setProgressText("Solver: Testing Optimal Candidates");
		minTotalFrames = candidates.get(0).totalFrames;
		bestDisp = 0;
		Candidate bestCandidate = null;
		for (Candidate c : candidates) {
			if (c.totalFrames == minTotalFrames || c.totalFrames > minTotalFrames && bestDisp < targetDisp) {
				if (c.totalFrames > minTotalFrames && bestDisp < targetDisp)
					minTotalFrames++;
				p.initialFrames = c.imFrames;
				p.framesJump = c.framesJump;
				int eliminatedMovements = 0;
				VectorCalculator.addPreset(c.midairs);
				for (int i = 0; i < p.midairs.length; i++) {
					if (p.midairs[i][1] == 0)
						eliminatedMovements++;
				}
				int[][] realMidairs = new int[p.midairs.length - eliminatedMovements][2];
				int realIndex = 0;
				for (int i = 0; i < p.midairs.length; i++) {
					if (p.midairs[i][1] != 0) {
						realMidairs[realIndex][0] = p.midairs[i][0];
						realMidairs[realIndex][1] = p.midairs[i][1];
						realIndex++;
					}
				}
				p.midairs = realMidairs;
				VectorCalculator.addPreset(p.midairs);
				if (p.coyoteType == CoyoteType.MOONWALK)
					p.framesMoonwalk = c.coyoteFrames;
				else if (p.coyoteType == CoyoteType.RUNNING)
					p.framesRun = c.coyoteFrames;
				double disp = test();
				if (disp > bestDisp) {
					bestDisp = disp;
					bestCandidate = c;
				}
				System.out.println(c.toString() + ", " + disp);
			}
		}
		System.out.println();
		if (bestDisp >= targetDisp) {
			System.out.println("Best Result: " + bestCandidate.toString() + ", " + bestDisp);
			p.initialFrames = bestCandidate.imFrames;
			p.framesJump = bestCandidate.framesJump;
			int eliminatedMovements = 0;
			// VectorCalculator.addPreset("Spinless", false);
			p.midairs = bestCandidate.midairs;
			for (int i = 0; i < p.midairs.length; i++) {
				if (p.midairs[i][1] == 0)
					eliminatedMovements++;
			}
			int[][] realMidairs = new int[p.midairs.length - eliminatedMovements][2];
			int realIndex = 0;
			for (int i = 0; i < p.midairs.length; i++) {
				if (p.midairs[i][1] != 0) {
					realMidairs[realIndex][0] = p.midairs[i][0];
					realMidairs[realIndex][1] = p.midairs[i][1];
					realIndex++;
				}
			}
			//TODO identify what the preset SHOULD be now
			p.midairs = realMidairs;
			VectorCalculator.addPreset(p.midairs);
			if (p.coyoteType == CoyoteType.MOONWALK)
				p.framesMoonwalk = bestCandidate.coyoteFrames;
			else if (p.coyoteType == CoyoteType.RUNNING)
				p.framesRun = bestCandidate.coyoteFrames;
			double disp = test();
			// VectorDisplayWindow.generateData(maximizer);
			// VectorDisplayWindow.display();
			VectorCalculator.setProgressText("Solver: Calculated in " + (System.currentTimeMillis() - startTime) + " ms");
			success = true;
			return true;
		}
		else {
			error = "No Solution Found";
			success = false;
			return false;
		}
	}

	public double test() {
        maximizer = VectorCalculator.getMaximizer();
        if (!diveTurn && !p.twoPlayerMode) {
            maximizer.edgeCBMin = Solver.EDGE_CB_MIN_NO_DIVE_TURN;
            maximizer.edgeCBMax = Solver.EDGE_CB_MAX_NO_DIVE_TURN;
        }
        else
            maximizer.edgeCBMin = 0;
        p.vectorAngle = 90;
        p.diveCapBounceAngle = diveTurn ? Solver.DEFAULT_EDGE_CB_ANGLE_DIVE_TURN : 0;
        maximizer.vectorAngleMax = Math.min(p.vectorAngle + 15, 90); //this is to speed things up
        maximizer.maximize();
        if (p.twoPlayerMode || maximizer.isDiveCapBouncePossible(-1, singleThrowAllowed, false, ttAllowed != YNT.YES, !singleThrowAllowed && ttAllowed != YNT.YES, ttAllowed != YNT.NO) > -1) { //also conforms the motion correctly
            maximizer.recalculateDisps(true);
            maximizer.adjustToGivenAngle();
            return maximizer.bestDisp;
        }
        else {
            Debug.println(4, "Not actually possible");
            return 0.0;
        }
    }

	public void calcIMDisps(int frames, int coyoteFrames) {
        VectorCalculator.addPreset(p.midairPreset, false);
		p.initialFrames = frames - p.framesCrouch - p.framesMoonwalk - p.framesRun;
		if (p.coyoteType == CoyoteType.MOONWALK)
			p.framesMoonwalk = coyoteFrames;
		else if (p.coyoteType == CoyoteType.RUNNING)
			p.framesRun = coyoteFrames;
		VectorMaximizer maximizer;
		maximizer = VectorCalculator.calculate();

		double dispX, dispY, dispZ;
		dispX = dispY = dispZ = 0;
		for (int j = 0; j <= maximizer.listPreparer.initialMovementIndex; j++) {
			dispX += maximizer.motions[j].dispX;
			dispY += maximizer.motions[j].calcDispY();
			dispZ += maximizer.motions[j].dispZ;
		};
		double targetAngle = Math.atan(maximizer.bestDispX / maximizer.bestDispZ);
		double coordAngle = Math.atan(dispX / dispZ);
		imForwardDisp = Math.abs(Math.sqrt(dispX * dispX + dispZ * dispZ) * Math.cos(targetAngle - coordAngle));;
		imYDisp = dispY;
	}

	public static DispData getDispData(int component, int minFrames, int maxFrames) {
        Properties p = Properties.getInstance();
        VectorCalculator.addPreset(p.midairPreset, false);
        double[] forwardDisps = new double[maxFrames + 1];
        double[] yDisps = new double[maxFrames + 1];
        for (int i = minFrames; i <= maxFrames; i++) {
			p.midairs[p.componentIndices[component]][1] = i;

            VectorMaximizer maximizer;
            maximizer = VectorCalculator.calculate();

			int maximizerStartIndex = maximizer.listPreparer.startIndices[component];
			int maximizerEndIndex = maximizer.listPreparer.endIndices[component];

            double dispX, dispY, dispZ;
            dispX = dispY = dispZ = 0;
            for (int j = maximizerStartIndex; j <= maximizerEndIndex; j++) {
                dispX += maximizer.motions[j].dispX;
                dispY += maximizer.motions[j].calcDispY();
                dispZ += maximizer.motions[j].dispZ;
            };
            double targetAngle = Math.atan(maximizer.bestDispX / maximizer.bestDispZ);
            double coordAngle = Math.atan(dispX / dispZ);
            double forwardDisp = Math.abs(Math.sqrt(dispX * dispX + dispZ * dispZ) * Math.cos(targetAngle - coordAngle));;
            forwardDisps[i] = forwardDisp;
            yDisps[i] = dispY;
        }
        return new DispData(p.initialMovementName, DispData.DEFAULT, forwardDisps, yDisps);
    }

	public static DispData getDispDataHCT(int minFrames, int maxFrames) {
        Properties p = Properties.getInstance();
        VectorCalculator.addPreset(p.midairPreset, false);
        double[] forwardDisps = new double[maxFrames + 1];
        double[] yDisps = new double[maxFrames + 1];
        for (int i = minFrames; i <= maxFrames; i++) {
			p.midairs[p.componentIndices[Movement.HCT]][1] = i;

            VectorMaximizer maximizer;
            maximizer = VectorCalculator.calculate();

			int maximizerStartIndex = maximizer.listPreparer.startIndices[Movement.HCT];
			int maximizerEndIndex = maximizer.listPreparer.endIndices[Movement.RS];

            double dispX, dispY, dispZ;
            dispX = dispY = dispZ = 0;
            for (int j = maximizerStartIndex; j <= maximizerEndIndex; j++) {
                dispX += maximizer.motions[j].dispX;
                dispY += maximizer.motions[j].calcDispY();
                dispZ += maximizer.motions[j].dispZ;
            };
            double targetAngle = Math.atan(maximizer.bestDispX / maximizer.bestDispZ);
            double coordAngle = Math.atan(dispX / dispZ);
            double forwardDisp = Math.abs(Math.sqrt(dispX * dispX + dispZ * dispZ) * Math.cos(targetAngle - coordAngle));;
            forwardDisps[i] = forwardDisp;
            yDisps[i] = dispY;
        }
        return new DispData(p.initialMovementName, DispData.DEFAULT, forwardDisps, yDisps);
    }

	@Override
	public String getError() {
		return error;
	}

	@Override
	public double getBestDisp() {
		return bestDisp;
	}

	@Override
	public boolean solveSuccess() {
		return success;
	}

	@Override
	public boolean singleThrowAllowed() {
		return singleThrowAllowed;
	}

	@Override
	public boolean mcctAllowed() {
		return true;
	}

	@Override
	public YNT ttAllowed() {
		return ttAllowed;
	}

	@Override
	public VectorMaximizer getMaximizer() {
		return maximizer;
	}

	public class Candidate implements Comparable<Candidate> {
		int totalFrames;
		int imFrames;
		int[][] midairs;
		int coyoteFrames;
		int framesJump;
		double forwardDisp;
		double yDisp;

		public Candidate(int totalFrames, int imFrames, int[][] midairs, int coyoteFrames, int framesJump, double forwardDisp, double yDisp) {
			this.totalFrames = totalFrames;
			this.imFrames = imFrames;
			this.midairs = midairs;
			this.coyoteFrames = coyoteFrames;
			this.framesJump = framesJump;
			this.forwardDisp = forwardDisp;
			this.yDisp = yDisp;
		}

		@Override
		public String toString() {
			int[] midairDurations = new int[midairs.length];
			for (int i = 0; i < midairs.length; i++) {
				midairDurations[i] = midairs[i][1];
			}
			return totalFrames + ", " + Arrays.toString(midairDurations) + ", " + forwardDisp + ", " + yDisp;
		}

		@Override 
		public int compareTo(Candidate other) {
			if (this.totalFrames != other.totalFrames)
				return this.totalFrames - other.totalFrames;
			else
				return this.forwardDisp < other.forwardDisp ? -1 : 1;
		}
	}
}