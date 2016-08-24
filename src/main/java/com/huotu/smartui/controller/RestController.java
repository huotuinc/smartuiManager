package com.huotu.smartui.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huotu.huobanplus.smartui.Types;
import com.huotu.huobanplus.smartui.entity.WidgetMain;
import com.huotu.huobanplus.smartui.entity.WidgetType;
import com.huotu.huobanplus.smartui.entity.support.Scope;
import com.huotu.huobanplus.smartui.entity.support.WidgetProperties;
import com.huotu.huobanplus.smartui.sdk.WidgetMainRepository;
import com.huotu.huobanplus.smartui.sdk.WidgetTypeRepository;
import com.huotu.smartui.model.NativeTypeModel;
import com.huotu.smartui.model.ScopeModel;
import com.huotu.smartui.model.WidgetTypeModel;
import com.huotu.smartui.utils.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
public class RestController {

    @Autowired
    private WidgetMainRepository widgetMainRepository;
    @Autowired
    private WidgetTypeRepository widgetTypeRepository;

    @PostConstruct
    public void setErrorSecretKey() throws IOException {
        widgetMainRepository.updateRole("demo", "1f2f3f4f5f6f7f8f9f");
        widgetTypeRepository.updateRole("demo", "1f2f3f4f5f6f7f8f9f");
    }

    @RequestMapping(value = {"/login"}, method = {RequestMethod.POST})
    @ResponseBody
    public ModelMap login(String user, String appkey, String apiRoot, HttpServletRequest request) throws IOException {
        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(1800);
        session.setAttribute("user", user);
        session.setAttribute("appkey", appkey);
        if (apiRoot != null && apiRoot != "") {
            widgetMainRepository.setRootHref(apiRoot);
            widgetTypeRepository.setRootHref(apiRoot);
        }
        return ResultUtil.success();
    }

    @RequestMapping(value = {"/widgetMain/list"}, method = {
            RequestMethod.GET})
    @ResponseBody
    public ModelMap list(Long typeId, String name, String rows, String page, HttpServletRequest request) throws IOException {
        setSecretKey(request);
        ModelMap map = new ModelMap();
        Pageable pageable = new PageRequest(Integer.parseInt(page) - 1,
                Integer.parseInt(rows));
        if (Objects.nonNull(typeId) && typeId.equals(0L)) {
            typeId = null;
        }
        if (Objects.nonNull(name) && name.equals("")) {
            name = null;
        }
//        Page<WidgetMain> widgetMains = widgetMainRepository.findAll(pageable);
        Page<WidgetMain> widgetMains = widgetMainRepository.findAllByNameAndType(typeId, name, pageable);
        map.addAttribute("total", Long.valueOf(widgetMains.getTotalElements()));
        map.addAttribute("rows", widgetMains.getContent());
        return map;
    }

    @RequestMapping(value = {"/widgetMain/delete"}, method = {RequestMethod.POST})
    @ResponseBody
    public ModelMap delete(Long id, HttpServletRequest request) throws IOException {
        setSecretKey(request);
        try {
            widgetMainRepository.deleteByPK(id);
            return ResultUtil.success();
        } catch (IOException e) {
            return ResultUtil.failure("删除异常");
        }
    }

    @RequestMapping(value = "/widgetType/scopeSearch", method = RequestMethod.GET)
    @ResponseBody
    public List<ScopeModel> scopeSearch(){
        Map<Scope,String> map = Scope.getAllScopes();
        List<ScopeModel> list = new ArrayList<>();
        map.forEach((scope, s) -> {
            ScopeModel model = new ScopeModel();
            model.setName(scope.name());
            model.setDescription(s);
            list.add(model);
        });
        return list;
    }

