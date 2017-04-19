package jshell.upgrader;

import com.sun.nio.zipfs.ZipPath;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

/**
 * @author Nikhil
 */
public class Update {
    
    static Scanner key;
    static String website;
    static String JShell_path;
    static String[] required_dirs = {"./BACKUPS", "./UPDATES"};
    
    public static void main(String[] args) {
        try{
            mkdir();
            if(args[0].equals("-u")){
                update();
            } else if (args[0].equals("-r")){
                restore();
            }
        } catch (ArrayIndexOutOfBoundsException ex){
            System.out.println("Invalid PARAMS");
        }
    }
    
    public static void restore(){
        try {
            key = new Scanner(System.in);
            System.out.println("BACKUP-DATE:");
            String[] date = key.nextLine().split(" ");
            String year = date[0];
            String month = date[1];
            String day = date[2];
            System.out.println("BACKUP-TIME:");
            String[] time = key.nextLine().split(" ");
            String hour = time[0];
            String min = time[1];
            String sec = time[2];
            ZipFile zipFile = new ZipFile("BACKUPS/"+year+"-"+month+"-"+day+"-"+hour+"-"+min+"-"+sec+".zip");
            System.out.println("JShell Path:");
            JShell_path = key.nextLine();
            String parent = new File(JShell_path).getParent();
            zipFile.extractAll(parent);
        } catch (ZipException ex) {
            Logger.getLogger(Update.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void update(){
        try {
            key = new Scanner(System.in);
            System.out.print("Update Website: ");
            website = key.nextLine() + "/jshell/Latest.zip";
            System.out.println("Fetching required file");
            ZipFile zf = new ZipFile(fetchfile());
            System.out.println("Fetched required file");
            System.out.println("Checking for Encryption");
            if(zf.isEncrypted()){
                System.out.print("ZIPPass: ");
                zf.setPassword(key.nextLine());
            }
            System.out.println("JShell Path:");
            JShell_path = key.nextLine();
            BackUp();
            System.out.println("Backup Finished");
            System.out.println("Extracting...");
            zf.extractAll(JShell_path);
        } catch (ZipException ex) {
            Logger.getLogger(Update.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void mkdir(){
        for (String str : required_dirs){
                File file = new File(str);
                if(!file.exists()){
                    file.mkdir();
                }
            }
    }
    
    public static File fetchfile(){
        String file_path = null;
        try {
            URL website = new URL(Update.website);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            file_path = "UPDATES/"+getSystemDate()+".zip";
            FileOutputStream fos = new FileOutputStream(file_path);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (IOException ex) {
            Logger.getLogger(Update.class.getName()).log(Level.SEVERE, "Error Occured", ex);
        }
        File f = new File(file_path);
        System.out.println("Successfully tranferred " + f.length() + " bytes");
        return f;
    }
    
    public static String getSystemDate(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
    
    public static void BackUp(){
        try {
            ZipFile zipFile = new  ZipFile("BACKUPS/" + getSystemDate() + ".zip");
            ZipParameters zp = new ZipParameters();
            zp.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            zp.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
            zipFile.addFolder(JShell_path, zp);
        } catch (ZipException ex) {
            Logger.getLogger(Update.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
