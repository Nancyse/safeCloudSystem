 <%@ page language="java" import="java.util.*" pageEncoding="utf-8" isELIgnored="false"%>
 <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> 
 <%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html>
<head>
<title>目录管理</title>
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="keywords" content="" />
<script type="application/x-javascript"> addEventListener("load", function() { setTimeout(hideURLbar, 0); }, false); function hideURLbar(){ window.scrollTo(0,1); } </script>

<!-- 弹窗效果 -->
<link rel="stylesheet" type="text/css" media="all" href="<%=basePath%>/css/leanModal.style.css">

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
<script src="<%=basePath%>/FileSaver/FileSaver.js" charset="utf-8"></script>
<script src="<%=basePath%>/CryptoJS/crypto-js.js"></script> 

<script type="text/javascript" src="<%=basePath%>/js/jquery.leanModal.min.js"></script>
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
<script>		
		//去掉字符串首位空格
		function trimStr(str){return str.replace(/(^\s*)|(\s*$)/g,"");}
		
		function createEncryptKey(data){
			var index = Math.round(Math.random()*64);
			//("index:"+index);
			var key = data+data.substring(index);
			return key;
		}
		
		function getSha256(data){ //sha256加密
			var hash = CryptoJS.SHA256(data)
			return CryptoJS.enc.Base64.stringify(hash);
		}
		
		function getDAesString(encrypted,key,iv){//解密
			console.info("key:"+key+"\niv:"+iv);
			var key  = CryptoJS.enc.Utf8.parse(key);
			var iv   = CryptoJS.enc.Utf8.parse(iv);
			var decrypted =CryptoJS.AES.decrypt(encrypted,key,
				{
					iv:iv,
					mode:CryptoJS.mode.CBC,
					padding:CryptoJS.pad.Pkcs7
				});
			return decrypted.toString(CryptoJS.enc.Utf8);     
		}
		
		function getDAes(data,key){//解密
			var iv   = 'abcdefghabcdefgh';
			var decryptedStr =getDAesString(data,key,iv);
			return decryptedStr;
		}
		
		function getAesString(data,key,iv){//加密
			var key  = CryptoJS.enc.Utf8.parse(key);
			var iv   = CryptoJS.enc.Utf8.parse(iv);
			var encrypted =CryptoJS.AES.encrypt(data,key,
				{
					iv:iv,
					mode:CryptoJS.mode.CBC,
					padding:CryptoJS.pad.Pkcs7
				});
			return encrypted.toString();    //返回的是base64格式的密文
		}
		
		function getAES(data,key){ //加密			
			var iv   = 'abcdefghabcdefgh';
			var encrypted =getAesString(data,key,iv); //密文
			var encrypted1 =CryptoJS.enc.Utf8.parse(encrypted);
			return encrypted;
		}
		
		//删除文件
		function deleteFile(filename,fileDir,uploader){			
			var formdata = new FormData();	  
			formdata.append("filename",filename);
			formdata.append("filePath",fileDir);
			formdata.append("uploader",uploader);			
			var xhr = new XMLHttpRequest();
			var url="deleteFile";
			xhr.open("POST",url, false);  //同步请求
			xhr.send(formdata);
		}
		
		//搜索文件
		function searchFile(){
			var minNum = $("#minNum").val();
			var maxNum = $("#maxNum").val();
			var dirKeywords = $("#dirKeyword").val();
			var fileType = $("#fileType_id").find("option:selected").text();
			var keywords = $("#keywords").val();
			//alert("minNum:"+minNum+"\n maxNum:"+maxNum+"\n dirKeywords: "+dirKeywords+"\n fileType: "+fileType+"\n keywords: "+keywords);
			var formdata = new FormData();	  
			formdata.append("minNum",minNum);
			formdata.append("maxNum",maxNum);
			formdata.append("dirKeyword",dirKeywords);	
			formdata.append("fileType",fileType);
			formdata.append("keywords",keywords);	
			formdata.append("JESSIONID",window.sess);
			var xhr = new XMLHttpRequest();
			var url="searchFile";
			xhr.open("POST",url, true);  //同步请求
			xhr.send(formdata);			
			
		}
		
		
		//用户下载文件
		function downloadFile(filename,fileDir){			
			var formdata = new FormData();	  
			formdata.append("filename",filename);
			formdata.append("filePath",fileDir);
			formdata.append("JESSIONID",window.sess);
			//获取加密的文件
			var xhr = new XMLHttpRequest();
			var url="downloadfile";
			xhr.open("POST",url, false);  //同步请求
			xhr.send(formdata);
			var encryptData = xhr.responseText;
			
			//获取文件加密的密钥和摘要
			url="http://localhost/safeCloudSystem/server/getFilehashAndKey";
			xhr.open("POST",url, false);
			//xhr.withCredentials = true; //设置传递cookie，如果不需要直接注释就好
			//xhr.setRequestHeader("X-Requested-With", "XMLHttpRequest"); //请求头部，需要服务端同时设置
			//xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
			xhr.send(formdata);	
			var json = xhr.responseText;
			json = eval('('+json+')');
			//解密文件
			var rawData = getDAes(encryptData,trimStr(json.fileKey));
			//alert("rawData:"+rawData);
			//计算哈希值
			var hash = getSha256(rawData);
			if( json.fileHash != hash){
				alert("注意！文件被篡改了！");
			}else{
				//将数据保存到本地文件
				var file = new Blob([rawData], {type: "text/plain;charset=utf-8"});
			    saveAs(file, filename);
			}
			
		}
		
		//var filename = "testuploadStr.txt";
		//var fileDir = "test_dir/";
		
		//downloadFile(filename,fileDir);	   

