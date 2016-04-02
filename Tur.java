package net.kc_kennylau.tur;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Stack;

public class Tur {
	public final int STOP = -1;
	public final int N = 0;
	public final int E = 1;
	public final int S = 2;
	public final int W = 3;
	
	private char[][] tape;
	private Hashtable<String,ArrayList<String[]>> instructions;
	
	private boolean ignore_whitespace;
	
	public Tur() {
		instructions = new Hashtable<String,ArrayList<String[]>>();
		this.ignore_whitespace = true;
	}
	public Tur(String code) {
		this.setTape(" ");
		this.parse(code);
		this.ignore_whitespace = true;
	}
	public Tur(String code, String tape) {
		this.setTape(tape);
		this.parse(code);
		this.ignore_whitespace = true;
	}
	public Tur(String code, String config, String tape) {
		this.setTape(tape);
		this.config(config);
		this.parse(code);
	}
	
	
	public void setTape(String tape) {
		String[] segments = tape.split("\n");
		this.tape = new char[segments.length][];
		for(int i=0;i<segments.length;i++){
			this.tape[i] = segments[i].toCharArray();
		}
	}
	public String getTape() {
		String[] segments = new String[this.tape.length];
		int max_before = 0;
		int min_after = -1;
		for(int i=0;i<segments.length;i++){
			segments[i] = new String(this.tape[i]);
			max_before = Math.max(max_before, segments[i].replaceAll("^(\\s*).*$", "$1").length());
			if(min_after == -1){
				min_after = segments[i].replaceAll("^(.+)\\s+$", "$1").length();
			}else{
				min_after = Math.min(min_after, segments[i].replaceAll("^(.*)\\s*$", "$1").length());
			}
		}
		for(int i=0;i<segments.length;i++){
			segments[i] = segments[i].substring(max_before,min_after);
		}
		int count1 = 0;
		int count2 = 0;
		for(int i=0;i<segments.length;i++){
			if(segments[i].matches("\\s+")){
				count1++;
			}else{
				break;
			}
		}
		for(int i=segments.length-1;i>=0;i--){
			if(segments[i].matches("\\s+")){
				count2++;
			}else{
				break;
			}
		}
		String[] temp = new String[this.tape.length-count1-count2];
		System.arraycopy(segments, count1, temp, 0, segments.length-count1-count2);
		return String.join("\n", segments);
	}
	
	
	public void config(String string) {
		if(string.indexOf("w") != -1){
			this.ignore_whitespace = false;
		}else{
			this.ignore_whitespace = true;
		}
	}
	
	private String expand(String regex) {
		ArrayList<String> units = new ArrayList<String>();
		for(int i=0;i<regex.length();i++){
			if(regex.charAt(i) == '\\' && regex.length() > i+1){
				units.add(regex.substring(i,(i++)+2));
			}else{
				units.add(regex.substring(i,i+1));
			}
		}
		String[] segments = units.toArray(new String[units.size()]);
		for(int i=1;i<segments.length-1;){
			if(segments[i].equals("-")){
				int start = segments[i-1].codePointAt(0);
				int end = segments[i+1].codePointAt(0);
				segments[i-1] = "";
				segments[i] = "";
				segments[i+1] = "";
				for(int j=start;j<=end;j++){
					segments[i] = segments[i] + new String(new int[]{j},0,1);
				}
				i += 3;
			}else{
				i++;
			}
		}
		for(int i=0;i<segments.length;i++){
			if((segments[i].length()==2) && (segments[i].charAt(0)=='\\') && (i==0||!segments[i-1].isEmpty()) && (i==segments.length-1||!segments[i+1].isEmpty())){
				segments[i] = segments[i].substring(1);
			}
		}
		return String.join("", segments);
	}
	
