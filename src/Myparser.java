import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;


public class Myparser {
	private static final long INVALID = Long.MIN_VALUE;
	private static final int EOF = -1;
	private static final String BIND = "bind";
	private static final String MULTIPLY = "*";
	private static final String ADD = "+";
	private static final String MINUS = "-";
	private static final String DEVIDE = "/";
	private static final String START = "(";
	private static final String END = ")";
	private static Reader reader;
	
	private static HashMap<String,Double> map = new HashMap<String,Double>();
	public static void main(String... args){
		String  input = "D:\\test.txt";
		System.out.println(parse(input));
		if(reader!=null)
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	private static String parse(String input) {
		try {
			FileInputStream fstream = new FileInputStream(input);
			DataInputStream in = new DataInputStream(fstream);
			reader = new InputStreamReader(in);
			double ans =0;
			while(true){
				String nextexpression = getNextExpression(); 
				if(nextexpression.isEmpty())break;
				else if((ans = evaluateinput(nextexpression))==INVALID) break;
			}
			if(ans ==INVALID) return "Invalid Expression";
			else return ""+ans;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return "file not found";
		}
	}

	private static String getNextExpression() {
		//Reader reader = new InputStreamReader(System.in);
		char ch;
		StringBuilder result = new StringBuilder();
		try {
			ch = (char) reader.read();
			
			if (ch != (char)(EOF)) {  // check for EOF
			    // we have a character ...
				while((ch== ' ' ||ch=='\n') && ch!=EOF)ch = (char)reader.read();
				if(isdigit(ch)){ // first digit is char.
					while(ch!=EOF && isdigit(ch)){
						result.append(ch);
						ch = (char)reader.read();
					}
					if(ch!=EOF)reader.read();
					return result.toString();
				}else if(ch=='('){
					int openbraces =1;
					result.append(ch);
					ch = (char)reader.read();
					while(ch!=(char)EOF && openbraces>0){
						if(ch =='(') openbraces++;
						else if (ch ==')') openbraces--;
						if(ch =='\n'||ch=='\r')ch= ' ';
						result.append(ch);
						ch = (char)reader.read();
					}
					return result.toString();
				}else return result.toString();
				
			}else{
				return result.toString();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
		
	}

	private static double evaluateinput(String nextLine) {
		if(nextLine.isEmpty()) return INVALID;
		nextLine = nextLine.replace("(", "( ");
		nextLine = nextLine.replace(")", " )");
		nextLine =trimspace(nextLine);
		int i=0;
		if(isdigit(nextLine.charAt(0))) return evaluate(nextLine);
		/*while(i<nextLine.length()){
			if(nextLine.substring(i, i+1).equals(START)){
				int start = i;
				while(+)
			}
		}*/
		String[] sexexpressions  = getsexpressions(nextLine);
		
		double ans = INVALID;
		if(sexexpressions==null) return ans;
		
		for(String exp:sexexpressions){
			ans = evaluate(exp);
			if(ans==INVALID)return INVALID;
		}
		return ans;
	}

	private static String trimspace(String nextLine) {
		StringBuilder s = new StringBuilder();
		boolean ignore = true;
		for(char ss:nextLine.toCharArray()){
			if(ss!=' '){ignore = false; s.append(ss); }
			else if(ss==' ' && !ignore){s.append(' '); ignore = true;}
		}
			
		return s.toString();
	}

	private static boolean isdigit(char charAt) {
		return ((int)charAt <= (int)'9') && ((int)charAt >= (int)'0')||charAt=='-';
	}

	private static String[] getsexpressions(String nextLine) {
		int start = 0;
		ArrayList<String> anslist = new ArrayList<String>();
		while(true){
		int LastIndexOfExpression = start+1;
		int openBraces =1;
		while (LastIndexOfExpression < nextLine.length()) {// removed -i 
			if (nextLine.substring(LastIndexOfExpression,LastIndexOfExpression+1).equals(START))
				{openBraces++;
			LastIndexOfExpression++;}
			else if (nextLine.substring(LastIndexOfExpression,LastIndexOfExpression+1).equals(END)) {
				openBraces--;
				
				if (openBraces == 0) {
					break;
				}
				LastIndexOfExpression++;
			}else{
				LastIndexOfExpression++;
			}
		}
		if(LastIndexOfExpression>start+1 && LastIndexOfExpression<nextLine.length()){
			anslist.add(nextLine.substring(start, LastIndexOfExpression+1));
			start = LastIndexOfExpression+2;
		}else return makeArray(anslist);
		}
	}

	private static String[] makeArray(ArrayList<String> anslist) {
		if(anslist==null)return null;
		String[] result = new String[anslist.size()];
		int i = 0; for(String s:anslist)result[i++]=s;
		return result;
	}

	private static double evaluate(String nextLine) {
		// returns -1 if invalid expression otherwise the ans;
		//(bind length 10) (+ 1 2 3 4) (bind breadth 10) (* length breadth)

		ArrayList<String> tokens = new ArrayList<String>();
		String[] temp = nextLine.split(" ");
		for(String s :temp)
			tokens.add(s);
		if(!resolvebinds(tokens)) return INVALID; // all the variables binds have been resolved.
		
		// simple 1234 or name of variable  like things...  
		if(tokens.size()==1){
			if (isdigit(tokens.get(0).charAt(0))){
				return parseDouble(tokens.get(0));
			}else if(map.get(tokens.get(0))!=null){
				 return map.get(tokens.get(0));
			}else return INVALID;
		}else if (tokens.size()==3){
			if(tokens.get(0).equals(START) && tokens.get(2).equals(END)){
				String token = tokens.get(1);
				if(token.isEmpty()) return INVALID;
				else if (isdigit(token.charAt(0))){
					return parseDouble(tokens.get(1));
				}else if(map.get(token)!=null){
					 return map.get(token);
				}else return INVALID;
			}else return INVALID;
		}else if(tokens.get(0).equals(START)) {
			String operation = tokens.get(1);
			if(!isValidOperation(operation)) return INVALID;
			//(  ()()() )
			ArrayList<Double> oprands = new ArrayList<Double>();
			while(tokens.size()>2 && !tokens.get(2).equals(END)){
				String expression = getExpression(tokens,2);
				double value = evaluate(expression);
				if(value==INVALID) return INVALID;
				oprands.add(value);
			}
			return calculate(operation, oprands);
			
		}else{
			return INVALID;
		}
	}

	private static double parseDouble(String number) {
		try{
			return Double.parseDouble(number);
		}catch(NumberFormatException e){
			return INVALID;
		}
	}

	private static double calculate(String operation, ArrayList<Double> oprands) {
		if (oprands.size() == 0)
			return INVALID;
		if (operation.equals(ADD)) {
			double ans = 0;
			for (double a : oprands)
				ans += a;
			return ans;

		} else if (operation.equals(MULTIPLY)) {
			double ans = 1;
			for (double a : oprands)
				ans *= a;
			return ans;
		} else if (operation.equals(DEVIDE)) { // only 2 operands are
												// supported...... other wise
												// invalid expression.
			if (oprands.size() > 2)
				return INVALID;

			double ans = oprands.get(0) / oprands.get(1);
			// for(Long a:oprands)ans*=a;
			return ans;
		} else if (operation.equals(MINUS)) {
			if (oprands.size() > 2)
				return INVALID;
			double ans = oprands.get(0) - oprands.get(1);
			return ans;
		} else
			return INVALID;
	}

	private static boolean isValidOperation(String operation) {
		if(operation.equals(ADD) ||operation.equals(MULTIPLY) || operation.equals(MINUS)||operation.equals(DEVIDE)) return true;
		return false;
	}

	private static boolean resolvebinds(ArrayList<String> tokens) {
		if (contains(tokens,BIND)){
			int i = indexOf(tokens ,BIND);
			if(i==-1 ) return true;
			if(!tokens.get(i-1).equals(START)) return false;
			
			String variable  = tokens.get(i+1);
			if(!validae(variable)) return false;
			String expression = getExpression(tokens,i+2);
			double value = evaluate(expression);
			if(value==INVALID) return false;
			else {
				map.put(variable, value);
			}
			tokens.remove(i); // remove bind keyword.
		};
		return true;
	}

	private static int indexOf(ArrayList<String> tokens, String key) {
		for(int i =0;i<tokens.size();i++){
			if(tokens.get(i).equals(key)) return i;
		}
		return -1;
	}

	private static boolean contains(ArrayList<String> tokens, String key) {
		for(int i =0;i<tokens.size();i++){
			if(tokens.get(i).equals(key)) return true;
		}
		return false;
	}

	private static boolean validae(String variable) {
		for(char c:variable.toCharArray()){
			if((int)c > (int)'z' ||(int)c <(int)'a') return false;
		}
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
						//LastIndexOfExpression++;
					}
				}
				LastIndexOfExpression++;
			}
			String result = makestring(tokens, i, LastIndexOfExpression);
			for (int a = i; a <= LastIndexOfExpression; a++)
				tokens.remove(i);
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
}
