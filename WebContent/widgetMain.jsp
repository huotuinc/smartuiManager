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
<script type="text/javascript" src="js/jquery.json.js"></script>
<script type="text/javascript">

	var pageSize2 = 20;
	var pageNumber2 = 1;

	$(function() {

		$('#list-table')
				.edatagrid(
						{
							title : "控件主体管理",
							url : 'widgetMain/list',
							pagination : true,//分页
							pageSize : pageSize2,//默认选择的分页是每页5行数据
							pageList : [ 10, 20, 40 ],//可以选择的分页集合
							method : 'get',
							singleSelect : true,
							rownumbers : true,//行号
							fitColumns : true,
							columns : [ [
									{
										field : 'href',
										checkbox : true
									},
									{
										field : 'name',
										title : '名字',
										align : 'center',
										width : 100,
										formatter : function(value, rec) {
											if (null != rec)
												return rec.name
											else
												return null;
										}
									},
									{
										field : 'type',
										title : '分类',
										align : 'center',
										width : 100,
										formatter : function(value, rec) {
											if (null != rec && null != rec.type)
												return rec.type.name
											else
												return null;
										}
									},
									{
										field : 'browseTemplateResourceValue',
										title : '浏览资源',
										align : 'center',
										width : 100,
										formatter : function(value, rec) {
											if (null != rec.browseTemplateResourceValue)
												return '<a target="_blank" rel="nofollow" href=\''+rec.browseTemplateResourceValue+'\' >浏览资源</a>';
											else
												return null;
										}
									},
									{
										field : 'editTemplateResourceValue',
										title : '编辑资源',
										align : 'center',
										width : 100,
										formatter : function(value, rec) {
											if (null != rec.editTemplateResourceValue)
												return '<a target="_blank" rel="nofollow" href=\''+rec.editTemplateResourceValue+'\' >编辑资源</a>';
											else
												return null;
										}
									},
									/*{field:'thumbnailResource',title:'缩略图资源',width:200}, */
									{
										field : 'description',
										title : '备注',
										width : 300,
										formatter : function(value, rec) {
											if (null != rec)
												return rec.description
											else
												return null;
										}
									},
									{
										field : 'opt',
										title : '操作',
										width : 100,
										align : 'center',
										formatter : function(value, rec) {
											var btn = '<a href="javascript:openWin(\''
													+ rec.href
													+ '\')"  >上传资源</a>';
											return btn;
										}
									},
									{
										field : 'opt2',
										title : '操作',
										width : 100,
										align : 'center',
										formatter : function(value, rec) {
											var btn = '<a href="javascript:updateTemplate(\''
												+ rec.href
												+ '\')"  >更改资源</a>';
											return btn;
										}
									} ] ]
						});

		$('#list-table')
				.edatagrid(
						{
							view : detailview,
							singleSelect : true,
							pagination : true,//分页
							pageSize : pageSize2,//默认选择的分页是每页5行数据
							pageList : [ 10, 20, 40 ],//可以选择的分页集合
							toolbar : "#toolbar",
							detailFormatter : function(index, row) {
								return '<div id="detailForm-'+index+'"></div>';
							},
							onExpandRow : function(index, row) {
								var href = row.href;
								$('#detailForm-' + index).panel(
										{
											doSize : true,
											border : false,
											cache : false,
											href : 'widgetMain/view?href='
													+ href,
											onLoad : function() {
												$('#list-table').edatagrid(
														'fixDetailRowHeight',
														index);
												$('#list-table').edatagrid(
														'selectRow', index);
											}
										});
								$('#list-table').edatagrid(
										'fixDetailRowHeight', index);
							}
						});

		var p = $('#list-table').datagrid('getPager');
		$(p).pagination({
			beforePageText : '第',//页数文本框前显示的汉字
			afterPageText : '页    共 {pages} 页',
			displayMsg : '当前显示 {from} - {to} 条记录   共 {total} 条记录',
			onSelectPage : function(pageNumber, pageSize) {
				$.messager.progress({
					text : '数据加载中....'
				});
				pageNumber2 = pageNumber;
				pageSize2 = pageSize;
				refresh();
				$.messager.progress('close');
			}
		});
	})

	var isEdit = false;

	function refresh(){
		$.ajax({
			type : "get",
			url : 'widgetMain/list',
			data : {
				"page" : pageNumber2,
				"rows" : pageSize2
			},
			async : false,
			success : function(ret) {
				$("#list-table").datagrid("loadData", ret);
			},
			error : function(ret) {
				$('#list-table').datagrid('clearSelections');
			}
		});
	}

	function add() {
		$("#win").panel({
			title : "新增控件主体"
		});
		$('#fm').form('clear');
		$('#widgetType').combobox({
			url : 'widgetType/list2',
			method : "get",
			hiddenName : 'href',
			valueField : 'href',
			textField : 'name',
			required : true,
			editable : false
		});
		isEdit = false;
		$("#win").window('open');
	}

	function closeWin() {
		$("#win").window('close');
		$('#fm').form('clear');
	}

	function saveMain() {
		$("#addMainButton").linkbutton("disable");
		if (isEdit) {
			var record = $('#list-table').edatagrid('getSelected');
			$.ajax({
				url : 'widgetMain/update',
				method : "post",
				data : {
					"href" : record.href,
					"name" : $("#name").val(),
					"description" : $("#description").val(),
					"orderWeight" : $("#orderWeight").val(),
					"type" : $("#widgetType").combobox('getValue'),
					"properties" : $("#properties").textbox('getValue')
				},
				success : function(result) {
					if (result.success) {
						$.messager.alert('提示', '编辑成功!', 'info');
						refresh();
						$('#fm').form('clear');
						$("#win").window('close');
						$("#addMainButton").linkbutton("enable");
						isEdit = false;
					} else {
						$.messager.alert('提示', '编辑失败:' + result.msg, 'info');
						$("#addMainButton").linkbutton("enable");
						isEdit = true;
					}
				},
				error:function(){
                    $.messager.alert('提示', '编辑异常', 'info');
                    $("#addMainButton").linkbutton("enable");
                    isEdit = true;
                }
			})
		} else {
			$.ajax({
				url : 'widgetMain/insert',
				method : "post",
				data : {
					"name" : $("#name").val(),
					"description" : $("#description").val(),
					"orderWeight" : $("#orderWeight").val(),
					"type" : $("#widgetType").combobox('getValue'),
					"properties" : $("#properties").textbox('getValue')
				},
				success : function(result) {
					if (result.success) {
						$.messager.alert('提示', '新增成功!', 'info');
						refresh();
						$('#fm').form('clear');
						$("#win").window('close');
						$("#addMainButton").linkbutton("enable");
					} else {
						$.messager.alert('提示', '新增失败：' + result.msg, 'info');
						$("#addMainButton").linkbutton("enable");
					}
				},
				error:function(){
					$.messager.alert('提示', '新增异常', 'info');
					$("#addMainButton").linkbutton("enable");
	            }
			});
		}
	}

	function openWin(href) {
		$('#fm2').form('clear');
		$("#path").val(href);
		$("#win2").window('open');
	}

	function upload() {
		$("#href").val($("#path").val());
		$('#fm2').form('submit', {
			url : 'widgetMain/upload',
			method : "post",
			success : function(result) {
				var data = eval('(' + result + ')');
				if (data.success) {
					$.messager.alert('提示', '上传成功!', 'info');
					refresh();
					$('#fm2').form('clear');
					$("#win2").window('close');
				} else {
					$.messager.alert('提示', data.msg, 'info');
				}
			}
		});
	}

	function closeWin2() {
		$("#win2").window('close');
	}

	function edit() {
		$('#fm').form('clear');
		$("#win").panel({
			title : "编辑控件主体"
		});
		isEdit = true;
		var record = $('#list-table').edatagrid('getSelected');
		if (null == record) {
			$.messager.alert('提示', '请选择一项进行操作!', 'info');
			return;
		}
		$('#widgetType').combobox({
			url : 'widgetType/list2',
			method : "get",
			hiddenName : 'href',
			valueField : 'href',
			textField : 'name',
			required : true,
			editable : false
		});
		$("#name").textbox('setValue', record.name);
		$("#description").val(record.description);
		$("#orderWeight").val(record.orderWeight);
		$("#widgetType").combobox('setValue', record.typeHref);
		if (null != record.properties)
			$("#properties").textbox('setValue', $.toJSON(record.properties));
		$("#win").window('open');
	}

	function deleteMain() {
		var record = $('#list-table').edatagrid('getSelected');
		if (null == record) {
			$.messager.alert('提示', '请选择一项进行删除!', 'info');
			return;
		}
		$.messager.confirm('提示框', '你确定要删除吗?', function(y) {
			if (y) {
				$.ajax({
					url : 'widgetMain/delete',
					method : "post",
					data : {
						"href" : record.href
					},
					success : function(result) {
						if (result.success) {
							$.messager.alert('提示', '删除成功!', 'info');
							refresh();
						} else {
							$.messager
									.alert('提示', '删除失败:' + result.msg, 'info');
							refresh();
						}
					}
				})
			}
		})
	}

	function changeType() {
		$("#content").textbox('setValue', "");
		$("#result").textbox('setValue', "");
		var record = $('#list-table').edatagrid('getSelected');
		if(null!=record.properties){
			$("#properties2").textbox('setValue', $.toJSON(record.properties));
		}
		$.ajax({
			url : 'widgetMain/getTemplate',
			method : "get",
			data : {
				"href" : $("#path").val(),
				"type" : $(":input[name=tempLateType]:checked").val()
			},
			success : function(result) {
				if (result.success)
					$("#content").textbox('setValue', result.data);
				else
					$.messager.alert('提示', result.msg, 'info');
			}
		})
	}

	function updateTemplate(href) {
		$("#fm3").form("clear");
		if(null!=href)
			$("#path").val(href);
			//$("#properties2").textbox('setValue', $.toJSON(properties));
		$("#win3").window('open');
	}

	function closeWin3() {
		$("#win3").window('close');
	}

	function saveTemplate() {
		$("#templateButton").linkbutton("disable");
		$.ajax({
			url : 'widgetMain/saveTemplate',
			method : "post",
			data : {
				"href" : $("#path").val(),
				"type" : $(":input[name=tempLateType]:checked").val(),
				"content" : $("#content").textbox('getValue')
			},
			success : function(result) {
				if (result.success) {
					refresh();
					$.messager.alert('提示', "更新成功!", 'info');
					$("#win3").window('close');
					$("#fm3").form("clear");
					$("#templateButton").linkbutton("enable");
				} else{
					$.messager.alert('提示', result.msg, 'info');
					$("#templateButton").linkbutton("enable");
				}
			},
			error:function(){
				$.messager.alert('提示', "更新模板异常", 'info');
                $("#templateButton").linkbutton("enable");
			}
		})
	}

	function previewTemplate() {
		$("#result").textbox('setValue', "");
		$.ajax({
			url : 'widgetMain/briefView',
			method : "post",
			data : {
				"href" : $("#path").val(),
				"type" : $(":input[name=tempLateType]:checked").val(),
				"properties" : $("#properties2").textbox('getValue')
			},
			success : function(result) {
				if (result.success) {
					$("#result").textbox('setValue',result.data);
				} else
					$.messager.alert('提示', result.msg, 'info');
			}
		})
	}
