package com.ktds.erpbarcode.common.media;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.Log;

import com.ktds.erpbarcode.R;

public class BarcodeSoundPlay {
	private static final String TAG = "BarcodeSoundPlay";
	
	public static final int SOUND_NOT_PLAY = -1;
	public static final int SOUND_DUPLICATION = 100;
	public static final int SOUND_NOTEXISTS = 101;
	public static final int SOUND_ERROR = 102;
	public static final int SOUND_ASTERISK = 103;
	public static final int SOUND_JUNG = 104;
	public static final int SOUND_NOTIFY = 105;
	public static final int SOUND_SCAN = 106;
	public static final int SOUND_SCANBELOW = 107;
	public static final int SOUND_SENDQUESTION = 108;
	public static final int SOUND_NOTLOCATION = 109;
	public static final int SOUND_NINE = 110;
	public static final int SOUND_JINWOO_SLOW = 111;
	
	public static final int SOUND_BARCODE = 900;

	private Context mContext;
	
	private AudioManager mAudioManager;
	private SoundPool mSoundPool;

	//private static int sound;
	//private static int sound_99, sound_0, sound_1, sound_2, sound_3, sound_4, sound_5, sound_6, sound_7, sound_8, sound_9, 
	//	sound_a, sound_b, sound_c, sound_d, sound_e, sound_f, sound_g, sound_h, sound_i, sound_j,
	//	sound_k, sound_l, sound_m, sound_n, sound_o, sound_p, sound_q, sound_r, sound_s, sound_t, 
	//	sound_u, sound_v, sound_w, sound_x, sound_y, sound_z;
	
