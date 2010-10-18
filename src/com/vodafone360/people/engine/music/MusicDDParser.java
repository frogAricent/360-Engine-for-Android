package com.vodafone360.people.engine.music;
/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the license at
 * src/com/vodafone360/people/VODAFONE.LICENSE.txt or
 * http://github.com/360/360-Engine-for-Android
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at src/com/vodafone360/people/VODAFONE.LICENSE.txt.
 * If applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 * Copyright 2010 Vodafone Sales & Services Ltd.  All rights reserved.
 * Use is subject to license terms.
 */


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.vodafone360.people.datatypes.MusicDDObject;


/**
 * This Class Parses the Deployment Descriptor into the MusicDeploymentDescriptor Object
 *
 */

public class MusicDDParser {

	/**
	 * @param args
	 */
	
	private static final String NAME = "name";
	private static final String INSTALL_NOTIFY_URL = "installNotifyURI";
	private static final String SUPPRESS_USER_CONF = "suppressUserConfirmation";
	private static final String ICON_URI = "iconURI";
	private static final String SIZE = "size";
	private static final String TYPE = "type";
	private static final String OBJECT_ID= "objectID";
	private static final String SERVER = "server";
	
	
	
	public List<MusicDDObject> parse(InputStream is)
	{
		List<MusicDDObject> listOfDD = new ArrayList<MusicDDObject>();
		
		try {
			  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			  DocumentBuilder db = dbf.newDocumentBuilder();
			  Document doc = db.parse(is);
			  doc.getDocumentElement().normalize();
			  System.out.println("Root element " + doc.getDocumentElement().getNodeName());
			  NodeList nodeLst = doc.getElementsByTagName("mediaObject");
			  System.out.println("Information of all tracks"+nodeLst.getLength()+" "+nodeLst.item(0).toString());
			  
			  for (int s = 0; s < nodeLst.getLength(); s++) {

			    Node mediaNode = nodeLst.item(s);

			    if (mediaNode.getNodeType() == Node.ELEMENT_NODE) {
			    	Element element = (Element) mediaNode;
			    	MusicDDObject musicDDObject = new MusicDDObject();

			    	String name = element.getElementsByTagName(NAME).item(0).getChildNodes().item(0).getNodeValue();
			    	System.out.println("Name: " + name);
			    	musicDDObject.setName(name);
			    	
			    	String installNotifyURL = element.getElementsByTagName(INSTALL_NOTIFY_URL).item(0).getChildNodes().item(0).getNodeValue();
//			    	System.out.println("installNotifyURL: " + installNotifyURL);
			    	musicDDObject.setInstallNotifyURI(installNotifyURL);
			    	
			    	String suppressUserConfirmation = element.getElementsByTagName(SUPPRESS_USER_CONF).item(0).getChildNodes().item(0).getNodeValue();
//			    	System.out.println("suppressUserConfirmation: " + suppressUserConfirmation);
			    	musicDDObject.setSuppressUserConfirmation(suppressUserConfirmation);
			    	
			    	String iconURI = element.getElementsByTagName(ICON_URI).item(0).getChildNodes().item(0).getNodeValue();
//			    	System.out.println("iconURI: " + iconURI);
			    	musicDDObject.setIconURI(iconURI);
			    	
			    	String size = element.getElementsByTagName(SIZE).item(0).getChildNodes().item(0).getNodeValue();
//			    	System.out.println("size: " + size);
			    	musicDDObject.setSize(Long.parseLong(size));
			    	
			    	String type = element.getElementsByTagName(TYPE).item(0).getChildNodes().item(0).getNodeValue();
//			    	System.out.println("type: " + type);
			    	musicDDObject.setFileType(type);
			    	
			    	String objectID = element.getElementsByTagName(OBJECT_ID).item(0).getChildNodes().item(0).getNodeValue();
//			    	System.out.println("objectID: " + objectID);
			    	musicDDObject.setTrackID(objectID);
			    	
			    	String server = element.getElementsByTagName(SERVER).item(0).getChildNodes().item(0).getNodeValue();
//			    	System.out.println("server: " + server);
			    	musicDDObject.setDownloadURL(server);
			    	
			    	listOfDD.add(musicDDObject);
			    	
			    }
			    
			  }
//			  System.out.println("Size of list" + listOfDD.size() + " List: " + listOfDD.toString() );
		} catch (Exception e) {
			e.printStackTrace();
		}
		return listOfDD;
	}
	
	
	public static void main(String[] args) throws FileNotFoundException {
		MusicDDParser parser = new MusicDDParser();
		String ddXML = "c:\\DDXml.xml";
		InputStream is = new FileInputStream(new File(ddXML));
//		InputStream bis = new ByteArrayInputStream(ddXML.getBytes());
		parser.parse(is);
	}

}
