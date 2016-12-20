/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.rest;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import eu.edisonproject.utility.file.ConfigHelper;
import eu.edisonproject.utility.file.MyProperties;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;

/**
 *
 * @author S. Koulouzis
 */
public class ECO2Server {

  public static void main(String[] args) {
    Thread jobWatcher;
    Thread cvWatcher;
    Thread courseWatcher;
    Server server = null;
    try {
      ECO2Controller.initPaths();
      jobWatcher = startTaskWatcher(ECO2Controller.jobClassisifcationFolder.getAbsolutePath());
      jobWatcher.start();

      cvWatcher = startTaskWatcher(ECO2Controller.cvClassisifcationFolder.getAbsolutePath());
      cvWatcher.start();

      courseWatcher = startTaskWatcher(ECO2Controller.courseClassisifcationFolder.getAbsolutePath());
      courseWatcher.start();

      server = startServer(args);
      server.start();
      jobWatcher.join();
      cvWatcher.join();
      courseWatcher.join();
      server.join();
    } catch (IOException ex) {
      Logger.getLogger(ECO2Server.class.getName()).log(Level.SEVERE, null, ex);
    } catch (InterruptedException ex) {
      Logger.getLogger(ECO2Server.class.getName()).log(Level.SEVERE, null, ex);
    } catch (Exception ex) {
      Logger.getLogger(ECO2Server.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
      if (server != null && !server.isStopped()) {
        server.destroy();
      }
    }
  }

  private static Server startServer(String[] args) throws IOException {
    MyProperties props = null;
    Integer port = 9999;
    if (args != null && args.length > 0) {
      props = ConfigHelper.getProperties(args[0]);
      port = Integer.valueOf(props.getProperty("e-co-2.server.port", "9999"));
    }
    ResourceConfig config = new ResourceConfig();
    config.packages("eu.edisonproject.rest");
    ServletHolder servlet = new ServletHolder(new ServletContainer(config));

    Server server = new Server(port);
    ServletContextHandler context = new ServletContextHandler(server, "/*");
    context.addServlet(servlet, "/*");

//    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
//    context.setContextPath("/");
//
//    Server jettyServer = new Server(port);
//    jettyServer.setHandler(context);
//
//    ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, "/*");
//    jerseyServlet.setInitOrder(0);
//
//    jerseyServlet.setInitParameter("jersey.config.server.provider.packages", "eu.edisonproject.rest");
//    return jettyServer;
    return server;
  }

  private static Thread startTaskWatcher(String dir) throws IOException, InterruptedException {

    Runnable folderWatcherRunnable = new FolderWatcherRunnable(dir);

    return new Thread(folderWatcherRunnable);

  }

}
