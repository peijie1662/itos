package nbct.com.cn.itos.jdbc;

import java.util.List;
import java.util.stream.Collectors;

import io.vertx.core.json.JsonObject;
import nbct.com.cn.itos.model.ItosUser;

/**
 * @author PJ
 * @version 创建时间：2019年12月31日 下午3:44:05 类说明
 */
public class UserRowMapper implements RowMapper<ItosUser> {

	@Override
	public ItosUser from(JsonObject row) {
		try {
			return ItosUser.from(row);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("DB模版转换错误。");
		}
	}

	@Override
	public List<ItosUser> from(List<JsonObject> rows) {
		List<ItosUser> result = rows.stream().map(row -> {
			try {
				return ItosUser.from(row);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("DB模版转换错误。");
			}
		}).collect(Collectors.toList());
		return result;
	}

}
