package worker;

import util.logger.Logger;
import worker.worker.WorkerConnection;

import java.lang.management.RuntimeMXBean;

public class WorkerMaster {
    public static void main(String[] args) {
        if (args.length<2) {
            System.out.println("failed attempt");
            System.exit(-1);
        }
        Logger.setLogger();
        Logger.logger.setName("worker"+ ProcessHandle.current().pid());
        int port = Integer.parseInt(args[0]);
        int storagePort = Integer.parseInt(args[1]);
        new WorkerConnection(port,storagePort).start();
    }
}
