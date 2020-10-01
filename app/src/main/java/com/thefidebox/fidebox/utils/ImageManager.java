package com.thefidebox.fidebox.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

public class ImageManager {

    private static final String TAG = "ImageManager";
    public static final int IMAGE_SAVE_QUALITY = 85;
    public static FileInputStream fis;
    public static Bitmap bitmap;
    public static URL imgUrl;

    public static Bitmap getBitmap(Uri imgUri) {
       // File imageFile = new File(imgUrl);

        File imageFile= new File(imgUri.getPath());

        FileInputStream fis = null;
        Bitmap bitmap = null;
        try{
            fis = new FileInputStream(imageFile);
            bitmap = BitmapFactory.decodeStream(fis);
        }catch (FileNotFoundException e){
            Log.e(TAG, "getBitmap: FileNotFoundException 1: " + e.getMessage() );
        }finally {
            try{
                fis.close();
            }catch (IOException e){
                Log.e(TAG, "getBitmap: FileNotFoundException: 2 " + e.getMessage() );
            }
        }
        return bitmap;
    }
    public static byte[] getBytesFromBitmap(Bitmap bm, int quality){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, quality, stream);

        return stream.toByteArray();
    }
    }
