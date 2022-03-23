import org.w3c.dom.ls.LSOutput;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class LogReader {

    final private static String timestampRgx = "(?<timestamp>\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3})";

    public static void main(String[] args) throws IOException {

        try (FileInputStream fStream = new FileInputStream("server.log")) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fStream));
            String strLine;
            List<String> matches = new ArrayList<>();
            long startTime = System.nanoTime();
            while ((strLine = bufferedReader.readLine()) != null) {
                Matcher m = Pattern.compile(timestampRgx).matcher(strLine);
                while (m.find()) {
                    matches.add(m.group());
                }
                System.out.println(strLine);
            }

            long endTime = System.nanoTime();

            long duration = (endTime - startTime);

            timeConverter(duration);

            List <String> sortedMatches = matches.stream().sorted().toList();

            String dataOfLastLog = sortedMatches.get(sortedMatches.size() - 1);
//            System.out.println(dataOfLastLog);
            String dataOfFirstLog = sortedMatches.get(0);
//            System.out.println(dataOfFirstLog);
            differenceBetweenLastAndFirstLog(dataOfFirstLog,dataOfLastLog);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }


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
