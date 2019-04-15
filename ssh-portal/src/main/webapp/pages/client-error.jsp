<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>SSH Key Portal Error Page</title>
  <link rel="stylesheet" type="text/css" href="css/default.css">
  <link rel="stylesheet" type="text/css" href="css/rcauth.css">
</head>

<body>

<table style="width: 100%">
  <tr>
    <td style="width:10%"><img src="css/RCauth-eu-logo-150.gif">
    <td style="width:10%"><div id="topText" class="topText" style="margin-left: 0">MasterPortal</div>
    <td><h1 style="text-align:center">SSH Public Key Upload Portal</h1>
    <td style="width:20%">
  </tr>
</table>

<hr>

<div style="text-align:left; margin: 20px">
<H1>There was a problem:</H1>

<H2 style="border-bottom: 0px">The message received was: </H2><br>

<c:if test="${not empty message}"><B>Message:</B>
<pre>${message}</pre>
<br></c:if><c:if test="${not empty cause}">
<B>Cause:</B>
<pre>${cause}</pre>
<br></c:if><c:if test="${not empty error}">
<B>Error:</B>
<pre>${error}</pre>
<br></c:if><c:if test="${not empty error_description}">
<B>Error description:</B>
<pre>${error_description}</pre>
<br></c:if><c:if test="${not empty state}">
<B>State:</B>
<pre>${state}</pre></c:if>

<br><br>

<form name="input" action="${action}" method="get"/>
<input type="submit" value="Return to client"/>
</form>
</div>

</body>
</html>
