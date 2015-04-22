package uk.ac.shef.oak.xpathExperiment;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.shef.oak.xpath.collectiveExperiment.SetOperations;

public class Gazeteer implements Serializable{
	private static Logger l4j = Logger.getLogger("Gazeteer");

	
	private  HashSet<String> words;

	public HashSet<String> getWords() {
		return words; 
	}

//	public StopWord() {
//		super();
//		this.words = new HashSet<String> () ;
//	}
	public Gazeteer(String wordFilePath) {
		super();
		this.words = new HashSet<String> () ;
		try {
			this.loadFile(wordFilePath);
		} catch (IOException e) {
			l4j.error("error loading stopWord file");

			e.printStackTrace();
		}
	}
	
	private void loadFile(String wordFilePath) throws IOException{
		BufferedReader input=new BufferedReader(new FileReader(wordFilePath));
		String line;
		line=input.readLine();
	
		while (line!=null)
		{
			line=line.trim();
			if (!line.equals("")){
			this.words.add(line.toLowerCase());}
			
			line=input.readLine();
			
		}
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Gazeteer fileExt = new Gazeteer("./gazeteers/book_titles.txt");
		Set<String> all = new HashSet<String>();
		Set<String> correct = new HashSet<String>();

		
		
		for (String s :fileExt.getWords()){
			all.add(s.toLowerCase());
		}
		
		
		LoadGS fileGs = new LoadGS("/Users/annalisa/Documents/CORPORAandDATASETS/swde-17477/groundtruth/book/book-amazon-title.txt");
		
		for (Set<String> s :fileGs.getValues().values()){
			for (String t :s){
			correct.add(t.toLowerCase());
		}}
		
		
		System.out.println("**** pages to use *****");

		String inPath ="/Users/annalisa/Documents/CORPORAandDATASETS/swde-17477/book/book-amazon(2000)/";
		String outPath ="/Users/annalisa/Documents/CORPORAandDATASETS/testBook/book-amazon(correct)/";

		for (Entry<String,Set<String>> s :fileGs.getValues().entrySet()){
			String firstRes="";
			if (!s.getValue().isEmpty())
				firstRes= s.getValue().iterator().next();
			if (all.contains(firstRes)){
				String in = inPath+s.getKey()+".htm";
				String out = outPath+s.getKey()+".htm";

				System.out.println("copying "+in+" to "+out);

				Gazeteer.copyFile(in, out);
				
			}
		}
		System.out.println("**** end pages to use *****");

//		System.out.println("LOD gathered annotations example 100");
//		int stop=0;
//		for (String s : all){
//			stop++;
//			System.out.println(s);
//			if (stop>100)
//				break;
//		}
// 
//		
//		System.out.println("GS annotations example 100");
//		int stopgs=0;
//		for (String s : correct){
//			stopgs++;
//			System.out.println(s);
//			if (stopgs>100)
//				break;
//		}
		
		
		System.out.println("LOD gathered annotations size " + all.size());
		System.out.println("GS annotations size " + correct.size());


		
		
		Set<String> inters = SetOperations.intersection(correct, all) ;
		

		System.out.println("intersection size " + inters.size());
		System.out.println(inters);

		
		
		/*
		
		HashTagEntropy ss = new HashTagEntropy(args[0]);

		
	File d = new File("/Users/annalisa/SOLR/simon-solr-3.4/data/index");
	if (d.isDirectory()) {
		Directory directory;
		try {
			directory = FSDirectory.open(d);
			List<String> tt = ss.computeTopTermOnGlobalIndex(directory);


			 System.out.println(tt);
			 
		     BufferedWriter outH = new BufferedWriter(new FileWriter("./out/stopword.txt"));

	        	  for (String t:tt) {
					outH.write(t);
					outH.newLine();
				}
			outH.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}*/

		
	}

	
	public static void copyFile(String in, String out){
		
		 
    	InputStream inStream = null;
	OutputStream outStream = null;
 
    	try{
 
    	    File afile =new File(in);
    	    File bfile =new File(out);
 
    	    inStream = new FileInputStream(afile);
    	    outStream = new FileOutputStream(bfile);
 
    	    byte[] buffer = new byte[1024];
 
    	    int length;
    	    //copy the file content in bytes 
    	    while ((length = inStream.read(buffer)) > 0){
 
    	    	outStream.write(buffer, 0, length);
 
    	    }
 
    	    inStream.close();
    	    outStream.close();
 
 
    	}catch(IOException e){
    		e.printStackTrace();
    	}
	}
	}
