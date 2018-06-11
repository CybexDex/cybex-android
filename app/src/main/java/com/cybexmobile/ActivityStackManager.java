package com.cybexmobile;

import android.app.Activity;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Stack;

public class ActivityStackManager {

    private static final String TAG = "com.cybexmobile.ActivityStackManager";

    private static ActivityStackManager mInstance = null;

    private Stack<WeakReference<Activity>> mActivityStack = null;

    private ActivityStackManager(){
        mActivityStack = new Stack<>();
    }

    public static ActivityStackManager getInstance(){
        if(mInstance == null){
            mInstance = new ActivityStackManager();
        }
        return mInstance;
    }

    public Activity getTopActivity(){
        return mActivityStack.lastElement().get();
    }

    public Stack<WeakReference<Activity>> getActivityStack(){
        return mActivityStack;
    }

    public void addActivity(Activity activity){
        if(mActivityStack == null){
            mActivityStack = new Stack<>();
        }
        mActivityStack.add(new WeakReference<>(activity));
    }

    public void removeActivity(Activity activity){
        Iterator<WeakReference<Activity>> it = mActivityStack.iterator();
        while(it.hasNext()){
            WeakReference<Activity> activityReference = it.next();
            if (activityReference.get() == activity) it.remove();
        }
    }

}
