<%@page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@page import="java.util.*, org.wso2.testgrid.common.TestPlan"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <title>TestGrid Web Application</title>
    </head>

    <body>

        <hr> <%
            @SuppressWarnings("unchecked")
            List<String> testPlans = (List<String>)request.getAttribute("testPlans");
            for (String testPlan : testPlans) { %>
                <legend> <%= testPlan %> </legend> <%
            } %>
        <hr>

        <iframe src="http://www.objectdb.com/pw.html?web-download"
            frameborder="0" scrolling="no" width="100%" height="30"></iframe>
     </body>
 </html>