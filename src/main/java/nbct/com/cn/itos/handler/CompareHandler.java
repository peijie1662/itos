package nbct.com.cn.itos.handler;
/**
* @author PJ 
* @version 创建时间：2020年6月20日 上午10:55:09
*/

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import nbct.com.cn.itos.jdbc.JdbcHelper;
import nbct.com.cn.itos.model.CompareFile;

import static nbct.com.cn.itos.model.CallResult.Err;
import static nbct.com.cn.itos.model.CallResult.OK;

public class CompareHandler {

	// key -> compareId@serviceName<br>
	// value -> ComparingFile
	public final static Map<String, CompareFile> CUR_COMPARES = new ConcurrentHashMap<String, CompareFile>();

	/**
	 * 页端访问列表
	 */
	public void pageCompareList(RoutingContext ctx) {
		HttpServerResponse res = ctx.response();
		String sql = "select * from itos_filecompare order by comparegroup,filepath,filename";
		Future<List<CompareFile>> f = JdbcHelper.rows(sql, new CompareFile());
		f.onSuccess(r -> {
			// 1.补上缓存数据
			r.forEach(item -> {
				CompareFile cur = CUR_COMPARES.get(item.key());
				if (cur != null) {
					item.setCurFileSize(cur.getCurFileSize());
					item.setCurFileModifyTime(cur.getCurFileModifyTime());
					item.setCurRefreshDate(cur.getCurRefreshDate());
				}
			});
			res.end(OK(r));
		}).onFailure(e -> {
			e.printStackTrace();
			res.end(Err(e.getMessage()));
		});
	}

	/**
	 * 添加比对项
	 */
	public void addCompareItem(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "insert into itos_filecompare(compareid,comparegroup,filepath," + //
				"filename,compareoption,servicename) values(?,?,?,?,?,?)";
		JsonArray params = new JsonArray().add(UUID.randomUUID().toString())//
				.add(rp.getString("compareGroup"))//
				.add(rp.getString("filePath"))//
				.add(rp.getString("fileName"))//
				.add(rp.getString("compareOption"))//
				.add(rp.getString("serviceName"));
		JdbcHelper.update(ctx, sql, params);
	}

	/**
	 * 修改对比项
	 */
	public void updateCompareItem(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "update itos_filecompare set compareGroup = ?,filePath = ?,fileName =?," + //
				"compareOption = ?,serviceName =? where compareId = ?";
		JsonArray params = new JsonArray().add(rp.getString("compareGroup"))//
				.add(rp.getString("filePath"))//
				.add(rp.getString("fileName"))//
				.add(rp.getString("compareOption"))//
				.add(rp.getString("serviceName"))//
				.add(rp.getString("compareId"));
		JdbcHelper.update(ctx, sql, params);
	}

	/**
	 * 删除对比项
	 */
	public void delCompareItem(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "delete from itos_filecompare where compareId = ?";
		JsonArray params = new JsonArray().add(rp.getString("compareId"));
		JdbcHelper.update(ctx, sql, params);
	}

	/**
	 * 客户端访问列表
	 */
	public void clientCompareList(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "select * from itos_filecompare where serviceName = ?";
		JsonArray params = new JsonArray().add(rp.getString("serviceName"));
		JdbcHelper.rows(ctx, sql, params, new CompareFile());
	}

	/**
	 * 客户端更新比对信息
	 */
	public void clientRefresh(RoutingContext ctx) {
		HttpServerResponse res = ctx.response();
		JsonObject rp = ctx.getBodyAsJson();
		CompareFile cf = new CompareFile();
		cf.setCompareGroup(rp.getString("compareGroup"));
		cf.setServiceName(rp.getString("serviceName"));
		cf.setCurFileSize(rp.getString("curFileSize"));
		cf.setCurFileModifyTime(rp.getString("curModifyTime"));
		cf.setCurRefreshDate(LocalDateTime.now());
		CUR_COMPARES.put(cf.key(), cf);
		res.end(OK());
	}

}
