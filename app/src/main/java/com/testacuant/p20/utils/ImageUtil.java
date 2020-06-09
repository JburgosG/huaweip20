package com.testacuant.p20.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.acuant.acuantimagepreparation.model.CroppingData;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ImageUtil {

    private ImageUtil() {
        throw new IllegalStateException("Image Util");
    }

    /**
     * Reads file from given location.
     *
     * @param fileUrl Location.
     * @return Bytes or {code null} if error occures.
     */
    public static byte[] readFile(final String fileUrl) throws IOException {
        final File file = new File(fileUrl);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
        try {
            final byte[] buffer = new byte[4096];
            int read = bufferedInputStream.read(buffer, 0, buffer.length);
            while (read != -1) {
                outputStream.write(buffer, 0, read);
                read = bufferedInputStream.read(buffer, 0, buffer.length);
            }
        } finally {
            try {
                bufferedInputStream.close();
            } finally {
                outputStream.close();
            }
        }
        return outputStream.toByteArray();
    }

    public static CroppingData cropImage(final byte[] imageBytes) {
        final Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        final CroppingData data = new CroppingData();
        data.image = bitmap;
        return  data;
    }

    public static byte[] bitmapToBytes(final Bitmap bitmap) {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
        bitmap.recycle();

        return stream.toByteArray();
    }

    public static Bitmap resize(final Bitmap bitmap, final int width) {
        final float ratio = (float) width / bitmap.getWidth();
        final int newHeight = Math.round(ratio * bitmap.getHeight());
        final int newWidth = Math.round(ratio * bitmap.getWidth());
        final Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        bitmap.recycle();

        return resizedBitmap;
    }

    /**
     * Encodes the input byte array as Base64.
     *
     * @param imageData Input byte array.
     * @return Base64 encoded {@code String}.
     */
    public static String base64FromImage(final byte[] imageData) {
        String vacio = "";
        if (imageData == null) {
            return vacio;
        }
        return new String(Base64.encode(imageData, Base64.NO_WRAP));
    }


    /**
     * Decodes Base64 to image bytes.
     *
     * @param base64 Input Base64 {@code String}.
     * @return Decoded byte array.
     */
    public static byte[] imageFromBase64(final String base64) {
        byte[] vacio = new byte[0];
        if (base64 == null) {
            return vacio;
        }
        return Base64.decode(base64, Base64.DEFAULT);
    }

}
