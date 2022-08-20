package CCS;

/**
 * An exception for an invalid syntax of a Collection.
 * @author Mika Thein
 * @version 1.0
 * @see Collection
 * @see Parser
 * @see InvalidObjectException
 */
public class InvalidSyntaxException extends RuntimeException {

	private static final long serialVersionUID = -4765435180758874673L;

	public InvalidSyntaxException() {
		super();
	}
	
	public InvalidSyntaxException(String message) {
		super(message);
	}

}
