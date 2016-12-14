package uk.gov.dvla.osg.msclookup;

import com.google.inject.AbstractModule;

import uk.gov.dvla.osg.msclookup.implementations.*;
import uk.gov.dvla.osg.msclookup.interfaces.*;

public class ApplicationInjector extends AbstractModule {

	@Override
	protected void configure() {
		
		bind(LookupMsc.class).to(RmMscLookup.class);
		
	}

}
