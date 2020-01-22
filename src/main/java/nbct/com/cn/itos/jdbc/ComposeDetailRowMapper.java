package nbct.com.cn.itos.jdbc;

import java.util.List;
import java.util.stream.Collectors;

import io.vertx.core.json.JsonObject;
import nbct.com.cn.itos.model.ComposeDetail;

/**
* @author PJ 
* @version 创建时间：2020年1月21日 下午2:50:57
*/
public class ComposeDetailRowMapper implements RowMapper<ComposeDetail> {

	@Override
	public ComposeDetail from(JsonObject row) {
		return ComposeDetail.from(row);
	}

	@Override
	public List<ComposeDetail> from(List<JsonObject> rows) {
		List<ComposeDetail> result = rows.stream().map(row -> {
			try {
				return ComposeDetail.from(row);
			} catch (Exception e) {
				throw new RuntimeException("DB模版转换错误。");
			}
		}).collect(Collectors.toList());
		return result;
	}

}
