package com.vectorcalculator;

class Inputs {
    public static final int NONE = 0, A = 1, B = 2, X = 3, Y = 4, ZL = 5, ZR = 6, M = 7, MU = 8, MD = 9, ML = 10, MR = 11, MUU = 12, MDD = 13, MLL = 14, MRR = 15, P2A = 16, P2B = 17, P2X = 18, P2Y = 19, P2ZL = 20, P2ZR = 21;
    public static final String[] displayInputs = {"", "A", "B", "X", "Y", "ZL", "ZR", "Shake", "Shake", "Shake", "Shake", "Shake", "Up shake", "Down shake", "Side shake", "Side shake", "P2 A", "P2 B", "P2 X", "P2 Y", "P2 ZL", "P2 ZR"};
    public static final String[] TSVInputs = {"", "a", "b", "x", "y", "zl", "zr", "m", "m-u", "m-d", "m-l", "m-r", "m-uu", "m-dd", "m-ll", "m-rr", "ca", "cb", "cx", "cy", "czl", "czr"};
    public static final String[] nxTASInputs = {"NONE", "KEY_A", "KEY_B", "KEY_X", "KEY_Y", "KEY_ZL", "KEY_ZR", "KEY_L", "KEY_L;KEY_DUP", "KEY_L;KEY_DDOWN", "KEY_L;KEY_DLEFT", "KEY_L;KEY_DRIGHT", "KEY_DUP", "KEY_DDOWN", "KEY_DLEFT", "KEY_DRIGHT", "KEY_A", "KEY_B", "KEY_X", "KEY_Y", "KEY_ZL", "KEY_ZR"};

    int input1, input2;
    double r, theta;
    double P2_r, P2_theta;

    public Inputs() {
        this.input1 = Inputs.NONE;
        this.input2 = Inputs.NONE;
        this.r = 0;
        this.theta = SimpleMotion.NO_ANGLE;
        this.P2_r = 0;
        this.P2_theta = SimpleMotion.NO_ANGLE;
    }

    public Inputs(double r, double theta) {
        this.input1 = Inputs.NONE;
        this.input2 = Inputs.NONE;
        this.r = r;
        this.theta = theta;
        this.P2_r = 0;
        this.P2_theta = SimpleMotion.NO_ANGLE;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Inputs)) {
            return false;
        }
        Inputs i = (Inputs) o;
        if (input1 == i.input1 && input2 == i.input2 && r == i.r && theta == i.theta && P2_r == i.P2_r && P2_theta == i.P2_theta) {
            return true;
        }
        return false;
    }

    public String joystickToTSV(boolean P2) {
        double r = P2 ? P2_r : this.r;
        double theta = P2 ? P2_theta : this.theta;
        if (theta != SimpleMotion.NO_ANGLE) {
            if (r == 1) {
				if (Math.round(theta) * 10000 == Math.round(theta * 10000)) { //check if equal with 4 decimal places
					return String.format((P2 ? "c" : "") + "ls(%d)", Math.round(theta));
				}
				else {
					return String.format((P2 ? "c" : "") + "ls(%.4f)", theta);
				}
			}
			else {
				if (Math.round(theta) * 10000 == Math.round(theta * 10000)) {
					return String.format((P2 ? "cls(%.4f; %d)" : "ls(%.2f; %d)"), r, Math.round(theta));
				}
				else {
					return String.format((P2 ? "cls(%.4f; %.4f)" : "ls(%.2f; %.4f)"), r, theta);
				}
			}
        }
        return "";
    }

    public String toTSV() {
        String joystickString = joystickToTSV(false);
        if (P2_theta != SimpleMotion.NO_ANGLE)
            joystickString += "\t" + joystickToTSV(true);
        // if (theta != SimpleMotion.NO_ANGLE) {
        //     if (r == 1) {
		// 		if (Math.round(theta) * 10000 == Math.round(theta * 10000)) { //check if equal with 4 decimal places
		// 			joystickString = String.format("ls(%d)", Math.round(theta));
		// 		}
		// 		else {
		// 			joystickString = String.format("ls(%.4f)", theta);
		// 		}
		// 	}
		// 	else {
		// 		if (Math.round(theta) * 10000 == Math.round(theta * 10000)) {
		// 			joystickString = String.format("ls(%.2f; %d)", r, Math.round(theta));
		// 		}
		// 		else {
		// 			joystickString = String.format("ls(%.2f; %.4f)", r, theta);
		// 		}
		// 	}
        // }
        return TSVInputs[input1] + "\t" + TSVInputs[input2] + "\t" + joystickString;
    }

    public String toNXTAS() {
        String joystickString = "0;0";
        if (theta != SimpleMotion.NO_ANGLE) {
            if (Math.round(theta) * 10000 == Math.round(theta * 10000)) {
                theta = Math.round(theta);
            }
            int x = (int) (32767 * r * Math.cos(Math.toRadians(theta)));
            int y = (int) (32767 * r * Math.sin(Math.toRadians(theta)));
            joystickString = x + ";" + y;
        }
        if (input2 == NONE) {
            return nxTASInputs[input1] + " " + joystickString + " 0;0";
        }
        else {
            return nxTASInputs[input1] + ";" + nxTASInputs[input2] + " " + joystickString + " 0;0";
        }
    }

    public static boolean isMotion(int input) {
        return input >= M && input < P2A;
    }

    public static boolean isP2(int input) {
        return input >= P2A;
    }
}