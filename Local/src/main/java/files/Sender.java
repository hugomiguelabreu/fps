package files;

import Utils.Peer;
import protos.HandshakeOuterClass;
import protos.HandshakeResponseOuterClass;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Sender {

    private ArrayList<String> ips;
    private ConcurrentHashMap<String, Peer> peers;

    public Sender(ArrayList<String> ips, ConcurrentHashMap<String, Peer> peers) {

        this.ips = ips;
        this.peers = peers;
    }

    public void send() throws IOException {

        String fileName = "Distributed Systems Principles and Paradigms.pdf";

        Path path = Paths.get("/home/padrao/Desktop/4ano/Distributed Systems Principles and Paradigms.pdf");
        //Path path = Paths.get(" /home/padrao/Desktop/fps.odt");

        byte[] data = Files.readAllBytes(path);

        System.out.println(data.length);

        HashMap<Integer,byte[]> blocos = new HashMap<>();

        byte[] insert = new byte[100000];

        //blocos de 100 k
        //TODO o tamanho pode variar dependendo do MTU

        int i, j, k;

        if(data.length > 100000){

            for(i = 0, j = 0, k = 0; i<data.length; i++, j++){

                insert[j] = data[i];

                if(j == 99999){
                    blocos.put(k,insert);
                    insert = new byte[100000];
                    k++;
                    j = -1;
                }
            }

            if(j != 0){

                byte[] trim = new byte[j];

                for(i=0; i<j; i++){

                    trim[i]=insert[i];
                }

                blocos.put(k,trim);
            }


        }
        else { // se tiver menos de 100 k for so num bloco

            byte[] trim = new byte[data.length];

            for(i=0; i<data.length; i++){

                trim[i]=insert[i];
            }

            blocos.put(0,trim);
        }

        // porta ?? se houverem varias transferencias ao mesmo tempo para o mesmo gajo e preciso mudar as portas
        // vai tudo para a mesma com um id unico
        Socket s;

        // precorre todos os mens que estao na rede manda para eles TESTE
        // sincrono TESTE
        for (Map.Entry<String, Peer> entry : peers.entrySet())
        {
            if(entry.getValue() != null){

                System.out.println("manda hanshake");

                s = new Socket(entry.getValue().getIp(), entry.getValue().getPort());

                HandshakeOuterClass.Handshake handshake = HandshakeOuterClass.Handshake.newBuilder()
                        .setAddress(ips.get(0))
                        .setFileName(fileName)
                        .setFileSize(data.length)
                        .setNBlocos(blocos.size()).build();

                handshake.writeDelimitedTo(s.getOutputStream());

                HandshakeResponseOuterClass.HandshakeResponse response = HandshakeResponseOuterClass.HandshakeResponse.parseDelimitedFrom(s.getInputStream());

                System.out.println("recebeu resposta");

                // fazer qq cena com a response


            }
        }

        // assumindo que so ha 1 interface de rede
        InetAddress addr = InetAddress.getByName(ips.get(0)); // assumindo que o proprio endereco esta na posicao 0
        ServerSocket srv = new ServerSocket(5555, 50, addr);

        while(true){

            Socket socket = srv.accept();
            new SenderThread(socket, blocos).start();
        }

    }


}
