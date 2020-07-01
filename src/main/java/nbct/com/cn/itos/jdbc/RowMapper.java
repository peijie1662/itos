package nbct.com.cn.itos.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

	default List<T> from(List<JsonObject> rows) {
		List<T> result = rows.stream().map(row -> {
			try {
				return from(row);
			} catch (Exception e) {
				throw new RuntimeException("DB模版转换错误。");
			}
		}).collect(Collectors.toList());
		return result;
	}

	default Map<String, List<T>> mfrom(List<JsonObject> rows, String key) {
		String bkey = key.toUpperCase();
		Map<String, List<T>> map = new HashMap<String, List<T>>();
		rows.forEach(r -> {
			String k = r.getString(bkey);
			if (map.get(k) != null) {
				map.get(k).add(from(r));
			} else {
				List<T> list = new ArrayList<T>();
				list.add(from(r));
				map.put(k, list);
			}
		});
		return map;
	}

}
