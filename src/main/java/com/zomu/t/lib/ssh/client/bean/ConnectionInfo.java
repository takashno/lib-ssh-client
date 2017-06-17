package com.zomu.t.lib.ssh.client.bean;

import java.io.Serializable;

import lombok.Data;

/**
 * 接続情報
 * 
 * @author takashno
 */
@Data
public class ConnectionInfo implements Serializable {

	/** デフォルトシリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** ホスト */
	private String host;

	/** ポート */
	private int port;

	/** ユーザー */
	private String user;

	/** パスワード */
	private String password;

	/** エンコーディング */
	private String encoding;

	/** 改行 */
	private String lineSeparator;

}
