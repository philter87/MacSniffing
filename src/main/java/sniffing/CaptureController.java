package sniffing;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.*;

@RestController
public class CaptureController {
    private Process p;
    private boolean shouldWeRerun=true;
    private int lineCount=500;
    private String[] arguments = new String[]{"tshark", "-i", "wlan1", "-I", "-c", ""+lineCount,"-T", "fields","-e","frame.time_epoch","-e", "wlan.sa", "-e", "wlan.fc.type_subtype","-Y","wlan.sa and wlan.fc.type_subtype == 4"};
    private Thread thread;

    @RequestMapping("/start")
    public String start() throws IOException, InterruptedException {
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
            handleOutput();
        }
    }

    private void handleOutput() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = reader.readLine();
        while(line != null){
            System.out.println(line);
            String[] words=getWords(line);
            line = reader.readLine();
        }
    }
    private String[] getWords(String line){
        return line.split("\\t");
    }

    @RequestMapping("/count")
    public long count(){
        return 0;
    }

    @RequestMapping("/count/{minutes}")
    public long count(@PathVariable int minutes){
        return 0;
    }

    @RequestMapping("/ips")
    public List<String> getIds(){
        return Arrays.asList("");
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
