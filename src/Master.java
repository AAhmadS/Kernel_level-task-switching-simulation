import util.logger.Logger;
import util.network.connection.Connection;
import util.network.server.AbstractServer;
import util.publicUtilities.DeadLockProtocol;
import util.publicUtilities.Protocol;
import util.schedule.FCFSScheduler;
import util.schedule.RRScheduler;
import util.schedule.SJFScheduler;
import util.schedule.Scheduler;
import util.task.TaskContext;
import worker.worker.WorkerHandler;
import cache.Cache;
import cache.storage.StorageApi;
import worker.WorkerMaster;

import java.io.*;
import java.util.*;

public class Master extends AbstractServer implements Runnable {
    private String[] commonArgs;
    private Integer workersCount;
    private Protocol protocol;
    private DeadLockProtocol deadlockProtocol;
    private Integer port;
    private Integer storagePort;
    private int[] data;
    private Integer tasksCount;
    private TaskContext[] tasks;
    private LinkedList<TaskContext> taskContexts;
    private final Scheduler scheduler;
    private LinkedList<WorkerHandler> workersList;
    private StorageApi cache;
    private LinkedList<Process> processes;

    public Master(String[] commonArgs, int workersCount, String protocol, String deadlockProtocol, Integer port,Integer storagePort, int[] data, Integer tasksCount, String[] tasks) {
        this.commonArgs = commonArgs;
        this.workersCount = workersCount;
        this.protocol = Protocol.valueOf(protocol);
        this.deadlockProtocol = DeadLockProtocol.valueOf(deadlockProtocol);
        this.port = port;
        this.storagePort = storagePort;
        this.data = data;
        this.tasksCount = tasksCount;
        this.tasks = createTasks(tasks);
        processes = new LinkedList<>();
        switch (this.protocol){
            case FCFS:
                scheduler = new FCFSScheduler();
                break;
            case SJF:
                scheduler = new SJFScheduler();
                break;
            default:
                scheduler = new RRScheduler();
                break;
        }
    }
    private TaskContext[] createTasks(String[] tasks) {
        taskContexts=new LinkedList<>();
        this.tasks = new TaskContext[tasksCount];
        for (int i=0;i<tasks.length;i++) {
            this.tasks[i]=new TaskContext(tasks[i],i);
            taskContexts.add(this.tasks[i]);
        }
        return this.tasks;
    }


    @Override
    public void run() {
        Logger.setLogger();
        Logger.logger.clearLog();
        Logger.logger.setName("master");
        Logger.logger.write("master started");
        this.listen(port);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
        @Override
        public void run() {
            for (Process p :
                    processes) {
                p.destroyForcibly();
            }
            try {
                kill();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        }));
        establishCacheServer(storagePort,data);
        createWorkers(workersCount,port,storagePort);
        while(workersCount>0){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //System.out.println("halt ended");
        startTasksOperation();
        //operate();
    }

