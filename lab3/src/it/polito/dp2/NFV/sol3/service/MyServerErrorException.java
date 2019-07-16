package it.polito.dp2.NFV.sol3.service;

public class MyServerErrorException extends Exception {
		private static final long serialVersionUID = 1L;

		public MyServerErrorException() {
		}

		public MyServerErrorException(String message) {
			super(message);
		}

		public MyServerErrorException(Throwable cause) {
			super(cause);
		}

		public MyServerErrorException(String message, Throwable cause) {
			super(message, cause);
		}

		public MyServerErrorException(String message, Throwable cause, boolean enableSuppression,
				boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}
}
