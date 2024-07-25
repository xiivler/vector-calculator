package vectorCalc;

public class Debug {
    
    public static boolean debug = false;

    public static void println(Object o) {
        if (debug) {
            System.out.println(o);
        }
    }

    public static void println() {
        if (debug) {
            System.out.println();
        }
    }

    public static void print(Object o) {
        if (debug) {
            System.out.print(o);
        }
    }
}