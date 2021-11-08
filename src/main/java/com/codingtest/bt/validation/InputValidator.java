package com.codingtest.bt.validation;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;

import com.codingtest.bt.util.ApplicationConstants;

@Component
public class InputValidator {

	/* Validate if a string is alphanumeric */
	public boolean isAlphaNumeric(String s) {
		return s != null && s.matches(ApplicationConstants.NAME_REGEX);
	}

	/* Validate if a string is 'Start' or 'End' */
	public boolean isValidSessionMarker(String s) {
		return s != null && (s.equals(ApplicationConstants.START) || s.equals(ApplicationConstants.END));
	}

	/*
	 * Validate if a string is valid timestamp HH:MM:SS
	 * (([0-1]?[0-9])|(2[0-3])):[0-5][0-9]:[0-5][0-9]
	 */
	public boolean isTimeStampValid(String inputString) {
		return inputString.matches(ApplicationConstants.TIMESTAMP_REGEX);
	}

	public boolean validateCommandLineArgs(String[] args) {
		try {
		if (args.length != 0) {
			return isPathValid(args[0]);
		}
		else {
			throw new IllegalArgumentException();
		}
		}catch(IllegalArgumentException i) {
			return false;
		}
	}

	public static boolean isPathValid(String path) {
		try {
			Paths.get(path);
		} catch (InvalidPathException ex) {
			return false;
		}
		return true;
	}
}
