package com.vectorcalculator;

class Inputs {
    public static final int NONE = 0, A = 1, B = 2, X = 3, Y = 4, ZL = 5, ZR = 6, M = 7, MU = 8, MD = 9, ML = 10, MR = 11, MUU = 12, MDD = 13, MLL = 14, MRR = 15;
    public static final String[] displayInputs = {"", "A", "B", "X", "Y", "ZL", "ZR", "Shake", "Shake", "Shake", "Shake", "Shake", "Up shake", "Down shake", "Side shake", "Side shake"};
    public static final String[] TSVInputs = {"", "a", "b", "x", "y", "zl", "zr", "m", "m-u", "m-d", "m-l", "m-r", "m-uu", "m-dd", "m-ll", "m-rr"};
    public static final String[] nxTASInputs = {"NONE", "KEY_A", "KEY_B", "KEY_X", "KEY_Y", "KEY_ZL", "KEY_ZR", "KEY_L", "KEY_L;KEY_DUP", "KEY_L;KEY_DDOWN", "KEY_L;KEY_DLEFT", "KEY_L;KEY_DRIGHT", "KEY_DUP", "KEY_DDOWN", "KEY_DLEFT", "KEY_DRIGHT"};

    int input1, input2;
    double r, theta;

    public Inputs(int input1, int input2, double r, double theta) {
        this.input1 = input1;
        this.input2 = input2;
        this.r = r;
        this.theta = theta;
    }

    public Inputs() {
        this.input1 = Inputs.NONE;
        this.input2 = Inputs.NONE;
        this.r = 0;
        this.theta = SimpleMotion.NO_ANGLE;
    }

    public Inputs(double r, double theta) {
        this.input1 = Inputs.NONE;
        this.input2 = Inputs.NONE;
        this.r = r;
        this.theta = theta;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Inputs)) {
            return false;
        }
        Inputs i = (Inputs) o;
        if (input1 == i.input1 && input2 == i.input2 && r == i.r && theta == i.theta) {
            return true;
        }
        return false;
    }

    public String toTSV() {
        String joystickString = "";
        if (theta != SimpleMotion.NO_ANGLE) {
            if (r == 1) {
				if (Math.round(theta) * 10000 == Math.round(theta * 10000)) { //check if equal with 4 decimal places
					joystickString = String.format("ls(%d)", Math.round(theta));
				}
				else {
					joystickString = String.format("ls(%.4f)", theta);
				}
			}
			else {
				if (Math.round(theta) * 10000 == Math.round(theta * 10000)) {
					joystickString = String.format("ls(%.2f; %d)", r, Math.round(theta));
				}
				else {
					joystickString = String.format("ls(%.2f; %.4f)", r, theta);
				}
			}
        }
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
}