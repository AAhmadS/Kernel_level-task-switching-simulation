package worker.worker;

import util.logger.Logger;
import util.task.Task;
import util.network.connection.Connection;
import cache.storage.StorageApi;

import java.io.IOException;


public class WorkerConnection extends Thread {
    private Task task;
    private final int storagePort;
    private final int port;
    private Connection connection;

    public WorkerConnection(int port, int storagePort) {
        this.port = port;
        this.storagePort = storagePort;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(200);
            this.connection = new Connection(port);
            Logger.logger.write("&&&&&&&&&started the workerConnection in worker process");
        } catch (IOException | InterruptedException e) {
            Logger.logger.write("failed to connect to the master server");
        }
        acceptConnection(connection);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                connection.close();
            }
        }));
    }

    public void acceptConnection(Connection connection) {
        new Thread(() -> {
            while (true) {
                serverConnection(connection);
            }
        }).start();
    }

    private void serverConnection(Connection connection) {
        WorkerRequest request = WorkerRequest.valueOf(connection.receive());

        switch (request) {
            case EXECUTE:
                Logger.logger.write("new task is requesting to be executed");
                execute(connection);
                break;
            case CANCEL:
                //todo
                this.interrupt();
                break;
            case REPORT:
                Logger.logger.write("new task is requesting to be reported");
                handleReport(connection);
            case CLOSE:
                close(connection);
            case KILL:
                kill();

        }
    }
//kill void added; todo
    private void kill() {
        System.exit(0);
    }

    private void close(Connection connection) {
        //mainThread.stop();
        connection.close();
    }
    /**
     * logger added
     * */
    private void handleReport(Connection connection) {
        new Thread(() -> {
            connection.send("working : workerServer");
        }).start();
        Logger.logger.write("worker report handled");
    }

    private void execute(Connection connection) {
        try {
            //Logger.logger.logMessage("handling");
            this.task = new Task(new StorageApi(storagePort), connection.readObject());
            //Logger.logger.logMessage("hanlded ");
        } catch (IOException e) {
            Logger.logger.write("Exception in execute(workerServer), context of the exception : "+e.getMessage()+" trace : "+e.getStackTrace()+" summary : storagePort of the new task couldn't be created");
        }
        task.start();
        while(!task.getFinished())synchronized (task){
            try {
                task.wait();
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Logger.logger.write(task.getContext().report());
        connection.sendTask(task.getContext());
    }
}
