package Utils;

import java.sql.Timestamp;

public class Peer {

    private String ip;
    private int port;
    private String name;
    private Timestamp last; // ainda falta

    public Peer(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
