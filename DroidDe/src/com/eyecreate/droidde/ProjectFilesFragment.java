package com.eyecreate.droidde;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.R.color;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ProjectFilesFragment extends Fragment {

	List<File> fileList; 
	ListView lv;
	@Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                             Bundle savedInstanceState) {
	        // Inflate the layout for this fragment
	    View v = inflater.inflate(R.layout.projectfiles, container, false); 
	    lv = (ListView) v.findViewById(R.id.filelist);
	    return v;
	    }
	
	@Override
	 public void onActivityCreated(Bundle bundle) {
		 if(bundle != null) {
			 //Put stuff here if getting a present!
		 }
		 else {
			 //
		 }
		 super.onActivityCreated(bundle);
	 }
	 
	 public void AddFilesToList(List<File> files)
	 {
		 fileList = files;
		 List<String> values = new ArrayList<String>();
		 for(File f : fileList){
			 values.add(f.getName());
		 }
		 lv.setAdapter(new ArrayAdapter<String>(getActivity().getApplicationContext(),android.R.layout.simple_list_item_1,values){
			 @Override
			 public View getView(int position, View convertView, ViewGroup parent) {
			     View v = super.getView(position, convertView, parent);
			     if (((DroiddeActivity)getActivity()).getProjectMainFile() != null && ((DroiddeActivity)getActivity()).getProjectMainFile().getName().equals(((TextView)v).getText())) {
			         v.setBackgroundColor(Color.DKGRAY);
			     }
			     else{
			    	 v.setBackgroundColor(color.background_dark);
			     }
			     return v;
			 }
		 });
		 //This part creates the listener for list clicks
		 final OnItemClickListener newListener = new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
				requestOpenFile(fileList.get(position));
			}
		    };
		 final OnItemLongClickListener longListener = new OnItemLongClickListener() {
			 public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) 
			 {
				 
				 lv.invalidateViews();
				 ((DroiddeActivity)getActivity()).setProjectMainFile(fileList.get(pos));
				 Toast.makeText(getActivity().getApplicationContext(), "Changed main file.", Toast.LENGTH_SHORT).show();
				 return true;
			 }
		};
		lv.setOnItemLongClickListener(longListener);
		lv.setOnItemClickListener(newListener);

	 }
	 
	 public void requestOpenFile(File f){
		 DroiddeActivity da = (DroiddeActivity) getActivity();
		 da.openFileInEditor(f);
	 }


}
