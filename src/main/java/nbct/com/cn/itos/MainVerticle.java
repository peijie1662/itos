package nbct.com.cn.itos;

import org.apache.log4j.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import nbct.com.cn.itos.config.Configer;
import nbct.com.cn.itos.handler.AssociateItopHandler;
import nbct.com.cn.itos.handler.CommonTaskHandler;
import nbct.com.cn.itos.handler.ComposeHandler;
import nbct.com.cn.itos.handler.DispatchClientHandler;
import nbct.com.cn.itos.handler.DispatchTaskHandler;
import nbct.com.cn.itos.handler.ManualTaskHandler;
import nbct.com.cn.itos.handler.ModelHandler;
import nbct.com.cn.itos.handler.SettingsHandler;
import nbct.com.cn.itos.handler.UploadHandler;
import nbct.com.cn.itos.handler.UserHandler;

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
		//LoginHandler loginHandler = new LoginHandler();
		ModelHandler modelHandler = new ModelHandler();
		CommonTaskHandler commonTaskHandler = new CommonTaskHandler();
		ManualTaskHandler manualTaskHandler = new ManualTaskHandler();
		DispatchTaskHandler dispatchTaskHandler = new DispatchTaskHandler();
		SettingsHandler settingsHandler = new SettingsHandler();
		UploadHandler uploadHandler = new UploadHandler();
		DispatchClientHandler dispatchClientHandler = new DispatchClientHandler();
		AssociateItopHandler associateItopHandler = new AssociateItopHandler();
		ComposeHandler composeHandler = new ComposeHandler();
		UserHandler userHandler = new UserHandler();

		// 静态文件
		router.route("/static/*").handler(StaticHandler.create(Configer.uploadDir));

		// 登录
		router.post("/login").blockingHandler(userHandler::handleLogin, false);

		// 模版列表
		router.post("/model/list").blockingHandler(modelHandler::getTimerTaskModelList, false);
		// 修改模版
		router.post("/model/update").blockingHandler(modelHandler::updateTimerTaskModel, false);
		// 删除模版
		router.post("/model/delete").blockingHandler(modelHandler::deleteTimerTaskModel, false);
		// 增加模版
		router.post("/model/add").blockingHandler(modelHandler::addTimerTaskModel, false);
		// 模版文件上传
		router.post("/model/uploadfile").blockingHandler(uploadHandler::uploadModelFile, false);
		// 模版状态改变
		router.post("/model/status").blockingHandler(modelHandler::chgModelStatus, false);
		// 组合任务模版
		router.post("/model/composelist").blockingHandler(modelHandler::getComposeModelList, false);
		// 非组合任务模版
		router.post("/model/notcomposelist").blockingHandler(modelHandler::getNotComposeModelList, false);

		// 保存组合任务模版详细信息
		router.post("/composetask/savecomposedetail").blockingHandler(composeHandler::saveComposeModelDetail, false);
		// 组合任务模版详细信息
		router.post("/composetask/getcomposedetail").blockingHandler(composeHandler::getComposeDetail, false);
		// 读取该组合模版的组合任务
		router.post("/composetask/getcomposetaskbymodel").blockingHandler(composeHandler::getComposeTaskByModel, false);
		// 启动组合任务
		router.post("/composetask/startcomposetask").blockingHandler(composeHandler::startComposeTask, false);
		// 读取组合任务的子任务
		router.post("/composetask/gettaskincompose").blockingHandler(composeHandler::getTaskInCompose, false);

		// 人工任务列表
		router.post("/manualtask/list").blockingHandler(manualTaskHandler::getManualTaskList, false);
		// 保存任务
		router.post("/manualtask/add").blockingHandler(manualTaskHandler::saveManualTask, false);
		// 任务状态-SWAP
		router.post("/manualtask/swap").blockingHandler(manualTaskHandler::swapTask, false);
		// 系统任务列表
		router.post("/dispatchtask/list").blockingHandler(dispatchTaskHandler::getDispatchTaskList, false);
		// 系统任务列表
		router.post("/dispatchtask/all").blockingHandler(dispatchTaskHandler::getDispatchAllTask, false);
		// 保存系统任务
		router.post("/dispatchtask/add").blockingHandler(dispatchTaskHandler::saveDispatchTask, false);
		// 生成临时任务
		router.post("/task/once").blockingHandler(commonTaskHandler::saveOnceTask, false);
		// 任务日志
		router.post("/task/log").blockingHandler(commonTaskHandler::getTaskLog, false);
		// 任务状态-PROCESSING,DONE,CANCEL
		router.post("/task/updatestatus").blockingHandler(commonTaskHandler::updateTaskStatus, false);
		// 任务状态-MODIFY
		router.post("/task/modify").blockingHandler(commonTaskHandler::modifyTask, false);
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

		// 下发终端列表
		router.post("/dispatchclient/list").blockingHandler(dispatchClientHandler::getClientList, false);
		// 下发终端注册
		router.post("/dispatchclient/registe").blockingHandler(dispatchClientHandler::registe, false);
		// 重载终端数据
		router.post("/dispatchclient/reload").blockingHandler(dispatchClientHandler::loadData, false);

		// ITOS设备号关联信息
		router.post("/associate/machinename").blockingHandler(associateItopHandler::machineNameAssociate, false);

		// 用户列表
		router.post("/user/list").blockingHandler(userHandler::getUserList, false);

		Configer.initDbPool(vertx);
		dispatchClientHandler.loadData();// 初始化DispatchClient数据
		vertx.deployVerticle(new TimerVerticle());
		vertx.deployVerticle(new WebsocketVerticle());
		vertx.createHttpServer().requestHandler(router).listen(Configer.getHttpPort());

		logger.info("ITOS server start");

	}
}
