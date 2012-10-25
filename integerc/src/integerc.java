import java.io.*;
import java.util.*;
public class integerc {
/* Programmed by Homer Uy Co
 * 1996-27395
 * Quiz #1
 * IS214
 * References:
 * Lecture slides: Prof Reinald Pugoy
 * http://www.oopweb.com/Java/Documents/ThinkCSJav/Volume/chap15.htm
 * http://www.chris-j.co.uk/parsing.php
 */
	/**
	 * Global variables
	 */
	static String fLine = ""; //holds the line of code read from text file
	static int lineNum = 2; //start of code line numbers, main(){ is #1
	static String code = ""; //will hold code from program 1 line at a time
	static String myInputLine = ""; //will hold input string from user console
	static String nextToken = ""; //the next token to be evaluated
	static String myString = ""; //will hold the formed string for output/write
	static String operand = ""; //temporary variable to hold operands prior to input to infix queue
	static String operator = ""; //temporary variable to hold operands prior to input to infix queue
	static StringTokenizer stCode; //holds the line of code to be tokenized
	static int[] varvalues = new int[52]; //reference of variable values
	static int[] varlist = new int[52]; //reference for status of variables if declared or not
	static Queue queue = new LinkedList(); //holds the post fix queue for evaluation
	static Queue inqueue = new LinkedList(); //holds the in fix queue
	static Stack stack = new Stack(); //for stacking of operators and evaluation of values 

/*The next two methods parses the first line to look for "main() {" 
 * 	and the last line to look for "}""
 */
	//will check for presence of the initial "main() {"  
	public static void is_main() throws Exception{
		lexer();
		if (nextToken.equals("main()")){
			lexer();
			if (nextToken.equals("{")){
				lexer();
				if (stCode.hasMoreTokens())
					throw new Exception ("\nError #1 at Line#" + lineNum + ": Expected nextline after 'main() {'");
			}	
			else throw new Exception ("\nError #1: Absent '{' after main()");
		}
		else throw new Exception ("\nError #1 at Line#" + lineNum + ": Absent 'main(){'");
	}
	
	//will check for presence of closing brace at end of code
	public  static void rbrace() throws Exception{
		if (!nextToken.equals("}") && fLine == null) //if last token is not }
		    throw new Exception ("\nError #2 at Line#" + lineNum + ": Absent closing '}' for main(){");
		if (fLine != null) //if } present before last line
			throw new Exception ("Error #10 at Line#" + lineNum + ": statement not recognized by Integer-C");
			
	}	
	
/* The following will check the "terminals"
 * 1) integers
 * 2) operators (Note: i used the following precedence rules..'()','*|/','%','+|-'
 * 3) identifiers/variables - i allowed only single letter character (for a simple program this will suffice..)	
 */
	
	//will check if string is an operator or not
	public static boolean is_operator_ch(char c1) throws Exception{
		if (c1 == '+' || c1 == '-' || c1 == '/' || c1 == '*' || c1 == '%')
			return true;
		return false;
		
	}
	
	//will check if token is a letter
	public static boolean is_alpha_ch(char c1) throws Exception{
		if (Character.isLetter(c1))
			return true;
		return false;
		
	}
	
	//will check if token is integer
	public static void integer() throws Exception {
		try{
			Integer.parseInt(nextToken);
		}catch (Exception e){
			throw new Exception("\nError #3 at Line#" + lineNum + ": Expecting a valid integer ('" + nextToken + "' is invalid)");
		}
		lexer();
	}

/*
 * This is the LEXER!!!	
 */
	//the lexer! will get next token; if there is no more token return false
	public static boolean lexer(){
		if(stCode.hasMoreTokens()){
			nextToken = stCode.nextToken();
			return true;
		}
		else
			return false;
	}
			

	
/*The following methods below evaluate expressions to their values 
 * Step by step...from infix queue convert to postfix
 * ...Then compute value using postfix!
 */
	//will call two methods below sequentially (for neatness' sake)
	public static void evaluate_exp(){
		in_postfix();
		compute_postfix();
	}
	
