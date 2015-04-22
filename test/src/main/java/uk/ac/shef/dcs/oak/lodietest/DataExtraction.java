/*
package uk.ac.shef.dcs.oak.lodietest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import org.deri.any23.Any23;
import org.deri.any23.extractor.ExtractionException;
import org.deri.any23.http.HTTPClient;
import org.deri.any23.source.DocumentSource;
import org.deri.any23.source.HTTPDocumentSource;
import org.deri.any23.writer.NTriplesWriter;
import org.deri.any23.writer.TripleHandler;
import org.deri.any23.writer.TripleHandlerException;


public class DataExtraction {

	*/
/**
	 * @param args
	 *//*

	public static void main(String[] args) {
		try {
			*/
/*1*//*
 Any23 runner = new Any23();
			*/
/*2*//*
 runner.setHTTPUserAgent("test-user-agent");
			*/
/*3*//*
 HTTPClient httpClient = runner.getHTTPClient();
			*/
/*4*//*
 DocumentSource source = new HTTPDocumentSource(
			         httpClient,
			         "http://www.rentalinrome.com/semanticloft/semanticloft.htm"
			      );
			*/
/*5*//*
 ByteArrayOutputStream out = new ByteArrayOutputStream();
			*/
/*6*//*
 TripleHandler handler = new NTriplesWriter(out);
			      try {
			*/
/*7*//*
     runner.extract(source, handler);
			      } finally {
			*/
/*8*//*
     handler.close();
			      }
			*/
/*9*//*
 String n3 = out.toString("UTF-8");
			
			
			System.out.println(n3);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExtractionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TripleHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
*/
