package sniffing;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.*;

@CrossOrigin()
@RestController
@CrossOrigin(origins = "http://localhost:9000")
public class CaptureController {
    private Process p;
    private boolean shouldWeRerun=true;
    private int lineCount=50;
    private String[] arguments = new String[]{"tshark", "-i", "wlan1", "-I", "-c", ""+lineCount,"-T", "fields","-e","frame.time_epoch","-e", "wlan.sa", "-e", "wlan.sa_resolved","-Y","wlan.sa and wlan.fc.type_subtype == 4"};
    private Thread thread;
    //private List<SniffedDevice> savedLines;
    private Map<String, String> tmpSavedLines;
    private List<String> blackListedDevices;
    private ListOfSniffedDevices allDevices = new ListOfSniffedDevices();
    private int counter_blackListingSniffing = 1; // we sniff 5 times to establish the black list
    //private IntBuffer map;

    @RequestMapping("/start")
    public String start() throws IOException, InterruptedException {
        blackListedDevices = new ArrayList<String>();
        allDevices = new ListOfSniffedDevices();
        //Start capturing
        if(thread!=null){
            return "Sniffing was already initiated";
        }
        createThreadWithSniffing();
        return "Sniffing started";
    }

    public void createThreadWithSniffing(){
        shouldWeRerun=true;
        thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    startSniffing();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void startSniffing() throws IOException, InterruptedException {
        while(shouldWeRerun){
            p = Runtime.getRuntime().exec(arguments);
            System.out.println("Process started:"+System.currentTimeMillis());
            p.waitFor();
            System.out.println("Time:"+System.currentTimeMillis()+", "+lineCount+" packets collected");
            tmpSavedLines = new HashMap<String, String>();
            handleOutput();
        }
    }

    private void handleOutput() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = reader.readLine();
        SniffedDevice sniffedDevice;

        // Black listing, for the 5 first sniffings
        if (counter_blackListingSniffing != 0) {
            System.out.println("Black listing starts now");
            while(line != null){
                String[] words=getWords(line);
                StringBuilder sb = new StringBuilder("New black listed device : ");
                for(String word : words) {
                    sb.append(word).append(" - ");
                }
                System.out.println(sb.toString());
                if (!blackListedDevices.contains(words[1]))
                    blackListedDevices.add(words[1]);
                line = reader.readLine();
            }
            System.out.println("Black listing is now over");
            counter_blackListingSniffing--;
            //savedLines = new HashMap<String, String>();
            // TODO tell that the blacklisting is in progress
            //tmpSavedLines.put("black listing in progress", "");
            return;

        }

        // real sniffing
        while(line != null){
            //System.out.println(line);
            String[] words=getWords(line);
            System.out.println("words length : " + words.length);
            StringBuilder sb = new StringBuilder("New Line : ");
            for(String word : words) {
                sb.append(word).append(" - ");
            }
            sniffedDevice = new SniffedDevice(words[0], words[1], words[2]);
            if (!blackListedDevices.contains(sniffedDevice.getMacAddress())) {
                tmpSavedLines.put(sniffedDevice.getMacAddress(), sniffedDevice.getTimestamp());
            }
            System.out.println(sb.toString());
            line = reader.readLine();
        }

        mergeLines();
    }
    private String[] getWords(String line){
        return line.split("\\t");
    }

    private void mergeLines() {
        for (String s : tmpSavedLines.keySet()) {
            allDevices.addSniffedDevice(s, tmpSavedLines.get(s));
        }
    }

    @RequestMapping("/count")
    public long count(){
        return allDevices.getCurrentDevices().size();
    }

    @RequestMapping("/count/{minutes}")
    public long count(@PathVariable int minutes){
        return 0;
    }

    @RequestMapping("/ips")
    public List<String> getIds(){
        return allDevices.getCurrentDevices();
    }

    @RequestMapping("/ips/count")
    public List<String> getIds1(){
        return Arrays.asList("");
    }

    @RequestMapping("/ips/{minutes}")
    public List<String> getIds(@PathVariable int minutes){
        return Arrays.asList("");
    }

    @RequestMapping("/stop")
    public String stop() throws IOException {
        shouldWeRerun=false;
        thread.interrupted();
        thread=null;
        p.destroyForcibly();
        return "sniffing stopped";
    }
}
