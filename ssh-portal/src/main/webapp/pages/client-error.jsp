<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>SSH Key Portal Error Page</title>
</head>

<body>

<h1 style="text-align:center">Welcome to the SSH Key Portal!</h1>

<H2>There was a problem:</H2>

The message received was: <br>

Message:
<pre>${message}</pre>
<br>
Cause:
<pre>${cause}</pre>
<br>
Error:
<pre>${error}</pre>
<br>
Error description:
<pre>${error_description}</pre>
<br>
State:
<pre>${state}</pre>

<br><br>

<form name="input" action="${action}" method="get"/>
<input type="submit" value="Return to client"/>
</form>

</body>
</html>
