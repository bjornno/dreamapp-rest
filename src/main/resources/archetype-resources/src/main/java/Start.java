
import org.apache.commons.dbcp.BasicDataSource;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.plus.naming.EnvEntry;
import org.mortbay.jetty.webapp.WebAppContext;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.Properties;


/**
 * TODO evaluate:
 * http://simplericity.org/svn/simplericity/trunk/jetty-console/jetty-console-core/src/main/java/JettyConsoleBootstrapMainClass.java
 *
 * (will enable us to put jetty deps in separate directory)
 *
 */
public class Start {

    private static final String WEBAPPLICATION_CONTEXT_NAME = "/";

    public static void main(String[] args) throws Exception {
        BasicDataSource dataSource = new BasicDataSource();
        Properties properties = loadProperties();
        dataSource.setDriverClassName(properties.getProperty("jdbc.driver"));
//        dataSource.setUrl("jdbc:hsqldb:mem:rest");
        dataSource.setUrl(properties.getProperty("jdbc.url"));
          dataSource.setPassword(properties.getProperty("jdbc.password"));
        dataSource.setUsername(properties.getProperty("jdbc.username"));
        new EnvEntry("jdbc/Ds", dataSource);
        Server server = new Server();
        Connector defaultConnector = new SocketConnector();
        defaultConnector.setPort(Integer.parseInt((String)properties.get("server.port")));
        server.setConnectors(new Connector[] { defaultConnector });
        server.addHandler(createWebappContextHandler());
        try {
            server.start();
        } catch (Exception e) {
            System.exit(-1);
        }
    }




    private static Handler createWebappContextHandler() {
        WebAppContext context = new WebAppContext();
        context.setContextPath(WEBAPPLICATION_CONTEXT_NAME);
        ProtectionDomain protectionDomain = Start.class.getProtectionDomain();
        setWebappFilesLocation(context, protectionDomain);
        return context;
    }

    private static void setWebappFilesLocation(WebAppContext context, ProtectionDomain protectionDomain) {
        URL location = protectionDomain.getCodeSource().getLocation();
        context.setWar(location.toExternalForm());
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("application.properties"));
        } catch (IOException e) {
            properties.put("server.port", "8080");
            properties.put("jdbc.driver", "org.hsqldb.jdbcDriver");
            properties.put("jdbc.url", "jdbc:hsqldb:file:database");
            properties.put("jdbc.username", "sa");
            properties.put("jdbc.password", "");
            try {
                properties.store(new FileOutputStream("application.properties"), null);
            } catch (Exception ex) {
              System.exit(-1);
            }
        }
        return properties;
    }
}
