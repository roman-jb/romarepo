package org.example.springback;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ApiUtils {
    public static String GetDirectoryContent(String path) throws IOException {
        Path dirPath = Paths.get(path);
        List<String> directoryContents = Files.list(dirPath)
                .map(Path::getFileName)
                .map(Path::toString)
                .toList();
        ObjectMapper mapper = new ObjectMapper();
        List<FileSystemObject> responseList = new ArrayList<>();
        for (String item : directoryContents) {
            Path itemPath = dirPath.resolve(item);
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
