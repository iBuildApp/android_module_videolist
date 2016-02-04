/****************************************************************************
*                                                                           *
*  Copyright (C) 2014-2015 iBuildApp, Inc. ( http://ibuildapp.com )         *
*                                                                           *
*  This file is part of iBuildApp.                                          *
*                                                                           *
*  This Source Code Form is subject to the terms of the iBuildApp License.  *
*  You can obtain one at http://ibuildapp.com/license/                      *
*                                                                           *
****************************************************************************/
package com.ibuildapp.romanblack.VideoPlugin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.appbuilder.sdk.android.Utils;
import com.appbuilder.sdk.android.Widget;
import com.appbuilder.sdk.android.authorization.Authorization;
import com.flurry.android.FlurryAgent;
import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents videos list adapter.
 */
public class MediaAdapter extends BaseAdapter {

    private int imageWidth = 96;
    private int imageHeight = 68;
    private String cachePath = "";
    private Widget widget = null;
    private Activity ctx = null;
    private LayoutInflater inflater = null;
    private ArrayList<VideoItem> items = null;
    private HashMap<Integer, Bitmap> bitmaps = new HashMap<Integer, Bitmap>();
    private HashMap<Integer, View> views = new HashMap<Integer, View>();
    private FBLikePressedListener fBLikePressedListener = null;
    private SharePressedListener sharePressedListener = null;

    /**
     * Constructs new MediaAdapter instance with given parameteers.
     * @param ctx activity that using this adapter
     * @param items array of video items
     * @param widget module configuration data 
     */
    public MediaAdapter(Activity ctx, ArrayList<VideoItem> items, Widget widget) {
        this.ctx = ctx;
        this.items = items;
        this.widget = widget;

        inflater = LayoutInflater.from(this.ctx);

        ImageDownloadTask idt = new ImageDownloadTask();
        idt.execute(items);
    }

    public int getCount() {
        try {
            return items.size();
        } catch (NullPointerException nPEx) {
            return 0;
        }
    }

    public Object getItem(int arg0) {
        try {
            return items.get(arg0);
        } catch (NullPointerException nPEx) {
            return null;
        } catch (IndexOutOfBoundsException iOOBEx) {
            return null;
        }
    }

    public long getItemId(int arg0) {
        return 0;
    }

