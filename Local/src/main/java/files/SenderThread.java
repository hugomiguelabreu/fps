package files;

import com.google.protobuf.ByteString;
import protos.BlocoOuterClass;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class SenderThread extends Thread {

    private Socket socket;
    private HashMap<Integer,byte[]> blocos;

    public SenderThread(Socket socket, HashMap<Integer,byte[]> blocos){
        this.socket = socket;
        this.blocos = blocos;
    }


//    AddrOuterClass.Addr.Builder addr = AddrOuterClass.Addr.newBuilder();
//                    addr.setAddr(to);
//                    addr.setPortNumber(5555);
//    AddrOuterClass.Addr data = addr.build();
//
//    ByteArrayOutputStream buff = new ByteArrayOutputStream(1024);
//                    data.writeDelimitedTo(buff);


    @Override
    public void run(){

        for (Map.Entry<Integer, byte[]> entry : blocos.entrySet())
        {

            BlocoOuterClass.Bloco bloco = BlocoOuterClass.Bloco.newBuilder()
                .setSize(entry.getValue().length)
                .setNbloco(entry.getKey())// futuramente nao sera preciso pq o n de blocos vai nos metadados do inicio
                .setBloco(ByteString.copyFrom(entry.getValue())).build();

            try {

                //socket.getOutputStream().write(bloco.getSerializedSize());
                bloco.writeDelimitedTo(socket.getOutputStream());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("sended");
    }

}
