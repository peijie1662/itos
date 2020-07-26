package nbct.com.cn.itos;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;

/**
* @author PJ 
* @version 创建时间：2020年7月26日 下午12:16:06
*/
public class ConfigVerticle extends AbstractVerticle{
	
	/**
	 * 配置项
	 */
	public static JsonObject CONFIG;

	/**
	 * 数据库连接
	 */
	public static SQLClient SC;

	@Override
	public void start() throws Exception {
		ConfigRetriever retriever = ConfigRetriever.create(vertx);
		retriever.getConfig(r -> {
			CONFIG = r.result();		
			JsonObject dbConfig = CONFIG.getJsonObject("db");
			if (dbConfig == null) {
				throw new RuntimeException("没有找到指定的数据源");
			}
			SC = JDBCClient.createShared(vertx, dbConfig);			
		});
	}
	
	/**
	 * 注册URL
	 */
	public static String getRegisterUrl() {
		JsonObject registerUrl = CONFIG.getJsonObject("registerUrl");
		return "http://" + registerUrl.getString("ip") + ":" + registerUrl.getInteger("port");
	}

	/** 
	 * 服务列表URL
	 */
	public static String getActiveUrl() {
		JsonObject registerUrl = CONFIG.getJsonObject("registerUrl");
		return getRegisterUrl() + registerUrl.getString("active");
	}
	
}
