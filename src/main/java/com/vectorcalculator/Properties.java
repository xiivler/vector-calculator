package com.vectorcalculator;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.vectorcalculator.VectorCalculator.Parameter;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude = {"currentTab", "savedInfoTableRows", "savedDataTableRows", "selectedParam", "movementSelectedRow", "movementSelectedCol"})
@XmlRootElement
public class Properties {
    static Properties p; //the current properties
    static Properties p_saved; //properties that are currently saved
    static Properties p_toSave; //properties that should be saved
    static Properties p_calculated; //properties present the last time a calculation was run (solve/calculate/etc.)

    public static Properties getInstance() {
        if (p == null) {
            p = new Properties();
        }
        return p;
    }

    static enum CalculateUsing {
		INITIAL_ANGLE("Initial Angle"), TARGET_ANGLE("Target Angle"), TARGET_COORDINATES("Target Coordinates");

        String name;

        CalculateUsing(String string) {
            this.name = string;
        }

        static CalculateUsing fromName(String string) {
            for (CalculateUsing cu : CalculateUsing.values()) {
                if (cu.name.equals(string)) {
                    return cu;
                }
            }
            return TARGET_COORDINATES;
        }
	}

    static enum TripleThrow {
        YES("Yes"), NO("No"), TEST("Test Both");

        String displayName;

        TripleThrow(String displayName) {
            this.displayName = displayName;
        }

        static TripleThrow fromDisplayName(String name) {
            for (TripleThrow tt : TripleThrow.values()) {
                if (tt.displayName.equals(name)) {
                    return tt;
                }
            }
            return NO;
        }
    }

    static enum TurnDuringDive {
        YES("Yes"), NO("No"), TEST("Test Both");

        String displayName;

        TurnDuringDive(String displayName) {
            this.displayName = displayName;
        }

        static TurnDuringDive fromDisplayName(String name) {
            for (TurnDuringDive dt : TurnDuringDive.values()) {
                if (dt.displayName.equals(name)) {
                    return dt;
                }
            }
            return YES;
        }
    }

	static enum CameraType {
		INITIAL("Initial Angle"), TARGET("Target Angle"), ABSOLUTE("Absolute"), CUSTOM("Custom");

        String name;

        CameraType(String string) {
            this.name = string;
        }

        static CameraType fromName(String string) {
            for (CameraType ct : CameraType.values()) {
                if (ct.name.equals(string)) {
                    return ct;
                }
            }
            return INITIAL;
        }
	}

    static enum Mode {
        SOLVE("Solve"), SOLVE_DIVES("Calculate (Solve Dives)"), CALCULATE("Calculate");

        String name;

        Mode(String string) {
            this.name = string;
        }

        static Mode fromName(String string) {
            for (Mode mode : Mode.values()) {
                if (mode.name.equals(string)) {
                    return mode;
                }
            }
            return SOLVE;
        }
    }

    static enum GroundMode {
        NONE("None"), UNIFORM("Uniform"), VARIED("Varied");

        String name;

        GroundMode(String string) {
            this.name = string;
        }

        static GroundMode fromName(String string) {
            for (GroundMode gm : GroundMode.values()) {
                if (gm.name.equals(string)) {
                    return gm;
                }
            }
            return NONE;
        }
    }

    static enum GroundType {
        NONE("None"), GROUND("Ground"), DAMAGING("Lava/Poison");

        String name;

        GroundType(String string) {
            this.name = string;
        }

        static GroundType fromName(String string) {
            for (GroundType gt : GroundType.values()) {
                if (gt.name.equals(string)) {
                    return gt;
                }
            }
            return NONE;
        }
    }

    static enum HctDirection {
        UP("Up"), DOWN("Down"), LEFT("Left"), RIGHT("Right");

        String name;

        HctDirection(String string) {
            this.name = string;
        }

        static HctDirection fromName(String string) {
            for (HctDirection hd : HctDirection.values()) {
                if (hd.name.equals(string)) {
                    return hd;
                }
            }
            return DOWN;
        }
    }

    static enum HctType {
        RELAX("Relax"), RELAXLESS("Relaxless"), CUSTOM("Custom");

        String name;

        HctType(String string) {
            this.name = string;
        }

        static HctType fromName(String string) {
            for (HctType ht : HctType.values()) {
                if (ht.name.equals(string)) {
                    return ht;
                }
            }
            return RELAX;
        }

        static String[] names = new String[HctType.values().length];
        static {
            for (int i = 0; i < HctType.values().length; i++)
                names[i] = HctType.values()[i].name;
        }
    }

    Mode mode = Mode.SOLVE;

    double x0 = 0, y0 = 0, z0 = 0;
	double x1 = 0, y1 = 0, z1 = 4000;

    boolean solveForInitialAngle = false;
	double initialAngle = 90;
	double targetAngle = 90;
    CalculateUsing calculateUsing = CalculateUsing.TARGET_COORDINATES;

