package nbct.com.cn.itos.config;

import java.io.File;
import java.io.FileInputStream;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;

/**
 * @author PJ
 * @version 创建时间：2019年12月23日 上午9:03:04 类说明
 */
@SuppressWarnings("resource")
public class Configer {
	
	private static JsonObject config;

	/**
	 * 登录服务URL
	 */
	public static String loginServer;
	
	/**
	 * ITOP服务URL
	 */
	public static String itopServer;

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

	static {
		byte[] buff = new byte[102400];
		try {
			new FileInputStream(new File("d:/itos.json")).read(buff);
			config = new JsonObject(new String(buff, "utf-8"));
			registerUrl = config.getJsonObject("registerUrl");
			loginServer = config.getString("loginServer");
			itopServer = config.getString("itopServer");
			provider = config.getJsonObject("provider");
			uploadDir = config.getString("uploadDir");
			heartbeatThreshold = config.getJsonObject("dispatchClient").getInteger("heartbeatThreshold");
		} catch (Exception e) {
			System.out.println("读取配置文件失败");
		}
	}

	public static void initDbPool(Vertx vertx, String dsName) {
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

	public static String getRegisterUrl() {
		return "http://" + registerUrl.getString("ip") + ":" + registerUrl.getInteger("port");
	}
}
