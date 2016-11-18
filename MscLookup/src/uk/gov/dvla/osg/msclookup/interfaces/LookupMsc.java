package uk.gov.dvla.osg.msclookup.interfaces;

public interface LookupMsc {
	
	public void setFile(String filepath, String delimitter);
	public String getMsc(String pc, int noOfZeros);
	
}
