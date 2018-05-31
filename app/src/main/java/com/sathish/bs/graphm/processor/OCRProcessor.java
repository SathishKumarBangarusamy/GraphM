package com.sathish.bs.graphm.processor;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class OCRProcessor {
    public String DATA_PATH;
    public static final String TAG = "TESSERACT";
    private static final String lang = "eng";

    public OCRProcessor(Context context) {
        DATA_PATH = context.getFilesDir() + "/ocr/";
        String[] paths = new String[]{DATA_PATH, DATA_PATH + "tessdata/"};

        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    return;
                } else {
                    Log.v(TAG, "Created directory " + path + " on sdcard");
                }
            }

        }

        if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
            try {

                AssetManager assetManager = context.getAssets();
                InputStream in = assetManager.open(lang + ".traineddata");
                OutputStream out = new FileOutputStream(DATA_PATH + "tessdata/" + lang + ".traineddata");
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
                Log.v(TAG, "Copied " + lang + " traineddata");
            } catch (Exception e) {
                Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
            }
        }
    }

    public String extractText(Bitmap bitmap) {
        try {
            TessBaseAPI baseApi = new TessBaseAPI();
            baseApi.setDebug(true);
            baseApi.init(DATA_PATH, lang);
            baseApi.setImage(bitmap);
            String recognizedText = baseApi.getUTF8Text();
            baseApi.end();
            Log.v(TAG, "ROUGH OCR TEXT: " + recognizedText);
            recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", "");
            recognizedText = recognizedText.trim();
            if (recognizedText.length() != 0) {
                return recognizedText.substring(recognizedText.length()-1);
            } else {
                return "Unk";
            }
        } catch (Exception e) {
            return "Unk";
        }
    }
}
