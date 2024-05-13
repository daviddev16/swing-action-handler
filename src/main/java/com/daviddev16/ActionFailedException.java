package com.daviddev16;

public class ActionFailedException extends RuntimeException {

	private static final long serialVersionUID = -5885665936213304288L;

	public ActionFailedException() {
		super();
	}

	public ActionFailedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ActionFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ActionFailedException(String message) {
		super(message);
	}

	public ActionFailedException(Throwable cause) {
		super(cause);
	}
	
}
