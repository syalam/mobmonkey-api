<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" 
"http://www.w3.org/TR/html4/loose.dtd">
<%@ page isErrorPage = "true"%>
<%@ page import = "java.io.*" %>
<HTML>
    <HEAD>
        <TITLE>ERROR!</TITLE>
    </head>
    <body>
       <h2>Your application has generated an error</h2>
       <h3>Please check for the error given below</h3>
       <b>Exception:</b><br> 
       <font color="red"><%= exception.toString() %></font>
    </body>
</html>