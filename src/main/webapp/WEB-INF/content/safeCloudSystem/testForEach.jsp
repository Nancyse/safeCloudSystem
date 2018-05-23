<%@ page language="java" import="java.util.*" pageEncoding="utf-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> 
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%> 
<%
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<html>
<head>
</head>
<body>
<c:out value="${fileList.get(0).file_name }"/>
<c:forEach var="file" items="${fileList }">
	<c:out value="filename=${file.file_name }"/><br>
</c:forEach>
	
</body>
</html>
