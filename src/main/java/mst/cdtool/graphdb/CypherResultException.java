package mst.cdtool.graphdb;


/**
 * This exception is thrown if the result of cypher query 
 * contains error messages.
 * 
 * @author martin
 *
 */
public class CypherResultException extends CypherQueryException {

	/**
	 * 
	 */
    private static final long serialVersionUID = 3660879025299739073L;

	public CypherResultException() {
	}

	public CypherResultException(String message) {
		super(message);
	}

	public CypherResultException(Throwable cause) {
		super(cause);
	}

	public CypherResultException(String message, Throwable cause) {
		super(message, cause);
	}

	public CypherResultException(String message, Throwable cause, boolean enableSuppression,
	        boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
