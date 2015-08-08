import java.util.Scanner;


public class Test {
	public static void main(String... args) {
		Scanner s = new Scanner (System.in);
		String input = "D:\\test.txt";
		System.out.println("ENTER ABSOLUTE PATH OF FILE THAT YOU WANT TO PARSE");
		input = s.nextLine();
		s.close();
		System.out.println(new PsilParser().parseFile(input));
	}
}
