package com.shoutout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.json.Json;
import javax.json.stream.JsonParser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutionException;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;


public class MainActivity extends ActionBarActivity {
    Context mainActivityContext;
    LinearLayout fullScreenLayout;
    LinearLayout audioClipsListLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fullScreenLayout = (LinearLayout) findViewById(R.id.fullScreenLayout);
        audioClipsListLayout = (LinearLayout) findViewById(R.id.audioClipsListLayout);
        mainActivityContext = this;
        loadAudioClipData();
    }

    public void loadAudioClipData() {
        new DownloadAudioClipsData().execute("Hello");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
       //     return true;
        //}

        return super.onOptionsItemSelected(item);
    }

    public class DownloadAudioClipsData extends AsyncTask<String, Integer, List<AudioClip>> {
        @Override
        protected List<AudioClip> doInBackground(String... params) {

            long totalSize = 0;
            String output = null;
            String valueStr = null;
            try {
                String serviceURI = "https://shoutout-ws2.herokuapp.com/db/audioclips/clip-id=swa";

                URL url = new URL(serviceURI);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                int respCode = conn.getResponseCode();

                if (conn.getResponseCode() != 200) {
                    int responseCode = conn.getResponseCode();
                    throw new RuntimeException("Failed : HTTP error code : "
                            + conn.getResponseCode());
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream())));

                output = br.readLine();

                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
            }

            JsonParser jsonParser = Json.createParser(new StringReader(output));
            String keyname = null;
            List<AudioClip> audioClipsList = new ArrayList<>();
            AudioClip newClip = null;

            //The first three objects in the audioClipsList ArrayList are used
            // only to store the icons of Whatsapp,FB Messenger
            // and generic Share widget

            //Object#1 in audioClipsList
            Bitmap whatsappIconBitMap = loadBitmapFromUrl("https://s3.amazonaws.com/shoutout-s3/images/app/whatsapp_icon.png");
            AudioClip newClipForWhatsappIcon = new AudioClip();
            newClipForWhatsappIcon.setClipImage(whatsappIconBitMap);
            audioClipsList.add(newClipForWhatsappIcon);

            //Object#2 in audioClipsList
            Bitmap fbMessengerIconBitMap = loadBitmapFromUrl("https://s3.amazonaws.com/shoutout-s3/images/app/fb_icon.png");
            AudioClip newClipForFbMessengerIcon = new AudioClip();
            newClipForFbMessengerIcon.setClipImage(fbMessengerIconBitMap);
            audioClipsList.add(newClipForFbMessengerIcon);

            //Object#3 in audioClipsList
            Bitmap shareIconBitMap = loadBitmapFromUrl("https://s3.amazonaws.com/shoutout-s3/images/app/share_icon.png");
            AudioClip newClipForShareIcon = new AudioClip();
            newClipForShareIcon.setClipImage(shareIconBitMap);
            audioClipsList.add(newClipForShareIcon);

            while (jsonParser.hasNext()) {
                switch (jsonParser.next()) {
                    case KEY_NAME:
                        keyname = jsonParser.getString();
                        break;
                    case VALUE_STRING:
                        valueStr = jsonParser.getString();

                        switch (keyname) {
                            case "ClipId":
                                newClip = new AudioClip();
                                newClip.setClipId(Integer.parseInt(valueStr));
                                break;
                            case "ClipTitle":
                                newClip.setClipTitle(valueStr);
                                audioClipsList.add(newClip);
                                break;
                            case "ClipTags":
                                newClip.setClipTags(valueStr);
                                break;
                            case "ClipCategory":
                                newClip.setClipCategory(valueStr);
                                break;
                            case "ClipImageURL":
                                newClip.setClipImageUrl(valueStr);
                                newClip.setClipImage(loadBitmapFromUrl(valueStr));
                                break;
                            case "ClipAudioURL":
                                newClip.setClipAudioUrl(valueStr);
                                break;
                        }
                    default:
                        break;
                }
            }

            return audioClipsList;
        }

        @Override
        protected void onPostExecute(List<AudioClip> audioClipsList) {
            ListIterator<AudioClip> audioClipIterator = audioClipsList.listIterator();

            AudioClip audioClip = null;
            String audioClipStr = "";
            int clipId = 0;
            int componentIdBegin = 0;
            String clipTitle = null;
            String clipTags = null;
            String clipCategory = null;
            int spaceBetweenShareIcons = 80;

            String clipAudioUrl = null;


            Bitmap whatsappIconBitmap = null;
            Bitmap fbMessengerIconBitmap = null;
            Bitmap shareIconBitmap = null;

            //Declare ImageViews for all the three share icons
            ImageView whatsappIconImageView = null;
            ImageView fbMessengerIconImageView = null;
            ImageView shareIconImageView = null;

            //Declare an AudioClip object to  temporarily store the three objects
            // of Share Icons from AudioClipsList Collection
            AudioClip audioClipForShareIcon = null;

            //The first Audioclip object in audioClipsList collection contains only Whatsapp icon image
            if (audioClipIterator.hasNext()) {
                audioClipForShareIcon = audioClipIterator.next();
                whatsappIconBitmap = audioClipForShareIcon.getClipImage();

            }

            //The second Audioclip object in audioClipsList collection contains only FBMessenger icon image
            if (audioClipIterator.hasNext()) {
                audioClipForShareIcon = audioClipIterator.next();
                fbMessengerIconBitmap = audioClipForShareIcon.getClipImage();

            }

            //The third Audioclip object in audioClipsList collection contains only GenericShare icon image
            if (audioClipIterator.hasNext()) {
                audioClipForShareIcon = audioClipIterator.next();
                shareIconBitmap = audioClipForShareIcon.getClipImage();
            }

            while (audioClipIterator.hasNext()) {

                audioClip = audioClipIterator.next();

                clipId = audioClip.getClipId();

                componentIdBegin = clipId;


                RelativeLayout singleAudioClipLayout = new RelativeLayout(mainActivityContext);
                RelativeLayout.LayoutParams singleAudioClipLayoutParams =
                        new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                singleAudioClipLayoutParams.setMargins(20, 20, 20, 20);
                singleAudioClipLayout.setLayoutParams(singleAudioClipLayoutParams);
                singleAudioClipLayout.setBackgroundColor(Color.WHITE);

                //Create a ImageView to display AudioClip Image on the Activity
                ImageView audioClipImageImageView = new ImageView(mainActivityContext);

                RelativeLayout.LayoutParams audioClipImageImageViewLayoutParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                audioClipImageImageViewLayoutParams.setMargins(5, 15, 20, 15);
                audioClipImageImageViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                audioClipImageImageView.setLayoutParams(audioClipImageImageViewLayoutParams);

                audioClipImageImageView.setImageBitmap(audioClip.getClipImage());
                audioClipImageImageView.setId(componentIdBegin * 10 + 1);

                singleAudioClipLayout.addView(audioClipImageImageView);

                //Read the next AudioClip Object in the audioClipsList Collection


                clipTitle = audioClip.getClipTitle();
                clipTags = audioClip.getClipTags();
                clipCategory = audioClip.getClipCategory();
                clipAudioUrl = audioClip.getClipAudioUrl();

                //Frame the string for Audio Clip Details
                audioClipStr = "Title : " + clipTitle + "\nTags : "
                        + clipTags + "\nCategory:" + clipCategory;

                //Create a TextView to display AudioClip Details String on the Activity
                TextView audioClipDetailsTextView = new TextView(mainActivityContext);
                audioClipDetailsTextView.setText(audioClipStr);

                RelativeLayout.LayoutParams audioClipDetailsTextViewLayoutParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                audioClipDetailsTextViewLayoutParams.addRule(RelativeLayout.RIGHT_OF, audioClipImageImageView.getId());
                audioClipDetailsTextViewLayoutParams.setMargins(5, 15, 20, 0);
                audioClipDetailsTextView.setLayoutParams(audioClipDetailsTextViewLayoutParams);
                audioClipDetailsTextView.setId(componentIdBegin * 10 + 2);
                //Add AudioClip Image and Details to the Layout on the activity
                //((LinearLayout) linearLayout).addView(audioClipImageImageView);
                singleAudioClipLayout.addView(audioClipDetailsTextView);

                //Initialize ImageViews for all the three share icons
                whatsappIconImageView = new ImageView(mainActivityContext);
                fbMessengerIconImageView = new ImageView(mainActivityContext);
                shareIconImageView = new ImageView(mainActivityContext);

                //Set the ImageBitMaps for all the three share icons
                whatsappIconImageView.setImageBitmap(whatsappIconBitmap);
                whatsappIconImageView.setId(componentIdBegin * 10 + 3);
                RelativeLayout.LayoutParams whatsappIconImageViewLayoutParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                whatsappIconImageViewLayoutParams.addRule(RelativeLayout.RIGHT_OF, audioClipImageImageView.getId());
                whatsappIconImageViewLayoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, audioClipImageImageView.getId());
                whatsappIconImageViewLayoutParams.setMargins(0, 10, spaceBetweenShareIcons, 15);
                whatsappIconImageView.setLayoutParams(whatsappIconImageViewLayoutParams);

                fbMessengerIconImageView.setImageBitmap(fbMessengerIconBitmap);
                fbMessengerIconImageView.setId(componentIdBegin * 10 + 4);
                RelativeLayout.LayoutParams fbMessengerIconImageViewLayoutParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                fbMessengerIconImageViewLayoutParams.addRule(RelativeLayout.RIGHT_OF, whatsappIconImageView.getId());
                fbMessengerIconImageViewLayoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, audioClipImageImageView.getId());
                fbMessengerIconImageViewLayoutParams.setMargins(0, 10, spaceBetweenShareIcons, 15);
                fbMessengerIconImageView.setLayoutParams(fbMessengerIconImageViewLayoutParams);

                shareIconImageView.setImageBitmap(shareIconBitmap);
                shareIconImageView.setId(componentIdBegin * 10 + 5);
                RelativeLayout.LayoutParams shareIconImageViewLayoutParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                shareIconImageViewLayoutParams.addRule(RelativeLayout.RIGHT_OF, fbMessengerIconImageView.getId());
                shareIconImageViewLayoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, audioClipImageImageView.getId());
                shareIconImageViewLayoutParams.setMargins(0, 10, spaceBetweenShareIcons, 15);
                shareIconImageView.setLayoutParams(shareIconImageViewLayoutParams);

                //Add the three share icons to this Audio Clip Display Object in the Activity
                singleAudioClipLayout.addView(whatsappIconImageView);
                singleAudioClipLayout.addView(fbMessengerIconImageView);
                singleAudioClipLayout.addView(shareIconImageView);

                //Create and add a Dummy view to insert border between each Clip
                View dummyBorder = new View(mainActivityContext);
                RelativeLayout.LayoutParams dummyBorderParams =
                        new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, 20);
                dummyBorder.setLayoutParams(dummyBorderParams);
                dummyBorder.setBackgroundColor(Color.DKGRAY);

                audioClipsListLayout.addView(dummyBorder);

                audioClipsListLayout.addView(singleAudioClipLayout);

                createImageViewClickListener(audioClipImageImageView, clipAudioUrl);
                createWhatsappIconClickListener(whatsappIconImageView, clipAudioUrl);
                createFbMessengerIconClickListener(fbMessengerIconImageView, clipAudioUrl);
                createShareIconClickListener(shareIconImageView, clipAudioUrl);
            }
        }

        public Bitmap loadBitmapFromUrl(String clipImageUrl) {
            Bitmap bitmap = null;
            try {
                URL url = new URL(clipImageUrl);

                HttpGet httpRequest = null;

                httpRequest = new HttpGet(url.toURI());

                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse response = (HttpResponse) httpclient
                        .execute(httpRequest);

                HttpEntity entity = response.getEntity();
                BufferedHttpEntity b_entity = new BufferedHttpEntity(entity);
                InputStream input = b_entity.getContent();

                bitmap = BitmapFactory.decodeStream(input);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return bitmap;
        }


        void createImageViewClickListener(final ImageView img, final String clipAudioUrl) {
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    try {
                        mediaPlayer.setDataSource(clipAudioUrl);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        mediaPlayer.prepare(); // might take long! (for buffering, etc)
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mediaPlayer.start();
                }
            });
        }

        //Listener method for Whatsapp Icon touch event
        void createWhatsappIconClickListener(final ImageView img, final String clipAudioUrl) {
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = getApplicationContext();
                    CharSequence text = "Sharing on Whatsapp..";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.setGravity(Gravity.CENTER_HORIZONTAL, 50, 0);
                    toast.show();

                    String downloadedFileName = null;
                    DownloadAudioClip downloadTask = new DownloadAudioClip();
                    downloadTask.execute(clipAudioUrl);
                    try {
                        //This Async task will return the downloaded file name along with the Path.
                        downloadedFileName = downloadTask.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    final Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                    shareIntent.setType("audio/mp3");
                    shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.parse("file://" + downloadedFileName));
                    shareIntent.setPackage("com.whatsapp");
                    startActivity(Intent.createChooser(shareIntent, "Share Audio Clip"));
                }
            });
        }

        //Listener method for FB Messenger Icon touch event
        void createFbMessengerIconClickListener(final ImageView img, final String clipAudioUrl) {
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = getApplicationContext();
                    CharSequence text = "Sharing on FB Messenger..";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.setGravity(Gravity.CENTER_HORIZONTAL, 50, 0);
                    toast.show();
                    String downloadedFileName = null;
                    DownloadAudioClip downloadTask = new DownloadAudioClip();
                    downloadTask.execute(clipAudioUrl);
                    try {
                        //This Async task will return the downloaded file name along with the Path.
                        downloadedFileName = downloadTask.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    final Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                    shareIntent.setType("audio/mp3");
                    shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.parse("file://" + downloadedFileName));
                    shareIntent.setPackage("com.facebook.orca");
                    startActivity(Intent.createChooser(shareIntent, "Share Audio Clip"));
                }
            });
        }

        //Listener method for Share Icon touch event
        void createShareIconClickListener(final ImageView img, final String clipAudioUrl) {
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String downloadedFileName = null;
                    DownloadAudioClip downloadTask = new DownloadAudioClip();
                    downloadTask.execute(clipAudioUrl);
                    try {
                        //This Async task will return the downloaded file name along with the Path.
                        downloadedFileName = downloadTask.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    final Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                    shareIntent.setType("audio/mp3");
                    shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.parse("file://" + downloadedFileName));
                    startActivity(Intent.createChooser(shareIntent, "Share Audio Clip"));
                }
            });
        }

    }
}
