package org.example;

import com.sun.net.httpserver.HttpExchange;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class MyUtils {
    static void sendFile(Path filePath, HttpExchange exchange) throws IOException {
        long fileSize = Files.size(filePath);  // Get the size of the file
        exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
        exchange.getResponseHeaders().add("Content-Length", Long.toString(fileSize));
        exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=\"" + filePath.getFileName() + "\"");
        exchange.sendResponseHeaders(200, fileSize);
        try (exchange; OutputStream os = exchange.getResponseBody();
             FileInputStream fis = new FileInputStream(filePath.toFile())) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
    }

    static void getDirectoryContentsHTML(Path filePath, HttpExchange exchange, String requestedFile) throws IOException {
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
                        .append(item).append("\">[")
                        .append(item).append("]").append("</a></li>");
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
}
