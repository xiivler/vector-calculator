package vectorCalc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptWriter {

    public void writeTSV() {
        PrintWriter print = null;

        try {
            File destination = new File("vector.tsv");
            print = new PrintWriter(destination);
        }
        catch (FileNotFoundException e) {
        }


    }
}