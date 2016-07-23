package br.com.neuberoliveira.serviceRequest;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;

/**
 * Created by dopamina on 07/07/15.
 */
public class ImageHelper {
    public static int NULL_PCT = -999;

    public static String getNameFromUri(Context ctx, Uri uri) {
        String path;

        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        // Get the cursor
        Cursor cursor = ctx.getContentResolver().query(uri, filePathColumn, null, null, null);
        // Move to first row
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        path = cursor.getString(columnIndex);
        cursor.close();

        return path;
    }

    public static int getOrientation(String filename) throws IOException {
        int orientation;
        ExifInterface exif = new ExifInterface(filename);
        orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        return orientation;
    }

    public static Bitmap scale(Bitmap source, float w, float h) throws IOException {
        return scale(source, w, h, false);
    }

    public static Bitmap scale(Bitmap source, float w, float h, boolean usePixel) throws IOException {
        //original height / original width x new width = new height
        Bitmap scaled = null;
        int finalW, finalH;
        float bmW, bmH, oriW, oriH;

        if (source != null) {
            oriW = (float) source.getWidth();
            oriH = (float) source.getHeight();

            if (oriW > oriH) {
                Log.d("scale", "W > H");
                bmW = w;
                bmH = oriH / oriW * w;

            } else if (oriH > oriW) {
                Log.d("scale", "H > W");
                bmW = oriW / oriH * h;
                bmH = h;

            } else {
                Log.d("scale", "ELSE");
                bmW = w;
                bmH = h;
            }
            finalW = (int) bmW;
            finalH = (int) bmH;


            if (usePixel)
                scaled = Bitmap.createBitmap(source, 0, 0, finalW, finalH);
            else
                scaled = Bitmap.createScaledBitmap(source, finalW, finalH, false);
        }

        return scaled;
    }

    public static Bitmap fixAngle(String filename) throws IOException {
        return fixAngle(filename, NULL_PCT);
    }

    public static Bitmap fixAngle(String filename, int pct) throws IOException {
        Bitmap sourceBitmap;
        sourceBitmap = BitmapFactory.decodeFile(filename);

        return fixAngleFromBitmap(sourceBitmap, filename, pct);
    }

    public static Bitmap fixAngle(Bitmap sourceBitmap, String filename) throws IOException {
        return fixAngleFromBitmap(sourceBitmap, filename, NULL_PCT);
    }

    public static Bitmap fixAngle(Bitmap sourceBitmap, String filename, int pct) throws IOException {
        return fixAngleFromBitmap(sourceBitmap, filename, pct);
    }

    protected static Bitmap fixAngleFromBitmap(Bitmap sourceBitmap, String filename, int pct) throws IOException {
        Bitmap rotatedBitmap, scaledBitmap;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        //Orientation angles: 3:180, 6:90, 8:270

        String imageType = options.outMimeType;
        int bmW = sourceBitmap.getWidth();
        int bmH = sourceBitmap.getHeight();
        int angle = 0;
        int orientation = getOrientation(filename);
        Matrix matrix = new Matrix();

        if (orientation == 6) {
            matrix.postRotate(90);
        } else if (orientation == 3) {
            matrix.postRotate(180);
        } else if (orientation == 8) {
            matrix.postRotate(270);
        }

        if (pct > NULL_PCT) {
            bmW = (bmW / 100) * pct;
            bmH = (bmH / 100) * pct;
        }

        //rotatedBitmap = Bitmap.createScaledBitmap(sourceBitmap, sourceBitmap.getWidth(), sourceBitmap.getHeight(), false);

        scaledBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0, bmW, bmH, matrix, false);
        //scaledBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0, bmW, bmH, matrix, false);
        //scaledBitmap = sourceBitmap;

        return scaledBitmap;
    }

    public static void flush(Bitmap bm) {
        if (bm != null) {
            bm.recycle();
            bm = null;
        }
    }
}
