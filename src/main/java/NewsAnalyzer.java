import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class NewsAnalyzer {

    private static final int PORT = 8080;
    private static final int ANALYSIS_INTERVAL = 10000;
    private final Map<String, Integer> positiveNewsCount;
    private final Map<String, Integer> positiveNewsPriority;

    public NewsAnalyzer() {
        positiveNewsCount = new HashMap<>();
        positiveNewsPriority = new HashMap<>();
        startListening();
    }

    private void startListening() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("News Analyzer started. Listening on port " + PORT);
                while (true) {
                    Socket socket = serverSocket.accept();
                    handleNews(socket);
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }).start();
    }

    private void handleNews(Socket socket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                String headline = parts[0];
                int priority = Integer.parseInt(parts[1]);
                analyzeNews(headline, priority);
                startAnalysisThread();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void analyzeNews(String headline, int priority) {
        String[] words = headline.split(" ");
        int positiveWordCount = 0;
        for (String word : words) {
            if (
                    word.equals("up") || word.equals("rise") || word.equals("good") || word.equals("success") || word.equals("high")
            ) {
                positiveWordCount++;
            }
        }
        if (positiveWordCount > words.length / 2) {
            positiveNewsCount.put(headline, positiveNewsCount.getOrDefault(headline, 0) + 1);
            if (!positiveNewsPriority.containsKey(headline) || priority > positiveNewsPriority.get(headline)) {
                positiveNewsPriority.put(headline, priority);
            }
        }
    }

    private void startAnalysisThread() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(ANALYSIS_INTERVAL);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
                synchronized (this) {
                    System.out.println("Positive News Count in Last 10 Seconds: " + positiveNewsCount.size());
                    System.out.println("Unique Headlines of Highest Priority Positive News:");
                    int count = 0;
                    for (String headline : positiveNewsPriority.keySet()) {
                        System.out.println(headline);
                        count++;
                        if (count >= 3) {
                            break;
                        }
                    }
                    positiveNewsCount.clear();
                    positiveNewsPriority.clear();
                }
            }
        }).start();
    }


    public static void main(String[] args) {
        new NewsAnalyzer();
    }
}