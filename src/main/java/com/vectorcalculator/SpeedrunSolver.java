package com.vectorcalculator;

import com.vectorcalculator.Properties.TripleThrow;

//this class finds the optimal durations for each midair input, given the target vertical displacement
public class SpeedrunSolver implements SolverInterface {
	@Override
	public boolean solve(int delta) {
		//iterate through IM lengths
			//iterate through CT-dive lengths
				//iterate through CB lengths
					//iterate through final dive lengths
						//accept if forwarddisp is as (almost) much as requested and vertical disp + UW is as much as requested
							//test candidates from least to most total frames ACCURATELY (stop at the end of testing all candidates for a certain number of frames when you have found one that works)
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