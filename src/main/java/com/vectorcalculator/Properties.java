package com.vectorcalculator;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude = {"file", "currentTab"})
@XmlRootElement
public class Properties {
    static Properties p;
    static Properties p_saved;

    @XmlTransient
    File file = null;

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

    // static enum AngleType {
	// 	INITIAL, TARGET, BOTH
	// }

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
        SOLVE("Solve"), SOLVE_CB("Calculate (Solve Cap Bounce)"), CALCULATE("Calculate");

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

    // static enum LockDurationType {
    //     NONE, FRAMES, VERTICAL_DISPLACEMENT
    // }

    double x0 = 0, y0 = 0, z0 = 0;
	double x1 = 0, y1 = 0, z1 = 3000;
	boolean targetCoordinates = true;
	double initialAngle = 0;
	double targetAngle = 90;
    CalculateUsing calculateUsing = CalculateUsing.TARGET_COORDINATES;
	//AngleType angleType = AngleType.TARGET;
	String initialMovementName = "Triple Jump";
	boolean chooseDurationType = true;
	boolean durationFrames = true;
	int initialFrames = 70;
	double initialDispY = 0;
	int framesJump = 10;
	boolean canMoonwalk = true;
	int framesMoonwalk = 0;
    boolean chooseInitialHorizontalSpeed = true;
	double initialHorizontalSpeed = 24;
	boolean rightVector = false;

    boolean diveCapBounce = true;
	double diveCapBounceAngle = 0; //how many more degrees the cap throw should be to the side than the dive angle
    double diveCapBounceTolerance = 0.02; //how much flexibility there is in the dive cap bounce working
    double diveFirstFrameDecel = 0; //how much to decelerate on the first frame of the dive before the cap bounce
    boolean diveTurn = true;

	String midairPreset = "Spinless";
    boolean canTripleThrow = true;
    boolean canTestTripleThrow = true; //whether the option Test Both is shown
    TripleThrow tripleThrow = TripleThrow.NO;
	boolean onMoon = false;
	boolean hyperoptimize = true;
	boolean xAxisZeroDegrees = true;
	CameraType cameraType = CameraType.TARGET;
	double customCameraAngle = 0;
    int[][] midairs = new int[][] { { VectorCalculator.MCCT, 31 }, { VectorCalculator.DIVE, 25 },
            { VectorCalculator.CB, 43 }, { VectorCalculator.MCCT, 31 }, { VectorCalculator.DIVE, 25 } };
    int scriptType = VectorDisplayWindow.NX_TAS;
    String scriptPath = "";

    //static final double NO_GROUND = -1000000;

    //new experimental settings
    double groundHeight = 0;
    double groundHeightFirstGP = 0;
    double groundHeightCB = 0;
    double groundHeightSecondGP = 0;
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

    //boolean isLavaOrPoison = false;
    double upwarp = 40;
    Mode mode = Mode.SOLVE;

    @XmlTransient
    int currentTab = 0;

    String initialMovementCategory = "Jump";
    GroundMode groundMode = GroundMode.NONE;
	boolean chooseJumpFrames = true;

    //select the initial movement once these properties are saved

    public static boolean save(File file) {
        try {
            JAXBContext jxbc = JAXBContext.newInstance(Properties.class);
            Marshaller m = jxbc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            //File f = new File("properties.xml");
            //p.file = file;
            m.marshal(p, file);
            return true;
        }
        catch (Exception ex) {
            Debug.println("XML Save Failed");
            return false;
        }
    }

    public static Properties load(File file) {
        try {
            JAXBContext jxbc = JAXBContext.newInstance(Properties.class);
            Unmarshaller um = jxbc.createUnmarshaller();
            //um.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            //File f = new File("properties.xml");
            //p.file = file;
            p_saved = (Properties) um.unmarshal(file);
            return p_saved;
            //System.out.println(p.x0);
        }
        catch (Exception ex) {
            System.out.println(ex);
            Debug.println("XML Load Failed");
            return null;
        }
    }

    public static boolean isUnsaved() {
        return !p.equals(p_saved);
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