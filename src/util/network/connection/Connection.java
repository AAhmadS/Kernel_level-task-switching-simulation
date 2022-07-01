package util.network.connection;

import util.serialize.serializer.Deserializer;
import util.serialize.serializer.Serializer;
import util.task.TaskContext;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.function.Consumer;

public class Connection {

    private final PrintStream out;
    private final Scanner in;


    private Thread listenThread;

    /** 
     * constructor used the server 
     * 
     * @param port port of the socket server accepting new connections
     * @throws IOException
     */
    public Connection(Socket socket) throws IOException {
        in = new Scanner(socket.getInputStream());
        out = new PrintStream(socket.getOutputStream());
    }
    
    /** 
     * constructor used by the clients
     * 
     * @param port port of the tcp server to connect to
     * @throws IOException
     */
    public Connection(int port) throws IOException {
        this(
            new Socket("localhost", port)
        );
    }

    public void send(String message) {
        out.println(message);
    }

    public void send(Object obj) {
        this.send(obj.toString());
    }

    /**
     *  returns a new message from server (if available),
     *  otherwise blocks
     */
    public String receive() {
        if (in.hasNextLine())
            return in.nextLine();
        return null;
    }

    /**
     *  @return true if there is new message
     *  @apiNote This method may block for input
     */
    public boolean hasNextLine() {
        return in.hasNextLine();
    }

    public void sendTask(TaskContext taskContext) {
        send(taskContext.code());
    }

    public TaskContext readObject() {
        String context = this.receive();
        String[] args = context.split("&");
        return new TaskContext(args[0],Integer.parseInt(args[1]),Integer.parseInt(args[2]));
    }

    /** 
     * closes connection.
     * notice this may be the only way to cancel the wait for nextLine or hasNextLine
     */
    public void close() {
        in.close();
        out.close();
    }

    /**
     *  an alternative way to receive message.
     * 
     *  catches the next message received by the connection and passes it to a callback
     * 
     *  @param onReceive called everytime connection recevies a message
     *  @apiNote warning: either use nextLine or listen
     */
    public void listen(Consumer<String> onRecieve) {
        listenThread = new Thread(() -> {
            onRecieve.accept(this.receive());
            listenThread = null;
        });
        listenThread.start();
    }

    public void interrupt() {
        if (listenThread != null)
            listenThread.stop();
    }
}