</script>
<title>控件主体管理</title>

</head>
<body>
	<table id="list-table"></table>
	<input type="hidden" id="path" />
	<div id="toolbar">
		<a href="javascript:void(0)" class="easyui-linkbutton"
			iconCls="icon-add" plain="true" onclick="javascript:add()">新增</a> <a
			href="javascript:void(0)" class="easyui-linkbutton"
			iconCls="icon-edit" plain="true" onclick="javascript:edit()">编辑</a> <a
			href="javascript:void(0)" class="easyui-linkbutton"
			iconCls="icon-remove" plain="true" onclick="javascript:deleteMain()">删除</a>
	</div>

	<div id="win" class="easyui-window" title="新增控件主体" closed="true"
		style="width: 700px; height: 400px;">
		<form id="fm" method="post">
			<table border="0" cellpadding="0" cellspacing="5">
				<tr>
					<td>控件主体名字：</td>
					<td><input type="text" name="name" id="name"
						class="easyui-textbox" required="true" /></td>
				</tr>
				<tr>
                    <td>排序权重：</td>
                    <td><input type="text" name="orderWeight" id="orderWeight" />
                    </td>
                </tr>
				<tr>
					<td>备注：</td>
					<td><input type="text" name="description" id="description" />
					</td>
				</tr>
				<tr>
					<td>分类：</td>
					<td><input name="type" class="easyui-combobox" id="widgetType" />
					</td>
				</tr>
				<tr>
					<td>属性:</td>
					<td><input class="easyui-textbox"
						data-options="multiline:true" name="properties" id="properties"
						style="width: 400px; height: 200px"></td>
				</tr>
			</table>
		</form>
		<div style="padding: 20px; text-align: center;">
			<a href="#" id="addMainButton" class="easyui-linkbutton" icon="icon-ok"
				onclick="saveMain()">保存</a> <a href="#" class="easyui-linkbutton"
				icon="icon-cancel" onclick="closeWin()">取消</a>
		</div>
	</div>
	<div id="win2" class="easyui-window" title="上传附件" closed="true"
		style="width: 400px; height: 250px;">
		<form id="fm2" method="post" enctype="multipart/form-data">
			<table border="0" cellpadding="0" cellspacing="5">
				<tr>
					<td>上传资源：</td>
					<td><input type="file" name="file" /> <input type="hidden"
						name="href" id="href" /></td>
				</tr>
				<tr>
					<td>文件类型:</td>
					<td><input type="radio" name="type"
						value="editTemplateResource">编辑</input> <input type="radio"
						name="type" value="browseTemplateResource">浏览</input> <input
						type="radio" name="type" value="thumbnailResource">缩略</input></td>
				</tr>
			</table>
		</form>
		<div style="padding: 20px; text-align: center;">
			<a href="#" class="easyui-linkbutton" icon="icon-ok"
				onclick="upload()">保存</a> <a href="#" class="easyui-linkbutton"
				icon="icon-cancel" onclick="closeWin2()">取消</a>
		</div>
	</div>

	<div id="win3" class="easyui-window" title="更改资源" closed="true"
		style="width: 800px; height: 500px;">
		<form id="fm3" method="post">
			<table border="0" cellpadding="0" cellspacing="5">
				<tr>
					<td>文件类型:</td>
					<td><input type="radio" name="tempLateType"
						value="editTemplateResource" onclick="changeType()">编辑</input> <input
						type="radio" name="tempLateType" value="browseTemplateResource"
						onclick="changeType()">浏览</input></td>
				</tr>
				<tr>
					<td>内容:</td>
					<td><input class="easyui-textbox"
						data-options="multiline:true" name="content" id="content"
						style="width: 600px; height: 250px"></td>
				</tr>
				<tr>
					<td>属性:</td>
					<td><input class="easyui-textbox"
						data-options="multiline:true" name="properties" id="properties2"
						style="width: 600px; height: 250px"></td>

				</tr>
				<tr>
					<td>预览:</td>
					<td><input class="easyui-textbox"
						data-options="multiline:true" name="result" id="result"
						style="width: 600px; height: 250px"></td>
				</tr>
			</table>
		</form>
		<div style="padding: 20px; text-align: center;">
			<a href="#" class="easyui-linkbutton" icon="icon-ok"
				onclick="previewTemplate()">预览</a>
			<a href="#"
				class="easyui-linkbutton" icon="icon-ok" id="templateButton" onclick="saveTemplate()">保存</a>
			<a href="#" class="easyui-linkbutton" icon="icon-cancel"
				onclick="closeWin3()">取消</a>
		</div>
	</div>
</body>
</html>