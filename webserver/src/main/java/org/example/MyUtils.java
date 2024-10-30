package org.example;

import com.sun.net.httpserver.HttpExchange;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
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
import java.sql.Connection;
import java.util.ArrayList;
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

        Node groupIdNode = (Node) groupId.evaluate(document, XPathConstants.NODE);
        if (groupIdNode == null) {
            groupId = xPath.compile("//project/parent/groupId");
            groupIdNode = (Node) groupId.evaluate(document, XPathConstants.NODE);
        }
        Node artifactIdNode = (Node) artifactId.evaluate(document, XPathConstants.NODE);
        Node versionNode = (Node) version.evaluate(document, XPathConstants.NODE);
        if (versionNode == null) {
            version = xPath.compile("//project/parent/version");
            versionNode = (Node) version.evaluate(document, XPathConstants.NODE);
        }

        return new MavenArtifact(
                groupIdNode.getTextContent(),
                artifactIdNode.getTextContent(),
                versionNode.getTextContent());
    }

    static void indexRepo(String rootDirectory, String databaseLocation) throws IOException {

        Path rootPath = Paths.get(rootDirectory);
        List<Path> allFiles = new ArrayList<>();
        findAllPoms(rootPath, allFiles);
        SQLiteUtils sqlliteUtils = new SQLiteUtils();
        Connection conn = sqlliteUtils.connect(databaseLocation);
        for (Path file : allFiles) {
            try {
                sqlliteUtils.insert(indexArtifact(file.toString()), conn);
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
                System.out.println("-------------------------------------");
                System.out.println("file: " + file);
            }
        }
    }

    private static void findAllPoms(Path currentPath, List<Path> pomsList)
            throws IOException
    {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentPath))
        {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    findAllPoms(entry, pomsList);
                } else if (entry.toString().endsWith(".pom")) {
                    pomsList.add(entry);
                }
            }
        }
    }
}
