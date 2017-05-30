<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="org.sshkeyportal.servlet.SSHKeyMainServlet" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>SSH Key Portal</title>
</head>

<body>

<h1>Welcome to the SSH Key Portal!</h1>

<br>

<form action="${redirect_host}" method="post" enctype="multipart/form-data">
<c:if test="${not empty ssh_keys}">
<table style="table-layout: fixed;width: 100%;border: 1px solid black;">
<tr>
<th style="width: 2%">
<th style="word-wrap:break-word;overflow: hidden;width: 8%;border: 1px solid black;">Label
<th style="word-wrap:break-word;overflow: hidden;width: 30%;border: 1px solid black;">Username
<th style="word-wrap:break-word;overflow: hidden;max-width: 50%;border: 1px solid black;">Public key
<th style="word-wrap:break-word;overflow: hidden;width: 10%;border: 1px solid black;">Description
</tr>

<c:set var="first" value="true"/>
<c:forEach items="${ssh_keys}" var="map">
   <tr style="text-align:center;word-wrap:break-word;border: 1px solid black;">
   	<td style="border: 1px solid black;">
	<input type="radio" name="label" value="${map.label}"<c:if
	  test="${first == true}"><c:set var="first" value="false"/> checked</c:if>>
	<td style="border: 1px solid black;">
        ${map.label}
	<td style="border: 1px solid black;">
        ${map.username}
	<td style="text-align:left;border: 1px solid black;">
        ${map.pub_key}
	<td style="border: 1px solid black;">
        <c:if test="${map.description != null}">
        ${map.description}
	</c:if>
    </tr>
</c:forEach>
</table>

<P>
<table style="text-align:left;table-layout: fixed;width: 70%;border: 1px solid black;">
  <tr>
    <th style="width:20%;height:0pt">
    <td>
  <tr><td colspan="2" style="text-align:center;font-size:x-large;padding:10pt;border: 1px solid black;"><B><I>Update selected SSH Key</I></B>
  <tr>
    <th style="width:20%">Upload updated SSH public key
    <td><input type="file" name="pubkey_file">
  <tr>
    <th style="width:20%">or enter value of updated public key
    <td><textarea rows="4" cols="50" name="pubkey_value"></textarea>
  <tr>
    <th style="width:20%">Specify description updated key
    <td><input type="text" name="description">
  <tr>
    <td colspan="2">
    <input type="submit" name="action" value="update selected key">
    <input type="submit" name="action" value="remove selected key">
</table>
</form>

<BR><BR>
</c:if>

<form action="${redirect_host}" method="post" enctype="multipart/form-data">
<table style="text-align:left;table-layout: fixed;width: 70%;border: 1px solid black;">
  <tr>
    <th style="width:20%;height:0pt">
    <td>
  <tr><td colspan="2" style="text-align:center;font-size:x-large;padding:10pt;border: 1px solid black;"><B><I>Add new SSH Key</I></B>
  <tr>
    <th style="width:20%">Upload new SSH public key
    <td><input type="file" name="pubkey_file">
  <tr>
    <th>or enter value of new public key
    <td><textarea rows="4" cols="50" name="pubkey_value"></textarea>
  <tr>
    <th>Specify label for new key (optional)
    <td><input type="text" name="label">
  <tr>
    <th>Specify description new key (optional)
    <td><input type="text" name="description">
  <tr>
    <td>
    <input type="submit" name="action" value="add new public key">
</table>
</form>

</body>
</html>
