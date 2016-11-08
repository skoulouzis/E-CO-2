/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.rest;

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
    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
    context.setContextPath("/");

    Server jettyServer = new Server(9999);
    jettyServer.setHandler(context);

    ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, "/*");
    jerseyServlet.setInitOrder(0);

    // Tells the Jersey Servlet which REST service/class to load.
    jerseyServlet.setInitParameter("jersey.config.server.provider.packages", "eu.edisonproject.rest");

    try {
      jettyServer.start();
      jettyServer.join();
    } catch (Exception ex) {
      Logger.getLogger(ECO2Server.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
      jettyServer.destroy();
    }
  }
}
