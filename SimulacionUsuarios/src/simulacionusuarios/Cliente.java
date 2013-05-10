/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simulacionusuarios;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import pruebaestadistica.Estadisticas;

/**
 *
 * @author jco322
 */
public class Cliente extends Thread
{
    Escritor LogWriter;
    Semaphore semaforoBloqueante;
    Semaphore semaforoMutex;
    Estadisticas estadisticas;
    int NumActions;

    
    enum EestadoCliente
    { 
        EnPost, EnPagina
    };
    

    //Constructor que se le pasarán todos los parámetros que necesite.
    public Cliente(String nombre) throws IOException
    {
        super(nombre);
        LogWriter = Simulador.LogWriter;
        this.semaforoBloqueante = Simulador.semaforoBloqueante;
        this.semaforoMutex = Simulador.semaforoMutex;
        estadisticas = new Estadisticas();
    }

    
    //Esta función decide qué post de la página principal va a visitar el cliente
    //o si va a ir a la siguiente página principal.
    //Devuelve un valor entre 1 y 11, donde 11 es "siguiente página".
    private int NumPostAVisitar(double decision)
    {
        if (0<=decision && decision<100/11) return 1;
        if (100/11<=decision && decision<2*100/11) return 2;
        if (2*100/11<=decision && decision<3*100/11) return 3;
        if (3*100/11<=decision && decision<4*100/11) return 4;
        if (4*100/11<=decision && decision<5*100/11) return 5;
        if (5*100/11<=decision && decision<6*100/11) return 6;
        if (6*100/11<=decision && decision<7*100/11) return 7;
        if (7*100/11<=decision && decision<8*100/11) return 8;
        if (8*100/11<=decision && decision<9*100/11) return 9;
        if (9*100/11<=decision && decision<10*100/11) return 10;
        return 11;
    }
    
    //Método que dado la página y el número de post obtiene el link
    private String obtenerPost(String paginaActual, int numPost) throws MalformedURLException, IOException
    {
        URL url = new URL(paginaActual);
        URLConnection connection = url.openConnection();

        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        int contadorPost = 1;
        String linea;
        while ((linea = input.readLine()) != null) 
        {
            if (linea.contains("pubdate") )//pubdate => esa linea contiene el link a un post
            {
                if (numPost == contadorPost)
                {
                    String resultados[] = linea.trim().split("\"");
                    for (int i = 0; i < resultados.length; i++) 
                    {
                        return resultados[1];// al hacer el split, por el código fuente de la página, en la pos. 1 está el link al post
                    }
                }
                contadorPost++;
            }
        }
        return "ERROR - No se ha encontrado el post";
    }
    /**
     * En el run se simula el comportamiento del cliente.
     * El comportamiento es el siguiente :
     * 
     *loop
     * En la primera iteracion, usuario accede a la página principal ( la más popular ).
     * Decide entre leer o escribir (hará un total de 5 acciones, aplicando una desviación):
     *      Si lee 99,15%:
     *          -El usuario puede visitar :
     *              -Post en concreto de forma equiprobable (10post = 10%) - REQUERIRÁ PARSER
     *              -Página siguiente
     *      Si escribe 0.85%:
     *          -Dependiendo de si está en una página principal o en un post en concreto escribirá :
     *              -Un post
     *              -Un comentario
     * end loop;
     */
    @Override
    public void run()
    {
        //variables locales
        String PaginaActual;//En la iteración 'i', sabremos dónde se encontraba en la iteración 'i-1'.
        String PostActual;
        String basePagina = "http://localhost/wordpress/?paged=";
        
        //variables de probabilidad
        double Decision;
        int PostAVisitar;
        long TimeSleep; //tiempo del sleep entre peticiones (en ms).
        EestadoCliente estadoCliente = null;
        
        //Decidimos cuántas acciones realizará el cliente
        NumActions = 5;

        //para saber el nº de la página 
        int numPaginaActual = 1;
        
        PaginaActual = basePagina + numPaginaActual;
        PostActual = "";
        long tiempoAccion = 0;

        //Para cada una de las peticiones que deberá hacer el cliente:
        for (int i = 0; i < NumActions; i++)
        {
            if (Simulador.finLog())
            {
                break;
            }
            //hacemos un wait para ser los únicos que escribimos en el fichero
            try
            {
                //Protocolo Entrada RC
                tiempoAccion = System.currentTimeMillis() / 100;
                semaforoMutex.acquire();
            }
            catch (InterruptedException ex)
            {
                Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
            }
            //REGIÓN CRÍTICA
            try
            {
                if (i==0)
                {
                    //Visita la página principal
                    LogWriter.escribirPeticionPagina(getName(), PaginaActual, tiempoAccion);
                    estadoCliente = EestadoCliente.EnPagina;
                }
                else
                {
                    //Decide si va a leer o a escribir.
                    Decision = estadisticas.Uniforme(0, 100);
                    if (Decision<99.15) //decide leer
                    {
                        //decide si entra en cualquiera de los 10 posts o si va a la siguiente pagina principal
                        Decision = estadisticas.Uniforme(0, 100);
                        PostAVisitar = NumPostAVisitar(Decision);
                        if (PostAVisitar<11)//entra en un post
                        {
                            try 
                            {
                                //Parsear para sacar el post nº = PostAVisitar
                                PostActual = obtenerPost(PaginaActual, PostAVisitar);
                            } 
                            catch (MalformedURLException ex) 
                            {
                                Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
                            } 
                            catch (IOException ex) 
                            {
                                Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            
                            LogWriter.escribirPeticionPagina(getName(), PostActual, tiempoAccion);
                            estadoCliente = EestadoCliente.EnPost;
                        }
                        else //siguiente pagina
                        {
                            //TODO sumar a la siguiente página
                            numPaginaActual++;
                            PaginaActual =  basePagina + numPaginaActual;
                            LogWriter.escribirPeticionPagina(getName(), PaginaActual, tiempoAccion);
                            estadoCliente = EestadoCliente.EnPagina;
                        }

                    }
                    else //decide escribir
                    {
                        if (estadoCliente == EestadoCliente.EnPagina) //estoy en la principal, por lo que si publico algo, es un post
                        {
                            LogWriter.escribirPost(getName(), tiempoAccion);
                        }
                        else //estoy en un post, por lo que si publico algo, es un comentario
                        {
                            LogWriter.escribirComentario(getName(), PostActual, tiempoAccion);
                        }
                    }
                }
                TimeSleep = (long) estadisticas.LogNormal(1.789, 2.366) ; 
                TimeSleep = (long)  Simulador.modificarTiempo(TimeSleep, true)% 5251;//% 5250 provisional para acortar las simulaciones
                System.out.println("TimeSleep = " + TimeSleep);
                //Protocolo Salida RC
                semaforoMutex.release();
                Thread.sleep(TimeSleep);
                //FIN REGIÓN CRÍTICA
            }
            catch (InterruptedException ex)
            {
                Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        semaforoBloqueante.release();
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<Cliente "+getName() + "muere.");
    }



}

//        System.out.println("Soy " + getName()+ "y voy a hacer una petición");
//        try
//        {
//            Request.RealizarPeticion("http://www.ascodevida.com");
//        }
//        catch (MalformedURLException ex)
//        {
//            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        catch (IOException ex)
//        {
//            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
//        }