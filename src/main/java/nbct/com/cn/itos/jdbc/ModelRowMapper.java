package nbct.com.cn.itos.jdbc;

import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

import io.vertx.core.json.JsonObject;
import nbct.com.cn.itos.model.TimerTaskModel;

/**
* @author PJ 
* @version 创建时间：2019年12月27日 上午9:01:14
*/
public class ModelRowMapper implements RowMapper<TimerTaskModel>{

	@Override
	public TimerTaskModel from(JsonObject row) {
		try {
			return TimerTaskModel.from(row);
		} catch (ParseException e) {
			throw new RuntimeException("DB模版转换错误。");
		}
	}

	@Override
	public List<TimerTaskModel> from(List<JsonObject> rows) {
		List<TimerTaskModel> result = rows.stream().map(row -> {
			try {
				return TimerTaskModel.from(row);
			} catch (Exception e) {
				throw new RuntimeException("DB模版转换错误。");
			}
		}).collect(Collectors.toList());
		return result;
	}

}