    public View getView(int arg0, View arg1, ViewGroup arg2) {
        if (arg1 == null) {
            arg1 = inflater.inflate(R.layout.romanblack_video_list_item, null);
        }

        ImageView thumbImageView = (ImageView) arg1.findViewById(R.id.romanblack_video_listview_item_thumb);
        thumbImageView.setOnClickListener(new thumbClickListener(arg0));

        if (items.get(arg0).getCoverUrl().length() > 0) {
            if (thumbImageView != null) {
                if (items.get(arg0).getCoverPath().length() > 0) {
                    thumbImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    Bitmap bitmap = null;
                    Integer key = new Integer(arg0);
                    if (bitmaps.containsKey(key)) {
                        bitmap = bitmaps.get(key);
                    } else {
                        try {
                            bitmap = decodeImageFile(items.get(arg0).getCoverPath());
                            bitmaps.put(key, bitmap);
                        } catch (Exception e) {
                            Log.d("", "");
                        }
                    }

                    if (bitmap != null) {
                        thumbImageView.setImageBitmap(bitmap);
                    }
                }
            }
        } else {
            thumbImageView.setImageBitmap(null);
        }

        TextView titleTextView = (TextView) arg1.findViewById(R.id.romanblack_video_listview_item_title);
        titleTextView.setText(items.get(arg0).getTitle());
        titleTextView.setTextColor(Statics.color3);

        TextView descriptionTextView = (TextView) arg1.findViewById(R.id.romanblack_video_listview_item_description);
        descriptionTextView.setText(items.get(arg0).getDescription());
        descriptionTextView.setTextColor(Statics.color4);

        LinearLayout shareButton = (LinearLayout) arg1.findViewById(R.id.romanblack_video_listview_item_share_btn);
        shareButton.setOnClickListener(new btnShareListener(arg0));
        if (Statics.sharingOn.equalsIgnoreCase("off")) {
            shareButton.setVisibility(View.GONE);
        }else shareButton.setVisibility(View.VISIBLE);

        LinearLayout likeLayout = (LinearLayout) arg1.findViewById(R.id.video_listview_item_like_layout);
        if (Statics.likesOn.equalsIgnoreCase("on")) {
            likeLayout.setVisibility(View.VISIBLE);
        }else likeLayout.setVisibility(View.GONE);

        LinearLayout likeButton = (LinearLayout) arg1.findViewById(R.id.romanblack_video_listview_item_like_btn);
        likeButton.setOnClickListener(new btnLikeListener(arg0));

        ImageView likeImage = (ImageView) arg1.findViewById(R.id.romanblack_video_list_like_image);

        TextView likeCaption = (TextView) arg1.findViewById(R.id.romanblack_video_list_like_caption);

        LinearLayout commentsCounter = (LinearLayout) arg1.findViewById(R.id.romanblack_video_listview_item_comments_count_layput);
        if (!Statics.commentsOn.equalsIgnoreCase("on")) {
            commentsCounter.setVisibility(View.INVISIBLE);
        }

        TextView commentsCountTextView = (TextView) arg1.findViewById(
                R.id.romanblack_video_listview_item_comments_count);
        if (items.get(arg0).getTotalComments() == 0) {
            commentsCountTextView.setText("+");
        } else if (items.get(arg0).getTotalComments() > 99) {
            commentsCountTextView.setText("99+");
        } else {
            commentsCountTextView.setText(items.get(arg0).getTotalComments() + "");
        }

        TextView likesCountTextView = (TextView) arg1.findViewById(R.id.romanblack_video_listview_item_likes_count);
        
        likesCountTextView.setText(items.get(arg0).getLikesCount() + "");
        
        if(!Utils.isChemeDark(Statics.color1)){
            likesCountTextView.setTextColor(Color.parseColor("#ffffff"));
        }else{
            likesCountTextView.setTextColor(Color.parseColor("#000000"));
        }

        if (!Statics.isOnline) {
            likeImage.setAlpha(100);
            ImageView shareImage = (ImageView) arg1.findViewById(R.id.romanblack_video_list_share_image);
            shareImage.setAlpha(100);
            likeCaption.setTextColor(Color.parseColor("#9bffffff"));
            TextView shareCaption = (TextView) arg1.findViewById(R.id.romanblack_video_list_share_caption);
            shareCaption.setTextColor(Color.parseColor("#9bffffff"));
            likeButton.getBackground().setAlpha(100);
            shareButton.getBackground().setAlpha(100);
        } else
        {
            likeImage.setAlpha(255);
            ImageView shareImage = (ImageView) arg1.findViewById(R.id.romanblack_video_list_share_image);
            shareImage.setAlpha(255);
            likeCaption.setTextColor(Color.parseColor("#ffffff"));
            TextView shareCaption = (TextView) arg1.findViewById(R.id.romanblack_video_list_share_caption);
            shareCaption.setTextColor(Color.parseColor("#ffffff"));
            likeButton.getBackground().setAlpha(255);
            shareButton.getBackground().setAlpha(255);
        }

        if (items.get(arg0).isLiked() && (Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_FACEBOOK) != null)) {
            likeImage.setAlpha(100);
            likeCaption.setTextColor(Color.parseColor("#9bffffff"));
            likeButton.getBackground().setAlpha(100);
        } else
        {
            likeImage.setAlpha(255);
            likeCaption.setTextColor(Color.parseColor("#ffffff"));
            likeButton.getBackground().setAlpha(255);
        }

        arg1.setOnClickListener(new playerStartListener(arg0));

        arg1.setBackgroundColor(Statics.color1);

