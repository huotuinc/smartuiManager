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
<script type="text/javascript" src="jquery-easyui/jquery.min.js"></script>
<script type="text/javascript" src="jquery-easyui/jquery.easyui.min.js"></script>
<script type="text/javascript" src="jquery-easyui/jquery.edatagrid.js"></script>
<script type="text/javascript"
	src="jquery-easyui/locale/easyui-lang-zh_CN.js"></script>
<script type="text/javascript"
	src="jquery-easyui/datagrid-detailview.js"></script>
<script type="text/javascript">
	var pageSize2 = 20;
	var pageNumber2 = 1;

		$(function(){

			$('#list-table').edatagrid({
				title:"模板分类管理",
				method:"get",
				url:'widgetType/list',
				toolbar:"#toolbar",
				singleSelect:true,
				pagination : true,//分页
				pageSize : pageSize2,//默认选择的分页是每页5行数据
	            pageList : [ 10, 20, 40 ],//可以选择的分页集合
				columns:[[
					{ field:'href',checkbox:true },
					{field:'name',title:'名字',align:'center',width:100,
			            formatter:function(value,rec){
			            	if(null!=rec)
			            		return rec.name
			            	else
			            		return null;
			            }
					},
					{field:'iconResourceValue',title:'分类',align:'center',width:100,
			            formatter:function(value,rec){
			            	if(null!=rec.iconResource)
			            		return '<img src="'+rec.iconResource.value+'"/>';
			            	else
			            		return null;
			            }
					},
					{field:'scopeValue',title:'范畴',align:'center',width:100,
                        formatter:function(value,rec){
                            if(null!=rec.scope){
                            	if(rec.scope=="global"){
                            	    return "商户";
                            	}else if(rec.scope=="sis"){
                            		return "店中店";
                            	}else if(rec.scope=="system"){
                            		return "系统默认";
                                }
                            }
                            else
                                return null;
                        }
                    },
					{field:'opt',title:'操作',width:100,align:'center',
			            formatter:function(value,rec){
			                var btn = '<a onclick="editRow(\''+rec.id+'\')" href="#" icon="icon-search">更新图片</a>';
			                return btn;
			            }
			        }
				]]
			});

			var p = $('#list-table').datagrid('getPager');
		    $(p).pagination({
		        beforePageText: '第',//页数文本框前显示的汉字
		        afterPageText: '页    共 {pages} 页',
		        displayMsg: '当前显示 {from} - {to} 条记录   共 {total} 条记录',
		        onSelectPage: function (pageNumber, pageSize) {
		        	$.messager.progress({ text: '数据加载中....' });

		        	pageNumber2 = pageNumber;
					pageSize2 = pageSize;
					refresh();
		            $.messager.progress('close');
		         }
		    });
		})

		function refresh(){
			$.ajax({
                type: "get",
                url: 'widgetType/list',
                data: {"page":pageNumber2,"rows":pageSize2},
                async: false,
                success: function (ret) {
                	$("#list-table").datagrid("loadData", ret);
                },
                error: function (ret) {
                    $('#list-table').datagrid('clearSelections');
                }
            });
		}

		var isEdit = false;

		function add(){
			isEdit = false;
			$("#name").val("");
            $("#orderWeight").val("");
			$("#win").window('open');
		}

		function closeWin(){
			$("#win").window('close');
		}

		//新增
		function saveType(){
			if(isEdit){
				var record = $('#list-table').edatagrid('getSelected');
				$("#win").window('close');
				$.ajax({
					url: 'widgetType/update',
	                method:"post",
	                data:{"id":record.id,"name":$("#name").val(),"orderWeight":$("#orderWeight").val(),"scopeName":$("input[name='scopeName']:checked").val()},
	                success: function(result){
	                	if(result.success){
	                		$.messager.alert('提示','编辑成功!','info');
	                		refresh();
		                	$("#win").window('close');
		                	$('#fm').form('clear');
	                	}else{
	                		$.messager.alert('提示','编辑失败!','info');
	                		refresh();
		                	$('#fm').form('clear');
	                	}
	                }
				})
				isEdit = false;
			}else{
				$("#win").window('close');
				$.ajax({
                    url: 'widgetType/add',
                    method:"post",
                    data:{"name":$("#name").val(),"orderWeight":$("#orderWeight").val(),"scopeName":$("input[name='scopeName']:checked").val()},
                    success: function(result){
                        if(result.success){
                        	$.messager.alert('提示','新增成功!','info');
                            refresh();
                            $('#fm').form('clear');
                        }else{
                            $.messager.alert('提示','新增失败!','info');
                            refresh();
                            $('#fm').form('clear');
                        }
                    }
                })
			}

		}

		function edit(){
			var record = $('#list-table').edatagrid('getSelected');
			if(null==record){
				$.messager.alert('提示','请选择一项进行编辑!','info');
				return;
			}
			isEdit = true;
			$("#fm").form('clear');
			$("#win").window('open');
			$("#name").val(record.name);
			$("#orderWeight").val(record.orderWeight);
			if(record.scope=="global"){
				$("#global").prop("checked",true);
			}else if(record.scope=="sis"){
				$("#sis").prop("checked",true);
			}else if(record.scope=="system"){
				$("#system").prop("checked",true);
			}
		}

		function editRow(id){
			$('#fm2').form('clear');
			$("#href2").val(id);
			$("#iconType").val("iconResource");
			$("#win2").window('open');
		}

		function upload(){
			$('#fm2').form('submit',{
                url: 'widgetType/upload',
                method:"post",
                success: function(result){
                	var data = eval('(' + result + ')');
                	if(data.success){
                		$.messager.alert('提示','上传成功!','info');
                		refresh();
                    	$("#win2").window('close');
                    	$('#fm2').form('clear');
                	}else{
                		$.messager.alert('提示','上传失败:'+data.msg,'info');
                	}
                }
            });
		}

		function doSearch(){

		}

		function deleteType(){
			var record = $('#list-table').edatagrid('getSelected');
			if(null==record){
				$.messager.alert('提示','请选择一项进行删除!','info');
				return;
			}
			$.messager.confirm('提示框', '你确定要删除吗?',function(y){
				if(y){
					$.ajax({
						url: 'widgetType/delete',
						method:"post",
		                data:{"id":record.id},
		                success: function(result){
		                	if(result.success){
		                		$.messager.alert('提示','删除成功!','info');
		                		refresh();
		                	}else{
		                		$.messager.alert('提示','删除失败!','info');
		                		refresh();
		                	}
		                }
					})
				}
			})
		}

	</script>
