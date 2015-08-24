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

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.appbuilder.sdk.android.Utils;
import com.appbuilder.sdk.android.Widget;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.http.util.ByteArrayBuffer;

/**
 * This class represents comments list adapter.
 */
public class CommentsAdapter extends BaseAdapter {

    private int imageWidth = 25;
    private int imageHeight = 25;
    private String cachePath = "";
    private Widget widget = null;
    private Context ctx = null;
    private LayoutInflater inflater = null;
    private VideoItem video = null;
    private ArrayList<CommentItem> items = null;
    private HashMap<String, Bitmap> bitmaps = new HashMap<String, Bitmap>();

    /**
     * Constructs new comments adapter with given parameteers.
     * @param ctx activity that using this adapter
     * @param items array of comments
     * @param video video on which comments was made
     * @param widget module configuration data 
     */
    public CommentsAdapter(Context ctx, ArrayList<CommentItem> items,
            VideoItem video, Widget widget) {
        this.ctx = ctx;
        this.items = items;
        this.widget = widget;
        this.video = video;

        inflater = LayoutInflater.from(this.ctx);

        ImageDownloadTask idt = new ImageDownloadTask();
        idt.execute(items);
    }

    /**
     * Sets new comments to adapter.
     * @param items array of comments
     */
    public void setItems(ArrayList<CommentItem> items) {
        this.items = items;
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
            arg1 = inflater.inflate(R.layout.romanblack_video_comments_list_item, null);
        }

        ImageView thumbImageView = (ImageView) arg1.findViewById(R.id.romanblack_video_comments_list_item_thumb);

        if (items.get(arg0).getAvatarUrl().length() > 0) {
            if (thumbImageView != null) {
                if (items.get(arg0).getAvatarPath().length() > 0) {
                    thumbImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    Bitmap bitmap = null;
                    String key = items.get(arg0).getAvatarUrl();
                    if (bitmaps.containsValue(key)) {
                        bitmap = bitmaps.get(key);
                    } else {
                        try {
                            bitmap = decodeImageFile(items.get(arg0).getAvatarPath());
                            bitmaps.put(key, bitmap);
                        } catch (Exception e) {
                            Log.d("", "");
                        }
                    }

                    if (bitmap != null) {
                        thumbImageView.setImageBitmap(bitmap);
                        thumbImageView.setBackgroundColor(Color.argb(180, 255, 255, 255));
                    }
                }
            }
        } else {
            thumbImageView.setImageBitmap(null);
        }

        TextView authorTextView = (TextView) arg1.findViewById(R.id.romanblack_video_comments_list_item_author);
        authorTextView.setText(items.get(arg0).getAuthor());
        authorTextView.setTextColor(Statics.color3);


        TextView textTextView = (TextView) arg1.findViewById(R.id.romanblack_video_comments_list_item_text);
        textTextView.setText(items.get(arg0).getText());
        textTextView.setTextColor(Statics.color4);

        TextView dateTextView = (TextView) arg1.findViewById(R.id.romanblack_video_comments_list_item_date);
        dateTextView.setTextColor(Statics.color5);
        String dateText = "";
        try {
            long msgDate = items.get(arg0).getDate().getTime();
            long curDate = System.currentTimeMillis();
            long interval = curDate - msgDate;

            if (interval < 60 * 1000) {
                dateText = ctx.getResources().getString(R.string.romanblack_video_date_just_now);
            } else if (interval < 2 * 60 * 1000) {
                dateText = ctx.getResources().getString(R.string.romanblack_video_date_one_minute);
            } else if (interval < 60 * 60 * 1000) {
                dateText = (interval / 60 / 1000) + " "
                        + ctx.getResources().getString(R.string.romanblack_video_date_minutes);
            } else if (interval < 2 * 60 * 60 * 1000) {
                dateText = ctx.getResources().getString(R.string.romanblack_video_date_one_hour);
            } else if (interval < 24 * 60 * 60 * 1000) {
                dateText = (interval / 60 / 60 / 1000) + " "
                        + ctx.getResources().getString(R.string.romanblack_video_date_hours);
            } else if (interval < 2 * 24 * 60 * 60 * 1000) {
                dateText = ctx.getResources().getString(R.string.romanblack_video_date_yesterday);
            } else if (interval < 5 * 24 * 60 * 60 * 1000) {
                dateText = (interval / 24 / 60 / 60 / 1000) + " "
                        + ctx.getResources().getString(R.string.romanblack_video_date_days);
            } else {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM, d");
                    dateText = sdf.format(items.get(arg0).getDate());
                } catch (Exception ex) {
                    Log.d("", "");
                }
            }
        } catch (Exception ex) {
        }
        dateTextView.setText(dateText);

        LinearLayout commentsCounterLayout = (LinearLayout) arg1.findViewById(R.id.romanblack_video_comments_list_item_comments_count_layput);
        TextView commentsCounter = (TextView) arg1.findViewById(R.id.romanblack_video_comments_list_item_comments_count);
        if (items.get(arg0).getCommentsCount() > 0) {
            commentsCounter.setText(items.get(arg0).getCommentsCount() + "+");
        } else {
            commentsCounter.setText("+");
        }

        if (items.get(arg0).getReplyId() == 0) {
            commentsCounterLayout.setOnClickListener(new btnCommentsCountListener(arg0));
        } else {
            commentsCounterLayout.setVisibility(View.GONE);
        }

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

            return Bitmap.createBitmap(bitmap, x, y, l, l, matrix, true);
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
        this.items.get(position).setAvatarPath(value);
    }

    /**
     * This class creates a background thread to download avatar images of comments.
     */
    private class ImageDownloadTask extends AsyncTask<ArrayList<CommentItem>, String, Void> {

        @Override
        protected Void doInBackground(ArrayList<CommentItem>... items) {
            try {//ErrorLogging

                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = 4;

                for (int i = 0; i < items[0].size(); i++) {
                    if (isCancelled()) {
                        return null;
                    }

                    //items[0].get(i).setAvatarPath(cachePath + "/images/" + Utils.md5(items[0].get(i).getAvatarUrl()));
                    String imagefile = cachePath + "/images/" + Utils.md5(items[0].get(i).getAvatarUrl());

                    if (/*items[0].get(i).getAvatarPath().length() > 0*/
                            imagefile.length() > 0) {
                        File file = new File(/*items[0].get(i).getAvatarPath()*/ imagefile);
                        if (file.exists()) {
                            downloadRegistration(i, imagefile);
                            publishProgress();
                            continue;
                        }
                    }

                    if (items[0].get(i).getAvatarUrl().length() == 0) {
                        continue;
                    }

                    SystemClock.sleep(10);
                    try {
                        URL imageUrl = new URL(URLDecoder.decode(items[0].get(i).getAvatarUrl()));
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
                        String filename = cachePath + "/images/" + Utils.md5(items[0].get(i).getAvatarUrl());
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
     * This OnClickListener sets up to start CommentsToCommentsActivity.
     */
    public class btnCommentsCountListener implements View.OnClickListener {

        private int position = 0;

        public btnCommentsCountListener(int position) {
            this.position = position;
        }

        public void onClick(View arg0) {
            Intent it = new Intent(ctx, CommentsToCommentActivity.class);
            it.putExtra("video", video);
            it.putExtra("items", items);
            it.putExtra("position", position);
            it.putExtra("cachePath", cachePath);
            it.putExtra("Widget", widget);
            ctx.startActivity(it);
        }
    }
}