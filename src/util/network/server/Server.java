package util.network.server;

import util.logger.Logger;
import util.network.connection.Connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

/** 
 *  TCP connection listener
 * 
 *  usage example:
 *  <pre>
 *  {@code
 *      new Server(
 *          connection -> connection.send("HI from server!")
 *      ).listen(port);
 *  }
 *  </pre>
 */
public class Server {
    private ServerSocket serverSocket;

    private final Consumer<Connection> onAccept;

    /**
     * @param onAccept called whenever a new tcp connection arrives
     */
    public Server(Consumer<Connection> onAccept) {
        this.onAccept = onAccept;
    }

    public void listen(int port) {
        new Thread(() -> {
            try {
                ServerSocket server = new ServerSocket(port);
                this.serverSocket=server;
                Logger.logger.write("listening in server");
                listen(server);
            } catch (Exception e) {
                Logger.logger.write("listen in server : "+e.getMessage());
            }
        }).start();
    }

    private void listen(ServerSocket server) throws IOException {
        while (!server.isClosed()) {
            Logger.logger.write("server is not closed");
            Socket socket = server.accept();
            onAccept.accept(new Connection(socket));
            Logger.logger.write(" zeky type 4");
        }
    }
    public void kill() throws IOException {
        this.serverSocket.close();
    }
}
