package com.atg.onecontrolv3.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;

import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.adapters.SelectImageAdapter;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.interfaces.OnItemClickListener;

public class GridImageViewActivity extends BaseActivity implements OnItemClickListener {
    public static final String TAG = "GridImageViewActivity";
    GridView mGridView;
    Toolbar mToolbar;
    OnItemClickListener mListener;
    private int mIsFrom;
    private String locImg = "";
    private int[] homePageItemsImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_image_view);
        mGridView = (GridView) findViewById(R.id.grid_view);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("One Control");
        setSupportActionBar(mToolbar);

        mListener = this;

        mIsFrom = getIntent().getIntExtra("IsFrom", 0);

        if (mIsFrom == 2) {
            locImg = getIntent().getStringExtra("img");
        }

        if (locImg != null && !locImg.isEmpty()) {

        }

        try {
            homePageItemsImages = new int[]{R.drawable.scene_one, R.drawable.scene_two, R.drawable.scene_three, R.drawable.scene_four, R.drawable.scene_five, R.drawable.scene_six, R.drawable.scene_seven};
            SelectImageAdapter selectImageAdtr = new SelectImageAdapter(GridImageViewActivity.this, homePageItemsImages, mListener);
            mGridView.setAdapter(selectImageAdtr);
        } catch (
                Exception e) {
            Log.e(TAG, "Exception");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, int position) {
        switch (view.getId()) {
            case R.id.grid_image:
                selectedImg(position);
                if (null != Utils.getScenesObj) {
                    Utils.getScenesObj.setImage(Utils.image);
                }
                startActivity(new Intent(this, SceneEditActivity.class).putExtra("IsFrom", 2));
                break;
        }
    }

    private void selectedImg(int position) {
        switch (position) {
            case 0:
                Utils.image = "Scene_1";
                break;
            case 1:
                Utils.image = "Scene_2";
                break;
            case 2:
                Utils.image = "Scene_3";
                break;
            case 3:
                Utils.image = "Scene_4";
                break;
            case 4:
                Utils.image = "Scene_5";
                break;
            case 5:
                Utils.image = "Scene_6";
                break;
            case 6:
                Utils.image = "Scene_7";
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, SceneEditActivity.class).putExtra("IsFrom", 2));
    }
}
