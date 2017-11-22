package com.atg.onecontrolv3.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.activities.MainActivity;
import com.atg.onecontrolv3.preferances.OneControlPreferences;

/**
 * Created by Bharath on 7/27/2015
 */
public class TabFragment extends Fragment {

    private static final String TAG = "TabFragment";
    //public static TabLayout tabLayout;
    public static ViewPager viewPager;
    public static int int_items = 5;
    public int curPos = 0;
    //public boolean isMaster;
    Bundle bundle;
    // String userMode;
    OneControlPreferences mPreferences;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /**
         *Inflate tab_layout and setup Views.
         */
        View x = inflater.inflate(R.layout.tab_layout, container, false);
        viewPager = (ViewPager) x.findViewById(R.id.viewpager);
        viewPager.setAdapter(new MyAdapter(getChildFragmentManager()));
        viewPager.setCurrentItem(curPos);
        mPreferences = new OneControlPreferences(getActivity());
        //userMode = mPreferences.getUserMode();

       /* viewPager.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View arg0, MotionEvent arg1) {
                return true;
            }
        });*/
        //To start viewpager scrolling (mViewPager.setOnTouchListener(null);)
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                try {
                    ((MainActivity) getContext()).onViewPageChangeListener(position);
                } catch (ClassCastException cce) {
                    Log.e(TAG, "ClassCastException:-:" + cce);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return x;
    }

    public interface PageChangeListener {
        void onViewPageChangeListener(int pos);
    }


    private class MyAdapter extends FragmentPagerAdapter {

        private MyAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Return fragment with respect to Position.
         */

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    //LV_POS = 0;
                    /*DashboardFragment dashboardFragment = new DashboardFragment();
                    dashboardFragment.isMaster = isMaster;
                    isMaster = false;*/
                    //MyPreferences.add(MyPreferences.PrefType.LV_POS, 0, getContext().getApplicationContext());
                    return new DashboardFragment();
                case 1:
                    return new SafetyFragNew();
                case 2:
                    return new ScenesTimerFragment();
                //return new SurveillanceFragment();
                case 3:
                    return new SetTimerFragment();
                case 4:
                    return new SettingFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return int_items;
        }

        /**
         * This method returns the title of the tab according to the position.
         */

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return "Lighting";
                case 1:
                    return "Safety";
                case 2:
                    return "Surveillance";
                case 3:
                    return "timer_home";
            }
            return null;
        }
    }
}
