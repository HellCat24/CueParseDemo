package com.example.simplecueplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.cueparse.media.Track;

public class LocalMediaProvider {

	private LocalMediaProvider() {
	}

	private static LocalMediaProvider thisProvider = new LocalMediaProvider();

	private List<Track> mAudioList;

	private String SD_CARD = "sdCard";
	private String EXTERNAL_SD_CARD = "externalSdCard";

	private Map<String, List<Track>> mMusicFolders;

	public Map<String, List<Track>> getMusicFolders() {
		return mMusicFolders;
	}

	public void setAudiosFromFolders(String folder) {
		mAudioList = mMusicFolders.get(folder);
	}

	public void setAudios(List<Track> list) {
		this.mAudioList = list;
	}

	public static LocalMediaProvider getInstance() {
		return thisProvider;
	}

	public List<Track> getAudioList(Context context) {
		if (mAudioList != null) {
			return mAudioList;
		}
		getAudios(context);
		return mAudioList;
	}

	public void resetAudios(Context context) {
		getAudios(context);
	}

	public void setAudioList(List<Track> mAudioList) {
		this.mAudioList = mAudioList;
	}

	public void setDeviceAudios(Context context) {
		getAudios(context);
	}

	private void getAudios(Context context) {
		Set<String> tempSet = new HashSet<String>();
		Map<String, List<Track>> tempMap = new HashMap<String, List<Track>>();
		List<Track> temp = new ArrayList<Track>();

		String[] projection = { MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.DISPLAY_NAME };

		ContentResolver contentResolver = context.getContentResolver();
		Uri uriExternal = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor cursor = contentResolver.query(uriExternal, projection,
				MediaStore.Audio.Media.IS_MUSIC + " != 0", null, null);
		Track tempFile = null;
		if (cursor != null) {
			while (cursor.moveToNext()) {
				tempFile = new Track();
				tempFile.setFilePath(cursor.getString(0));
				tempFile.setTitle(cursor.getString(1));
				tempFile.setArtist(cursor.getString(2));
				tempFile.setDuration((cursor.getInt(3) / 1000));
				String folder = (cursor.getString(0)).substring(0, (cursor.getString(0)).lastIndexOf("/") + 1);
				temp.add(tempFile);
				if (tempSet.add(folder)) {
					tempMap.put(folder, new ArrayList<Track>());
					tempMap.get(folder).add(tempFile);
				} else {
					tempMap.get(folder).add(tempFile);
				}
			}
			cursor.close();
		}
		this.mAudioList = temp;
		this.mMusicFolders = tempMap;
	}

	public boolean isDatabaseReacheble(Context context) {
		String[] projection = { MediaStore.Audio.Media.DATA };
		ContentResolver contentResolver = context.getContentResolver();
		Uri uriExternal = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor cursor = contentResolver.query(uriExternal, projection,
				MediaStore.Audio.Media.IS_MUSIC + " != 0", null, null);
		if (cursor != null) {
			return true;
		}
		return false;
	}

	public int getAllStorageCount() {
		Map<String, File> map = new HashMap<String, File>(10);

		List<String> mMounts = new ArrayList<String>(10);
		List<String> mVold = new ArrayList<String>(10);
		mMounts.add("/mnt/sdcard");
		mVold.add("/mnt/sdcard");

		try {
			File mountFile = new File("/proc/mounts");
			if (mountFile.exists()) {
				Scanner scanner = new Scanner(mountFile);
				while (scanner.hasNext()) {
					String line = scanner.nextLine();
					if (line.startsWith("/dev/block/vold/")) {
						String[] lineElements = line.split(" ");
						String element = lineElements[1];

						// don't add the default mount path
						// it's already in the list.
						if (!element.equals("/mnt/sdcard"))
							mMounts.add(element);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			File voldFile = new File("/system/etc/vold.fstab");
			if (voldFile.exists()) {
				Scanner scanner = new Scanner(voldFile);
				while (scanner.hasNext()) {
					String line = scanner.nextLine();
					if (line.startsWith("dev_mount")) {
						String[] lineElements = line.split(" ");
						String element = lineElements[2];

						if (element.contains(":"))
							element = element
									.substring(0, element.indexOf(":"));
						if (!element.equals("/mnt/sdcard"))
							mVold.add(element);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (int i = 0; i < mMounts.size(); i++) {
			String mount = mMounts.get(i);
			if (!mVold.contains(mount))
				mMounts.remove(i--);
		}
		mVold.clear();

		List<String> mountHash = new ArrayList<String>(10);

		for (String mount : mMounts) {
			File root = new File(mount);
			if (root.exists() && root.isDirectory() && root.canWrite()) {
				File[] list = root.listFiles();
				String hash = "[";
				if (list != null) {
					for (File f : list) {
						hash += f.getName().hashCode() + ":" + f.length()
								+ ", ";
					}
				}
				hash += "]";
				if (!mountHash.contains(hash)) {
					String key = SD_CARD + "_" + map.size();
					if (map.size() == 0) {
						key = SD_CARD;
					} else if (map.size() == 1) {
						key = EXTERNAL_SD_CARD;
					}
					mountHash.add(hash);
					map.put(key, root);
				}
			}
		}

		mMounts.clear();

		if (map.isEmpty()) {
			map.put(SD_CARD, Environment.getExternalStorageDirectory());
		}
		return map.size();
	}

}
