package util.network.server;

import util.network.connection.Connection;

import java.io.IOException;

public abstract class AbstractServer {
    private final Server server;

    protected AbstractServer() {
        this.server = new Server(this::acceptConnection);
    }

    public void listen(int port) {
        server.listen(port);
    }
    public void kill() throws IOException {
        server.kill();
    }

    public abstract void acceptConnection(Connection connection);
}
