package CCS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

/**
 * A class for reading and writing Collections.
 * @author Mika Thein
 * @version 1.0
 * @see #Collection()
 * @see #Collection(String)
 * @see #Collection(File)
 * @see #asObjects(String)
 * @see Parser
 * @see InvalidSyntaxException
 * @see InvalidObjectException
 */
public class Collection {
	
	/**
	 * The default notation of dates (ISO-8601):<br>
	 * {@code yyyy-MM-dd'T'HH:mm:ssZ}
	 */
	public final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	
	/**
	 * Creates a new empty Collection.
	 * @see #Collection(String)
	 * @see #Collection(File)
	 */
	public Collection() {}
	
	/**
	 * Converts a given Collection in text notation into a Collection.
	 * @param collection The Collection in text notation.
	 * @see #Collection()
	 * @see #Collection(File)
	 * @throws InvalidSyntaxException If the String does not contain a Collection or if it contains an invalid key (or two equal keys).
	 */
	public Collection(String collection) {
		Parser.fillRaw(Parser.strip(collection), rawContent);
	}
	
	/**
	 * Converts a given Collection in text notation into a Collection.
	 * @param file A file containing the collection in text notation.
	 * @throws InvalidSyntaxException If the String does not contain a Collection or if it contains an invalid key (or two equal keys).
	 * @throws IOException If it is not possible to read the file.
	 */
	public Collection(File file) throws IOException {
		BufferedReader r = new BufferedReader(new FileReader(file));
		String t = "";
		for(String l; (l = r.readLine()) != null; t += l);
		Parser.fillRaw(Parser.strip(t), rawContent);
	}
	
	private final HashMap<String, String> rawContent = new HashMap<>();
	private final HashMap<String, Object[]> content = new HashMap<>();
	
	/**
	 * Returns an empty array if there is no item set with that key.
	 * @param key The key of the item set.
	 * @return The item set as Object array.
	 * @see #firstObject(String)
	 * @throws InvalidSyntaxException If the item set contains an invalid data type.
	 * @throws InvalidObjectException If the class of an object does not exist or the object could not be interpreted.
	 */
	public Object[] asObjects(String key) {
		if(has(key)) {
			if(content.containsKey(key)) return content.get(key);
			String[] c = Parser.split(rawContent.get(key), ',');
			Object[] r = new Object[c.length];
			int numType;
			for(int i = 0; i<c.length; i++) {
				c[i] = c[i].trim();
				// String
				if(c[i].startsWith("\"") && c[i].endsWith("\"")) {
					r[i] = Parser.decodeString(c[i].substring(1, c[i].length()-1));
				}
				// Number
				else if((numType = Parser.getNumType(c[i])) > -1) {
					// Integer
					if(numType == Parser.INTEGER) r[i] = Integer.parseInt(c[i]);
					// Long
					else if(numType == Parser.LONG) r[i] = Long.parseLong(c[i]);
					// Double
					else if(numType == Parser.DOUBLE) r[i] = Double.parseDouble(c[i]);
					// BigDecimal
					else r[i] = new BigDecimal(c[i]);
				}
				// Boolean
				else if(c[i].equalsIgnoreCase("true") || c[i].equalsIgnoreCase("false")) {
					r[i] = Boolean.parseBoolean(c[i]);
				}
				// Serializable
				else if(c[i].startsWith("i[\"") && c[i].endsWith("\"]")) {
					try {
						r[i] = Parser.toObject(Parser.decodeString(c[i].substring(3, c[i].length()-2)));
					} catch(ClassNotFoundException e) {
						throw new InvalidObjectException("Class not found (key: \"" + key + "\").");
					} catch(IOException e) {
						throw new InvalidObjectException("Cannot interpretate object (key: \"" + key + "\").");
					}
				} else {
					// Date
					try {
						r[i] = dateFormat.parse(c[i]);
					} catch(java.text.ParseException e1) {
						// Collection
						try {
							r[i] = new Collection(c[i]);
						} catch(InvalidSyntaxException e2) {}
					}
				}
				if(r[i] == null) throw new InvalidSyntaxException("Unknown type: " + c[i] + " cannot be interpreted (key: \"" + key + "\", index: " + i + ").");
			} content.put(key, r);
			rawContent.remove(key);
			return r;
		} else return new Object[0];
	}
	
