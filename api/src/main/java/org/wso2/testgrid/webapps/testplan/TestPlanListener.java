package org.wso2.testgrid.webapps.testplan;
 
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 *  Creates an EntityManger using the persistence.xml file
 */
public class TestPlanListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent e) {
        EntityManagerFactory emf =
                Persistence.createEntityManagerFactory("eclipse_link_jpa");
        e.getServletContext().setAttribute("emf", emf);
    }

    @Override
    public void contextDestroyed(ServletContextEvent e) {
        EntityManagerFactory emf =
            (EntityManagerFactory) e.getServletContext().getAttribute("emf");
        emf.close();
    }
}
