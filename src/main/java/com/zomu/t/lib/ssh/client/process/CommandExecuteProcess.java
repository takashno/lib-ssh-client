package com.zomu.t.lib.ssh.client.process;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.zomu.t.lib.ssh.client.bean.Command;
import com.zomu.t.lib.ssh.client.bean.ConnectionInfo;
import com.zomu.t.lib.ssh.client.exception.CommandExecuteFailureException;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * コマンド実行プロセス
 * 
 * @author takashno
 */
@Slf4j
public class CommandExecuteProcess {

	/** 接続情報 */
	private ConnectionInfo connectionInfo;

	/** コマンドリスト */
	private List<Command> commandList;

	/** 対象コマンドインデックス */
	private int targetCommandListIndex = -1;

	/** 入力ストリーム */
	private InputStream is;

	/** 出力ストリーム */
	private OutputStream os;

	/** コマンド実行可能状態フラグ */
	private boolean commandExecuteReady = true;

	/**
	 * コンストラクタ.
	 * 
	 * @param commandList
	 * @param is
	 * @param os
	 */
	public CommandExecuteProcess(ConnectionInfo connectionInfo, List<Command> commandList, InputStream is,
			OutputStream os) {
		this.commandList = commandList;
		this.is = is;
		this.os = os;
	}

	/**
	 * 実行処理.
	 */
	synchronized public void execute() {

		if (!this.commandExecuteReady) {
			try {
				wait();
			} catch (InterruptedException e) {
				throw new CommandExecuteFailureException("failure command execute...", e);
			}
		}

		// コマンド実行不可状態とする
		this.commandExecuteReady = false;

		// インデックスをインクリメント
		this.targetCommandListIndex++;

		// コマンド取得
		Command command = commandList.get(targetCommandListIndex);

		log.debug("--- execute command ---");
		log.debug("Command         : " + command.getCommand());
		log.debug("WaitFor         : " + command.getWaitFor());
		log.debug("Timeout         : " + command.getTimeout());
		log.debug("Encoding        : " + command.getEncoding());
		log.debug("TreatedAsErrors : " + command.getTreatedAsErrors());

		try {
			if (StringUtils.isEmpty(command.getCommand())) {
				// コマンド書き込み
				os.write((command.getCommand() + connectionInfo.getLineSeparator()).getBytes(command.getEncoding()));
				os.flush();

				// 待機
				long start = System.currentTimeMillis();

				while (true) {

					// 終了時間
					long end = System.currentTimeMillis();

					if (end - start > command.getTimeout()) {
						throw new CommandExecuteFailureException(
								"command execute timeout... not found : " + command.getWaitFor());
					}

					// 0.5秒毎にポーリング
					Thread.sleep(500L);

					if (read(command)) {
						break;
					}
				}
			}
		} catch (Exception e) {
			throw new CommandExecuteFailureException("failure command execute... command : " + command.getCommand(), e);
		}

		// waitを解除
		notifyAll();

		// 実行可能状態とする
		this.commandExecuteReady = true;
	}

	/**
	 * 結果を読み込み.
	 * 
	 * @param command
	 * @return
	 */
	private boolean read(Command command) {

		try {

			int len = 0;
			StringBuilder sb = new StringBuilder();

			while ((len = is.available()) > 0) {
				int n = 1024;
				byte[] b = new byte[n];
				is.read(b, 0, len);
				sb.append(new String(b, command.getEncoding()));
			}

			String result = sb.toString();
			System.out.println(result);

			if (CollectionUtils.isNotEmpty(command.getTreatedAsErrors())) {
				for (String errorStr : command.getTreatedAsErrors()) {
					if (StringUtils.contains(result, errorStr)) {
						throw new CommandExecuteFailureException(
								"failure command execute... found error target : " + errorStr);
					}
				}
			}

			// 待機文字列が存在すれば、正常にコマンドが実行できたとみなす
			if (StringUtils.isNotEmpty(command.getWaitFor())) {
				return StringUtils.contains(result, command.getWaitFor());
			} else {
				return true;
			}

		} catch (IOException e) {
			throw new RuntimeException("failur command result read...", e);
		}

	}

	/**
	 * コマンド実行可能状態か判断します.
	 * 
	 * @return
	 */
	public boolean isCommandExecuteReady() {
		return commandExecuteReady;
	}

	/**
	 * 全てのコマンドの実行が終わったか判断します.
	 * 
	 * @return
	 */
	public boolean isFullyExecuted() {
		return targetCommandListIndex == commandList.size() - 1;
	}
}
