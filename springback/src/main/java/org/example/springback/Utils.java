package org.example.springback;

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
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static List<String> findPoms(String repoRootString) {
        List<String> poms = new ArrayList<>();
        Path repoRoot = Paths.get(repoRootString);
        try {
            Files.walkFileTree(repoRoot, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.toString().endsWith(".pom")) {
                        //System.out.println(file);
                        poms.add(file.toString());
                    }
                    return FileVisitResult.CONTINUE; // Continue searching
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    System.err.println("Failed to access file: " + file + " (" + exc.getMessage() + ")");
                    return FileVisitResult.CONTINUE; // Skip the current file and continue
                }
            });
            System.out.println("Found " + poms.size() + " POMs.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return poms;
    }

    public static MavenArtifact indexArtifact(String pathToPomXml) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
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

        MavenArtifact mavenArtifact = new MavenArtifact();
        mavenArtifact.setGroupId(groupIdNode.getTextContent());
        mavenArtifact.setArtifactId(artifactIdNode.getTextContent());
        mavenArtifact.setVersion(versionNode.getTextContent());

        return mavenArtifact;
    }
}
