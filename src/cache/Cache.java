package cache;


import util.logger.Logger;
import cache.storage.Storage;
import cache.storage.StorageServer;
import util.publicUtilities.DeadLockProtocol;
import util.publicUtilities.Protocol;

import java.util.ArrayList;
import java.util.Arrays;

public class Cache {
    public static void main(String[] args) throws InterruptedException {
        if (args.length<1) {
            System.out.println(-1);
            System.exit(-1);
        }
        Logger.setLogger();
        Logger.logger.setName("cache");
        Logger.logger.write(Arrays.toString(args));
        int port = Integer.parseInt(args[0]);
        int dataSize = Integer.parseInt(args[1]);
        int taskNumber = Integer.parseInt(args[2]);
        ArrayList<Integer> data = new ArrayList<>();
        ArrayList<String> tasks = new ArrayList<>();
        for (int i=0;i<dataSize;i++){
            data.add(Integer.parseInt(args[i+3]));
        }
        for (int i=0;i<taskNumber;i++){
            tasks.add(args[i+3+dataSize]);
        }
        DeadLockProtocol protocol = DeadLockProtocol.valueOf(args[args.length-1]);
        StorageServer ss = new StorageServer(new Storage(data,tasks),protocol);
        ss.listen(port);
        /*Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                ss.
            }
        }));*/
    }
}