	/**
	 * Returns an empty array if there is no item set with that key.
	 * @param key The key of the item set.
	 * @return The item set as String array.
	 * @see #firstString(String)
	 * @see #asObjects(String)
	 * @throws InvalidSyntaxException If the item set contains an invalid data type.
	 * @throws java.lang.ClassCastException If not every item of this set can be casted to a String.
	 */
	public String[] asStrings(String key) {
		Object[] o = asObjects(key);
		String[] s = new String[o.length];
		for(int i = 0; i<s.length; i++) s[i] = o[i].toString();
		return s;
	}
	
	/**
	 * <b>Please note:</b> There might be a loss of data when casting e. g. a double to an integer. This method will not warn you about such a scenario.
	 * Returns an empty array if there is no item set with that key.
	 * @param key The key of the item set.
	 * @return The item set as int array.
	 * @see #firstInt(String)
	 * @see #asObjects(String)
	 * @throws InvalidSyntaxException If the item set contains an invalid data type.
	 * @throws java.lang.ClassCastException If not every item of this set can be casted to an integer.
	 */
	public int[] asInts(String key) {
		Object[] o = asObjects(key);
		int[] s = new int[o.length];
		for(int i = 0; i<s.length; i++) s[i] = o[i] instanceof Long ? (int) ((long) o[i]) : o[i] instanceof Double ? (int) ((double) o[i]) : o[i] instanceof BigDecimal ? ((BigDecimal) o[i]).intValue() : (int) o[i];
		return s;
	}
	
	/**
	 * <b>Please note:</b> There might be a loss of data when casting e. g. a double to a long. This method will not warn you about such a scenario.
	 * Returns an empty array if there is no item set with that key.
	 * @param key The key of the item set.
	 * @return The item set as long array.
	 * @see #firstLong(String)
	 * @see #asObjects(String)
	 * @throws InvalidSyntaxException If the item set contains an invalid data type.
	 * @throws java.lang.ClassCastException If not every item of this set can be casted to a long.
	 */
	public long[] asLongs(String key) {
		Object[] o = asObjects(key);
		long[] s = new long[o.length];
		for(int i = 0; i<s.length; i++) s[i] = o[i] instanceof Integer ? (long) ((int) o[i]) : o[i] instanceof Double ? (long) ((double) o[i]) : o[i] instanceof BigDecimal ? ((BigDecimal) o[i]).longValue() : (long) o[i];
		return s;
	}
	
	/**
	 * <b>Please note:</b> There might be a loss of data when casting e. g. a BigDecimal to a double. This method will not warn you about such a scenario.
	 * Returns an empty array if there is no item set with that key.
	 * @param key The key of the item set.
	 * @return The item set as double array.
	 * @see #firstDouble(String)
	 * @see #asObjects(String)
	 * @throws InvalidSyntaxException If the item set contains an invalid data type.
	 * @throws java.lang.ClassCastException If not every item of this set can be casted to a double.
	 */
	public double[] asDoubles(String key) {
		Object[] o = asObjects(key);
		double[] s = new double[o.length];
		for(int i = 0; i<s.length; i++) s[i] = o[i] instanceof BigDecimal ? ((BigDecimal) o[i]).doubleValue() : o[i] instanceof Integer ? (double) ((int) o[i]) : o[i] instanceof Long ? (double) ((long) o[i]) : (double) o[i];
		return s;
	}
	
	/**
	 * Returns an empty array if there is no item set with that key.
	 * @param key The key of the item set.
	 * @return The item set as BigDecimal array.
	 * @see #firstBigDecimal(String)
	 * @see #asObjects(String)
	 * @throws InvalidSyntaxException If the item set contains an invalid data type.
	 * @throws java.lang.ClassCastException If not every item of this set can be casted to a BigDecimal.
	 */
	public BigDecimal[] asBigDecimals(String key) {
		Object[] o = asObjects(key);
		BigDecimal[] s = new BigDecimal[o.length];
		for(int i = 0; i<s.length; i++) s[i] = o[i] instanceof Double ? BigDecimal.valueOf((double) o[i]) : o[i] instanceof Integer ? BigDecimal.valueOf((double) ((int) o[i])) : o[i] instanceof Long ? BigDecimal.valueOf((long) o[i]) : (BigDecimal) o[i];
		return s;
	}
	
