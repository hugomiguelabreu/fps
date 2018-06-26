import client_network.ClientWrapper;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import server_network.ServerWrapper;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class TrackSim {



    public static void main(String[] args) throws IOException {

        ServerSocket ss = new ServerSocket(1234);

        while(true){
            Socket s = ss.accept();
            System.out.println(receive(s));

            ServerWrapper.TorrentResponse tr = ServerWrapper.TorrentResponse.newBuilder().setContent(ByteString.copyFromUtf8("asdasdkalsdjalsdk")).build();
            ServerWrapper.ServerMessage m = ServerWrapper.ServerMessage.newBuilder().setTorrentResponse(tr).build();
            send(s,m);

        }
    }

    public static ServerWrapper.ServerMessage receive(Socket socket) throws IOException {
        CodedInputStream in = CodedInputStream.newInstance(socket.getInputStream());
        byte[] header = in.readRawBytes(4);
        int l = ByteBuffer.wrap(header).getInt();
        System.out.println(l);
        byte[] data = in.readRawBytes(l);
        return ServerWrapper.ServerMessage.parseFrom(data);
    }


    private static void send(Socket socket, ServerWrapper.ServerMessage wrapper) {

        try {
            byte[] size = ByteBuffer.allocate(4).putInt(wrapper.getSerializedSize()).array();
            socket.getOutputStream().write(size);
            wrapper.writeTo(socket.getOutputStream());

        } catch (Exception e) {}

    }



}
