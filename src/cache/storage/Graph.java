package cache.storage;

import util.task.TaskContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Graph {
    private ArrayList<ArrayList<Integer>> adj;
    private ArrayList<TaskContext> tasks;
    private Integer taskCount;
    private Integer resources;

    public Graph(int res,int taskNum, ArrayList<TaskContext> tasks){
        this.adj = new ArrayList<>();
        for (int j = 0; j < taskNum+res; j++) {
            adj.add(new ArrayList<>());
        }
        this.taskCount=taskNum;
        this.resources=res;
        this.tasks = tasks;
    }

    public void initialize(){
        for (int i = resources; i < resources+taskCount; i++) {
            for (Integer index : tasks.get(i-resources).getStorageIndices()) {
                if (!adj.get(i).contains(index))adj.get(i).add(index);
            }
        }
    }

    public ArrayList<ArrayList<Integer>> getAdj() {
        return adj;
    }

    public ArrayList<TaskContext> getTasks() {
        return tasks;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public int getResources() {
        return resources;
    }

    public void ReverseEdge(Integer res, Integer task){
        adj.get(task+resources).remove(res);
        adj.get(res).add(task+resources);
    }

    public void removeTask( Integer task){
        for (int i = 0; i < resources; i++) {
            if (adj.get(i).contains(task+resources)){
                adj.get(i).remove(adj.get(i).indexOf(task+resources));
            }
        }
    }

    public boolean dfs(int root,int index, boolean[] mask){
        mask[index]=true;
        for (Integer edge: adj.get(index)) {
            if (edge==root)return true;
            else{
                if (!mask[edge])if (dfs(root,edge,mask))return true;
            }
        }
        return false;
    }

    public boolean prevent(int task){
        for (Integer adjJ : adj.get(task+resources)) {
            if (!adj.get(adjJ).isEmpty())return true;
        }
        return false;
    }
}
