package com.example.clienteservidorficherosandroid.VistasControladores;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.example.clienteservidorficherosandroid.Modelo.DAOFicheros;
import com.example.clienteservidorficherosandroid.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

public class ActivityDescargar extends AppCompatActivity {

    //Almacena la ruta
    String ruta;

    //Controles
    TextView txvRutaSeleccionada;
    Spinner spArchivos;

    ArrayList<String> arrFicheros;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descargar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Obtenemos los datos del spinner
        arrFicheros = getIntent().getStringArrayListExtra("arrFicheros");

        //Obtenemos el id de donde se guardaran la ruta
        txvRutaSeleccionada = (TextView)findViewById(R.id.txvRutaSeleccionada);
        spArchivos = (Spinner)findViewById(R.id.spArchivos);

        //Obtenemos la ruta de descargas del usuario
        ruta = Environment.getExternalStorageDirectory().getPath() + "/Download";

        //Añadimos la ruta al textview
        txvRutaSeleccionada.setText(ruta);

        //Creamos el adaptador para el spinner
        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arrFicheros);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Añadimos el adaptador
        spArchivos.setAdapter(adaptador);
    }

    //Si el usuario pulsa en el boton descargar
    public void Descargar_OnClick(View v)
    {
       try
       {
           //Enviamos a la primera activity el nombre del archivo y la ruta donde queremos que se descargue
            Intent intent = new Intent();
            intent.putExtra("nombre", spArchivos.getSelectedItem().toString());
            intent.putExtra("ruta",ruta);
            setResult(RESULT_OK,intent);
            finish();
       }
       catch (Exception error)
       {
           Toast.makeText(this, error.getMessage(),Toast.LENGTH_LONG).show();
       }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_descargarsubir, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_salir)
        {
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }
}