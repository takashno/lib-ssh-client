package com.zomu.t.lib.ssh.client.executor;

import java.util.concurrent.Callable;

import com.zomu.t.lib.ssh.client.exception.CommandExecuteFailureException;
import com.zomu.t.lib.ssh.client.process.CommandExecuteProcess;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author takashno
 *
 */
@Slf4j
public class CommandExecutor implements Callable<Integer> {

	/** コマンド実行プロセス */
	private CommandExecuteProcess process;

	/**
	 * コンストラクタ.
	 * 
	 * @param process
	 */
	public CommandExecutor(CommandExecuteProcess process) {
		this.process = process;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer call() throws Exception {

		while (true) {

			try {

				// 0.5秒毎にポーリング
				Thread.sleep(500L);

				if (!process.isCommandExecuteReady()) {
					continue;
				} else {
					if (!process.isFullyExecuted()) {
						// 実行すべきコマンドがある場合は実行
						process.execute();
					} else {
						// 全て実行済みであれば終了
						log.debug("all command executed.");
						break;
					}
				}

			} catch (Exception e) {
				throw new CommandExecuteFailureException("failure command execute...", e);
			}

		}

		return 0;

	}

}
