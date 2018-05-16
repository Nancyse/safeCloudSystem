<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html>
<head>
<title>Home</title>
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
<script>
        (function () {
          var js;
          if (typeof JSON !== 'undefined' && 'querySelector' in document && 'addEventListener' in window) {
            js = '<%=basePath%>/js/jquery.2.0.3.min.js';
          } else {
            js = '<%=basePath%>/js/jquery.1.10.2.min.js';
          }
          document.write('<script src="' + js + '"><\/script>');
        }());
 </script>
<script src="<%=basePath%>/js/modernizr.js"></script>
<script src="<%=basePath%>/js/jquery.cookie.js"></script>
<script src="<%=basePath%>/js/screenfull.js"></script>
		<script>
		$(function () {
			$('#supported').text('Supported/allowed: ' + !!screenfull.enabled);

			if (!screenfull.enabled) {
				return false;
			}

			

			$('#toggle').click(function () {
				screenfull.toggle($('#container')[0]);
			});	
		});
		</script>
<!-- charts -->
<script src="<%=basePath%>/js/raphael-min.js"></script>
<script src="<%=basePath%>/js/morris.js"></script>
<link rel="stylesheet" href="<%=basePath%>/css/morris.css">
<!-- //charts -->
<!--skycons-icons-->
<script src="<%=basePath%>/js/skycons.js"></script>
<!--//skycons-icons-->
</head>
<body class="dashboard-page">
	<script>
	        var theme = $.cookie('protonTheme') || 'default';
	        $('body').removeClass (function (index, css) {
	            return (css.match (/\btheme-\S+/g) || []).join(' ');
	        });
	        if (theme !== 'default') $('body').addClass(theme);
        </script>
	
	<nav class="main-menu">
		<ul>
			<li>
				<a href="index.html">
					<i class="fa fa-home nav_icon"></i>
					<span class="nav-text">
					首页
					</span>
				</a>
			</li>
			
		
			
			<li>
				<a href="uploadfile.html">
					<i class="icon-font nav-icon"></i>
					<span class="nav-text">
					上传文件
					</span>
				</a>
			</li>
			<li>
				<a href="filedetail.html">
					<i class="icon-table nav-icon"></i>
					<span class="nav-text">
					文件信息
					</span>
				</a>
			</li>
			
			<li >
				<a href="persondetail.html">
					<i class="fa fa-check-square-o nav_icon"></i>
					<span class="nav-text">
					个人信息
					</span>
				</a>
			</li>
			
			
			<li class="has-subnav">
				<a href="commonfaq.html">
					<i class="fa fa-list-ul" ></i>
					<span class="nav-text">常见问题</span>
					
				</a>
				
			</li>
		</ul>
		<ul class="logout">
			<li>
			<a href="login.html">
			<i class="icon-off nav-icon"></i>
			<span class="nav-text">
			注销
			</span>
			</a>
			</li>
		</ul>
	</nav>
	
	
	<section class="wrapper scrollable">
		<nav class="user-menu">
			<a href="javascript:;" class="main-menu-access">
			<i class="icon-proton-logo"></i>
			<i class="icon-reorder"></i>
			</a>
		</nav>
		<section class="title-bar">
			<div class="logo" style="width:400px;">
				<h1><a href="index.html"><img src="<%=basePath%>/images/logo.png" alt="" />安全文件传输系统</a></h1>
			</div>
			
			<div class="header-right">
				<div class="profile_details_left">
					
					<div class="profile_details">		
						<ul>
							<li class="dropdown profile_details_drop">
								<a href="#" class="dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
									<div class="profile_img">	
										<span class="prfil-img"><i class="fa fa-user" aria-hidden="true"></i></span> 
										<div class="clearfix"></div>	
									</div>	
								</a>
								<ul class="dropdown-menu drp-mnu"> 
									<li> <a href="persondetail.html"><i class="fa fa-user"></i> 个人信息</a> </li> 
									<li> <a href="login.html"><i class="fa fa-sign-out"></i> 注销</a> </li>
								</ul>
							</li>
						</ul>
					</div>
					<div class="clearfix"> </div>
				</div>
			</div>
			<div class="clearfix"> </div>
		</section>
		
		
		<div class="main-grid">
			
			<div class="social grid">
					<div class="grid-info">
						<div class="col-md-3 top-comment-grid">
							<div class="comments views">
								<div class="comments-icon">
									<!--<i class="fa fa-comments"></i>-->
									<i class="fa fa-cloud-upload" style="font-size:80px;color:#EBEBEB"></i>
								</div>
								<div class="comments-info">
									<!--<h3>upload</h3>-->
									<br>
									<a href="uploadfile"><font size="5" style="color:#EBEBEB;">文件上传</font></a>
								</div>
								<div class="clearfix"> </div>
							</div>
						</div>
						
						<div class="col-md-3 top-comment-grid">
							<div class="comments tweets">
								<div class="comments-icon">
									<i class="fa fa-cloud-download" style="font-size:80px; color:#EBEBEB"></i>
									
								</div>
								<div class="comments-info">
									<!--<h3>upload</h3>-->
									<br>
									<a href="filedetail.html"><font size="5" style="color:#EBEBEB;">文件下载</font></a>
								</div>
								<div class="clearfix"> </div>
							</div>
						</div>
						
						
						<div class="col-md-3 top-comment-grid">
							<div class="comments ">
								<div class="comments-icon">
									<i class="fa fa-file" style="font-size:80px;color:#EBEBEB"></i>
									
								</div>
								<div class="comments-info">
									<br>
									<a href="filedetail.html"><font size="5" style="color:#EBEBEB;">文件详情</font></a>
								</div>
								<div class="clearfix"> </div>
							</div>
						</div>
						
						<div class="clearfix"> </div>
					</div>
			</div>
			
			
		</div>
		<!-- footer -->
		
		<!-- //footer -->
	</section>
	<script src="<%=basePath%>/js/bootstrap.js"></script>
	<script src="<%=basePath%>/js/proton.js"></script>
	
</body>
</html>
