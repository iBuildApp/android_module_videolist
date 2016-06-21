package com.ibuildapp.romanblack.VideoPlugin.viewholders.main;

import android.view.View;

import com.bumptech.glide.Glide;
import com.ibuildapp.romanblack.VideoPlugin.R;
import com.ibuildapp.romanblack.VideoPlugin.api.vimeoapi.VimeoApi;
import com.ibuildapp.romanblack.VideoPlugin.api.vimeoapi.model.VimeoResponse;
import com.ibuildapp.romanblack.VideoPlugin.model.VideoItem;
import com.ibuildapp.romanblack.VideoPlugin.utils.DateUtils;
import com.ibuildapp.romanblack.VideoPlugin.utils.rx.RxUtils;
import com.ibuildapp.romanblack.VideoPlugin.utils.rx.SimpleSubscriber;

import java.util.ArrayList;
import java.util.Date;

import rx.schedulers.Schedulers;


public class VimeoViewHolder extends MainViewHolder{
    public VimeoViewHolder(View v, VimeoApi vimeoApi) {
        super(v);
    }

    @Override
    public void postView(final int position, final ArrayList<VideoItem> items) {
        VideoItem currentItem = items.get(position);
        Glide.with(thumbImageView.getContext())
                .load(currentItem.getCoverUrl())
                .dontAnimate()
                .into(thumbImageView);

        final String agoString = DateUtils.getAgoDateWithAgo(postTime.getContext(), items.get(position).getCreationLong());

        postTime.setText(agoString);
        durationLayout.setVisibility(View.VISIBLE);
        durationText.setText(currentItem.getXmlDuration());
        /*if (currentItem.getVimeoResponse() == null) {
            api.getVideoInfo(currentItem.getUrl())
                    .compose(RxUtils.<VimeoResponse>applyCustomSchedulers(Schedulers.io(), Schedulers.computation()))
                    .subscribe(new SimpleSubscriber<VimeoResponse>() {

                        @Override
                        public void onNext(VimeoResponse response) {
                            items.get(position).setVimeoResponse(response);
                            onVimeoDataLoad(response);
                        }
                    });
        }else
            onVimeoDataLoad(items.get(position).getVimeoResponse());*/
    }
}
