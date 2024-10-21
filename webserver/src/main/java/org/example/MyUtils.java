package org.example;

import com.sun.net.httpserver.HttpExchange;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;


public class MyUtils {
    private static final Logger log = LogManager.getLogger(MyUtils.class);

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

    // !!! Does not account for cases, where <version> is not explicitly specified
    static MavenArtifact indexArtifact(String pathToPomXml) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(pathToPomXml));
        document.getDocumentElement().normalize();
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xPath = xPathfactory.newXPath();

        // vvv optimize? vvv
        XPathExpression groupId = xPath.compile("//project/groupId");
        XPathExpression artifactId = xPath.compile("//project/artifactId");
        XPathExpression version = xPath.compile("//project/version");

        NodeList groupIdList = (NodeList) groupId.evaluate(document, XPathConstants.NODESET);
        NodeList artifactIdList = (NodeList) artifactId.evaluate(document, XPathConstants.NODESET);
        NodeList versionList = (NodeList) version.evaluate(document, XPathConstants.NODESET);

        return new MavenArtifact(
                groupIdList.item(0).getTextContent(),
                artifactIdList.item(0).getTextContent(),
                versionList.item(0).getTextContent());
    }

    static void indexRepo() {
        String startDir = "C:\\tmp\\mavenLocal";
        String fileToFind = "pom.xml";

        try {
            Files.walkFileTree(Paths.get(startDir), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.getFileName().toString().equals(fileToFind)) {
                        System.out.println("Found file: " + file);
                        return FileVisitResult.TERMINATE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    log.error("e: ", exc);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
