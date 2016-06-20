package com.ibuildapp.romanblack.VideoPlugin.details.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.ibuildapp.romanblack.VideoPlugin.R;
import com.ibuildapp.romanblack.VideoPlugin.api.youtubeapi.YouTubeApi;
import com.ibuildapp.romanblack.VideoPlugin.api.youtubeapi.YouTubeUtils;
import com.ibuildapp.romanblack.VideoPlugin.model.VideoItem;
import com.ibuildapp.romanblack.VideoPlugin.utils.VideoPluginConstants;

public class YouTubeFragment extends Fragment{

    private VideoItem currentItem;
    private YouTubePlayerSupportFragment fragment;
    private YouTubePlayer player;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.video_plugin_details_youtube, container, false);

        Bundle args = getArguments();
        currentItem = (VideoItem) args.getSerializable(VideoPluginConstants.ITEM);

        initData();
        return rootView;
    }

    private void initData() {
        fragment = YouTubePlayerSupportFragment.newInstance();
        initialize();

        android.support.v4.app.FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id. video_plugin_details_youtube_container, fragment);
        transaction.commit();
    }

    public void initialize() {
        fragment.initialize(YouTubeApi.YOUTUBE_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, final YouTubePlayer youTubePlayer, boolean b) {
                if (!b) {
                    player = youTubePlayer;
                    youTubePlayer.cueVideo(YouTubeUtils.getVideoId(currentItem.getUrl()));

                }
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                System.out.print("asd");
            }
        });
    }


    public void goToPortrait() {
        player.setFullscreen(false);
    }
}