    @RequestMapping(value = {"/widgetMain/insert"}, method = {RequestMethod.POST})
    @ResponseBody
    public ModelMap addWidgetMain(String name, String description, Long typeId, String properties, Integer orderWeight, Integer nativeType, @RequestParam(required = false) String version,
                                  Integer appSupportVersion, HttpServletRequest request) throws IOException {
        setSecretKey(request);
        if ("".equals(typeId) || "".equals(typeId))
            return ResultUtil.failure("下拉框或者控件名字未填!");
        WidgetMain main = new WidgetMain();
        main.setName(name);
        if (!"".equals(description) && Objects.nonNull(description))
            main.setDescription(description);
        if ("".equals(orderWeight) || Objects.isNull(orderWeight))
            main.setOrderWeight(50);
        else
            main.setOrderWeight(orderWeight);
        if (!"".equals(version) && Objects.nonNull(version))
            main.setVersion(version);
        if (!"".equals(nativeType) && Objects.nonNull(nativeType))
            main.setNativeType(nativeType);
        if (null == appSupportVersion)
            appSupportVersion = 0;
        main.setAppSupportVersion(appSupportVersion);
        try {
            WidgetType type = widgetTypeRepository.getOneByPK(typeId);
            main.setType(type);
            main = widgetMainRepository.insert(main);

        } catch (IOException e) {
            return ResultUtil.failure("基本信息新增失败");
        }

        try {
            if (properties != null && !"".equals(properties)) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> data = mapper.readValue(properties, WidgetProperties.class);
                widgetMainRepository.patchByPK(main.getId(), "properties", data);
            }
            return ResultUtil.success();
        } catch (IOException e) {
            return ResultUtil.failure("属性新增失败");
        }
    }

    @RequestMapping(value = {"/widgetMain/update"}, method = {
            RequestMethod.POST})
    @ResponseBody
    public ModelMap updateWidgetMain(Long id, String name, String description, Long typeId, String properties,
                                     Integer appSupportVersion, Integer orderWeight, Integer nativeType, @RequestParam(required = false) String version, HttpServletRequest request) throws IOException {
        setSecretKey(request);
        ObjectMapper mapper;
        WidgetType type;
        Map<String, Object> data;
        WidgetMain main;
        if ("".equals(typeId) || "".equals(typeId))
            return ResultUtil.failure("分类或者控件名字未填!");
        if (properties != null && !"".equals(properties)) {
            mapper = new ObjectMapper();
            data = mapper.readValue(properties, WidgetProperties.class);
            widgetMainRepository.patchByPK(id, "properties", data);
        }
        type = widgetTypeRepository.getOneByPK(typeId);
        main = widgetMainRepository.getOneByPK(id);
        main.setType(type);
        main.setName(name);
        if (!"".equals(description) && Objects.nonNull(description))
            main.setDescription(description);
        if (!"".equals(orderWeight) && Objects.nonNull(orderWeight))
            main.setOrderWeight(orderWeight);
        if (!"".equals(nativeType) && Objects.nonNull(nativeType))
            main.setNativeType(nativeType);
        if (null == appSupportVersion)
            appSupportVersion = 0;
        main.setAppSupportVersion(appSupportVersion);
        if (null != version)
            main.setVersion(version);
        return ResultUtil.success();
    }

    @RequestMapping(value = {"/widgetMain/view"}, method = {
            RequestMethod.GET,
            RequestMethod.POST})
    public ModelAndView view(Long id, HttpServletRequest request) throws IOException {
        setSecretKey(request);
        WidgetMain model = widgetMainRepository.getOneByPK(id);
        ModelAndView mav = new ModelAndView();
        mav.addObject("main", model);
        mav.setViewName("/view");
        return mav;
    }

    @RequestMapping(value = {"/widgetMain/upload"}, method = {
            RequestMethod.POST})
    @ResponseBody
    public ModelMap upload(Long id, String type, @RequestParam(value = "file") MultipartFile file,
                           HttpServletRequest request) throws IOException {
        setSecretKey(request);
        InputStream inputStream;
        try {
            inputStream = file.getInputStream();
            if (inputStream.available() == 0)
                return ResultUtil.failure("不能上传空文件");
        } catch (IOException e) {
            return ResultUtil.failure("上传文件异常");
        }
        if (id == null || type == null)
            return ResultUtil.failure("未选择分类或者没有该控件主体");
        widgetMainRepository.putResource(id, type, inputStream);
        return ResultUtil.success();
    }

    @RequestMapping(value = {"/widgetMain/getTemplate"}, method = {
            RequestMethod.GET})
    @ResponseBody
    public ModelMap getTemplate(Long id, String type, HttpServletRequest request) throws IOException {
        setSecretKey(request);
        WidgetMain model = widgetMainRepository.getOneByPK(id);
        if (model == null)
            return ResultUtil.failure("控件主体不存在！");
        String content = null;
        if ("browseTemplateResource".equals(type)) {
            if (model.getBrowseTemplateResource() != null) {
                content = getContent(model.getBrowseTemplateResource().getValue());
                return ResultUtil.success(content);
            }
        } else if ("editTemplateResource".equals(type) && model.getEditTemplateResource() != null) {
            content = getContent(model.getEditTemplateResource().getValue());
            return ResultUtil.success(content);
        } else if ("appBrowseTemplateResource".equals(type) && model.getAppBrowseTemplateResource() != null) {
            content = getContent(model.getAppBrowseTemplateResource().getValue());
            return ResultUtil.success(content);
        } else if ("appEditTemplateResource".equals(type) && model.getAppEditTemplateResource() != null) {
            content = getContent(model.getAppEditTemplateResource().getValue());
            return ResultUtil.success(content);
        }
        return ResultUtil.failure("无此编码");
    }

    @RequestMapping(value = {"/widgetMain/saveTemplate"}, method = {
            RequestMethod.POST})
    @ResponseBody
    public ModelMap saveTemplate(Long id, String type, @RequestParam String content, HttpServletRequest request)
            throws IOException {
        setSecretKey(request);
        InputStream stream;
        if (type == null || "".equals(type))
            return ResultUtil.failure("文件类型必选");
        stream = new ByteArrayInputStream(content.getBytes("utf-8"));
        widgetMainRepository.putResource(id, type, stream);
        return ResultUtil.success();
    }

    private String getContent(String path) throws MalformedURLException, IOException {
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

    @RequestMapping(value = {"/widgetType/list"}, method = {RequestMethod.GET})
    @ResponseBody
    public ModelMap listType(String rows, String page,Scope scope, HttpServletRequest request) throws IOException {
        setSecretKey(request);
        ModelMap map = new ModelMap();
        Pageable pageable = new PageRequest(Integer.parseInt(page) - 1,
                Integer.parseInt(rows));
        Page<WidgetType> pages;
        if(Objects.isNull(scope))
            pages = widgetTypeRepository.findAll(pageable);
        else
            pages = widgetTypeRepository.findAllByScope(scope,pageable);
        map.addAttribute("total", Long.valueOf(pages.getTotalElements()));
        map.addAttribute("rows", pages.getContent());
        return map;
    }

    @RequestMapping(value = {"/widgetMain/listByType"}, method = {RequestMethod.GET})
    @ResponseBody
    public List<NativeTypeModel> listByType(HttpServletRequest request) throws IOException {
        setSecretKey(request);
        Map<Integer, String> types = Types.allTypes();
        List<NativeTypeModel> list = new ArrayList<>();
        types.forEach((id, value) -> {
            NativeTypeModel model = new NativeTypeModel();
            model.setId(id);
            model.setName(value);
            list.add(model);
        });
        return list;
    }

    @RequestMapping(value = {"/widgetType/list2"}, method = {RequestMethod.GET})
    @ResponseBody
    public List<WidgetTypeModel> listType2(HttpServletRequest request) throws IOException {
        setSecretKey(request);
        Pageable pageable = new PageRequest(0, 50);
        List<WidgetType> list = widgetTypeRepository.findAll(pageable).getContent();
        List<WidgetTypeModel> models = new ArrayList<>();
        for (WidgetType widgetType : list) {
            WidgetTypeModel model = new WidgetTypeModel();
            model.setId(widgetType.getId());
            model.setTitle(widgetType.getName() + "(" + widgetType.getScope().toString() + ")");
            models.add(model);
        }
        return models;
    }

    @RequestMapping(value = {"/widgetType/listSearch"}, method = {RequestMethod.GET})
    @ResponseBody
    public List<WidgetTypeModel> listSearch(HttpServletRequest request) throws IOException {
        setSecretKey(request);
        Pageable pageable = new PageRequest(0, 50);
        List<WidgetType> list = widgetTypeRepository.findAll(pageable).getContent();
        List<WidgetTypeModel> models = new ArrayList<>();
        WidgetTypeModel oneModel = new WidgetTypeModel();
        oneModel.setTitle("所有");
        oneModel.setId(0L);
        models.add(oneModel);
//        for (WidgetType widgetType : list) {
//            WidgetTypeModel model = new WidgetTypeModel();
//            model.setId(widgetType.getId());
//            model.setTitle(widgetType.getName() + "(" + widgetType.getScope().toString() + ")");
//            models.add(model);
//        }
        list.stream().forEach(widgetType -> {
            WidgetTypeModel model = new WidgetTypeModel();
            model.setId(widgetType.getId());
            model.setTitle(widgetType.getName() + "(" + widgetType.getScope().toString() + ")");
            models.add(model);
        });
        return models;
    }

    @RequestMapping(value = {"/widgetType/add"}, method = {RequestMethod.POST})
    @ResponseBody
    public ModelMap addWidgetType(String name, Integer orderWeight, String scopeName, HttpServletRequest request)
            throws IOException {
        setSecretKey(request);
        WidgetType type = new WidgetType();
        type.setName(name);
        type.setOrderWeight(orderWeight.intValue());
        setScopeValue(type, scopeName);
        widgetTypeRepository.insert(type);
        return ResultUtil.success();
    }

    @RequestMapping(value = {"/widgetType/update"}, method = {RequestMethod.POST})
    @ResponseBody
    public ModelMap updateWidgetType(Long id, String name, Integer orderWeight, String scopeName,
                                     HttpServletRequest request) throws IOException {
        setSecretKey(request);
        WidgetType type = widgetTypeRepository.getOneByPK(id);
        type.setName(name);
        type.setOrderWeight(orderWeight.intValue());
        setScopeValue(type, scopeName);
        return ResultUtil.success();
    }

    private WidgetType setScopeValue(WidgetType type, String scopeName) {
        if ("global".equals(scopeName))
            type.setScope(Scope.global);
        else if ("sis".equals(scopeName))
            type.setScope(Scope.sis);
        else if ("system".equals(scopeName))
            type.setScope(Scope.system);
        else if ("common".equals(scopeName))
            type.setScope(Scope.common);
        else if ("agent".equals(scopeName))
            type.setScope(Scope.agent);
        else if ("supplier".equals(scopeName))
            type.setScope(Scope.supplier);
        return type;
    }

    @RequestMapping(value = {"/widgetType/delete"}, method = {RequestMethod.POST})
    @ResponseBody
    @Transactional
    public ModelMap deleteType(Long id, HttpServletRequest request) throws IOException {
        setSecretKey(request);
        widgetTypeRepository.deleteByPK(id);
        return ResultUtil.success();
    }

    @RequestMapping(value = {"/widgetType/upload"}, method = {RequestMethod.POST})
    @ResponseBody
    public ModelMap uploadType(Long id, String type, @RequestParam(value = "file") MultipartFile file,
                               HttpServletRequest request) throws IOException {
        setSecretKey(request);
        InputStream inputStream = file.getInputStream();
        if (inputStream.available() == 0) {
            return ResultUtil.failure("不能上传空文件");
        } else {
            widgetTypeRepository.putResource(id, type, inputStream);
            return ResultUtil.success();
        }
    }

    @RequestMapping(value = {"/widgetMain/briefView"}, method = {RequestMethod.POST})
    @ResponseBody
    public ModelMap briefView(Long id, String type, String properties, HttpServletRequest request) throws IOException {
        setSecretKey(request);
        try {
            Map<String, Object> data = null;
            String result = null;
            if (properties != null && !"".equals(properties)) {
                ObjectMapper mapper = new ObjectMapper();
                data = mapper.readValue(properties, WidgetProperties.class);
            }
            if ("browseTemplateResource".equals(type)) {
//            result = widgetMainRepository
//                    .getJson((new StringBuilder()).append(id).append("/briefViewGenerater").toString(), data);
                result = widgetMainRepository.getJson(id, "briefViewGenerater", data);
            } else if ("editTemplateResource".equals(type)) {
//            result = widgetMainRepository
//                    .getJson((new StringBuilder()).append(id).append("/editable/briefViewGenerater").toString(), data);
                result = widgetMainRepository.getJson(id, "editable/briefViewGenerater", data);
            }

            return ResultUtil.success(result);
        } catch (IOException e) {
            return ResultUtil.failure("字符处理异常");
        }
    }

    private void setSecretKey(HttpServletRequest request) throws IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("user") != null && session.getAttribute("appkey") != null) {
            widgetMainRepository.updateRole(session.getAttribute("user").toString(),
                    session.getAttribute("appkey").toString());
            widgetTypeRepository.updateRole(session.getAttribute("user").toString(),
                    session.getAttribute("appkey").toString());
        } else {
            setErrorSecretKey();
        }
    }
}
