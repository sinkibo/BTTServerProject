/*_
 * UNICOM(R) Multichannel Bank Transformation Toolkit Source Materials 
 * Copyright(C) 2007 - 2017 UNICOM Systems, Inc. - All Rights Reserved 
 * Highly Confidential Material - All Rights Reserved 
 * THE INFORMATION CONTAINED HEREIN CONSTITUTES AN UNPUBLISHED 
 * WORK OF UNICOM SYSTEMS, INC. ALL RIGHTS RESERVED. 
 * NO MATERIAL FROM THIS WORK MAY BE REPRINTED, 
 * COPIED, OR EXTRACTED WITHOUT WRITTEN PERMISSION OF 
 * UNICOM SYSTEMS, INC. 818-838-0606 
 */

package com.ibm.btt.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Manifest;

import com.ibm.btt.base.BTTClassLoader;
import com.ibm.btt.base.DSEObjectNotFoundException;
import com.ibm.btt.base.DataInitializer;
import com.ibm.btt.base.KeyedCollection;
import com.ibm.btt.base.ServiceInitializer;
import com.ibm.btt.base.Settings;
import com.ibm.btt.base.TraceInitializer;
import com.ibm.btt.config.impl.DSEParser;
import com.ibm.btt.config.impl.GlobalSettings;
import com.ibm.btt.cs.servlet.CSConstants;
import com.ibm.btt.sm.CSSessionHandler;

/**
 * This class is used to initialize all the component of BTT product. It reads
 * components definition in btt.xml file, and call the Initializer of each
 * component one by one.
 *
 */
public class InitManager {

	public static final java.lang.String COPYRIGHT = 
              "UNICOM(R) Multichannel Bank Transformation Toolkit Source Materials "
			+ "Copyright(C) 2007 - 2017 UNICOM Systems, Inc. - All Rights Reserved "
			+ "Highly Confidential Material - All Rights Reserved "
			+ "THE INFORMATION CONTAINED HEREIN CONSTITUTES AN UNPUBLISHED "
			+ "WORK OF UNICOM SYSTEMS, INC. ALL RIGHTS RESERVED. "
			+ "NO MATERIAL FROM THIS WORK MAY BE REPRINTED, "
			+ "COPIED, OR EXTRACTED WITHOUT WRITTEN PERMISSION OF "
			+ "UNICOM SYSTEMS, INC. 818-838-0606 ";



	private static String configLocation = null;

	private static String bttXML = null;

	private static Object sem = new Object();

	private static Object sem1 = new Object();

//	private static boolean initialized = false;
	
	public enum Status{
		/* BTT environment is not initialized */
		UNINITIALIZED,
		/* BTT environment initialized sucessfully*/
		INITIALIZED_OK,
		/* BTT environment initialized with errors */
		INITIALIZED_WITH_ERROR
	}
	
private static Status status = Status.UNINITIALIZED ;
	
	private static long numWorkgroupUsers = 0;
	
	public static long getNumWorkgroupUsers() {
		return numWorkgroupUsers;
	}
	
	public static void setNumWorkgroupUsers(long num) {
		numWorkgroupUsers = num;
	}

