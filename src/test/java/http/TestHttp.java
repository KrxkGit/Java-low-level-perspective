package http;

import com.sun.net.httpserver.HttpServer;
import org.junit.Test;

import java.io.IOException;
import java.net.http.HttpClient;

public class TestHttp {
    @Test
    public void test() throws IOException {
        HttpClient client = HttpClient.newHttpClient();
        HttpServer server = HttpServer.create();
    }
}
