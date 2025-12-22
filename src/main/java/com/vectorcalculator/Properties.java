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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement
public class Properties {
    static Properties p;

    public static Properties getInstance() {
        if (p == null) {
            p = new Properties();
        }
        return p;
    }

    static enum AngleType {
		INITIAL, TARGET, BOTH
	}

	static enum CameraType {
		INITIAL, TARGET, ABSOLUTE, CUSTOM
	}

    double x0 = 0, y0 = 0, z0 = 0;
	double x1 = 0, y1 = 0, z1 = 3000;
	boolean targetCoordinates = true;
	double initialAngle = 0;
	double targetAngle = 90;
	AngleType angleType = AngleType.TARGET;
	String initialMovementName = "Triple Jump";
	boolean durationFrames = true;
	int initialFrames = 70;
	double initialDispY = 0;
	int framesJump = 10;
	boolean canMoonwalk = true;
	int framesMoonwalk = 0;
	double initialHorizontalSpeed = 24;
	boolean rightVector = false;
	double diveCapBounceAngle = 0; //how many more degrees the cap throw should be to the side than the dive angle
	int currentPresetIndex = 1;
	boolean onMoon = false;
	boolean hyperoptimize = true;
	boolean xAxisZeroDegrees = true;
	CameraType cameraType = CameraType.TARGET;
	double customCameraAngle = 0;
    int[][] midairs;

    //select the initial movement once these properties are saved

    public static void save() {
        try {
            JAXBContext jxbc = JAXBContext.newInstance(Properties.class);
            Marshaller m = jxbc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            File f = new File("properties.xml");
            m.marshal(p, f);
        }
        catch (JAXBException ex) {
            Debug.println("XML Save Failed");
        }
    }

    public static Properties load() {
        try {
            JAXBContext jxbc = JAXBContext.newInstance(Properties.class);
            Unmarshaller um = jxbc.createUnmarshaller();
            //um.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            File f = new File("properties.xml");
            return (Properties) um.unmarshal(f);
            //System.out.println(p.x0);
        }
        catch (JAXBException ex) {
            Debug.println("XML Load Failed");
            return null;
        }
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