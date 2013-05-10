/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simulacionusuarios;

import java.io.*;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.Semaphore;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import pruebaestadistica.Estadisticas;

/**
 *
 * @author jco322
 */
public class Simulador 
{
    //public static int numeroClientes = 5;
    public static int tiempoSimulacion;
    public static int tiempoEntreLlegadas;
    static File Fichero;
    public static String NombreFichero;
    public static Escritor LogWriter;
    public static Semaphore semaforoBloqueante;
    public static Semaphore semaforoMutex;
    //BD stuff
    //pool de conexiones
    public static DataSource dataSourcePool;
    
    private static Boolean finLog;

    public Simulador() 
    {
        //inicialización y conexión a la bd
        BasicDataSource basicDataSource = new BasicDataSource();

        basicDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        basicDataSource.setUsername("cliente2");
        //basicDataSource.setPassword("");
        basicDataSource.setUrl("jdbc:mysql://localhost/wp2");

        // Opcional. Sentencia SQL que le puede servir a BasicDataSource
        // para comprobar que la conexion es correcta.
        basicDataSource.setValidationQuery("select 1");

        dataSourcePool = (DataSource) basicDataSource;
        empiezaLog();
    }

    /**
     * Cierra la conexion. Al provenir de BasicDataSource, en realidad no se
     * esta cerrando. La llamada a close() le indica al BasicDataSource que
     * hemos terminado con dicha conexion y que puede asignarsela a otro que la
     * pida.
     *
     * @param conexion
     */
    public static void liberaConexion(Connection conexion) {
        try 
        {
            if (null != conexion) 
            {
                // En realidad no cierra, solo libera la conexion.
                conexion.close();
            }
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
        }
    }
    
    //para finalizar el log
    private static void acabaLog()
    {
        finLog = true;
    }
    
    private static void empiezaLog()
    {
        finLog = false;
    }
    
    public static boolean finLog()
    {
        return finLog;
    }

    public void setNombreFichero(String NombreFichero, boolean manual) 
    {
        if (!manual)
        {
            Date fecha = new Date();
            Simulador.NombreFichero = "Log " + DateFormat.getDateTimeInstance().format(fecha) + ".txt";
            //no deja guardar en windows ficheros con "/" o con ":" así que lo cambiamos por "-" y "."
            Simulador.NombreFichero = Simulador.NombreFichero.replaceAll("-", " ");
            Simulador.NombreFichero = Simulador.NombreFichero.replaceAll(":", ".");
        }
        else
        {
            Simulador.NombreFichero = NombreFichero;
        }
    }

    public static String getNombreFichero() 
    {
        return NombreFichero;
    }
    
    //método para modificar el tiempo globalmente
    //en la creacion del log multiplicamos por 10 para hacerlo más preciso y más rápido
    // y en la simulación el log lo ponemos por 100
    public static float modificarTiempo (float tiempo, boolean creacionLog)
    {
        if (creacionLog)
        {
            return tiempo*100/**10*/;
        }
        else
        {
            return tiempo*1000;
        }
    }
    //Método que recorre el log y genera un hilo por cada petición
    public static void procesarLog() throws MalformedURLException, IOException
    {
        System.out.println("(Simulación clientes) Empieza la simulación de los clientes a partir del log");
        /*Formato de línea del log: cliente    accion    tamaño    url    tiempo
        *
        * S[0] -> ID Cliente.
        * S[1] -> Acción a realizar "VisitaPagina", "EscribePost" o "EscribeComentario".
        * S[2] -> Tamaño ("Null" para VisitaPagina).
        * S[3] -> URL ("Null" para EscribePost).
        * S[4] -> Instante en el que se realiza la acción.
        * 
        */
        String s[];

        //Abrir log
        File archivo = new File (getNombreFichero());
        FileReader fr = new FileReader (archivo);
        BufferedReader Lector = new BufferedReader(fr);

        /*
            * linea.read();
            * mientras !EOF loop
            *      linea.split();
            *      case (Acción)
            *          Visitar página:
            * 
            *          Escribir post:
            * 
            *          Escribir comentario en un post:
            * 
            *      end case;
            *      linea.read();
            * end loop;
            */
        long TiempoDormir;
        long InstanteActual = 0;
        String linea = Lector.readLine();
        boolean primeraLinea = true;
        while (linea != null)
        {
            s = linea.split(Escritor.Separador);
            //Para la primera línea, como todavía no hay ningún valor dentro de InstanteActual, hay que
            //darle el valor del instante 0, el cual es el instante de la primera línea para que empiece enseguida.
            if (primeraLinea)
            {
                InstanteActual = Long.parseLong(s[4]);
                primeraLinea = false;
            }
            
            TiempoDormir = Long.parseLong(s[4])-InstanteActual;
            InstanteActual = Long.parseLong(s[4]);
            
            try
            {
                System.out.println("Sleep = "+modificarTiempo(TiempoDormir, false));
                Thread.sleep( (long)modificarTiempo(TiempoDormir, false));
            }
            catch (InterruptedException e){}


            if ( s[1].equals("VisitaPagina"))
            {
                //Crear un hilo que genere una petición de VisitaPagina(urlPagina);
                new Peticion(Peticion.EAccion.Consultar, s[3],0).start();
                System.out.println("Visito página");
            }
            else if (s[1].equals("EscribePost") )
            {
                //Crear un hilo que genere una petición de EscribirPost(tamaño);
                new Peticion(Peticion.EAccion.Postear, "", Integer.parseInt(s[2])).start();
                System.out.println("Escribo post");
            }
            else if (s[1].equals("EscribeComentario"))
            {
                //Crear un hilo que genere una petición de VisitaPagina(tamaño, urlPost);
                new Peticion(Peticion.EAccion.Comentar, s[3],Integer.parseInt(s[2])).start();                
                System.out.println("Escribo comentario");
            }

            linea = Lector.readLine();
        }
        System.out.println("(Simulación clientes) Acaba la simulación");
    }
    
