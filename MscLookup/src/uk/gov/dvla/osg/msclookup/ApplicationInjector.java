package uk.gov.dvla.osg.msclookup;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.gov.dvla.osg.msclookup.implementations.DpsFromQAS;
import uk.gov.dvla.osg.msclookup.implementations.RmMscLookup;
import uk.gov.dvla.osg.msclookup.interfaces.LookupDps;
import uk.gov.dvla.osg.msclookup.interfaces.LookupMsc;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class ApplicationInjector extends AbstractModule {
	
	private Properties configProps;
	private String input;
	private OutputStream output;
	private String propPath ="mscLookup.properties";
	
	public ApplicationInjector(String str){
		input=str;
	}
	
	//Define logger
	private static final Logger LOGGER = LogManager.getLogger(Main.class.getName());
		
	@Override
	protected void configure() {
		configProps = new Properties();
		
		try {
			configProps.load(new FileInputStream(input));
	        Names.bindProperties(binder(), configProps);
	    } catch (FileNotFoundException e) {
	    	LOGGER.fatal("The configuration file mscLookup.properties can not be found. {}",e.getMessage());

	    } catch (IOException e) {
	    	LOGGER.fatal("I/O Exception during loading configuration {}",e.getMessage());
	        
	    }
		
		bind(LookupMsc.class).to(RmMscLookup.class);
		bind(LookupDps.class).to(DpsFromQAS.class);
		
	}
	
	public void writeProperty(String property, String val){
		try {
			output = new FileOutputStream(propPath);
			configProps.setProperty(property, val);
			configProps.store(output, null);
			output.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

}