</script>
	
<!-- tables -->
<link rel="stylesheet" type="text/css" href="<%=basePath%>/css/table-style.css" />
<link rel="stylesheet" type="text/css" href="<%=basePath%>/css/basictable.css" />
<script type="text/javascript" src="<%=basePath%>/js/jquery.basictable.min.js"></script>
<script type="text/javascript">
    $(document).ready(function() {
      $('#table').basictable();

      $('#table-breakpoint').basictable({
        breakpoint: 768
      });

      $('#table-swap-axis').basictable({
        swapAxis: true
      });

      $('#table-force-off').basictable({
        forceResponsive: false
      });

      $('#table-no-resize').basictable({
        noResize: true
      });

      $('#table-two-axis').basictable();

      $('#table-max-height').basictable({
        tableWrapper: true
      });
    });
</script>
<!-- //tables -->
</head>
<body class="dashboard-page" style="background:white;">

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
				<a href="sys-filemanage.html">
					<i class="fa fa-file-text-o nav-icon"></i>
					<span class="nav-text">
					文件管理
					</span>
				</a>
			</li>
			
			<li >
				<a href="sys-dirsmanage.html">
					<i class="fa fa-book nav-icon"></i>
					<span class="nav-text">
					目录管理
					</span>
				</a>
			</li>
			
			<li >
				<a href="sys-usermanage.html">
					<i class="fa fa-user nav-icon"></i>
					<span class="nav-text">
					用户管理
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
			<a href="logout">
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
		<section class="title-bar"  >
			<div class="logo" style="width:400px;">
				<h1><a href="index.html"><img src="../images/logo.png" alt="" />后台管理系统</a></h1>
			</div>
			<!--
			<div class="w3l_search">
				<form action="#" method="post">
					<input type="text" name="search" value="Search" onFocus="this.value = '';" onBlur="if (this.value == '') {this.value = 'Search';}" required="">
					<button class="btn btn-default" type="submit"><i class="fa fa-search" aria-hidden="true"></i></button>
				</form>
			</div>
			-->
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
									
									<c:if test="${sessionScope.userType eq '2' }">
										<li> <a href="sys-filemanage.html"><i class="fa fa-user"></i> 系统管理</a> </li> 
									</c:if>										
									
									<li> <a href="logout"><i class="fa fa-sign-out"></i> 注销</a> </li>
								</ul>
							</li>
						</ul>
					</div>
					<div class="clearfix"> </div>
				</div>
			</div>
			<div class="clearfix"> </div>
		</section>
		
		
		<div class="main-grid" >
			<div class="agile-grids">		
				
				<div class="banner" style="height:50px;">
					<h2>
						<a href="index.html">首页</a>
						<i class="fa fa-angle-right"></i>
						<span>目录管理</span>
					</h2>
				</div>
				
				<div class="asked">
					<div class="w3l-table-info">
					  
					    <table id="table" data-session="<%=session.getId() %>">
						<div style="height:40px;border-bottom:2px solid #EBEBEB;">					
				
								搜索：
								  用户名
								  &nbsp;<input type="text" placeholder="查找用户名"  id="userKeyword" class="input" style="width:120px; line-height:17px;display:inline-block" /> 
								  &nbsp;&nbsp;								 

								专属空间
								  &nbsp;<input type="text" placeholder="查找空间"  id="spaceKeyword" class="input" style="width:120px; line-height:17px;display:inline-block" /> 
								  &nbsp;&nbsp;								  
								路径
								  &nbsp;<input type="text" placeholder="查找路径"  id="dirKeyword" class="input" style="width:120px; line-height:17px;display:inline-block" /> 
								  &nbsp;&nbsp;
								 <a href="#" class="btn btn-info btn-sm " onclick="searchFile()"><span class="icon-search"></span></a>
								
								</td>								
						</div>
						
						<thead>
						  <tr>
							<th>序号</th>
							<th>用户名</th>													
							<th>专属空间</th>						
							<th>已创建的路径</th>
						  </tr>
						</thead>
						<tbody>
						  
						  	<c:set var="num" value="0"/>
						  	<c:forEach var="dir" items="${dirsList}">
						  		<c:set var="num" value="${num+1}"/>
						  		<tr>
						  		<td><c:out value="${num }"/></td>
						  		<td><c:out value="${dir.user_name }"/></td>						  							  		
						  		<td><c:out value="${dir.user_space }"/></td>
						  		<td><c:out value="${dir.file_dir }"/></td>
								</tr>
						  	</c:forEach>
						  	
						  	<tr>
						  		<td><c:out value="1"/></td>
						  		<td><c:out value="lps"/></td>						  							  		
						  		<td><c:out value="lpsSpace/"/></td>
						  		<td><c:out value="test_dir/"/></td>
							</tr>
							
							 <script>	
							 	window.numId;
							 	window.sess = $("#table").attr("data-session");//获取页面的session						
							 	
							 </script>
							 
						  </tr>
						  						  
						
						  <tr align="center">
							<td colspan="8" align="center">
							
								<c:if test="${currentPage == 1 }">
									 <span class="disabled"><< 前一页</span>    
								</c:if>
								<c:if test="${currentPage != 1 }">
									 <a href="sys-dirsmanage?page=${currentPage-1 }"><< 前一页</span>    
								</c:if>
																
						        <span >${currentPage }</span>      
							    
								<c:forEach var="i" begin="${currentPage }" end="${pageTimes }">
									
							        <c:if test="${i != currentPage}">
							             <a href="sys-dirsmanage?page=${i}">${i}</a>
							        </c:if>
							        
								</c:forEach>								
						        <c:if test="${currentPage < pageTimes}">
						        	<a href="sys-dirsmanage?page=${currentPage+1}">后一页 >></a>
						        </c:if> 
						        <c:if test="${currentPage == pageTimes}">
						            	<span class="disabled">后一页 >></span>        
						      	</c:if>
							</td>
						  </tr>
						  
						</tbody>
					  </table>
					</div>


				  
				</div>
				<!-- //tables -->
			</div>
		</div>
		
	</section>
	
	<script src="<%=basePath%>/js/bootstrap.js"></script>
	<script src="<%=basePath%>/js/proton.js"></script>
	
</body>
</html>
