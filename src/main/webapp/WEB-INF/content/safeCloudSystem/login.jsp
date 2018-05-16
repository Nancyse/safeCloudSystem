<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html>
<head>
<title>Login</title>
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
<body class="signup-body">
		<div class="agile-signup">	
			
			<div class="content2">
				<div class="grids-heading gallery-heading signup-heading">
					<h2>Login</h2>
				</div>
				<form action="index.html" method="post">
					<input type="text" name="Username" value="Username" onFocus="this.value = '';" onBlur="if (this.value == '') {this.value = 'Username';}">
					<input type="password" name="Password" value="Password" onFocus="this.value = '';" onBlur="if (this.value == '') {this.value = 'Password';}">
					<input type="submit" class="register" value="Login">
				</form>
				
			</div>
			
			
		</div>
	
</body>
</html>
