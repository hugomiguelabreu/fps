package Offline.probes;

import java.sql.Timestamp;

public class Peer {

    private String username;
    private String ip;
    private Timestamp last;

    public Peer(String username, String ip, Timestamp last) {
        this.username = username;
        this.ip = ip;
        this.last = last;
    }

    public String getUsername() {
        return username;
    }

    public String getIp() {
        return ip;
    }

    public Timestamp getLast() {
        return last;
    }

    public void setLast(Timestamp last) {
        this.last = last;
    }
}
