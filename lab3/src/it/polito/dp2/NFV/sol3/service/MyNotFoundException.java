package it.polito.dp2.NFV.sol3.service;

public class MyNotFoundException extends Exception {
	private static final long serialVersionUID = 1L;

	public MyNotFoundException() {
	}

	public MyNotFoundException(String message) {
		super(message);
	}

	public MyNotFoundException(Throwable cause) {
		super(cause);
	}

	public MyNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public MyNotFoundException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
