package sniffing;

public class Probe {
    private String type;
    private String ip;
    private double epochTime;

    public Probe(double epochTime, String ip, String type) {
        this.type = type;
        this.ip = ip;
        this.epochTime = epochTime;
    }

    public String getType() {
        return type;
    }

    public String getIp() {
        return ip;
    }

    public double getEpochTime() {
        return epochTime;
    }

}