    String initialMovementCategory = "Jump";
	String initialMovementName = "Triple Jump";
	boolean chooseDurationType = false;
	boolean durationFrames = true;
	int initialFrames = 70;
	double initialDispY = 0;
    int vaultCapReturnFrame = 28;
    boolean chooseJumpFrames = true;
	int framesJump = 10;
	boolean canMoonwalk = true;
	int framesMoonwalk = 5;
    boolean chooseInitialHorizontalSpeed = true;
	double initialHorizontalSpeed = 24;
	boolean rightVector = false;

    boolean diveCapBounce = true;
	double diveCapBounceAngle = 0; //how many more degrees the cap throw should be to the side than the dive angle
    double diveCapBounceTolerance = 0.01; //how much flexibility there is in the dive cap bounce working
    double diveFirstFrameDecel = 0; //how much to decelerate on the first frame of the dive before the cap bounce
    TurnDuringDive diveTurn = TurnDuringDive.YES;
    int cbCapReturnFrame = 25;

	String midairPreset = "MCCT First";
    boolean canTripleThrow = true;
    boolean canTestTripleThrow = false; //whether the option Test Both is shown
    TripleThrow tripleThrow = TripleThrow.YES;
    int firstCTIndex = 0;
    boolean turnarounds = true;
    double upwarp = 40;

    int durationSearchRange = 4;

	boolean onMoon = false;

	boolean xAxisZeroDegrees = true;

	CameraType cameraType = CameraType.ABSOLUTE;
	double customCameraAngle = 0;
    int[][] midairs;

    int scriptType = VectorDisplayWindow.TSV_TAS;
    String scriptPath = "";

    double groundHeight = 0;
    double groundHeightFirstGP = 0;
    double groundHeightCB = 0;
    double groundHeightSecondGP = 0;
    GroundMode groundMode = GroundMode.NONE;
    GroundType groundType = GroundType.NONE;
    GroundType groundTypeFirstGP = GroundType.NONE;
    GroundType groundTypeCB = GroundType.NONE;
    GroundType groundTypeSecondGP = GroundType.NONE;
    boolean hasgroundHeightFirstGP = false;
    boolean hasgroundHeightCB = false;
    boolean hasgroundHeightSecondGP = false;

    boolean hct = false;
    HctType hctType = HctType.RELAX;
    double hctThrowAngle = 60;
    boolean hctNeutralHoming = true;
    HctDirection hctDirection = HctDirection.DOWN;
    int hctHomingFrame = 19;
    int hctCapReturnFrame = 36;

    boolean initialAndTargetGiven = false;
    boolean initialAngleGiven = false;
    boolean targetAngleGiven = false;
    boolean targetCoordinatesGiven = true;

    //currently internal setting only
    boolean spreadOutOvershoot = false;

    //saving the VectorDisplayWindow tables
    String[] savedInfoTableRows = null;
    String[][] savedDataTableRows = null;

    //UI state
    @XmlTransient
    int currentTab = 0;

    @XmlTransient
    int lastEditTab = 0;

    @XmlTransient
    Parameter selectedParam = null;

    @XmlTransient
    int movementSelectedRow = -1;

    @XmlTransient
    int movementSelectedCol = -1;



    public static boolean save(File file, boolean defaults) {
        try {
            JAXBContext jxbc = JAXBContext.newInstance(Properties.class);
            Marshaller m = jxbc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            //File f = new File("properties.xml");
            m.marshal(p_toSave, file);
            if (!defaults) {
                p_saved = new Properties();
                Properties.copyAttributes(p, p_saved);
            }
            return true;
        }
        catch (Exception ex) {
            Debug.println("XML Save Failed");
            return false;
        }
    }

    public static Properties load(File file, boolean defaults) {
        try {
            JAXBContext jxbc = JAXBContext.newInstance(Properties.class);
            Unmarshaller um = jxbc.createUnmarshaller();
            Properties p_loaded = (Properties) um.unmarshal(file);
            if (!defaults)
                p_saved = p_loaded;
            return p_loaded;
        }
        catch (Exception ex) {
            Debug.println(ex);
            Debug.println("XML Load Failed");
            return null;
        }
    }

    public static boolean isSaved() {
        return p.equals(p_saved);
    }

    public static final double UPWARP_ERROR = 0.001; //this error should be greater than the one in the Solver so that it will not accidentally give too big of an upwarp

    public double getUpwarpMinusError() {
        return Math.max(upwarp - UPWARP_ERROR, 0);
    }

    public static void copyAttributes(Object from, Object to) {
        try {
            Map<String, Field> toFieldNameMap = new HashMap<>();
            for(Field f : to.getClass().getDeclaredFields()) {
                toFieldNameMap.put(f.getName(), f);
            }
            for(Field f : from.getClass().getDeclaredFields()) {
                Field ff = toFieldNameMap.get(f.getName());
                f.setAccessible(true);
                if(ff != null && ff.getType().equals(f.getType())) {
                    ff.setAccessible(true);
                    ff.set(to, f.get(from));
                }
            }
        }
        catch (IllegalAccessException ex) {
            Debug.println("Illegal access to Properties");
        }
    }
}