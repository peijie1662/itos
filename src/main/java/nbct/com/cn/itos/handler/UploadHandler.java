package nbct.com.cn.itos.handler;

import static nbct.com.cn.itos.model.CallResult.Err;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.file.CopyOptions;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import nbct.com.cn.itos.config.Configer;
import nbct.com.cn.itos.model.CallResult;

/**
 * @author PJ
 * @version 创建时间：2020年1月12日 下午5:08:05
 */
public class UploadHandler {
	
	public static Logger log = LogManager.getLogger(UploadHandler.class);

	/**
	 * 文档文件上传
	 */
	public void uploadDocumentFile(RoutingContext ctx) {
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		try {
			FileSystem fs = ctx.vertx().fileSystem();
			String savePath = Configer.uploadDir + "pdf/document/";
			if (!fs.existsBlocking(savePath)) {
				fs.mkdirsBlocking(savePath);
			}
			Set<FileUpload> uploads = ctx.fileUploads();
			CallResult<String> result = new CallResult<String>().ok();
			uploads.forEach(fileUpload -> {
				String path = savePath + "/" + fileUpload.fileName();
				fs.move(fileUpload.uploadedFileName(), path, new CopyOptions().setReplaceExisting(true), ar -> {
					if (!ar.succeeded()) {
						// fs.move is asynchronously,so not work...
						result.err(ar.cause().getMessage());
					}
				});
			});
			res.end(result.toString());
		} catch (Exception e) {
			res.end(Err(e.getMessage()));
			e.printStackTrace();
		}
	}

	/**
	 * 任务文件上传
	 */
	public void uploadTaskFile(RoutingContext ctx) {
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		try {
			FileSystem fs = ctx.vertx().fileSystem();
			String taskId = ctx.request().getFormAttribute("taskId");
			String savePath = Configer.uploadDir + "task_file/" + taskId;
			if (!fs.existsBlocking(savePath)) {
				fs.mkdirsBlocking(savePath);
			}
			Set<FileUpload> uploads = ctx.fileUploads();
			CallResult<String> result = new CallResult<String>().ok();
			uploads.forEach(fileUpload -> {
				String path = savePath + "/" + fileUpload.fileName();
				fs.move(fileUpload.uploadedFileName(), path, new CopyOptions().setReplaceExisting(true), ar -> {
					if (!ar.succeeded()) {
						// fs.move is asynchronously,so not work...
						result.err(ar.cause().getMessage());
					}
				});
			});
			res.end(result.toString());
		} catch (Exception e) {
			res.end(Err(e.getMessage()));
			e.printStackTrace();
		}
	}

	/**
	 * 模版文件上传
	 */
	public void uploadModelFile(RoutingContext ctx) {
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		try {
			FileSystem fs = ctx.vertx().fileSystem();
			String modelId = ctx.request().getFormAttribute("modelId");
			String savePath = Configer.uploadDir + "model_file/" + modelId;
			if (!fs.existsBlocking(savePath)) {
				fs.mkdirsBlocking(savePath);
			}
			Set<FileUpload> uploads = ctx.fileUploads();
			CallResult<String> result = new CallResult<String>().ok();
			uploads.forEach(fileUpload -> {
				String path = savePath + "/" + fileUpload.fileName();
				fs.move(fileUpload.uploadedFileName(), path, new CopyOptions().setReplaceExisting(true), ar -> {
					if (!ar.succeeded()) {
						// fs.move is asynchronously,so not work...
						result.err(ar.cause().getMessage());
					}
				});
			});
			res.end(result.toString());
		} catch (Exception e) {
			res.end(Err(e.getMessage()));
			e.printStackTrace();
		}
	}

	/**
	 * 用户头像上传
	 */
	public void uploadUserFace(RoutingContext ctx) {
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		try {
			FileSystem fs = ctx.vertx().fileSystem();
			String userId = ctx.request().getFormAttribute("userId");
			String savePath = Configer.uploadDir + "user_face";
			if (!fs.existsBlocking(savePath)) {
				fs.mkdirsBlocking(savePath);
			}
			Set<FileUpload> uploads = ctx.fileUploads();
			CallResult<String> result = new CallResult<String>().ok();
			uploads.forEach(fileUpload -> {
				String path = savePath + "/" + userId + ".jpg";// 约定头像必须是JPG文件
				fs.move(fileUpload.uploadedFileName(), path, new CopyOptions().setReplaceExisting(true), ar -> {
					if (ar.failed()) {
						ar.cause().printStackTrace();
					}
				});
			});
			res.end(result.toString());
		} catch (Exception e) {
			res.end(Err(e.getMessage()));
			e.printStackTrace();
		}
	}
}
