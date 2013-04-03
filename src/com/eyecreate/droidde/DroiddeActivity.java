package com.eyecreate.droidde;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class DroiddeActivity extends Activity {
	
	FragmentManager fragman;
	Project loadedProject;
	static final int DIALOG_NEW_FILE_ID = 0;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if(!getIntent().getData().getPath().isEmpty())
        {
        	if(!checkFileForProject(getIntent().getData().getPath()))
        	{
        		throwInvalid();
        	}
        }
        else{
        	throwInvalid();
        }
        loadedProject = null;
        if(findProjectType(getIntent().getData().getPath()).equals(ProjectTypes.ANDROID.name())) loadedProject = new AndroidProject(getIntent().getData().getPath());
        if(loadedProject != null && loadedProject.isValid()) setUpProjectSpace(loadedProject);
        if(loadedProject == null || !loadedProject.isValid()) findFaultAndNotify();
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }
    
    protected Dialog onCreateDialog(int id)
    {
    	final Dialog dialog;
    	switch(id)
    	{
    	case DIALOG_NEW_FILE_ID:
    		//Initialize the dialog
			dialog = new Dialog(this);
			dialog.setContentView(R.layout.newfiledialog);
			dialog.setTitle("New File");
			//Set up the spinner
			final Spinner fileTypes = (Spinner)dialog.findViewById(R.id.fileTypes);
			ArrayList<String> fileTypeArrayList = new ArrayList<String>();
			for(String s : loadedProject.getProjectType().getAcceptedFileTypes())
				fileTypeArrayList.add(s);
			String [] fileTypeArray = fileTypeArrayList.toArray(new String[fileTypeArrayList.size()]);
			//connect buttons
			Button create = (Button)dialog.findViewById(R.id.createNewFile);
			create.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					dialog.cancel();
					//should really do some checks here to make sure bad values don't mess things up.
					//including quick sanity check on location, though
					if(!((EditText)dialog.findViewById(R.id.fileLocation)).getText().toString().contains(loadedProject.getProjectDir().getAbsolutePath())){
						Toast.makeText(getBaseContext(), "File not created. Location is not in project directory.", Toast.LENGTH_LONG);
						return;
					}
					File newFile = new File( ((EditText)dialog.findViewById(R.id.fileLocation)).getText().toString()+File.separator+((EditText)dialog.findViewById(R.id.fileName)).getText().toString()+"."+((String) fileTypes.getSelectedItem()) );
					if(!newFile.exists())
						try {
							newFile.createNewFile();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							Toast.makeText(getBaseContext(), "File not created. Problem creating file.", Toast.LENGTH_LONG);
							e.printStackTrace();
							return;
						}
					loadedProject.addNewFileToProject(newFile);
		    		fragman=getFragmentManager();
		    		ProjectFilesFragment projFiles = (ProjectFilesFragment) fragman.findFragmentById(R.id.projectfiles);
		    		projFiles.AddFilesToList(loadedProject.getProjectFiles());
					
				}
			});
			//Initialize adapter
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item,
					fileTypeArray);
			fileTypes.setAdapter(adapter);
			break;
    	default:
    		dialog=null;
    	}
    	return dialog;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId())
    	{
	    	case R.id.saveproj:
	    		loadedProject.triggerProjectStateSave();
	    		return true;
	    	case R.id.run:
	    		if(!loadedProject.runProject(this)) Toast.makeText(getBaseContext(), "Project failed to run!", Toast.LENGTH_LONG);
	    		return true;
	    	case R.id.addfile:
	    		showDialog(DIALOG_NEW_FILE_ID);
	    		return true;
	        default:
	            return super.onOptionsItemSelected(item);
    	}
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	if(requestCode == 42 && resultCode==RESULT_CANCELED)
    	{
    		Toast.makeText(getBaseContext(), "Problem while running/compiling.", Toast.LENGTH_LONG);
    	}
    	if(requestCode == 42 && resultCode == RESULT_OK)
    	{
    		loadedProject.handleRunResult(data);
    	}
    }

	private void findFaultAndNotify() {
		//This needs to do something besides say oops.
		Toast.makeText(getApplicationContext(), "A problem was found loading the project. Please examine Logcat.",Toast.LENGTH_LONG).show();
		
	}

	private void setUpProjectSpace(Project loadedProject) {
		// TODO Auto-generated method stub
		//Check if some specific files are missing and notify. This might be a good idea to put into a Project subclass in the future.
		fragman=getFragmentManager();
		ProjectFilesFragment projFiles = (ProjectFilesFragment) fragman.findFragmentById(R.id.projectfiles);
		for(File f : loadedProject.getProjectFiles())
		{
			if(loadedProject.getProjectType().equals(ProjectTypes.ANDROID)){
				if(f.getName().equals("android.jar") && !f.exists())
				Toast.makeText(getApplicationContext(), "File android.jar was missing from project folder. Please copy this from an android SDK to your project folder.",Toast.LENGTH_LONG).show();
			}
		}
		projFiles.AddFilesToList(loadedProject.getProjectFiles());
        /*FragmentTransaction editortrans = fragman.beginTransaction();
        Fragment editorfrag = new EditorFragment();
        editortrans.add(R.id.mainlayout,editorfrag);
        editortrans.commit();*/
		
	}

	private void throwInvalid() {
		Toast.makeText(getApplicationContext(),R.string.invalidProject,5).show();
		this.finish();
		
	}
	
	public void openFileInEditor(File f)
	{
		EditorFragment edFragment = (EditorFragment) fragman.findFragmentById(R.id.fileeditor);
		edFragment.openFile(f);
	}
	
	public void removeFileFromProject(File f)
	{
		ProjectFilesFragment projectFiles = (ProjectFilesFragment) fragman.findFragmentById(R.id.projectfiles);
		loadedProject.removeFileFromProject(f);
		projectFiles.AddFilesToList(loadedProject.getProjectFiles());
	}

	private boolean checkFileForProject(String path) {
		DocumentBuilder builder;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(new File(path));
			if(doc.getChildNodes().item(0).getNodeName().toLowerCase().equals("project")){
				return true;
			}
			else{
				return false;
			}
		}catch (ParserConfigurationException e1) {
			e1.printStackTrace();
			return false;
		} 
		catch (SAXException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	private String findProjectType(String path){
		DocumentBuilder builder;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(new File(path));
			if(doc.getChildNodes().item(0).getNodeName().toLowerCase().equals("project")){
				return doc.getChildNodes().item(0).getAttributes().getNamedItem("type").getTextContent();
			}
		}catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		} 
		catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public void setProjectMainFile(File file){
		loadedProject.setMainProjectFile(file);
	}
	
	public File getProjectMainFile()
	{
		return loadedProject.getMainProjectFile();
	}
}
