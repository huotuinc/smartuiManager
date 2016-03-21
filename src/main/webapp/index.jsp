<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link rel="stylesheet" type="text/css"
	href="jquery-easyui/themes/default/easyui.css" />
<link rel="stylesheet" type="text/css"
	href="jquery-easyui/themes/icon.css" />
<script type="text/javascript" src="jquery-easyui/jquery.min.js"></script>
<script type="text/javascript" src="jquery-easyui/jquery.easyui.min.js"></script>
<script type="text/javascript"
	src="jquery-easyui/locale/easyui-lang-zh_CN.js"></script>
<link rel="stylesheet" href="css/style.css" type="text/css" />
<title></title>
<style>
</style>
<script type="text/javascript">
	$(function() {
		//动态菜单数据
		var treeData = [ {
			text : "菜单",
			children : [ {
				text : "控件主体",
				attributes : {
					url : "widgetMain.jsp"
				}
			}, {
				text : "控件分类",
				attributes : {
					url : "widgetType.jsp"
				}
			}, {
				text : "必需配置",
				attributes : {
					url : "login.jsp"
				}
			} ]
		} ];

		//实例化树形菜单
		$("#tree").tree({
			data : treeData,
			lines : true,
			onClick : function(node) {
				if (node.attributes) {
					Open(node.text, node.attributes.url);
				}
			}
		});
		//在右边center区域打开菜单，新增tab
		function Open(text, url) {
			if ($("#tabs").tabs('exists', text)) {
				$('#tabs').tabs('select', text);
			} else {
				$('#tabs')
						.tabs(
								'add',
								{
									title : text,
									closable : true,
									content : '<iframe width="100%" height="100%" frameborder="0"  src="'
											+ url
											+ '" style="width:100%;height:100%;"></iframe>'
								});
			}
		}

		//绑定tabs的右键菜单
		$("#tabs").tabs({
			onContextMenu : function(e, title) {
				e.preventDefault();
				$('#tabsMenu').menu('show', {
					left : e.pageX,
					top : e.pageY
				}).data("tabTitle", title);
			}
		});

		//实例化menu的onClick事件
		$("#tabsMenu").menu({
			onClick : function(item) {
				CloseTab(this, item.name);
			}
		});

		//几个关闭事件的实现
		function CloseTab(menu, type) {
			var curTabTitle = $(menu).data("tabTitle");
			var tabs = $("#tabs");

			if (type === "close") {
				tabs.tabs("close", curTabTitle);
				return;
			}

			var allTabs = tabs.tabs("tabs");
			var closeTabsTitle = [];

			$.each(allTabs, function() {
				var opt = $(this).panel("options");
				if (opt.closable && opt.title != curTabTitle
						&& type === "Other") {
					closeTabsTitle.push(opt.title);
				} else if (opt.closable && type === "All") {
					closeTabsTitle.push(opt.title);
				}
			});

			for (var i = 0; i < closeTabsTitle.length; i++) {
				tabs.tabs("close", closeTabsTitle[i]);
			}
		}
	});
</script>
</head>
<body>
	<div class="easyui-layout" data-options="fit:true,border:false">
		<div data-options="region:'north',border:false" class="header">
			<div class="left">
				<div align="center">
					<img src="images/header-logo.jpg" />
				</div>
			</div>
		</div>
		<div data-options="region:'center',border:false" class="inner-content">
			<div class="easyui-tabs" fit="true" border="false" id="tabs">
				<div title="首页"></div>
			</div>
		</div>
		<div data-options="region:'west',split:true,border:false"
			class="sidebar">
			<div class="easyui-layout" data-options="fit:true,border:false">
				<div class="sidebar-top" data-options="region:'north',border:false">系统菜单</div>
				<div class="sidebar-menu"
					data-options="region:'center',border:false">
					<ul id="tree" style="height: 100%;"></ul>
				</div>
			</div>
		</div>
		<div data-options="region:'south',border:false" class="inner-footer">huotu</div>
		<div id="tabsMenu" class="easyui-menu" style="width: 120px;">
			<div name="close">关闭</div>
			<div name="Other">关闭其他</div>
			<div name="All">关闭所有</div>
		</div>
	</div>
</body>
</html>
