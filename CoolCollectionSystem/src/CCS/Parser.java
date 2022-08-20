package CCS;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.HashMap;

/**
 * Used by the {@link Collection} class to parse a Collection.<br>
 * You might want to use the Collection class instead.
 * @author Mika Thein
 * @version 1.0
 * @see Collection
 * @see InvalidSyntaxException
 * @see InvalidObjectException
 */
public class Parser {
	
	/**
	 * Translates the given string into useful text to read the given Collection. (Removes unnecessary text like comments, line breaks, tabs or text outside of the brackets.)
	 * @param str Unparsed Collection in text notation.
	 * @return A clear text notation of the Collection without useless passages.
	 */
	public static String strip(String str) {
		String r = "";
		int f = 0, t = str.length(), o = 0;
		boolean quote = false;
		for(int i = 0; i<t; i++) {
			if(str.charAt(i) == '\\') i++;
			else if(str.charAt(i) == '\"') quote = !quote;
			else if(str.charAt(i) == '<' && !quote) {
				if(o == 0) f = i;
				o++;
			} else if(str.charAt(i) == '>' && !quote) {
				o--;
				if(o == 0) {
					t = i;
					break;
				}
			}
		}
		try {
			r = str.substring(f, t+1).replace("\n", "").replace("\t", "").replace("\r", "");
			return r;
		} catch(java.lang.StringIndexOutOfBoundsException e) {
			throw new InvalidSyntaxException("Missing collection! A Collection has to start with a '<' and has to end with a '>'.");
		}
	}
	
	/**
	 * Splits a string at every delimiter outside of quotes and Collections.
	 * @param str The input string.
	 * @param d The delimiter.
	 * @return An array out of resulting elements.
	 */
	public static String[] split(String str, char d) {
		String del = "π!#13";
		while(str.contains(del)) del += "+";
		
		int o = 0;
		boolean quote = false;
		for(int i = 0; i<str.length(); i++) {
			if(str.charAt(i) == '\\') i++;
			else if(str.charAt(i) == '\"') quote = !quote;
			else if(str.charAt(i) == '<' && !quote) o++;
			else if(str.charAt(i) == '>' && !quote) o--;
			else if(str.charAt(i) == d && !quote && o == 0) {
				str = str.substring(0, i) + del + str.substring(i+1, str.length());
				i += del.length();
			}
		} return str.split(del);
	}
	
	/**
	 * Fills a HashMap with the content of a Collection.
	 * @param stripped The Collection in text notation and stripped.
	 * @param rawContentMap The HashMap which is supposed to be filled.
	 * @see #strip(String)
	 * @throws InvalidSyntaxException If a key is invalid or exists multiple times.
	 */
	public static void fillRaw(String stripped, HashMap<String, String> rawContentMap) {
		String[] obj = split(stripped.substring(1, stripped.length()-1), ';');
		for(String i : obj) {
			if(i.trim().length() > 0) {
				String[] object = i.split(":", 2);
				String k = object[0].trim();
				if(!isValidKey(k)) throw new InvalidSyntaxException("Invalid key: \"" + k + "\".");
				else if(rawContentMap.containsKey(k)) throw new InvalidSyntaxException("An item set with the key \"" + k + "\" does already exist.");
				rawContentMap.put(k, object[1].trim());
			}
		}
	}
	
	/**
	 * @param o The object.
	 * @return The object as String.
	 * @throws IOException
	 * @see #toObject(String)
	 */
	public static String toString(Serializable o) throws IOException {
		try(ByteArrayOutputStream b = new ByteArrayOutputStream(); ObjectOutputStream out = new ObjectOutputStream(b)) {
			out.writeObject(o);
			return Base64.getEncoder().encodeToString(b.toByteArray());
		}
	}
	
	/**
	 * @param str The String.
	 * @return String as object.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @see #toString(Serializable)
	 */
	public static Object toObject(String str) throws IOException, ClassNotFoundException {
		try(ByteArrayInputStream b = new ByteArrayInputStream(Base64.getDecoder().decode(str)); ObjectInputStream in = new ObjectInputStream(b)) {
			return in.readObject();
		}
	}
	
	/**
	 * Prepares a String for being placed into a Collection.<br>
	 * (Converts backslash escapes: \", \\, \n, \r, \b, \t and \f.)
	 * @param str The original String.
	 * @return The prepared String.
	 * @see #decodeString(String)
	 */
	public static String prepareString(String str) {
		String r = "";
		for(int i = 0; i<str.length(); i++) {
			switch(str.charAt(i)) {
				case '\n':
					r += "\\n";
					break;
				case '\\':
					r += "\\\\";
					break;
				case '\r':
					r += "\\r";
					break;
				case '"':
					r += "\\\"";
					break;
				case '\b':
					r += "\\b";
					break;
				case '\t':
					r += "\\t";
					break;
				case '\f':
					r += "\\f";
					break;
				default:
					r += str.charAt(i);
			}
		}
		return r;
	}
	
	/**
	 * Converts a String from its "prepared version" into its original form.<br>
	 * (Converts backslash escapes: \", \\, \n, \r, \b, \t and \f.)
	 * @param str The "prepared" String.
	 * @return The original String.
	 * @see #prepareString(String)
	 */
	public static String decodeString(String str) {
		String r = "";
		for(int i = 0; i<str.length(); i++) {
			if(str.charAt(i) == '\\') {
				if(str.length() <= i+1) throw new InvalidSyntaxException("Invalid backslash escape: \"" + str + "\", char at: " + i + ".");
				switch(str.charAt(i+1)) {
					case 'n':
						r += "\n"; i++;
						break;
					case '\\':
						r += "\\"; i++;
						break;
					case 'r':
						r += "\r"; i++;
						break;
					case '"':
						r += "\""; i++;
						break;
					case 'b':
						r += "\b"; i++;
						break;
					case 't':
						r += "\t"; i++;
						break;
					case 'f':
						r += "\f"; i++;
						break;
					default:
						throw new InvalidSyntaxException("Invalid backslash escape: \"" + str + "\", char at: " + (i+1) + " ('\\" + str.charAt(i+1) + "').");
				}
			} else r += str.charAt(i);
		} return r;
	}
	
	/**
	 * A valid key matches the following regular expression: "[a-zA-Z_\- ]+".
	 * @param key The key.
	 * @return Whether the key is a valid key.
	 */
	public static boolean isValidKey(String key) {
		return key.matches("[a-zA-Z_\\- ]+");
	}
	
	public static final int INTEGER = 0,
			LONG = 1,
			DOUBLE = 2,
			BIGDECIMAL = 3;
	
	/**
	 * @param str The input string.
	 * @return Whether the input string consists of an integer, a long, a double or a big decimal.
	 * @see #INTEGER
	 * @see #LONG
	 * @see #DOUBLE
	 * @see #BIGDECIMAL
	 */
	public static int getNumType(String str) {
		try {
			long l = Long.parseLong(str);
			return Math.abs(l) > Integer.MAX_VALUE ? LONG : INTEGER;
		} catch(java.lang.NumberFormatException e1) {
			try {
				BigDecimal d = new BigDecimal(str);
				if(d.abs().doubleValue() == Double.POSITIVE_INFINITY) return BIGDECIMAL;
				return d.compareTo(BigDecimal.valueOf(d.doubleValue())) != 0 ? BIGDECIMAL : DOUBLE;
			} catch(java.lang.NumberFormatException e2) { return -1; }
		}
	}

}