<title>模板分类管理</title>

</head>
<body>
	<table id="list-table"></table>
	<input type="hidden" name="href" id="href" />
	<div id="toolbar">
		<div><a href="javascript:void(0)" class="easyui-linkbutton"
			iconCls="icon-add" plain="true" onclick="javascript:add()">新增</a> <a
			href="javascript:void(0)" class="easyui-linkbutton"
			iconCls="icon-edit" plain="true" onclick="javascript:edit()">编辑</a> <a
			href="javascript:void(0)" class="easyui-linkbutton"
			iconCls="icon-remove" plain="true" onclick="javascript:deleteType()">删除</a>
        </div>
	</div>
	<div id="win" class="easyui-window" title="新增分类" closed="true"
		style="width: 300px; height: 200px;">
		<form id="fm" method="post">
			<table border="0" cellpadding="0" cellspacing="5">
				<tr>
					<td>模板分类名字：</td>
					<td><input type="text" name="name" id="name" /></td>
				</tr>
				<tr>
					<td>排序权重：</td>
					<td><input type="text" name="orderWeight" id="orderWeight" />
					</td>
				</tr>
				<tr>
					<td>范畴：</td>
					<td><input type="radio" name="scopeName" value="global"
						id="global" />商户 <input type="radio" name="scopeName" value="sis"
						id="sis" />店中店 <input type="radio" name="scopeName" value="system"
						id="system" />系统默认</td>
				</tr>
			</table>
		</form>
		<div style="padding: 20px; text-align: center;">
			<a href="#" class="easyui-linkbutton" icon="icon-ok"
				onclick="saveType()">保存</a> <a href="#" class="easyui-linkbutton"
				icon="icon-cancel" onclick="closeWin()">取消</a>
		</div>
	</div>
	<div id="win2" class="easyui-window" title="上传附件" closed="true"
		style="width: 300px; height: 200px; text-align: center;">
		<form id="fm2" method="post" enctype="multipart/form-data">
			<table border="0" cellpadding="0" cellspacing="5">
				<tr>
					<td>上传附件</td>
					<td><input type="hidden" name="id" id="href2" /> <input
						type="hidden" name="type" id="iconType" /> <input type="file"
						name="file" /></td>
				</tr>
			</table>
		</form>
		<div style="padding: 20px; text-align: center;">
			<a href="#" class="easyui-linkbutton" icon="icon-ok"
				onclick="upload()">上传</a> <a href="#" class="easyui-linkbutton"
				icon="icon-cancel" onclick="closeWin2()">取消</a>
		</div>
	</div>
</body>
</html>