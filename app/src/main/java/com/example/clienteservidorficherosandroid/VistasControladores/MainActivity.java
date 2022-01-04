package com.example.clienteservidorficherosandroid.VistasControladores;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.os.StrictMode;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.clienteservidorficherosandroid.Modelo.DAOFicheros;
import com.example.clienteservidorficherosandroid.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity
{
    //Almacnea los permisos
    int PERMISOS_LECTURA;
    int PERMISOS_ESCRITURA;

    //Objeto que realiza las conexiones con la base de datos
    public DAOFicheros objDAOFicheros = new DAOFicheros();

    //Controles
    ListView lsvArchivos;
    Menu miMenu;

    //Adaptador
    ArrayAdapter<String> adaptador = null;

    //Array el nombre de los archivos
    ArrayList<String> arrArchivos;

    Boolean conexionActiva = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Pedimos los permisos
        this.PedirPermisos();

        //----------------------------------------------------------------------------------------------------
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //----------------------------------------------------------------------------------------------------

        //Obtenemos la referencia del listview
        lsvArchivos = (ListView)findViewById(R.id.lsvArchivos);

        //Creamos el arraylist
        arrArchivos = new ArrayList<>();

        //Creamos el adaptador
        adaptador = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrArchivos);

        //Añadimos el adaptador
        lsvArchivos.setAdapter(adaptador);

        //Establecemos la conexion con el servidor y obtenemos los ficheros
        this.EstablecerConexion();

        //Comprobamos si la conexion esta activa y si es asi obtenemos los archivos
        if (conexionActiva)
            this.ObtenerFicheros();
    }
    //Pide los permisos
    private void PedirPermisos()
    {
        final String[] PERMISOS_A_PEDIR = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //Permiso de lectura
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, PERMISOS_A_PEDIR, PERMISOS_LECTURA);
        }
        //Permiso de escritura
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, PERMISOS_A_PEDIR, PERMISOS_ESCRITURA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Compruebo que son los permisos que quiero.
        if (requestCode == PERMISOS_ESCRITURA || requestCode == PERMISOS_LECTURA) {
            //Si no da los permisos, cierro la aplicación.
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Necesitas conceder los permisos para usar la aplicación.",
                        Toast.LENGTH_LONG).show();
                this.finish();
            }
        }
    }

    //Método que inicia la conexión
    private void EstablecerConexion()
    {
        try
        {
            //Establecemos la conexion
            objDAOFicheros.EstablecerConexion();

            //Asignamos a la variable que la conexion esta activa
            conexionActiva = true;
        }
        catch (Exception error)
        {
            Toast.makeText(this, "No se ha podido establece conexión con el Servidor" ,Toast.LENGTH_LONG).show();

            //Asignamos que la conexion no esta activa
            conexionActiva = false;
        }
    }

    //Método que obtiene los ficheros
    private void ObtenerFicheros()
    {
        //Almacena los ficheros del servidor
        ArrayList<String> arrFicherosNuevos = null;

        try
        {
            //Obtenemos la lista de fichero
            arrFicherosNuevos = new ArrayList<String>(Arrays.asList(objDAOFicheros.ObtenerFicheros()));

            //Limpiamos el arraylist conectado al adaptador
            arrArchivos.clear();

            //Añadimos los datos del array al arraylist
            arrArchivos.addAll(arrFicherosNuevos);

            //Actualizamos el adaptador
            adaptador.notifyDataSetChanged();
        }
        catch (Exception error)
        {
            Toast.makeText(this, error.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        miMenu = menu;
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
            objDAOFicheros.Salir();
            this.finish();
        }

        //Si se pulsa en la opcio accserver
        if (id == R.id.action_AccionesServidor)
        {
            //Si la conexión no esta activa
            if (!conexionActiva)
            {
                miMenu.findItem(R.id.action_DescargarFichero).setVisible(false);
                miMenu.findItem(R.id.action_SubirFichero).setVisible(false);
                miMenu.findItem(R.id.action_ActualizarArchivos).setVisible(false);
            }
            else // Si esta activa
            {
                miMenu.findItem(R.id.action_SubirFichero).setVisible(true);
                miMenu.findItem(R.id.action_ActualizarArchivos).setVisible(true);

                //Comprobamos si el array esta vacio, si es así ocultamos descargar
                if (arrArchivos.isEmpty())
                    miMenu.findItem(R.id.action_DescargarFichero).setVisible(false);
                else
                    miMenu.findItem(R.id.action_DescargarFichero).setVisible(true);
            }
        }

        //Si se pulsa en reconectar
        if (id == R.id.action_reconectar)
        {
            this.EstablecerConexion();

            //Comprobamos si la conexion esta activa y si es asi obtenemos los archivos
            if (conexionActiva)
                this.ObtenerFicheros();
        }

        //Si se pulsa descargar
        if (id == R.id.action_DescargarFichero)
        {
            //Actualizamos los ficheros
            this.ObtenerFicheros();

            //Llamamos a la activity
            Intent intent = new Intent(this,ActivityDescargar.class);
            intent.putStringArrayListExtra("arrFicheros",arrArchivos);
            startActivityForResult(intent, 11);

        }

        //Si se quiere actualizar la lista de archivos
        if (id == R.id.action_ActualizarArchivos)
        {
            this.ObtenerFicheros();
        }

        //Si queremos subir un fichero
        if (id == R.id.action_SubirFichero)
        {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT );
            intent.setType("*/*");
            startActivityForResult(Intent.createChooser(intent,"Selecciona un fichero.."), 12);
        }

        return super.onOptionsItemSelected(item);
    }

    //Espera los resultados de las activities
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 11) //Si la activity es descargar
        {
            //Descargamos el archivo
            try
            {
                objDAOFicheros.DescargarFichero(data.getStringExtra("nombre"),data.getStringExtra("ruta"));
            }
            catch (Exception e)
            {
                Toast.makeText(this, e.getMessage(),Toast.LENGTH_LONG).show();
            }
            Toast.makeText(this, "Se ha descargado con exito",Toast.LENGTH_LONG).show();
        }

        if (requestCode == 12) //Si el intent es seleccionar un archvio
        {
            try
            {
                //Si selecciono algun archivo
                if(data != null)
                {
                    //subimos el fichero
                    objDAOFicheros.SubirFichero(data.getData().getPath());
                    this.ObtenerFicheros();
                }
            }
            catch (Exception error)
            {
                Toast.makeText(this, error.getMessage(),Toast.LENGTH_LONG).show();
            }
            Toast.makeText(this, "Se ha subido con exito",Toast.LENGTH_LONG).show();
        }

    }
}