    //Método principal que crea el log con todos los usuarios concurrentemente
    public static void crearLog() throws IOException, InterruptedException
    {
        empiezaLog();
        //Creamos el fichero del log
        int numeroClientesCreados = 0;

        LogWriter = new Escritor(new FileWriter(NombreFichero));

        //Creamos el semáforo bloqueante para esperar a los clientes
        semaforoBloqueante = new Semaphore(0, true);
        //Creamos el semáforo mutex para que sólo escriba un cliente en el fichero
        semaforoMutex = new Semaphore(1, true);

        //se crean los clientes
        int tiempoRestante = tiempoSimulacion;
        System.out.println("(Creacion log)Empieza la creación de los clientes ");
        while (tiempoRestante > 0)
        {
            numeroClientesCreados++;
            System.out.println("(Creacion log)Nuevo cliente ("+numeroClientesCreados+")");
            new Cliente("Cliente-"+numeroClientesCreados).start();
            //Cada dia entran 116692 clientes, de los cuales estimamos que unos 100000 lo hacen durante el
            //dia (de 8h00 hasta las 24h00). De ahí se puede deducir el tiempo medio entre el que se conecan dos clientes.
            Thread.sleep(tiempoEntreLlegadas); //1.16s 
            tiempoRestante -= tiempoEntreLlegadas; 
        }
        System.out.println("(Creacion log)Acaba la creación de los clientes, clientesCreados = " + numeroClientesCreados);
        Thread.sleep(tiempoEntreLlegadas);
        acabaLog();

        //Hacemos wait del NUMERO_CLIENTES para esperar a que acaben todos
        semaforoBloqueante.acquire(numeroClientesCreados);
        System.out.println("(Creacion log)Todos los clientes han acabado");

        //Cerramos el fichero
        LogWriter.flush();
        LogWriter.close();

    }
    public void setTiempoSimulacion(float tiempoSimulacion) 
    {
        //modificamos el tiempo para hacer la creación del log más rápida
        Simulador.tiempoSimulacion = (int) modificarTiempo(tiempoSimulacion, true);
    }
    
    public void setTiempoEntreLlegadas(float tiempoEntreLlegadas) 
    {
       //modificamos el tiempo para hacer la creación del log más rápida
        Simulador.tiempoEntreLlegadas = (int) modificarTiempo(tiempoEntreLlegadas, true);
    }

    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException
    {
        //invocamos la interfaz
        Ventana ventana = new Ventana();
        ventana.setVisible(true);
        //Simulador simulador = new Simulador();
        //Peticion peticion = new Peticion(Peticion.EAccion.Postear, null, 50);
//        Peticion peticion = new Peticion(Peticion.EAccion.Comentar, "http://localhost/wordpress/?p=1157", 550);
//        peticion.start();
//        double x = 0;
//        Estadisticas esta = new Estadisticas();
//        for (int i = 0; i < 5000000; i++)
//        {
//            x += esta.LogNormal(1.789, 2.366)%5251;
//            
//        }
//        double y = x / 5000000;
//        System.out.println("y = " + y);
    }


}
