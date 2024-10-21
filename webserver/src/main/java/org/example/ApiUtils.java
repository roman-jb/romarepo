package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.nio.file.Path;
import java.util.*;

public class ApiUtils {
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