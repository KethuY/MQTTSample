package com.atg.onecontrolv3.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


import com.atg.onecontrolv3.R;

/**
 * Created by Bharath on 7/29/2015
 */
public class SurveillanceFragment extends Fragment {
    //TextView liveStream, photoVideo;
    ImageButton imageButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.surveillance_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //imageButton = getActivity().findViewById(R.id.imageButton);
        /*Animation animationLeft = AnimationUtils.loadAnimation(getContext(), R.anim.left_to_right);
        Animation animationRight = AnimationUtils.loadAnimation(getContext(), R.anim.right_to_left);
        liveStream = (TextView) getActivity().findViewById(R.id.live_stream);
        photoVideo = (TextView) getActivity().findViewById(R.id.photo_video);*/
       /* liveStream.startAnimation(animationLeft);
        photoVideo.startAnimation(animationRight);*/

       /* imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent launchIntent = getActivity().getPackageManager().getLaunchIntentForPackage("com.ezviz");//com.mcu.iVMS
                if (launchIntent != null) {
                    startActivity(launchIntent);//null pointer check in case package name was not found
                } else {
                    Toast.makeText(getActivity(), "Please install EZVIZ app", Toast.LENGTH_SHORT).show();
                }
            }
        });*/
    }
}
