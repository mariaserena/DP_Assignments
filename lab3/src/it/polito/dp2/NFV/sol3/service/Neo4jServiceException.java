package it.polito.dp2.NFV.sol3.service;

public class Neo4jServiceException  extends Exception {

	/**
	 * the class of exception for the communication between the NvfDeployer service and the Neo4j remote DB
	 */
	private static final long serialVersionUID = 1L;

	public Neo4jServiceException() {
	}

	public Neo4jServiceException(String message) {
		super(message);
	}

	public Neo4jServiceException(Throwable cause) {
		super(cause);
	}

	public Neo4jServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public Neo4jServiceException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}