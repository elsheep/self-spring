package com.sheepmagic.selfspring.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sheepmagic.framework.annotations.SheepAutowired;
import com.sheepmagic.framework.annotations.SheepController;
import com.sheepmagic.framework.annotations.SheepRequestMapping;
import com.sheepmagic.framework.annotations.SheepService;

public class SheepDispatchServlet extends HttpServlet{
	
	private Properties contextConfig = new Properties();
	private List<String> classNames = new ArrayList<String>();
	private Map<String, Object> ioc = new HashMap<String, Object>();
//	private Map<String, Handler> handlerMapping = new HashMap<String, Handler>();
	private List<Handler> handlerMapping = new ArrayList<Handler>();
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -9207136912083456231L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		super.doPost(req, resp);
		doDispatch(req, resp);
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		System.out.println("Init......");
		super.init(config);
		doConfig(config.getInitParameter("contextLoadLocation"));
		doScanner(contextConfig.getProperty("scanPackage"));
		doInstance();
		doAutowire();
		initHandlerMapping();
		System.out.println("Framework start finished");
		
	}
	
	public void doConfig(String location){
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(location);
		try {
			contextConfig.load(is);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(null != is){
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public void doScanner(String scanPackage){
		URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
		File dir = new File(url.getFile());
		for(File file : dir.listFiles()){
			if(file.isDirectory()){
				doScanner(scanPackage + "." + file.getName());
			}else{
				String className = scanPackage + "." + file.getName().replace(".class", "");
				classNames.add(className);
				System.out.println(className);
			}
		}
	}
	
	public String lowerFirstChar(String str){
		char [] chars = str.toCharArray();
		chars[0] += 32;
		return String.valueOf(chars);
	}
	
	public void doInstance(){
		if(classNames.isEmpty()){
			return;
		}else{
			try{
				for(String className : classNames){
					Class<?> classObj = Class.forName(className);
					if(classObj.isAnnotationPresent(SheepController.class)){
						ioc.put(lowerFirstChar(classObj.getSimpleName()), classObj.newInstance());
					}else if(classObj.isAnnotationPresent(SheepService.class)){
						SheepService sheepService = classObj.getAnnotation(SheepService.class);
						String beanName = sheepService.value();
						if(!"".equals(beanName)){
							
						}else{
							beanName = lowerFirstChar(classObj.getSimpleName()); 
						}
						ioc.put(beanName.trim(), classObj.newInstance());
						
						for(Class<?> i : classObj.getInterfaces()){
							ioc.put(i.getName(), classObj.newInstance());
						}
					}else{
						continue;
					}
					
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public void doAutowire(){
		if(ioc.isEmpty()){
			return ;
		}else{
			try{
				for(Entry<String, Object> entry : ioc.entrySet()){
					for(Field field : entry.getValue().getClass().getDeclaredFields()){
						if(field.isAnnotationPresent(SheepAutowired.class)){
							SheepAutowired autowired = field.getAnnotation(SheepAutowired.class);
							String beanName = autowired.value();
							if("".equals(beanName)){
								beanName = field.getType().getName();
							}
							field.setAccessible(true);
							field.set(entry.getValue(), ioc.get(beanName));
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public void initHandlerMapping(){
		if(ioc.isEmpty()){
			return;
		}else{
			for(Entry<String, Object> entry : ioc.entrySet()){
				Class<?> classObj = entry.getValue().getClass();
				if(!classObj.isAnnotationPresent(SheepController.class)){
					return;
				}
				String baseUrl = "";
				if(classObj.isAnnotationPresent(SheepRequestMapping.class)){
					baseUrl = classObj.getAnnotation(SheepRequestMapping.class).value();
				}
				
				Method [] methods = classObj.getMethods();
				for(Method m : methods){
					if(!m.isAnnotationPresent(SheepRequestMapping.class)){
						continue;
					}
					String regex = m.getAnnotation(SheepRequestMapping.class).value();
					regex = (baseUrl + regex).replaceAll("/+", "/");
					Pattern pattern = Pattern.compile(regex);
					handlerMapping.add(new Handler(entry.getValue(), m, pattern));
					System.out.println(regex + "  " + m.getName());
				}
			}
		}
	}
	
	public void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception{
		Handler handler = getHandler(req);
		if(handler == null){
			resp.getWriter().write("404");
		}else{
			Class<?> [] paramTypes = handler.method.getParameterTypes();
			Object [] paramValues = new Object[paramTypes.length];
			Map<String, String[]> params = req.getParameterMap();
			for(Entry<String, String[]> param : params.entrySet()){
				System.out.println(param.getValue());
				String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll("\\s", "");
				if(!handler.paramIndexMapping.containsKey(param.getKey())){
					continue;
				}
			}
		}
	}
	
	public Handler getHandler(HttpServletRequest req){
		if(handlerMapping.isEmpty()){
			return null;
		}else{
			String url = req.getRequestURI();
			String contextPath = req.getContextPath();
			url = url.replace(contextPath, "").replaceAll("/+", "/");
			for(Handler handler : handlerMapping){
				try{
					Matcher matcher = handler.getPattern().matcher(url);
					if(!matcher.matches()){
						continue;
					}else{
						return handler;
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			return null;
		}
	}
	

}
