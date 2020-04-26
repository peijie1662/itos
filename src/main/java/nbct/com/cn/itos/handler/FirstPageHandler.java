package nbct.com.cn.itos.handler;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import nbct.com.cn.itos.jdbc.JdbcHelper;
import nbct.com.cn.itos.model.Duty;
import nbct.com.cn.itos.model.FirstPageLog;

/**
 * @author PJ
 * @version 创建时间：2020年2月9日 下午3:56:07
 */
public class FirstPageHandler {

	/**
	 * 值班表
	 */
	public void getDutyList(RoutingContext ctx) {
		String sql = "select userId,substr(dutyDate,1,4)||'-'||substr(dutyDate,5,2)||'-'||substr(dutyDate,7,2) as dutyDate from itos_duty ";
		JdbcHelper.entrys(ctx, sql, new Duty(), "dutyDate");
	}

	/**
	 * 添加值班
	 */
	public void addDuty(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		JsonArray params = new JsonArray().add(rp.getString("userId")).add(rp.getString("dutyDate").replace("-", ""));
		String sql = "insert into itos_duty(userId,dutyDate) values(?,?)";
		JdbcHelper.update(ctx, sql, params);
	}

	/**
	 * 取消值班
	 */
	public void delDuty(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		JsonArray params = new JsonArray().add(rp.getString("userId")).add(rp.getString("dutyDate").replace("-", ""));
		String sql = "delete from itos_duty where userId = ? and dutyDate = ?";
		JdbcHelper.update(ctx, sql, params);
	}
	
	/**
	 * 系统动态
	 */
	public void getItosLog(RoutingContext ctx) {
		String sql = "select * from (select opdate,status,statusdesc from itos_tasklog order by opdate desc) a " + 
				" where rownum <= 10";
		JdbcHelper.rows(ctx, sql, new FirstPageLog());	
	}
}
