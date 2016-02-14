package edu.jhuapl.dorset.demos;

import java.net.URL;
import java.security.ProtectionDomain;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class Runner {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8888);

        WebAppContext context = new WebAppContext();
        context.setServer(server);
        context.setContextPath("/");
        // turn off class loading from WEB-INF due to logging
        context.setParentLoaderPriority(true);

        ProtectionDomain protectionDomain = Runner.class.getProtectionDomain();
        URL location = protectionDomain.getCodeSource().getLocation();
        context.setWar(location.toExternalForm());

        server.setHandler(context);
        server.start();
        server.join();
    }
}
