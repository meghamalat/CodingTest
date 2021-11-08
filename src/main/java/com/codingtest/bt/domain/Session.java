package com.codingtest.bt.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Session{
	
	private Integer sessionNo;
	
	private boolean isPaired;

	private String startTime;

	private String endTime;

	private long duration;

}