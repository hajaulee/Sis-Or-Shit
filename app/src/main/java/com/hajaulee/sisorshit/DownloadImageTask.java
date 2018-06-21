package com.hajaulee.sisorshit;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    Activity sender;
    public DownloadImageTask(Activity sender) {
        this.sender = sender;
    }

    protected Bitmap doInBackground(String... urls) {
        String displayUrl = urls[0];
        Bitmap mIcon11 = null;
        try {
          InputStream in = new java.net.URL(displayUrl).openStream();
          mIcon11 = BitmapFactory.decodeStream(in);
          MainActivity.getInstance().getCaptchaView().setImageBitmap(mIcon11);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {/*
    	ImageView bmImage = (ImageView) sender.findViewById(R.id.webCaptcha);
    	TextView capText = (TextView) sender.findViewById(R.id.captchaText);
    	if(bmImage != null)
    		bmImage.setImageBitmap(result);
    	if(capText != null)
    		capText.setText(MainActivity.captchaToText(result, MainActivity.digit));
    	*/
    }
  }