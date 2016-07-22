package com.neuberdesigns.ServiceRequest;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.lang.reflect.Field;

/**
 * Created by neuber on 28/03/15.
 */
public abstract class BaseViewHolder {
    public static String LOG_ID = "view_holder";
    public int position;
    public String url;
    public Bitmap bitmap;
    public ImageView image;
    public ProgressBar progress;

    public Object get(String property) {
        Object value = null;
        Field field = getField(property);

        if (field != null) {
            try {
                value = field.get(this);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    public void set(String property, Object value) {
        Field field = getField(property);

        if (field != null) {
            try {
                field.set(this, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    protected Field getField(String property) {
        Object o = this;
        Class<?> c = o.getClass();

        Field f = null;
        try {
            f = c.getDeclaredField(property);
            f.setAccessible(true);
        } catch (NoSuchFieldException e) {
            //e.printStackTrace();
            Log.w(LOG_ID, "Property: '" + property + "' not found");
        }

        return f;
    }
}
