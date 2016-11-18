/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.rest;

import eu.edisonproject.utility.file.ConfigHelper;
import eu.edisonproject.utility.file.MyProperties;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 *
 * @author S. Koulouzis
 */
public class ECO2Server {

  public static void main(String[] args) {
    Thread t = null;
    Server server = null;
    try {
      ECO2Controller.initPaths();
      t = startTaskWatcher();
      t.start();
      server = startServer(args);
      server.start();
      t.join();
      server.join();
    } catch (IOException ex) {
      Logger.getLogger(ECO2Server.class.getName()).log(Level.SEVERE, null, ex);
    } catch (InterruptedException ex) {
      Logger.getLogger(ECO2Server.class.getName()).log(Level.SEVERE, null, ex);
    } catch (Exception ex) {
      Logger.getLogger(ECO2Server.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
      if (server != null) {
        server.destroy();
      }
      if (t != null) {
        try {
          t.join(100);
        } catch (InterruptedException ex) {
          Logger.getLogger(ECO2Server.class.getName()).log(Level.SEVERE, null, ex);
        }
      }

    }
  }

  private static Server startServer(String[] args) throws IOException {
    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
    context.setContextPath("/");
    MyProperties props = null;
    Integer port = 9999;
    if (args != null) {
      props = ConfigHelper.getProperties(args[0]);
      port = Integer.valueOf(props.getProperty("e-co-2.server.port", "9999"));
    }

    Server jettyServer = new Server(port);
    jettyServer.setHandler(context);

    ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, "/*");
    jerseyServlet.setInitOrder(0);

    jerseyServlet.setInitParameter("jersey.config.server.provider.packages", "eu.edisonproject.rest");
    return jettyServer;
  }

  private static Thread startTaskWatcher() throws IOException, InterruptedException {

    Runnable folderWatcherRunnable = new FolderWatcherRunnable(ECO2Controller.baseClassisifcationFolder.getAbsolutePath());

    return new Thread(folderWatcherRunnable);

  }

}
