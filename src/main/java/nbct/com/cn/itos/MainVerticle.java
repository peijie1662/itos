package nbct.com.cn.itos;

import org.apache.log4j.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import nbct.com.cn.itos.config.Configer;
import nbct.com.cn.itos.handler.LoginHandler;
import nbct.com.cn.itos.handler.ModelHandler;
import nbct.com.cn.itos.handler.SettingsHandler;
import nbct.com.cn.itos.handler.TaskHandler;
import nbct.com.cn.itos.handler.UploadHandler;

public class MainVerticle extends AbstractVerticle {

	public static Logger logger = Logger.getLogger(MainVerticle.class);

	@Override
	public void start() throws Exception {
		Router router = Router.router(vertx);
		router.route()
				.handler(CorsHandler.create("*")//
						.allowedMethod(HttpMethod.GET)//
						.allowedMethod(HttpMethod.OPTIONS)//
						.allowedMethod(HttpMethod.POST)//
						.allowedHeader("X-PINGARUNER")//
						.allowedHeader("Content-Type"));
		router.route().handler(BodyHandler.create());

		LoginHandler loginHandler = new LoginHandler();
		ModelHandler modelHandler = new ModelHandler();
		TaskHandler taskHandler = new TaskHandler();
		SettingsHandler settingsHandler = new SettingsHandler();
		UploadHandler uploadHandler = new UploadHandler();

		// 静态文件
		router.route("/static/*").handler(StaticHandler.create(Configer.uploadDir));

		// 登录
		router.post("/login").blockingHandler(loginHandler::handleLogin, false);

		// 模版列表
		router.post("/model/list").blockingHandler(modelHandler::getTimerTaskModelList, false);
		// 修改模版
		router.post("/model/update").blockingHandler(modelHandler::updateTimerTaskModel, false);
		// 删除模版
		router.post("/model/delete").blockingHandler(modelHandler::deleteTimerTaskModel, false);
		// 增加模版
		router.post("/model/add").blockingHandler(modelHandler::addTimerTaskModel, false);
		//模版文件上传
		router.post("/model/uploadfile").blockingHandler(uploadHandler::uploadModelFile, false);

		// 人工任务列表
		router.post("/manualtask/list").blockingHandler(taskHandler::getManualTaskList, false);
		// 保存任务
		router.post("/manualtask/add").blockingHandler(taskHandler::saveManualTask, false);
		// 任务状态-SWAP
		router.post("/manualtask/swap").blockingHandler(taskHandler::swapTask, false);
		// 系统任务列表
		router.post("/dispatchtask/list").blockingHandler(taskHandler::getDispatchTaskList, false);
		// 任务日志
		router.post("/task/log").blockingHandler(taskHandler::getTaskLog, false);
		// 任务状态-PROCESSING,DONE,CANCEL
		router.post("/task/updatestatus").blockingHandler(taskHandler::updateTaskStatus, false);
		// 任务状态-MODIFY
		router.post("/task/modify").blockingHandler(taskHandler::modifyTask, false);
		// 任务图片上传
		router.post("/task/uploadfile").blockingHandler(uploadHandler::uploadTaskFile, false);

		// 智能提示列表
		router.post("/smarttips/list").blockingHandler(settingsHandler::getSmartTipsList, false);
		// 添加智能提示
		router.post("/smarttips/add").blockingHandler(settingsHandler::addSmartTips, false);
		// 修改智能提示
		router.post("/smarttips/update").blockingHandler(settingsHandler::updateSmartTips, false);
		// 删除智能提示
		router.post("/smarttips/delete").blockingHandler(settingsHandler::deleteSmartTips, false);

		Configer.initDbPool(vertx);
		vertx.deployVerticle(new TimerVerticle());
		vertx.deployVerticle(new WebsocketVerticle());
		vertx.createHttpServer().requestHandler(router).listen(Configer.getHttpPort());

		logger.info("ITOS server start");

	}
}
