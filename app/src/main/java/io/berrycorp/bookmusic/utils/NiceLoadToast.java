package io.berrycorp.bookmusic.utils;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

/**
 * Created by Wannes2 on 23/04/2015.
 */
public class NiceLoadToast {
    private String mText = "";
    private NiceLoadToastView mView;
    private ViewGroup mParentView;
    private int mTranslationY = 0;
    private boolean mShowCalled = false;
    private boolean mToastCanceled = false;
    private boolean mInflated = false;
    private boolean mVisible = false;
    private boolean mReAttached = false;

    public NiceLoadToast(Context context){
        mView = new NiceLoadToastView(context);
        mParentView = (ViewGroup) ((Activity) context).getWindow().getDecorView();
    }

    private void cleanup() {
        int childCount = mParentView.getChildCount();
        for(int i = childCount; i >= 0; i--){
            if(mParentView.getChildAt(i) instanceof NiceLoadToastView){
                NiceLoadToastView ltv = (NiceLoadToastView) mParentView.getChildAt(i);
                ltv.cleanup();
                mParentView.removeViewAt(i);
            }
        }

        mInflated = false;
        mToastCanceled = false;
    }

    public NiceLoadToast setTranslationY(int pixels){
        mTranslationY = pixels;
        return this;
    }

    public NiceLoadToast setText(String message){
        mText = message;
        mView.setText(mText);
        return this;
    }

    public NiceLoadToast setTextColor(int color){
        mView.setTextColor(color);
        return this;
    }

    public NiceLoadToast setBackgroundColor(int color){
        mView.setBackgroundColor(color);
        return this;
    }

    public NiceLoadToast setProgressColor(int color){
        mView.setProgressColor(color);
        return this;
    }

    public NiceLoadToast setTextDirection(boolean isLeftToRight){
        mView.setTextDirection(isLeftToRight);
        return this;
    }

    public NiceLoadToast setBorderColor(int color){
        mView.setBorderColor(color);
        return this;
    }

    public NiceLoadToast setBorderWidthPx(int width){
        mView.setBorderWidthPx(width);
        return this;
    }

    public NiceLoadToast setBorderWidthRes(int resourceId){
        mView.setBorderWidthRes(resourceId);
        return this;
    }

    public NiceLoadToast setBorderWidthDp(int width){
        mView.setBorderWidthDp(width);
        return this;
    }

    public NiceLoadToast show(){
        mShowCalled = true;
        attach();
        return this;
    }

    private void showInternal(){
        mView.show();
        ViewHelper.setTranslationX(mView, (mParentView.getWidth() - mView.getWidth()) / 2);
        ViewHelper.setAlpha(mView, 0f);
        ViewHelper.setTranslationY(mView, -mView.getHeight() + mTranslationY);
        //mView.setVisibility(View.VISIBLE);
        ViewPropertyAnimator.animate(mView).alpha(1f).translationY(25 + mTranslationY)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(null)
                .setDuration(300).setStartDelay(0).start();

        mVisible = true;
    }

    private void attach() {
        cleanup();

        mReAttached = true;

        mParentView.addView(mView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ViewHelper.setAlpha(mView, 0);
        mParentView.postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewHelper.setTranslationX(mView, (mParentView.getWidth() - mView.getWidth()) / 2);
                ViewHelper.setTranslationY(mView, -mView.getHeight() + mTranslationY);
                mInflated = true;
                if(!mToastCanceled && mShowCalled) showInternal();
            }
        },1);
    }

    public void success(){
        if(!mInflated){
            mToastCanceled = true;
            return;
        }
        if(mReAttached){
            mView.success();
            slideUp();
        }
    }

    public void error(){
        if(!mInflated){
            mToastCanceled = true;
            return;
        }
        if(mReAttached){
            mView.error();
            slideUp();
        }
    }

    public void hide(){
        if(!mInflated){
            mToastCanceled = true;
            return;
        }
        if(mReAttached){
            slideUp(0);
        }
    }

    private void slideUp(){
        slideUp(1000);
    }

    private void slideUp(int startDelay){
        mReAttached = false;

        ViewPropertyAnimator.animate(mView).setStartDelay(startDelay).alpha(0f)
                .translationY(-mView.getHeight() + mTranslationY)
                .setInterpolator(new AccelerateInterpolator())
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if(!mReAttached){
                            cleanup();
                        }
                    }
                })
                .start();

        mVisible = false;
    }
}
