import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;

public class PsilParser {
	private static final long INVALID = Long.MIN_VALUE;
	private static final int EOF = -1;
	private static final String BIND = "bind";
	private static final String MULTIPLY = "*";
	private static final String ADD = "+";
	private static final String MINUS = "-";
	private static final String DEVIDE = "/";
	private static final String BRAKET_OPEN = "(";
	private static final char BRAKET_OPEN_CHAR = BRAKET_OPEN.charAt(0);
	private static final String BRAKET_CLOSE = ")";
	private static final char BRAKET_CLOSE_CHAR = BRAKET_CLOSE.charAt(0);
	private static final String SPACE = " ";
	private static final char SPACE_ = ' ';
	private static final char CR = '\r'; // CR (Carriage Return)
	private static final char LF = '\n'; // LF (Line Feed)

	private Reader reader;
	// Map stores the all the binds of variables to the values.
	private HashMap<String, Double> map;

	public Myparser() {
		map = new HashMap<String, Double>();
	}

	public  String parseFile(String input) {
		double ans = 0;
		try {
			reader = initReader(input);
			// loop though every expressions and print last evaluated answer.
			while (true) {
				String nextexpression = getNextExpression();
				if (nextexpression.isEmpty()
						|| (ans = evaluateinput(nextexpression)) == INVALID)
					break;
			}
			closeReader(reader);
			return (ans == INVALID) ? ("Invalid Expression") : (ans + "");

		} catch (FileNotFoundException e) {
			// e.printStackTrace();
			return "File not Found";
		} catch (IOException e) {
			// e.printStackTrace();
			System.out.println("Reader could not be closed...");
			return "" + ans;
		}
	}

	private static Reader initReader(String input) throws FileNotFoundException {
		FileInputStream fileInputStream = new FileInputStream(input);
		DataInputStream dataIn = new DataInputStream(fileInputStream);
		return new InputStreamReader(dataIn);
	}

	private static void closeReader(Reader reader) throws IOException {
		if (reader != null)
			reader.close();
	}

