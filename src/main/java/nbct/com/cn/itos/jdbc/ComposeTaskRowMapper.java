package nbct.com.cn.itos.jdbc;

import java.util.List;
import java.util.stream.Collectors;

import io.vertx.core.json.JsonObject;
import nbct.com.cn.itos.model.ComposeTask;

/**
* @author PJ 
* @version 创建时间：2020年1月28日 上午10:53:19
* 类说明
*/
public class ComposeTaskRowMapper implements RowMapper<ComposeTask>{

	@Override
	public ComposeTask from(JsonObject row) {
		try {
			return ComposeTask.from(row);
		} catch (Exception e) {
			throw new RuntimeException("DB模版转换错误。");
		}
	}

	@Override
	public List<ComposeTask> from(List<JsonObject> rows) {
		List<ComposeTask> result = rows.stream().map(row -> {
			try {
				return ComposeTask.from(row);
			} catch (Exception e) {
				throw new RuntimeException("DB模版转换错误。");
			}
		}).collect(Collectors.toList());
		return result;
	}



}
