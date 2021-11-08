package com.codingtest.bt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.codingtest.bt.exception.ApplicationException;
import com.codingtest.bt.service.FairBillingService;
import com.codingtest.bt.util.ApplicationConstants;
import com.codingtest.bt.validation.InputValidator;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class BtApplication implements CommandLineRunner {

	@Autowired
	FairBillingService fairBillingService;
	
	@Autowired
	InputValidator inputValidator;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BtApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(BtApplication.class, args);
		LOGGER.info("Welcome to BT Fair Billing Application");
	}

	@Override
	public void run(String... args) {
		try {
		if (inputValidator.validateCommandLineArgs(args)){
			boolean result =fairBillingService.getUsageReport(args[0]);
		}
		else {
			throw new IllegalArgumentException();
		}
		}catch(Exception e) {
			throw new ApplicationException(ApplicationConstants.ARGS_ERROR);
		}
	}

}
