package uk.gov.dvla.osg.msclookup.implementations;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

import uk.gov.dvla.osg.msclookup.interfaces.LookupMsc;
import uk.gov.osg.msclookup.Main;

public class RmMscLookup implements LookupMsc{

	private static final Logger LOGGER = LogManager.getLogger(Main.class.getName());
	private String lookupFile = "C:\\Users\\dendlel\\Desktop\\RPD\\mscLookup\\MSC_LIST.DAT";
	private Map<String, String> mscs;
	private String delim = "\\|";

	LookupMsc lookupMsc;
	@Inject
	public RmMscLookup(LookupMsc lookupMsc)
	{
		lookupMsc = lookupMsc;
		init_RmMscLookup();
	}
	private void init_RmMscLookup(){
		mscs = new Hashtable<String, String>();
		String line;
		String[] parts;
		try {
			FileInputStream fis = new FileInputStream(lookupFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			while ( (line = br.readLine()) != null ){
				parts=line.split(delim);
				
				mscs.put(parts[0], parts[1]);
			}
			LOGGER.info("Map contains {} entries",mscs.size());
		} catch (IOException e) {
			LOGGER.fatal(e.getMessage());
			System.exit(1);
		}
	}
	
	public String getMsc(String pc, int noOfZeros){
		if(noOfZeros > 5){
			LOGGER.fatal("Number of zeros passed to getMsc() cannot exceed 5");
			System.exit(1);
		}
		pc = pc.replaceAll("\\s", "");
		String lookupValue = pc.substring(0, pc.trim().length()-3) + " " + 
				pc.substring(pc.trim().length()-3,pc.trim().length()-2);
		lookupValue = String.format("%-6.6s", lookupValue);
		String result = mscs.get(lookupValue);
		
		if(result == null){
			result = "*****";
		}else{
			result = StringUtils.rightPad(result.substring(0,result.length() - noOfZeros), 5, "0");
		}
		
		return result;
	}


	public void setFile(String filepath, String delimitter) {
		this.lookupFile = filepath;
		this.delim = delimitter;
		init_RmMscLookup();
	}

}
