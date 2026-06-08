package com.facebook.drawee.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import com.facebook.common.activitylistener.ActivityListener;
import com.facebook.common.activitylistener.BaseActivityListener;
import com.facebook.common.internal.Objects;
import com.facebook.common.internal.Preconditions;
import com.facebook.common.logging.FLog;
import com.facebook.drawee.components.DraweeEventTracker;
import com.facebook.drawee.drawable.VisibilityAwareDrawable;
import com.facebook.drawee.drawable.VisibilityCallback;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.interfaces.DraweeHierarchy;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes.dex */
public class DraweeHolder<DH extends DraweeHierarchy> implements VisibilityCallback {
    private final ActivityListener mActivityListener;
    private DH mHierarchy;
    private boolean mIsControllerAttached = false;
    private boolean mIsHolderAttached = false;
    private boolean mIsVisible = true;
    private boolean mIsActivityStarted = true;
    private DraweeController mController = null;
    private final DraweeEventTracker mEventTracker = new DraweeEventTracker();

    public void registerWithContext(Context context) {
    }

    public static <DH extends DraweeHierarchy> DraweeHolder<DH> create(@Nullable DH dh, Context context) {
        DraweeHolder<DH> draweeHolder = new DraweeHolder<>(dh);
        draweeHolder.registerWithContext(context);
        return draweeHolder;
    }

    public DraweeHolder(@Nullable DH dh) {
        if (dh != null) {
            setHierarchy(dh);
        }
        this.mActivityListener = new BaseActivityListener() { // from class: com.facebook.drawee.view.DraweeHolder.1
            @Override // com.facebook.common.activitylistener.BaseActivityListener, com.facebook.common.activitylistener.ActivityListener
            public void onStart(Activity activity) {
                DraweeHolder.this.setActivityStarted(true);
            }

            @Override // com.facebook.common.activitylistener.BaseActivityListener, com.facebook.common.activitylistener.ActivityListener
            public void onStop(Activity activity) {
                DraweeHolder.this.setActivityStarted(false);
            }
        };
    }

    public void onAttach() {
        this.mEventTracker.recordEvent(DraweeEventTracker.Event.ON_HOLDER_ATTACH);
        this.mIsHolderAttached = true;
        attachOrDetachController();
    }

    public void onDetach() {
        this.mEventTracker.recordEvent(DraweeEventTracker.Event.ON_HOLDER_DETACH);
        this.mIsHolderAttached = false;
        attachOrDetachController();
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        DraweeController draweeController = this.mController;
        if (draweeController == null) {
            return false;
        }
        return draweeController.onTouchEvent(motionEvent);
    }

    @Override // com.facebook.drawee.drawable.VisibilityCallback
    public void onVisibilityChange(boolean z) {
        if (this.mIsVisible == z) {
            return;
        }
        this.mEventTracker.recordEvent(z ? DraweeEventTracker.Event.ON_DRAWABLE_SHOW : DraweeEventTracker.Event.ON_DRAWABLE_HIDE);
        this.mIsVisible = z;
        attachOrDetachController();
    }

    @Override // com.facebook.drawee.drawable.VisibilityCallback
    public void onDraw() {
        if (this.mIsControllerAttached) {
            return;
        }
        FLog.wtf((Class<?>) DraweeEventTracker.class, "%x: Draw requested for a non-attached controller %x. %s", Integer.valueOf(System.identityHashCode(this)), Integer.valueOf(System.identityHashCode(this.mController)), toString());
        this.mIsHolderAttached = true;
        this.mIsVisible = true;
        this.mIsActivityStarted = true;
        attachOrDetachController();
    }

    private void setVisibilityCallback(@Nullable VisibilityCallback visibilityCallback) {
        Object topLevelDrawable = getTopLevelDrawable();
        if (topLevelDrawable instanceof VisibilityAwareDrawable) {
            ((VisibilityAwareDrawable) topLevelDrawable).setVisibilityCallback(visibilityCallback);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setActivityStarted(boolean z) {
        this.mEventTracker.recordEvent(z ? DraweeEventTracker.Event.ON_ACTIVITY_START : DraweeEventTracker.Event.ON_ACTIVITY_STOP);
        this.mIsActivityStarted = z;
        attachOrDetachController();
    }

    public void setController(@Nullable DraweeController draweeController) {
        boolean z = this.mIsControllerAttached;
        if (z) {
            detachController();
        }
        if (this.mController != null) {
            this.mEventTracker.recordEvent(DraweeEventTracker.Event.ON_CLEAR_OLD_CONTROLLER);
            this.mController.setHierarchy(null);
        }
        this.mController = draweeController;
        if (draweeController != null) {
            this.mEventTracker.recordEvent(DraweeEventTracker.Event.ON_SET_CONTROLLER);
            this.mController.setHierarchy(this.mHierarchy);
        } else {
            this.mEventTracker.recordEvent(DraweeEventTracker.Event.ON_CLEAR_CONTROLLER);
        }
        if (z) {
            attachController();
        }
    }

    @Nullable
    public DraweeController getController() {
        return this.mController;
    }

    public void setHierarchy(DH dh) {
        this.mEventTracker.recordEvent(DraweeEventTracker.Event.ON_SET_HIERARCHY);
        setVisibilityCallback(null);
        DH dh2 = (DH) Preconditions.checkNotNull(dh);
        this.mHierarchy = dh2;
        onVisibilityChange(dh2.getTopLevelDrawable().isVisible());
        setVisibilityCallback(this);
        DraweeController draweeController = this.mController;
        if (draweeController != null) {
            draweeController.setHierarchy(dh);
        }
    }

    public DH getHierarchy() {
        return (DH) Preconditions.checkNotNull(this.mHierarchy);
    }

    public boolean hasHierarchy() {
        return this.mHierarchy != null;
    }

    public Drawable getTopLevelDrawable() {
        DH dh = this.mHierarchy;
        if (dh == null) {
            return null;
        }
        return dh.getTopLevelDrawable();
    }

    private void attachController() {
        if (this.mIsControllerAttached) {
            return;
        }
        this.mEventTracker.recordEvent(DraweeEventTracker.Event.ON_ATTACH_CONTROLLER);
        this.mIsControllerAttached = true;
        DraweeController draweeController = this.mController;
        if (draweeController == null || draweeController.getHierarchy() == null) {
            return;
        }
        this.mController.onAttach();
    }

    private void detachController() {
        if (this.mIsControllerAttached) {
            this.mEventTracker.recordEvent(DraweeEventTracker.Event.ON_DETACH_CONTROLLER);
            this.mIsControllerAttached = false;
            DraweeController draweeController = this.mController;
            if (draweeController != null) {
                draweeController.onDetach();
            }
        }
    }

    private void attachOrDetachController() {
        if (this.mIsHolderAttached && this.mIsVisible && this.mIsActivityStarted) {
            attachController();
        } else {
            detachController();
        }
    }

    public String toString() {
        return Objects.toStringHelper(this).add("controllerAttached", this.mIsControllerAttached).add("holderAttached", this.mIsHolderAttached).add("drawableVisible", this.mIsVisible).add("activityStarted", this.mIsActivityStarted).add("events", this.mEventTracker.toString()).toString();
    }
}
