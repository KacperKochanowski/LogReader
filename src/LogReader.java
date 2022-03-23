import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LogReader {

    final private static String timestampRgx = "(?<timestamp>\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3})";
    final private static String levelRgx = "(?<level>INFO|ERROR|WARN|TRACE|DEBUG|FATAL)";

    public static void main(String[] args) throws IOException {

        try (FileInputStream fStream = new FileInputStream("server.log")) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fStream));
            String strLine;
            List<String> dataMatches = new ArrayList<>();
            List<String> lvlMatches = new ArrayList<>();
            Map<String, Integer> mapLvl = new HashMap<>();
            long startTime = System.nanoTime();
            while ((strLine = bufferedReader.readLine()) != null) {
                Matcher m = Pattern.compile(timestampRgx).matcher(strLine);
                Matcher lvl = Pattern.compile(levelRgx).matcher(strLine);
                while (m.find()) {
                    dataMatches.add(m.group());
                }
                while (lvl.find()) {
                    lvlMatches.add(lvl.group());
                }
                System.out.println(strLine);
            }

            long endTime = System.nanoTime();

            long duration = (endTime - startTime);

            timeConverter(duration);

            List<String> sortedMatches = dataMatches.stream().sorted().toList();

            String dataOfLastLog = sortedMatches.get(sortedMatches.size() - 1);
//            System.out.println(dataOfLastLog);
            String dataOfFirstLog = sortedMatches.get(0);
//            System.out.println(dataOfFirstLog);
            differenceBetweenLastAndFirstLog(dataOfFirstLog, dataOfLastLog);

            thrownLogSeverity(lvlMatches, mapLvl);

            ratioOfErrorLogsOrHigherToTheRest(mapLvl);


        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void thrownLogSeverity(List<String> lvlMatches, Map<String, Integer> mapLvl) {
        for (String lvl : lvlMatches) {
            if (!mapLvl.containsKey(lvl)) {
                mapLvl.put(lvl, 1);
            } else {
                mapLvl.put(lvl, mapLvl.get(lvl) + 1);
            }
        }
        System.out.println("\nTypes of logs: " + mapLvl);
    }

    private static void ratioOfErrorLogsOrHigherToTheRest(Map<String, Integer> mapLvl) {
        int logsWithSeverityErrorOrHigher = 0;
        int logsWithSeverityLessThanError = 0;
        double ratio;
        for (Map.Entry<String, Integer> lvl : mapLvl.entrySet()) {
            if (lvl.getKey().equals("ERROR") || lvl.getKey().equals("FATAL")) {
                logsWithSeverityErrorOrHigher += lvl.getValue();
            } else {
                logsWithSeverityLessThanError += lvl.getValue();
            }
        }
        ratio = (double) logsWithSeverityErrorOrHigher / (logsWithSeverityErrorOrHigher + logsWithSeverityLessThanError);
        System.out.printf("\nThe share of logs with a severity of 'ERROR' or higher compared to all logs is around: %.2f", ratio);
    }

    private static void timeConverter(long duration) {
        if (duration <= 1_000_000_000) {
            System.out.println("\nFile has been written in: " + duration + " nanoseconds.");
        } else if (duration < 60_000_000_000L) {
            long timeConvertedIntoSeconds = TimeUnit.SECONDS.convert(duration, TimeUnit.NANOSECONDS);
            System.out.println("\nFile has been written in: " + timeConvertedIntoSeconds + " seconds.");
        } else if (duration > 60_000_000_000L) {
            long timeConvertedIntoMinutes = TimeUnit.MINUTES.convert(duration, TimeUnit.NANOSECONDS);
            System.out.println("\nFile has been written in: " + timeConvertedIntoMinutes + " minutes.");
        }
    }

    private static void differenceBetweenLastAndFirstLog(String firstLog, String lastLog) {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        try {
            Date dateOfFirstLog = simpleDateFormat.parse(firstLog);
            Date dateOfLastLog = simpleDateFormat.parse(lastLog);

            long differenceInMilliseconds = dateOfLastLog.getTime() - dateOfFirstLog.getTime();
            long differenceInYears = (differenceInMilliseconds / (1000L * 60 * 60 * 24 * 365));
            long differenceInDays = (differenceInMilliseconds / (1000L * 60 * 60 * 24)) % 365;
            long differenceInHours = (differenceInMilliseconds / (1000L * 60 * 60)) % 24;
            long differenceInMinutes = (differenceInMilliseconds / (1000L * 60)) % 60;
            long differenceInSeconds = (differenceInMilliseconds / 1000L) % 60;

            System.out.printf("\nDifference between first and last log is: %d years, %d days, %d hours, %d minutes, %d seconds.",
                    differenceInYears, differenceInDays, differenceInHours, differenceInMinutes, differenceInSeconds);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}