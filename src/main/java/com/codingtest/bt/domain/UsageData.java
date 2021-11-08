package com.codingtest.bt.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UsageData{

	private String username;

	private Integer noOfSessions;

	private Long totalTime;
	
}