	//procedure to convert infix queue to postfix queue for evaluation of numeric expressions
	//infix queue has been prepared by other methods ready to be processed
	public static void in_postfix(){
		while(!inqueue.isEmpty()){
			//if inqueue element is not an operator add it to infix queue
			if (!inqueue.element().toString().equals("+") && !inqueue.element().toString().equals("-") && !inqueue.element().toString().equals("/") && !inqueue.element().toString().equals("*") && !inqueue.element().toString().equals("%") && !inqueue.element().toString().equals("(") && !inqueue.element().toString().equals(")")){
				queue.add(inqueue.remove());
			}
			else{
				//if next infix queue element is close parenthesis, remove from inqueue and pop top of stack into postfix queue and pop the opening parenthesis 
				if (inqueue.element().toString().equals(")")){ 
					inqueue.remove();
					queue.add(stack.pop());
					if (stack.lastElement().toString().equals("("))
						stack.pop();
				}
				//if top of stack is open parenthesis or is empty, push operator from inqueue
				else if (stack.empty() || stack.lastElement().toString().equals("(") || inqueue.element().toString().equals("(")) //if empty stack or top element is open parenthesis push operator
					stack.push(inqueue.remove());
				//if top of stack is (+|-|%)  and inqueue element is (*|+) 	
				else if ((stack.lastElement().toString().equals("+") || stack.lastElement().toString().equals("-") || inqueue.element().toString().equals("%")) && (inqueue.element().toString().equals("*") || inqueue.element().toString().equals("/")))
						stack.push(inqueue.remove());
				/*
				 *if top of stack is (*|/) and inqueue element is (+|-|%) pop stack operator and add this to post fix queue
				 * if top of stack is equal in precedence to inqueue operator, pop stack again and this to post fix queue; then put inqueue operator inside stack 
				*/
				else if((stack.lastElement().toString().equals("+") || stack.lastElement().toString().equals("-")) && (inqueue.element().toString().equals("+") || inqueue.element().toString().equals("-"))){
					queue.add(stack.pop());
					while ((!stack.empty()) && (stack.lastElement().toString().equals("+") || stack.lastElement().toString().equals("-"))){
						queue.add(stack.pop());
					}
					stack.push(inqueue.remove());
				}
				//if inqueue element and top of stack is similarly (*|/), pop top of stack to postfix queue and push inqueue element to stack
				else if ((stack.lastElement().toString().equals("*") || stack.lastElement().toString().equals("/")) && (inqueue.element().toString().equals("*") || inqueue.element().toString().equals("/"))){
					queue.add(stack.pop());
					while ((!stack.empty()) && (stack.lastElement().toString().equals("*") || stack.lastElement().toString().equals("/"))){
						queue.add(stack.pop());
					}	
					stack.push(inqueue.remove());
				}
				//if inqueue element is (+|-) and stack has on top (*|/|%) pop stack and add it to infix queue
				else if ((inqueue.element().toString().equals("+") || inqueue.element().toString().equals("-")) && (stack.lastElement().toString().equals("*") || stack.lastElement().toString().equals("/") || stack.lastElement().toString().equals("%"))){
					queue.add(stack.pop());
					while ((!stack.empty()) && (stack.lastElement().toString().equals("*")|| stack.lastElement().toString().equals("/") || stack.lastElement().toString().equals("%"))){
						queue.add(stack.pop());
					}
					stack.push(inqueue.remove());
				}
				//in case inqueue operator is modulo and top of stack is (*|/), (*|/) takes precedence over % 
				else if (inqueue.element().toString().equals("%") && (stack.lastElement().toString().equals("*") || stack.lastElement().toString().equals("/"))){
					queue.add(stack.pop());
					while ((!stack.empty()) && (stack.lastElement().toString().equals("*")|| stack.lastElement().toString().equals("/"))){
						queue.add(stack.pop());
					}
					stack.push(inqueue.remove());
				}
				else
				//all other cases, just add operator to postfix queue 	
					stack.push(inqueue.remove());	
			}
		}
		//once inqueue has been emptied, check stack and pop remaining contents to infix queue
		while(!stack.empty())
			queue.add(stack.pop());
	}
	
