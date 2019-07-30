package com.sheepmagic.selfspring.servlet;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Pattern;

public class Handler {
	Object controller;
	Method method;
	Pattern pattern;
	Map<String, Integer> paramIndexMapping;
	
	public Handler(Object controller, Method method, Pattern pattern){
		this.controller = controller;
		this.method = method;
		this.pattern = pattern;
	}
	
	public Object getController() {
		return controller;
	}
	public void setController(Object controller) {
		this.controller = controller;
	}
	public Method getMethod() {
		return method;
	}
	public void setMethod(Method method) {
		this.method = method;
	}
	public Pattern getPattern() {
		return pattern;
	}
	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}
	public Map<String, Integer> getParamIndexMapping() {
		return paramIndexMapping;
	}
	public void setParamIndexMapping(Map<String, Integer> paramIndexMapping) {
		this.paramIndexMapping = paramIndexMapping;
	}
}
