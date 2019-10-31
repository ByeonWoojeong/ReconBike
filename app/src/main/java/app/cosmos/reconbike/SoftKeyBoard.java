package app.cosmos.reconbike;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SoftKeyBoard implements View.OnFocusChangeListener {
    private static final int CLEAR_FOCUS = 0;
    private ViewGroup layout;
    private int layoutBottom;
    private InputMethodManager im;
    private int[] coords;
    private boolean isKeyboardShow;
    private SoftKeyboardChangesThread softKeyboardThread;
    private List<EditText> editTextList;
    private View tempView;
    public SoftKeyBoard(ViewGroup layout, InputMethodManager im) {
        this.layout = layout;
        keyboardHideByDefault();
        initEditTexts(layout);
        this.im = im;
        this.coords = new int[2];
        this.isKeyboardShow = false;
        this.softKeyboardThread = new SoftKeyboardChangesThread();
        this.softKeyboardThread.start();
    }
    public void openSoftKeyboard() {
        if (!isKeyboardShow) {
            layoutBottom = getLayoutCoordinates();
            im.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);
            softKeyboardThread.keyboardOpened();
            isKeyboardShow = true;
        }
    }
    public void closeSoftKeyboard() {
        if (isKeyboardShow) {
            im.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            isKeyboardShow = false;
        }
    }
    public void setSoftKeyboardCallback(SoftKeyboardChanged mCallback) {
        softKeyboardThread.setCallback(mCallback);
    }
    public void unRegisterSoftKeyboardCallback() {
        softKeyboardThread.stopThread();
    }
    public interface SoftKeyboardChanged {
        public void onSoftKeyboardHide();
        public void onSoftKeyboardShow();
    }
    private int getLayoutCoordinates() {
        layout.getLocationOnScreen(coords);
        return coords[1] + layout.getHeight();
    }
    private void keyboardHideByDefault() {
        layout.setFocusable(true);
        layout.setFocusableInTouchMode(true);
    }

    private void initEditTexts(ViewGroup viewgroup) {
        if (editTextList == null) {
            editTextList = new ArrayList<EditText>();
        }
        int childCount = viewgroup.getChildCount();
        for (int i=0; i<= childCount-1;i++) {
            View v = viewgroup.getChildAt(i);
            if (v instanceof ViewGroup) {
                initEditTexts((ViewGroup) v);
            }
            if (v instanceof EditText) {
                EditText editText = (EditText) v;
                editText.setOnFocusChangeListener(this);
                editText.setCursorVisible(true);
                editTextList.add(editText);
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            tempView = v;
            if (!isKeyboardShow) {
                layoutBottom = getLayoutCoordinates();
                softKeyboardThread.keyboardOpened();
                isKeyboardShow = true;
            }
        }
    }
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message m) {
            switch (m.what) {
                case CLEAR_FOCUS:
                    if (tempView != null) {
                        tempView.clearFocus();
                        tempView = null;
                    }
                    break;
            }
        }
    };
    private class SoftKeyboardChangesThread extends Thread {
        private AtomicBoolean started;
        private SoftKeyboardChanged mCallback;
        public SoftKeyboardChangesThread() {
            started = new AtomicBoolean(true);
        }
        public void setCallback(SoftKeyboardChanged mCallback) {
            this.mCallback = mCallback;
        }

        @Override
        public void run() {
            while (started.get()) {
                synchronized(this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                int currentBottomLocation = getLayoutCoordinates();
                while (currentBottomLocation == layoutBottom && started.get()) {
                    currentBottomLocation = getLayoutCoordinates();
                }
                if (started.get()) {
                    mCallback.onSoftKeyboardShow();
                }
                while (currentBottomLocation >= layoutBottom && started.get()) {
                    currentBottomLocation = getLayoutCoordinates();
                }
                while (currentBottomLocation != layoutBottom && started.get()) {
                    synchronized(this) {
                        try {
                            wait(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    currentBottomLocation = getLayoutCoordinates();
                }
                if (started.get()) {
                    mCallback.onSoftKeyboardHide();
                }
                if (isKeyboardShow && started.get()) {

                }
                isKeyboardShow = false;
                if (started.get()) {
                    mHandler.obtainMessage(CLEAR_FOCUS).sendToTarget();
                }
            }
        }
        public void keyboardOpened() {
            synchronized(this) {
                notify();
            }
        }
        public void stopThread() {
            synchronized(this) {
                started.set(false);
                notify();
            }
        }
    }
}