	//will compute the value of a post fix expression from postfix queue
	//makes use of a stack and the postfix queue
	//Based algorithm from this: http://www.chris-j.co.uk/parsing.php
	//the final result will be available in the stack
	public static void compute_postfix(){
		//perform algorithm until postfix queue is emptied
		while (!queue.isEmpty()){
			int opd1 = 0; //second operand
			int opd2 = 0; //first operand
			int opd3 = 0; //holds result of operation

			String opt = "";
			//from queue push into stack if operand (not operator)	
			if (!queue.element().toString().equals("+") && !queue.element().toString().equals("-") && !queue.element().toString().equals("/") && !queue.element().toString().equals("*") && !queue.element().toString().equals("%")){	
				stack.push(queue.remove());	
			}
			//if next in queue is operator, pop from stack 2 items and assign to op1 and op2 then evaluate using operator
			else{
				opd1 = Integer.parseInt(stack.pop().toString());
				opd2 = Integer.parseInt(stack.pop().toString());
				opt = queue.remove().toString();
				
				//execute the operation 
				if (opt.equals("+"))
					opd3 = opd2 + opd1;
				if (opt.equals("-"))
					opd3 = opd2 - opd1;
				if (opt.equals("*"))
					opd3 = opd2 * opd1;
				if (opt.equals("/"))
					opd3 = opd2 / opd1;
				if (opt.equals("%"))
					opd3 = opd2 % opd1;
				//push result of operation to stack
				stack.push(opd3);	
			}
		}	
	}

	
/*
 * The following will parse non-terminals
 * 1) string expressions
 * 2) "numeric" expressions
 * 3) assignment statements
 * 4) declarations
 * 5) expressions not specified
 * 6) readline procedure
 * 7) "write" procedures
 */
		
		//will check for string expression
		//string expression grammar::= '"'{any characters}'"'	
		public static void strExpression() throws Exception{
			boolean single = false; //if string has only one word
			myString = "";
			myString = nextToken.replaceAll("\"", ""); //add first element to string
			while (stCode.hasMoreElements()){ //repeat steps until last element that contains closing "
				lexer();
				myString = myString + " " + nextToken.replaceAll("\"", "");
			}		
			if (nextToken.equals('"') || nextToken.charAt(nextToken.length()-1)=='"')
				lexer();
			else
				throw new Exception ("\nError #4 at Line#" + lineNum + ": Expecting end quotes '\"' in string expression");
			
		}
		/*parser for "numeric" or arithmetic expressions 
		 *Grammar test for numerical expressions
		 *grammar::= {"("(<variable>|<integer>|"("<numeric expression>")")")"}{(+|-|*|/|%)(<variable>|<integer>|"("<numeric expression>")")} 	
		 *again, there should be a space in between operators and operands
		 *after successful parsing of elements, values are added to infix queue for evaluation later
		 */		
		public static void numExpression() throws Exception{
			//check if token is an open parenthesis operator, evaluate expression inside parenthesis first eg "("(<variable>|<integer>|"("<numeric expression>")")")"
			if (nextToken.equals("(")){
				inqueue.add(nextToken);
				lexer();
				numExpression(); //recursively check for numeric expressions within parenthesis
				if (nextToken.equals(")")){ //check for presence of closing parenthesis
					inqueue.add(nextToken);	//add value to infix queue
					lexer();
				}	
				else throw new Exception ("\nError #5 at Line#" + lineNum + ": Expecting a closing ')' for numeric expression"); 
				
			}	
			//check if token is non-numeric and a valid character variable eg <variable>
			else if (is_alpha_ch(nextToken.charAt(0)) && nextToken.length()==1){
				int arr_index = 0;
				char c3 = nextToken.charAt(0);
				if ((int)c3 >= 97)
					arr_index = (int)c3 % 97; //based on ASCII values
				else
					arr_index = (int)c3 % 65 + 26; 
				if (varlist[arr_index] == 1 ){//if character check if declared variable 
					inqueue.add(varvalues[arr_index]); //add value of variable in infix queue
					lexer();
				}
				else throw new Exception ("\nError #6 at Line#" + lineNum + ": Use of an undeclared variable '" + c3 + "'");
			}
			//if token has integer first character, check if it is a valid integer 
			else {
				operand = nextToken; //store value of token in case it passes integer test for addition into infix queue
				integer(); //check if valid integer, signed or unsigned
				inqueue.add(operand); //add to infix queue
			}
			//check for one or more sequences of operator and variable|integer|numeric expressions
			//if next token is closing parenthesis, skip and return; 
			//if last token in line is an operator with no following numeric expression throw error
			while ((stCode.hasMoreTokens() && nextToken.charAt(0) != ')') || is_operator_ch(nextToken.charAt(0))){ 
				if (nextToken.length()==1 ){//operators have only 1 character
				    if (is_operator_ch(nextToken.charAt(0))){ 
				    	inqueue.add(nextToken.charAt(0)); //add value to infix queue
				    	if (lexer() == true){
				    		numExpression();
				    	}	
				    	else
				    		throw new Exception ("\nError #12 at Line#" + lineNum + ": Missing numeric expression");
				    }
				    else throw new Exception("\nError #7 at Line#" + lineNum + ": Invalid operator '" + nextToken +  "', expecting (+|-|*|/)"); 
			    }
				else
					throw new Exception("\nError #7 at Line#" + lineNum + ": Invalid operator '" + nextToken + "', expecting (+|-|*|/)"); 
			}
			    
		}		
	

