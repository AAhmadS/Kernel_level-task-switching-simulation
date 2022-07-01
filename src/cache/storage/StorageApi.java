package cache.storage;


import util.logger.Logger;
import util.network.connection.Connection;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class StorageApi {
    private final Connection connection;

    public StorageApi(int storagePort) throws IOException {
        connection = new Connection(storagePort);
    }

    public StorageApi(String[] args,int port) throws IOException {
        this(port);
        //TODO
    }
    public int obtain(int index, int id) throws InterruptedException {
        connection.send(StorageRequest.OBTAIN);
        connection.send(index);
        connection.send(id);
        try {
            return awaitStorageResponse();
        } catch (InterruptedException e) {
            connection.send(StorageRequest.CANCEL);
            throw e;
        } catch (ExecutionException e) {
            return -1;
        }
    }
    /**
     * kill requests added to the APIs
     * */
    public void kill(){
        connection.send(StorageRequest.KILL);
    }

    private Integer awaitStorageResponse() throws InterruptedException, ExecutionException {
        FutureTask<Integer> getStorageResponseTask = new FutureTask<>(
            () -> Integer.parseInt(connection.receive())
        );
        Executors.newFixedThreadPool(1).execute(getStorageResponseTask);
        
        return getStorageResponseTask.get();
    }

    public void release(int id) {
        connection.send(StorageRequest.RELEASE);
        connection.send(id);
    }

    public void interrupt() {
        connection.close();
    }


    public void close() {
        connection.close();
        Logger.logger.write("StorageApi connection closed successfully");
    }

    public void report() {
        connection.send(StorageRequest.REPORT);
        try{
            FutureTask<String> getStorageResponseTask = new FutureTask<>(
                    () -> connection.receive()
            );
            Executors.newFixedThreadPool(1).execute(getStorageResponseTask);
            System.out.println(getStorageResponseTask.get());
        }catch (ExecutionException | InterruptedException e){
            Logger.logger.write(e.getMessage()+" with trace : "+e.getStackTrace()+" in StorageApi report function");
        }
    }

    public boolean deadlock(int task) {
        connection.send(StorageRequest.DEADLOCK);
        connection.send(task);
        try{
            FutureTask<String> getStorageResponse = new FutureTask<>(
                    connection::receive
            );
            Executors.newFixedThreadPool(1).execute(getStorageResponse);
            Logger.logger.write("storage response to deadlock : "+getStorageResponse.get());
            return Boolean.valueOf(getStorageResponse.get());
        }catch (ExecutionException | InterruptedException e){
            Logger.logger.write(e.getMessage()+" in storageApi deadlock function");
        }
        return true;
    }
}
