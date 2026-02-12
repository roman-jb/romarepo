package org.example;

import com.sun.net.httpserver.*;
// ... existing code ...
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Objects;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RunServerHttps {
//    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(String[] args) throws IOException {
        System.setProperty("log4j.configurationFile", "log4j2-dev.xml"); //Doesn't work - needs fixing
        Logger logger = LogManager.getLogger();

        String rootPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("")).getPath();
        String appConfigPath = rootPath + "romarepo.properties";
        Properties appProps = new Properties();
        appProps.load(new FileInputStream(appConfigPath));

        int port = Integer.parseInt(appProps.getProperty("port","1234"));
        String localRoot = appProps.getProperty("localRoot","C:\\romarepo");
        String bindTo = appProps.getProperty("bindTo","/");
        String databaseLocation = appProps.getProperty("databaseLocation", "C:\\Users\\<USER>\\AppData\\Local\\romarepo");

        boolean sslEnabled = Boolean.parseBoolean(appProps.getProperty("sslEnabled", "false"));

        logger.debug("Server port: {}", port);
        logger.debug("Local root: {}", localRoot);
        logger.debug("Bind to: {}", bindTo);
        logger.debug("DB Location: {}", databaseLocation);
        logger.debug("SSL enabled: {}", sslEnabled);

        checkDatabaseStatus(databaseLocation);

        if (sslEnabled) {
            startHttpsServerWithCustomHandlers(port, 10, bindTo, appProps);
        } else {
            StartServerWithCustomHandlers(port, 10, bindTo);
        }
    }

    private static void StartServerWithCustomHandlers(int port, int backlog, String path) throws IOException {
        System.setProperty("log4j.configurationFile", "log4j2-dev.xml"); //Doesn't work - needs fixing
        Logger logger = LogManager.getLogger();
        HttpServer myServer = HttpServer.create(new InetSocketAddress(port), backlog);
        myServer.createContext(path, new MyHandlers());
        myServer.createContext("/api", new ApiHandlers());
        myServer.setExecutor(null);
        myServer.start();
        logger.info("============= HTTP SERVER STARTED ===========");
    }

    private static void startHttpsServerWithCustomHandlers(int port, int backlog, String path, Properties appProps) throws IOException {
        Logger logger = LogManager.getLogger();

        SSLContext sslContext;
        try {
            sslContext = buildSslContext(appProps);
        } catch (Exception e) {
            throw new IOException("Failed to initialize SSL context", e);
        }

        HttpsServer myServer = HttpsServer.create(new InetSocketAddress(port), backlog);
        myServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
            @Override
            public void configure(HttpsParameters params) {
                SSLContext c = getSSLContext();
                SSLEngine engine = c.createSSLEngine();
                params.setNeedClientAuth(false);
                params.setProtocols(engine.getEnabledProtocols());
                params.setCipherSuites(engine.getEnabledCipherSuites());
                params.setSSLParameters(c.getDefaultSSLParameters());
            }
        });

        myServer.createContext(path, new MyHandlers());
        myServer.createContext("/api", new ApiHandlers());
        myServer.setExecutor(null);
        myServer.start();
        logger.info("============= HTTPS SERVER STARTED ===========");
    }

    private static SSLContext buildSslContext(Properties appProps) throws Exception {
        String keystoreResource = appProps.getProperty("sslKeystore");
        String keystorePassword = appProps.getProperty("sslKeystorePassword");
        String keyPassword = appProps.getProperty("sslKeyPassword", keystorePassword);
        String keystoreType = appProps.getProperty("sslKeystoreType", "PKCS12");

        if (keystoreResource == null || keystorePassword == null) {
            throw new IllegalArgumentException("Missing sslKeystore / sslKeystorePassword in properties");
        }

        KeyStore ks = KeyStore.getInstance(keystoreType);
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(keystoreResource)) {
            if (is == null) {
                throw new IllegalArgumentException("Keystore not found on classpath: " + keystoreResource);
            }
            ks.load(is, keystorePassword.toCharArray());
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, keyPassword.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return sslContext;
    }

    private static void checkDatabaseStatus(String databaseLocation) {
        String databasePathString = databaseLocation + "/romarepo.db";
        Path databasePath = Path.of(databasePathString);
        if(!Files.exists(databasePath)) {
            SQLiteUtils sqlLiteInit = new SQLiteUtils();
            sqlLiteInit.initDatabase(sqlLiteInit.connect(databasePathString));
        }
    }

    //Deprecated - to be removed
    private static void StartSimpleServer() {
        InetSocketAddress address = new InetSocketAddress(1234);
        Path path = Path.of("C:\\tmp\\mavenLocal");
        HttpServer server = SimpleFileServer.createFileServer(address, path, SimpleFileServer.OutputLevel.VERBOSE);
        server.start();
    }
}