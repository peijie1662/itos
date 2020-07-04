package nbct.com.cn.itos.config;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;

/**
 * @author PJ
 * @version 创建时间：2019年12月23日 上午9:03:04 
 */
public class Configer {

	private static JsonObject config;

	/**
	 * 登录服务URL
	 */
	public static String loginServer;

	/**
	 * ITOP服务
	 */
	public static String itopServer;
	
	/**
	 * 工作联系单服务
	 */
	public static String itafServer;	
	
	/**
	 * 短信服务
	 */
	public static JsonObject smsServer;

	/**
	 * 注册服务URL
	 */
	public static JsonObject registerUrl;

	/**
	 * 上传目录
	 */
	public static String uploadDir;

	/**
	 * 注册时提供的自描述
	 */
	public static JsonObject provider;

	/**
	 * 数据库链接
	 */
	public static SQLClient client;

	/**
	 * dispatch client 心跳阈值
	 */
	public static int heartbeatThreshold;
	
	private static JsonObject getConfig(Vertx vertx) {
		FileSystem fs = vertx.fileSystem();
		Buffer buf = fs.readFileBlocking("d:/itos.json");
		return new JsonObject(buf);
	}

	/**
	 * 初始化数据库连接池 
	 */
	public static void initDbPool(Vertx vertx, String dsName) {
		config = getConfig(vertx);
		registerUrl = config.getJsonObject("registerUrl");
		loginServer = config.getString("loginServer");
		itopServer = config.getString("itopServer");
		itafServer = config.getString("itafServer");
		smsServer = config.getJsonObject("smsServer");
		provider = config.getJsonObject("provider");
		uploadDir = config.getString("uploadDir");
		heartbeatThreshold = config.getJsonObject("dispatchClient").getInteger("heartbeatThreshold");
		JsonObject dbConfig = config.getJsonObject(dsName);
		if (dbConfig == null) {
			throw new RuntimeException("没有找到指定的数据源");
		}
		client = JDBCClient.createShared(vertx, dbConfig);
	}

	public static void initDbPool(Vertx vertx) {
		initDbPool(vertx, "db");
	}

	public static int getHttpPort() {
		return provider.getInteger("port");
	}

	public static int getWebsocketPort() {
		return provider.getInteger("wsport");
	}

	/**
	 * 注册URL
	 */
	public static String getRegisterUrl() {
		return "http://" + registerUrl.getString("ip") + ":" + registerUrl.getInteger("port");
	}

	/** 
	 * 服务列表URL
	 */
	public static String getActiveUrl() {
		return getRegisterUrl() + registerUrl.getString("active");
	}
}