    private void establishCacheServer(Integer storagePort, int[] data) {
        String className = Cache.class.getName();
        String[] backArgs = startCacheProcess(className,storagePort,data);
        try{
            Thread.sleep(1000);
            this.cache = new StorageApi(backArgs,storagePort);
        }catch (IOException | InterruptedException e){
            Logger.logger.write("failed to create storageApi; in Master line : 90");
        }
    }
    private String[] startCacheProcess(String className,Integer storagePort, int[] data){
        List<String> command = new LinkedList<>();
        command.addAll(List.of(commonArgs));
        command.add(className);
        command.add(String.valueOf(storagePort));
        command.add(String.valueOf(data.length));
        command.add(String.valueOf(tasksCount));
        for (int i=0;i<data.length;i++){
            command.add(String.valueOf(data[i]));
        }
        for (int i = 0; i < tasksCount; i++) {
            command.add(tasks[i].code());
        }
        command.add(String.valueOf(deadlockProtocol));
        String[] backArgs = new String[2];
        try {
            System.out.println(command);
            ProcessBuilder builder = new ProcessBuilder(command);
            Process process = builder.start();
            Scanner scannerError = new Scanner(process.getErrorStream());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(scannerError.hasNextLine()){
                        System.out.println(scannerError.nextLine());
                    }
                }
            }).start();
            processes.add(process);
            long pid = process.pid();
            backArgs[0] = String.valueOf(pid);
            Logger.logger.write("cache created successfully; pid : "+backArgs[0]);
        } catch (Exception e) {
            Logger.logger.write("building of the process faced a problem : "+e.getMessage()+" trace: "+e.getStackTrace());
            e.printStackTrace();
        }
        return backArgs;
    }

    private void createWorkers(int workersCount, Integer port,Integer storagePort) {
        workersList = new LinkedList<>();
        String className = WorkerMaster.class.getName();

        for (int i=0;i<workersCount;i++){
            startWorkerProcess(className, port,storagePort);
        }
    }
    private void startWorkerProcess(String className, int port, int storagePort) {
        List<String> command = new LinkedList<>();
        command.addAll(List.of(commonArgs));
        command.add(className);
        command.add(String.valueOf(port));
        command.add(String.valueOf(storagePort));
        String[] backArgs = new String[1];
        try {
            System.out.println(command);
            ProcessBuilder builder = new ProcessBuilder(command);
            Process process = builder.start();
            processes.add(process);
            long pid = process.pid();
            backArgs[0] = String.valueOf(pid);
            Scanner scannerError = new Scanner(process.getErrorStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(scannerError.hasNextLine()){
                        System.out.println(scannerError.nextLine());
                    }
                }
            }).start();

            Logger.logger.write("worker created successfully pid : "+backArgs[0]);
        } catch (Exception e) {
            Logger.logger.write("process building of the process faced a problem : "+e.getMessage()+" trace: "+e.getStackTrace());
            e.printStackTrace();
        }
    }

    private void startTasksOperation() {
        switch (protocol){
            case RR ->
                    operateRR();
            case SJF -> {
                try {
                    operateSJF();
                } catch (InterruptedException e) {
                    Logger.logger.write("interrupted exception while operating SJF");
                    e.printStackTrace();
                }
            }
            case FCFS -> {
                try {
                    operateFCFS();
                } catch (InterruptedException e) {
                    Logger.logger.write("interrupted exception while operating FCFS");
                    e.printStackTrace();
                }
            }
        }
    }
    private void operate() {
        for (WorkerHandler worker:
             workersList) {
            worker.report();
        }
        cache.report();
    }
    private void operateSJF() throws InterruptedException {
        Collections.sort(taskContexts, new Comparator<TaskContext>() {
            @Override
            public int compare(TaskContext o1, TaskContext o2) {
                long sleeps1 = 0;
                long sleeps2 = 0;
                for (Integer i : o1.getSleepDurations()) {
                    sleeps1+=i;
                }
                sleeps1+=o1.getCurrentSleepDuration();
                for (Integer i : o2.getSleepDurations()) {
                    sleeps2+=i;
                }
                sleeps2+=o2.getCurrentSleepDuration();
                if (sleeps1>sleeps2)return 1;
                else{
                    if (sleeps1<sleeps2)return -1;
                }
                return 0;
            }
        });
        operateFCFS();
    }
    private void operateRR() {
    }
    private void operateFCFS() throws InterruptedException {
        int index = -1;
        while(true) {
            TaskContext task = null;
            if (!taskContexts.isEmpty()){
                task = taskContexts.removeFirst();
                Logger.logger.write("task "+task.getId()+" being executed");
                index++;
                WorkerHandler worker;
                synchronized (workersList){
                    //System.out.println("entered");
                    while (workersList.size()==0) {
                        Logger.logger.write("waiting in operateFCFS");
                        workersList.wait();
                        Logger.logger.write("cease waiting "+task.getId());
                    }
                    worker = workersList.removeFirst();
                }
                int finalIndex = index;
                if (!cache.deadlock(task.getId())){
                    if(protocol.equals(Protocol.SJF)){
                        Collections.sort(taskContexts, new Comparator<TaskContext>() {
                            @Override
                            public int compare(TaskContext o1, TaskContext o2) {
                                long sleeps1 = 0;
                                long sleeps2 = 0;
                                for (Integer i : o1.getSleepDurations()) {
                                    sleeps1+=i;
                                }
                                sleeps1+=o1.getCurrentSleepDuration();
                                for (Integer i : o2.getSleepDurations()) {
                                    sleeps2+=i;
                                }
                                sleeps2+=o2.getCurrentSleepDuration();
                                if (sleeps1>sleeps2)return 1;
                                else{
                                    if (sleeps1<sleeps2)return -1;
                                }
                                return 0;
                            }
                        });
                        Logger.logger.write("SJF protocol cleaning up the list");
                    }
                    TaskContext finalTask = task;
                    new Thread(()->{
                        try {
                            Logger.logger.write(finalTask.code());
                            TaskContext task1 = worker.ExecuteTask(finalTask);
                            System.out.println(task1.report());
                        } catch (InterruptedException e) {
                            Logger.logger.write("interrupted exception while executing the task"+finalIndex);
                            e.printStackTrace();
                        }
                        workersList.add(worker);
                        synchronized (workersList){
                            Logger.logger.write("releasing and notifying");
                            workersList.notifyAll();
                        }
                    }).start();
                }
                else{
                    Logger.logger.write("faced deadlock in FCFSOperation");
                    taskContexts.add(task);
                    workersList.add(worker);
                }
            }
            else{
                Logger.logger.write("breaking the while loop");
                break;
                //todo
            }
        }
    }

    @Override
    public void acceptConnection(Connection connection) {
        Logger.logger.write("worker connected to the server " + workersCount);
        workersCount--;
        try {
            workersList.add(new WorkerHandler(connection));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void start() {
        this.run();
    }
}