package com.atg.onecontrolv3.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.atg.onecontrolv3.models.IRAppliancesModel;
import com.atg.onecontrolv3.models.IRSendKeysModel;
import com.atg.onecontrolv3.helpers.Utils;

import java.util.ArrayList;

/**
 * Created by Bharath on 10-Jul-17
 */

public class DatabaseHelperForDVD extends SQLiteOpenHelper {
    public static final String ROOM_ID = "room_id";     //Room id
    public static final String APPLIANCE_POS = "appliance_pos"; //Appliance positon
    public static final String APPLIANCE_TYPE = "appliance_type";   //Appliance Type
    //Column names
    private static final String ROW_ID = "row_id";
    private static final String MAC_ID = "mac_id";
    private static final String CHIP_ID = "chip_id";     //Room id
    private static final String Z_ID = "zmote_id";
    private static final String Z_SECRET = "zmote_secret";
    private static final String KEY_NAME = "key_name";   //key board key
    private static final String KEY_VALUE = "key_value"; //value
    private static final int DB_VERSION = 2;
    private static final String TAG = DatabaseHelperForDVD.class.getSimpleName();
    private static String DB_NAME = "OC_IR_BLASTER";
    private static String TABLE_IR_BLASTER = "IR_BLASTER_DVD";
    //    public static final String KEY_STATUS = "key_status"; //value