	public static String getBTTTVersion(){
		try {
			Enumeration<URL> list = InitManager.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
			
			while(list.hasMoreElements()){
				URL url = list.nextElement();
				if(url.toString().contains("bttcore.jar")){
					InputStream inputStream = url.openStream();
					Manifest manifest = new Manifest(inputStream);
					return manifest.getMainAttributes().getValue("Bundle-Version");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	protected static void setstrLocation(String loc) {
		configLocation = loc;
	}

	/**
	 * Given a relative path to btt.xml, this method will return the full path
	 *
	 * @param str
	 *            relative path to btt.xml.
	 * @return full path.
	 */
	public static String getFullPath(String str) {
		if (str.startsWith("jar://"))
			return str;
		if (new File(str).exists())
			return str;
		else {
			try {
				new URL(str);
				return str;
			} catch (MalformedURLException e) {
				try {
					if (Settings.getSgmlPath() != null)
						if (Settings.getSgmlPath().endsWith("/") || Settings.getSgmlPath().endsWith("\\\\"))
							return Settings.getSgmlPath() + str;
						else
							return Settings.getSgmlPath() + "/" + str;
					else
						return configLocation + str;
				} catch (Throwable th) {
					return configLocation + str;
				}
			}
		}
	}

	/**
	 * This method can only be invoked in Websphere Application Server
	 * environment<br>
	 * In Application Server, you should define the path of the btt.xml file in
	 * JNDI name space.<br>
	 * It initializes all the btt components defined in btt.xml file.
	 *
	 * @throws Exception
	 *             failed to initialize btt.
	 * @Deprecated
	 */
	public static void reset() throws BTTInitException {
		synchronized (sem1) {
			status = Status.UNINITIALIZED ;
			try {
				Class klass = Class
						.forName("com.ibm.btt.config.impl.LookupConfigFileAndReset");
				klass.newInstance();
			} catch (Throwable e) {
				throw new BTTInitException(
						"you can not call \"reset()\" in NONE-WAS environment, "
								+ "please call method \"reset(String initialPath)\" instead",
						e);
			}
			status = Status.INITIALIZED_OK ;
		}
	}

	/**
	 * This method initialize all the btt components defined in the btt.xml<br>
	 *
	 * @param initialPath
	 *            the location of btt.xml file. It should be any string of a URL<br>
	 *            for example:<br>
	 *            <I>http://mycompany.com/btt.xml</I><br>
	 *            <I>file:///c:\btt.xml</I><br>
	 *            <I>jar:///mypackage/btt.xml</I> if the string starts with
	 *            jar://, InitManager will find the btt.xml in class-path<br>
	 * @throws Exception
	 *             failed to initialize
	 */
	public static void reset(String initialPath) throws BTTInitException {
		synchronized (sem) {
			try {
				status = Status.UNINITIALIZED;
				String refPath = InitUtils.getFileDirectory(initialPath);
				setstrLocation(refPath);
				System.out.println("Read BTT configuration from : \""
						+ initialPath + "\"");
				KeyedCollection components = DSEParser.getSettings(initialPath);

				bttXML = initialPath;
				initializeAllComponents(components);

				Settings.checkForOptionalExternalFiles();
				CSSessionHandler.sessionMode=null;
				status = Status.INITIALIZED_OK;
			} catch (BTTInitException e) {
				status = Status.INITIALIZED_WITH_ERROR ;
				throw e;
			}
		}
	}
	
	/**
	 * Get btt configuration file path
	 * @return path of btt configuration file
	 */
	public static String getSgmlPath() {
		return bttXML;
	}

	/**
	 * Clean up all components
	 * @throws BTTInitException
	 */
	public static void cleanUp() throws BTTInitException {
		synchronized (sem) {
			if (isInitialized()) {
				String refPath = InitUtils.getFileDirectory(bttXML);
				setstrLocation(refPath);
				System.out.println("Read BTT configuration from : \"" + bttXML +"\"");
				KeyedCollection components = DSEParser.getSettings(bttXML);
				cleanUpAllComponents(components);
			}
		}
	}

	private static void cleanUpAllComponents(KeyedCollection components) throws BTTInitException {
		for (int i = components.size() - 1; i >= 0; i--) {
			try {
				KeyedCollection comp = (KeyedCollection) components
						.getElementAt(i);
				System.out
						.println("Clean up BTT Component: " + comp.getName());
				try {
					cleanUpComponent(comp);
				} catch (BTTInitException e) {
					System.out.println("Clean up BTT Component: " + comp.getName() +"\t[Failed]");
					throw e;
				}
				System.out.println("Clean up BTT Component: " + comp.getName() + "\t[Success]");
			} catch (DSEObjectNotFoundException e) {
				throw new BTTInitException(e);
			}
		}
		System.out.println(components.size() + " BTT Components cleaned up.");
	}

	private static void cleanUpComponent(KeyedCollection comp) throws BTTInitException {
		try {
			String klassN = (String) comp.getValueAt("initializer");
			Class klass = BTTClassLoader.loadClass(klassN);
			java.lang.reflect.Constructor con = klass
					.getDeclaredConstructor(new Class[] {});
			Initializer init = (Initializer) con.newInstance(new Object[] {});
			init.cleanup(comp);
		} catch (BTTInitException e) {
			throw e;
		} catch (Exception e) {
			throw new BTTInitException("Failed in initialize component: \"" + comp.getName() + "\""+ e);
		}
	}

	/**
	 * Get if initialization finished
	 * @return
	 */
	public static boolean isInitialized() {
		return status == Status.INITIALIZED_OK ;
	}
	/**
	 * get initialization status
	 * @return
	 */
	public static Status getInitializedStatus() {
		return status;
	}

	private static void initializeAllComponents(KeyedCollection components) throws BTTInitException{
		//Check License
		try {
			if(null != components.getValueAt(CSConstants.CHANNELHANDLERS)){
// BTT License				
//				License.checkLicense(components);
			}
		} catch (DSEObjectNotFoundException e1) {
			System.out.println("not find "+CSConstants.CHANNELHANDLERS);
//		} catch (BTTInitException e){
//			e.printStackTrace();
//			throw new BTTInitException("License check failed");
		}
		
		List<KeyedCollection> result = reorderComponents(components);
		for (int i = 0; i < result.size(); i++) {
			KeyedCollection comp = result.get(i);
			System.out.println("Initialize BTT Component: " + comp.getName());
			try {
				initializeComponent(comp);
			} catch (BTTInitException e) {
				System.out.println("Initialize BTT Component: " + comp.getName() + "\t[Failed]");
				System.out.println("The configuration detail for the component \"" + comp.getName()
						+ "\" is:");
				System.out.println(comp);
				throw e;
			}
			System.out.println("Initialize BTT Component: " + comp.getName() + "\t[Success]");
		}

		System.out.println(result.size() + " BTT Components initialized.");
	}

	/**
	 * Remove initialization hidden rules like: <br/>
	 * 1. forced first component must be trace <br/> 
	 * 2. service must be in front of context <br/> 
	 * 3. data must be in front of context" <br/>
	 * @param components
	 * @return
	 * @throws BTTInitException
	 */
	private static List<KeyedCollection> reorderComponents(KeyedCollection components) throws BTTInitException {
		List<KeyedCollection> result = new ArrayList<KeyedCollection>();
		KeyedCollection traceKColl=null, serviceKColl=null, dataKColl=null, settingsKColl=null;
		for (int i=0; i < components.size(); i++) {
			try {
				KeyedCollection comp = (KeyedCollection)components.getElementAt(i);
				String klassN = (String) comp.getValueAt("initializer");
				try {
					Class klass = BTTClassLoader.loadClass(klassN);
					if (TraceInitializer.class.isAssignableFrom(klass)) {
						traceKColl = comp;
					} else if (GlobalSettings.class.isAssignableFrom(klass)) {
						settingsKColl = comp;
					} else if (ServiceInitializer.class.isAssignableFrom(klass)) {
						serviceKColl = comp;
					} else if (DataInitializer.class.isAssignableFrom(klass)) {
						dataKColl = comp;
					} else {
						result.add(comp);
					}
				} catch (ClassNotFoundException e) {
					throw new BTTInitException("Failed in loading initializer class" + klassN, e);
				}
			} catch (DSEObjectNotFoundException e) {
				throw new BTTInitException(e);
			}
		}
		if (serviceKColl != null)
			result.add(0, serviceKColl);
		if (dataKColl != null)
			result.add(0, dataKColl);
		if (traceKColl != null)
			result.add(0, traceKColl);
		if (settingsKColl != null)
			result.add(0, settingsKColl);
		return result;
	}

	private static void initializeComponent(KeyedCollection comp)
			throws BTTInitException {
		try {
			String klassN = (String) comp.getValueAt("initializer");
			Class klass = BTTClassLoader.loadClass(klassN);
			java.lang.reflect.Constructor con = klass
					.getDeclaredConstructor(new Class[] {});
			Initializer init = (Initializer) con.newInstance(new Object[] {});
			init.cleanup(comp);
			init.initialize(comp);
		} catch (BTTInitException e) {
			throw e;
		} catch (Exception e) {
			throw new BTTInitException("Failed in initialize component: \"" + comp.getName() + "\"", e);
		}

	}
}
