package com.neuberdesigns.ServiceRequest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.net.URL;

/**
 * @author Neuber Oliveira
 */
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    protected String property;
    protected ImageView imageView;
    protected ProgressBar progress;
    protected BaseViewHolder holder;
    protected Activity activity;
    protected int width = -1;
    protected int height = -1;
    protected int resize = -1;

    public DownloadImageTask(ImageView iv) {
        imageView = iv;
    }

    public DownloadImageTask(ImageView iv, ProgressBar p, Activity ac) {
        imageView = iv;
        progress = p;
        activity = ac;
    }

    public DownloadImageTask(BaseViewHolder h, String prop) {
        holder = h;
        property = prop;
        imageView = (ImageView) h.get(property);
    }

    public DownloadImageTask(BaseViewHolder h, String prop, ProgressBar p, Activity ac) {
        holder = h;
        property = prop;
        imageView = (ImageView) h.get(property);
        progress = p;
        activity = ac;
    }

    public void setResize(int w, int h) {
        width = w;
        height = h;
    }

    public void setScale(int s) {
        if (s > 0)
            resize = s;
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        toggleLoader(true);
        if (imageView != null) {
            //Log.d(JSONAdapter.LOG_ID, "VIEW: " + urls[0]);
            return downloadImage(urls[0]);
        } else {
            Bitmap bm = downloadImage(holder.url);
            holder.bitmap = bm;
            return bm;
        }
    }

    @Override
    protected void onPostExecute(Bitmap im) {
        if (imageView != null) {
            imageView.setImageBitmap(null);
            imageView.setImageBitmap(im);
        } else {
            imageView.setImageBitmap(null);
            imageView.setImageBitmap(im);
        }
        toggleLoader(false);
    }

    protected Bitmap downloadImage(String url) {
        Bitmap image = null;
        try {
            //Log.d(JSONAdapter.LOG_ID, "URL: "+url);
            URL imageUrl = new URL(url);


            if (resize == -1) {
                image = BitmapFactory.decodeStream(imageUrl.openStream());
                Log.d("scale", "NOT SCALED");
            } else {
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                int width_tmp = o.outWidth, height_tmp = o.outHeight;
                int scale = 1;
                while (true) {
                    if (width_tmp / 2 < resize || height_tmp / 2 < resize)
                        break;
                    width_tmp /= 2;
                    height_tmp /= 2;
                    scale *= 2;
                }
                Log.d("scale", "SCALED TO: " + scale);
                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = scale;
                image = BitmapFactory.decodeStream(imageUrl.openStream(), null, o2);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(JSONAdapter.LOG_ID, "Download failed: " + e.getMessage());
        }

        return image;
    }

    protected void toggleLoader(final boolean show) {
        if (activity != null && progress != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        }
    }
}
