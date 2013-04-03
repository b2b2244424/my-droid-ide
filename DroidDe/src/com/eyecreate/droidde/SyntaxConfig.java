package com.eyecreate.droidde;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SyntaxConfig {
	private File extStorage = Environment.getExternalStorageDirectory();
	private File confDir = new File(this.extStorage, "droidde-config");
	private File configFile = new File(this.confDir, "syntax.xml");
	private Document configXML = null;
	private DocumentBuilder dBuilder = null;
	private DocumentBuilderFactory dbFactory = DocumentBuilderFactory
			.newInstance();
	private Boolean finishedParse = Boolean.valueOf(false);
	private Map<String, Map<String, String>> languageRegex = new HashMap<String, Map<String, String>>();

	public SyntaxConfig() {
		if (!this.confDir.exists())
			this.confDir.mkdirs();
		if (this.configFile.exists()) {
			initializeRegex();
		} else {
			Log.w("Droidde",
					"Can not find config file. Do you have external storage?");
		}
	}

	private String getClonedValue(String valueName, String languageType) {
		String str = "";
		try {
			this.dBuilder = this.dbFactory.newDocumentBuilder();
			this.configXML = this.dBuilder.parse(this.configFile);
			NodeList localNodeList = this.configXML.getChildNodes().item(0)
					.getChildNodes();
			for (int i = 0; i < localNodeList.getLength(); i++) {
				if (localNodeList.item(i).getNodeType() != 1)
					continue;
				// Node is an element at this point
				Node localNode = localNodeList.item(i);
				if (!localNode.getNodeName().equals(languageType))
					continue;
				str = ((Element) localNode).getElementsByTagName(valueName)
						.item(0).getTextContent();
			}
		} catch (Exception localException) {
			Log.w("Droidde",
					"I did something that made a value's clone unhappy.");
		}
		return str;
	}

	private void initializeRegex() {
		try {
			this.dBuilder = this.dbFactory.newDocumentBuilder();
			this.configXML = this.dBuilder.parse(this.configFile);
			NodeList localNodeList1 = this.configXML.getChildNodes().item(0)
					.getChildNodes();
			int i = 0;
			int j = localNodeList1.getLength();
			while (!finishedParse) {
				if (i >= j) {
					this.finishedParse = Boolean.valueOf(true);
					return;
				}
				if (localNodeList1.item(i).getNodeType() == 1) {
					// node of current language
					Node localNode = localNodeList1.item(i);
					// name of current language
					String str1 = localNode.getNodeName();
					// child elements of current language
					NodeList localNodeList2 = localNode.getChildNodes();
					// create hashmap of containing regex values along with
					// extra
					// Node for what files it opens
					HashMap<String, String> localHashMap = new HashMap<String, String>();
					localHashMap.put("FileTypes",
							((Element) localNode).getAttribute("filetypes"));
					int k = 0;
					boolean moreNodes=true;
					// start looping through regex values and put into main
					// Hashmap
					// when done.
					while (moreNodes) {
						if (k >= localNodeList2.getLength()) {
							this.languageRegex.put(str1, localHashMap);
							moreNodes=false;
						} else {
							Object localObject = localNodeList2.item(k);
							if (((Node) localObject).getNodeType() != 1) {
								k++;
							} else {
								// node object is confirmed as Element
								localObject = (Element) localObject;
								String regexValue;
								if (((Element) localObject)
										.hasAttribute("cloneof")) {
									regexValue = getClonedValue(
											((Element) localObject)
													.getNodeName(),
											((Element) localObject)
													.getAttribute("cloneof"));
								} else {
									regexValue = ((Element) localObject)
											.getTextContent();
								}
								localHashMap.put(
										((Element) localObject).getNodeName(),
										regexValue);
								k++;
							}
						}
					}
				}
				i++;
			}
		} catch (Exception localException) {
			Log.w("Droidde", "I've failed to read the syntax file."+localException.getMessage());
		}
	}

	public String regexValue(String languageType, String valueName) {
		// get regex value from Map
		if(languageType.equals("")) return "";
		return (String) ((Map<String, String>) languageRegex.get(languageType))
				.get(valueName);
	}
	
	public String languageFromFileType(String s){
		for(String l : languageRegex.keySet())
		{
			//Get language type and split into file extensions
			for(String type :((String) ((Map<String,String>) languageRegex.get(l)).get("FileTypes")).split(","))
			{
				if(type.equals(s)) return l;
			}
		}
		return "";
	}
}

/*
 * Recovered/Decompiled Class (restructure needed) Qualified Name:
 * com.eyecreate.SyntaxConfig JD-Core Version: 0.6.0
 */
