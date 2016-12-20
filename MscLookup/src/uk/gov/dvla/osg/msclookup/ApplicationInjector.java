package uk.gov.dvla.osg.msclookup;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import uk.gov.dvla.osg.msclookup.implementations.RmMscLookup;
import uk.gov.dvla.osg.msclookup.interfaces.LookupMsc;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class ApplicationInjector extends AbstractModule {

	@Override
	protected void configure() {
		Properties configProps = new Properties();
		
		
		try {
			configProps.load(new FileReader("mscLookup.properties"));
	        Names.bindProperties(binder(), configProps);
	    } catch (FileNotFoundException e) {
	        System.out.println("The configuration file Test.properties can not be found");
	    } catch (IOException e) {
	        System.out.println("I/O Exception during loading configuration");
	    }
		
		
		
		
		bind(LookupMsc.class).to(RmMscLookup.class);
		
	}

}
