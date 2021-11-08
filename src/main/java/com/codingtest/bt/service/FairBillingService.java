package com.codingtest.bt.service;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codingtest.bt.domain.LogData;
import com.codingtest.bt.domain.Session;
import com.codingtest.bt.domain.UsageData;
import com.codingtest.bt.domain.UserSession;
import com.codingtest.bt.util.ApplicationConstants;
import com.codingtest.bt.validation.InputValidator;

@Service
public class FairBillingService {
	@Autowired
	InputValidator inputValidator;

	static String firstStartTime = "";
	static String lastEndTime = "";

	// String filename = "C://Users//Megha//Desktop//BTCoding//server.log";

	public List<LogData> readLogFile(String filePath) throws IOException {
		/*
		 * Read the log file line by line, split into array of strings, validate and
		 * save as List of objects
		 */
		List<LogData> logData = new ArrayList<>();
		Files.lines(Paths.get(filePath)).filter(l -> !l.trim().isEmpty()).map(s -> s.split("\\s+")).forEach(arr -> {
			if (arr.length == 3 && inputValidator.isAlphaNumeric(arr[1]) && inputValidator.isTimeStampValid(arr[0])
					&& inputValidator.isValidSessionMarker(arr[2])) {
				LogData log = new LogData(arr[0], arr[1], arr[2]);
				logData.add(log);
			}
		});
		return logData;
	}

	public List<LogData> validateLogData(List<LogData> logData) throws IOException {
		/*
		 * Read the log file line by line, split into array of strings, validate and
		 * save as List of objects
		 */
		List<LogData> validLogData = new ArrayList<>();
		/* Extract and save the default start time and default end time */
		firstStartTime = logData.stream().findFirst().get().getTimestamp();
		lastEndTime = logData.stream().reduce((first, second) -> second).get().getTimestamp();

		/*
		 * Checking if all the timestamps are in between the first timestamp and last
		 * timestamp to avoid negative duration
		 */
		logData.stream().forEach(x -> {
			if (LocalTime.parse(x.getTimestamp()).isAfter(LocalTime.parse(firstStartTime))
					&& LocalTime.parse(x.getTimestamp()).isBefore(LocalTime.parse(lastEndTime))) {
				validLogData.add(x);
			}
		});
		return validLogData;
	}

	public boolean getUsageReport(String filePath) {
		try {

			List<LogData> logData = readLogFile(filePath);
			if (!logData.isEmpty()) {
				List<LogData> validLogData = validateLogData(logData);
				if (!validLogData.isEmpty()) {
					/* Get the list of distinct users */
					List<String> users = validLogData.stream().map(p -> p.getUsername()).distinct()
							.collect(Collectors.toList());

					/* Initialize the final usage report list of objects */
					List<UsageData> usageDataResult = users.stream().map(a -> new UsageData(a, 0, 0L))
							.collect(Collectors.toList());

					/* Calculating usage */
					usageDataResult.stream().forEach(m -> {
						/* Sort the input data by username,timestamp */
						Comparator<LogData> compareByName = Comparator.comparing(LogData::getUsername)
								.thenComparing(LogData::getTimestamp);
						List<LogData> sortedList = validLogData.stream().sorted(compareByName)
								.collect(Collectors.toList());

						List<UserSession> result = new ArrayList<>();

						/*
						 * Filter the sorted list and process the data to create a list of user session
						 * objects
						 */
						sortedList.stream()
								.filter(x -> x.getUsername().equals(m.getUsername())
										&& (x.getSessionStatus().equals(ApplicationConstants.START))) // filter based on
																										// username and
								// session status = "Start"
								.forEach(x -> {// create an object for each user session and increment the no.of
												// sessions
									UserSession udata = new UserSession(m.getUsername(),
											new Session(m.getNoOfSessions() + 1, false, x.getTimestamp(), "", 0L));
									m.setNoOfSessions(m.getNoOfSessions() + 1);
									result.add(udata);
								});
						sortedList.stream().filter(x -> x.getUsername().equals(m.getUsername())
								&& (x.getSessionStatus().equals(ApplicationConstants.END))).forEach(x -> {
									result.stream().filter(
											l -> l.getUsername().equals(x.getUsername()) && !l.getSession().isPaired())
											.findFirst().map(u -> {// pairing the end
																	// time for the
																	// user sessions
																	// created
																	// above
												int i = LocalTime.parse(u.getSession().getStartTime())
														.compareTo(LocalTime.parse(x.getTimestamp()));
												if (i == -1) {
													u.getSession().setEndTime(x.getTimestamp());
													u.getSession().setPaired(true);
												} else {// handling dangling sessions without a "Start"
													UserSession udata = new UserSession(m.getUsername(),
															new Session(m.getNoOfSessions() + 1, true, firstStartTime,
																	x.getTimestamp(), 0L));
													m.setNoOfSessions(m.getNoOfSessions() + 1);
													result.add(udata);
												}
												return u;
											}).orElseGet(() -> {// handling dangling sessions without a "Start"
												UserSession udata = new UserSession(m.getUsername(),
														new Session(m.getNoOfSessions() + 1, true, firstStartTime,
																x.getTimestamp(), 0L));
												m.setNoOfSessions(m.getNoOfSessions() + 1);
												result.add(udata);
												return udata;
											});
								});
						/*
						 * handling dangling session with out 'Start' or ' End' by using the default
						 * values
						 */
						result.stream().filter(p -> !p.getSession().isPaired()).forEach(x -> {
							if (x.getSession().getStartTime() == null) {
								x.getSession().setStartTime(firstStartTime);
								x.getSession().setPaired(true);
							}
							if (x.getSession().getEndTime() == null) {
								x.getSession().setEndTime(lastEndTime);
								x.getSession().setPaired(true);
							}
						});
						
						Map<String, Long> sum = calculateDuration(result);
						m.setTotalTime(sum.get(m.getUsername()));

						System.out.println(m.getUsername() + "  " + m.getNoOfSessions() + "  " + m.getTotalTime());
					});
					
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true; 
	}
public Map<String,Long> calculateDuration(List<UserSession> result) {
	/* Calculate the duration of the sessions in SECONDS */
	result.stream().forEach(r -> {
		LocalTime startTime = LocalTime.parse(r.getSession().getStartTime());
		LocalTime endTime = LocalTime.parse(r.getSession().getEndTime());
		long duration = SECONDS.between(startTime, endTime);
		r.getSession().setDuration(duration);
	});

	/* Calculate the total duration of sessions in SECONDS for each user */
	Map<String, Long> sum = result.stream().collect(Collectors.groupingBy(UserSession::getUsername,
			Collectors.summingLong(d -> d.getSession().getDuration())));

	return sum;
}

}