package Offline.LocalTorrent;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ListenerTorrents extends Thread{

    private final int port = 5558;
    private String username, ownAddress;

    public ListenerTorrents(String username, String ownaddresses){

        this.username = username;
        this.ownAddress = ownaddresses;
    }

    @Override
    public void run(){

        try {
            ServerSocket socket = new ServerSocket(port, 50, InetAddress.getByName(ownAddress));

            while (true){

                Socket s = socket.accept();
                System.out.println("vai receber um torrent");
                new ListenerTorrentThread(s, ownAddress).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
