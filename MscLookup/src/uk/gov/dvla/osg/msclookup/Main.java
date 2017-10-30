package uk.gov.dvla.osg.msclookup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.gov.dvla.osg.common.classes.RpdFileHandler;
import uk.gov.dvla.osg.msclookup.interfaces.LookupDps;
import uk.gov.dvla.osg.msclookup.interfaces.LookupMsc;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Main {
	//Define logger
	private static final Logger LOGGER = LogManager.getLogger(Main.class.getName());
	
	public static void main(String[] args) {

		LOGGER.info("Starting uk.gov.dvla.osg.msclookup.Main");
		LOGGER.debug("{} args passed ",args.length);
		
		Properties configProps = new Properties();
		List<String> reqFields = new ArrayList<String>();
		
		String input = "";
		String output = "";
		String postCodeField = "";
		String mscField = "";
		String docRef = "";
		int noOfZeros = 0;
		String hostIp ="";
		String hostUser ="";
		String hostDestination = "";
		String returnDir = "";
		String name1 ="";
		String name2 ="";
		String add1Field ="";
		String add2Field ="";
		String add3Field ="";
		String add4Field ="";
		String add5Field ="";
		String dpsField ="";
		String qasPath ="";
		String qasPrefix="";
		String jobId = "";
		
		if(args.length == 4){
			try{
				input = args[0];
				output = args[1];
				if(new File(args[2]).exists()){
					configProps.load(new FileInputStream(args[2]));
				}else{
					LOGGER.fatal("Log file: '{}' doesn't exist",args[2]);
					System.exit(1);
				}
				jobId = args[3];
				
				postCodeField = configProps.getProperty("postCodeField");
				reqFields.add(postCodeField + ",postCodeField,Y");
				mscField = configProps.getProperty("mscField");
				reqFields.add(mscField + ",mscField,Y");
				noOfZeros = Integer.parseInt(configProps.getProperty("noOfZerosInResult"));
				reqFields.add(noOfZeros + ",noOfZerosInResult,N");
				docRef = configProps.getProperty("documentReference");
				reqFields.add(docRef + ",documentReference,Y");
				name1 = configProps.getProperty("name1Field");
				reqFields.add(name1 + ",name1Field,Y");
				name2 = configProps.getProperty("name2Field");
				reqFields.add(name2 + ",name2Field,Y");
				add1Field = configProps.getProperty("address1Field");
				reqFields.add(add1Field + ",address1Field,Y");
				add2Field = configProps.getProperty("address2Field");
				reqFields.add(add2Field + ",address2Field,Y");
				add3Field = configProps.getProperty("address3Field");
				reqFields.add(add3Field + ",address3Field,Y");
				add4Field = configProps.getProperty("address4Field");
				reqFields.add(add4Field + ",address4Field,Y");
				add5Field = configProps.getProperty("address5Field");
				reqFields.add(add5Field + ",address5Field,Y");
				dpsField = configProps.getProperty("dpsField");
				reqFields.add(dpsField + ",dpsField,Y");
				qasPath = configProps.getProperty("qasFilePath");
				reqFields.add(qasPath + ",qasFilePath,N");
				qasPrefix = configProps.getProperty("qasFilePrefix");
				reqFields.add(qasPrefix + ",qasFilePrefix,N");
				hostIp = configProps.getProperty("hostIpAddress");
				reqFields.add(hostIp + ",hostIpAddress,N");
				hostUser = configProps.getProperty("hostUser");
				reqFields.add(hostUser + ",hostUser,N");
				hostDestination = configProps.getProperty("hostDestination");
				reqFields.add(hostDestination + ",hostDestination,N");
				returnDir = configProps.getProperty("returnDir");
				reqFields.add(returnDir + ",returnDir,N");
			}catch (NumberFormatException e){
				LOGGER.fatal("NumberFormatException:{}",e.getMessage());
				System.exit(1);
			} catch (FileNotFoundException e) {
				LOGGER.fatal("FileNotFoundException:{}",e.getMessage());
				System.exit(1);
			} catch (IOException e) {
				LOGGER.fatal("IOException:{}",e.getMessage());
				System.exit(1);
			}
		}else{
			LOGGER.fatal("Incorrect number of args ({}) passed to app. Required args are:\n"
					+ "Input file\nOutput file\nPath to properties files\njobID", args.length);
			System.exit(1);
		}
		
		//Build container
		Injector injector = Guice.createInjector(new ApplicationInjector(args[2]));     
		//Use container
		LookupMsc lmsc = injector.getInstance(LookupMsc.class);
		LookupDps ldps = injector.getInstance(LookupDps.class);
		
		RpdFileHandler fh = new RpdFileHandler(input, output);
		
		try {
			List<String> inputHeaders = fh.getHeaders();

			for(String str : reqFields){
				String[] split = str.split(",");
				if ( "null".equals(split[0])){
					LOGGER.fatal("Field '{}' not in properties file {}.",split[1],args[2]);
					System.exit(1);
				}else{
					if( !(inputHeaders.contains(split[0])) && "Y".equals(split[2]) ){
						LOGGER.fatal("Field '{}' not found in input file {}.",split[0],input);
						System.exit(1);
					}
				}
			}
			
			//Write headers out
			fh.write(fh.getHeaders());
			HashMap<String,Integer> fileMap = fh.getMapping();
			
			//Write records out
			ArrayList<Addresses> adds = new ArrayList<Addresses>();
			
			File f = new File(input);
            BufferedReader b = new BufferedReader(new FileReader(f));
            String readLine = b.readLine();
            
            LOGGER.debug("Read line as header '{}'",readLine);
            int i = 1;
            
            while ((readLine = b.readLine()) != null) {
            	String[] split = readLine.split("\\t",-1);
            	Addresses add = new Addresses(i,
						split[fileMap.get(docRef)],
						split[fileMap.get(name1)],
						split[fileMap.get(name2)],
						split[fileMap.get(add1Field)], 
						split[fileMap.get(add2Field)], 
						split[fileMap.get(add3Field)], 
						split[fileMap.get(add4Field)], 
						split[fileMap.get(add5Field)], 
						split[fileMap.get(postCodeField)]);
				add.setMsc(lmsc.getMsc(split[fileMap.get(postCodeField)], noOfZeros));
				add.setJobId(jobId);
				adds.add(add);
				i ++;
            }
            
            b.close();
			
            
            int mscIdx = fileMap.get(mscField);
            int dpsIdx = fileMap.get(dpsField);
            
            //Output
            BufferedReader bu = new BufferedReader(new FileReader(f));
            readLine = bu.readLine();
            List<String> list = new ArrayList<String>();
            
            List<Addresses> results = ldps.getDps(adds);
            
            i = 0;
            while ((readLine = bu.readLine()) != null) {
            	String[] split = readLine.split("\\t",-1);
            	list.clear();
            	for( int x = 0; x < split.length; x ++ ){
					if( x == mscIdx ){
						if( results.get(i).getMsc() != null){
							list.add(results.get(i).getMsc());
						}else{
							list.add("");
						}
						
					} else if( x == dpsIdx ){
						if( results.get(i).getDps() != null){
							list.add(results.get(i).getDps());
						}else{
							list.add("");
						}
					} else {
						list.add(split[x]);
					}
				}
				fh.write(list);
            	i ++;
            }
			
			fh.closeFile();
			bu.close();

		} catch (IOException e) {
			LOGGER.fatal(e.getMessage());
			System.exit(1);
		}
	}
}
