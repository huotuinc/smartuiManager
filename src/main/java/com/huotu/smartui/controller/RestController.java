package com.huotu.smartui.controller;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huotu.huobanplus.smartui.entity.support.Scope;
import com.huotu.huobanplus.smartui.entity.support.WidgetProperties;
import com.huotu.huobanplus.smartui.model.WidgetMainModel;
import com.huotu.huobanplus.smartui.model.WidgetTypeModel;
import com.huotu.huobanplus.smartui.sdk.WidgetMainRepository;
import com.huotu.huobanplus.smartui.sdk.WidgetTypeRespository;
import com.huotu.smartui.utils.ResultUtil;

@Controller
public class RestController {

    @Autowired
    private WidgetMainRepository widgetMainRepository;

    @Autowired
    private WidgetTypeRespository widgetTypeRespository;

	/**
	 * 显示控件主体
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value="/widgetMain/list" ,method=RequestMethod.GET)
	@ResponseBody
	public ModelMap list(String rows,String page) throws IOException {
		ModelMap map = new ModelMap();
		Pageable pageable = new PageRequest(Integer.parseInt(page)-1, Integer.parseInt(rows));
		long totalSize = widgetMainRepository.getTotalSize(pageable);
		map.addAttribute("total", totalSize);
		List<WidgetMainModel> list = widgetMainRepository.listModel(pageable);
		if(list.size()>0)
    		for(WidgetMainModel model:list){
    			WidgetTypeModel typeModel = widgetTypeRespository.getOneModel(model.getTypeHref());
    			model.setType(typeModel);
    			model.setTypeHref(typeModel.getHref());
    		}
		map.addAttribute("rows", list);
		return map;
	}

	/**
	 * 删除控件主体
	 * @param href
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value="/widgetMain/delete" ,method=RequestMethod.POST)
	@ResponseBody
	public ModelMap delete(String href){
		try {
			if(null==href||"".equals(href))
				return ResultUtil.failure("无此控件");
			widgetMainRepository.delete(href);
			return ResultUtil.success();
		} catch (IOException e) {
			return ResultUtil.failure("删除异常");
		}
	}

	/**
	 * 新增控件主体
	 * @param main
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value="/widgetMain/insert" ,method={RequestMethod.POST})
	@ResponseBody
	public ModelMap addWidgetMain(String name,String description,String type,String properties,Integer orderWeight) {
		if("".equals(type)||"".equals(name))
			return ResultUtil.failure("下拉框或者控件名字未填!");
		return add(name, description, type,properties,orderWeight);
	}

	@Transactional
	ModelMap add(String name,String description,String type,String properties,Integer orderWeight){
		try {
			HashMap<String, Object> doPatch = new HashMap<>();
			if(null!=properties&&!"".equals(properties)){
				ObjectMapper mapper = new ObjectMapper();
				Map<String, Object> data = mapper.readValue(properties, WidgetProperties.class);
				doPatch.put("properties", data);
			}else{
				doPatch.put("properties", null);
			}
			doPatch.put("type", type);
			String href = widgetMainRepository.insertModel(name, description,orderWeight);
			try {
			    widgetMainRepository.patchModel(href, doPatch);
            } catch (IOException e) {
                widgetMainRepository.delete(href);
            }
			return ResultUtil.success();
		} catch (IOException e) {
			return ResultUtil.failure("字符处理异常");
		}
	}

	/**
	 * 更新控件主体
	 * @param href
	 * @param name
	 * @param description
	 * @param type
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value="/widgetMain/update" ,method={RequestMethod.POST})
	@ResponseBody
	public ModelMap updateWidgetMain(String href,String name,String description,String type,String properties, Integer orderWeight){
		try {
			if("".equals(type)||"".equals(name))
				return ResultUtil.failure("下拉框或者控件名字未填!");
			HashMap<String, Object> doPatch = new HashMap<>();
			if(null!=properties&&!"".equals(properties)){
				ObjectMapper mapper = new ObjectMapper();
				Map<String, Object> data = mapper.readValue(properties, WidgetProperties.class);
				doPatch.put("properties", data);
			}else{
				doPatch.put("properties", null);
			}
			doPatch.put("name", name);
	        doPatch.put("description", description);
	        doPatch.put("type", type);
	        doPatch.put("orderWeight", orderWeight);
	        widgetMainRepository.patchModel(href, doPatch);
	        return ResultUtil.success();
		} catch (IOException e) {
			return ResultUtil.failure("字符串处理异常");
		}
	}

	/**
	 * 显示控件主体的扩展界面
	 * @param href
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value="/widgetMain/view",method={RequestMethod.GET,RequestMethod.POST})
	public ModelAndView view(String href,HttpServletRequest request) throws IOException{
		WidgetMainModel model = widgetMainRepository.getOneModel(href);
		ModelAndView mav = new ModelAndView();
		mav.addObject("main",model);
		mav.setViewName("/view");
		return mav;
	}

	/**
	 * 上传模板资源
	 * @param href
	 * @param type
	 * @param file
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value="/widgetMain/upload" ,method={RequestMethod.POST})
	@ResponseBody
	public ModelMap upload(String href,String type,@RequestParam("file")MultipartFile file,HttpServletRequest request){
//		WidgetMainRepositoryImpl widgetMainRepositoryImpl = new WidgetMainRepositoryImpl();
//		widgetMainRepositoryImpl.getAuthorise().updateAuthorizes("demo", "0e329a8698bad1d78c29b3465fae5aba");
		try {
			InputStream inputStream =file.getInputStream();
			if(inputStream.available()==0)
				return ResultUtil.failure("不能上传空文件");
			if(null==href||null==type)
				return ResultUtil.failure("未选择分类或者没有该控件主体");
			widgetMainRepository.putResource(href+"/"+type,inputStream);
//			widgetMainRepositoryImpl.putResource("http://localhost:8080/webservice/widgetMains/1/thumbnailResource", inputStream);
			return ResultUtil.success();
		} catch (IOException e) {
			return ResultUtil.failure("上传文件异常");
		}
	}

	/**
	 * 根据type得到浏览资源的内容或者编辑资源的内容
	 * @param href
	 * @param type
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value="/widgetMain/getTemplate" ,method=RequestMethod.GET)
	@ResponseBody
	public ModelMap getTemplate(String href,String type,HttpServletRequest request) throws IOException{
		WidgetMainModel model = widgetMainRepository.getOneModel(href);
		if(null==model)
			return ResultUtil.failure("控件主体不存在！");
		String content = null;
		if("browseTemplateResource".equals(type)){
			if(null!=model.getBrowseTemplateResourceValue()){
				content=getContent(model.getBrowseTemplateResourceValue());
				return ResultUtil.success(content);
			}
		}else if("editTemplateResource".equals(type)){
			if(null!=model.getEditTemplateResourceValue()){
				content=getContent(model.getEditTemplateResourceValue());
				return ResultUtil.success(content);
			}
		}
		return ResultUtil.failure("无此编码");
	}


	@RequestMapping(value="/widgetMain/saveTemplate" ,method=RequestMethod.POST)
	@ResponseBody
	public ModelMap saveTemplate(@RequestParam String href,String type,@RequestParam String content,HttpServletRequest request) {
		try {
			if(null==type||"".equals(type))
				return ResultUtil.failure("文件类型必选");
			InputStream stream = new ByteArrayInputStream(content.getBytes("utf-8"));
			widgetMainRepository.putResource(href+"/"+type, stream);
			return ResultUtil.success();
		} catch (IOException e) {
			return ResultUtil.failure("修改模板异常");
		}
	}

	/**
	 * 根据path得到内容
	 * @param path
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private String getContent(String path) throws MalformedURLException,IOException{
		URL url = new URL(path);
		URLConnection connection = url.openConnection();
		InputStream is = connection.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
		StringBuilder builder = new StringBuilder();
		String line;
        while ((line = reader.readLine()) != null) {
        	builder.append(line);
        	builder.append("\r\n");
        }
        is.close();
		return builder.toString().trim();
	}

	/**
	 * 控件主体分类的显示
	 * @param rows
	 * @param page
	 * @param request
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value="/widgetType/list" ,method=RequestMethod.GET)
	@ResponseBody
	public ModelMap listType(String rows,String page,HttpServletRequest request) throws IOException {
		ModelMap map = new ModelMap();
		Pageable pageable = new PageRequest(Integer.parseInt(page)-1, Integer.parseInt(rows));
		long totalSize = widgetTypeRespository.getTotalSize(pageable);
		map.addAttribute("total", totalSize);
		List<WidgetTypeModel> list = widgetTypeRespository.listModel(pageable);
		map.addAttribute("rows", list);
		return map;
	}

	/**
	 * 控件主体分类的显示，适用于下拉框
	 * @param request
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value="/widgetType/list2" ,method=RequestMethod.GET)
	@ResponseBody
	public List<WidgetTypeModel> listType2(HttpServletRequest request) throws IOException {
		List<WidgetTypeModel> list = widgetTypeRespository.listModel(null);
		return list;
	}

	/**
	 * 新增分类
	 * @param type
	 * @param request
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value="/widgetType/add" ,method=RequestMethod.POST)
	@ResponseBody
	public ModelMap addWidgetType(String name,Integer orderWeight,String scopeName) throws IOException{
	    widgetTypeRespository.insertModel(name, orderWeight,scopeName);
		return ResultUtil.success();
	}

	/**
	 * 编辑分类
	 * @param href
	 * @param name
	 * @param request
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value="/widgetType/update" ,method=RequestMethod.POST)
	@ResponseBody
	public ModelMap updateWidgetType(String href,String name,Integer orderWeight,String scopeName) throws IOException{
        widgetTypeRespository.patch(href,"name", name);
        widgetTypeRespository.patch(href, "orderWeight", orderWeight);
        if("global".equals(scopeName)){
            widgetTypeRespository.patch(href, "scope", Scope.global);
        }else if("sis".equals(scopeName)){
            widgetTypeRespository.patch(href, "scope", Scope.sis);
        }else if("system".equals(scopeName)){
            widgetTypeRespository.patch(href, "scope", Scope.system);
        }
        return ResultUtil.success();
	}

	/**
	 * 删除分类
	 * @param href
	 * @param request
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value="/widgetType/delete" ,method=RequestMethod.POST)
	@ResponseBody
	@Transactional
	public ModelMap deleteType(String href,HttpServletRequest request) throws IOException{
	    widgetTypeRespository.delete(href);
		return ResultUtil.success();
	}

	/**
	 * 更新分类的图片
	 * @param href
	 * @param type
	 * @param file
	 * @param request
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value="/widgetType/upload" ,method={RequestMethod.POST})
	@ResponseBody
	public ModelMap uploadType(String href,String type,@RequestParam("file")MultipartFile file,HttpServletRequest request) throws IOException{
		InputStream inputStream =file.getInputStream();
		if(inputStream.available()==0)
			return ResultUtil.failure("不能上传空文件");
		widgetTypeRespository.putResource(href+"/"+type,inputStream);
		return ResultUtil.success();
	}

	@RequestMapping(value="/widgetMain/briefView", method=RequestMethod.POST)
	@ResponseBody
	public ModelMap briefView(String href,String type,String properties,HttpServletRequest request){
		try {
			Map<String, Object> data =  null;
			String result = null;
			if(null!=properties&&!"".equals(properties)){
				ObjectMapper mapper = new ObjectMapper();
				data = mapper.readValue(properties, WidgetProperties.class);
			}
			//特殊标记
			if("browseTemplateResource".equals(type)){
				result = widgetMainRepository.getJson(href+"/briefViewGenerater", data);
			}else if("editTemplateResource".equals(type)){
				result = widgetMainRepository.getJson(href+"/editable/briefViewGenerater", data);
			}
			return ResultUtil.success(result);
		} catch (IOException e) {
			return ResultUtil.failure("字符处理异常");
		}
	}

//	private WidgetMainRepositoryImpl getWidgetMainRepositoryImpl(HttpServletRequest request){
//		WidgetMainRepositoryImpl widgetMainRepositoryImpl = new WidgetMainRepositoryImpl();
//		HttpSession session = request.getSession();
//		widgetMainRepositoryImpl.getAuthorise().updateAuthorizes(session.getAttribute("user").toString(), session.getAttribute("appkey").toString());
//		widgetMainRepositoryImpl.setProduction(true);
//		return widgetMainRepositoryImpl;
//	}

//	private WidgetTypeRepositoryImpl getWidgetTypeRepositoryImpl(HttpServletRequest request){
//		WidgetTypeRepositoryImpl widgetTypeRepositoryImpl = new WidgetTypeRepositoryImpl();
//		HttpSession session = request.getSession();
//		widgetTypeRepositoryImpl.getAuthorise().updateAuthorizes(session.getAttribute("user").toString(), session.getAttribute("appkey").toString());
//		widgetTypeRepositoryImpl.setProduction(true);
//		return widgetTypeRepositoryImpl;
//	}

}