	private String dictionary(char c){
		switch(c){
		case 'd':	return "0123456789";
		case '1':	return "123456789";
		case '2':	return "01";
		case '3':	return "012";
		case '4':	return "0123";
		case '5':	return "01234";
		case '6':	return "012345";
		case '7':	return "0123456";
		case '8':	return "01234567";
		case '9':	return "012345678";
		case 'h':	return "0123456789abcdef";
		case 'i':	return "0123456789ABCDEF";
		case 'j':	return "0123456789abcdefABCDEF";
		case 'w':	return "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		case 'l':	return "abcdefghijklmnopqrstuvwxyz";
		case 'u':	return "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		case 'a':	return "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		case 'b':	return "_0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		case '@':	return "23456789";
		case '#':	return "3456789";
		case '$':	return "456789";
		case '%':	return "56789";
		case '^':	return "6789";
		case '&':	return "789";
		case '*':	return "89";
		default:	return "";
		}
	}
	
	
	public void parse(String code) {
		if(this.ignore_whitespace){
			while(!code.equals(code = code.replaceAll("(?<!')\\x20+(?=([^\"]*\"[^\"]*\")*[^\"]*$)","")));
		}
		while(!code.equals(code = code.replaceAll("[\\t\\n\\x0B\\f\\r]","")));
		ArrayList<String> segments = new ArrayList<String>();
		while(!code.isEmpty()){
			if(code.substring(0,1).equals("'")){
				segments.add("+"+code.substring(1,2));
				code = code.substring(2);
			}else if(code.substring(0,1).equals("\"")){
					int next_quote_mark = code.indexOf("\"",1);
					if(next_quote_mark == -1){
						System.err.println("Unbalanced quotation marks");
						return;
					}else if(next_quote_mark == 2){
						segments.add("+"+code.substring(1, 2));
					}else{
						segments.add("+"+code.substring(1, next_quote_mark));
					}
					code = code.substring(next_quote_mark+1);
			}else{
				segments.add(code.substring(0,1));
				code = code.substring(1);
			}
		}
		for(int i=0;i<segments.size();){
			String current_state = segments.get(i);
			String current_symbol = (segments.size()>i+1)?segments.get(i+1):"1.";
			String next_symbol = (segments.size()>i+2)?segments.get(i+2):"1_";
			ArrayList<String[]> temp = this.instructions.get(current_state);
			if(temp == null){
				temp = new ArrayList<String[]>();
			}
			if(current_state.equals("H")){
				temp.add(new String[]{current_symbol,next_symbol});
				i += 3;
			}else{
				String next_direction = (segments.size()>i+3)?segments.get(i+3):"r";
				String next_state = (segments.size()>i+4)?segments.get(i+4):"H";
				temp.add(new String[]{current_symbol,next_symbol,next_direction,next_state});
				i += 5;
			}
			this.instructions.put(current_state, temp);
		}
	}
	
	
	public void run(){
		int head_x = 0;
		int head_y = 0;
		char current_symbol = tape[0][0];
		String previous_state = "0";
		String current_state = "0";
		int previous_direction = 1;
		Stack<Character> stack = new Stack<Character>();
		char clipboard = ' ';
		while(true){
			if(current_state.equals("H")){
				if(this.instructions.containsKey("H")){
					for(int i=0;i<this.instructions.get("H").size();i++){
						if(this.instructions.get("H").get(i)[0].equals(previous_state) || this.instructions.get("H").get(i)[0].equals("+.")){
							String append = this.instructions.get("H").get(i)[1];
							if(append.length() > 1){
								append = append.substring(1);
							}
							if(head_x+append.length() > this.tape[head_y].length){
								this.tape[head_y] = (new String(this.tape[head_y],0,head_x) + append).toCharArray();
							}else{
								this.tape[head_y] = (new String(this.tape[head_y],0,head_x) + append + new String(this.tape[head_y],head_x+append.length(),this.tape[head_y].length-head_x-append.length())).toCharArray();
							}
							break;
						}
					}
				}
				break;
			}
			if(this.instructions.containsKey(current_state)){
				ArrayList<String[]> current_instruction = this.instructions.get(current_state);
				boolean matched = false;
				String[] matched_instruction;
				String dictionary = "";
				for(int i=0;i<current_instruction.size();i++){
					String candidate = current_instruction.get(i)[0];
					if(candidate.length()==1){
						matched = candidate.equals(String.valueOf(current_symbol));
					}else if(candidate.length()==2){
						if(candidate.equals("+.")){
							matched = true;
						}else if(candidate.equals("+_")){
							matched = (current_symbol == ' ');
						}else if(!dictionary(candidate.charAt(1)).isEmpty()){
							dictionary = dictionary(candidate.charAt(1));
						}else if(candidate.equals("+D")){
							matched = String.valueOf(current_symbol).matches("[^0-9]");
						}else if(candidate.equals("+E")){
							matched = String.valueOf(current_symbol).matches("[^1-9]");
						}else if(candidate.equals("+H")){
							matched = String.valueOf(current_symbol).matches("[^0-9a-f]");
						}else if(candidate.equals("+I")){
							matched = String.valueOf(current_symbol).matches("[^0-9A-F]");
						}else if(candidate.equals("+J")){
							matched = String.valueOf(current_symbol).matches("[^0-9a-fA-F]");
						}else if(candidate.equals("+W")){
							matched = String.valueOf(current_symbol).matches("[^a-zA-Z]");
						}else if(candidate.equals("+L")){
							matched = String.valueOf(current_symbol).matches("[^a-z]");
						}else if(candidate.equals("+U")){
							matched = String.valueOf(current_symbol).matches("[^A-Z]");
						}else if(candidate.equals("+A")){
							matched = String.valueOf(current_symbol).matches("[^0-9a-zA-Z]");
						}else if(candidate.equals("+B")){
							matched = String.valueOf(current_symbol).matches("[^_0-9a-zA-Z]");
						}else{
							matched = String.valueOf(current_symbol).equals(candidate.substring(1,2));
						}
					}else{
						candidate = "["+candidate.replaceAll("([\\[\\]])","\\\\$1")+"]";
						matched = String.valueOf(current_symbol).matches(candidate);
						dictionary = expand(candidate);
					}
					if(!dictionary.isEmpty()){
						matched = String.valueOf(current_symbol).matches("["+dictionary+"]");
					}
					if(matched){
						matched_instruction = current_instruction.get(i);
						String new_symbol = matched_instruction[1];
						char new_direction_char = matched_instruction[2].charAt(0);
						current_state = matched_instruction[3];
						if(new_symbol.length()==1){
							this.tape[head_y][head_x] = new_symbol.charAt(0);
						}else if(new_symbol.length()==2){
							if(new_symbol.equals("+x")){
								clipboard = this.tape[head_y][head_x];
								this.tape[head_y][head_x] = ' ';
							}else if(new_symbol.equals("+c")){
								clipboard = this.tape[head_y][head_x];
							}else if(new_symbol.equals("+v")){
								this.tape[head_y][head_x] = clipboard;
							}else if(new_symbol.equals("+,")){
								stack.push(this.tape[head_y][head_x]);
							}else if(new_symbol.equals("+.")){
								if(!stack.empty()){
									this.tape[head_y][head_x] = stack.pop();
								}else{
									previous_state = current_state;
									current_state = "H";
								}
							}else if(new_symbol.equals("+;")){
								if(!stack.empty()){
									this.tape[head_y][head_x] = stack.peek();
								}
							}else if(new_symbol.equals("+:")){
								if(!stack.empty()){
									stack.push(stack.peek());
								}
							}else if(new_symbol.equals("+\\")){
								if(stack.size() > 2){
									char temp1 = stack.pop();
									char temp2 = stack.pop();
									stack.push(temp1);
									stack.push(temp2);
								}
							}else if(new_symbol.equals("+/")){
								if(stack.size() > 2){
									char temp = stack.pop();
									this.tape[head_y][head_x] = stack.pop();
									stack.push(temp);
								}
							}else if(new_symbol.equals("+@")){
								if(stack.size() > 2){
									char temp1 = stack.pop();
									char temp2 = stack.pop();
									char temp3 = stack.pop();
									stack.push(temp2);
									stack.push(temp1);
									stack.push(temp3);
								}
							}else if(new_symbol.equals("+#") || new_symbol.equals("+a")){
								if(stack.size() > 2){
									char temp1 = stack.pop();
									char temp2 = stack.pop();
									this.tape[head_y][head_x] = stack.pop();
									stack.push(temp2);
									stack.push(temp1);
								}
							}else if(new_symbol.equals("+_")){
								this.tape[head_y][head_x] = ' ';
							}else if(new_symbol.equals("+=")){
								//do not change
							}else if(!dictionary(new_symbol.charAt(1)).isEmpty()){
								new_symbol = dictionary(new_symbol.charAt(1));
								if(dictionary.isEmpty()){
									this.tape[head_y][head_x] = new_symbol.charAt(1);
								}else{
									int index = dictionary.indexOf(this.tape[head_y][head_x]);
									this.tape[head_y][head_x] = new_symbol.charAt(index<0 ? new_symbol.length()-1 : index);
								}
							}else{
								this.tape[head_y][head_x] = new_symbol.charAt(1);
							}
						}else{
							if(dictionary.isEmpty()){
								this.tape[head_y][head_x] = new_symbol.charAt(1);
							}else{
								new_symbol = new_symbol.substring(1);
								int index = dictionary.indexOf(this.tape[head_y][head_x]);
								this.tape[head_y][head_x] = new_symbol.charAt(index<0 ? new_symbol.length()-1 : index);
							}
						}
						int new_direction;
						switch(new_direction_char){
						case 'u':
						case 'U':
						case 'n':
						case 'N':
						case '^':
							new_direction = this.N;
							break;
						case 'r':
						case 'R':
						case 'e':
						case 'E':
						case '>':
							new_direction = this.E;
							break;
						case 'd':
						case 'D':
						case 's':
						case 'S':
						case 'v':
							new_direction = this.S;
							break;
						case 'l':
						case 'L':
						case 'o':
						case 'O':
						case 'w':
						case 'W':
						case '<':
							new_direction = this.W;
							break;
						case '_':
						case '=':
						case '0':
							new_direction = this.STOP;
							break;
						case '+':
							new_direction = previous_direction;
							break;
						case 'c':
							new_direction = previous_direction<0 ? -1 : (previous_direction+1)%4;
							break;
						case '-':
							new_direction = previous_direction<0 ? -1 : (previous_direction+2)%4;
							break;
						case 'a':
							new_direction = previous_direction<0 ? -1 : (previous_direction+3)%4;
							break;
						default:
							System.err.println("invalid direction: " + String.valueOf(new_direction_char));
							return;
						}
						switch(new_direction){
						case N:
							head_y--;
							break;
						case E:
							head_x++;
							break;
						case S:
							head_y++;
							break;
						case W:
							head_x--;
							break;
						case STOP:
							break;
						}
						previous_direction = new_direction;
						if(head_y<0){
							char[][] temp = new char[this.tape.length+1][];
							System.arraycopy(this.tape, 0, temp, 1, this.tape.length);
							this.tape = temp;
							for(int j=0;j<head_x;j++){
								this.tape[0][j] = ' ';
							}
							head_y++;
							current_symbol = ' ';
						}else if(head_y>=this.tape.length){
							char[][] temp = new char[this.tape.length+1][];
							System.arraycopy(this.tape, 0, temp, 0, this.tape.length);
							this.tape = temp;
							for(int j=0;j<head_x;j++){
								this.tape[head_y][j] = ' ';
							}
							current_symbol = ' ';
						}else if(head_x<0){
							this.tape[head_y] = (" " + new String(this.tape[head_y])).toCharArray();
							head_x++;
							current_symbol = ' ';
						}else if(head_x>=this.tape[head_y].length){
							this.tape[head_y] = (new String(this.tape[head_y]) + " ").toCharArray();
							current_symbol = ' ';
						}else{
							current_symbol = this.tape[head_y][head_x];
						}
						break;
					}
				}
				if(!matched){
					previous_state = current_state;
					current_state = "H";
				}
			}else{
				
			}
		}
		
		System.out.println(this.getTape());
	}
	
	
	public static void main(String[] args) throws IOException{
		Tur x = new Tur();
		switch(args.length){
		case 0:
			break;
		case 1:
			x.setTape(" ");
			break;
		case 2:
			x.setTape(args[1]);
			break;
		default:
			x.config(args[1]);
			x.setTape(args[2]);
		}
		Path path = FileSystems.getDefault().getPath(args[0]);
		String code = new String(Files.readAllBytes(path),Charset.defaultCharset());
		x.parse(code);
		x.run();
	}
}
