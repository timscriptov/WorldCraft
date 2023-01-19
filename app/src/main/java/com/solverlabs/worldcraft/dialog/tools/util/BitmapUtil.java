package com.solverlabs.worldcraft.dialog.tools.util;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public final class BitmapUtil {
    public static Bitmap decodeFile(String filepath, int size, boolean square) {
        return decodeFile(new File(filepath), size, square);
    }

    public static Bitmap decodeFile(File file, int size, boolean square) {
        try {
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(file), null, bitmapOptions);
            if (size <= 0) {
                return null;
            }
            int width_tmp = bitmapOptions.outWidth;
            int height_tmp = bitmapOptions.outHeight;
            int scale = 1;
            while (width_tmp / 2 >= size && height_tmp / 2 >= size) {
                width_tmp /= 2;
                height_tmp /= 2;
                scale++;
            }
            BitmapFactory.Options bitmapOptions2 = new BitmapFactory.Options();
            bitmapOptions2.inSampleSize = scale;
            bitmapOptions2.inScaled = true;
            if (square) {
                return cropToSquare(BitmapFactory.decodeFile(file.getAbsolutePath(), bitmapOptions2));
            }
            return BitmapFactory.decodeFile(file.getAbsolutePath(), bitmapOptions2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap cropToSquare(Bitmap bitmap) {
        if (bitmap != null) {
            if (bitmap.getWidth() > bitmap.getHeight()) {
                return Bitmap.createBitmap(bitmap, (bitmap.getWidth() - bitmap.getHeight()) / 2, 0, bitmap.getHeight(), bitmap.getHeight());
            }
            if (bitmap.getWidth() < bitmap.getHeight()) {
                return Bitmap.createBitmap(bitmap, 0, (bitmap.getHeight() - bitmap.getWidth()) / 2, bitmap.getWidth(), bitmap.getWidth());
            }
            return bitmap;
        }
        return bitmap;
    }

    public static Bitmap getThumbnail(ContentResolver contentResolver, long id) {
        Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{"_data"}, "_id=?", new String[]{String.valueOf(id)}, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            String filepath = cursor.getString(0);
            cursor.close();
            int rotation = 0;
            try {
                ExifInterface exifInterface = new ExifInterface(filepath);
                int exifRotation = exifInterface.getAttributeInt("Orientation", 0);
                if (exifRotation != 0) {
                    switch (exifRotation) {
                        case 3:
                            rotation = 180;
                            break;
                        case 6:
                            rotation = 90;
                            break;
                        case 8:
                            rotation = 270;
                            break;
                    }
                }
            } catch (IOException e) {
            }
            Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(contentResolver, id, 1, null);
            if (rotation != 0) {
                Matrix matrix = new Matrix();
                matrix.setRotate(rotation);
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
            return bitmap;
        }
        return null;
    }

    public static void createScaledImage(String sourceFile, String destinationFile, int desiredWidth, int desiredHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(sourceFile, options);
        int srcWidth = options.outWidth;
        int srcHeight = options.outHeight;
        if (desiredWidth > srcWidth) {
            desiredWidth = srcWidth;
        }
        int inSampleSize = 1;
        while (srcWidth / 2 > desiredWidth) {
            srcWidth /= 2;
            srcHeight /= 2;
            inSampleSize *= 2;
        }
        float desiredScale = desiredWidth / srcWidth;
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inSampleSize = inSampleSize;
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap sampledSrcBitmap = BitmapFactory.decodeFile(sourceFile, options);
        Matrix matrix = new Matrix();
        matrix.postScale(desiredScale, desiredScale);
        Bitmap scaledBitmap = Bitmap.createBitmap(sampledSrcBitmap, 0, 0, sampledSrcBitmap.getWidth(), sampledSrcBitmap.getHeight(), matrix, true);
        try {
            FileOutputStream out = new FileOutputStream(destinationFile);
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
