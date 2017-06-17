package com.zomu.t.lib.ssh.client.exception;

public class CommandExecuteFailureException extends RuntimeException {

	/** デフォルトシリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	public CommandExecuteFailureException(String msg) {
		super(msg);
	}

	public CommandExecuteFailureException(String msg, Throwable t) {
		super(msg, t);
	}

}
