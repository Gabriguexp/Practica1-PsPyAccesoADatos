package com.example.practica1_pspyaccesoadatos;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.example.practica1_pspyaccesoadatos.settings.SettingsActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


    /*

Primera práctica de acceso a datos:

Se debe implementar una aplicación que guarde en un archivo el historial de llamadas entrantes.
Es decir, una vez instalada la aplicación se deben registrar la fecha, la hora, el número y
el nombre de la persona que está llamando al dispositivo móvil.

Para poder registrar las llamadas entrantes será necesario crear un BroadcastReceiver.
Para que el BroadcastReceiver pueda funcionar será necesario obtener el permiso READ_PHONE_STATE.
Para poder obtener el número de la llamada entrante también será necesario disponer del permiso READ_CALL_LOG.

Una vez que se tenga el número de la llamada entrante, habrá que buscar en la agenda si el
número corresponde a alguno de los contactos. Si el número corresponde a alguno de los contactos
deberemos recuperar su nombre. Para poder acceder a la agenda se deberá disponer del permiso android.permission.READ_CONTACTS.

La aplicación deberá guardar dos archivos en dos localizaciones diferentes:

a) en la memoria interna (getFilesDir()) se guardará el archivo historial.csv en el que por cada llamada
se almacena una línea con el siguiente formato: año, mes, día, hora, minutos, segundos,
número entrante, nombre del contacto o "desconocido"; se utiliza el punto y coma como separador,
las llamadas se guardan en el orden en el que se van registrando:

2020; 10; 12; 15; 35; 47; 958123456; Juan López
2020; 10; 19; 09; 08; 02; 670123456; desconocido
2020; 10; 19; 15; 15; 27; 958123456; Juan López

b) en la memoria externa (getExternalFilesDir(null)) se guardará el archivo llamadas.csv en el que
se guarda el nombre del contacto; año, mes, día, hora, minutos y segundos; de nuevo se usa el punto
y coma como separador, los registros se guardan ordenados según el orden alfabético del nombre del
contacto y por fecha y hora:

desconoddo; 2020; 10; 19; 09; 08; 02; 670123456
Juan López; 2020; 10; 12; 15; 35; 47; 958123456
Juan López; 2020; 10; 19; 15; 15; 27; 958123456

El proceso mediante el cual se obtiene el nombre del contacto y se guarda posteriormente la llamada
en los dos archivos se debe ejecutar en una hebra, ya que su tiempo de ejecución es largo y podría
provocar el bloqueo del dispositivo móvil (ANR, application not responding).

La aplicación nos debe permitir consultar cualquiera de ambos archivos. De hecho, en los ajustes
debemos poder definir cuál de los dos listados es el que se va a mostrar de forma predeterminada al abrir la aplicación.*/

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener  {
     private static final int PERMISSIONS = 0;
     private static final String TAG = MainActivity.class.getName() + "xyzyx";
     private TextView tv1;

     private SharedPreferences sharedPreferences;
     private SharedPreferences.OnSharedPreferenceChangeListener listener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

    }

    private void init() {
        tv1 =findViewById(R.id.tv1);
        askPermissions();
        listener = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
        String option = sharedPreferences.getString("selectedFile","historial");
        switch (option){
            case "historial":
                readhistorial();
                break;
            case "llamadas":
                readllamadas();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    private void askPermissions() {
        // READ_PHONE_STATE READ_CALL_LOG

        // IDEA DEL ARRAYLIST ESTA DPM.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int contactPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
            int phoneStatePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
            int callLogPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG);
            ArrayList<String> permissions = new ArrayList<>();
            if (contactPermission != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.READ_CONTACTS);
            }
            if(phoneStatePermission != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.READ_PHONE_STATE);
            }
            if(callLogPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_CALL_LOG);
            }
            if(permissions.size() >0){
                if(shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) ||
                        shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE) ||
                        shouldShowRequestPermissionRationale(Manifest.permission.READ_CALL_LOG)){

                    detailedExplanation();
                } else{

                    requestPermissions(permissions.toArray(new String[permissions.size()]),PERMISSIONS);
                }
            }
        }
    }


    @SuppressLint("NewApi")
    private void detailedExplanation() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.permissions);
        builder.setMessage(R.string.permissionsmsg);
        builder.setPositiveButton(R.string.acept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG },PERMISSIONS);
            }
        });
        builder.setNegativeButton(R.string.deny, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for(int result: grantResults){
          if (result == PackageManager.PERMISSION_GRANTED){

          } else{
              askPermissions();
              return;
          }
        }

    }



    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String option = sharedPreferences.getString("selectedFile","historial");

        switch (option){
            case "historial":
                readhistorial();
                break;
            case "llamadas":
                readllamadas();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        switch (id){
            case R.id.settingsMi:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
        }
        return true;
    }


    private void viewSettingsActivity(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void readllamadas(){
        File f = new File(getExternalFilesDir(null),"llamadas.csv");
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(f));
            String linea;
            StringBuilder texto = new StringBuilder();
            while ((linea = bufferedReader.readLine())!=null){
                texto.append(linea);
                texto.append("\n");
            }
            tv1.setText(texto);
        } catch(FileNotFoundException ex){
            tv1.setText(R.string.empty);
        } catch (IOException ex){
        }
    }

    private void readhistorial(){
        File f = new File(getFilesDir(),"historial.csv");
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(f));
            String linea;
            StringBuilder texto = new StringBuilder();
            while ((linea = bufferedReader.readLine())!=null){
                texto.append(linea);
                texto.append("\n");
            }
            tv1.setText(texto);
        } catch(FileNotFoundException ex){
            tv1.setText(R.string.empty);
        } catch (IOException ex){
        }
    }




}