package sniffing;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class AppWriter {
    public static String WIN_PRE="cmd /c ";
    public static String LOG_FOLDER ="logs/";
    public static String FILE_EXTENSTION =".pcap ";
    public static String COMPACT_LOG_FOLDER="compact/";
    public static String FILE_BASE ="sniffing";
    public static int RECORDING_DURATION_PR_FILE_SEC =5;

    public static void main1(String[] args) throws IOException, InterruptedException {
        createFolderIfAbsent(LOG_FOLDER);
        String command = createRunCommand();

        System.out.println(command);
        Process p = Runtime.getRuntime().exec(command);
        //Process p = Runtime.getRuntime().exec("tshark -c 10");
        int a=p.waitFor();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        StringBuffer output = new StringBuffer();
        String line ="";
        while((line = reader.readLine()) != null){
            output.append(line+"\n");
        }
        System.out.println(output.toString());
    }

    private static String createRunCommand() {
        String tShark="tshark ";
        String wAdapter="-i wlan1 ";
        String filename= LOG_FOLDER + FILE_BASE +FILE_EXTENSTION;
        String fileSetting="-b filesize:30 -b duration:"+ RECORDING_DURATION_PR_FILE_SEC +" -w " + filename;

        String command="";
        if(isWindows10()){
            command=WIN_PRE+tShark+fileSetting;
        } else {
            command=tShark+wAdapter+fileSetting;
        }
        System.out.println(tShark+wAdapter+fileSetting);
        return command;
    }
    public static boolean isWindows10(){
        return System.getProperty("os.name").contentEquals("Windows 10");
    }

    public static void createFolderIfAbsent(String folderName) {
        File file = new File(folderName);
        if(!file.isDirectory()){
            file.mkdir();
        }
    }


}

