 <%@ page language="java" import="java.util.*" pageEncoding="utf-8" isELIgnored="false"%>
 <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> 
<%
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html>
<head>
<title>Error</title>
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="keywords" content="" />
<script type="application/x-javascript"> addEventListener("load", function() { setTimeout(hideURLbar, 0); }, false); function hideURLbar(){ window.scrollTo(0,1); } </script>
<!-- bootstrap-css -->
<link rel="stylesheet" href="<%=basePath%>/css/bootstrap.css">
<!-- //bootstrap-css -->
<!-- Custom CSS -->
<link href="<%=basePath%>/css/style.css" rel='stylesheet' type='text/css' />
<!-- font CSS -->
<link href='http://fonts.useso.com/css?family=Roboto:400,100,100italic,300,300italic,400italic,500,500italic,700,700italic,900,900italic' rel='stylesheet' type='text/css'>
<!-- font-awesome icons -->
<link rel="stylesheet" href="<%=basePath%>/css/font.css" type="text/css"/>
<link href="<%=basePath%>/css/font-awesome.css" rel="stylesheet"> 
<!-- //font-awesome icons -->
</head>
<body class="error-body">
		<div class="agile-signup">	
			
			<div class="error-page">
				<img src="<%=basePath%>/images/error.png" alt="">
			</div>
			
			<div class="go-back">
				<a href="index.html">Back To Home</a>
			</div>
			
			<!-- footer -->
			<div class="copyright">
				<p>Copyright &copy; 2016.Company name All rights reserved.<a target="_blank" href="http://down.admin5.com/">A5 源码</a></p>
			</div>
			<!-- //footer -->
		</div>
	
</body>
</html>
