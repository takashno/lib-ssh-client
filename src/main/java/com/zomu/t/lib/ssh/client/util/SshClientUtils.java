package com.zomu.t.lib.ssh.client.util;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.util.io.NoCloseInputStream;
import org.apache.sshd.common.util.io.NoCloseOutputStream;

import com.zomu.t.lib.ssh.client.bean.ConnectionInfo;
import com.zomu.t.lib.ssh.client.bean.SshClientModel;
import com.zomu.t.lib.ssh.client.exception.SshClientException;
import com.zomu.t.lib.ssh.client.executor.CommandExecutor;
import com.zomu.t.lib.ssh.client.process.CommandExecuteProcess;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author takashimanozomu
 *
 */
@Slf4j
public class SshClientUtils {

	public static void execute(SshClientModel model) {

		try (SshClient client = SshClient.setUpDefaultClient()) {

			client.start();

			ConnectionInfo connInfo = model.getConnectionInfo();

			try (ClientSession session = client.connect(connInfo.getUser(), connInfo.getHost(), connInfo.getPort())
					.verify(7L, TimeUnit.SECONDS).getSession()) {

				session.addPasswordIdentity(connInfo.getPassword());
				AuthFuture authFuture = session.auth();
				authFuture.verify(30L, TimeUnit.SECONDS);

				try (ClientChannel channel = session.createChannel(ClientChannel.CHANNEL_SHELL)) {

					try (PipedOutputStream pos = new PipedOutputStream();
							PipedInputStream pis = new PipedInputStream(pos);
							PipedOutputStream resultPos = new PipedOutputStream();
							PipedInputStream resultPis = new PipedInputStream(resultPos)) {

						ExecutorService exec = Executors.newSingleThreadExecutor();

						// コマンド実行プロセスを構築
						CommandExecuteProcess process = new CommandExecuteProcess(connInfo, model.getCommandList(),
								resultPis, pos);

						CommandExecutor executor = new CommandExecutor(process);

						channel.setIn(new NoCloseInputStream(pis));
						channel.setOut(new NoCloseOutputStream(resultPos));
						channel.setErr(new NoCloseOutputStream(resultPos));
						channel.open();

						Future<Integer> f = exec.submit(executor);
						try {
							Integer threadResult = f.get();
							log.debug("thread result : " + threadResult);
						} catch (ExecutionException | InterruptedException e) {
							log.debug("thread error end....");
							throw new SshClientException(e);
						} finally {
							exec.shutdown();
						}
						log.debug("thread normal end.");

						channel.waitFor(Arrays.asList(new ClientChannelEvent[] { ClientChannelEvent.CLOSED }), 0L);
					}

				} finally {
					session.close(false);
				}

			} finally {
				client.stop();
			}

		} catch (IOException e) {
			throw new SshClientException(e);
		}

	}

}
