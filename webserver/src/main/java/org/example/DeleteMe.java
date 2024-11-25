package org.example;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

class DeleteMe {

    public static void main(String[] args) throws IOException {
        //investigateArtifact("C:\\tmp\\mavenLocal\\args4j\\args4j\\2.33\\args4j-2.33.pom");
        mockRepoIndex();
    }

    private static void mockRepoIndex() throws IOException {
        Path rootPath = Paths.get("C:\\tmp\\mavenLocal");
        List<Path> allFiles = new ArrayList<>();
        findAllPoms(rootPath, allFiles);
        SQLiteUtils sqlliteUtils = new SQLiteUtils();

        System.out.println("Found " + allFiles.size() + " files" );
        System.out.println("=====================================");
        //allFiles.forEach(System.out::println);
        for (Path file : allFiles) {
//            System.out.println(file);
//            try {
//                MavenArtifact artifact = indexArtifact(file.toString());
//                System.out.println("groupId: " + artifact.getGroupId());
//                System.out.println("artifactId: " + artifact.getArtifactId());
//                System.out.println("version: " + artifact.getVersion());
//                System.out.println("-------------------------------------");
//            } catch (Exception e) {
//                System.out.println("ERROR: " + e.getMessage());
//            }
            try {
                Connection conn = sqlliteUtils.connect("C:\\tmp\\romarepo\\database\\romarepo.db");
                sqlliteUtils.insert(indexArtifact(file.toString()), conn);
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
                System.out.println("-------------------------------------");
                System.out.println("file: " + file);
            }
        }
        System.out.println("=====================================");
        System.out.println("TOTAL: " + allFiles.size());
    }

    private static void findAllPoms(Path currentPath, List<Path> allFiles)
            throws IOException
    {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentPath))
        {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    findAllPoms(entry, allFiles);
                } else if (entry.toString().endsWith(".pom")) {
                    allFiles.add(entry);
                }
            }
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

    private static void investigateArtifact(String pathToPomString) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        Path pathToPom = Paths.get(pathToPomString);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(String.valueOf(pathToPom)));
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
        System.out.println(pathToPom);
        System.out.println(">> groupId: " + groupIdNode.getTextContent());
        System.out.println(">> artifactId: " + artifactIdNode.getTextContent());
        System.out.println(">> version: " + versionNode.getTextContent());
    }
}