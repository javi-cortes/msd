/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulacionusuarios;

/**
 *
 * @author jco322
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

public class Peticion extends Thread
{
    private DataSource dataSourcePool;
   
    public static enum EAccion
    {
        Consultar, Comentar, Postear;
    }
    
    private EAccion accion;
    private String url;
    //tamaño del post / comentario a generar
    private int size;
    
    private Peticion()
    {
        
    }

    public Peticion(EAccion accion, String url, int size)
    {
        this.accion = accion;
        //linkamos al pool de conexiones del simulador
        this.dataSourcePool = Simulador.dataSourcePool;
        this.url = url;
        this.size = size;
        System.out.println("Nueva petición("+getName()+") : accion = "+accion+", url = " +url +", size = "+size );
    }

    
    //Se ejecuta una de las 3 acciones posibles
    @Override
    public void run() 
    {
        switch (accion)
        {
            case Consultar :
                try 
                {
                    consultarPagina(url);
                } 
                catch (MalformedURLException ex) 
                {
                    Logger.getLogger(Peticion.class.getName()).log(Level.SEVERE, null, ex);
                } 
                catch (IOException ex) 
                {
                        Logger.getLogger(Peticion.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case Comentar : escribirComentario(url, size);
                break;
            case Postear : escribirPost(size);
                break;
        }
    }
    

    private void consultarPagina(String Sitio) throws MalformedURLException, IOException
    {
        URL url = new URL(Sitio);
        URLConnection connection = url.openConnection();

        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        String linea;
        while ((linea = input.readLine()) != null) 
        {
            //recorremos todo el input, para simular que el usuario lee la página.
        }
    }

    
    private void escribirComentario(String UrlPost, int commentSize)
    {
        Connection conexion = null;
        PreparedStatement preparedStatement = null;
        try
        {
            conexion = dataSourcePool.getConnection();

            // the mysql insert statement
            String query = " insert into wp_comments (comment_post_ID, comment_content, comment_author)"
                    + " values (?, ?, ?)";
            // PreparedStatements can use variables and are more efficient
            preparedStatement = conexion.prepareStatement(query);
            int id = parsearPostId(UrlPost);
            //asignamos cada campo
            preparedStatement.setInt(1, id);
            preparedStatement.setString (2, dummyComment(commentSize));
            preparedStatement.setString (3, "simulador");
            preparedStatement.execute();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            Simulador.liberaConexion(conexion);
        }
    }
    
    private void escribirPost(int postSize)
    {
        Connection conexion = null;
        PreparedStatement preparedStatement = null;
        try
        {
            conexion = dataSourcePool.getConnection();

            // the mysql insert statement
            String query = " insert into wp_posts (post_content, post_title, post_excerpt, comment_count, post_like, post_nolike, post_dontcare, to_ping, pinged, post_content_filtered)" 
                    + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            // PreparedStatements can use variables and are more efficient
            preparedStatement = conexion.prepareStatement(query);

            preparedStatement.setString (1, dummyComment(postSize));
            preparedStatement.setString (2, "Post cliente java");
            preparedStatement.setString (3, "post_excerpt cliente java");
            preparedStatement.setInt (4, 0);
            preparedStatement.setInt(5, 0);
            preparedStatement.setInt(6, 0);
            preparedStatement.setInt(7, 0);
            preparedStatement.setString(8, "to_ping cliente java");
            preparedStatement.setString(9, "pinged cliente java");
            preparedStatement.setString(10, "post_content_filtered cliente java");
            preparedStatement.execute();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            Simulador.liberaConexion(conexion);
        }
    }

    //Obtiene la id del post dada un URL, ex : "http://localhost/wordpress/?p=123"; ( id = 123)
    private static int parsearPostId(String UrlPost)
    {
        return Integer.parseInt(UrlPost.split("/?p=")[1]);
    }

    //Obtiene la id del post dada un URL, ex : "http://localhost/wordpress/?p=123"; ( id = 123)
    private static String parsearCliente(String UrlPost)
    {
        return (UrlPost.split("_")[0]);
    }

    //método que crea una string de tamaño commentSize
    private static String dummyComment(int commentSize)
    {
        String dummy ="";
        for (int i = 0; i < commentSize; i++) 
        {
            if ( i%60 == 0 )
            {
                dummy +="\n";
            }
            dummy += "c";
        }
        return dummy;
    }
    
    private void testConexion() 
    {
        Connection conexion = null;
        int i = 4;
        try 
        {
            conexion = dataSourcePool.getConnection();
            Statement sentencia = conexion.createStatement();
            ResultSet rs = sentencia.executeQuery("select * from wp_posts");

            // La tabla tiene cuatro campos.
            while (rs.next() && i > 0 ) 
            {
                System.out.println("sOY "+getName());
                System.out.println(rs.getObject("post_content"));
                System.out.println("--------------");
                i--;
            }
            

        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
        } 
        finally 
        {
            Simulador.liberaConexion(conexion);
        }

    }
    
    
  
}

// MONO thread example ( para multi -> poolConnection )
//    public static void main(String[] args) 
//    {
        //EJEMPLO ESCRIBIR UN comment
        
//        Connection connect = null;
//        PreparedStatement preparedStatement = null;
//        try
//        {
//            // This will load the MySQL driver, each DB has its own driver
//            Class.forName("com.mysql.jdbc.Driver");
//            // Setup the connection with the DB
//            connect = DriverManager.getConnection("jdbc:mysql://localhost/wp2?", "cliente2", "");
//            
//            // the mysql insert statement
//            String query = " insert into wp_comments (comment_post_ID, comment_content, comment_author)" 
//                    + " values (?, ?, ?)";
//            // PreparedStatements can use variables and are more efficient
//            preparedStatement = connect.prepareStatement(query);
//
//            preparedStatement.setInt(1, 1056);//ejemplo de id, habría que sacarlo del post en concreto
//            preparedStatement.setString (2, "contenido de un comentario  desde cliente java");
//            preparedStatement.setString (3, "cliente java");
//            preparedStatement.execute();
            
            
            
            //QUERY JEMPLO COMENTARIO 
                        // the mysql insert statement
//            String query = " insert into wp_comments (comment_post_ID, comment_content, comment_author)" 
//                    + " values (?, ?, ?)";
//            // PreparedStatements can use variables and are more efficient
//            preparedStatement = connect.prepareStatement(query);
//
//            preparedStatement.setInt(1, 1056);//ejemplo de id, habría que sacarlo del post en concreto
//            preparedStatement.setString (2, "contenido de un comentario  desde cliente java");
//            preparedStatement.setString (3, "cliente java");
//            preparedStatement.execute();
        
        
        
//        //EJEMPLO QUERY ESCRIBIR UN POST
//            // the mysql insert statement
//            String query = " insert into wp_posts (post_content, post_title, post_excerpt, comment_count, post_like, post_nolike, post_dontcare, to_ping, pinged, post_content_filtered)" 
//                    + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
//            // PreparedStatements can use variables and are more efficient
//            preparedStatement = connect.prepareStatement(query);
//
//            preparedStatement.setString (1, "ejemplo post deun cliente");
//            preparedStatement.setString (2, "titulo post cliente java");
//            preparedStatement.setString (3, "post_excerpt cliente java");
//            preparedStatement.setInt (4, 2);
//            preparedStatement.setInt(5, 3);
//            preparedStatement.setInt(6, 4);
//            preparedStatement.setInt(7, 5);
//            preparedStatement.setString(8, "to_ping cliente java");
//            preparedStatement.setString(9, "pinged cliente java");
//            preparedStatement.setString(10, "post_content_filtered cliente java");
//            preparedStatement.execute();
//        }
//        catch (Exception e)
//        {
//            System.err.println("Got an exception!");
//            System.err.println(e.getMessage());
//        }
//        finally 
//        {
//            try 
//            {
//                if (connect != null) 
//                {
//                    connect.close();
//                }
//            }
//            catch (Exception e) 
//            {
//                System.err.println("Got an exception closing connect!");
//                System.err.println(e.getMessage());
//            }
//        }
//    }