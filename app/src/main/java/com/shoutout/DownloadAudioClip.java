package com.shoutout;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Random;

/**
 * Created by swarup921 on 4/29/2015.
 */
public class DownloadAudioClip extends AsyncTask<String, Integer, String> {
    @Override
    protected String doInBackground(String... params) {
        String targetFileName="";
        String downloadFolder="Shoutout";
        String downloadPath="";
        int count=0;
        try {


            URL url = new URL((String) params[0]);
            URLConnection conn = url.openConnection();
            conn.connect();
            Random randomGenerator = new Random();
                int randomInt = randomGenerator.nextInt(999999);
            targetFileName="shoutout"+randomInt+".mp3";//Change name and subname
            int lenghtOfFile = conn.getContentLength();
            downloadPath = Environment.getExternalStorageDirectory()+ "/"+downloadFolder+"/";
            File folder = new File(downloadPath);
            if(!folder.exists()){
                folder.mkdir();//If there is no folder it will be created.
            }
            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(downloadPath+targetFileName);
            byte data[] = new byte[1024];
            long total = 0;
            while ((count = input.read(data)) != -1) {
                total += count;
                publishProgress ((int)(total*100/lenghtOfFile));
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {}
        return downloadPath+targetFileName;
    }

    @Override
    protected void onPostExecute(String downloadedFileName) {
        super.onPostExecute(downloadedFileName);
    }
}
