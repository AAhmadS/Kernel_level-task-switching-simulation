import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Scanner;

public class main {
    public static void main(String[] args) throws FileNotFoundException {
//        Scanner scanner = new Scanner(System.in);
        Scanner scanner=new Scanner(new File("tests/input17.txt"));
        int argumentCount = scanner.nextInt();scanner.nextLine();
        String[] arguments = new String[argumentCount];
        for (int i =0;i<argumentCount;i++){
            arguments[i] = scanner.nextLine();
        }
        int port = scanner.nextInt();
        int workersCount = scanner.nextInt();scanner.nextLine();
        String protocol = scanner.nextLine();
        String deadLockProtocol = scanner.nextLine();
        int storagePort = scanner.nextInt();scanner.nextLine();
        String[] dat = scanner.nextLine().split(" ");
        int[] data = new int[dat.length];
        for (int i = 0; i < dat.length; i++) {
            data[i]=Integer.parseInt(dat[i]);
        }
        int taskNumbers = scanner.nextInt();scanner.nextLine();
        String[] tasks = new String[taskNumbers];
        for (int i = 0; i < taskNumbers; i++) {
            tasks[i]= scanner.nextLine();
        }
        new Master(arguments,workersCount,protocol,deadLockProtocol,port,storagePort,data,taskNumbers,tasks).start();
    }
}