	private String getNextExpression() {
		// gets next Expression from the open file reader.
		if (reader == null)
			return "";

		char ch;
		StringBuilder result = new StringBuilder();
		try {
			ch = (char) reader.read();

			if (ch != (char) (EOF)) { // check for EOF
				while ((ch == SPACE_ || ch == LF || ch == CR) && ch != EOF)
					ch = (char) reader.read();

				if (isdigit(ch)) { // first digit is char. then extract number
					while (ch != EOF && isdigit(ch)) {
						result.append(ch);
						ch = (char) reader.read();
					}
				} else if (ch == BRAKET_OPEN_CHAR) {
					// if it is expression then expression may be in multiple
					// line.

					int openbraces = 1;
					result.append(ch);
					ch = (char) reader.read();

					while (ch != (char) EOF) {
						if (ch == BRAKET_OPEN_CHAR)
							openbraces++;
						else if (ch == BRAKET_CLOSE_CHAR)
							openbraces--;
						if (ch == LF || ch == CR)
							ch = SPACE_; // replace \n and \r with space.
						result.append(ch);
						if (openbraces <= 0)
							break;
						ch = (char) reader.read();
					}
				}
			}
			return result.toString(); // return the extracted expression.
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	private double evaluateinput(String nextLine) {
		if (nextLine.isEmpty())
			return INVALID;
		nextLine = makeEvaluable(nextLine);
		return evaluate(nextLine);
	}

	private static String makeEvaluable(String nextLine) {
		nextLine = nextLine.replace(BRAKET_OPEN, BRAKET_OPEN + SPACE_);
		nextLine = nextLine.replace(BRAKET_CLOSE, SPACE_ + BRAKET_CLOSE);
		nextLine = trimExtraSpace(nextLine);
		return nextLine;
	}

	private static String trimExtraSpace(String line) {
		StringBuilder s = new StringBuilder();
		boolean ignore = true;
		for (char ss : line.toCharArray()) {
			if (ss != SPACE_) {
				ignore = false;
				s.append(ss);
			} else if (ss == SPACE_ && !ignore) {
				s.append(SPACE_);
				ignore = true;
			}
		}

		return s.toString();
	}

	private static boolean isdigit(char charAt) {
		return ((int) charAt <= (int) '9') && ((int) charAt >= (int) '0')
				|| charAt == '-' || charAt == '.';
	}

	private double evaluate(String line) {
		ArrayList<String> tokens = new ArrayList<String>();

		String[] temp = line.split(SPACE);
		for (String s : temp)
			tokens.add(s);

		if (!resolveBind(tokens))
			return INVALID; // if expression has bind resolve it.

		// CASE: a number or variable enclosed in brackets E.g (1234) or
		// (variable).
		if (tokens.size() == 3) {
			if ((tokens.get(0).equals(BRAKET_OPEN) && tokens.get(2).equals(
					BRAKET_CLOSE))) { // remove braces
				tokens.remove(0);
				tokens.remove(1);
			} else {
				return INVALID;
			}
		}
		// CASE: a number or variable eg 1234 or variable
		if (tokens.size() == 1) {
			final String token = tokens.get(0);
			if (token.isEmpty()) {
				return INVALID;
			} else if (isdigit(token.charAt(0))) {
				return parseDouble(token);
			} else if (map.get(token) != null) {
				return map.get(token);
			} else
				return INVALID;
		}
		// CASE 3 : nested expression.
		else if (tokens.get(0).equals(BRAKET_OPEN)) {
			String symbol = tokens.get(1);
			if (!validateSymbol(symbol))
				return INVALID;
			ArrayList<Double> oprands = new ArrayList<Double>();
			while (tokens.size() > 2 && !tokens.get(2).equals(BRAKET_CLOSE)) {
				String expression = extractExpression(tokens, 2);
				double value = evaluate(expression);
				if (value == INVALID)
					return INVALID;
				oprands.add(value);
			}
			return calculate(symbol, oprands);

		} else {
			return INVALID;
		}
	}

	private static double parseDouble(String number) {
		// try to parse string in double if can not return invalid number.
		try {
			return Double.parseDouble(number);
		} catch (NumberFormatException e) {
			return INVALID;
		}
	}

	private static double calculate(String operation, ArrayList<Double> oprands) {
		if (oprands.size() == 0)
			return INVALID;
		if (operation.equals(ADD)) {
			double ans = 0; // Identity in addition .
			for (double a : oprands)
				ans += a;
			return ans;

		} else if (operation.equals(MULTIPLY)) {
			double ans = 1; // multiplicative identity
			for (double a : oprands)
				ans *= a;
			return ans;
		} else if (operation.equals(DEVIDE)) { // only 2 operands are
												// supported...... other wise
												// invalid expression.
			if (oprands.size() > 2)
				return INVALID;
			return oprands.get(0) / oprands.get(1);
		} else if (operation.equals(MINUS)) {
			if (oprands.size() > 2)
				return INVALID;
			return oprands.get(0) - oprands.get(1);
		} else
			return INVALID;
	}

	private static boolean validateSymbol(String operation) {
		if (operation.equals(ADD) || operation.equals(MULTIPLY)
				|| operation.equals(MINUS) || operation.equals(DEVIDE))
			return true;
		return false;
	}

	private boolean resolveBind(ArrayList<String> tokens) {
		if (contains(tokens, BIND)) {
			int bindIndex = indexOf(tokens, BIND);
			if (bindIndex == -1)
				return true;
			if (bindIndex == 0 || bindIndex + 2 > tokens.size()
					|| !tokens.get(bindIndex - 1).equals(BRAKET_OPEN))
				return false;

			String variable = tokens.get(bindIndex + 1);
			if (!validate(variable))
				return false;

			String expression = extractExpression(tokens, bindIndex + 2);
			double value = evaluate(expression);
			if (value == INVALID)
				return false;
			else {
				map.put(variable, value); // store variable value in map.
			}
			tokens.remove(bindIndex); // remove bind keyword.
		}
		return true;
	}

	private static int indexOf(ArrayList<String> list, String key) {
		if (list == null || key == null || key.isEmpty())
			return -1;

		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).equals(key))
				return i;
		}

		return -1;
	}

	private static boolean contains(ArrayList<String> list, String key) {
		if (list == null || key == null || key.isEmpty())
			return false;
		for (String s : list) {
			if (s.equals(key))
				return true;
		}
		return false;
	}

	private static boolean validate(String variable) {
		if (variable == null || variable.isEmpty())
			return false;
		for (char c : variable.toCharArray()) {
			if ((int) c > (int) 'z' || (int) c < (int) 'a')
				return false;
		}
		return true;
	}

	private static String extractExpression(ArrayList<String> tokens,
			int beginIndex) {
		// CASE 1 : is a number.
		if (!tokens.get(beginIndex).equals(BRAKET_OPEN)) {
			String result = tokens.get(beginIndex);
			tokens.remove(beginIndex);
			return result;
		}
		// CASE 2 : is an expression.
		else {
			int openBraces = 1;
			int endIndexOfExpression = beginIndex + 1;
			while (endIndexOfExpression++ < tokens.size()) {
				if (tokens.get(endIndexOfExpression).equals(BRAKET_OPEN))
					openBraces++;
				else if (tokens.get(endIndexOfExpression).equals(BRAKET_CLOSE)) {
					openBraces--;
					if (openBraces == 0) {
						break;
					}
				}
			}
			String result = makeString(tokens, beginIndex, endIndexOfExpression);
			// remove the extracted expression from tokens since it will be
			// replaced by variable
			for (int a = beginIndex; a <= endIndexOfExpression; a++)
				tokens.remove(beginIndex);

			return result;
		}
	}

	private static String makeString(ArrayList<String> tokens, int beginIndex,
			int endIndex) {
		StringBuilder s = new StringBuilder(tokens.get(beginIndex++));
		while (beginIndex < endIndex + 1) {
			s.append(" " + tokens.get(beginIndex++));
		}
		return s.toString();
	}
}