    public DatabaseHelperForDVD(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create_table_ir_blaster = "CREATE TABLE " + TABLE_IR_BLASTER + "("
                + ROW_ID + " INTEGER PRIMARY KEY,"
                + MAC_ID + " TEXT,"
                + ROOM_ID + " TEXT,"
                + APPLIANCE_POS + " TEXT,"
                + CHIP_ID + " TEXT,"
                + Z_ID + " TEXT,"
                + Z_SECRET + " TEXT,"
                + KEY_NAME + " TEXT,"
                + KEY_VALUE + " BLOB" + ")";
        Log.e(TAG, "create_table_ir_blaster:-:" + create_table_ir_blaster);
        db.execSQL(create_table_ir_blaster);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IR_BLASTER);
        onCreate(db);
    }

    /**
     * Inserting IR Blaster device in the local DB
     *
     * @param model model class
     */
    public long addIRDevice(IRAppliancesModel model) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = 0;
        try {
            ContentValues values = new ContentValues();
            values.put(MAC_ID, Utils.MAC_ID);
            values.put(ROOM_ID, model.getRoomId());
            values.put(APPLIANCE_POS, model.getAppliance_no());
            values.put(CHIP_ID, model.getChipID());
            values.put(Z_ID, model.getId());
            values.put(Z_SECRET, model.getSecretId());
            values.put(KEY_NAME, model.getKey_name());
            values.put(KEY_VALUE, model.getValue());
            id = db.insert(TABLE_IR_BLASTER, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Exception:-:" + e.getMessage());
        }
        db.close();

        return id;
    }

    public byte[] getIRDeviceKeyValue(String chipId, String keyName, String roomId, String applianceNo) {
        Log.e(TAG, "chipId:-:" + chipId + "keyname:-:" + keyName + "roomId:-:" + roomId + "applianceNo" + applianceNo);
        byte[] val = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_IR_BLASTER,
                new String[]{KEY_VALUE},
                MAC_ID + "=? AND "
                        + ROOM_ID + "=? AND "
                        + APPLIANCE_POS + "=? AND "
                        + CHIP_ID + "=? AND "
                        + KEY_NAME + "=?",
                new String[]{Utils.MAC_ID, roomId, applianceNo, chipId, keyName},
                null, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val = cursor.getBlob(0);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return val;
    }

    public ArrayList<IRSendKeysModel> getApplianceIRValues(String chipId, String roomId, String applianceNo) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<IRSendKeysModel> irImportSyncModelArrLst = new ArrayList<>();

        Cursor cursor = db.rawQuery("Select " + KEY_NAME + "," + KEY_VALUE + " from "
                        + TABLE_IR_BLASTER + " WHERE "
                        + MAC_ID + "='" + Utils.MAC_ID + "' AND "
                        + ROOM_ID + "='" + roomId + "' AND "
                        + APPLIANCE_POS + "='" + applianceNo + "' AND "
                        + CHIP_ID + "='" + chipId + "'", null);
        Log.e(TAG, "DB getIR from:-:" + TABLE_IR_BLASTER);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                IRSendKeysModel model = new IRSendKeysModel();
                String keyNameStr = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                byte[] keyValue = cursor.getBlob(cursor.getColumnIndex(KEY_VALUE));
                String keyValueStr = Utils.getStringFromBytes(keyValue);
                model.setKeyName(keyNameStr);
                model.setKeyValue(keyValueStr);
                irImportSyncModelArrLst.add(model);
            }
            cursor.close();
        }

        return irImportSyncModelArrLst;
    }

    public String getUUID(String roomId, String applianceNo) {
        String val = null;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_IR_BLASTER,
                new String[]{CHIP_ID},
                MAC_ID + "=? AND "
                        + ROOM_ID + "=? AND "
                        + APPLIANCE_POS + "=?",
                new String[]{Utils.MAC_ID, roomId, applianceNo},
                null, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val = cursor.getString(0);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return val;
    }

    public ArrayList<String> getIdSecret(String roomId, String applianceNo) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> zmoteIdSecretArrLst = new ArrayList<>();
        Cursor cursor = db.query(TABLE_IR_BLASTER,
                new String[]{Z_ID, Z_SECRET},
                MAC_ID + "=? AND "
                        + ROOM_ID + "=? AND "
                        + APPLIANCE_POS + "=?",
                new String[]{Utils.MAC_ID, roomId, applianceNo},
                null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String zmoteId = cursor.getString(cursor.getColumnIndex(Z_ID));
                String zmoteSecret = cursor.getString(cursor.getColumnIndex(Z_SECRET));
                zmoteIdSecretArrLst.add(zmoteId);
                zmoteIdSecretArrLst.add(zmoteSecret);
            }
            cursor.close();
        }
        return zmoteIdSecretArrLst;
    }

    public long updateIRDeviceKeyValue(String chipId, String roomId, String applPos, String keyName, byte[] keyValue, String userId, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MAC_ID, Utils.MAC_ID);
        values.put(ROOM_ID, roomId);
        values.put(CHIP_ID, chipId);
        values.put(Z_ID, userId);
        values.put(Z_SECRET, password);
        values.put(APPLIANCE_POS, applPos);
        values.put(KEY_NAME, keyName);
        values.put(KEY_VALUE, keyValue);

        long id = db.update(TABLE_IR_BLASTER, values,
                MAC_ID + "=? AND "
                        + ROOM_ID + "=? AND "
                        + CHIP_ID + "=? AND "
                        + APPLIANCE_POS + "=? AND "
                        + KEY_NAME + "=?",
                new String[]{Utils.MAC_ID, roomId, chipId, applPos, keyName});
        db.close();

        return id;
    }

    public boolean isKeyValueExists(String chipId, String keyName, boolean isInitialCheck) {
        boolean isExists = false;
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            if (isInitialCheck) {
                cursor = db.rawQuery("SELECT " + "*" + " FROM " + TABLE_IR_BLASTER
                        + " WHERE " + KEY_VALUE + "!=" + "'' AND " + KEY_NAME + "='" + keyName + "' AND " + CHIP_ID + "='" + chipId + "'", null);
            } else {
                cursor = db.rawQuery("SELECT " + "*" + " FROM " + TABLE_IR_BLASTER
                        + " WHERE " + KEY_VALUE + "=" + "'' AND " + KEY_NAME + "='" + keyName + "' AND " + CHIP_ID + "='" + chipId + "'", null);
            }

            if (cursor != null) {
                isExists = cursor.getCount() > 0;
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isExists;
    }

}
