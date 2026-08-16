package com.vectorcalculator;

//calculates the behavior of vectorable movement if the joystick angle is consistent
public class UpwarpMaximizer {

	double vy1 = 0; //y speed on frame of upwarp
	double vf = 0; //velocity in direction of ledge
	double rot = 0; //mario's rotation relative to the ledge
	double dland = 30; //minimum distance needed to land

	double best = 0; //best possible upwarp
	double best_d1 = 55; //distance the frame before the best possible upwarp

	// public static void main(String[] args) {
	// 	UpwarpMaximizer um = new UpwarpMaximizer(-1.7, 2, Math.PI / 6, Math.PI / 6 + Math.PI);
	// 	System.out.println(um.maximize());
	// }

	public UpwarpMaximizer(double vy1, double vf, double rot) {
		this.vy1 = vy1;
		this.vf = vf;
		this.rot = rot;
		while (rot < 0)
			rot += Math.PI * 2;
		dland = 30 * Math.cos(((rot + Math.PI / 3) % (2.0 / 3.0 * Math.PI) - Math.PI / 3));
	}

	//vh is horizontal velocity
	//va is velocity angle relative to ledge (0 is perpendicular to ledge)
	public UpwarpMaximizer(double vy1, double vh, double va, double rot) {
		this(vy1, vh * Math.cos(va), rot);
	}

	public double maximize() {
		double low = 0;
		double high = 40;
		double mid = 40;
		best = 0;
		best_d1 = 55;
	
		while (high - low > .001) {
			mid = (high + low) / 2;
			if ((boolean) willUpwarp(mid)[0]) {
				low = mid;
				high = high;
				best = mid;
				best_d1 = (double) willUpwarp(mid)[1];
			}
			else {
				low = low;
				high = mid;
			}
		}
		//console.log(best);
		//return new double[]{best, best_d1};
		return best;
	}

	private Object[] willUpwarp(double uw) {
		if (uw >= 40) {
        	return new Object[]{false, -Double.MAX_VALUE};
		}
		double h0 = uw + vy1; //height of ledge above Mario the frame before upwarp
		double hdiff = 65 - h0;
		double d0;
		if (h0 < 10) {
			d0 = dland;
		}
		else if (h0 > 55) {
			d0 = 55;
		}
		else {
			d0 = Math.max(dland, Math.sqrt(55 * 55 - hdiff * hdiff)); //closest Mario can be on frame before upwarp
		}
		double d1 = d0 - vf;
		return new Object[]{d1 <= dland, d0};
	}

}

