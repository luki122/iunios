/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.calculator2;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.provider.Settings;

import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

class EventListener implements View.OnKeyListener, View.OnClickListener,
		View.OnLongClickListener {
	// Aurora liugj 2014-02-26 modified for aurora's new feature start
	private static final String RAW_PATH = "system/media/audio/ui/Calculator.ogg";
	Logic mHandler;
	private Context context;
	ViewPager mPager;
	// Aurora liugj 2014-01-16 added for aurora's new feature start
	private SoundPool sp;
	private int music;// 定义一个整型load（）用来设置suondID
	private AudioManager mAudioManager;

	public EventListener(Context context) {
		this.context = context;
		initBtnSound(context);
	}

	private void initBtnSound(Context context) {
		mAudioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		mAudioManager.unloadSoundEffects(); // 屏蔽系统按键提示音
		sp = new SoundPool(5, AudioManager.STREAM_SYSTEM, 5);
		try {
			music = sp.load(RAW_PATH, 1);
		} catch (Exception e) {
			// music = sp.load(context, R.raw.btn_music, 1);
			e.printStackTrace();
		}
	}

	// Aurora liugj 2014-01-16 added for aurora's new feature end
	// Aurora liugj 2014-02-26 modified for aurora's new feature end

	void setHandler(Logic handler, ViewPager pager) {
		mHandler = handler;
		mPager = pager;
	}

	public void releaseSound() {
		mAudioManager.loadSoundEffects();
	}

	@Override
	public void onClick(View view) {
		// Aurora liugj 2014-01-16 added for aurora's new feature start
		// Aurora liugj 2014-01-16 added for aurora's new feature start
		try {
              //相应设置里的按键提示音设置by xiexiujie 5.26 am
			int soundEffects = Settings.System.getInt(
					context.getContentResolver(),
					Settings.System.SOUND_EFFECTS_ENABLED);
			if (soundEffects == 1) {
				sp.play(music, 1, 1, 0, 0, 1);

			}

		} catch (Exception e) {
			// TODO: handle exception
		}

		// Aurora liugj 2014-01-16 added for aurora's new feature end
		int id = view.getId();
		switch (id) {
		case R.id.del:
			mHandler.onDelete();
			break;

		case R.id.clear:
			mHandler.onClear();
			break;

		case R.id.equal:
			mHandler.onEnter();
			break;

		default:
			if (view instanceof Button) {
				String text = ((Button) view).getText().toString();
				if (text.length() >= 2) {
					// add paren after sin, cos, ln, etc. from buttons
					text += '(';
				}
				mHandler.insert(text);
				if (mPager != null
						&& mPager.getCurrentItem() == Calculator.ADVANCED_PANEL) {
					mPager.setCurrentItem(Calculator.BASIC_PANEL);
				}
			}
		}
	}

	@Override
	public boolean onLongClick(View view) {
		int id = view.getId();
		if (id == R.id.del) {
			mHandler.onClear();
			return true;
		}
		return false;
	}

	@Override
	public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
		int action = keyEvent.getAction();

		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
				|| keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			boolean eat = mHandler
					.eatHorizontalMove(keyCode == KeyEvent.KEYCODE_DPAD_LEFT);
			return eat;
		}

		// Work-around for spurious key event from IME, bug #1639445
		if (action == KeyEvent.ACTION_MULTIPLE
				&& keyCode == KeyEvent.KEYCODE_UNKNOWN) {
			return true; // eat it
		}

		// Calculator.log("KEY " + keyCode + "; " + action);

		if (keyEvent.getUnicodeChar() == '=') {
			if (action == KeyEvent.ACTION_UP) {
				mHandler.onEnter();
			}
			return true;
		}

		if (keyCode != KeyEvent.KEYCODE_DPAD_CENTER
				&& keyCode != KeyEvent.KEYCODE_DPAD_UP
				&& keyCode != KeyEvent.KEYCODE_DPAD_DOWN
				&& keyCode != KeyEvent.KEYCODE_ENTER) {
			if (keyEvent.isPrintingKey() && action == KeyEvent.ACTION_UP) {
				// Tell the handler that text was updated.
				mHandler.onTextChanged();
			}
			return false;
		}

		/*
		 * We should act on KeyEvent.ACTION_DOWN, but strangely sometimes the
		 * DOWN event isn't received, only the UP. So the workaround is to act
		 * on UP... http://b/issue?id=1022478
		 */

		if (action == KeyEvent.ACTION_UP) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_ENTER:
			case KeyEvent.KEYCODE_DPAD_CENTER:
				mHandler.onEnter();
				break;

			case KeyEvent.KEYCODE_DPAD_UP:
				mHandler.onUp();
				break;

			case KeyEvent.KEYCODE_DPAD_DOWN:
				mHandler.onDown();
				break;
			}
		}
		return true;
	}
}
