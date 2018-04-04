package files;

import Utils.Peer;
import protos.BlocoOuterClass;
import protos.HandshakeOuterClass;
import protos.HandshakeResponseOuterClass;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Receiver {

    private ArrayList<String> ipsAux;
    private ArrayList<String> ips;
    private HashMap<String, Peer> peers;

    public Receiver(ArrayList<String> ips, HashMap<String, Peer> peers) {
        this.ips = ips;
        this.peers = peers;
        ipsAux = new ArrayList<>();
    }

    public void listen() throws IOException {

        InetAddress addr = InetAddress.getByName(ips.get(0)); // assumindo que o proprio endereco esta na posicao 0
        ServerSocket srv = new ServerSocket(5555, 50, addr);

        Socket socket = srv.accept(); // meter dentro de um while

        System.out.println("recebe hanshake");

        HandshakeOuterClass.Handshake handshake = HandshakeOuterClass.Handshake.parseDelimitedFrom(socket.getInputStream());

        String address = handshake.getAddress();
        String fileName = handshake.getFileName();
        int fileSize = handshake.getFileSize();
        int nBlocos = handshake.getNBlocos();

        //reponder
        HandshakeResponseOuterClass.HandshakeResponse response = HandshakeResponseOuterClass.HandshakeResponse.newBuilder()
                .setResponse(true).build();

        response.writeDelimitedTo(socket.getOutputStream());

        System.out.println("manda resposta");

        socket.close();

        receive(address, fileName, fileSize, nBlocos);

    }



    public void receive(String address, String fileName, int fileSize, int nBlocos) throws IOException {

        System.out.println(address);

        Socket socket = new Socket(address, 5555); // get(1) so para testar, depois e preciso ir buscar os addr certos
        InputStream stream = socket.getInputStream();

        int i = 0;
        byte[] novo = new byte[fileSize];
        //HashMap<Integer,byte[]> blocos = new HashMap<>();
        HashMap<Integer, BlocoTemp> blocosTemp = new HashMap<>();


        while(true){

            BlocoOuterClass.Bloco bloco = BlocoOuterClass.Bloco.parseDelimitedFrom(stream);

            blocosTemp.put(bloco.getNbloco(), new BlocoTemp(bloco.getSize(), bloco.getBloco().toByteArray()));
            //blocos.put(bloco.getNbloco(), bloco.getBloco().toByteArray());

            if(blocosTemp.size() == nBlocos)
                break;
        }

        for (Map.Entry<Integer, BlocoTemp> entry : blocosTemp.entrySet())
        {
            for(byte b : entry.getValue().getData()){

                novo[i] = b;
                i++;
            }
        }

        Files.write(new File("/tmp/broadcast/" + fileName).toPath(), novo);
        System.out.println("acabou");

    }

}
