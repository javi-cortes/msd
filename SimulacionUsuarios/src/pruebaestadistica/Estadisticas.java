/*
 * Para utilizar las 3 funciones, basta con utilizar Estadisticas.Lognormal().random (por ejemplo)
 * Gamma y Lognormal tienen los parámetros en la función random, pero Zipf los tiene en
 * el constructor de Estadisticas.
 */

package pruebaestadistica;
import java.util.Random;
import jdistlib.Gamma;
import jdistlib.LogNormal;
import jdistlib.Uniform;
import jdistlib.Zipf;
/**
 *
 * @author rrs160
 */
public class Estadisticas {
    LogNormal LogNormal;
    Gamma Gamma;
    Random Random;
    Zipf Zipf;
    Uniform Uniform;
    public Estadisticas()
    {
        LogNormal = new LogNormal();
        Gamma = new Gamma();
        Zipf = new Zipf(10,4);
        Uniform = new Uniform();
        Random = new Random();
        Random.setSeed(System.currentTimeMillis());
    }

    public double LogNormal(double mu, double sigma){
        return LogNormal.random(mu, sigma, Random);
    }
    
    public double Gamma(){
        return Gamma.random(1, 2, Random);
    }
    
    public double Zipf(){
        return Zipf.random();
    }

    public double Uniforme(double min, double max){
        return Uniform.random(min, max, Random);
    }
}
