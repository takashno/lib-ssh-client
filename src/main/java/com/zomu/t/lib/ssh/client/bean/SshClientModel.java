package com.zomu.t.lib.ssh.client.bean;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * SSHクライアントモデル.
 * 
 * @author takashno
 */
@Data
public class SshClientModel implements Serializable {

	/** デフォルトシリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 接続情報 */
	private ConnectionInfo connectionInfo;

	/** コマンドリスト */
	private List<Command> commandList;

}
