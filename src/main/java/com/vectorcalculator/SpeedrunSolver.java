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

	public static final double RANGE = 10; //how much less far should still be considered as a candidate

	@Override
	public boolean solve(int delta) {
		double xDiff = p.x1 - p.x0;
		double zDiff = p.z1 - p.z0;
		double targetDisp = Math.sqrt(xDiff * xDiff + zDiff * zDiff);

		DispData imData = DispData.getDispData("Vault", DispData.DEFAULT);
		DispDataCTDive ctDiveData = (DispDataCTDive) DispData.getDispData("MCCT Dive", DispData.DEFAULT);
		DispData cbData = DispData.getDispData("Dive Cap Bounce", DispData.DEFAULT);
		DispData diveData = DispData.getDispData("Final Dive", DispData.DEFAULT);

		ArrayList<Candidate> candidates = new ArrayList<Candidate>();
		
		for (int imFrames = 0; imFrames <= imData.maxFrames(); imFrames++) {
			for (int cbFrames = 1; cbFrames <= cbData.maxFrames(); cbFrames++) {
				for (int diveFrames = 14; diveFrames <= diveData.maxFrames(); diveFrames++) {
					for (int ctDiveDataIndex = 0; ctDiveDataIndex < ctDiveData.data.length; ctDiveDataIndex++) {
						double forwardDisp = imData.forwardDisps[imFrames] + cbData.forwardDisps[cbFrames] + diveData.forwardDisps[diveFrames] + ctDiveData.data[ctDiveDataIndex][3];
						double yDisp = imData.yDisps[imFrames] + cbData.yDisps[cbFrames] + diveData.yDisps[diveFrames] + ctDiveData.data[ctDiveDataIndex][4];
						double y1 = p.y0 + yDisp;
						if (y1 <= p.y1 + Solver.ERROR && y1 + p.getUpwarpMinusError() >= p.y1 - Solver.ERROR) {
							if (forwardDisp >= targetDisp - RANGE) {
								int totalFrames = imFrames + cbFrames + diveFrames + ctDiveData.frames(ctDiveDataIndex) + 2; //+2 for the two ground pounds
								int durations[] = {imFrames, ctDiveData.ctFrames(ctDiveDataIndex), ctDiveData.diveFrames(ctDiveDataIndex), cbFrames, diveFrames};
								candidates.add(new Candidate(totalFrames, durations, forwardDisp, yDisp));
							}
						}
					}
				}
			}
		}
		if (candidates.size() == 0) {
			System.out.println("No Solution Found");
			return false;
		}
		candidates.sort(null);
		int minTotalFrames = candidates.get(0).totalFrames;
		double bestDisp = 0;
		Candidate bestCandidate = null;
		for (Candidate c : candidates) {
			if (c.totalFrames == minTotalFrames || c.totalFrames > minTotalFrames && bestDisp < targetDisp) {
				if (c.totalFrames > minTotalFrames && bestDisp < targetDisp)
					minTotalFrames++;
				p.initialFrames = c.durations[0];
				for (int i = 1; i < c.durations.length; i++)
					p.midairs[i - 1][1] = c.durations[i];
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
			for (int i = 1; i < bestCandidate.durations.length; i++)
				p.midairs[i - 1][1] = bestCandidate.durations[i];
			VectorCalculator.addPreset(p.midairs);
			double disp = test();
			VectorDisplayWindow.generateData(maximizer);
			VectorDisplayWindow.display();
			return true;
		}
		else {
			System.out.println("No Solution Found");
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

	@Override
	public String getError() {
		return "";
	}

	@Override
	public double getBestDisp() {
		return 0;
	}

	@Override
	public boolean solveSuccess() {
		return false;
	}

	@Override
	public boolean singleThrowAllowed() {
		return false;
	}

	@Override
	public boolean mcctAllowed() {
		return false;
	}

	@Override
	public TripleThrow ttAllowed() {
		return null;
	}

	@Override
	public VectorMaximizer getMaximizer() {
		return null;
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