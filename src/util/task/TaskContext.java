package util.task;

import java.io.Serializable;
import java.util.LinkedList;

public class TaskContext implements Serializable {
    private int currentSleepDuration;
    private int currentSum;
    private final LinkedList<Integer> storageIndices;
    private final LinkedList<Integer> sleepDurations;
    private int id;

    public TaskContext(String taskInput){
        storageIndices = new LinkedList<>();
        sleepDurations = new LinkedList<>();
        String[] input = taskInput.split(" ");
        for (int i=0;i< input.length;i++){
            if (i % 2 == 0) {
                sleepDurations.add(Integer.parseInt(input[i]));
            } else {
                storageIndices.add(Integer.parseInt(input[i]));
            }
        }
        currentSum = 0;
        currentSleepDuration = sleepDurations.removeFirst();
        this.id=-1;
    }

    public TaskContext(String taskInput, int id){
        this(taskInput);
        this.id= id;
    }

    public TaskContext(String taskInput,int id, int sum){
        this(taskInput, id);
        this.currentSum = sum;
    }

    public int getNextReadingIndex() {
        if (storageIndices.size()==0)return -1;
        return storageIndices.removeFirst();
    }

    public int getId() {
        return id;
    }

    public void addResult(int obtain) {
        this.currentSum += obtain;
    }

    public void rollBackReadChanges(int index) {
        storageIndices.addFirst(index);
    }

    public long getNextSleep() {
        long out = currentSleepDuration;
        if (sleepDurations.size()>0)currentSleepDuration = sleepDurations.removeFirst();
        else currentSleepDuration=0;
        return out;
    }

    public void setTimeToSleep(int toSleep) {
        sleepDurations.addFirst(currentSleepDuration);
        currentSleepDuration=toSleep;
    }

    public String report(){
        if (finished()){
            return "task "+this.getId()+" executed successfully with result "+currentSum;

        }else{
            return "unfinished";
        }
    }

    public boolean finished(){
        return !(sleepDurations.size()>0 || storageIndices.size()>0 ||currentSleepDuration!=0);
    }

    public void setCurrentSleepDuration(int currentSleepDuration) {
        this.currentSleepDuration = currentSleepDuration;
    }

    public void setCurrentSum(int currentSum) {
        this.currentSum = currentSum;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String code() {
        StringBuilder sb = new StringBuilder();
        LinkedList<Integer> sI= new LinkedList<>();
        sI .addAll(storageIndices);
        LinkedList<Integer> SD = new LinkedList<>();
        SD .addAll(sleepDurations);
        sb.append(currentSleepDuration);
        while(true){if (sI.size()==0)break;
            sb.append(" "+sI.removeFirst());
            if (SD.size()==0)continue;
            sb.append(" "+SD.removeFirst());
        }
        sb.append("&").append(id).append("&").append(currentSum);
        return sb.toString();
    }

    public int getSum() {
        return this.currentSum;
    }

    public int getCurrentSleepDuration() {
        return currentSleepDuration;
    }

    public int getCurrentSum() {
        return currentSum;
    }

    public LinkedList<Integer> getStorageIndices() {
        return storageIndices;
    }

    public LinkedList<Integer> getSleepDurations() {
        return sleepDurations;
    }
}
