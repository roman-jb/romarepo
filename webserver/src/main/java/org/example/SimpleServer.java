package org.example;

import com.sun.net.httpserver.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;

public class SimpleServer {
    public static void main(String[] args) throws IOException {
        //StartSimpleServer();
        StartServerWithCustomHandlers();
    }

    private static void StartSimpleServer() {
        InetSocketAddress address = new InetSocketAddress(1234);
        Path path = Path.of("C:\\tmp\\mavenLocal");
        HttpServer server = SimpleFileServer.createFileServer(address, path, SimpleFileServer.OutputLevel.VERBOSE);
//      HttpHandler fileHandler = SimpleFileServer.createFileHandler(Path.of("C:\\tmp\\mywebserver\\myrepo"));
//      HttpHandler uploadHandler = HttpHandlers.of(200, Headers.of("PUT"), "");
//      Predicate<Request> IS_UPLOAD = r -> r.getRequestMethod().equals("PUT");
//      HttpHandler finalHandler = HttpHandlers.handleOrElse(IS_UPLOAD, uploadHandler, fileHandler);
//      server.createContext("/myrepo", finalHandler);
        server.start();
    }

    private static void StartServerWithCustomHandlers() throws IOException {
        HttpServer myServer = HttpServer.create(new InetSocketAddress(1234), 10, "/myrepo", new MyHandlers());
        myServer.start();
        System.out.println("============= SERVER STARTED ===========");
    }
}
