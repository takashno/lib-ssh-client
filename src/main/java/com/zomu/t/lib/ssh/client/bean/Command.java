package com.zomu.t.lib.ssh.client.bean;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * コマンドクラス.
 * 
 * @author takashno
 */
@Data
@AllArgsConstructor
@Builder
public class Command {

	/** コマンド */
	@NonNull
	private String command;

	/** 待機文字列 */
	@NonNull
	private String waitFor;

	/** 文字コード */
	private String encoding;

	/** タイムアウト値 */
	private long timeout;

	/** エラーとして扱う文言 */
	private List<String> treatedAsErrors;

}
