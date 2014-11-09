package mst.cdtool.graphdb;

public class CypherQueryException extends Exception {

	/**
	 * 
	 */
    private static final long serialVersionUID = 3928922777072393173L;

	public CypherQueryException() {
	}

	public CypherQueryException(String message) {
		super(message);
	}

	public CypherQueryException(Throwable cause) {
		super(cause);
	}

	public CypherQueryException(String message, Throwable cause) {
		super(message, cause);
	}

	public CypherQueryException(String message, Throwable cause,
	        boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
