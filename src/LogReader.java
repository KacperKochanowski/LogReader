import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;


public class LogReader {

    final private static String timestampRgx = "(?<timestamp>\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3})";
    final private static String levelRgx = "(?<level>INFO|ERROR|WARN|TRACE|DEBUG|FATAL)";
    final static String classRgx = "\\[(?<class>[^]]+)]";

    public static void main(String[] args) throws IOException {

        try (Stream<Path> paths = Files.walk(Paths.get("D:\\logs"))) {
            List<File> sortedFiles = paths
                    .filter(Files::isRegularFile)
                    .sorted((f1, f2) -> {
                        try {
                            return Files.readAttributes(f2, BasicFileAttributes.class).lastModifiedTime()
                                    .compareTo(Files.readAttributes(f1, BasicFileAttributes.class).lastModifiedTime());
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .map(Path::toFile)
                    .toList();

            for (File file : sortedFiles) {
                long startTime = System.nanoTime();
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                 /*
                I am not sure do I understood correctly sentence to show in console time, which elapsed for reading the file.
                Another way would be to uncomment the two lines below and delete lines 67 - 68
                long endTime = System.nanoTime();
                long duration = (endTime - startTime);
                */
                String strLine;
                List<String> dataMatches = new ArrayList<>();
                List<String> lvlMatches = new ArrayList<>();
                Map<String, Integer> mapLvl = new HashMap<>();
                Set<String> librarySet = new HashSet<>();
                while ((strLine = bufferedReader.readLine()) != null) {
                    Matcher m = Pattern.compile(timestampRgx).matcher(strLine);
                    Matcher lvl = Pattern.compile(levelRgx).matcher(strLine);
                    Matcher lib = Pattern.compile(classRgx).matcher(strLine);
                    while (m.find()) {
                        dataMatches.add(m.group());
                    }
                    while (lvl.find()) {
                        lvlMatches.add(lvl.group());
                    }
                    while (lib.find()) {
                        librarySet.add(lib.group());
                    }
                }
                bufferedReader.close();

                long endTime = System.nanoTime();
                long duration = (endTime - startTime);

                timeConverter(duration);

                List<String> sortedMatches = dataMatches.stream().sorted().toList(); // it's sorted because first log from "server.log" is the newest from whole file and due to this case, it has to sorted ;)

                String dataOfLastLog = sortedMatches.get(sortedMatches.size() - 1);

                String dataOfFirstLog = sortedMatches.get(0);

                differenceBetweenLastAndFirstLog(dataOfFirstLog, dataOfLastLog);

                logLevelsWithOccurrences(lvlMatches, mapLvl);

                ratioOfAtLeastErrorLogsToAll(mapLvl);

                distinctTypesOfLibrariesInLogs(librarySet);
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void distinctTypesOfLibrariesInLogs(Set<String> librarySet) {
        // Same as with time elapsed for reading I'm not sure do I understood correctly sentence to show number of unique libraries in log.
        System.out.println("\nNumber of unique libraries: " + librarySet.size());
        System.out.println("\nLibraries in logs: " + librarySet);
    }

    private static void logLevelsWithOccurrences(List<String> lvlMatches, Map<String, Integer> mapLvl) {
        for (String lvl : lvlMatches) {
            if (!mapLvl.containsKey(lvl)) {
                mapLvl.put(lvl, 1);
            } else {
                mapLvl.put(lvl, mapLvl.get(lvl) + 1);
            }
        }
        System.out.println("\nTypes of logs: " + mapLvl);
    }

    private static void ratioOfAtLeastErrorLogsToAll(Map<String, Integer> mapLvl) {
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
        System.out.printf("\nThe share of logs with a severity of 'ERROR' or higher compared to all logs is around: %.2f\n", ratio);
    }

    private static void timeConverter(long duration) {
        if (duration <= 1_000_000_000) {
            System.out.println("\nThe file was read in: " + duration + " nanoseconds.\n");
        } else if (duration < 60_000_000_000L) {
            long timeConvertedIntoSeconds = TimeUnit.SECONDS.convert(duration, TimeUnit.NANOSECONDS);
            System.out.println("\nThe file was read in: " + timeConvertedIntoSeconds + " seconds.\n");
        } else if (duration > 60_000_000_000L) {
            long timeConvertedIntoMinutes = TimeUnit.MINUTES.convert(duration, TimeUnit.NANOSECONDS);
            System.out.println("\nThe file was read in: " + timeConvertedIntoMinutes + " minutes.");
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

            System.out.printf("Difference between first and last log is: %d years, %d days, %d hours, %d minutes, %d seconds.\n",
                    differenceInYears, differenceInDays, differenceInHours, differenceInMinutes, differenceInSeconds);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}