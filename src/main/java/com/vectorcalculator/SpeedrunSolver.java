package com.vectorcalculator;

import java.util.ArrayList;
import java.util.Arrays;

import com.vectorcalculator.Properties.TripleThrow;

//this class finds the optimal durations for each midair input, given the target vertical displacement
public class SpeedrunSolver implements SolverInterface {
	Properties p = Properties.p;
	VectorMaximizer maximizer;
	boolean diveTurn = true;
	boolean singleThrowAllowed = true;
	TripleThrow ttAllowed = TripleThrow.NO;
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

		//DispData imData = DispData.getDispData("Vault", DispData.DEFAULT);
		DispDataCTDive ctDiveData = (DispDataCTDive) DispData.getDispData("MCCT Dive", DispData.DEFAULT);
		DispData cbData = DispData.getDispData("Dive Cap Bounce", DispData.DEFAULT);
		DispData diveData = DispData.getDispData("Final Dive", DispData.DEFAULT);
		DispData fctData = DispData.getDispData("Final Cap Throw", DispData.DEFAULT);

		ArrayList<Candidate> candidates = new ArrayList<Candidate>();

		VectorCalculator.setProgressText("Solver: Testing Permutations");
		
		int maxFrames = Integer.MAX_VALUE;

		double ctDiveDataMaxYDisp = ctDiveData.maxYDisp();
		double cbDataMaxYDisp = cbData.maxYDisp();
		double diveDataMaxYDisp = diveData.maxYDisp();
		double fctDataMaxYDisp = fctData.maxYDisp();

		double lowestYDisp = p.y1 - Solver.ERROR - p.getUpwarpMinusError() - p.y0; //lowest yDisp that will make the movement land
		double highestYDisp = p.y1 - p.y0 + Solver.ERROR; //highest yDisp that will make the movement land

		int imMinFrames = p.framesCrouch + p.framesMoonwalk + p.framesRun + 1;
		int cbMinFrames = 1;
		int diveMinFrames = 14;
		int fctMinFrames = 0;
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
		int totalFrames = minFrames;

		int minCoyoteFrames = 0;
		int maxCoyoteFrames = p.getCoyoteTimeMaxFrames();

