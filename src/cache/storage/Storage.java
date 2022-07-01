package cache.storage;

import util.logger.Logger;
import util.task.TaskContext;

import java.util.*;
import java.util.concurrent.Semaphore;

public class Storage {
    private final Map<Integer, Integer> data;   // index, value
    private Map<Integer, Integer> lockOwner; // index, owner
    private final Map<Integer, Semaphore> locks;
    private final ArrayList<TaskContext> tasks;
    private Graph graph;

    public Storage() {
        data = new HashMap<>();
        lockOwner = new HashMap<>();
        locks = new HashMap<>();
        tasks = new ArrayList<>();
    }

    public Storage(List<Integer> data,ArrayList<String> tasks) {
        this();
        for (int i=0; i<data.size(); ++i) {
            write(i, data.get(i));
        }
        for (int i = 0; i < tasks.size(); i++) {
            Logger.logger.write(tasks.get(i));
            String[] ar= tasks.get(i).split("&");
            Logger.logger.write(Arrays.toString(ar));
            this.tasks.add(new TaskContext(ar[0],Integer.parseInt(ar[1])));
        }
        this.graph=new Graph(data.size(), tasks.size(), this.tasks);
        graph.initialize();
        Logger.logger.write(graph.getAdj().toString());
    }

    public int obtain(int index, int id) throws InterruptedException {
        if (lockOwner.getOrDefault(index, -1) != id) {
            locks.get(index).acquire();
            graph.ReverseEdge(index,id);
            lockOwner.put(index, id);
        }
        return data.get(index);
    }

    public void write(int index, int value) {
        data.put(index, value);
        locks.computeIfAbsent(index, i -> new Semaphore(1));
        lockOwner.computeIfAbsent(index,i->-1);
    }

    public synchronized void release(int index, int id) {
        if (lockOwner.getOrDefault(index,-1) == id) {
            Logger.logger.write("releasing lock "+index+" "+id);
            locks.get(index).release();
            lockOwner.put(index,-1);
        }
    }

    public void release(int id) {
        for (int i=0;i<lockOwner.size();i++) {
            Logger.logger.write("trying to release "+i+" "+id);
            release(i,id);
        }
        graph.removeTask(id);
    }

    public boolean checkDetection(int task) {
        return graph.dfs(task+ graph.getResources(),task+graph.getResources(),new boolean[graph.getResources()+ graph.getTaskCount()]);
    }

    public boolean checkPrevention(int task) {
        boolean ans=graph.prevent(task);
        if (!ans) for (Integer index : tasks.get(task).getStorageIndices()){
            try {
                obtain(index,task);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return ans;
    }
}