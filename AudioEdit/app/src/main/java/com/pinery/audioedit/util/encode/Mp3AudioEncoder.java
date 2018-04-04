package com.pinery.audioedit.util.encode;

import android.os.Build;
import android.support.annotation.RequiresApi;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
class Mp3AudioEncoder extends AudioEncoder {

	private final static String TAG = "Mp3AudioEncoder";

	Mp3AudioEncoder(String rawAudioFile) {
		super(rawAudioFile);
	}

	@Override
	public void encodeToFile(String outEncodeFile) {
		FileInputStream fisRawAudio = null;
		FileOutputStream fosAccAudio = null;
		try {
			fisRawAudio = new FileInputStream(rawAudioFile);
			fosAccAudio = new FileOutputStream(outEncodeFile);


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			try {
				if (fisRawAudio != null)
					fisRawAudio.close();
				if (fosAccAudio != null)
					fosAccAudio.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}






}
