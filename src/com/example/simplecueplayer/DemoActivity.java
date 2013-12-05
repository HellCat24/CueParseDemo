package com.example.simplecueplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.cueparse.media.CueParse;
import com.cueparse.media.CueParseException;
import com.cueparse.media.Track;

@SuppressLint("NewApi")
public class DemoActivity extends Activity {
	
	private Map<String,List<Track>> folders;
	
	private ListView folderList;
	
	private ArrayAdapter<String> folderAdapter;
	
	private int storageCount = LocalMediaProvider.getInstance().getAllStorageCount();
	
	private int tempStorageCount = 0;
	
	private CueParse mCueParse;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		folderList = (ListView) findViewById(R.id.audioList);
		mCueParse = new CueParse();
		
		IntentFilter filter = new IntentFilter();
		filter.addDataScheme("file");
        filter.addAction(Intent.ACTION_MEDIA_SHARED);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);

        registerReceiver(mediaMountedReceiver, filter);
		if(canWriteToFlash()){
			initialize(savedInstanceState);
		} else {
			Toast.makeText(getApplicationContext(), "Sorry, SD card is busy at this moment", Toast.LENGTH_LONG).show();
		}
	}
	
	
	BroadcastReceiver mediaMountedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
	        if(action.equals(Intent.ACTION_MEDIA_SHARED)){
	        	Toast.makeText(getApplicationContext(), "Sorry, SD card is busy at this moment", Toast.LENGTH_LONG).show();
	        } else if(action.equals(Intent.ACTION_MEDIA_MOUNTED)){
	        } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
	        	tempStorageCount++;
	        	if(tempStorageCount == storageCount){
	        		initialize(null);
	        		tempStorageCount = 0;
	        	}
			}
		}
	};
	
	private boolean canWriteToFlash() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)){
	    	return true;
	    } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return false;
	    } else {
	        return false;
	    }
	}
	
	private void initialize(Bundle savedInstancestate) {

		LocalMediaProvider.getInstance().getAudioList(getApplicationContext());
		folders = LocalMediaProvider.getInstance().getMusicFolders();
		List<String> musicFolders = new ArrayList<String>(folders.size());
		for (Map.Entry<String, List<Track>> cursor : folders.entrySet()) {
			musicFolders.add(cursor.getKey());
		}
		folderAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.folder_view, R.id.folder, musicFolders);
		folderList.setAdapter(folderAdapter);
		folderList.setOnItemClickListener( new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				try {
					mCueParse.parseFolderWithCue(new File(folderAdapter.getItem(position)));
					if(mCueParse.getTracks().size()>0){
						Intent audioActivity = new Intent(getApplicationContext(), AudioActivity.class);
						audioActivity.putParcelableArrayListExtra("tracks", (ArrayList<? extends Parcelable>) mCueParse.getTracks());
						startActivity(audioActivity);
					}
				} catch (CueParseException e) {
					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	protected void onDestroy() {
		this.unregisterReceiver(mediaMountedReceiver);
		super.onDestroy();
	}
	
	
}