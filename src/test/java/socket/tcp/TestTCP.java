package socket.tcp;

import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;

public class TestTCP {
    @Test
    public void test() throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
    }
}
