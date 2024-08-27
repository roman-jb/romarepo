package org.example;

import com.sun.net.httpserver.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RunServer {
    protected static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) throws IOException {
        System.setProperty("log4j.configurationFile", "log4j2-dev.xml");

        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String appConfigPath = rootPath + "romarepo.properties";
        Properties appProps = new Properties();
        appProps.load(new FileInputStream(appConfigPath));

        int port = Integer.parseInt(appProps.getProperty("port","1234"));
        String localRoot = appProps.getProperty("localRoot","C:\\romarepo");
        String bindTo = appProps.getProperty("bindTo","/");
        logger.debug("Server port: {}", port);
        logger.debug("Local root: {}", localRoot);
        logger.debug("Bind to: {}", bindTo);

        //StartSimpleServer();
        StartServerWithCustomHandlers(port, 10, bindTo);
    }

    private static void StartSimpleServer() {
        InetSocketAddress address = new InetSocketAddress(1234);
        Path path = Path.of("C:\\tmp\\mavenLocal");
        HttpServer server = SimpleFileServer.createFileServer(address, path, SimpleFileServer.OutputLevel.VERBOSE);
        server.start();
    }

    private static void StartServerWithCustomHandlers(int port, int backlog, String path) throws IOException {
        final Logger logger = LogManager.getLogger();
        HttpServer myServer = HttpServer.create(new InetSocketAddress(port), backlog, path, new MyHandlers());
        myServer.start();
        logger.info("============= SERVER STARTED ===========");
    }
}
