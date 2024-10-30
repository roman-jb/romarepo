package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

class ApiHandlers implements HttpHandler {

    protected static final Logger logger = LogManager.getLogger();
    private String localRoot;

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String appConfigPath = rootPath + "romarepo.properties";
        Properties appProps = new Properties();
        appProps.load(new FileInputStream(appConfigPath));
        localRoot = appProps.getProperty("localRoot","C:\\romarepo");

        String requestMethod = exchange.getRequestMethod();
        switch (requestMethod) {
            case "GET" -> handleGet(exchange);
            case "PUT" -> handlePut(exchange);
            default -> handleError(exchange);
        }
    }

//    LEGACY - To be removed
//    public void handleGet(HttpExchange exchange) throws IOException {
//        logger.debug(">> Received API GET request <<");
//        logger.debug("The following resource was requested: {}", exchange.getRequestURI());
//        String localResource = exchange.getRequestURI().getPath().replace("/api/browse", "");
//        Path LocalPath = Path.of(localRoot, localResource);
//        logger.debug("Local resource is: {}", LocalPath);
//        if (Files.exists(LocalPath) && !Files.isDirectory(LocalPath)) {
//            //TODO: Add an option to view contents of a file on the Frontend, instead of downloading it?
//            logger.info("!!! Received an API-Browse call for a file: {}   |   Such calls are not currently supported.", localResource);
//        } else if (Files.exists(LocalPath) && Files.isDirectory(LocalPath)) {
//            String jsonResponse = ApiUtils.getDirectoryContentsJSON(LocalPath);
//            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
//            OutputStream os = exchange.getResponseBody();
//            os.write(jsonResponse.getBytes());
//            os.close();
//        }
//    }

    public void handleGet(HttpExchange exchange) throws IOException {
        logger.debug(">> Received API GET request: {}", exchange.getRequestURI());
        String jsonResponse = ApiUtils.processApiCall(exchange.getRequestURI());
        exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(jsonResponse.getBytes());
        os.close();
    }

    public void handlePut(HttpExchange exchange) throws IOException {}

    private void handleError(HttpExchange exchange) throws IOException {}
}