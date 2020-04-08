package nbct.com.cn.itos;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import nbct.com.cn.itos.handler.AssociateItopHandler;
import nbct.com.cn.itos.handler.CommonTaskHandler;
import nbct.com.cn.itos.handler.ComposeHandler;
import nbct.com.cn.itos.handler.DispatchClientHandler;
import nbct.com.cn.itos.handler.FirstPageHandler;
import nbct.com.cn.itos.handler.ManualTaskHandler;
import nbct.com.cn.itos.handler.ModelHandler;
import nbct.com.cn.itos.handler.SettingsHandler;
import nbct.com.cn.itos.handler.UploadHandler;
import nbct.com.cn.itos.handler.UserHandler;

/**
 * @author PJ
 * @version 创建时间：2020年3月7日 上午11:04:46 类说明
 */
public class ItosRouter {

	/**
	 * 模版维护路由
	 */
	public static Router modelRouter(Vertx vertx, ModelHandler modelHandler, UploadHandler uploadHandler) {
		Router router = Router.router(vertx);
		// 模版列表
		router.post("/list").blockingHandler(modelHandler::getTimerTaskModelList, false);
		// 修改模版
		router.post("/update").blockingHandler(modelHandler::updateTimerTaskModel, false);
		// 删除模版
		router.post("/delete").blockingHandler(modelHandler::deleteTimerTaskModel, false);
		// 增加模版
		router.post("/add").blockingHandler(modelHandler::addTimerTaskModel, false);
		// 模版文件上传
		router.post("/uploadfile").blockingHandler(uploadHandler::uploadModelFile, false);
		// 模版状态改变
		router.post("/status").blockingHandler(modelHandler::chgModelStatus, false);
		// 组合任务模版
		router.post("/composelist").blockingHandler(modelHandler::getComposeModelList, false);
		// 非组合任务模版
		router.post("/notcomposelist").blockingHandler(modelHandler::getNotComposeModelList, false);
		// 修改模版分组信息
		router.post("/updategroup").blockingHandler(modelHandler::chgModelGroup, false);
		// 添加分组
		router.post("/addgroup").blockingHandler(modelHandler::addGroup, false);
		// 删除分组
		router.post("/delgroup").blockingHandler(modelHandler::delGroup, false);
		// 分组列表
		router.post("/grouplist").blockingHandler(modelHandler::getGroups, false);
		return router;
	}

	/**
	 * 组合任务路由
	 */
	public static Router composeRouter(Vertx vertx, ComposeHandler composeHandler) {
		Router router = Router.router(vertx);
		// 保存组合任务模版详细信息
		router.post("/savecomposedetail").blockingHandler(composeHandler::saveComposeModelDetail, false);
		// 组合任务模版详细信息
		router.post("/getcomposedetail").blockingHandler(composeHandler::getComposeDetail, false);
		// 读取该组合模版的组合任务
		router.post("/getcomposetaskbymodel").blockingHandler(composeHandler::getComposeTaskByModel, false);
		// 启动组合任务
		router.post("/startcomposetask").blockingHandler(composeHandler::startComposeTask, false);
		// 读取组合任务的子任务
		router.post("/gettaskincompose").blockingHandler(composeHandler::getTaskInCompose, false);
		return router;
	}

	/**
	 * 人工任务路由
	 */
	public static Router manualtaskRouter(Vertx vertx, ManualTaskHandler manualTaskHandler) {
		Router router = Router.router(vertx);
		// 人工任务列表
		router.post("/list").blockingHandler(manualTaskHandler::getManualTaskList, false);
		// 保存任务
		router.post("/add").blockingHandler(manualTaskHandler::saveManualTask, false);
		// 任务状态-SWAP
		router.post("/swap").blockingHandler(manualTaskHandler::swapTask, false);
		return router;
	}

