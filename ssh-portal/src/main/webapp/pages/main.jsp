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

<form action="${redirect_host}" method="get">
  <table style="table-layout: fixed;width: 100%;">
    <tr style="vertical-align:center">
      <td style="width: 2.5%">
      <td style="text-align:left">
        <c:if test="${not empty display_name}"><I><B>${display_name}</B></I><br></c:if>
        <c:if test="${not empty idp_display_name}"><I><B>${idp_display_name}</B></I><br></c:if>
        <c:if test="${not empty username}"><I>${username}</I></c:if>
      <td style="text-align:right"><input type="submit" name="submit" value="logout">
      <td style="width: 2.5%">
    </tr>
  </table>
</form>

<hr>

<div style="width: 100%">

<c:if test="${not empty ssh_keys}">
<form action="${redirect_host}" method="post" enctype="multipart/form-data">
  <div style="width: 100%;margin-bottom: 20px;  display: inline-block">
    <table style="table-layout: fixed;margin:auto;width: 95%;border: 1px solid black; background-color: #E0E0E0;">
      <tr style="background-color: #F0F0F0;">
        <th style="width:30px;">
        <th style="word-wrap:break-word;overflow: hidden;width: 10%;border: 1px solid black;">Label
        <th style="word-wrap:break-word;overflow: hidden;max-width: 55%;border: 1px solid black;">Public key
        <th style="word-wrap:break-word;overflow: hidden;width: 20%;border: 1px solid black;">Description
      </tr>

      <c:set var="first" value="true"/>
      <c:forEach items="${ssh_keys}" var="map">
      <tr style="text-align:center;word-wrap:break-word;border: 1px solid black;">
        <td style="border: 1px solid black;">
          <input type="radio" name="label" value="${map.label}"<c:if test="${first == true}"><c:set var="first" value="false"/> checked</c:if>>
        <td style="border: 1px solid black;">
          ${map.label}
        <td style="border: 1px solid black;">
          <div style="text-align:left;max-height:70px; overflow:auto">
            ${map.pub_key}
          </div>
        <td style="border: 1px solid black;">
          <c:if test="${map.description != null}">
          ${map.description}
          </c:if>
      </tr>
      </c:forEach>
    </table>
  </div>
</c:if>

<c:if test="${not empty ssh_keys}">
  <div style="width: 50%;display: inline-block; vertical-align:top; float: right">
    <table style="margin:auto;width: 90%;text-align:left;border: 1px solid black; background-color: #E0E0E0;">
      <tr>
        <th style="width:20%;height:0pt">
        <td>
      <tr><td colspan="2" style="text-align:center;font-size:x-large;padding:10pt;border: 1px solid black; background-color: #F0F0F0;"><B><I>Update selected SSH Key</I></B>
      <tr>
        <th style="width:20%">Upload <I>updated</I> SSH public key
        <td><input type="file" name="pubkey_file">
      <tr>
        <th style="width:20%">or enter value of <I>updated</I> public key
        <td><textarea style="width:99%" rows="4" name="pubkey_value"></textarea>
      <tr>
        <th style="width:20%">Specify description <I>updated</I> key
        <td><input style="width:90%" type="text" name="description">
      <tr>
        <td colspan="2">
        <table style="width:100%"><tr style="text-align:center">
          <td><input type="submit" name="submit" value="update selected key">
          <td><input type="submit" name="submit" value="remove selected key">
        </table>
    </table>
  </div>
</form>
</c:if>

  <div style="width: 50%; display: inline-block; float: left">
    <form action="${redirect_host}" method="post" enctype="multipart/form-data">
      <table style="margin:auto;width: 90%;text-align:left;border: 1px solid black; background-color: #E0E0E0;">
        <tr>
          <th style="width:20%;height:0pt">
          <td>
        <tr><td colspan="2" style="text-align:center;font-size:x-large;padding:10pt;border: 1px solid black; background-color: #F0F0F0;"><B><I>Add new SSH Key</I></B>
        <tr>
          <th style="width:20%">Upload <I>new</I> SSH public key
          <td><input type="file" name="pubkey_file">
        <tr>
          <th>or enter value of <I>new</I> public key
          <td><textarea style="width:99%" rows="4" name="pubkey_value"></textarea>
        <tr>
          <th>Specify label for <I>new</I> key (optional)
          <td><input type="text" name="label">
        <tr>
          <th>Specify description <I>new</I> key (optional)
          <td><input style="width:90%" type="text" name="description">
        <tr>
          <td colspan="2">
          <table style="width:100%"><tr style="text-align:center">
            <td><input type="submit" name="submit" value="add new public key">
          </table>
      </table>
    </form>
  </div>
</div>

</body>
</html>
