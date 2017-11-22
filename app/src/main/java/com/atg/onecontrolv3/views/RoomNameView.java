package com.atg.onecontrolv3.views;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.interfaces.OnItemClickListener2;
import com.atg.onecontrolv3.preferances.MyPreferences;
import com.atg.onecontrolv3.preferances.OneControlPreferences;

import static android.content.Context.VIBRATOR_SERVICE;
import static com.atg.onecontrolv3.helpers.Utils.getDateStr;

/**
 * Created by user on 08-09-2017
 */

public class RoomNameView {
    private String roomName;
    private int roomCount;
    private OnItemClickListener2 mListener;
    private int appPos;
    private Context context;
    private Vibrator vibrator;

    public RoomNameView(Context context, String roomName, int roomCount, OnItemClickListener2 mListener, int appPos) {
        this.context = context;
        this.roomCount = roomCount;
        this.roomName = roomName;
        this.mListener = mListener;
        this.appPos = appPos;
    }

    public View relayButton(Activity activity, String autoRevoke, int roomId) {
        View v = activity.getLayoutInflater().inflate(R.layout.room_name_class_view, null);
        TextView tvRoomName = v.findViewById(R.id.room_name_tv);
        TextView tvRoomCount = v.findViewById(R.id.tv_room_cnt);
        final LinearLayout roomAllLl = v.findViewById(R.id.room_all_ll);
        final TextView autoRevokeTv = v.findViewById(R.id.revoke_tv);
        TextView dateTimeTv = v.findViewById(R.id.date_time_tv);
        LinearLayout lstOpLL = v.findViewById(R.id.lst_po_ll);
        vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
        String roomNameStr = roomName.toUpperCase();
        tvRoomName.setText(roomNameStr);
        tvRoomCount.setText(String.valueOf(roomCount));
        roomAllLl.setTag(appPos);
        autoRevokeTv.setTag(appPos);

        int crntTheme = MyPreferences.getInt(MyPreferences.PrefType.THEME_COLOR, context);
        switch (crntTheme) {
            case 1:
                roomAllLl.setBackgroundColor(context.getResources().getColor(R.color.colorRumLblOne));
                break;
            case 2:
                roomAllLl.setBackgroundColor(context.getResources().getColor(R.color.colorRumLblTwo));
                break;
            case 3:
                roomAllLl.setBackgroundColor(context.getResources().getColor(R.color.colorRumLblThree));
                break;
            case 4:
                roomAllLl.setBackgroundColor(context.getResources().getColor(R.color.colorRumLblFour));
                break;
        }

        OneControlPreferences mPreferences = new OneControlPreferences(context);
        String lstOperated = mPreferences.getLstOperated("QT" + Utils.MAC_ID + roomId);
        if (lstOperated != null && !lstOperated.isEmpty()) {
            lstOpLL.setVisibility(View.VISIBLE);
            if (null != getDateStr(lstOperated) && !getDateStr(lstOperated).isEmpty())
                dateTimeTv.setText(getDateStr(lstOperated));
        } else {
            lstOpLL.setVisibility(View.INVISIBLE);
        }

        if (autoRevoke.equals("1")) {
            autoRevokeTv.setTextColor(ContextCompat.getColor(context, R.color.green));
        } else {
            autoRevokeTv.setTextColor(ContextCompat.getColor(context, R.color.white));
        }

        roomAllLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrator.vibrate(150);
                mListener.onItemClick(roomAllLl);
            }
        });
        autoRevokeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrator.vibrate(150);
                mListener.onItemClick(autoRevokeTv);
            }
        });
        return v;
    }
}
