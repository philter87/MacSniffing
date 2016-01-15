package sniffing;

/**
 * Created by nordea on 14-01-2016.
 */
public class SniffedDevice {
    private String timestamp;
    private String macAddress;
    private String other;

    public SniffedDevice(String timestamp, String macAddress, String other) {
        this.timestamp = timestamp;
        this.macAddress = macAddress;
        this.other = other;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }
}
