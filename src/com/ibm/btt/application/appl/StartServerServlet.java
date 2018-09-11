/*_
 * UNICOM(R) Multichannel Bank Transformation Toolkit Source Materials 
 * Copyright(C) 2008 - 2017 UNICOM Systems, Inc. - All Rights Reserved 
 * Highly Confidential Material - All Rights Reserved 
 * THE INFORMATION CONTAINED HEREIN CONSTITUTES AN UNPUBLISHED 
 * WORK OF UNICOM SYSTEMS, INC. ALL RIGHTS RESERVED. 
 * NO MATERIAL FROM THIS WORK MAY BE REPRINTED, 
 * COPIED, OR EXTRACTED WITHOUT WRITTEN PERMISSION OF 
 * UNICOM SYSTEMS, INC. 818-838-0606 
 */

package com.ibm.btt.application.appl;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.ibm.btt.base.Context;
import com.ibm.btt.base.ContextFactory;
import com.ibm.btt.clientserver.CSServerService;
import com.ibm.btt.config.InitManager;
import com.ibm.btt.config.dynamic.DynamicFlowHelper;
/**
 * This class is a sample servlet that starts the server in the Web server workstation. 
 * It creates an initial context in the server and starts the client server service.
 * In a real implementation, this class would have to perform any process required by the 
 * server such as the initial context creation and the 
 * initialization of the client server service.
 */
public class StartServerServlet extends HttpServlet implements Servlet {

	public static final java.lang.String COPYRIGHT = 
              "UNICOM(R) Multichannel Bank Transformation Toolkit Source Materials "
			+ "Copyright(C) 2008 - 2017 UNICOM Systems, Inc. - All Rights Reserved "
			+ "Highly Confidential Material - All Rights Reserved "
			+ "THE INFORMATION CONTAINED HEREIN CONSTITUTES AN UNPUBLISHED "
			+ "WORK OF UNICOM SYSTEMS, INC. ALL RIGHTS RESERVED. "
			+ "NO MATERIAL FROM THIS WORK MAY BE REPRINTED, "
			+ "COPIED, OR EXTRACTED WITHOUT WRITTEN PERMISSION OF "
			+ "UNICOM SYSTEMS, INC. 818-838-0606 ";

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	/* (non-Javadoc)
	 * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
	 */
	public void init(ServletConfig arg0) throws ServletException {
		try {
			// Prevent the multi-initialization
			if (!InitManager.isInitialized()) {
				// Get the initialization parameter from servlet  
				String bttPath = (String)arg0.getInitParameter("bttConfigPath");
				// Initiate the BTT runtime environment 
				InitManager.reset(bttPath);
				
				// Initiate the rule definition for dynamic flow
				// Remove the comment on the following code to enable dynamic flow function
				// DynamicFlowHelper.getInstance().initDynamicFlow();
				
				// Creates the initial context in the server.
				Context context = ContextFactory.createContext("branchServer");
				((CSServerService)context.getService("realCSServer")).initiateServer();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}

