package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class MyHandlers implements HttpHandler {

    protected static final Logger logger = LogManager.getLogger();

    //public static final String ROOT_FOLDER = "C:\\tmp\\mavenLocal";

    private String localRoot;

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String appConfigPath = rootPath + "romarepo.properties";
        Properties appProps = new Properties();
        appProps.load(new FileInputStream(appConfigPath));
        localRoot = appProps.getProperty("localRoot","C:\\romarepo");
        //logger.debug("Local root: {}", localRoot); //Too spammy!

        String requestMethod = exchange.getRequestMethod();
        switch (requestMethod) {
            case "GET" -> handleGet(exchange);
            case "PUT" -> handlePut(exchange);
            default -> handleError(exchange);
        }
    }

    public void handleGet(HttpExchange exchange) throws IOException {
        logger.debug("<< Received GET request >>");
        String requestedFile = exchange.getRequestURI().getPath().replace("/myrepo", "");
        logger.debug("The following resource was requested: {}", requestedFile);
        Path filePath = Path.of(localRoot, requestedFile);
        logger.debug("Local path to the file is: {}", filePath);

        if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
            long fileSize = Files.size(filePath);  // Get the size of the file
            // Set the response headers
            exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
            exchange.getResponseHeaders().add("Content-Length", Long.toString(fileSize));
            exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=\"" + filePath.getFileName() + "\"");
            exchange.sendResponseHeaders(200, fileSize);
            try (exchange; OutputStream os = exchange.getResponseBody();
                 FileInputStream fis = new FileInputStream(filePath.toFile())) {
                //String contentType = Files.probeContentType(filePath);
                //exchange.getResponseHeaders().set("Content-Type", contentType != null ? contentType : "application/octet-stream");
                //exchange.sendResponseHeaders(200, 0);
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                logger.debug("<< GET request completed >>");
            }
//            logger.debug("> The file exists!");
//            byte[] fileBytes = Files.readAllBytes(filePath); //May eat too much RAM - FIX!
//            String contentType = Files.probeContentType(filePath);
//
//            exchange.getResponseHeaders().set("Content-Type", contentType != null ? contentType : "application/octet-stream");
//            exchange.sendResponseHeaders(200, fileBytes.length);
//
//            try (OutputStream os = exchange.getResponseBody()) {
//                os.write(fileBytes);
//            }
        } else if (Files.exists(filePath) && Files.isDirectory(filePath)) {
            List<String> directoryContents = Files.list(filePath)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .toList();

            // Start constructing the HTML response
            StringBuilder response = new StringBuilder("<html><body>");
            response.append("<h2>Directory: ").append(requestedFile).append("</h2>");
            response.append("<ul>");
            if (!requestedFile.equals("/")) {
                response.append("<li><a href=\"../\">[..]</a></li>");
            }
            for (String item : directoryContents) {
                Path itemPath = filePath.resolve(item);
                if (Files.isDirectory(itemPath)) {
                    response.append("<li><a href=\"")
                            .append(exchange.getRequestURI()
                            .getPath()).append("/")
                            .append(item).append("\">[Folder] ")
                            .append(item).append("</a></li>");
                } else {
                    response.append("<li><a href=\"")
                            .append(exchange.getRequestURI()
                            .getPath()).append("/")
                            .append(item).append("\">")
                            .append(item).append("</a></li>");
                    //response.append("<li>").append(item).append("</li>");
                }
            }
            response.append("</ul>");
            response.append("</body></html>");

            exchange.sendResponseHeaders(200, response.toString().getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.toString().getBytes());
            }
        }
        else {
            logger.info("!!! File not found !!!");
            exchange.sendResponseHeaders(404, -1); // 404 Not Found
        }
    }

    public void handlePut(HttpExchange exchange) throws IOException {
        logger.debug("<< Received PUT request >>");
        String pathAndName = exchange.getRequestURI().getPath().replace("/myrepo", "");
        logger.debug("Attempting to upload the file: {}", pathAndName);
        String fileName = pathAndName.substring(pathAndName.lastIndexOf("/") + 1);
        logger.debug("The file name is: {}", fileName);
        String filePath = pathAndName.substring(0, pathAndName.lastIndexOf("/"));
        logger.debug("The file path is: {}", filePath);

        if (filePath.isEmpty() || fileName.isEmpty()) {
            exchange.sendResponseHeaders(400, -1); // 400 Bad Request
            return;
        }

        Path localFilePath = Path.of(localRoot, filePath);
        Path localFilePathAndName = Path.of(localRoot, pathAndName);
        logger.debug("Local path to the file is: {}", localFilePath);
        logger.debug("Local path and name of the file is: {}", localFilePathAndName);
        File directory = new File(String.valueOf(localFilePath));
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                logger.debug("Successfully created the directories!");
            } else {
                logger.info("!!! Failed to create directories !!!");
                logger.info(directory);
            }
        }

        try (InputStream inputStream = exchange.getRequestBody();
             FileOutputStream outputStream = new FileOutputStream(localFilePathAndName.toFile())) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        exchange.sendResponseHeaders(201, -1); // 201 Created
        logger.debug("<< PUT request completed >>");
    }

    private void handleError(HttpExchange exchange) throws IOException {
        var responseBytes = "<html><body>Error 451</body></html>".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.getResponseHeaders().set("Content-Length", Integer.toString(responseBytes.length));
        exchange.sendResponseHeaders(451, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
