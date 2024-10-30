package org.example;

import com.sun.net.httpserver.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RunServer {
//    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(String[] args) throws IOException, SQLException {
        System.setProperty("log4j.configurationFile", "log4j2-dev.xml"); //Doesn't work - needs fixing
        Logger logger = LogManager.getLogger();

        String rootPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("")).getPath();
        String appConfigPath = rootPath + "romarepo.properties";
        Properties appProps = new Properties();
        appProps.load(new FileInputStream(appConfigPath));

        int port = Integer.parseInt(appProps.getProperty("port","1234"));
        String localRoot = appProps.getProperty("localRoot","C:\\romarepo");
        String bindTo = appProps.getProperty("bindTo","/");
        String databaseLocation = appProps.getProperty("databaseLocation", "C:\\Users\\Roman.Vatagin\\AppData\\Local\\romarepo");
        logger.debug("Server port: {}", port);
        logger.debug("Local root: {}", localRoot);
        logger.debug("Bind to: {}", bindTo);
        logger.debug("DB Location: {}", databaseLocation);
        checkDatabaseStatus(databaseLocation);
        StartServerWithCustomHandlers(port, 10, bindTo);
    }

    private static void StartServerWithCustomHandlers(int port, int backlog, String path) throws IOException {
        System.setProperty("log4j.configurationFile", "log4j2-dev.xml"); //Doesn't work - needs fixing
        Logger logger = LogManager.getLogger();
        //HttpServer myServer = HttpServer.create(new InetSocketAddress(port), backlog, path, new MyHandlers());
        HttpServer myServer = HttpServer.create(new InetSocketAddress(port), backlog);
        myServer.createContext(path, new MyHandlers());
        myServer.createContext("/api", new ApiHandlers());
        myServer.setExecutor(null);
        myServer.start();
        logger.info("============= SERVER STARTED ===========");
    }

    private static void checkDatabaseStatus(String databaseLocation) {
        String databasePathString = databaseLocation + "/romarepo.db";
        Path databasePath = Path.of(databasePathString);
        if(!Files.exists(databasePath)) {
            SQLiteUtils sqlLiteInit = new SQLiteUtils();
            sqlLiteInit.initDatabase(sqlLiteInit.connect(databasePathString));
        };
    }

    //Deprecated - to be removed
    private static void StartSimpleServer() {
        InetSocketAddress address = new InetSocketAddress(1234);
        Path path = Path.of("C:\\tmp\\mavenLocal");
        HttpServer server = SimpleFileServer.createFileServer(address, path, SimpleFileServer.OutputLevel.VERBOSE);
        server.start();
    }
}
