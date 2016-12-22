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
import com.google.inject.name.Named;

import uk.gov.dvla.osg.msclookup.Main;
import uk.gov.dvla.osg.msclookup.interfaces.LookupMsc;

public class RmMscLookup implements LookupMsc{

	private static final Logger LOGGER = LogManager.getLogger(Main.class.getName());
	private Map<String, String> mscs;
	private String lookupFile;
	private String delim = "\\|";

	@Inject
	public RmMscLookup(LookupMsc lookupMsc, @Named("lookupFile") String lookupFile)
	{
		this.lookupFile=lookupFile;
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
			br.close();
		} catch (IOException e) {
			LOGGER.fatal(e.getMessage());
			System.exit(1);
		}
	}
	
	public String getMsc(String pc, int noOfZeros){	
		if(noOfZeros > 4){
			LOGGER.fatal("Number of zeros passed to {} cannot exceed 4",this.getClass().getName() +"."+ Thread.currentThread().getStackTrace()[1].getMethodName());
			System.exit(1);
		}
		pc = pc.replaceAll("\\s", "");
		String lookupValue = pc.substring(0, pc.trim().length()-3) + " " + 
				pc.substring(pc.trim().length()-3,pc.trim().length()-2);
		lookupValue = String.format("%-6.6s", lookupValue);
		String result = mscs.get(lookupValue);
		
		if(result == null){
			result = "";
		}else{
			result = StringUtils.rightPad(result.substring(0,result.length() - noOfZeros), 5, "0");
		}
		
		return result;
	}
	
}
