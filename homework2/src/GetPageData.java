import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetPageData implements Runnable {
    Server server;
    URL url;
    String pageData;

    GetPageData(Server server) {
        this.server = server;
    }

    public String getPageData() {
        return pageData;
    }

    @Override
    public void run() {
        url = server.getURL();
        pageData = "";
        String lineString;
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "text/plain");
            connection.connect();

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            while ((lineString = br.readLine()) != null) {
                pageData += lineString;
            }
        } catch (IOException e) {
            System.out.println("IO Exception occurred while getting page data in Page Data Thread.");
        }
    }
}
