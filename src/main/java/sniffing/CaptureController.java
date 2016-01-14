package sniffing;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class CaptureController {
    private Process p;
    private final String LOG_FOLDER="logs/";
    private final String FILE_PREFIX="probes";
    private boolean shouldWeRerun=true;
    private int lineCount=500;
    private long expire_hour=24;
    private Map<String,Long> ips = new HashMap<>();
    private TreeMap<LocalDateTime,String> ipsTimeSorted = new TreeMap<>();


    //private String tsharkCommand="tshark -i wlan1 -I -c "+lineCount+" -T fields -e frame.time_epoch -e wlan.sa -e wlan.fc.type_subtype";
    //-e wlan.sa_resolved
    private String[] arguments = new String[]{"tshark", "-i", "wlan1", "-I", "-c", ""+lineCount,"-T", "fields","-e","frame.time_epoch","-e", "wlan.sa", "-e", "wlan.fc.type_subtype","-Y","wlan.sa and wlan.fc.type_subtype == 4"};
    private Thread thread;
    private LocalDate today = LocalDate.now();

    @RequestMapping("/start")
    public String start() throws IOException, InterruptedException {
        //Start capturing
        if(thread!=null){
            return "Sniffing was already initiated";
        }
        createThreadWithSniffing();
        return "Sniffing started";
    }

    private void saveIpInfoAndReset() throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        ips.entrySet().stream().forEach(entry -> stringBuffer.append(entry.getKey()+"\t"+entry.getValue()+"\n"));
        createFolderIfAbsent(LOG_FOLDER);
        BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FOLDER+FILE_PREFIX+System.currentTimeMillis()+".txt"));
        writer.write(stringBuffer.toString());
        writer.close();

        ips = new HashMap<>();
        ipsTimeSorted = new TreeMap<>();
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
            resetIpInfoOnNewDate();
            p = Runtime.getRuntime().exec(arguments);
            System.out.println("Process started:"+System.currentTimeMillis());
            p.waitFor();
            System.out.println("Time:"+System.currentTimeMillis()+", "+lineCount+" packets collected");
            StringBuffer output = getProcessOutput();
        }
    }

    private void resetIpInfoOnNewDate() throws IOException {
        if(!today.isEqual(LocalDate.now())){
            System.out.println("New day. Information is reset");
            today = LocalDate.now();
            saveIpInfoAndReset();;
        }
    }

    private StringBuffer getProcessOutput() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuffer output = new StringBuffer();
        String line ="";
        while((line = reader.readLine()) != null){
            System.out.println(line);
            String[] words=getWords(line);
            if(words.length == 3){
                output.append(line+"\n");
                double time=Double.parseDouble(words[0]);
                String ipAddress=words[1];
                String probeType=words[2];
                Probe probe = new Probe(time,ipAddress,probeType);


                Long ipCount=ips.get(ipAddress);
                if( ipCount == null ){
                    ips.put(ipAddress,1l);
                    LocalDateTime now=LocalDateTime.now();

                    //String test=ipsTimeSorted.get(now);
                    String shouldBeNull = ipsTimeSorted.get(now);
                    while(shouldBeNull!=null){
                        now=now.minusNanos(1);
                        shouldBeNull = ipsTimeSorted.get(now);
                        System.out.println("This should not happen. An IP is replaced: "+ipAddress);
                    }
                    ipsTimeSorted.put(now, ipAddress);
                } else {
                    ips.put(ipAddress,ipCount+1);
                }
            }
        }
        return output;
    }
    private String[] getWords(String line){
        return line.split("\\t");
    }

    @RequestMapping("/count")
    public long count(){
        return ips.size();
    }
    @RequestMapping("/count/{minutes}")
    public long count(@PathVariable int minutes){
        LocalDateTime lowerLimit = LocalDateTime.now().minusMinutes(minutes);
        return ipsTimeSorted.subMap(lowerLimit,LocalDateTime.now()).size();
    }

    @RequestMapping("/ips")
    public List<String> getIds(){
        return ipsTimeSorted.entrySet().stream().map(entry -> entry.getValue()+"\t"+entry.getKey()).collect(Collectors.toList());
    }

    @RequestMapping("/ips/count")
    public List<String> getIds1(){
        return ips.entrySet().stream().map(entry -> entry.getKey()+"\t"+entry.getValue()).collect(Collectors.toList());
    }

    @RequestMapping("/ips/{minutes}")
    public List<String> getIds(@PathVariable int minutes){
        return ipsTimeSorted.subMap(LocalDateTime.now().minusMinutes(minutes),LocalDateTime.now()).entrySet().stream().map(entry -> entry.getValue()+"\t"+entry.getKey()).collect(Collectors.toList());
    }

    @RequestMapping("/old/{minutes}")
    public List<String> getOldIdsMinutes(@PathVariable int minutes) throws IOException {
        List<String> probes= new ArrayList<>();
        long substractMs=minutes*60*1000;
        long fromMs=System.currentTimeMillis()-substractMs;
        File folderOfLogs = new File(LOG_FOLDER);
        File[] allFiles = folderOfLogs.listFiles();
        List<File> filesSince=Arrays.stream(allFiles).filter(file -> ( Long.parseLong(file.getName().substring(FILE_PREFIX.length(),file.getName().length()-4)) > fromMs)).collect(Collectors.toList());
        System.out.println("Files included:"+filesSince);
        for(File file:filesSince){
            if(file.canRead()){
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line=reader.readLine();
                while(line!=null){
                    probes.add(line);
                    line=reader.readLine();
                }
            }
        }
        return probes;
    }

    private static void createFolderIfAbsent(String folderName) {
        File file = new File(folderName);
        if(!file.isDirectory()){
            file.mkdir();
        }
    }

    @RequestMapping("/stop")
    public String stop() throws IOException {
        saveIpInfoAndReset();
        shouldWeRerun=false;
        thread.interrupted();
        thread=null;
        p.destroyForcibly();
        return "sniffing stopped";
    }
}
