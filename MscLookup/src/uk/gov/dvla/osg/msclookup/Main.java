package uk.gov.dvla.osg.msclookup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.gov.dvla.osg.msclookup.interfaces.LookupMsc;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Main {
	
	private static final Logger LOGGER = LogManager.getLogger(Main.class.getName());

	public static void main(String[] args) {

		LOGGER.info("Starting uk.gov.dvla.osg.msclookup.Main");
		LOGGER.debug("{} args passed ",args.length);
		String input = "";
		String output = "";
		String delim = "";
		int position = 0;
		int noOfZeros = 0;
		
		if(args.length == 5){
			try{
				input = args[0];
				output = args[1];
				delim = args[2];
				position = Integer.parseInt(args[3]);
				noOfZeros = Integer.parseInt(args[4]);
			}catch (NumberFormatException e){
				LOGGER.fatal(e.getMessage());
				System.exit(1);
			}
		}else{
			LOGGER.fatal("Incorrect number of args ({}) passed to app. "
					+ "Required args are: input file, output file, input"
					+ " file delimitter, postcode position (base0), number "
					+ "of zeros in result", args.length);
			System.exit(1);
		}

		LOGGER.debug("Input file set to '{}'",input);
		LOGGER.debug("Output file set to '{}'",output);
		LOGGER.debug("Delimitter set to '{}'",delim);
		LOGGER.debug("Position set to '{}'",position);
		LOGGER.debug("NoOfZeros set to '{}'",noOfZeros);
		
		//Build container
		Injector injector = Guice.createInjector(new ApplicationInjector());        
		//Use container
		LookupMsc lmsc = injector.getInstance(LookupMsc.class);
		
		try {
			String line;
			String[] parts;
			String result;
			FileInputStream fis = new FileInputStream(input);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			
			
			try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(output))) {
				while ( (line = br.readLine()) != null ){
					parts=line.split(delim);
					result=lmsc.getMsc(parts[position], noOfZeros);
					LOGGER.trace("MSC = '{}'",result);
					
			            writer.write(line + "," + result + "\n");
			        
				}
			} catch (IOException e){
	        	LOGGER.fatal(e.getMessage());
				System.exit(1);
	        }
		} catch (IOException e) {
			LOGGER.fatal(e.getMessage());
			System.exit(1);
		}
		
	}

}
