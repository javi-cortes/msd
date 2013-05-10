/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulacionusuarios;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 *
 * @author rrs160
 */
public class Escritor extends BufferedWriter
{
    Date Date;
    SimpleDateFormat sdf;
    public static String Separador = "_";

    public Escritor(FileWriter F)
    {
        super(F);
        Date = new Date();
        sdf = new SimpleDateFormat("HH:mm:ss");
    }

    private int getRandomTamanyo()
    {
        double size = 40 + Math.random()*(150-40);
        return (int)size;
    }

    //cliente    accion    tamaño    url    tiempo
    void escribirPeticionPagina(String Client, String urlPagina, long tiempoAccion)
    {

        try
        {
           write(Client + Separador + "VisitaPagina" + Separador + "null" + Separador + urlPagina + Separador + tiempoAccion + "\n");
        }
        catch (IOException ioe)
        {
        }
    }

    void escribirPost(String Client, long tiempoAccion)
    {
        try
        {
            this.write(Client + Separador + "EscribePost" + Separador + getRandomTamanyo() + Separador + "null" + Separador + tiempoAccion + "\n");
        }
        catch (IOException ioe)
        {
        }
    }

    void escribirComentario (String Client, String urlPost, long tiempoAccion)
    {
        try
        {
            this.write(Client + Separador + "EscribeComentario" + Separador + getRandomTamanyo() + Separador + urlPost+ Separador + tiempoAccion + "\n");
        }
        catch (IOException ioe)
        {
        }
    }
}