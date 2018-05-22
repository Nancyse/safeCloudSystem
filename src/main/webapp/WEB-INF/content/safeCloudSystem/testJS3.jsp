<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html>
<html>
<head>
	<script src="<%=path%>/CryptoJS/crypto-js.js"></script>
	<script src="<%=path%>/FileSaver/FileSaver.js"></script>
</head>

<body>
jsReadFile:<input type="file" onchange="jsReadFiles(this.files)"/>
<button onclick="jsReadFiles();">read</button>

</body>
<script src="<%=basePath%>/js/jquery.min.js"></script>
<script>
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

	function getAES(data){ //加密
		var key  = 'AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA';  //密钥
		var iv   = '1234567812345678';
		var encrypted =getAesString(data,key,iv); //密文
		var encrypted1 =CryptoJS.enc.Utf8.parse(encrypted);
		return encrypted;
	}

	function getDAes(data){//解密
		var key  = 'AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA';  //密钥
		var iv   = '1234567812345678';
		var decryptedStr =getDAesString(data,key,iv);
		return decryptedStr;
	}
	////上传字符串
	function uploadstr(fileData){
		
		//formdata.append("fileList", file);
		//formdata.append("description","testuploadstr");		
		//formdata.append("filePath","test_dir/");		
		//formdata.append("fileData", fileData);			
			
		//formdata.append("filePath","new_dir/");
		//formdata.append("filename","testHTTPS.txt");
		//formdata.append("length",encryptdata.length);
		var formdata = new FormData();
		formdata.append("description","desc");
		formdata.append("filePath","new_dir/");
		formdata.append("filename","testHTTPS.txt");
		formdata.append("fileKey","1234");
		formdata.append("fileHash","hash");
		formdata.append("length","23");
		
		url = "https://localhost/safeCloudSystem/server/safe/uploadFileData"
		var xhr = new XMLHttpRequest();
		xhr.open("POST",url, false);
		//xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		//xhr.withCredentials = true; 
		//xhr.setRequestHeader("X_FILENAME", file.name);
		xhr.send(formdata);
		
		//接收返回的值
		xhr.onreadystatechange=function(){
			if(xhr.readyState == 4){
				if( xhr.status == 200 ){
					var encryptData = xhr.responseText;
					var rawdata =  getDAes(encryptdata)  ;
					var blob = new Blob([rawdata], {type: "text/plain;charset=utf-8"});
				    saveAs(blob, "文件导出测试.txt");
				}
			}else{
				alert("readystatu:"+xhr.readyState);
			}
			
		};
		
		
	}

	//js 读取文件
    function jsReadFiles(files) {
		if(files==null){
			alert("null");
		}
        if (files.length) {
            var file = files[0];
            var reader = new FileReader();//new一个FileReader实例
            if (/text+/.test(file.type)) {//判断文件类型，是不是text类型
                reader.onload = function() {
					var rawdata = this.result ; 
					var hash = getSha256(rawdata);
					var key = createEncryptKey(hash)
					//alert("hash:"+hash+"\nkey:"+key);
					
					encryptdata=getAES(rawdata);
					uploadstr(encryptdata);
					
                    $('body').append('<pre>' + this.result + '</pre>');
					$('body').append('<pre>' + encryptdata+ '</pre>');
					
					rawdata = getDAes(encryptdata) ;
					$('body').append('<pre>' + rawdata + '</pre>');
                }
                reader.readAsText(file);
            } else if(/image+/.test(file.type)) {//判断文件是不是imgage类型
                reader.onload = function() {
					
                   // $('body').append('<img src="' + this.result + '"/>');
					
                }
                reader.readAsDataURL(file);
            }
        }
    }
</script>
</html>