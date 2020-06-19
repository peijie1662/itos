package nbct.com.cn.itos.handler;

import static nbct.com.cn.itos.model.CallResult.Err;
import static nbct.com.cn.itos.model.CallResult.OK;

import java.time.LocalDateTime;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.file.CopyOptions;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import nbct.com.cn.itos.config.Configer;
import nbct.com.cn.itos.jdbc.JdbcHelper;
import nbct.com.cn.itos.model.ItosDocument;
import nbct.com.cn.itos.model.SysCode;
import util.DateUtil;

/**
 * @author PJ
 * @version 创建时间：2020年6月12日 下午1:38:20
 */
public class DocumentHandler {

	public static Logger log = LogManager.getLogger(DocumentHandler.class);

	/**
	 * 文档列表
	 */
	public void getDocumentList(RoutingContext ctx) {
		String sql = "select * from itos_document order by groupId,orderInGroup";
		JdbcHelper.rows(ctx, sql, new ItosDocument());
	}

	/**
	 * 分组列表
	 */
	public void getGroupList(RoutingContext ctx) {
		String sql = "select * from itos_syscode where sycategory = 'DOCUMENTGROUP' order by syid";
		JdbcHelper.rows(ctx, sql, new SysCode());
	}

	/**
	 * 新建分组
	 */
	public void newGroup(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String func = "{call itos.p_syscode(?,?,?,?)}";
		JsonArray params = new JsonArray().add("ADD" + "^" + "DOCUMENTGROUP" + "^" + rp.getString("groupName"));
		JsonArray outputs = new JsonArray().addNull().add("VARCHAR").add("VARCHAR").add("VARCHAR");
		JdbcHelper.call(ctx, func, params, outputs);
	}

	/**
	 * 删除分组
	 */
	public void delGroup(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String func = "{call itos.p_syscode(?,?,?,?)}";
		JsonArray params = new JsonArray().add("DEL" + "^" + "DOCUMENTGROUP" + "^" + rp.getString("groupName"));
		JsonArray outputs = new JsonArray().addNull().add("VARCHAR").add("VARCHAR").add("VARCHAR");
		JdbcHelper.call(ctx, func, params, outputs);
	}

	/**
	 * 分类列表
	 */
	public void getCategoryList(RoutingContext ctx) {
		String sql = "select * from itos_syscode where sycategory = 'DOCUMENTCATEGORY' order by syid";
		JdbcHelper.rows(ctx, sql, new SysCode());
	}

	/**
	 * 新建分类
	 */
	public void newCategory(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String func = "{call itos.p_syscode(?,?,?,?)}";
		JsonArray params = new JsonArray().add("ADD" + "^" + "DOCUMENTCATEGORY" + "^" + rp.getString("category"));
		JsonArray outputs = new JsonArray().addNull().add("VARCHAR").add("VARCHAR").add("VARCHAR");
		JdbcHelper.call(ctx, func, params, outputs);
	}

	/**
	 * 删除分类
	 */
	public void delCategory(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String func = "{call itos.p_syscode(?,?,?,?)}";
		JsonArray params = new JsonArray().add("DEL" + "^" + "DOCUMENTCATEGORY" + "^" + rp.getString("category"));
		JsonArray outputs = new JsonArray().addNull().add("VARCHAR").add("VARCHAR").add("VARCHAR");
		JdbcHelper.call(ctx, func, params, outputs);
	}

	/**
	 * 文档文件删除
	 */
	public void delDocument(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String fileName = rp.getString("fileName");
		// 1.删除对应文件
		try {
			String path = Configer.uploadDir + "pdf/document/" + fileName;
			FileSystem fs = ctx.vertx().fileSystem();
			if (fs.existsBlocking(path)) {
				fs.deleteBlocking(path);
			}
			log.info("DELDOCUMENT-01::" + fileName + "删除成功。");
		} catch (Exception e) {
			log.error("DELDOCUMENT-02::", e);
		}
		// 2.删除数据记录
		String sql = "delete from itos_document where fileName = ?";
		JsonArray params = new JsonArray().add(fileName);
		JdbcHelper.update(ctx, sql, params);
	}

	/**
	 * 文档文件上传
	 */
	public void uploadDocument(RoutingContext ctx) {
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		try {
			// 1.传入参数
			String category = ctx.request().getFormAttribute("category");
			String abs = ctx.request().getFormAttribute("abstract");
			String userId = ctx.request().getFormAttribute("userId");
			String groupId = ctx.request().getFormAttribute("groupId");
			// 2.保存路径
			FileSystem fs = ctx.vertx().fileSystem();
			String savePath = Configer.uploadDir + "pdf/document/";
			if (!fs.existsBlocking(savePath)) {
				fs.mkdirsBlocking(savePath);
			}
			// 3.文件名
			Set<FileUpload> uploads = ctx.fileUploads();
			if (uploads == null || uploads.isEmpty()) {
				res.end(Err("上传文档出错"));
				return;
			}
			FileUpload f1 = (FileUpload) uploads.toArray()[0];
			String fileName = f1.fileName();
			// 4.写记录并保存文件
			String sql = "insert into itos_document(filePath,fileName,category,abstract,groupId,oper,opDate) "
					+ " values(?,?,?,?,?,?,?)";
			JsonArray params = new JsonArray().add(savePath).add(fileName)//
					.add(category).add(abs).add(groupId).add(userId)//
					.add(DateUtil.localToUtcStr(LocalDateTime.now()));
			JdbcHelper.update(sql, params).onSuccess(r -> {
				uploads.forEach(fileUpload -> {
					String path = savePath + "/" + fileName;
					fs.move(fileUpload.uploadedFileName(), path, new CopyOptions().setReplaceExisting(true), ar -> {
						if (ar.failed())
							log.error("UPLOADDOCUMENT-01::", ar.cause());
					});
				});
			});
			log.info("UPLOADDOCUMENT-02::" + fileName + "上传成功。");
			res.end(OK());
		} catch (Exception e) {
			log.error("UPLOADDOCUMENT-03::", e);
			res.end(Err(e.getMessage()));
		}
	}

	/**
	 * 文件信息修改
	 */
	public void updateDocument(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "update itos_document set category = ?,abstract = ?,groupId = ? where fileName = ?";
		JsonArray params = new JsonArray()//
				.add(rp.getString("category"))//
				.add(rp.getString("abs"))//
				.add(rp.getString("groupId"))//
				.add(rp.getString("fileName"));
		JdbcHelper.update(ctx, sql, params);
	}

}