	/**
	 * Returns an empty array if there is no item set with that key.
	 * @param key The key of the item set.
	 * @return The item set as boolean array.
	 * @see #firstBoolean(String)
	 * @see #asObjects(String)
	 * @throws InvalidSyntaxException If the item set contains an invalid data type.
	 * @throws java.lang.ClassCastException If not every item of this set can be casted to a boolean.
	 */
	public boolean[] asBooleans(String key) {
		Object[] o = asObjects(key);
		boolean[] s = new boolean[o.length];
		for(int i = 0; i<s.length; i++) s[i] = (boolean) o[i];
		return s;
	}
	
	/**
	 * Returns an empty array if there is no item set with that key.
	 * @param key The key of the item set.
	 * @return The item set as String array.
	 * @see #firstString(String)
	 * @see #asObjects(String)
	 * @throws InvalidSyntaxException If the item set contains an invalid data type.
	 * @throws java.lang.ClassCastException If not every item of this set can be casted to a Date.
	 */
	public Date[] asDates(String key) {
		Object[] o = asObjects(key);
		Date[] s = new Date[o.length];
		for(int i = 0; i<s.length; i++) s[i] = (Date) o[i];
		return s;
	}
	
	/**
	 * Returns an empty array if there is no item set with that key.
	 * @param key The key of the item set.
	 * @return The item set as Collection array.
	 * @see #firstCollection(String)
	 * @see #asObjects(String)
	 * @throws InvalidSyntaxException If the item set contains an invalid data type.
	 * @throws java.lang.ClassCastException If not every item of this set can be casted to a Collection.
	 */
	public Collection[] asCollections(String key) {
		Object[] o = asObjects(key);
		Collection[] s = new Collection[o.length];
		for(int i = 0; i<s.length; i++) s[i] = (Collection) o[i];
		return s;
	}
	
	/**
	 * This method returns the first item of the item set with the given key and therefore the same as:<br>
	 * {@code asObjects(key)[0];}
	 * @param key The key of the item set.
	 * @see #asObjects(String)
	 * @return The first item of the item set as object.
	 * @throws InvalidSyntaxException If the item set contains an invalid data type.
	 * @throws java.lang.ArrayIndexOutOfBoundsException If there is no item set with that key.
	 */
	public Object firstObject(String key) {
		return asObjects(key)[0];
	}
	
	/**
	 * Returns {@code null} if there is no item set with that key.
	 * @param key The key of the item set.
	 * @see #asStrings(String)
	 * @return The first item of the item set.
	 * @throws InvalidSyntaxException If the item set contains an invalid data type.
	 * @throws java.lang.ClassCastException If the first item cannot be casted to a String.
	 */
	public String firstString(String key) {
		return has(key) ? (String) asObjects(key)[0] : null;
	}
	
	/**
	 * @param key The key of the item set.
	 * @see #asInts(String)
	 * @return The first item of the item set.
	 * @throws InvalidSyntaxException If the item set contains an invalid data type.
	 * @throws java.lang.ClassCastException If the first item cannot be casted to an Integer.
	 * @throws java.lang.ArrayIndexOutOfBoundsException If there is no item set with that key.
	 */
	public int firstInt(String key) {
		return (int) asObjects(key)[0];
	}
	
	/**
	 * @param key The key of the item set.
	 * @see #asLongs(String)
	 * @return The first item of the item set.
	 * @throws InvalidSyntaxException If the item set contains an invalid data type.
	 * @throws java.lang.ClassCastException If the first item cannot be casted to a long.
	 * @throws java.lang.ArrayIndexOutOfBoundsException If there is no item set with that key.
	 */
	public long firstLong(String key) {
		return (long) asObjects(key)[0];
	}
	
	/**
	 * @param key The key of the item set.
	 * @see #asDoubles(String)
	 * @return The first item of the item set.
	 * @throws InvalidSyntaxException If the item set contains an invalid data type.
	 * @throws java.lang.ClassCastException If the first item cannot be casted to a double.
	 * @throws java.lang.ArrayIndexOutOfBoundsException If there is no item set with that key.
	 */
	public double firstDouble(String key) {
		return (double) asObjects(key)[0];
	}
	
