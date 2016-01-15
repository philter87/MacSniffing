package sniffing;

import java.sql.Timestamp;
import java.util.*;

/**
 * Created by nordea on 14-01-2016.
 */
public class ListOfSniffedDevices {
    // key String is mac address
    // table of String contains:
    // the first String is the timestamp when this mac address has been sniffed for the first time
    // the second String is the timestamp of the latest sniff
    private Map<String, String[]> sniffedDevices;

    public ListOfSniffedDevices() {
        sniffedDevices = new HashMap<String, String[]>();
    }

    public void addSniffedDevice(String macAddress, String timestamp) {
        boolean isPresent = false;
        for (String key : sniffedDevices.keySet()) {
            if (key.equals(macAddress)) {
                isPresent = true;
                String[] device = sniffedDevices.get(key);
                device[1] = timestamp;
                return;
            }
        }
        if (!isPresent) {
            sniffedDevices.put(macAddress, new String[] {timestamp, timestamp});
        }
    }

    /**
     * We return only the current devices.
     * For this, we check the earliest timestamp: if it's within the last 30 minutes, we return it.
     * If not, we check the latest timestamp when it has been scanned: if it's within the last 10 minutes, we return it.
     * Otherwise, if none of these conditions are respected, the device is not returned.
     * @return list of current devices
     */
    public List<String> getCurrentDevices() {
        List<String> results = new ArrayList<String>();
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis()/1000L; /* millis -> sec */
        for (String macAddress : sniffedDevices.keySet()) {
            String firstTimestampRaw = sniffedDevices.get(macAddress)[0];
            String latestTimestampRaw = sniffedDevices.get(macAddress)[1];
            System.out.println(macAddress + ": firstTimestampRaw : " + firstTimestampRaw + ",latestTimestampRaw : " + latestTimestampRaw);
            //results.add(macAddress);
           if (now - ((long) Double.parseDouble(firstTimestampRaw)) < 10 * 60 ) {
                results.add(macAddress);
                System.out.println("Added first timestamp" + macAddress);
            } else {
                if (now - ((long) Double.parseDouble(latestTimestampRaw)) < 5 * 60 ) {
                    results.add(macAddress);
                    System.out.println("Added latest timestamp" + macAddress);
                }
            }

        }
        return results;
    }
}
