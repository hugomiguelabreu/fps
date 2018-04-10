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

    public Peer(String ip, int port, Timestamp last){
        this.ip = ip;
        this.port = port;
        this.last = last;
    }

    public void setLast(Timestamp last) {
        this.last = last;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public Timestamp getLast() {
        return last;
    }
}