	//parser for assignment statements 
	//uses above method-parser for numeric expressions		
	public static void assignVariable() throws Exception{
		char c5 = nextToken.charAt(0);
		int index = 0;
		if (is_alpha_ch(c5)){ //...if it has a valid letter variable
			if ((int)c5 >=97)
				index = (int)c5 % 97;
			else
				index = (int)c5 % 65 + 26;
			if (varlist[index] == 1){ //...if its variable is declared 
				lexer();
				if (nextToken.equals("=")){
					lexer();
					numExpression();
					evaluate_exp();
					varvalues[index] = Integer.parseInt(stack.pop().toString()); //assign values to variable values array
				}
				else throw new Exception ("Error #9 at Line#" + lineNum + ": expecting '=' operator after variable in assignment statement");
			}
			else throw new Exception ("Error #6 at Line#" + lineNum + ": variable " + c5 + " not declared");
		}	
		else throw new Exception ("Error #8 at Line#" + lineNum + ": Invalid variable or statement"); 	
	}

	//parser for declarative statements
	//declaration grammar::= " "<variable>" "(=)([+|-]<integer>[" "{+|-|/|*|%" "<integer>}]
	public static void declaration() throws Exception{
		int index = 0;
		lexer();
		if (nextToken.length()==1){//only 1 char variables (a-z, A-Z) allowed
			char c1 = nextToken.charAt(0);
		    if (is_alpha_ch(c1)){ //check if valid character variable
		    	if ((int)c1 >=97)
		    		index = (int)c1 % 97;
		    	else
		    		index = (int)c1 % 65 + 26;
		    	if (varlist[index]==1)
		    		throw new Exception ("Error #11 at Line#" + lineNum + ": a variable cannot be declared more than once");
		    	varlist[index]= 1;	
		    	lexer();
		    
		    }
			else throw new Exception("\nError #8 at Line#" + lineNum + ": Invalid variable or statement"); 
			if (nextToken.equals("=")){ //if there is an equal sign or attempt to assign value
				lexer();
				numExpression();
				evaluate_exp();
				varvalues[index] = Integer.parseInt(stack.pop().toString()); //assign value to variable values array 
			}
			
		}
		else throw new Exception("\nError #8 at Line#" + lineNum + ": Invalid variable or statement"); 
			
	}	
	