	/**
	 * Returns {@code null} if there is no item set with that key.
	 * @param key The key of the item set.
	 * @see #asBigDecimals(String)
	 * @return The first item of the item set.
	 * @throws InvalidSyntaxException If the item set contains an invalid data type.
	 * @throws java.lang.ClassCastException If the first item cannot be casted to a BigDecimal.
	 */
	public BigDecimal firstBigDecimal(String key) {
		return has(key) ? (BigDecimal) asObjects(key)[0] : null;
	}
	
	/**
	 * @param key The key of the item set.
	 * @see #asBooleans(String)
	 * @return The first item of the item set.
	 * @throws InvalidSyntaxException If the item set contains an invalid data type.
	 * @throws java.lang.ClassCastException If the first item cannot be casted to a boolean.
	 * @throws java.lang.ArrayIndexOutOfBoundsException If there is no item set with that key.
	 */
	public boolean firstBoolean(String key) {
		return (boolean) asObjects(key)[0];
	}
	
	/**
	 * Returns {@code null} if there is no item set with that key.
	 * @param key The key of the item set.
	 * @see #asDates(String)
	 * @return The first item of the item set.
	 * @throws InvalidSyntaxException If the item set contains an invalid data type.
	 * @throws java.lang.ClassCastException If the first item cannot be casted to a Date.
	 */
	public Date firstDate(String key) {
		return has(key) ? (Date) asObjects(key)[0] : null;
	}
	
	/**
	 * Returns {@code null} if there is no item set with that key.
	 * @param key The key of the item set.
	 * @see #asCollections(String)
	 * @return The first item of the item set.
	 * @throws InvalidSyntaxException If the item set contains an invalid data type.
	 * @throws java.lang.ClassCastException If the first item cannot be casted to a Collection.
	 */
	public Collection firstCollection(String key) {
		return has(key) ? (Collection) asObjects(key)[0] : null;
	}
	
	/**
	 * @param key The key.
	 * @return Whether the Collection contains an item set with the given key.
	 */
	public boolean has(String key) {
		return rawContent.containsKey(key) || content.containsKey(key);
	}
	
	/**
	 * @return All keys of the Collection.
	 */
	public String[] keys() {
		Set<String> keySet1 = rawContent.keySet(),
				keySet2 = content.keySet();
		String[] keys = new String[keySet1.size()+keySet2.size()];
		int i = 0;
		for(String s : keySet1) {
			keys[i] = s;
			i++;
		}
		for(String s : keySet2) {
			keys[i] = s;
			i++;
		} return keys;
	}
	
	/**
	 * Adds or overwrites an item set to the Collection.
	 * @param key The key of the item set.
	 * @param values The values of the items.
	 * @return This Collection.
	 * @see #put(String, Object)
	 * @see #append(String, Object)
	 * @see #remove(String)
	 * @see #remove(String, int)
	 * @throws InvalidSyntaxException If either the data type of an item or the key is invalid.
	 * @throws NullPointerException If the key or an item value is null.
	 */
	public Collection put(String key, Object[] values) {
		if(values == null) throw new NullPointerException("An item value cannot be null.");
		for(Object v : values) {
			if(v == null) throw new NullPointerException("An item value cannot be null.");;
		}
		if(!Parser.isValidKey(key)) throw new InvalidSyntaxException("Invalid key: \"" + key + "\".");
		rawContent.remove(key);
		content.put(key, values);
		return this;
	}
	
	/**
	 * Adds or overwrites an item set with a new item set with the given key and the value.<br>
	 * @param key The key of the item set.
	 * @param value The item value.
	 * @return This Collection.
	 * @see #put(String, Object[])
	 * @see #append(String, Object)
	 * @see #remove(String)
	 * @see #remove(String, int)
	 * @throws InvalidSyntaxException If either the data type of the item or the key is invalid.
	 * @throws NullPointerException If the key or an item value is null.
	 */
	public Collection put(String key, Object value) {
		remove(key);
		append(key, value);
		return this;
	}
	
