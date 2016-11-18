package uk.gov.osg.msclookup;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import uk.gov.dvla.osg.msclookup.implementations.RmMscLookup;
import uk.gov.dvla.osg.msclookup.interfaces.LookupMsc;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
	
	private static final Logger LOGGER = LogManager.getLogger(Main.class.getName());

	public static void main(String[] args) {
		String input = "";
		String output = "";
		String lookup = "C:\\Users\\dendlel\\Desktop\\RPD\\mscLookup\\MSC_LIST.DAT";
		
		if(args.length == 0){
			input = "C:\\Users\\dendlel\\Desktop\\DataMakerResources\\OTHER.TEST.1.DAT";
			output = "C:\\Users\\dendlel\\Desktop\\DataMakerResources\\UPDATED.DPF.DAT";
		}else{
			input = args[0];
			output = args[1];
		}

		//Build container
		Injector injector = Guice.createInjector(new ApplicationInjector());        
		//Use container
		LookupMsc lmsc = injector.getInstance(LookupMsc.class);
		lmsc.setFile(lookup,"\\|");
		
		try {
			String line;
			String[] parts;
			FileInputStream fis = new FileInputStream(input);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			while ( (line = br.readLine()) != null ){
				parts=line.split(",");
				LOGGER.info("MSC = '{}'", lmsc.getMsc(parts[10], 2));
			}
		} catch (IOException e) {
			LOGGER.fatal(e.getMessage());
		}
		
	}

}
