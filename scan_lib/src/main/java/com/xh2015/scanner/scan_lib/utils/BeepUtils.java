package com.xh2015.scanner.scan_lib.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import com.xh2015.scanner.scan_lib.R;

import java.io.IOException;

/**
 * Author：gary
 * Email: xuhaozv@163.com
 * description:提示音工具
 * Date: 2018/4/11 上午11:19
 */
public class BeepUtils {
    private static final float BEEP_VOLUME = 0.50f;
    private static final int VIBRATE_DURATION = 200;
    private static boolean playBeep = false;
    private static MediaPlayer mediaPlayer;

    /**
     * 自定义声音
     *
     * @param mContext
     * @param vibrate
     */
    public static void playBeep(Activity mContext, boolean voice, boolean vibrate) {
        if (voice) {
            playBeep = true;
            AudioManager audioService = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            //ringerMode为手机的系统声音设置的状态值，0位静音，1为震动，2为响铃
            if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
                playBeep = false;
            }
            if (playBeep && mediaPlayer != null) {
                mediaPlayer.start();
            } else {
                mContext.setVolumeControlStream(AudioManager.STREAM_MUSIC);
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        mediaPlayer.seekTo(0);
                    }
                });

                AssetFileDescriptor file = mContext.getResources().openRawResourceFd(R.raw.beep);
                try {
                    mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                    file.close();
                    mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    mediaPlayer = null;
                }
            }
        }
        if (vibrate) {
            VibrateUtils.vibrateOnce(mContext, VIBRATE_DURATION);
        }
    }

    /**
     * 系统通知声音
     *
     * @param mContext
     * @param vibrate
     */
    public static void playSystemBeep(Activity mContext, boolean voice, boolean vibrate) {
        if (voice) {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone rt = RingtoneManager.getRingtone(mContext.getApplicationContext(), uri);
            rt.play();
        }

        if (vibrate) {
            VibrateUtils.vibrateOnce(mContext, VIBRATE_DURATION);
        }
    }
}
