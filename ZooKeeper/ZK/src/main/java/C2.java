import server_network.ServerWrapper;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class C2 {

    public static void main(String[] args) throws IOException {
        ServerSocket
                serverSocket = new ServerSocket(4444);
        Socket socket = serverSocket.accept();

        System.out.println(ServerWrapper.ServerMessage.parseDelimitedFrom(socket.getInputStream()).getTorrentWrapper());
    }
}
