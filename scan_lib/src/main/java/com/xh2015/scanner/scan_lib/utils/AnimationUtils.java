package com.xh2015.scanner.scan_lib.utils;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;

/**
 * Author：gary
 * Email: xuhaozv@163.com
 * description:动画工具
 * Date: 2018/4/11 上午11:47
 */
public class AnimationUtils {
    public static void ScaleUpDown(View view) {
        ScaleAnimation animation = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.RESTART);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(1200);
        view.startAnimation(animation);
    }
}
