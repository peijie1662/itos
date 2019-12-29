package nbct.com.cn.itos.jdbc;

import java.util.List;

import io.vertx.core.json.JsonObject;

/**
 * ORM Convert
 * 
 * @author PJ
 * @version 创建时间：2019年12月27日 上午8:39:01
 * @param <T>
 */
public interface RowMapper<T> {

	T from(JsonObject row);
	
	List<T> from(List<JsonObject> rows);

}
