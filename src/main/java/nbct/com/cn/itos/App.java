package nbct.com.cn.itos;

import io.vertx.core.Vertx;

/**
 * ITOS SERVER
 */
public class App 
{
    public static void main( String[] args )
    {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle());
    }
}
