import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


public class Myparser {
	private static final String BIND = "bind";
	private static final String MULTIPLY = "*";
	private static final String ADD = "+";
	private static final String START = "(";
	private static final String END = ")";
	
	private static HashMap<String,Long> map = new HashMap<String,Long>();
	public static void main(String... args){
		File input = new File("D:\\test.txt");
		System.out.println(parse(input));
	}

	private static String parse(File input) {
		try {
			Scanner sc = new Scanner(input);
			long ans =0;
			while(sc.hasNextLine()){
				if((ans = evaluateinput(sc.nextLine()))==-1) break;
			}
			if(ans ==-1) return "Invalid Expression";
			else return ""+ans;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "file not found";
		}
	}

	private static long evaluateinput(String nextLine) {
		int i=0;
		if(nextLine.charAt(0) < (int)'a') return evaluate(nextLine);
		/*while(i<nextLine.length()){
			if(nextLine.substring(i, i+1).equals(START)){
				int start = i;
				while(+)
			}
		}*/
		String[] sexexpressions  = getsexpressions(nextLine);
		
		long ans = -1;
		if(sexexpressions==null) return ans;
		
		for(String exp:sexexpressions){
			ans = evaluate(exp);
			if(ans==-1)return -1;
		}
		return -1;
	}

	private static String[] getsexpressions(String nextLine) {
		int start = 0;
		ArrayList<String> anslist = new ArrayList<String>();
		while(true){
		int LastIndexOfExpression = start+1;
		int openBraces =1;
		while (LastIndexOfExpression < nextLine.length()) {// removed -i 
			if (nextLine.substring(LastIndexOfExpression,LastIndexOfExpression+1).equals(START))
				openBraces++;
			else if (nextLine.substring(LastIndexOfExpression,LastIndexOfExpression+1).equals(END)) {
				openBraces--;
				if (openBraces == 0) {
					break;
				} else {
					LastIndexOfExpression++;
				}
			}
		}
		if(LastIndexOfExpression>start+1){
			anslist.add(nextLine.substring(start, LastIndexOfExpression));
			start = LastIndexOfExpression+1;
		}else return (String[])anslist.toArray();
		}
	}

	private static long evaluate(String nextLine) {
		// returns -1 if invalid expression otherwise the ans;
		//(bind length 10) (+ 1 2 3 4) (bind breadth 10) (* length breadth)

		ArrayList<String> tokens = new ArrayList<String>();
		String[] temp = nextLine.split(" ");
		for(String s :temp)
			tokens.add(s);
		if(!resolvebinds(tokens)) return -1; // all the variables binds have been resolved.
		
		// simple 1234 or name of variable  like things...  
		if(tokens.size()==1){
			if ((int)tokens.get(0).charAt(0) < (int)'a'){
				return Long.parseLong(tokens.get(0));
			}else {
				 return map.get(tokens.get(0));
			}
		}else if (tokens.size()==3){
			if(tokens.get(0).equals(START) && tokens.get(2).equals(END)){
				if ((int)tokens.get(1).charAt(0) < (int)'a'){
					return Long.parseLong(tokens.get(1));
				}else {
					 return map.get(tokens.get(1));
				}
			}
		}else if(tokens.get(0).equals(START)) {
			String operation = tokens.get(1);
			if(!isValidOperation(operation)) return -1;
			//(  ()()() )
			ArrayList<Long> oprands = new ArrayList<Long>();
			while(!tokens.get(2).equals(END)){
				String expression = getExpression(tokens,2);
				long value = evaluate(expression);
				if(value==-1) return -1;
				oprands.add(value);
			}
			return calculate(operation, oprands);
			
		}else{
			return -1;
		}
		return -1;
		
	}

	private static long calculate(String operation, ArrayList<Long> oprands) {
		if(oprands.size()==0) return -1;
		if(operation.equals(ADD)){
			long ans =0;
			for(Long a:oprands)ans+=a;
			return ans;
			
		}else if(operation.equals(MULTIPLY)){
			long ans =1;
			for(Long a:oprands)ans*=a;
			return ans;
		}
		else return -1;
	}

	private static boolean isValidOperation(String operation) {
		return true;
	}

	private static boolean resolvebinds(ArrayList<String> tokens) {
		while(contains(tokens,BIND)){
			int i = indexof(tokens,BIND);
			if(i==-1 ) return true;
			if(!tokens.get(i-1).equals(START)) return false;
			
			String variable  = tokens.get(i+1);
			if(!validae(variable)) return false;
			String expression = getExpression(tokens,i+2);
			long value = evaluate(expression);
			if(value==-1) return false;
			else {
				map.put(variable, value);
			}
			tokens.remove(i); // remove bind keyword.
		};
		return true;
	}

	private static boolean validae(String variable) {
		// validity of a variable.
		return true;
	}

	private static String getExpression(ArrayList<String> tokens, int i) {
		//( ) or direct
		if(!tokens.get(i).equals(START)){ 
			String result = tokens.get(i);
			tokens.remove(i);
			return result;
		}else {
			int openBraces = 1;
			int LastIndexOfExpression = i + 1;
			while (LastIndexOfExpression < tokens.size()) {// removed -i 
				if (tokens.get(LastIndexOfExpression).equals(START))
					openBraces++;
				else if (tokens.get(LastIndexOfExpression).equals(END)) {
					openBraces--;
					if (openBraces == 0) {
						break;
					} else {
						LastIndexOfExpression++;
					}
				}
			}
			String result = makestring(tokens, i, LastIndexOfExpression);
			for (int a = i; a <= LastIndexOfExpression; a++)
				tokens.remove(a);
			return result;
		}
	}

	private static String makestring(ArrayList<String> tokens, int i,
			int lastIndexOfExpression) {
		StringBuilder s = new StringBuilder(tokens.get(i++));
		while(i<lastIndexOfExpression+1){
			s.append(" "+tokens.get(i++));
		}
		return s.toString();
	}
	private static boolean contains(ArrayList<String> tokens,String key){
		for(String k:tokens){
			if(k.equals(key))return true;
		}
		return false;
	}
	private static int indexof(ArrayList<String> tokens,String key){
		for(int k=0;k<tokens.size();k++){
			if(tokens.get(k).equals(key))return k;
		}
		return -1;
	}
}
