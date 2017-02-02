package uk.gov.dvla.osg.msclookup.interfaces;


import java.util.ArrayList;
import java.util.List;

import uk.gov.dvla.osg.msclookup.Addresses;

public interface LookupDps {

	public ArrayList<Addresses> getDps(ArrayList<Addresses> adds);

}
