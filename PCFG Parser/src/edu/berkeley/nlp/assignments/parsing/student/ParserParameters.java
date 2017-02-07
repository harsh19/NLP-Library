package edu.berkeley.nlp.assignments.parsing.student;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ParserParameters {

	public static int HORIZONTAL_MARKOVIZATION_INF = 9999;
	public static boolean DO_HORIZONTAL_MARKOVIZATION = true;
	public static int HORIZONTAL_MARKOVIZATION_DEGREE = 2;
	
	public static boolean DO_VERTICAL_MARKOVIZATION = true;
	public static boolean DO_VARIABLE_VERTICAL_MARKOVIZATION = true;
	public static int VERTICAL_MARKOVIZATION_DEGREE = 2;
	static int VARIABLE_VERTICAL_MARKOVIZATION_COUNT_THRESH = 20;
	
	public static boolean DO_VERTICAL_MARKOVIZATION_OF_PRETERMINALS = true;
	
	
	public static boolean DO_INTERNAL_UNARY_MARK = true;
	public static boolean DO_UNARY_DT = true;
	public static boolean DO_UNARY_RB = false;
	
	public static boolean DO_SPLIT_IN = true;
	public static boolean DO_SPLIT_AUX = true;
	public static boolean DO_SPLIT_CC = true;


	public static String paramsFileName = "params.txt";
	
	public static void readParams(){
		try(BufferedReader br = new BufferedReader(new FileReader(paramsFileName))) {
		    String line = br.readLine();
		    while (line != null) {
		    	line = line.replace('\n','\u0000');
		    	String vals[] = line.split("\\s+");
		    	String sym = vals[0];
		    	if(sym.equalsIgnoreCase("v")){
		    		VERTICAL_MARKOVIZATION_DEGREE = Integer.parseInt(vals[1]);
		    		System.out.println("Setting VERTICAL_MARKOVIZATION_DEGREE = "+VERTICAL_MARKOVIZATION_DEGREE);
		    	}
		    	else if(sym.equalsIgnoreCase("h")){
		    		HORIZONTAL_MARKOVIZATION_DEGREE = Integer.parseInt(vals[1]);
		    		System.out.println("Setting HORIZONTAL_MARKOVIZATION_DEGREE = "+HORIZONTAL_MARKOVIZATION_DEGREE);
		    	}
		    	else if(sym.equalsIgnoreCase("dov")){
		    		DO_VERTICAL_MARKOVIZATION = (Integer.parseInt(vals[1])==1);
		    		System.out.println("Setting DO_VERTICAL_MARKOVIZATION = "+DO_VERTICAL_MARKOVIZATION);
		    	}
		    	else if(sym.equalsIgnoreCase("doh")){
		    		DO_HORIZONTAL_MARKOVIZATION = (Integer.parseInt(vals[1])==1);
		    		System.out.println("Setting DO_HORIZONTAL_MARKOVIZATION = "+DO_HORIZONTAL_MARKOVIZATION);
		    	}
		    	else if(sym.equalsIgnoreCase("dovar")){
		    		DO_VARIABLE_VERTICAL_MARKOVIZATION = (Integer.parseInt(vals[1])==1);
		    		System.out.println("Setting DO_VARIABLE_VERTICAL_MARKOVIZATION = "+DO_VARIABLE_VERTICAL_MARKOVIZATION);
		    	}
		    	else if(sym.equalsIgnoreCase("dou")){
		    		DO_INTERNAL_UNARY_MARK = (Integer.parseInt(vals[1])==1);
		    		System.out.println("Setting DO_INTERNAL_UNARY_MARK = "+DO_INTERNAL_UNARY_MARK);
		    	}
		    	else if(sym.equalsIgnoreCase("dodt")){
		    		DO_UNARY_DT = (Integer.parseInt(vals[1])==1);
		    		System.out.println("Setting DO_UNARY_DT = "+DO_UNARY_DT);
		    	}
		    	else if(sym.equalsIgnoreCase("dorb")){
		    		DO_UNARY_RB = (Integer.parseInt(vals[1])==1);
		    		System.out.println("Setting DO_UNARY_RB = "+DO_UNARY_RB);
		    	}
		    	else if(sym.equalsIgnoreCase("dopre")){
		    		DO_VERTICAL_MARKOVIZATION_OF_PRETERMINALS = (Integer.parseInt(vals[1])==1);
		    		System.out.println("Setting DO_VERTICAL_MARKOVIZATION_OF_PRETERMINALS = "+DO_VERTICAL_MARKOVIZATION_OF_PRETERMINALS);
		    	}
		    	else if(sym.equalsIgnoreCase("dopre")){
		    		DO_VERTICAL_MARKOVIZATION_OF_PRETERMINALS = (Integer.parseInt(vals[1])==1);
		    		System.out.println("Setting DO_VERTICAL_MARKOVIZATION_OF_PRETERMINALS = "+DO_VERTICAL_MARKOVIZATION_OF_PRETERMINALS);
		    	}
		    	else if(sym.equalsIgnoreCase("doin")){
		    		DO_SPLIT_IN = (Integer.parseInt(vals[1])==1);
		    		System.out.println("Setting DO_SPLIT_IN = "+DO_SPLIT_IN);
		    	}
		    	else if(sym.equalsIgnoreCase("doaux")){
		    		DO_SPLIT_AUX = (Integer.parseInt(vals[1])==1);
		    		System.out.println("Setting DO_SPLIT_AUX = "+DO_SPLIT_AUX);
		    	}
		    	else if(sym.equalsIgnoreCase("docc")){
		    		DO_SPLIT_CC = (Integer.parseInt(vals[1])==1);
		    		System.out.println("Setting DO_SPLIT_CC = "+DO_SPLIT_CC);
		    	}
		        line = br.readLine();
		    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
