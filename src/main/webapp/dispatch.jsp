<%@ page language="java" contentType="text/html; charset=US-ASCII"
    pageEncoding="US-ASCII"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
<title>Dispatch Livestreaming Camera</title>
</head>
<body>
<center>
<h1>Fill out the form entirely</h1>
<form id="dispatch" action="livestreaming" method="post">
    <p>
        MAC: <input name="mac"><br>
        OAK: <input name="oak"><br>
        <input type="submit" value="Submit">
    </p>
    <p>Result: <span id="result">${resp}</span></p>
</form>
</center>
</body>
</html>