	//private boolean mSoundLoaded = false;

	
	public BarcodeSoundPlay(Context context) {
		Log.i(TAG, "BarcodeSoundPlay  Start...");
		
		mContext = context;

		//this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

		// Load the sound
		mSoundPool = new SoundPool(40, AudioManager.STREAM_MUSIC, 0);

		mSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				//mSoundLoaded = true;
				Log.i(TAG, "BarcodeSoundPlay  SoundLoaded==true  sampleId,status ==>" + sampleId + "," + status);
				if (status == 0) {
					play(soundPool, sampleId);
				}
			}
		});
	}
	
	public void play(int sound_id) {
		
		mSoundPool.autoPause();
		
		//-----------------------------------------------------------
		// 해당하는 음원파일을 로드한다.
		//-----------------------------------------------------------
		if (sound_id == SOUND_DUPLICATION) {
			mSoundPool.load(mContext, R.raw.duplication, 0);
		} else if (sound_id == SOUND_NOTEXISTS) {
			mSoundPool.load(mContext, R.raw.notexists, 0);
		} else if (sound_id == SOUND_ERROR) {
			mSoundPool.load(mContext, R.raw.alert, 0);
		} else if (sound_id == SOUND_ASTERISK) {
			mSoundPool.load(mContext, R.raw.asterisk, 0);
		} else if (sound_id == SOUND_NOTIFY) {
			mSoundPool.load(mContext, R.raw.notify, 0);
		} else if (sound_id == SOUND_JUNG) {
			mSoundPool.load(mContext, R.raw.jung, 0);
		} else if (sound_id == SOUND_SCAN) {
			mSoundPool.load(mContext, R.raw.scan, 0);
		} else if (sound_id == SOUND_SCANBELOW) {
			mSoundPool.load(mContext, R.raw.scanbelow, 0);
		} else if (sound_id == SOUND_SENDQUESTION) {
			mSoundPool.load(mContext, R.raw.sendquestion, 0);
		} else if (sound_id == SOUND_NOTLOCATION) {
			mSoundPool.load(mContext, R.raw.notlocation, 0);
		} else if (sound_id == SOUND_JINWOO_SLOW) {
			mSoundPool.load(mContext, R.raw.jinwoo_slow, 0);
		}
	}
	
	private void play(SoundPool soundPool, int sound) {

		// Getting the user sound settings
		float actualVolume = (float) mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		//float maxVolume = (float) mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		//if (actualVolume<1) actualVolume = 1;
		//float volume = (maxVolume - actualVolume) / 2;
		//volume = actualVolume;

		soundPool.play(sound, actualVolume, actualVolume, 1, 0, 1f);
	}
	
	public void bacodePlay(String barcode) {

		barcode = barcode.toUpperCase();
		
		int i=0;
		while (i<barcode.length()) {
        	char chr = barcode.charAt(i);
        	
        	if (i==1 && chr=='9') mSoundPool.load(mContext, R.raw.n99, 0);
        	else if (chr=='0') mSoundPool.load(mContext, R.raw.n0, 0);
        	else if (chr=='1') mSoundPool.load(mContext, R.raw.n1, 0);
        	else if (chr=='2') mSoundPool.load(mContext, R.raw.n2, 0);
        	else if (chr=='3') mSoundPool.load(mContext, R.raw.n3, 0);
        	else if (chr=='4') mSoundPool.load(mContext, R.raw.n4, 0);
        	else if (chr=='5') mSoundPool.load(mContext, R.raw.n5, 0);
        	else if (chr=='6') mSoundPool.load(mContext, R.raw.n6, 0);
        	else if (chr=='7') mSoundPool.load(mContext, R.raw.n7, 0);
        	else if (chr=='8') mSoundPool.load(mContext, R.raw.n8, 0);
        	else if (chr=='9') mSoundPool.load(mContext, R.raw.n9, 0);
        	else if (chr=='A') mSoundPool.load(mContext, R.raw.a, 0);
        	else if (chr=='B') mSoundPool.load(mContext, R.raw.b, 0);
        	else if (chr=='C') mSoundPool.load(mContext, R.raw.c, 0);
        	else if (chr=='D') mSoundPool.load(mContext, R.raw.d, 0);
        	else if (chr=='E') mSoundPool.load(mContext, R.raw.e, 0);
        	else if (chr=='F') mSoundPool.load(mContext, R.raw.f, 0);
        	else if (chr=='G') mSoundPool.load(mContext, R.raw.g, 0);
        	else if (chr=='H') mSoundPool.load(mContext, R.raw.h, 0);
        	else if (chr=='I') mSoundPool.load(mContext, R.raw.i, 0);
        	else if (chr=='J') mSoundPool.load(mContext, R.raw.j, 0);
        	else if (chr=='K') mSoundPool.load(mContext, R.raw.k, 0);
        	else if (chr=='L') mSoundPool.load(mContext, R.raw.l, 0);
        	else if (chr=='M') mSoundPool.load(mContext, R.raw.m, 0);
        	else if (chr=='N') mSoundPool.load(mContext, R.raw.n, 0);
        	else if (chr=='O') mSoundPool.load(mContext, R.raw.o, 0);
        	else if (chr=='P') mSoundPool.load(mContext, R.raw.p, 0);
        	else if (chr=='Q') mSoundPool.load(mContext, R.raw.q, 0);
        	else if (chr=='R') mSoundPool.load(mContext, R.raw.r, 0);
        	else if (chr=='S') mSoundPool.load(mContext, R.raw.s, 0);
        	else if (chr=='T') mSoundPool.load(mContext, R.raw.t, 0);
        	else if (chr=='U') mSoundPool.load(mContext, R.raw.u, 0);
        	else if (chr=='V') mSoundPool.load(mContext, R.raw.v, 0);
        	else if (chr=='W') mSoundPool.load(mContext, R.raw.w, 0);
        	else if (chr=='X') mSoundPool.load(mContext, R.raw.x, 0);
        	else if (chr=='Y') mSoundPool.load(mContext, R.raw.y, 0);
        	else if (chr=='Z') mSoundPool.load(mContext, R.raw.z, 0);
        	else {
        		i++;
        		continue;
        	}
        	
        	try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        	i++;
        }
	}
}
