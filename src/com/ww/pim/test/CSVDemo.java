package com.ww.pim.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

public class CSVDemo {

	
	public static void main(String[] args) {
		String filePath="C:\\Users\\kumarsp\\Documents\\Watts Water\\Production Support\\Server Credential Change\\Final Plan\\resultSumana.csv";
		File file = new File(filePath); 
		  
	    try { 
	        // create FileWriter object with file as parameter 
	        FileWriter outputfile = new FileWriter(file); 
	  
	        // create CSVWriter object filewriter object as parameter 
	        CSVWriter writer = new CSVWriter(outputfile); 
	  
	        // create a List which contains String array 
	        List<String[]> data = new ArrayList<String[]>(); 
	        data.add(new String[] { "Name", "Class", "Marks" }); 
	        data.add(new String[] { "Sumana", "10", "620" }); 
	        data.add(new String[] { "Pratip", "10", "630" }); 
	        writer.writeAll(data); 
	  
	        // closing writer connection 
	        writer.close(); 
	    } 
	    catch (IOException e) { 
	        // TODO Auto-generated catch block 
	        e.printStackTrace(); 
	    } 

	}

}
