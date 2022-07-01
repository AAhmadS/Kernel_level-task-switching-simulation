package util.task;

import util.logger.Logger;
import cache.storage.StorageApi;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.concurrent.Semaphore;

public class Task extends Thread implements Serializable {
    private final StorageApi storage;
    private final TaskContext context;
    private final Semaphore lock;
    private boolean finished;

    public Task(StorageApi storage, TaskContext context){
        this.context = context;
        this.storage = storage;
        this.lock = new Semaphore(0);
        finished = false;
    }
    public Task(int storagePort,String taskString) throws IOException {
        this(new StorageApi(storagePort),new TaskContext(taskString));
    }
    public void run(){
        while(true){
            try{
                if (! (isInterrupted() || this.finished)){
                    taskSleep();
                    Read();
                    setFinished();
                }else{
                    release(context.getId());
                    break;
                }
            }catch(InterruptedException e){
                this.lock.release();
                Thread.currentThread().interrupt();
                break;
            }
        }
        synchronized (this){this.notifyAll();}
    }

    private void release(int id) {
        this.storage.release(id);
    }

    //certain unspecific line added to the content of the void ;todo
    private void Read() throws InterruptedException {
        int index = context.getNextReadingIndex();
        Logger.logger.write("next reading index is : "+index);
        if (index==-1)return;
        try{
            context.addResult(this.storage.obtain(index,this.context.getId()));
        }catch (InterruptedException e){
            this.context.rollBackReadChanges(index);
            Logger.logger.write("task interrupted during reading; id :"+this.context.getId());
            throw e;
        }
    }

    private void taskSleep() throws InterruptedException {
        int start = getNowMillis();
        long sleepTime = context.getNextSleep();
        try{
            Thread.sleep(sleepTime);
        }catch (InterruptedException e){
            int end = getNowMillis();
            int slept = end - start;
            int toSleep = (int) (sleepTime-slept);
            context.setTimeToSleep(toSleep);
            Logger.logger.write("task interrupted during sleeping; id :"+this.context.getId());
            throw e;
        }
    }

    public synchronized TaskContext interruptTask() {
        this.interrupt();

        try {
            this.lock.acquire();
        } catch (InterruptedException var2) {
        }

        return this.context;
    }

    public static int getNowMillis() {
        return LocalTime.now().getNano() / 1000000;
    }

    public void setFinished(){
        finished= context.finished();
    }

    public boolean getFinished(){
        return context.finished();
    }

    public int getResult(){
        return context.getSum();
    }

    public TaskContext getContext() {
        return context;
    }
}
