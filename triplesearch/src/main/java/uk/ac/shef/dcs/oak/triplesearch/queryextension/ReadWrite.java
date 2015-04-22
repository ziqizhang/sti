package uk.ac.shef.dcs.oak.triplesearch.queryextension;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class ReadWrite {
	
	public static void printListOnFile(ArrayList<String> strset, String filename) {
		PrintWriter out;
		try {
			out = new PrintWriter(new FileWriter(filename));
			for (String a : strset) {
				out.print(a + "\n");
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	public HashMap<String, Float> readTrainingFile(String filename) throws IOException {
		HashMap<String, Float> trainmap = new HashMap<String, Float>();
		FileInputStream fstream = new FileInputStream(filename);
		  // Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		  //Read File Line By Line
		while ((strLine = br.readLine()) != null)   {
		  // Print the content on the console
			String[] trainsplit = strLine.split("\t");
			trainmap.put(trainsplit[1], new Float (trainsplit[0]));
		}
		  //Close the input stream
		in.close();
		return trainmap;
	}
	
	
	public Set<String> readSetFromFile(String filename) throws IOException {
		HashSet<String> newSet = new HashSet<String>();
		FileInputStream fstream = new FileInputStream(filename);
		  // Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		  //Read File Line By Line
		while ((strLine = br.readLine()) != null)   {
		  // Print the content on the console
			newSet.add(strLine);
		}
		  //Close the input stream
		in.close();
		return newSet;		
	}

	public ArrayList<String> readPropListFromFile(String filename) throws IOException {
		ArrayList<String> newList = new ArrayList<String>();
		FileInputStream fstream = new FileInputStream(filename);
		  // Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		  //Read File Line By Line
		while ((strLine = br.readLine()) != null)   {
		  try {
			// Print the content on the console
				String[] newArray = strLine.split("Predicate = \\[");
				String preds = newArray[1].replace("[", "").replace("]", "");
				for (String s : preds.split(", ")) {
					newList.add(s);
				}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  continue;
		}
		  //Close the input stream
		in.close();
		return newList;		
	}
	
	
	public HashMap<String, Integer> readFromFilesIDFDenom(String path) throws IOException {
		HashMap<String, Integer> matchMap = new HashMap<String, Integer>();
		File folder = new File(path);
	    for (final File fileEntry : folder.listFiles()) {
	        try {
				if (fileEntry.isDirectory()) {

				} else {
				    System.out.println(fileEntry.getName());
				    FileInputStream fstream = new FileInputStream(path + "/" + fileEntry.getName());
				    // Get the object of DataInputStream
				    DataInputStream in = new DataInputStream(fstream);
				    BufferedReader br = new BufferedReader(new InputStreamReader(in));
				    String strLine;
				    //Read File Line By Line
				    while ((strLine = br.readLine()) != null)   {
				    	try {
				    		// Print the content on the console
				    		String[] newArray = strLine.split("Predicate = \\[");
				    		String preds = newArray[1].replace("[", "").replace("]", "");
				    		String uri = newArray[0].replace("DBpediaURI = ", "").replace(", ", "");
				    		String[] predsplit = preds.split(", ");
				    		if (matchMap.containsKey(uri)) {
				    			Integer cnt = matchMap.get(uri);
				    			matchMap.put(uri, cnt+1);
				    		}
				    		else {
					    		matchMap.put(uri, 1);
				    		}

				    	} catch (Exception e) {
				    		// TODO Auto-generated catch block
				    		e.printStackTrace();
				    	}
				    	continue;
				    }
				    in.close();
				}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        continue;
		    }
		  //Close the input stream
		return matchMap;		
	}
	
	
	public HashMap<String, Integer> readFromFilesTFDenom(String path) throws IOException {
		HashMap<String, Integer> matchMap = new HashMap<String, Integer>();
		File folder = new File(path);
	    for (final File fileEntry : folder.listFiles()) {
	        try {
				if (fileEntry.isDirectory()) {

				} else {
				    System.out.println(fileEntry.getName());
				    FileInputStream fstream = new FileInputStream(path + "/" + fileEntry.getName());
				    // Get the object of DataInputStream
				    DataInputStream in = new DataInputStream(fstream);
				    BufferedReader br = new BufferedReader(new InputStreamReader(in));
				    String strLine;
				    //Read File Line By Line
				    while ((strLine = br.readLine()) != null)   {
				    	try {
				    		// Print the content on the console
				    		String[] newArray = strLine.split("Predicate = \\[");
				    		String preds = newArray[1].replace("[", "").replace("]", "");
				    		String uri = newArray[0].replace("DBpediaURI = ", "").replace(", ", "");
				    		String[] predsplit = preds.split(", ");
				    		if (matchMap.containsKey(uri)) {
				    			Integer cnt = matchMap.get(uri);
				    			matchMap.put(uri, cnt+predsplit.length);
				    		}
				    		else {
					    		matchMap.put(uri, predsplit.length);
				    		}

				    	} catch (Exception e) {
				    		// TODO Auto-generated catch block
				    		e.printStackTrace();
				    	}
				    	continue;
				    }
				    in.close();
				}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        continue;
		    }
		  //Close the input stream
		return matchMap;		
	}
	
	public HashMap<String, HashMap<String,Integer>> readEvalData(String path) {
		HashMap<String, HashMap<String,Integer>> matchMap = new HashMap<String, HashMap<String,Integer>>();
		File folder = new File(path);
	    for (final File fileEntry : folder.listFiles()) {
	        try {
				if (fileEntry.isDirectory()) {

				} else {
				    System.out.println(fileEntry.getName());
					ArrayList<String> exactList = readPropListFromFile(fileEntry.toString());
					HashMap<String, Integer> exactMatchCount = new HashMap<String, Integer>();
					for (String entry : exactList) {
						if (exactMatchCount.containsKey(entry)) {
							Integer cnt = exactMatchCount.get(entry);
							cnt++;
							exactMatchCount.put(entry, cnt);
						}
						else {
							exactMatchCount.put(entry, 1);
						}
					}
					matchMap.put(fileEntry.getName(), exactMatchCount);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        continue;
	    }
	    return matchMap;
	}
	
	
	public void printHashMapOnFile(HashMap<String, Set<String>> uris, String filename) {
		PrintWriter out;
		try {
			out = new PrintWriter(new FileWriter(filename));
			Iterator<Entry<String, Set<String>>> entries = uris.entrySet().iterator();
			while(entries.hasNext()) {
				Entry<String, Set<String>> entry = entries.next();
				String key = entry.getKey();
				Set<String> value = entry.getValue();
				out.print("DBpediaURI = " + key + ", Predicate = " + value + "\n");			
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
	public void printRankedResultsOnFile(Map<Double, String> rankmapSingle, String filename) {
		PrintWriter out;
		try {
			out = new PrintWriter(new FileWriter(filename));
			Iterator<Entry<Double, String>> entries = rankmapSingle.entrySet().iterator();
			while(entries.hasNext()) {
				Entry<Double, String> entry = entries.next();
				Double key = entry.getKey();
				String value = entry.getValue();
				out.print(key + "\t" + value + "\n");			
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void printCntOnFile(HashMap<String, Integer> all_cnt, String filename) {
		PrintWriter out;
		try {
			out = new PrintWriter(new FileWriter(filename));
			Iterator<Entry<String, Integer>> entries = all_cnt.entrySet().iterator();
			while(entries.hasNext()) {
				Entry<String, Integer> entry = entries.next();
				String key = entry.getKey();
				Integer value = entry.getValue();
				out.println(value + "\t" + key);
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
}
