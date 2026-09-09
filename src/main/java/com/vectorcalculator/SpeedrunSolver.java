package com.vectorcalculator;

import com.vectorcalculator.Properties.TripleThrow;

//this class finds the optimal durations for each midair input, given the target vertical displacement
public class SpeedrunSolver implements SolverInterface {
	@Override
	public boolean solve(int delta) {
		return false;
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
}