package util.logger;

import java.io.*;

public class Logger {
    public static Logger logger;
    static String address = "log.txt";
    private FileOutputStream fOut;
    private PrintStream ps;
    private String name;

    public void setName(String name){
        this.name = name;
    }

    public static void setLogger(){
        try {
            logger = new Logger();
        } catch (FileNotFoundException e) {
            System.exit(-1);
        }
    }
    private Logger() throws FileNotFoundException {
        fOut =new FileOutputStream(new File(address),true);
        ps = new PrintStream(fOut);
    }
    public void write(String message){
        //ps.println(name + " : " +message);
        //ps.flush();
    }

    public void clearLog(){
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(address);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        writer.println();
        writer.close();
    }

    public void flush(){
        ps.flush();
        ps.close();
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