		//for (int coyoteFrames = minCoyoteFrames; coyoteFrames <= maxCoyoteFrames; coyoteFrames++) {
			for (int imFrames = imMinFrames; totalFrames <= maxFrames; imFrames++, totalFrames++) { //TODO: start at a more reasonable value (max height probably)
				double prevImForwardDisp = imForwardDisp;
				double prevImYDisp = imYDisp;
				calcIMDisps(imFrames);
				double maxYDisp = imYDisp + ctDiveDataMaxYDisp + cbDataMaxYDisp + diveDataMaxYDisp + fctDataMaxYDisp;
				if (maxYDisp < lowestYDisp)
					break; //if it is impossible to get high enough, break now
				double maxYDisp2 = maxYDisp - cbDataMaxYDisp;
				int totalFrames2 = totalFrames;
				double forwardDisp2 = imForwardDisp;

				//calculate efficiency of last frame of IM
				double yVel = imYDisp - prevImYDisp;
				double forwardDelta = imForwardDisp - prevImForwardDisp;
				double imEfficiency;
				if (yVel >= 0)
					imEfficiency = 2;
				else
					imEfficiency = -1 / ((yVel / forwardDelta) - 1);

				System.out.println("IM: " + imForwardDisp + ", " + imYDisp + ", " + imEfficiency);

				cbMaxFrames = cbData.maxFrames(imEfficiency);
				diveMaxFrames = diveData.maxFrames(imEfficiency);
				//fctMaxFrames = fctData.maxFrames(imEfficiency);
				System.out.println(" CB Max Frames: " + cbMaxFrames);
				System.out.println(" Dive Max Frames: " + diveMaxFrames);

				for (int cbFrames = cbMinFrames; cbFrames <= cbMaxFrames && totalFrames2 <= maxFrames; cbFrames++, totalFrames2++) {
					fctMaxFrames = fctData.maxFrames(cbData.efficiencies[cbFrames]); //doesn't really help because it doesn't tend to get this long
					//System.out.println(" FCT Max Frames: " + fctMaxFrames);

					if (maxYDisp2 + cbYDisps[cbFrames] < lowestYDisp)
						break;
					double maxYDisp3 = maxYDisp2 + cbYDisps[cbFrames] - diveDataMaxYDisp;
					int totalFrames3 = totalFrames2;
					double forwardDisp3 = forwardDisp2 + cbForwardDisps[cbFrames];

					for (int diveFrames = diveMinFrames; diveFrames <= diveMaxFrames && totalFrames3 <= maxFrames; diveFrames++, totalFrames3++) {
						if (maxYDisp3 + diveYDisps[diveFrames] < lowestYDisp)
							break;
						double maxYDisp4 = maxYDisp3 + diveYDisps[diveFrames] - fctDataMaxYDisp;
						int totalFrames4 = totalFrames3;
						double forwardDisp4 = forwardDisp3 + diveFrames * 20; //TODO allow for diveangle

						for (int fctFrames = fctMinFrames; fctFrames <= fctMaxFrames && totalFrames4 <= maxFrames; fctFrames++, totalFrames4++) {
							if (fctFrames > 0 && fctFrames < 8)
								continue;
							if (maxYDisp4 + fctYDisps[fctFrames] < lowestYDisp)
								break;
							double maxYDisp5 = maxYDisp4 + fctYDisps[fctFrames] - ctDiveDataMaxYDisp;
							int totalFrames5 = totalFrames4;
							double forwardDisp5 = forwardDisp4 + fctForwardDisps[fctFrames];

							for (int ctDiveDataIndex = 0; ctDiveDataIndex < ctDiveData.data.length; ctDiveDataIndex++) {
								totalFrames5 = totalFrames4 + ctDiveData.frames(ctDiveDataIndex) - ctDiveMinFrames;
								if (totalFrames5 > maxFrames)
									continue;
								double yDisp = maxYDisp5 + ctDiveYDisps[ctDiveDataIndex];
								if (yDisp > highestYDisp || yDisp < lowestYDisp) {
									continue;
								}
								double forwardDisp = forwardDisp5 + ctDiveForwardDisps[ctDiveDataIndex];
								if (forwardDisp >= targetDisp - RANGE) {
									int durations[] = {imFrames, ctDiveData.ctFrames(ctDiveDataIndex), ctDiveData.diveFrames(ctDiveDataIndex), cbFrames, fctFrames, diveFrames};
									candidates.add(new Candidate(totalFrames5, durations, forwardDisp, yDisp));
									if (forwardDisp >= targetDisp + RANGE)
										maxFrames = totalFrames5;
								}
							}
						}
					}
				}
			}
		//}
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
				p.initialFrames = c.durations[0];
				int eliminatedMovements = 0;
				VectorCalculator.addPreset("Spinless", false);
				for (int i = 1; i < c.durations.length; i++) {
					p.midairs[i - 1][1] = c.durations[i];
					if (c.durations[i] == 0)
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
			p.initialFrames = bestCandidate.durations[0];
			int eliminatedMovements = 0;
			VectorCalculator.addPreset("Spinless", false);
			for (int i = 1; i < bestCandidate.durations.length; i++) {
				p.midairs[i - 1][1] = bestCandidate.durations[i];
				if (bestCandidate.durations[i] == 0)
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
        if (p.twoPlayerMode || maximizer.isDiveCapBouncePossible(-1, singleThrowAllowed, false, ttAllowed != TripleThrow.YES, !singleThrowAllowed && ttAllowed != TripleThrow.YES, ttAllowed != TripleThrow.NO) > -1) { //also conforms the motion correctly
            maximizer.recalculateDisps(true);
            maximizer.adjustToGivenAngle();
            return maximizer.bestDisp;
        }
        else {
            Debug.println(4, "Not actually possible");
            return 0.0;
        }
    }

	public void calcIMDisps(int frames) {
        VectorCalculator.addPreset(p.midairPreset, false);
		p.initialFrames = frames - p.framesCrouch - p.framesMoonwalk - p.framesRun;
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
	public TripleThrow ttAllowed() {
		return ttAllowed;
	}

	@Override
	public VectorMaximizer getMaximizer() {
		return maximizer;
	}

	public class Candidate implements Comparable<Candidate> {
		int totalFrames;
		int[] durations;
		double forwardDisp;
		double yDisp;

		public Candidate(int totalFrames, int[] durations, double forwardDisp, double yDisp) {
			this.totalFrames = totalFrames;
			this.durations = durations;
			this.forwardDisp = forwardDisp;
			this.yDisp = yDisp;
		}

		@Override
		public String toString() {
			return totalFrames + ", " + Arrays.toString(durations) + ", " + forwardDisp + ", " + yDisp;
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