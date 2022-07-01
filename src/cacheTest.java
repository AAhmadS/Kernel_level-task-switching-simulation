import util.logger.Logger;
import cache.Cache;
import cache.storage.StorageApi;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class cacheTest {
    public static void main(String[] args) throws IOException {
        Logger.setLogger();
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
        Process p = new ProcessBuilder(commands).start();
        Scanner scanner1 = new Scanner(p.getInputStream());
        System.out.println(scanner1.next());
        scanner1.close();
        StorageApi cache = new StorageApi(8001);
        cache.report();
       // new Task(cache,new TaskContext("1000 0 500 1 500 0 500 2"));
cache.kill();
cache.close();
p.destroyForcibly();
System.exit(45);
    }
}
