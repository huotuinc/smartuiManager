<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link rel="stylesheet" type="text/css"
	href="jquery-easyui/themes/default/easyui.css" />
<link rel="stylesheet" type="text/css"
	href="jquery-easyui/themes/icon.css" />
<script type="text/javascript"
	src="jquery-easyui/jquery.min.js"></script>
<script type="text/javascript"
	src="jquery-easyui/jquery.easyui.min.js"></script>
	<script type="text/javascript"
	src="jquery-easyui/jquery.edatagrid.js"></script>
	<script type="text/javascript"
	src="jquery-easyui/locale/easyui-lang-zh_CN.js"></script>
<script type="text/javascript"
	src="jquery-easyui/datagrid-detailview.js"></script>
<title>登录</title>
<script type="text/javascript">
</script>
</head>
<body>

	<form name='loginForm' action="login" method='POST'>
		<table border="0"   cellpadding="0"   cellspacing="5">
    			<tr>
    				<td>user:</td>
    				<td>
    	 				<input type="text"name="user" id="user"></input>
    	 			</td>
    			</tr>
    			<tr>
    				<td>appkey:</td>
    				<td>
    	 				<input type="text"name="appkey" id="appkey"></input>
    	 			</td>
    			</tr>
    			<tr>
                    <td>apiRoot:</td>
                    <td>
                        <input type="text"name="apiRoot" id="apiRoot"></input>
                    </td>
                </tr>
    			<tr>
                     <td colspan='2'><input name="submit" type="submit"
                         value="submit" /></td>
                 </tr>
    		</table>
	</form>
</body>
</html>