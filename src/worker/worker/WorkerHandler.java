package worker.worker;

import util.logger.Logger;
import util.network.connection.Connection;
import util.task.TaskContext;
import cache.storage.StorageRequest;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class WorkerHandler {
    private final Connection connection;
    private boolean busy;
    public WorkerHandler(Connection connection) throws IOException {
        this.connection = connection;
        busy =false;
    }

    public TaskContext ExecuteTask(TaskContext task) throws InterruptedException{
        connection.send(WorkerRequest.EXECUTE);
        connection.sendTask(task);
        this.busy=true;
        try{
            return awaitWorkerResponse();
        }catch(InterruptedException e){
            Logger.logger.write("Exception in execute task function in workerHandler : "+e.getMessage()+" trace being : "+e.getStackTrace());
            connection.send(WorkerRequest.CANCEL);
            this.busy=false;
            throw e;
        }catch(ExecutionException e){
            Logger.logger.write("Exception in execute task function in workerHandler : "+e.getMessage()+" trace being : "+e.getStackTrace());
            this.busy=false;
            return null;
        }
    }
    private TaskContext awaitWorkerResponse() throws InterruptedException, ExecutionException{
        FutureTask<TaskContext> getWorkerResponseTask = new FutureTask<>(
                connection::readObject
        );
        Executors.newFixedThreadPool(1).execute(getWorkerResponseTask);
        this.busy=false;
        //System.out.println("returning");
        return getWorkerResponseTask.get();
    }


    public void release() {
        /* TODO */
    }

    public void interrupt() {
        connection.send(WorkerRequest.CLOSE);
        connection.close();
    }

    /**
     * kill requests added to the APIs
     * */
    public void kill(){
        connection.send(StorageRequest.KILL);
    }

    public void close() {
        connection.close();
        Logger.logger.write("workerHandler connection closed successfully");
    }

    public void report() {
        connection.send(WorkerRequest.REPORT);
        try{
            FutureTask<String> getWorkerResponseTask = new FutureTask<>(
                    connection::receive
            );
            Executors.newFixedThreadPool(1).execute(getWorkerResponseTask);
            System.out.println(getWorkerResponseTask.get());
        }catch (ExecutionException | InterruptedException e){
            Logger.logger.write(e.getMessage()+" with trace : "+e.getStackTrace()+" in WorkerHandler report function");
        }
    }

    public boolean isBusy() {
        return busy;
    }
}
