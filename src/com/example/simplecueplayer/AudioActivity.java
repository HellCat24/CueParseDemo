package com.example.simplecueplayer;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cueparse.media.Track;

public class AudioActivity extends Activity {

	private ListView musicList;
	
	private MusicAdapter aduioAdapter;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Bundle bundle = this.getIntent().getExtras();
		List<Track> mAudios = (List<Track>) bundle.getSerializable("tracks");
		musicList = (ListView) findViewById(R.id.audioList);
		aduioAdapter = new MusicAdapter(mAudios);
		musicList.setAdapter(aduioAdapter);
	}	
	
	private class MusicAdapter extends BaseAdapter{

		private List<Track> mAudios;
		
		public MusicAdapter(List<Track> mAudios){
			super();
			this.mAudios = mAudios;
		}
		
		@Override
		public int getCount() {
			return mAudios.size();
		}

		@Override
		public Object getItem(int position) {
			return mAudios.get(position);
		}

		@Override
		public long getItemId(int position) {
			return Integer.parseInt(mAudios.get(position).getId().toString());
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Track curTrack = mAudios.get(position);
			ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.audio_item, null, false);
				holder = new ViewHolder();
				holder.id = (TextView) convertView.findViewById(R.id.id);
				holder.title = (TextView) convertView.findViewById(R.id.title);
				holder.artist = (TextView) convertView.findViewById(R.id.artist);
				holder.album = (TextView) convertView.findViewById(R.id.album);
				holder.file = (TextView) convertView.findViewById(R.id.file);
				holder.duration = (TextView) convertView.findViewById(R.id.duration);
				holder.startPosition = (TextView) convertView.findViewById(R.id.startPosition);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.id.setText(getResources().getText(R.string.id) + curTrack.getId().toString());
			holder.title.setText(getResources().getText(R.string.title) + curTrack.getTitle()); 
			holder.artist.setText(getResources().getText(R.string.artist) + curTrack.getArtist()); 
			holder.album.setText(getResources().getText(R.string.album) + curTrack.getAlbum());
			holder.file.setText(getResources().getText(R.string.file) + curTrack.getFile()); 
			holder.duration.setText(getResources().getText(R.string.duration) + Integer.toString(curTrack.getDuration()));
			holder.startPosition.setText(getResources().getText(R.string.startPosition) + Integer.toString(curTrack.getStartPosition()));
			return convertView;
		}
	}
	
	private static class ViewHolder {
		TextView id;
		TextView title;
		TextView artist;
		TextView album;
		TextView file;
		TextView duration;
		TextView startPosition;
	}
}