import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;


public class LogReader {


    public static void main(String[] args) throws IOException {

        try (FileInputStream fStream = new FileInputStream("server.log")) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fStream));
            String strLine;
            long startTime = System.nanoTime();
            while ((strLine = bufferedReader.readLine()) != null) {
                System.out.println(strLine);
            }
            long endTime = System.nanoTime();

            long duration = (endTime - startTime);

            timeConverter(duration);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

    }

    /*
     *** Method timeConverter can only give values as nanoseconds, seconds or minutes.
     */

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
}
