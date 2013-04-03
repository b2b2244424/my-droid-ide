package com.eyecreate.droidde;

import java.io.File;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.util.Log;

public class AndroidProject implements Project {

	private Document projectXML = null;
	private DocumentBuilder dBuilder = null;
	private boolean isValid;
	private ProjectTypes projectType = ProjectTypes.ANDROID;
	private List<File> projectFiles = new ArrayList<File>();
	private List<File> projectLibs = new ArrayList<File>();
	AndroidRunner andRun = new AndroidRunner();
	private String projectName;
	private File projectFile;
	private String projectAuthor = "";
	private File mainProjectFile;
	
	//This class manages the project structure from the project XML and gives other classes information about the loaded project.
	//In order to make error finding easier, pass a Directory, name, and type when you need a new project and the project.dpj when you need it loaded.
	public AndroidProject(String path) {
		initialSanityChecks(path);
		try {
			dBuilder=DocumentBuilderFactory.newInstance().newDocumentBuilder();
			projectXML = dBuilder.parse(new File(path));
			if(projectXML.getChildNodes().item(0).getNodeName().toLowerCase().equals("project")){
				isValid=true;
				projectFile=new File(path);
				processXMLForLoad();
			}
			else{
				isValid=false;
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			isValid = false;
		} catch (SAXException e) {
			e.printStackTrace();
			isValid = false;
		} catch (IOException e) {
			e.printStackTrace();
			isValid = false;
		}
	}
	
	public AndroidProject(String path, String name, String type) {
		try {
			dBuilder=DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File projectDir=new File(path);
		if(projectDir.isDirectory() && projectDir.list().length==0)
		{
			createProject(new File(path),name,type);
			isValid=true;
		}
		//this part is needed to allow "importing" existing project
		else if(projectDir.isDirectory() && projectDir.list().length!=0 && Arrays.asList(projectDir.list()).contains("AndroidManifest.xml"))
		{
			createProject(new File(path),name,type);
			isValid=true;
		}
		else
		{
			Log.e("Droidde","Project directory is not empty. Must be empty in order to start a new project.");
			isValid=false;
		}
	}
	
	private void initialSanityChecks(String path) {
		File filePath = new File(path);
		if(!filePath.exists()) isValid=false;
		
	}

	private void createProject(File filePath,String name, String type)
	{
		try{
			projectType = ProjectTypes.valueOf(type.toUpperCase());
			projectName = name;
			projectFile = new File(filePath.getAbsolutePath()+File.separator+projectName+".dpj");
			createProjectFileList(filePath);
			for(String s: projectType.getDefaultLibs()){
				if(!(new File(filePath.getAbsolutePath()+File.separator+s).exists())){
					Log.w("Droidde","File "+filePath.getAbsolutePath()+File.separator+s+" was not found. You might want to find out why this file wasn't found.");
				}
				projectLibs.add(new File(filePath.getAbsolutePath()+File.separator+s));
			}
			triggerProjectStateSave();
		}
		catch(IllegalArgumentException e){
			Log.e("Droidde", "Invalid project type given to createProject");
			isValid=false;
		}
	}
	
	public void triggerProjectStateSave() {
		processProjectForSave();
		
	}

	private void createProjectFileList(File filePath)
	{
		recursiveDirectorySearch(filePath);
	}
	
	private void recursiveDirectorySearch(File dir)
	{
		File[] dirs = dir.listFiles();
		for (File f :dirs)
		{
			if(f.isDirectory()){
				recursiveDirectorySearch(f);
			}
			if(f.isFile() && projectType.isAcceptedFile(f.getName().split("\\.")[f.getName().split("\\.").length-1])){
				if(!f.getName().toLowerCase().equals("r.java")) projectFiles.add(f);
			}
		}
	}
	
	public boolean runProject(Activity activity)
	{
		return andRun.runProject(this, activity);
	}
	
	public void handleRunResult(Intent data)
	{
		andRun.handleRunResult(data);
	}
	
	private void processXMLForLoad()
	{
		Node root=projectXML.getChildNodes().item(0);
		projectName = root.getAttributes().getNamedItem("name").getTextContent();
		projectAuthor = root.getAttributes().getNamedItem("author").getTextContent();
		Node libs = null;
		Node sources = null;
		for(Node n : nodeList(root.getChildNodes()))
		{
			if(n.getNodeName().toLowerCase().equals("libs")) libs = n;
			if(n.getNodeName().toLowerCase().equals("source")) sources = n;
		}
		if(libs != null){
			for(Node n : nodeList(libs.getChildNodes()))
			{
				if(n.getNodeName().toLowerCase().equals("file")) projectLibs.add(new File(projectDirFromPath(projectFile.getAbsolutePath())+n.getAttributes().getNamedItem("src").getTextContent()));
			}
		}
		if(sources != null){
			for(Node n : nodeList(sources.getChildNodes()))
			{
				if(n.getNodeName().toLowerCase().equals("file")) projectFiles.add(new File(projectDirFromPath(projectFile.getAbsolutePath())+n.getAttributes().getNamedItem("src").getTextContent()));
				if(n.getAttributes().getNamedItem("mainfile") != null) mainProjectFile = new File(projectDirFromPath(projectFile.getAbsolutePath())+n.getAttributes().getNamedItem("src").getTextContent());
			}
		}
	}
	
	private List<Node> nodeList(final NodeList list) {
		return new AbstractList<Node>() {
			public int size() {
				return list.getLength();
			}

			public Node get(int index) {
				Node item = list.item(index);
				if (item == null)
					throw new IndexOutOfBoundsException();
				return item;
			}
		};
	}
	
	private File projectDirFromPath(String path)
	{
		String finalPath = "";
		for(String s:path.split(File.separator))
		{
			if(!s.toLowerCase().contains(".dpj"))finalPath+=s+File.separator;
		}
		return new File(finalPath);
	}
	
	private String relativeFilePathFromFile(File f)
	{
		return f.getAbsolutePath().replace(projectDirFromPath(projectFile.getAbsolutePath()).getAbsolutePath(),"");
	}
	
	private void processProjectForSave()
	{
		projectXML=dBuilder.newDocument();
		Element root = projectXML.createElement("project");
		root.setAttribute("name", projectName);
		root.setAttribute("author", projectAuthor);
		root.setAttribute("type", projectType.name());
		Element libs = projectXML.createElement("libs");
		for(File f : projectLibs)
		{
			Element e = projectXML.createElement("file");
			e.setAttribute("src",relativeFilePathFromFile(f));
			libs.appendChild(e);
		}
		Element files = projectXML.createElement("source");
		for(File f : projectFiles)
		{
			Element e = projectXML.createElement("file");
			e.setAttribute("src",relativeFilePathFromFile(f));
			//TODO: find way to set mainproject file outside loading.
			if(mainProjectFile != null && f.getAbsolutePath().equals(mainProjectFile.getAbsolutePath())) e.setAttribute("mainfile", "true");
			files.appendChild(e);
		}
		root.appendChild(libs);
		root.appendChild(files);
		projectXML.appendChild(root);
		writeXmlFile(projectXML, projectFile);
	}
	
	private void writeXmlFile(Document doc, File filename) {
	    try {
	        // Prepare the DOM document for writing
	        Source source = new DOMSource(doc);

	        // Prepare the output file
	        Result result = new StreamResult(filename);

	        // Write the DOM document to the file
	        Transformer xformer = TransformerFactory.newInstance().newTransformer();
	        xformer.transform(source, result);
	    } catch (TransformerConfigurationException e) {
	    } catch (TransformerException e) {
	    }
	}
	
	/* (non-Javadoc)
	 * @see com.eyecreate.Project#isValid()
	 */
	public boolean isValid(){
		return isValid;
	}
	
	/* (non-Javadoc)
	 * @see com.eyecreate.Project#getProjectFiles()
	 */
	public List<File> getProjectFiles()
	{
		return projectFiles;
	}
	
	public List<File> getProjectLibs()
	{
		return projectLibs;
	}
	
	/* (non-Javadoc)
	 * @see com.eyecreate.Project#getProjectType()
	 */
	public ProjectTypes getProjectType()
	{
		return projectType;
	}
	
	public File getProjectDir()
	{
		return new File(projectDirFromPath(projectFile.getAbsolutePath()).getAbsolutePath());
	}
	
	public String getProjectName()
	{
		return projectName;
	}
	
	public String getMainFile()
	{
		return mainProjectFile.getAbsolutePath();
	}
	
	public void setMainProjectFile(File f)
	{
		mainProjectFile=f;
	}
	
	public void addNewFileToProject(File file)
	{
		if(!projectFiles.contains(file)) projectFiles.add(file);
		triggerProjectStateSave();
	}
	
	public void removeFileFromProject(File file)
	{
		if(projectFiles.remove(file)) triggerProjectStateSave();
	}
	
	public File getMainProjectFile()
	{
		return mainProjectFile;
	}

}
