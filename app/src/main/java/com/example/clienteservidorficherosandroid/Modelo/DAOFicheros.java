package com.example.clienteservidorficherosandroid.Modelo;

import android.net.Uri;
import android.os.Environment;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;

public class DAOFicheros implements Serializable
{
    //Almacena el cliente y el puerto del servidor al que se conectar
    private final String HOST = "192.168.1.36";  // La ip debe corresponder a la ip de ethernet del equipo fisico
    private final int Puerto=2000;

    //Socket del cliente
    private Socket sCliente = null;

    //Flujos de salida
    private OutputStream os = null;
    private DataOutputStream flujoSalida = null;

    //Flujos de entrada
    private InputStream is = null;
    private DataInputStream flujoEntrada = null;
    private ObjectInputStream entradaObjeto = null;

    //Establece la conexión con el servidor
    public void EstablecerConexion() throws Exception
    {
        try
        {
            //Creamos el inedAddres con la conexion
            SocketAddress sockAddress = new InetSocketAddress(HOST, Puerto);

            //Creamos el socket
            sCliente = new Socket();

            //Intentamos conectar y asignamos el tiempo durante cuanto lo intentamos
            sCliente.connect(sockAddress,2000);

            //Creamos los flujos de salida
            os = sCliente.getOutputStream();
            flujoSalida = new DataOutputStream(sCliente.getOutputStream());

            //Creamos los flujos de entrada
            is = sCliente.getInputStream();
            flujoEntrada = new DataInputStream(sCliente.getInputStream());
            entradaObjeto = new ObjectInputStream(sCliente.getInputStream());

            //Mostramos el mensaje de bienvenida unicamnete por el terminal
            System.out.println(flujoEntrada.readUTF());
        }
        catch (Exception error)
        {
            throw new Exception(error.getMessage());
        }
    }

    //Metodo que realiza la eleccion del usuario
    public String[] ObtenerFicheros() throws Exception
    {
        String[] arrArchivos = null;
        try
        {
            //Obtenemos la lista de opciones pero unicamnete la mostramos por el cmd ya que no se observará
            System.out.println(flujoEntrada.readUTF());

            //Enviamos un uno al servidor la opción 1
            flujoSalida.writeUTF("1");

            //Leemos el booleano aunque no lo mostramos
            flujoEntrada.readBoolean();

            //Obtenemos la lista de ficheros y la devolvemos
            arrArchivos = (String[]) entradaObjeto.readObject();

            //Si no hay archivos que mostrar
            if (arrArchivos.length == 0)
            {
                throw new Exception("El servidor no contiene ningun fichero");
            }
        }
        catch (Exception error)
        {
           throw new Exception(error.getMessage());
        }

        //Devolvemos la lista
        return arrArchivos;
    }

    //Descargar fichero
    public void DescargarFichero(String nombreFichero, String ruta) throws Exception
    {
        //Almacenará la cadena de bytes
        byte[] arrFichero = null;
        //Flujos para almacenar el fichero
        BufferedOutputStream bos = null;

        //Valor que se lee
        int valor = 0;

        try
        {
            //Obtenemos la lista de opciones pero unicamnete la mostramos por el cmd ya que no se observará
            System.out.println(flujoEntrada.readUTF());

            //Enviamos un uno al servidor la opción 2
            flujoSalida.writeUTF("2");

            //Leemos el booleano aunque no lo mostramos
            flujoEntrada.readBoolean();

            //Enviamos el nombre del archivo
            flujoSalida.writeUTF(nombreFichero);

            //Leemos el booleano pero no lo mostramos
            flujoEntrada.readBoolean();

            //Creamos el buffer con el tamaño del array
            arrFichero = new byte[(int) flujoEntrada.readLong()];

            //Obtenemos el fichero recibido
            bos = new BufferedOutputStream(new FileOutputStream(ruta + "/" + nombreFichero));

            //Leemos los datos
            while(valor < arrFichero.length)
            {
                arrFichero[valor] = flujoEntrada.readByte();
                valor++;
            }

            //Grabamos los byte en el fichero
            bos.write(arrFichero);
        }
        catch (Exception error)
        {
            throw new Exception(error.getMessage());
        }
    }

    //Subir archivo
    public void SubirFichero(String fichero) throws Exception
    {
        //Almacena el fichero
        File ficheroUsuario;

        //Almacenará la cadena de bytes
        byte[] arrFichero = null;

        //Flujos para almacenar el fichero
        BufferedInputStream bis = null;

        //Almacena los intentos a añadir nombre
        int veces = 3;

        //Almacena el nombre del fichero
        String nombreFichero = null;
        String [] arrNombre = null;
        try
        {
            //Obtenemos la lista de opciones pero unicamnete la mostramos por el cmd ya que no se observará
            System.out.println(flujoEntrada.readUTF());

            //Enviamos un uno al servidor la opción 3
            flujoSalida.writeUTF("3");

            //Leemos el booleano aunque no lo mostramos
            flujoEntrada.readBoolean();

            //Obtenemos el nombre del fichero
            arrNombre = fichero.split("/");
            nombreFichero = arrNombre[arrNombre.length - 1];

            //Enviamos el nombre del archivo
            flujoSalida.writeUTF(nombreFichero);

            //Si esa true dividimos el nombre del archivo y le añadimos al final un numero aleatorio
            while(flujoEntrada.readBoolean() && veces > 0)
            {
                //Separamos la terminación
                String[] arrPartesNombre = nombreFichero.split("\\.");

                //Le añadimos un numero aleatorio
                String nombreNuevo = arrPartesNombre[0] + new Random().nextInt(500) + "." + arrPartesNombre[1];

                //Lo enviamos
                flujoSalida.writeUTF(nombreNuevo);
            }

            //Si no se agotaron las veces
            if (veces > 0)
            {
                //Hacemos que la ruta sea valida
                fichero = this.DevolverRutaValida(fichero);

                //Obtenemos el fichero
                ficheroUsuario = new File(fichero);

                //Enviamos el tamaño del fichero
                flujoSalida.writeLong(ficheroUsuario.length());

                //Creamos el array del buffered que se enviará
                arrFichero = new byte[(int) ficheroUsuario.length()];

                //Añadimos a los flujos que leen el fichero los datos
                bis = new BufferedInputStream(new FileInputStream(ficheroUsuario));

                //Leemos los datos en el buffer
                bis.read(arrFichero, 0, arrFichero.length);

                //Enviamos el fichero
                flujoSalida.write(arrFichero, 0, arrFichero.length);

                //Limpiamos el flujo
                flujoSalida.flush();
            }
            else throw new Exception("Fichero no valido");
        }
        catch (Exception error)
        {
            throw new Exception(error.getMessage());
        }
    }

    //Método que devuleve un string con una ruta valida
    private String DevolverRutaValida(String ruta)
    {
        String rutaValida = null;
        String[] rutaVal = null;

        try
        {
            rutaVal = ruta.split(":");
            rutaValida = Environment.getExternalStorageDirectory().getPath() + "/" + rutaVal[1];
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return rutaValida;
    }

    //Metodo que cierra la conexion
    public void Salir()
    {
        try
        {
            //Obtenemos la lista de opciones pero unicamnete la mostramos por el cmd ya que no se observará
            System.out.println(flujoEntrada.readUTF());

            //Enviamos un uno al servidor la opción 3
            flujoSalida.writeUTF("4");

            //Leemos el booleano aunque no lo mostramos
            flujoEntrada.readBoolean();
        }
        catch (Exception error)
        {
            error.printStackTrace();
        }
    }
}


