package util.schedule;

import util.task.TaskContext;

public interface Scheduler {
    TaskContext getTask();
    void addTask();
}
