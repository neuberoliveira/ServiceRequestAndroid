package com.neuberdesigns.ServiceRequest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by neuber on 10/07/15.
 */
public class FileUploadHolder {
    protected String filename;
    protected Bitmap bitmap;

    public static FileUploadHolder create(String file) {
        return create(file, null);
    }

    public static FileUploadHolder create(String file, Bitmap bm) {
        FileUploadHolder uploader = new FileUploadHolder();

        uploader.setFilename(file);

        if (bm != null)
            uploader.setBitmap(bm);

        return uploader;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(String file) {
        this.bitmap = BitmapFactory.decodeFile(file);
    }

    public Bitmap getBitmapFromFile() {
        Bitmap bm = BitmapFactory.decodeFile(getFilename());
        //setBitmap( bm );

        return bm;
    }

    public void flush() {
        Bitmap bm = getBitmap();

        if (bm != null) {
            bm.recycle();
            this.bitmap = null;
        }
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
