<%@ page isErrorPage = "true"%>
<%@ page import = "java.io.*" %>
{ 
  "status":"Internal ERROR",
  "description":"An exception has occured: <%= exception.toString() %>",
  "id", "500" 
}
