package cache.storage;

import util.logger.Logger;
import util.network.connection.Connection;
import util.network.server.AbstractServer;
import util.publicUtilities.DeadLockProtocol;
import util.publicUtilities.Protocol;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class StorageServer extends AbstractServer {
    private final Storage storage;
    private final Map<Connection, Thread> obtainThreads;
    private final DeadLockProtocol deadLockProtocol;

    public StorageServer() {
        storage = new Storage();
        obtainThreads = new HashMap<>();
        this.deadLockProtocol= DeadLockProtocol.NONE;
    }

    public StorageServer(Storage storage,DeadLockProtocol protocol) {
        this.storage = storage;
        obtainThreads = new HashMap<>();
        this.deadLockProtocol = protocol;
    }

    @Override
    public void acceptConnection(Connection connection) {
        new Thread(() -> {
            while (true) {
                serverConnection(connection);
            }
        }).start();
    }

    private void serverConnection(Connection connection) {
        StorageRequest request = StorageRequest.valueOf(connection.receive());

        switch (request) {
            case OBTAIN:
            handleObtain(connection);
            break;

            case RELEASE:
            handleRelease(connection);
            break;

            case CANCEL:
            obtainThreads.get(connection).interrupt();
            break;

            case WRITE:
            // TODO
                break;
            case REPORT:
                handleReport(connection);
            break;
            case KILL:
                //kill();
            case DEADLOCK:
                handleDeadLock(connection);
        }
    }

    private void handleDeadLock(Connection connection) {
        String id = connection.receive();
        switch (deadLockProtocol){
            case NONE ->
                    connection.send("false");
            case DETECTION ->
                    connection.send(String.valueOf(storage.checkDetection(Integer.valueOf(id))));
            case PREVENTION ->
                    connection.send(String.valueOf(storage.checkPrevention(Integer.valueOf(id))));
        }
    }

    private void handleRelease(Connection connection) {
        int id = Integer.parseInt(connection.receive());
         Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                storage.release(id);
                Logger.logger.write("releasing handled");
            }
        });
         obtainThreads.put(connection,thread);
         thread.start();
    }

    private void handleReport(Connection connection) {
        new Thread(() -> {
            connection.send("working : storageServer");
        }).start();
        Logger.logger.write("storage report handled");
    }

    private void handleObtain(Connection connection) {
        int index = Integer.parseInt(connection.receive());
        int id = Integer.parseInt(connection.receive());
        Logger.logger.write(id+" "+index);
        Thread thread = new Thread(() -> {
            try {
                int value = storage.obtain(index, id);
                connection.send(value);
                Logger.logger.write("value : "+value+" sent successfully");
            } catch (InterruptedException e) { }

        });
        obtainThreads.put(connection, thread);
        thread.start();
    }
}
