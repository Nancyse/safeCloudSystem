<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <title></title>
    <script src="<%=basePath%>/FileSaver/FileSaver.js" charset="utf-8"></script>
    <script src="<%=basePath%>/CryptoJS/crypto-js.js"></script> 
    <script type="text/javascript">
     <!-- var blob = new Blob(["欢迎访问 hangge.com"], {type: "text/plain;charset=utf-8"});
      saveAs(blob, "文件导出测试.txt");-->
    </script>
 
    
	<script>		
		function getSha256(data){ //sha256加密
			var hash = CryptoJS.SHA256(data)
			return CryptoJS.enc.Base64.stringify(hash);
		}
		
		function getDAesString(encrypted,key,iv){//解密
			
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
		
		
		//用户下载文件
		function downloadFile(filename,fileDir){				
			var formdata = new FormData();	  
			formdata.append("filename",filename);
			formdata.append("filePath",fileDir);
			//获取加密的文件
			var xhr = new XMLHttpRequest();
			var url="downloadfile";
			xhr.open("POST",url, false);  //同步请求
			xhr.send(formdata);
			var encryptData = xhr.responseText;
			console.info("encryptData:\n"+encryptData);
			//获取文件加密的密钥和摘要
			url="http://localhost/safeCloudSystem/server/getFilehashAndKey";
			xhr.open("POST",url, false);
			xhr.send(formdata);	
			var json = xhr.responseText;
			json = eval('('+json+')');
			console.info("key:\n"+json.fileKey);
			console.info("hash:\n"+json.fileHash);
			//解密文件
			var rawData = getDAes(encryptData,json.fileKey);
			alert("rawData:"+rawData);
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
		
		var filename = "testUpload(1).txt";
		var fileDir = "new_dir/";
		
		downloadFile(filename,fileDir);	   

	</script>

  </head>
  <body>
  </body>
</html>