import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        Go2Web go2Web = new Go2Web();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Enter command (type 'exit' to quit): ");
            String command = scanner.nextLine();

            if (command.equals("exit")) {
                break;
            }

            String[] commandArgs = command.split("\\s+");
            executeCommand(go2Web, commandArgs);
        }

        scanner.close();
    }

    private static void executeCommand(Go2Web go2Web, String[] args) {
        if (args.length == 0 || args[0].equals("-h")) {
            printHelp();
        } else if (args[0].equals("-u")) {
            if (args.length != 2) {
                System.err.println("Usage: go2web -u <URL>");
                return;
            }
            String url = args[1];
            go2Web.makeHTTPRequest(url);
        } else if (args[0].equals("-s")) {
            if (args.length != 2) {
                System.err.println("Usage: go2web -s <search-term>");
                return;
            }
            String searchTerm = args[1];
            go2Web.searchOnWeb(searchTerm);
        } else {
            System.err.println("Invalid option. Use 'go2web -h' for help.");
        }
    }

    private static void printHelp() {
        System.out.println("Usage:");
        System.out.println("go2web -u <URL>         # make an HTTP request to the specified URL and print the response");
        System.out.println("go2web -s <search-term> # make an HTTP request to search the term using your favorite search engine and print top 10 results");
        System.out.println("go2web -h               # show this help");
    }
}

class Go2Web {

    public void makeHTTPRequest(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                reader.close();
                printSearchResults(responseBuilder.toString());
            } else {
                System.err.println("Failed to make HTTP request. Response code: " + responseCode);
            }
        } catch (IOException e) {
            System.err.println("Error making HTTP request: " + e.getMessage());
        }
    }

    public void searchOnWeb(String searchTerm) {
        try {
            String encodedSearchTerm = URLEncoder.encode(searchTerm, "UTF-8");
            String searchUrl = "https://www.google.com/search?q=" + encodedSearchTerm;
            makeHTTPRequest(searchUrl);
        } catch (IOException e) {
            System.err.println("Error searching on the web: " + e.getMessage());
        }
    }

    private static void printSearchResults(String response) {
        Pattern pattern = Pattern.compile("<a href=\"/url\\?q=(https?://[^\"]+)&amp;");
        Matcher matcher = pattern.matcher(response);

        int count = 0;
        while (matcher.find() && count < 10) {
            String link = matcher.group(1);
            link = cleanLink(link);
            if (isValidLink(link)) {
                System.out.println(++count + ". " + link);
            }
        }
    }

    private static String cleanLink(String link) {
        // Remove any trailing query parameters or fragments
        int endIndex = link.indexOf('&');
        if (endIndex != -1) {
            link = link.substring(0, endIndex);
        }
        return link;
    }

    private static boolean isValidLink(String link) {
        // Check if the link is not a Google internal link and is a valid web page link
        return link.startsWith("https://") && !link.contains("google.com");
    }
}
