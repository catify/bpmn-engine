/**
 * *******************************************************
 * Copyright (C) 2013 catify <info@catify.com>
 * *******************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.catify.processengine.core;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.catify.processengine.management.ProcessManagementSer;
import com.catify.processengine.management.ProcessManagementServiceImpl;


/**
 * Main class and starting point for the application.
 */
public class AppStarter {

	static final Logger LOG = LoggerFactory.getLogger(AppStarter.class);
	
	//FIXME: provide actual clientId in the management API
	private static String clientId = "Client";

	public static void main(String... args) throws Exception {

		LOG.info("If you have problems viewing the colored log messages in your console, please change the encoder pattern in logback.xml.");
		
		AbstractApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/spring-context.xml");
	    context.registerShutdownHook();
		
	    ProcessManagementSer pm = new ProcessManagementServiceImpl();
	    
	    pm.startAllDeployedProcesses(clientId);
	}

}
