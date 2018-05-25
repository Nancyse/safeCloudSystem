<%@ page language="java" import="java.util.*" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> 
<%
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html>
<head>
<title>个人信息</title>
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
<script src="<%=basePath%>/FileSaver/FileSaver.js" charset="utf-8"></script>
<script src="<%=basePath%>/CryptoJS/crypto-js.js"></script>
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
<script type="text/javascript">
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



</head>
<body class="dashboard-page">

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
		
		
		<div class="main-grid">
			<div class="agile-grids">	
				<!-- input-forms -->
				<div class="grids">
					
					<div class="panel panel-widget forms-panel">
						<div class="forms">
							<div class="form-grids widget-shadow" data-example-id="basic-forms"> 
								<div class="form-title">
									<h4>更新文件 :</h4>
								</div>
								<div id="table" data-session="<%=session.getId() %>" class="form-body" >
									<div class="form-group">
										文件名：<input type="text" disabled="disabled" name="filename"  value="${file.file_name }"/>
									</div>	
									<div class="form-group">
										文件路径：<input type="text" disabled="disabled" name="filePath" value="${file.file_dir }"/>
									</div>
									<div class="form-group">
										文件上传者：<input type="text" disabled="disabled" name="uploader" value="${file.file_uploader }"/>
									</div>
									<div class="form-group"> 
											<label >文件描述： </label>											
											<input type="text" name="description" />
									</div> 																		
									<div class="form-group" > 
										<label>文件:</label>
										<input type="file" name="file" id="username"  class="txtfield" />					
										
									</div>	
									<a href="filedetail.html" class="btn btn-info" onclick="submitNewFile()"><span class="icon-edit"></span>提交</a>
									
								</div>
								
								<script type="text/javascript">
								
							 	window.sess = $("#table").attr("data-session");//获取页面的session							 	 
							 	
							 	function submitNewFile(){							 		
							 		
							 		var filename = $('input[name="filename"').val();							 		
							 		var filePath = $('input[name="filePath"] ').val();							 							 		
							 		var uploader =  $('input[name="uploader"] ').val();
							 		var files = $('input[name="file"]').prop('files');//获取到文件列表
									var desc = $('input[name="description"').val();
									var url = "";
									alert("hello:"+desc+"\nfilename:"+filename+"\nfilePath:"+filePath+"\nuploader: "+uploader);
									//alert("new file: "+files[0].name);
									//读取文件内容
									var reader = new FileReader();//new一个FileReader实例
									reader.onload = function() {
										var rawdata = this.result ; 
										//alert("rawdata:"+rawdata);
										//加密
										var hash = getSha256(rawdata);
										var key = createEncryptKey(hash);										
										encryptdata=getAES(rawdata,key);
										//alert("encryptdata:"+encryptdata);										
										
										var param = 'input[name="filename"] ';
							 			var filename = $(param).val();
										param = 'input[name="filePath"] ';
							 			var filePath = $(param).val();
							 			param = 'input[name="uploader"] ';
							 			var uploader =  $(param).val();
							 			param = 'input[name="description"] ';
							 			var desc = $(param).val();
							 			alert("hello:"+desc+"\nfilename:"+filename+"\nfilePath:"+filePath+"\nuploader"+uploader);
							 			
										//上传加密文件
										var xhr = new XMLHttpRequest();
										var url =  "updateStr";
										var formdata = new FormData();	  
										formdata.append("filePath",filePath);
										formdata.append("filename",filename);
										formdata.append("fileData",encryptdata);
										formdata.append("length",encryptdata.length);
										formdata.append("uploader",uploader);
										formdata.append("JESSIONID",window.sess);
										xhr.open("POST",url, false);
										//xhr.setRequestHeader("X_FILENAME", file.name);
										xhr.send(formdata);											
									    
									    //上传文件密钥和文件摘要
									    var url ="updateFileData"; 
										var formdata2 = new FormData();  
										formdata2.append("description",desc);
										formdata2.append("filePath",filePath);
										formdata2.append("filename",filename);
										formdata2.append("fileKey",key);
										formdata2.append("fileHash",hash);
										formdata2.append("length",encryptdata.length);
										formdata2.append("uploader",uploader);
										formdata2.append("JESSIONID",window.sess);
										xhr.open("POST",url, false);										
										
										xhr.send(formdata2);
										//解密
										//rawdata = getDAes(encryptdata,key) ;
										//alert("rawdata:"+rawdata);										
					                }
					                reader.readAsText(files[0]);									
									return false;
								}
								</script>
							</div>
						</div>
					</div>
					
				</div>
				<!-- //input-forms -->
			</div>
		</div>
		<!-- footer -->
		
		<!-- //footer -->
	</section>
	<script src="<%=basePath%>/js/bootstrap.js"></script>
	<script src="<%=basePath%>/js/proton.js"></script>
</body>
</html>
