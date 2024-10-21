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

    public void handleGet(HttpExchange exchange) throws IOException {
        logger.debug(">> Received API GET request <<");
        logger.debug("The following resource was requested: {}", exchange.getRequestURI());
        String localResource = exchange.getRequestURI().getPath().replace("/api/browse", "");
        Path LocalPath = Path.of(localRoot, localResource);
        logger.debug("Local resource is: {}", LocalPath);
        if (Files.exists(LocalPath) && !Files.isDirectory(LocalPath)) {
            MyUtils.sendFile(LocalPath, exchange); //TODO: Doesn't work!
        } else if (Files.exists(LocalPath) && Files.isDirectory(LocalPath)) {
            String jsonResponse = ApiUtils.getDirectoryContentsJSON(LocalPath);
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(jsonResponse.getBytes());
            os.close();
        }
    }

    public void handlePut(HttpExchange exchange) throws IOException {}

    private void handleError(HttpExchange exchange) throws IOException {}

//    public void handleGet(HttpExchange exchange) throws IOException {
//
//        String response = "API Response Placeholder for GET method";
//
//        logger.debug(">> Received API GET request <<");
//        logger.debug("The following resource was requested: {}", exchange.getRequestURI());
//        String localResource = exchange.getRequestURI().getPath().replace("/api/browse", "");
//        Path LocalPath = Path.of(localRoot, localResource);
//        logger.debug("Local resource is: {}", LocalPath);
//
//        if (Files.exists(LocalPath) && !Files.isDirectory(LocalPath)) {
//            response = "...This is where it supposed to send you the file...";
//        } else if (Files.exists(LocalPath) && Files.isDirectory(LocalPath)) {
//            List<String> directoryContents = Files.list(LocalPath)
//                    .map(Path::getFileName)
//                    .map(Path::toString)
//                    .toList();
//            StringBuilder responseSB = new StringBuilder("Directory contents,");
//            for (String item : directoryContents) {
//                Path itemPath = LocalPath.resolve(item);
//                if (Files.isDirectory(itemPath)) {
//                    item = "[" + item + "],";
//                    responseSB.append(item);
//                }
//                else { responseSB.append(item).append(","); }
//            }
//            response = responseSB.toString();
//        }
//        exchange.sendResponseHeaders(200, response.getBytes().length);
//        OutputStream os = exchange.getResponseBody();
//        os.write(response.getBytes());
//        os.close();
//    }

//    @Override
//    public void handle(HttpExchange exchange) throws IOException {
//        String response = "Response for API!";
//        exchange.sendResponseHeaders(200, response.getBytes().length);
//        OutputStream os = exchange.getResponseBody();
//        os.write(response.getBytes());
//        os.close();
//    }
}