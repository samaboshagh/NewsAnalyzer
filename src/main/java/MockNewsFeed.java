import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

@RequiredArgsConstructor
public class MockNewsFeed {

    private static final String[] WORDS = {"up", "down", "rise", "fall", "good", "bad", "success", "failure", "high", "low"};
    private static final int MAX_PRIORITY = 9;
    private static final int MIN_PRIORITY = 0;
    private static final int DEFAULT_FREQUENCY = 5000;

    private final String analyzerHost;

    private final int analyzerPort;

    private final int frequency;

    public void startSending() {
        try (Socket socket = new Socket(analyzerHost, analyzerPort)) {
            OutputStream outputStream = socket.getOutputStream();
            Random random = new Random();
            while (true) {
                String headline = generateRandomHeadline(random);
                int priority = random.nextInt(MAX_PRIORITY - MIN_PRIORITY + 1) + MIN_PRIORITY;
                String message = headline + "|" + priority + "\n";
                outputStream.write(message.getBytes());
                outputStream.flush();
                Thread.sleep(frequency);
            }
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    private String generateRandomHeadline(Random random) {
        StringBuilder headlineBuilder = new StringBuilder();
        int numWords = random.nextInt(3) + 3;
        for (int i = 0; i < numWords; i++) {
            int index = random.nextInt(WORDS.length);
            headlineBuilder.append(WORDS[index]);
            if (i < numWords - 1) {
                headlineBuilder.append(" ");
            }
        }
        return headlineBuilder.toString();
    }

    public static void main(String[] args) {
        MockNewsFeed mockNewsFeed = new MockNewsFeed("localhost", 8080, DEFAULT_FREQUENCY);
        mockNewsFeed.startSending();
    }
}