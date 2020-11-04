package com.example.practica1_pspyaccesoadatos;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;

public class CallReceiver extends BroadcastReceiver{

    private static final String TAG = "xyzyx";


    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);


            ContentResolver contentResolver = context.getContentResolver();
            TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            telephony.listen(new PhoneStateListener(){
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onCallStateChanged(int state, String phoneNumber) {
                    super.onCallStateChanged(state, phoneNumber);
                    if(phoneNumber.equalsIgnoreCase("")) return;
                    int incomingCallNumber = Integer.parseInt(phoneNumber);
                    String contact = getContacts(contentResolver,incomingCallNumber);
                    Date time = GregorianCalendar.getInstance().getTime();
                    Call call = new Call(contact, incomingCallNumber, LocalDateTime.now());

                    Thread thread = new Thread(){
                        @Override
                        public void run() {
                            super.run();
                            saveHistorial(call, context);
                        }
                    };
                    thread.start();

                }
            },PhoneStateListener.LISTEN_CALL_STATE);
    }

    public String getContacts(ContentResolver contentResolver,int incomingCallNumber){

        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,null,null,null,null);
        while(cursor.moveToNext()){
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
            int numbers = Integer.parseInt(phone);
            Cursor phoneCursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                    new String[]{id},
                    null);
            while(phoneCursor.moveToNext()){
                int phoneNumber = Integer.parseInt(phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                if(phoneNumber== incomingCallNumber){
                    phoneCursor.close();
                    cursor.close();
                    return name;
                }
            }
            phoneCursor.close();
        }
        cursor.close();
        return "desconocido";
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveLlamada(ArrayList<Call> c, Context context){
        try{
            File f = new File(context.getExternalFilesDir(null),"llamadas.csv");
            FileWriter fw = new FileWriter(f, false);
            for(Call call : c) {
                fw.write(call.callToCsv2() + "\n");
            }
            fw.flush();
            fw.close();
        } catch(IOException ex){
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveHistorial(Call c, Context context){
        try{
            File f = new File(context.getFilesDir(),"historial.csv");
            FileWriter fw = new FileWriter(f, true);
            fw.write(c.callToCsv1()+"\n");
            fw.flush();
            fw.close();
        } catch(IOException ex){
        }
        ArrayList <Call> calls = new ArrayList<>();

        try{
            File f = new File(context.getFilesDir(),"historial.csv");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(f));
            String linea;

            while ((linea = bufferedReader.readLine())!=null){

                Call call = Call.fromCsvToCall(linea);
                calls.add(call);
            }
            calls.sort(new Comparator<Call>() {
                @Override
                public int compare(Call o1, Call o2) {
                    return o1.getContactName().compareTo(o2.ContactName);
                }
            });

            Thread thread = new Thread(){
                @Override
                public void run() {
                    super.run();
                    saveLlamada(calls, context);
                }
            };
             thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

