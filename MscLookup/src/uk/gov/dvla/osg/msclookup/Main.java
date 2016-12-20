package uk.gov.dvla.osg.msclookup;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
		String input = "";
		String output = "";
		String postCodeField = "";
		String resultField = "";
		int noOfZeros = 0;
		
		if(args.length == 5){
			try{
				input = args[0];
				output = args[1];
				postCodeField = args[2];
				resultField = args[3];
				noOfZeros = Integer.parseInt(args[4]);
			}catch (NumberFormatException e){
				LOGGER.fatal(e.getMessage());
				System.exit(1);
			}
		}else{
			LOGGER.fatal("Incorrect number of args ({}) passed to app. "
					+ "Required args are: input file, output file, postCodeField name, "
					+ "resultField name, number "
					+ "of zeros in result", args.length);
			System.exit(1);
		}

		LOGGER.debug("Input file set to '{}'",input);
		LOGGER.debug("Output file set to '{}'",output);
		LOGGER.debug("postCodeField set to '{}'",postCodeField);
		LOGGER.debug("resultField set to '{}'",resultField);
		LOGGER.debug("NoOfZeros set to '{}'",noOfZeros);
		
		//Build container
		Injector injector = Guice.createInjector(new ApplicationInjector());        
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

			printer.close();

		} catch (IOException e) {
			LOGGER.fatal(e.getMessage());
			System.exit(1);
		}
		
	}

}
