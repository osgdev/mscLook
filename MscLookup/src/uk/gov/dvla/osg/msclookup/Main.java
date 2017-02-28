package uk.gov.dvla.osg.msclookup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
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
		String add1Field ="";
		String add2Field ="";
		String add3Field ="";
		String add4Field ="";
		String add5Field ="";
		String dpsField ="";
		String qasPath ="";
		String qasPrefix="";
		
		if(args.length == 3){
			try{
				input = args[0];
				output = args[1];
				if(new File(args[2]).exists()){
					configProps.load(new FileInputStream(args[2]));
				}else{
					LOGGER.fatal("Log file: '{}' doesn't exist",args[2]);
					System.exit(1);
				}
				
				postCodeField = configProps.getProperty("postCodeField");
				reqFields.add(postCodeField + ",postCodeField,Y");
				mscField = configProps.getProperty("mscField");
				reqFields.add(mscField + ",mscField,N");
				noOfZeros = Integer.parseInt(configProps.getProperty("noOfZerosInResult"));
				reqFields.add(noOfZeros + ",noOfZerosInResult,N");
				docRef = configProps.getProperty("documentReference");
				reqFields.add(docRef + ",documentReference,Y");
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
				reqFields.add(dpsField + ",dpsField,N");
				qasPath = configProps.getProperty("qasFilePath");
				reqFields.add(qasPath + ",qasFilePath,N");
				qasPrefix = configProps.getProperty("qasFilePrefix");
				reqFields.add(qasPrefix + ",qasFilePrefix,N");
				hostIp = configProps.getProperty("hostIpAddress");
				reqFields.add(hostIp + ",hostIpAddress,N");
				hostUser = configProps.getProperty("hostUser");
				reqFields.add(hostUser + ",hostUser,N");

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
					+ "Input file\nOutput file\nPath to properties files", args.length);
			System.exit(1);
		}
		
		//Build container
		Injector injector = Guice.createInjector(new ApplicationInjector(args[2]));     
		//Use container
		LookupMsc lmsc = injector.getInstance(LookupMsc.class);
		LookupDps ldps = injector.getInstance(LookupDps.class);
		
		RpdFileHandler fh = new RpdFileHandler(input, output);
		
		try {
			/*//Define input csv
			FileReader in = new FileReader(input);
			CSVFormat inputFormat= CSVFormat.RFC4180.withFirstRecordAsHeader();
			
			//Define output csv
			Appendable out = new FileWriter(output);
			CSVFormat outputFormat = CSVFormat.RFC4180.withQuoteMode(QuoteMode.ALL);
			CSVPrinter printer = new CSVPrinter(out, outputFormat);
		
			//Get Headers from csv
			CSVParser csvFileParser = new CSVParser(in, inputFormat);
			Map<String, Integer> headers = csvFileParser.getHeaderMap();*/
			
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
			//printer.printRecord(docRef,mscField,dpsField);
			
			//Write records out
			//Iterable<CSVRecord> records = csvFileParser.getRecords();
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
						split[fileMap.get(add1Field)], 
						split[fileMap.get(add2Field)], 
						split[fileMap.get(add3Field)], 
						split[fileMap.get(add4Field)], 
						split[fileMap.get(add5Field)], 
						split[fileMap.get(postCodeField)]);
				add.setMsc(lmsc.getMsc(split[fileMap.get(postCodeField)], noOfZeros));
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
			
			
		/*	for (CSVRecord record : records) {
				Addresses add = new Addresses(i,
						record.get(docRef),
						record.get(add1Field), 
						record.get(add2Field), 
						record.get(add3Field), 
						record.get(add4Field), 
						record.get(add5Field), 
						record.get(postCodeField));
				add.setMsc(lmsc.getMsc(record.get(postCodeField), noOfZeros));
				adds.add(add);
				i ++;
			}*/

			/*for(Addresses add : ldps.getDps(adds)){
				printer.printRecord((Object[])add.print());
			}*/
			
			/*for(Addresses add : adds){
				printer.printRecord((Object[])add.print());
			}*/
			fh.closeFile();
			//csvFileParser.close();
			//printer.close();

		} catch (IOException e) {
			LOGGER.fatal(e.getMessage());
			System.exit(1);
		}
	}
}
