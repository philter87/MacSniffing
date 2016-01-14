package sniffing;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AppReader {
    public static DateTimeFormatter formatter =DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static void main1(String[] args) throws IOException, InterruptedException {
        while(true) {
            List<File> allFilesExceptNewest = getAllFilesExceptNewest();
            AppWriter.createFolderIfAbsent(AppWriter.COMPACT_LOG_FOLDER);
            for (File file : allFilesExceptNewest) {
                String compressedLog = getCompressedLogFromUncompressedFile(file);
                writeCompressedToNewFile(file, compressedLog);
                file.delete();
                System.out.println(compressedLog);
            }
        }
    }

    private static void writeCompressedToNewFile(File uncompressedFile, String compressedLog) throws IOException {
        String compactFileName=AppWriter.COMPACT_LOG_FOLDER+"compact_"+extractDateFromFileName(uncompressedFile.getName())+".txt";
        FileWriter newFile=new FileWriter(compactFileName);
        BufferedWriter writer = new BufferedWriter(newFile);
        writer.write(compressedLog);
        writer.close();
    }


    private static String getCompressedLogFromUncompressedFile(File file) throws IOException, InterruptedException {
        String uncompressedFilePath=file.getPath();
        System.out.println(uncompressedFilePath);
        String command = "tshark -r "+uncompressedFilePath+" -T fields -e wlan.sa -e wlan.fc.type_subtype -Y \"wlan.fc.type_subtype\"";
        if(AppWriter.isWindows10()){
            command = AppWriter.WIN_PRE+command;
        }
        Process p = Runtime.getRuntime().exec(command);
        int a=p.waitFor();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        StringBuffer output = new StringBuffer();
        String line ="";
        while((line = reader.readLine()) != null){
            output.append(line+"\n");
        }
        return output.toString();
    }


    private static List<File> getAllFilesExceptNewest() {
        File folderOfLogs = new File(AppWriter.LOG_FOLDER);
        File[] allFiles = folderOfLogs.listFiles();
        final String fileToExclude=fileToExclude(allFiles);
        return Arrays.stream(allFiles).filter(file -> !file.getName().equalsIgnoreCase(fileToExclude)).collect(Collectors.toList());
    }

    private static String fileToExclude(File[] allFiles) {
        long newestTime=-1;
        String fileNameToExclude="";
        for(File file:allFiles){
            String name=file.getName();
            long dateNumber=extractDateFromFileName(name);
            if(dateNumber>newestTime){
                fileNameToExclude=name;
            }
        }
        return fileNameToExclude;
    }
    private static long extractDateFromFileName(String fileName){
        String dateString=fileName.substring(fileName.length()-19,fileName.length()-5);
        return Long.parseLong(dateString);
    }
}
