import cache.Cache;
import worker.WorkerMaster;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class WorkerTest {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        List<String> commandsW = new LinkedList<>();
        commandsW.add("java");
        commandsW.add("-classpath");
        commandsW.add("out/production/osHW@");
        commandsW.add(WorkerMaster.class.getName());
        commandsW.add("8003");
        commandsW.add("8001");
        System.out.println(commandsW);
        Process p = new ProcessBuilder(commandsW).start();
        Scanner scanner1 = new Scanner(p.getInputStream());
        System.out.println(scanner1.nextLine());
        scanner1.close();
        List<String> commands = new LinkedList<>();
        commands.add("java");
        commands.add("-classpath");
        commands.add("out/production/osHW@");
        commands.add(Cache.class.getName());
        commands.add("8001");
        commands.add("3");
        commands.add("23");
        commands.add("1");
        commands.add("2");
        System.out.println(commands);
        Process p2 = new ProcessBuilder(commands).start();
        Scanner scanner2 = new Scanner(p2.getInputStream());
        System.out.println(scanner2.next());
        scanner2.close();
       // WorkerHandler worker = new WorkerHandler(new String[]{String.valueOf(p.pid())},8003);
        //try {
        //    TaskContext t = worker.ExecuteTask(new TaskContext("1000 0 500 1 500 0 500 2"));
          //  System.out.println(t.report());
        //} //catch (InterruptedException e) {
            //e.printStackTrace();
       // }
        //p.destroy();
        //p2.destroyForcibly();
    }
}
