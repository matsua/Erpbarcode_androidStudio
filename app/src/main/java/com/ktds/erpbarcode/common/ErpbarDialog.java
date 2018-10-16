package com.ktds.erpbarcode.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;

public class ErpbarDialog {
	
	private Activity mActivity;
	private BarcodeSoundPlay mBarcodeSoundPlay;
	//private ErpBarEventListener mErpBarEventListener;
	
	public ErpbarDialog(Activity activity) {
		mActivity = activity;
		mBarcodeSoundPlay = new BarcodeSoundPlay(activity.getApplicationContext());
	}

	public void showMessageDialog(final String message) {
		
		if (message.isEmpty()) return;
		showMessageDialog(new ErpBarcodeException(-1, message));
	}
	
	public void showMessageDialog(ErpBarcodeException erpbarException) {

		if (GlobalData.getInstance().isGlobalAlertDialog()) return;
		GlobalData.getInstance().setGlobalAlertDialog(true);
		
		
		final String message = erpbarException.getErrMessage();
		final int sound = erpbarException.getSound();
		//final int messageClass = erpbarException.getMessageClass();
		String title = "안내";
		//if (messageClass == ErpBarcodeException.MESSAGE_CLASS_ERROR) {
		//	title = "에러";
		//}
		
		if (sound == BarcodeSoundPlay.SOUND_NOT_PLAY) {
			if (message.contains("중복 스캔")) {
				mBarcodeSoundPlay.play(BarcodeSoundPlay.SOUND_DUPLICATION);
			} else if (message.contains("전송하시겠습니까")) {
				mBarcodeSoundPlay.play(BarcodeSoundPlay.SOUND_SENDQUESTION);
			} else if (message.contains("스캔하지 않은 하위")) {
				mBarcodeSoundPlay.play(BarcodeSoundPlay.SOUND_SCANBELOW);
			} else if (message.contains("존재하지 않는")) {
				mBarcodeSoundPlay.play(BarcodeSoundPlay.SOUND_NOTEXISTS);
			} else if (message.contains("유효하지 않은") || message.contains("없습니다") || message.contains("처리할 수 없는") || message.contains("기존에 전송한") || message.contains("기존에 전송한") || message.contains("선택된 항목이")) {
				mBarcodeSoundPlay.play(BarcodeSoundPlay.SOUND_ERROR);
			} else {
				mBarcodeSoundPlay.play(BarcodeSoundPlay.SOUND_NOTIFY);
			}
		} else {
			mBarcodeSoundPlay.play(sound);
		}
		
		
		//---------------------------------------------------------------------
		// 메시지 Close Dialog
    	//---------------------------------------------------------------------
		final Builder builder = new AlertDialog.Builder(mActivity); 
		builder.setIcon(android.R.drawable.ic_menu_info_details);
		builder.setTitle(title);
        TextView msgText = new TextView(mActivity.getApplicationContext());
		msgText.setPadding(10, 30, 10, 30);
		msgText.setText(message);
		msgText.setGravity(Gravity.CENTER);
		msgText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
		msgText.setTextColor(Color.BLACK);
		builder.setView(msgText);
		builder.setCancelable(false);
		builder.setNeutralButton("닫기", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	GlobalData.getInstance().setGlobalAlertDialog(false);
            }
        });
		AlertDialog dialog = builder.create();
		dialog.show();
		return;
	}
}
