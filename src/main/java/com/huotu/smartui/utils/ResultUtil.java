package com.huotu.smartui.utils;

import org.springframework.ui.ModelMap;

public class ResultUtil {
	
	public static ModelMap success(){
		return ResultUtil.success(null, null);
	}
	
	public static ModelMap success(Object object){
		return ResultUtil.success(null, object);
	}

	public static ModelMap success(String msg, Object object) {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("success", Boolean.TRUE);
		if (null != msg)
			modelMap.addAttribute("msg", msg);
		modelMap.addAttribute("data", object);
		return modelMap;
	}
	
	public static ModelMap failure(){
		return ResultUtil.failure(null);
	}
	
	public static ModelMap failure(String msg) {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("success", Boolean.FALSE);
		if (null != msg)
			modelMap.addAttribute("msg", msg);
		modelMap.addAttribute("data", null);
		return modelMap;
	}

}
