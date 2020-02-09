package nbct.com.cn.itos.handler;

import io.vertx.ext.web.RoutingContext;
import nbct.com.cn.itos.jdbc.JdbcHelper;
import nbct.com.cn.itos.model.Duty;

/**
 * @author PJ
 * @version 创建时间：2020年2月9日 下午3:56:07
 */
public class FirstPageHandler {

	/**
	 * 值班表
	 */
	public void getDutyList(RoutingContext ctx) {
		String sql = "select * from itos_duty order by dutyDate";
		JdbcHelper.entrys(ctx, sql, new Duty(), "dutyDate");
	}

}
