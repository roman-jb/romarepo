package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.List;
import java.nio.file.Path;
import java.util.*;

public class ApiUtils {

    protected static final Logger logger = LogManager.getLogger();

    static String processApiCall(URI ApiCall) throws IOException {
        if (ApiCall.getPath().startsWith("/api/browse")) {
            return processApiBrowseCall(ApiCall);
        } else if (ApiCall.getPath().startsWith("/api/search")) {
            return processApiSearchCall(ApiCall);
        } else {
            logger.info("!!! Received an API call for an unknown resource: {}", ApiCall.getPath());
            return "!!! Received an API call for an unknown resource: " + ApiCall.getPath();
        }
    }

    static String processApiBrowseCall(URI ApiCall) throws IOException {
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String appConfigPath = rootPath + "romarepo.properties";
        Properties appProps = new Properties();
        appProps.load(new FileInputStream(appConfigPath));
        String localRoot = appProps.getProperty("localRoot","C:\\romarepo");

        String localResource = ApiCall.getPath().replace("/api/browse", "");
        Path LocalPath = Path.of(localRoot, localResource);
        logger.debug("Local resource is: {}", LocalPath);
        if (Files.exists(LocalPath) && !Files.isDirectory(LocalPath)) {
            //TODO: Add an option to view contents of a file on the Frontend, instead of downloading it?
            logger.info("!!! Received an API-Browse call for a file: {}   |   Such calls are not currently supported.", localResource);
            return "!!! Received an API-Browse call for a file are not currently supported: " + localResource;
        } else if (Files.exists(LocalPath) && Files.isDirectory(LocalPath)) {
            return ApiUtils.getDirectoryContentsJSON(LocalPath);
        } else {
            logger.info("!!! Received an API-Browse call for a non-existent resource: {}", localResource);
            return "!!! Received an API-Browse call for a non-existent resource: " + localResource;
        }
    }

    static String processApiSearchCall(URI ApiCall) throws IOException {
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String appConfigPath = rootPath + "romarepo.properties";
        Properties appProps = new Properties();
        appProps.load(new FileInputStream(appConfigPath));
        String databaseLocation = appProps.getProperty("databaseLocation", "C:\\Users\\Roman.Vatagin\\AppData\\Local\\romarepo");

        String searchQuery = ApiCall.getQuery(); //"query=HelloWorld"
        searchQuery = searchQuery.replace("query=", "");
        logger.debug("Search query is: {}", searchQuery);
        SQLiteUtils sqlLite = new SQLiteUtils();
        ObjectMapper mapper = new ObjectMapper();
        List<MavenArtifact> searchResults = sqlLite.findArtifacts(searchQuery, sqlLite.connect(databaseLocation + "\\romarepo.db"));
        return mapper.writeValueAsString(searchResults);
    }

    static String getDirectoryContentsJSON(Path LocalPath) throws IOException {
        List<String> directoryContents = Files.list(LocalPath)
        .map(Path::getFileName)
        .map(Path::toString)
        .toList();
        ObjectMapper mapper = new ObjectMapper();
        List<FileSystemObject> responseList = new ArrayList<>();
        for (String item : directoryContents) {
            Path itemPath = LocalPath.resolve(item);
            if (Files.isDirectory(itemPath)) {
                responseList.add(new FileSystemObject(item, "Directory"));
            }
            else {
                responseList.add(new FileSystemObject(item, "File"));
            }
        }
        return mapper.writeValueAsString(responseList);
    }
}