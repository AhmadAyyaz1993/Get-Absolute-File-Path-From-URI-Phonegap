package com.phonegap.plugin.GetActualFilePath;

import org.json.JSONArray;
import org.json.JSONException;

import android.os.Environment;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import android.content.Intent;
import android.util.Log;
import java.util.Formatter;
import java.text.DateFormat;
import android.provider.Settings;
import java.util.Currency;
import java.util.Locale;
import java.text.NumberFormat;
import android.net.Uri.Builder;
import org.json.JSONObject;
import org.json.JSONArray;
import android.content.Context;
import java.text.SimpleDateFormat;
import java.text.Format;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import android.annotation.SuppressLint; 
import android.content.ContentUris; 
import android.content.Context; 
import android.database.Cursor; 
import android.net.Uri; 
import android.os.Build; 
import android.os.Environment; 
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.*;
import java.net.URISyntaxException;
import com.red_folder.phonegap.plugin.backgroundservice.sample.DatabasePoolManager;

public class GetActualFilePathPlugin extends CordovaPlugin {
 
	private static final String TAG = "GetActualFilePathPlugin";
	
	private StringWriter logStringWriter = null;
	private PrintWriter logPrintWriter = null;
	private Context context = null;
	private String fileUri = "";

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException 
	{
		 Log.d(TAG, "Inside of GetActualFilePathPlugin");
			if ("getActualFilePath".equals(action)) 
			{
			  try{
				logStringWriter = new StringWriter();
				logPrintWriter = new PrintWriter(logStringWriter);
				JSONObject userData = args.getJSONObject(0);
				fileUri = userData.getString("Uri");
				context = this.cordova.getActivity();
				String absolutePath = this.getActualFilePath(context);
				callbackContext.success(absolutePath);
				}catch(URISyntaxException e){
				}
				return true;
			}
			Log.d(TAG, "Called invalid action: "+action);
			return false;  
	}
	 
	private String getActualFilePath(Context context) throws URISyntaxException
	{
		Uri uri = Uri.parse(fileUri);
		final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{ split[1] };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { MediaStore.Images.Media.DATA };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
		return null;
	}
	 /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
	 private void LogException(Exception ex)
	 {
		if(ex != null)
		{
			try
			{
				ex.printStackTrace(logPrintWriter);
				String data = logStringWriter.toString();
			
				AddErrorLogEntry(data);
			}
			finally 
			{
				Log.d(TAG, "Logged exception in file");
			}
		}
	 }
	 
	private void AddErrorLogEntry(String exceptionDetails)
	{
		try
		{
			ContentValues errorLogRowValues = GetErrorLogContent(exceptionDetails);																	
			long errorlogResult = DatabasePoolManager.getExtendedInstance().insert("ErrorLog", null, errorLogRowValues );
			Log.d(TAG, "Response of the adding an entry for error log is " + errorlogResult);			
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
		}
	}

	private ContentValues GetErrorLogContent (String exceptionDetail)
	{
		ContentValues errorLogContent = new ContentValues();
		try 
		{
			
			errorLogContent.put("Method", "Nativee Service");
			errorLogContent.put("Parameters", "");
			errorLogContent.put("Exception", exceptionDetail);
			errorLogContent.put("IsActive", "1");
		} 
		finally 
		{
			
		}      
		return errorLogContent;
	}

	private Boolean convertToBoolean(String value)
	{
		Boolean result = false;
		String check = value.toString();

		if(check.equals("false") || Integer.parseInt(check) == 0)
		{
			result = false;
		}
		else if(check.equals("true") || Integer.parseInt(check) == 1)
		{
			result = true;
		}

		return Boolean.valueOf(result);
	}

	public String nullScrubber(String value)
	{
		String result = "";
		if (value != null && !value.equals("null")) 
		{
			result = value;
		}
		return result;
	}

 }