	/**
	 * 任务通用路由
	 */
	public static Router commonTaskRouter(Vertx vertx, CommonTaskHandler commonTaskHandler,
			UploadHandler uploadHandler) {
		Router router = Router.router(vertx);
		// 生成临时任务
		router.post("/once").blockingHandler(commonTaskHandler::saveOnceTask, false);
		// 任务日志
		router.post("/log").blockingHandler(commonTaskHandler::getTaskLog, false);
		// 任务状态-PROCESSING,DONE,CANCEL
		router.post("/updatestatus").blockingHandler(commonTaskHandler::updateTaskStatus, false);
		// 任务状态-MODIFY
		router.post("/modify").blockingHandler(commonTaskHandler::modifyTask, false);
		// 任务图片上传
		router.post("/uploadfile").blockingHandler(uploadHandler::uploadTaskFile, false);
		return router;
	}

	/**
	 * 智能提示路由
	 */
	public static Router smarttipsRouter(Vertx vertx, SettingsHandler settingsHandler) {
		Router router = Router.router(vertx);
		// 智能提示列表
		router.post("/list").blockingHandler(settingsHandler::getSmartTipsList, false);
		// 添加智能提示
		router.post("/add").blockingHandler(settingsHandler::addSmartTips, false);
		// 修改智能提示
		router.post("/update").blockingHandler(settingsHandler::updateSmartTips, false);
		// 删除智能提示
		router.post("/delete").blockingHandler(settingsHandler::deleteSmartTips, false);
		return router;
	}

	/**
	 * 任务终端路由
	 */
	public static Router dispatchclientRouter(Vertx vertx, DispatchClientHandler dispatchClientHandler) {
		Router router = Router.router(vertx);
		// 登记终端
		router.post("/add").blockingHandler(dispatchClientHandler::addClient, false);
		// 修改终端
		router.post("/update").blockingHandler(dispatchClientHandler::updateClient, false);
		// 删除终端
		router.post("/delete").blockingHandler(dispatchClientHandler::deleteClient, false);
		// 查找终端（终端访问）
		router.post("/get").blockingHandler(dispatchClientHandler::getClient, false);
		// 终端列表
		router.post("/list").blockingHandler(dispatchClientHandler::getClientList, false);
		// 重载终端数据
		router.post("/reload").blockingHandler(dispatchClientHandler::loadData, false);
		// 终端对应任务列表（终端访问）
		router.post("/clienttask").blockingHandler(dispatchClientHandler::getDispatchTaskList, false);
		// 所有任务列表
		router.post("/alltask").blockingHandler(dispatchClientHandler::getDispatchAllTask, false);
		return router;
	}

	/**
	 * 关联路由
	 */
	public static Router associateRouter(Vertx vertx, AssociateItopHandler associateItopHandler) {
		Router router = Router.router(vertx);
		// ITOS设备号关联信息
		router.post("/machinename").blockingHandler(associateItopHandler::machineNameAssociate, false);
		return router;
	}

	/**
	 * 用户维护路由
	 */
	public static Router userRouter(Vertx vertx, UserHandler userHandler, UploadHandler uploadHandler) {
		Router router = Router.router(vertx);
		// 用户列表
		router.post("/list").blockingHandler(userHandler::getUserList, false);
		// 用户权限
		router.post("/authority").blockingHandler(userHandler::updateAuthority, false);
		// 用户信息
		router.post("/content").blockingHandler(userHandler::updateContent, false);
		// 删除用户
		router.post("/delete").blockingHandler(userHandler::delUser, false);
		// 新用户
		router.post("/add").blockingHandler(userHandler::saveUser, false);
		// 用户头像
		router.post("/face").blockingHandler(uploadHandler::uploadUserFace, false);
		// 用户首页
		router.post("/firstpage").blockingHandler(userHandler::updateFirstPage, false);
		// 修改密码
		router.post("/password").blockingHandler(userHandler::updatePassword, false);
		// 在线用户
		router.get("/onlineusers").blockingHandler(userHandler::onlineUsers, false);
		return router;
	}

	/**
	 * 首页路由
	 */
	public static Router pageRouter(Vertx vertx, FirstPageHandler firstPageHandler) {
		Router router = Router.router(vertx);
		// 值班表
		router.post("/duty/list").blockingHandler(firstPageHandler::getDutyList, false);
		// 添加值班
		router.post("/duty/add").blockingHandler(firstPageHandler::addDuty, false);
		// 删除值班
		router.post("/duty/delete").blockingHandler(firstPageHandler::delDuty, false);
		return router;
	}

}
