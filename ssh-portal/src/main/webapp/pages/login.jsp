<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>SSH Key Portal</title>
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
Via this portal you can upload one or more OpenSSH public keys to the
MasterPortal. By using ssh-key authentication you can then retrieve
RCauth.eu-based proxy certificates.
<P>You need to make sure there is a valid longer-lived proxy certificate in the
MasterPortal, e.g. by going once every &pm;10 days to a VO-portal.
<BR>
You can retrieve proxy certificates using
<em>ssh proxy@&lt;SSH HOST&gt;</em>
<BR>
For further options, try
<em>ssh proxy@&lt;SSH HOST&gt; help</em>
<P>
<hr>
<form action="${redirect_host}" method="get">
You need to first <input type="submit" name="submit" value="login"> via the RCauth.eu online CA.
</form>
</div>

</body>
</html>
