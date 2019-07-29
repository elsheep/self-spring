package com.sheepmagic.selfspring.controller;

import com.sheepmagic.framework.annotations.SheepAutowired;
import com.sheepmagic.framework.annotations.SheepController;
import com.sheepmagic.framework.annotations.SheepRequestMapping;
import com.sheepmagic.framework.annotations.SheepRequestParameter;
import com.sheepmagic.framework.interfaces.IService;

@SheepController
@SheepRequestMapping("/demo")
public class TestController {
	@SheepAutowired
	IService demoService;
	
	@SheepRequestMapping("/test")
	public String test(@SheepRequestParameter("name") String name){
		return "testing : " + name;
	}
	
}
