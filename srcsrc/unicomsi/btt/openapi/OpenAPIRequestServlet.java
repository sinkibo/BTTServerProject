/*_
 * UNICOM(R) Multichannel Bank Transformation Toolkit Source Materials 
 * Copyright(C) 2011 - 2017 UNICOM Systems, Inc. - All Rights Reserved 
 * Highly Confidential Material - All Rights Reserved 
 * THE INFORMATION CONTAINED HEREIN CONSTITUTES AN UNPUBLISHED 
 * WORK OF UNICOM SYSTEMS, INC. ALL RIGHTS RESERVED. 
 * NO MATERIAL FROM THIS WORK MAY BE REPRINTED, 
 * COPIED, OR EXTRACTED WITHOUT WRITTEN PERMISSION OF 
 * UNICOM SYSTEMS, INC. 818-838-0606 
 */


package unicomsi.btt.openapi;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.btt.base.DSEObjectNotFoundException;
import com.ibm.btt.channel.BTTChannelDriver;
import com.ibm.btt.channel.ChannelInitializer;
import com.ibm.btt.channel.ChannelRequest;
import com.ibm.btt.channel.ChannelResponse;
import com.ibm.btt.clientserver.ChannelContext;
import com.ibm.btt.clientserver.ChannelDriver;
import com.ibm.btt.clientserver.DSEChannelContext;
import com.ibm.btt.clientserver.DSENoRegisteredDeviceTypeException;
import com.ibm.btt.cs.servlet.CSReqServlet;
import com.ibm.btt.http.HttpChannelRequest;
import com.ibm.btt.http.HttpChannelResponse;
import com.ibm.btt.http.JavaEstablishSessionRequest;

public class OpenAPIRequestServlet extends CSReqServlet {

	public static final java.lang.String COPYRIGHT = 
            "UNICOM(R) Multichannel Bank Transformation Toolkit Source Materials "
			+ "Copyright(C) 2011 - 2017 UNICOM Systems, Inc. - All Rights Reserved "
			+ "Highly Confidential Material - All Rights Reserved "
			+ "THE INFORMATION CONTAINED HEREIN CONSTITUTES AN UNPUBLISHED "
			+ "WORK OF UNICOM SYSTEMS, INC. ALL RIGHTS RESERVED. "
			+ "NO MATERIAL FROM THIS WORK MAY BE REPRINTED, "
			+ "COPIED, OR EXTRACTED WITHOUT WRITTEN PERMISSION OF "
			+ "UNICOM SYSTEMS, INC. 818-838-0606 ";
	
	

	private static final long serialVersionUID = 1L;
	
	private static boolean CORS = false;
	private static String ORIGIN = null;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		CORS = "true".equals(config.getInitParameter("CORS"));
		ORIGIN = config.getInitParameter("ORIGIN");
	}

	@SuppressWarnings("serial")
	@Override
	protected ChannelDriver getChannelDriver() {
		return new BTTChannelDriver(){

			@Override
			protected void initDeviceType(ChannelContext channelContext) throws DSENoRegisteredDeviceTypeException, DSEObjectNotFoundException {
				
				String device = OpenAPIConstant.DEVICETYPE;
				if ("op_flow".equals(channelContext.getChannelRequest().getAttribute("BTT-action")))
				{
					device = OpenAPIConstant.DEVICETYPE_OP_FLOW;
				}
				
				try {
					// verify that the device type is a registered device type
					ChannelInitializer.getSettings().getValueAt(device);
				} catch (Exception e) {
					throw new DSENoRegisteredDeviceTypeException(MessageFormat.format("Can''t find the channel handler for deviceType ''{0}''", OpenAPIConstant.DEVICETYPE ), e);
				}

				channelContext.setDeviceType(device);
			}
		};
	}

	@Override
	protected ChannelResponse getChannelResponse(HttpServletResponse res) {
		return super.getChannelResponse(res);
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		
		if(CORS)
		{
			res.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, BTT-action");
			res.setHeader("Access-Control-Allow-Methods", "GET, POST");
			res.setHeader("Access-Control-Allow-Origin", "*".equals(ORIGIN) ? req.getHeader("origin") : ORIGIN);
			res.setHeader("Access-Control-Allow-Credentials", "true");
		}
		
		if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
			this.doOptions(req, res);
			return;
		}
		
		String action = req.getHeader("BTT-action");
		req.setAttribute("BTT-action", action);
		
		if ("logout".equals(action)) // do logout
		{
			req.getSession(false).invalidate();
			res.getWriter().print("logout");
			return;
		}
		
		ChannelDriver driver = this.getChannelDriver();
		ChannelRequest request = ("session".equals(action)) ? new JavaEstablishSessionRequest(req) : new HttpChannelRequest(req);
		ChannelResponse response = new HttpChannelResponse(res);
		ChannelContext ctx = driver.createChannelContext(request, response);
		if (ctx instanceof DSEChannelContext)
		{
			((DSEChannelContext)ctx).reqServlet = this;		
		}
			
		driver.service(ctx);
	}

	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		
		res.setStatus(CORS ? 200 : 403);
		super.doOptions(req, res);
	}	
}
