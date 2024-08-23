package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class MyHandlers implements HttpHandler {

    public static final String ROOT_FOLDER = "C:\\tmp\\mavenLocal";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        switch (requestMethod) {
            case "GET" -> handleGet(exchange);
            case "PUT" -> handlePut(exchange);
            default -> handleError(exchange);
        }
    }

    public void handleGet(HttpExchange exchange) throws IOException {
        System.out.println("<< Received GET request >>");
        String requestedFile = exchange.getRequestURI().getPath().replace("/myrepo", "");
        System.out.println("This file was requested: " + requestedFile);
        Path filePath = Path.of(ROOT_FOLDER, requestedFile);
        System.out.println("Local path to the file is: " + filePath);

        if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
            System.out.println("> The file exists!");
            byte[] fileBytes = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);

            exchange.getResponseHeaders().set("Content-Type", contentType != null ? contentType : "application/octet-stream");
            exchange.sendResponseHeaders(200, fileBytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileBytes);
            }
        } else if (Files.exists(filePath) && Files.isDirectory(filePath)) {
            List<String> directoryContents = Files.list(filePath)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .toList();

            // Start constructing the HTML response
            StringBuilder response = new StringBuilder("<html><body>");
            response.append("<h2>Directory: ").append(filePath).append("</h2>");
            response.append("<ul>");
            for (String item : directoryContents) {
                Path itemPath = filePath.resolve(item);
                if (Files.isDirectory(itemPath)) {
                    response.append("<li><a href=\"")
                            .append(exchange.getRequestURI()
                            .getPath()).append("/")
                            .append(item).append("\">[Folder] ")
                            .append(item).append("</a></li>");
                } else {
                    response.append("<li>").append(item).append("</li>");
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
            System.out.println("!!! File not found !!!");
            exchange.sendResponseHeaders(404, -1); // 404 Not Found
        }
    }

    public void handlePut(HttpExchange exchange) throws IOException {
        System.out.println("<< Received PUT request >>");
        String pathAndName = exchange.getRequestURI().getPath().replace("/myrepo", "");
        System.out.println("Attempting to upload the file: " + pathAndName);
        String fileName = pathAndName.substring(pathAndName.lastIndexOf("/") + 1);
        System.out.println("The file name is: " + fileName);
        String filePath = pathAndName.substring(0, pathAndName.lastIndexOf("/"));
        System.out.println("The file path is: " + filePath);

        if (filePath.isEmpty() || fileName.isEmpty()) {
            exchange.sendResponseHeaders(400, -1); // 400 Bad Request
            return;
        }

        Path localFilePath = Path.of(ROOT_FOLDER, filePath);
        Path localFilePathAndName = Path.of(ROOT_FOLDER, pathAndName);
        System.out.println("Local path to the file is: " + localFilePath);
        System.out.println("Local path and name of the file is: " + localFilePathAndName);
        File directory = new File(String.valueOf(localFilePath));
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("Successfully created the directories!");
            } else {
                System.out.println("!!! Failed to create directories !!!");
            }
        }

        try (InputStream inputStream = exchange.getRequestBody();
             FileOutputStream outputStream = new FileOutputStream(localFilePathAndName.toFile())) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        exchange.sendResponseHeaders(201, -1); // 201 Created
        System.out.println("<< PUT request completed >>");
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
