package CCS;

/**
 * An exception for an invalid object inside of a Collection.
 * @author Mika Thein
 * @version 1.0
 * @see Collection
 * @see Parser
 * @see InvalidObjectException
 */
public class InvalidObjectException extends RuntimeException {

	private static final long serialVersionUID = 5522812500053823230L;

	public InvalidObjectException() {
		super();
	}
	
	public InvalidObjectException(String message) {
		super(message);
	}

}
