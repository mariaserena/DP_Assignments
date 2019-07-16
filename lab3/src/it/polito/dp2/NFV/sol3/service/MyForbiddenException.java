package it.polito.dp2.NFV.sol3.service;

public class MyForbiddenException extends Exception {
	private static final long serialVersionUID = 1L;

	public MyForbiddenException() {
	}

	public MyForbiddenException(String message) {
		super(message);
	}

	public MyForbiddenException(Throwable cause) {
		super(cause);
	}

	public MyForbiddenException(String message, Throwable cause) {
		super(message, cause);
	}

	public MyForbiddenException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
