package com.zomu.t.lib.ssh.client.handler;

import java.lang.Thread.UncaughtExceptionHandler;

import com.zomu.t.lib.ssh.client.exception.SshClientException;

public class SshClientExceptionHandler implements UncaughtExceptionHandler {

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		throw new SshClientException(e);
	}

}
