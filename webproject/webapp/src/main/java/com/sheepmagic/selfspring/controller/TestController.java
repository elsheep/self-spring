package com.sheepmagic.selfspring.controller;

import com.sheepmagic.selfspring.annotation.SheepAutowired;
import com.sheepmagic.selfspring.annotation.SheepController;
import com.sheepmagic.selfspring.annotation.SheepRequestMapping;
import com.sheepmagic.selfspring.annotation.SheepRequestParameter;
import com.sheepmagic.selfspring.interfaces.IService;

@SheepController
@SheepRequestMapping("demo")
public class TestController {
	@SheepAutowired
	IService demoService;
	
	@SheepRequestMapping("test")
	public String test(@SheepRequestParameter("name") String name){
		return "testing : " + name;
	}
	
}