        return arg1;
    }

    /**
     * Sets the external storage cache path.
     * @param cachePath the cache path to set
     */
    public void setCachePath(String cachePath) {
        this.cachePath = cachePath;
    }

    /**
     * Decodes image file to bitmap from device external storage.
     * @param imagePath image file path
     * @return decoded image bitmap
     */
    private Bitmap decodeImageFile(String imagePath) {
        try {
            File file = new File(imagePath);
            //Decode image size
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(file), null, opts);

            //Find the correct scale value. It should be the power of 2.
            int width = opts.outWidth, height = opts.outHeight;
            int scale = 1;
            while (true) {
                if (width / 2 < imageWidth || height / 2 < imageHeight) {
                    break;
                }
                width /= 2;
                height /= 2;
                scale *= 2;
            }

            //Decode with inSampleSize
            opts = new BitmapFactory.Options();
            opts.inSampleSize = scale;

            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, opts);

            int x = 0, y = 0, l = 0;
            if (width > height) {
                x = (int) (width - height) / 2;
                y = 1;
                l = height - 1;
            } else {
                x = 1;
                y = (int) (height - width) / 2;
                l = width - 1;
            }

            float matrixWidth = (float) (imageWidth - 4) / (float) l;
            float matrixHeight = (float) (imageHeight - 4) / (float) l;
            Matrix matrix = new Matrix();
            matrix.postScale(matrixWidth, matrixHeight);

            return bitmap;
        } catch (Exception e) {
            Log.d("", "");
        }

        return null;
    }

    /**
     * Refreshes rss list if image was downloaded.
     */
    private void viewUpdated() {
        this.notifyDataSetChanged();
    }

    /**
     * Refreshes rss list if image was downloaded.
     */
    private void downloadComplete() {
        this.notifyDataSetChanged();
    }

    private void downloadRegistration(int position, String value) {
        this.items.get(position).setCoverPath(value);
    }

    /**
     * Sets "Like" button on click listener.
     * @param fBLikePressedListener the listener to set
     */
    public void setfBLikePressedListener(FBLikePressedListener fBLikePressedListener) {
        this.fBLikePressedListener = fBLikePressedListener;
    }

    /**
     * Sets "Share" button on click listener.
     * @param sharePressedListener the listener to set
     */
    public void setSharePressedListener(SharePressedListener sharePressedListener) {
        this.sharePressedListener = sharePressedListener;
    }

    /**
     * Callback for Facebook likes.
     * Must be implemented by Activity that using this Adapter.
     */
    public interface FBLikePressedListener {

        public void onLikePressed(int position);
    }

    /** 
     * Callback for Sharing.
     * Must be implemented by Activity that using this Adapter.
     */
    public interface SharePressedListener {

        public void onSharePressed(int position);
    }

    /**
     * This class creates a background thread to download avatar images of comments.
     */
    private class ImageDownloadTask extends AsyncTask<ArrayList<VideoItem>, String, Void> {

        @Override
        protected Void doInBackground(ArrayList<VideoItem>... items) {
            try {//ErrorLogging

                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = 4;

                for (int i = 0; i < items[0].size(); i++) {
                    if (isCancelled()) {
                        return null;
                    }

                    String imagefile = cachePath + "/images/" + Utils.md5(items[0].get(i).getCoverUrl());
                    
                    //items[0].get(i).setCoverPath(cachePath + "/images/" + Utils.md5(items[0].get(i).getCoverUrl()));

                    if (/*items[0].get(i).getCoverPath().length()*/imagefile.length() > 0) {
                        File file = new File(items[0].get(i).getCoverPath());
                        if (file.exists()) {
                            downloadRegistration(i, imagefile);
                            publishProgress();
                            continue;
                        }
                    }

                    if (items[0].get(i).getCoverUrl().length() == 0) {
                        continue;
                    }

                    SystemClock.sleep(10);
                    try {
                        URL imageUrl = new URL(URLDecoder.decode(items[0].get(i).getCoverUrl()));
                        BufferedInputStream bis = new BufferedInputStream(imageUrl.openConnection().getInputStream());
                        ByteArrayBuffer baf = new ByteArrayBuffer(32);
                        int current = 0;
                        while ((current = bis.read()) != -1) {
                            baf.append((byte) current);
                        }
                        String fileImagesDir = cachePath + "/images/";
                        File fileImagesDirect = new File(fileImagesDir);
                        if (!fileImagesDirect.exists()) {
                            fileImagesDirect.mkdirs();
                        }
                        String filename = cachePath + "/images/" + Utils.md5(items[0].get(i).getCoverUrl());
                        FileOutputStream fos = new FileOutputStream(new File(filename));
                        fos.write(baf.toByteArray());
                        fos.close();

                        downloadRegistration(i, filename);
                    } catch (Exception e) {
                        Log.e("", "");
                    }
                    publishProgress();
                }

                return null;

            } catch (Exception e) {//ErrorLogging
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(String... param) {
            viewUpdated();
        }

        @Override
        protected void onPostExecute(Void unused) {
            downloadComplete();
        }
    }

    /**
     * OnClickListener to set to share button.
     */
    public class btnShareListener implements View.OnClickListener {

        private int position = 0;

        public btnShareListener(int position) {
            this.position = position;
        }

        public void onClick(View arg0) {

            if (Utils.networkAvailable(ctx)) {
                if (sharePressedListener != null) {
                    sharePressedListener.onSharePressed(position);
                }
            } else {
                Toast.makeText(ctx, ctx.getResources().getString(R.string.romanblack_video_alert_share_need_internet), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * OnClickListener to set to like button.
     */
    public class btnLikeListener implements View.OnClickListener {

        private int position = 0;

        public btnLikeListener(int position) {
            this.position = position;
        }

        public void onClick(View arg0) {
            if (Utils.networkAvailable(ctx)) {
                if (fBLikePressedListener != null) {
                    fBLikePressedListener.onLikePressed(position);
                }
            } else
                Toast.makeText(ctx, ctx.getResources().getString(R.string.romanblack_video_alert_like_need_internet), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * OnClickListener to set to comments count bubble.
     */
    public class btnCommentsCountListener implements View.OnClickListener {

        private int position = 0;

        public btnCommentsCountListener(int position) {
            this.position = position;
        }

        public void onClick(View arg0) {
            Intent it = new Intent(ctx, CommentsActivity.class);
            it.putExtra("items", items);
            it.putExtra("position", position);
            it.putExtra("cachePath", cachePath);
            it.putExtra("Widget", widget);
            ctx.startActivity(it);
        }
    }

    /**
     * OnClickListener to set to adapter result item.
     */
    public class playerStartListener implements View.OnClickListener {

        private int position = 0;

        public playerStartListener(int position) {
            this.position = position;
        }

        public void onClick(View arg0) {
            Intent it = new Intent(ctx, VideoPlayer.class);
            it.putExtra("items", items);
            it.putExtra("position", position);
            it.putExtra("cachePath", cachePath);
            it.putExtra("Widget", widget);
            ctx.startActivity(it);
        }
    }
    /* CallBack for Facebook likes*/

    /**
     * OnClickListener to set to video thumbnail.
     */
    public class thumbClickListener implements View.OnClickListener {

        private int position = 0;

        public thumbClickListener(int position) {
            this.position = position;
        }

        public void onClick(View arg0) {
            startVideoPlayer();
        }

        private void startVideoPlayer() {
            if (VideoPlugin.userID != null && VideoPlugin.userID.equals("186589")) {
                Map<String, String> maps = new HashMap<String, String>();
                maps.put("Watch", items.get(position).getTitle());
                FlurryAgent.logEvent("VideoPlugin", maps, true);
            }

            if (items.get(position).getUrl().contains("youtube.com")) {
                ctx.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.youtube.com")).setData(Uri.parse(items.get(position).getUrl())));

                return;
            }
            if (items.get(position).getUrl().contains("vimeo.com")) {
                ctx.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(items.get(position).getUrl())));

                return;
            }

            if (items.get(position).getUrl().contains("m3u8")) {
                Intent it = new Intent(ctx, VideoBuffer.class);
                it.putExtra("position", position);
                it.putExtra("items", items);
                it.putExtra("Widget", widget);
                ctx.startActivityForResult(it, VideoPlugin.VIDEO_PLAYER);
                return;
            }

            Intent it = new Intent(ctx, PlayerWebActivity.class);
            it.putExtra("position", position);
            it.putExtra("items", items);
            it.putExtra("Widget", widget);
            ctx.startActivity(it);
        }
    }
    /* CallBack for Facebook likes*/
}
