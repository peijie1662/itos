package nbct.com.cn.itos.handler;

import io.vertx.ext.web.RoutingContext;
import nbct.com.cn.itos.jdbc.JdbcHelper;
import nbct.com.cn.itos.jdbc.ModelRowMapper;

/**
* @author PJ 
* @version 创建时间：2019年12月28日 下午9:02:04
*/
public class TaskHandler {
	
	public void getTaskList(RoutingContext ctx) {
		String sql = "select * from itos_task where invalid = 'N' order by opdate desc";
		JdbcHelper.rows(ctx, sql, new ModelRowMapper());
	}
	
	public void phoneAssociate(RoutingContext ctx){
		
	}

}