	//calls appropriate parsers for different expressions
	public static void expression() throws Exception{
	//expression grammar::= <quoted string>|<numeric expression>}|"("<numeric expression>")"	
		if (nextToken.charAt(0)=='"') //if quoted string expression
			strExpression();
		else { //otherwise...
			numExpression();
			evaluate_exp();
			myString = stack.pop().toString();
		}

		
	}
	
/*
 * the following are parsers for 'write' and 'read' procedures	
 * uses the above methods to parse expressions as needed
 */
	//write grammar::= ("write"|"writeline")" "<expression>
	public static void write() throws Exception{
		int mywrite = 0;
		if (nextToken.equals("write"))
			mywrite = 0;
		else
			mywrite = 1;
		lexer();
		expression();
		
		if (mywrite==1)
			System.out.println(myString);
		else
			System.out.print(myString);	
		lexer();	
	}
	//reads user input values for variables (only integers allowed)
	//read grammar::= ("read"|"readln")<variable>
	public static void read() throws Exception{
		int index = 0;
		myInputLine = "";
		String myRead = nextToken;
		lexer();
		if ((nextToken.length()==1) && (!stCode.hasMoreTokens())){
			char c1 = nextToken.charAt(0);
			if ((int)c1 >= 97)
				index = (int)c1 % 97;
			else
				index = (int)c1 % 65 + 26; 
			if (varlist[index]==1){
				if (myRead.equals("readline")){
					BufferedReader br2 = new BufferedReader(new InputStreamReader(System.in));
					myInputLine = br2.readLine(); //read input from user
					try{						
						varvalues[index] = Integer.parseInt(myInputLine); //enter values into variable value array
					}
					catch (Exception e){
						throw new Exception ("\nError #3 at Line#" + lineNum + ": Expecting a valid integer ('" + myInputLine + "' is invalid)"); 
					}
				}
				else{ //not yet implemented for "read" function
						
				}
				
			}
			else throw new Exception ("\nError #6 at Line#" + lineNum + ": Use of an undeclared variable '" + c1 + "'");
		}
		else
			throw new Exception ("Error #8 at Line#" + lineNum + ": Invalid variable or statement");
	}
	

	
/* This is the main body of the Integer-C program
 * 	
 */
	public static void main(String[] args) throws Exception {
		String lastToken = ""; //to hold last token for checking of ending bracket 
		String fname = args[0]; //holds the string name of text file with integer-c code
		int index = 0; 
	
		try{
			FileInputStream fstream = new FileInputStream(fname);	
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			fLine = br.readLine();	//get first line of code from file
			code = fLine.trim();  //trim spaces before and after the line
			stCode =  new StringTokenizer(code);  //tokenize!
			is_main(); //parse first line that should contain main (){
			
			/*
			 * The lines below will parse the body of the program 
			 * It will call the necessary parsers for the following
			 * 1) declarations 
			 * 2) write|writeline procedures
			 * 3) readline procedures
			 * 4) assignment statements
			 */
			while ((fLine = br.readLine())!= null){ //while the file has  more lines
				code = fLine.trim(); 
				stCode =  new StringTokenizer(code); //tokenize next line!	

				while (stCode.hasMoreTokens()){
					lexer(); //start the lexer();
					lastToken = nextToken; //copy last token
					//1. if starts with "int" for declarations
					if (nextToken.equals("int"))
							declaration(); //call parser for declarations
					//2. if starts with write or writeline for output commands
					else if (nextToken.equals("write") || nextToken.equals("writeline"))
						    write(); //call parser for write commands
				    //3. if starts with readline for input values from user
					else if (/*nextToken.equals("read") || */nextToken.equals("readline"))
							read(); //call parser for readline command
						//4. if starts with a candidate variable (single character), check first...see above
					else if (nextToken.length() == 1 && !nextToken.equals("}"))
						assignVariable();
						//if does not match any of above, error...
					else if (nextToken.equals("}"))
						break;
					else throw new Exception ("Error #10 at Line#" + lineNum + ": statement not recognized by Integer-C");
					
				}
			 lineNum++;	
			}		
			nextToken = lastToken;
			rbrace(); //parse last line, should contain '}'
		}catch (Exception e){
			System.err.println("System Error:" + e.getMessage()); //if file not found or cannot be read
		}

}

}	