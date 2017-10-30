package uk.gov.dvla.osg.msclookup.implementations;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.gov.dvla.osg.msclookup.Addresses;
import uk.gov.dvla.osg.msclookup.Main;
import uk.gov.dvla.osg.msclookup.interfaces.LookupDps;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class DpsFromQAS implements LookupDps {
	
	private static final Logger LOGGER = LogManager.getLogger(Main.class.getName());
	private String qasFilePath, qasFilePrefix, hostname, hostDestination, username, remoteDirPath, returnDir;
	
	@Inject
	public DpsFromQAS(LookupDps lookupDps, 
			@Named("qasFilePath") String qasFilePath, 
			@Named("qasFilePrefix") String qasFilePrefix,
			@Named("hostIpAddress") String hostname,
			@Named("hostDestination") String hostDestination,
			@Named("hostUser") String username,
			@Named("returnDir") String returnDir,
			@Named("remoteDirPath") String remoteDirPath){
		this.qasFilePath=qasFilePath;
		this.qasFilePrefix=qasFilePrefix;
		this.hostDestination=hostDestination;
		this.hostname=hostname;
		this.username=username;
		this.returnDir=returnDir;
		this.remoteDirPath=remoteDirPath;
	}

	@Override
	public ArrayList<Addresses> getDps(ArrayList<Addresses> adds) {
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
		//final SimpleDateFormat eotsdf = new SimpleDateFormat("ddMMyyyy");
		//Generate timestamp
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		//String eotTimeStamp = eotsdf.format(timestamp);
		
		UUID uuid = java.util.UUID.randomUUID();
		
        String outboundFileName = qasFilePrefix + adds.get(0).getJobId().trim() + "." + uuid.toString()  + ".DAT";
        //String eotFileName = qasFilePrefix + adds.get(0).getJobId().trim() + "." + uuid.toString()  + ".EOT";
        String outboundFilepath = qasFilePath + outboundFileName;
        //String outboundEotFilepath = qasFilePath + eotFileName;
        String filePath = remoteDirPath + "RETURN." + outboundFileName;
        String localPath = returnDir + "RETURN." + outboundFileName;

        try {
        	LOGGER.info("Address array size={}",adds.size());
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outboundFilepath,false)));
			//PrintWriter pw2 = new PrintWriter(new BufferedWriter(new FileWriter(outboundEotFilepath,false)));
			for(Addresses add : adds){
				pw.println(add);
			}
			pw.close();
			
			//pw2.println("RUNDATE=" + eotTimeStamp);
			//pw2.close();
			
			String cmd = "scp -p " + outboundFilepath + " " + username + "@" + hostname + ":" + hostDestination;
			LOGGER.info("TRANSFER COMMAND='{}'",cmd);
			
			Process sendDat = Runtime.getRuntime().exec(cmd);
			int rc = sendDat.waitFor();
			LOGGER.info("TRANSFER RC={}",rc);
			if(rc != 0){
				LOGGER.fatal("Transfer of file '{}' failed with code {}",outboundFilepath, rc);
				System.exit(1);
			}
			
			//cmd = "scp -p " + outboundEotFilepath + " " + username + "@" + hostname + ":" + hostDestination;
			//Process sendEot = Runtime.getRuntime().exec(cmd);
			//rc = sendEot.waitFor();
//			if(rc != 0){
//				LOGGER.fatal("Transfer of file '{}' failed with code {}",outboundEotFilepath, rc);
//				System.exit(1);
//			}
			
	        try {
	        	LOGGER.info("Attempting download of '{}' to '{}'",filePath,localPath);

	            boolean exists = false;
	            cmd = "scp -p " + username + "@" + hostname + ":" + filePath + " " + localPath;
				LOGGER.info("TRANSFER COMMAND='{}'",cmd);
				Process getDat;
	            while(!(exists)){
	            	getDat = Runtime.getRuntime().exec(cmd);
	            	rc = getDat.waitFor();
	            	if(rc != 0){
	            		try {
	            			LOGGER.info("File {} not present rc={}, sleeping..",filePath,rc);
	            		    Thread.sleep(10000);
	            		} catch(InterruptedException ex) {
	            		    Thread.currentThread().interrupt();
	            		    LOGGER.error("Interuptted");
	            		    System.exit(1);
	            		}
	            	}else{
	            		LOGGER.info("Succesfully retrieved remote file '{}' to '{}'",filePath,localPath);
	            		exists=true;
	            	}
	            }
			} catch (Exception e) {
				e.printStackTrace();
			}
	        
	        
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.fatal("{}",e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        
        try (BufferedReader br = new BufferedReader(new FileReader(localPath))) {
            String line;
            String qasDps;
            String qasMsc;
            String qasConf;
            int k = 0;
            Integer dpsCount=0;
            Integer recordCount=0;
            while ((line = br.readLine()) != null) {
            	recordCount ++;
            	String[] split = line.split("\\|", -1);
            	qasConf = split[15].substring(2, 3);
            	qasMsc = split[16];
            	if(split.length == 18){
            		qasDps = split[17];
            	}else{
            		qasDps = "";
            	}
            	if( ("1".equals(qasConf)) && !(qasMsc.trim().isEmpty()) ){
            		if( ( !(adds.get(k).getPc().isEmpty()) || !(adds.get(k).getPc()==null) ) ){
            			adds.get(k).setDps(qasDps);
                		adds.get(k).setMsc(qasMsc);
                		dpsCount ++;
            		}else{
            			adds.get(k).setMsc("");
            		}
            	}
            	k ++;
            }
            
            LOGGER.info("Total number of records={}. Total number of DPS records={}",recordCount,dpsCount);
            
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        
		return adds;
	}

	
}
