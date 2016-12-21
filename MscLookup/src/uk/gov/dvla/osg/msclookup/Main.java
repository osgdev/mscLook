package uk.gov.dvla.osg.msclookup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
		
		String input = "";
		String output = "";
		String postCodeField = "";
		String resultField = "";
		int noOfZeros = 0;
		
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

				postCodeField = configProps.getProperty("postCodeFieldName");
				resultField = configProps.getProperty("resultFieldName");
				noOfZeros = Integer.parseInt(configProps.getProperty("noOfZerosInResult"));
				
			}catch (NumberFormatException e){
				LOGGER.fatal(e.getMessage());
				System.exit(1);
			} catch (FileNotFoundException e) {
				LOGGER.fatal(e.getMessage());
				System.exit(1);
			} catch (IOException e) {
				LOGGER.fatal(e.getMessage());
				System.exit(1);
			}
		}else{
			LOGGER.fatal("Incorrect number of args ({}) passed to app. Required args are:\n"
					+ "Input file\nOutput file\nPath to properties files", args.length);
			System.exit(1);
		}

		LOGGER.debug("Input file set to '{}'",input);
		LOGGER.debug("Output file set to '{}'",output);
		LOGGER.debug("postCodeField set to '{}'",postCodeField);
		LOGGER.debug("resultField set to '{}'",resultField);
		LOGGER.debug("NoOfZeros set to '{}'",noOfZeros);
		
		//Build container
		Injector injector = Guice.createInjector(new ApplicationInjector(args[2]));        
		//Use container
		LookupMsc lmsc = injector.getInstance(LookupMsc.class);
		
		try {
			String result;
			//Define input csv
			FileReader in = new FileReader(input);
			CSVFormat inputFormat= CSVFormat.RFC4180.withFirstRecordAsHeader();
			
			//Define output csv
			Appendable out = new FileWriter(output);
			CSVFormat outputFormat = CSVFormat.RFC4180.withQuoteMode(QuoteMode.ALL);
			CSVPrinter printer = new CSVPrinter(out, outputFormat);
		
			//Get Headers from csv
			CSVParser csvFileParser = new CSVParser(in, inputFormat);
			Map<String, Integer> headers = csvFileParser.getHeaderMap();
			
			List<String> heads = new ArrayList<String>();
			for(Map.Entry<String,Integer> en : headers.entrySet()){
				heads.add(en.getKey());
			}

			LOGGER.debug(heads);
			if( !(heads.contains(postCodeField)) ){
				LOGGER.fatal("'{}' is not a field in input file '{}'",postCodeField, input);
			}
			if( !(heads.contains(resultField)) ){
				LOGGER.fatal("'{}' is not a field in input file '{}'",resultField, input);
			}
			//Write headers out
			printer.printRecord(heads);
			
			//Write records out
			Iterable<CSVRecord> records = csvFileParser.getRecords();
			List<String> results = new ArrayList<String>();
			String fieldVal;
			for (CSVRecord record : records) {
				for(String field : heads){
					if(field.equalsIgnoreCase(resultField)){
						String pc = record.get(postCodeField);
					    result=lmsc.getMsc(pc, noOfZeros);
					    LOGGER.debug("input postcode = '{}' result = '{}'",pc ,result);
					    fieldVal=result;
					}else{
						fieldVal = record.get(field);
					}
					results.add(fieldVal);
				}

			    printer.printRecord(results);
			    results.clear();
			}

			csvFileParser.close();
			printer.close();

		} catch (IOException e) {
			LOGGER.fatal(e.getMessage());
			System.exit(1);
		}
	}
}