	/**
	 * Appends an item to the end of the item set with the given key.<br>
	 * If there is no item set with the given key, a new one will be created.
	 * @param key The key of the item set.
	 * @param value The value of the item.
	 * @return This Collection.
	 * @see #put(String, Object[])
	 * @see #put(String, Object)
	 * @see #remove(String)
	 * @see #remove(String, int)
	 * @throws InvalidSyntaxException If either the data type of the value or the key is invalid.
	 * @throws NullPointerException If the key or the value is null.
	 */
	public Collection append(String key, Object value) {
		if(value == null) throw new NullPointerException("An item value cannot be null.");
		if(rawContent.containsKey(key)) asObjects(key);
		else if(!Parser.isValidKey(key)) throw new InvalidSyntaxException("Invalid key: \"" + key + "\".");
		if(!content.containsKey(key)) content.put(key, new Object[] {value});
		else {
			Object[] o = content.get(key),
					n = new Object[o.length+1];
			for(int i = 0; i<o.length; i++) n[i] = o[i];
			n[n.length-1] = value;
		} return this;
	}
	
	/**
	 * Removes the item set with the given key.<br>
	 * If the key does not exist, this will be ignored.
	 * @param key The key.
	 * @return This Collection.
	 * @see #put(String, Object[])
	 * @see #append(String, Object)
	 * @see #remove(String, int)
	 */
	public Collection remove(String key) {
		rawContent.remove(key);
		content.remove(key);
		return this;
	}
	
	/**
	 * Remove a single item from the item set with the given key.<br>
	 * If the key does not exist, this will be ignored. 
	 * @param key The key.
	 * @param index The index of the item.
	 * @return This Collection.
	 * @throws java.lang.ArrayIndexOutOfBoundsException If the index is larger than the amount of items within the item set.
	 */
	public Collection remove(String key, int index) {
		if(rawContent.containsKey(key)) asObjects(key);
		if(content.containsKey(key)) {
			Object[] o = content.get(key),
					n = new Object[o.length-1];
			if(n.length > 0) {
				for(int i = 0; i<o.length; i++) {
					if(i != index) n[i > index ? i-1 : i] = o[i];
				} content.put(key, n);
			} else content.remove(key);
		} return this;
	}
	
	/**
	 * Converts the Collection into text notation.
	 * @see #toString(int)
	 * @return The Collection as text notation.
	 * @throws InvalidSyntaxException If a data type is invalid.
	 * @throws InvalidObjectException If an object cannot be translated.
	 */
	@Override
	public String toString() {
		return toString(0);
	}
	
	/**
	 * Converts the Collection into text notation.
	 * @param whitespace The amount of spaces after line breaks.
	 * @see #toString()
	 * @return The Collection as text notation.
	 * @throws InvalidSyntaxException If a data type is invalid.
	 * @throws InvalidObjectException If an object cannot be translated.
	 */
	public String toString(int whitespace) {
		String r = "",
				w = whitespace > 0 ? "\n" : "";
		for(int i = 0; i<whitespace; i++) w += " ";
		
		for(String k : keys()) {
			String t = k + ": ";
			for(Object o : asObjects(k)) {
				if(o instanceof String) t += "\"" + Parser.prepareString(o.toString()) + "\"";
				else if(o instanceof Integer) t += (int) o;
				else if(o instanceof Long) t += (long) o;
				else if(o instanceof Double) t += (double) o;
				else if(o instanceof BigDecimal) t += ((BigDecimal) o).toString();
				else if(o instanceof Boolean) t += ((boolean) o) ? "true" : "false";
				else if(o instanceof Date) t += dateFormat.format((Date) o);
				else if(o instanceof Collection) t += ((Collection) o).toString(whitespace).replace("\n", whitespace > 0 ? w : "\n");
				else if(o instanceof Serializable) {
					try {
						t += "i[\"" + Parser.prepareString(Parser.toString((Serializable) o)) + "\"]";
					} catch(IOException e) {
						throw new InvalidObjectException("Object cannot be translated (key: \"" + k + "\").");
					}
				} else throw new InvalidSyntaxException("Unknown type: " + o + " cannot be interpreted (key: \"" + k + "\").");
				t += ", ";
			} r += t.substring(0, t.length()-2) + ";" + w;
		} return "<" + w + (r.length() > 0 ? r.substring(0, r.length()-whitespace) : r) + ">";
	}

}
