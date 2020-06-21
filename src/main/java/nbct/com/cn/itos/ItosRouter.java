package nbct.com.cn.itos;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import nbct.com.cn.itos.handler.AppInfoHandler;
import nbct.com.cn.itos.handler.AssociateItopHandler;
import nbct.com.cn.itos.handler.CommonTaskHandler;
import nbct.com.cn.itos.handler.CompareHandler;
import nbct.com.cn.itos.handler.ComposeHandler;
import nbct.com.cn.itos.handler.DispatchClientHandler;
import nbct.com.cn.itos.handler.DocumentHandler;
import nbct.com.cn.itos.handler.FirstPageHandler;
import nbct.com.cn.itos.handler.ManualTaskHandler;
import nbct.com.cn.itos.handler.ModelHandler;
import nbct.com.cn.itos.handler.PdfHandler;
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
		// 0.模版
		router.post("/get").blockingHandler(modelHandler::getTimerTaskModel, false);
		// 1.模版列表
		router.post("/list").blockingHandler(modelHandler::getTimerTaskModelList, false);
		// 2.修改模版
		router.post("/update").blockingHandler(modelHandler::updateTimerTaskModel, false);
		// 3.删除模版
		router.post("/delete").blockingHandler(modelHandler::deleteTimerTaskModel, false);
		// 4.增加模版
		router.post("/add").blockingHandler(modelHandler::addTimerTaskModel, false);
		// 5.模版文件上传
		router.post("/uploadfile").blockingHandler(uploadHandler::uploadModelFile, false);
		// 6.模版状态改变
		router.post("/status").blockingHandler(modelHandler::chgModelStatus, false);
		// 7.组合任务模版
		router.post("/composelist").blockingHandler(modelHandler::getComposeModelList, false);
		// 8.非组合任务模版
		router.post("/notcomposelist").blockingHandler(modelHandler::getNotComposeModelList, false);
		// 9.修改模版分组信息
		router.post("/updategroup").blockingHandler(modelHandler::chgModelGroup, false);
		// 10.添加分组
		router.post("/addgroup").blockingHandler(modelHandler::addGroup, false);
		// 11.删除分组
		router.post("/delgroup").blockingHandler(modelHandler::delGroup, false);
		// 12.分组列表
		router.post("/grouplist").blockingHandler(modelHandler::getGroups, false);
		// 13.分组排序
		router.post("/sortgroup").blockingHandler(modelHandler::sortingGroup, false);
		return router;
	}

	/**
	 * 组合任务路由
	 */
	public static Router composeRouter(Vertx vertx, ComposeHandler composeHandler) {
		Router router = Router.router(vertx);
		// 1.保存组合任务模版详细信息
		router.post("/savecomposedetail").blockingHandler(composeHandler::saveComposeModelDetail, false);
		// 2.组合任务模版详细信息
		router.post("/getcomposedetail").blockingHandler(composeHandler::getComposeDetail, false);
		// 3.读取该组合模版的组合任务
		router.post("/getcomposetaskbymodel").blockingHandler(composeHandler::getComposeTaskByModel, false);
		// 4.启动组合任务
		router.post("/startcomposetask").blockingHandler(composeHandler::startComposeTask, false);
		// 5.读取组合任务的子任务
		router.post("/gettaskincompose").blockingHandler(composeHandler::getTaskInCompose, false);
		// 6.删除组合任务
		router.post("/deletecomposetask").blockingHandler(composeHandler::deleteComposeTask, false);
		return router;
	}

	/**
	 * 人工任务路由
	 */
	public static Router manualtaskRouter(Vertx vertx, ManualTaskHandler manualTaskHandler) {
		Router router = Router.router(vertx);
		// 1.人工任务列表
		router.post("/list").blockingHandler(manualTaskHandler::getManualTaskList, false);
		// 2.保存任务
		router.post("/add").blockingHandler(manualTaskHandler::saveManualTask, false);
		// 3.任务状态-SWAP
		router.post("/swap").blockingHandler(manualTaskHandler::swapTask, false);
		// 4.新委外记录
		router.post("/newdr").blockingHandler(manualTaskHandler::saveDeliverRepair, false);
		// 5.修改委外记录
		router.post("/updatedr").blockingHandler(manualTaskHandler::updateDeliverRepair, false);
		// 6. 删除委外记录
		router.post("/deletedr").blockingHandler(manualTaskHandler::deleteDeliverRepair, false);
		// 7.任务委外列表
		router.post("/drlist").blockingHandler(manualTaskHandler::getDeliverRepair, false);
		return router;
	}

	/**
	 * 任务通用路由
	 */
	public static Router commonTaskRouter(Vertx vertx, CommonTaskHandler commonTaskHandler,
			UploadHandler uploadHandler) {
		Router router = Router.router(vertx);
		// 0.任务
		router.post("/get").blockingHandler(commonTaskHandler::getTask, false);
		// 1.生成临时任务
		router.post("/once").blockingHandler(commonTaskHandler::saveOnceTask, false);
		// 2.在时间周期内生成任务
		router.post("/period").blockingHandler(commonTaskHandler::savePeriodTask);
		// 3.任务日志
		router.post("/log").blockingHandler(commonTaskHandler::getTaskLog, false);
		// 4.任务状态-PROCESSING,DONE,CANCEL
		router.post("/updatestatus").blockingHandler(commonTaskHandler::updateTaskStatus, false);
		// 5.任务状态-MODIFY
		router.post("/modify").blockingHandler(commonTaskHandler::modifyTask, false);
		// 6.任务图片上传
		router.post("/uploadfile").blockingHandler(uploadHandler::uploadTaskFile, false);
		return router;
	}

	/**
	 * 智能提示路由
	 */
	public static Router smarttipsRouter(Vertx vertx, SettingsHandler settingsHandler) {
		Router router = Router.router(vertx);
		// 1.智能提示列表
		router.post("/list").blockingHandler(settingsHandler::getSmartTipsList, false);
		// 2.添加智能提示
		router.post("/add").blockingHandler(settingsHandler::addSmartTips, false);
		// 3.修改智能提示
		router.post("/update").blockingHandler(settingsHandler::updateSmartTips, false);
		// 4.删除智能提示
		router.post("/delete").blockingHandler(settingsHandler::deleteSmartTips, false);
		return router;
	}

	/**
	 * 任务终端路由
	 */
	public static Router dispatchclientRouter(Vertx vertx, DispatchClientHandler dispatchClientHandler) {
		Router router = Router.router(vertx);
		// 1.登记终端
		router.post("/add").blockingHandler(dispatchClientHandler::addClient, false);
		// 2.修改终端
		router.post("/update").blockingHandler(dispatchClientHandler::updateClient, false);
		// 3.删除终端
		router.post("/delete").blockingHandler(dispatchClientHandler::deleteClient, false);
		// 4.查找终端（终端访问）
		router.post("/get").blockingHandler(dispatchClientHandler::getClient, false);
		// 5.终端列表
		router.post("/list").blockingHandler(dispatchClientHandler::getClientList, false);
		// 6.重载终端数据
		router.post("/reload").blockingHandler(dispatchClientHandler::loadData, false);
		// 7.终端对应任务列表（终端访问）
		router.post("/clienttask").blockingHandler(dispatchClientHandler::getDispatchTaskList, false);
		// 8.所有任务列表
		router.post("/alltask").blockingHandler(dispatchClientHandler::getDispatchAllTask, false);
		return router;
	}

	/**
	 * 关联路由
	 */
	public static Router associateRouter(Vertx vertx, AssociateItopHandler associateItopHandler) {
		Router router = Router.router(vertx);
		// 1.测试
		router.get("/test").blockingHandler(associateItopHandler::test, false);
		// 2.ITOS设备号关联信息
		router.post("/machinename").blockingHandler(associateItopHandler::machineNameAssociate, false);
		return router;
	}

	/**
	 * 文档维护路由
	 */
	public static Router documentRouter(Vertx vertx, DocumentHandler documentHandler) {
		Router router = Router.router(vertx);
		// 1.文档上传
		router.post("/upload").blockingHandler(documentHandler::uploadDocument, false);
		// 2.删除文档
		router.post("/del").blockingHandler(documentHandler::delDocument, false);
		// 3.修改文档
		router.post("/update").blockingHandler(documentHandler::updateDocument, false);
		// 4.文档列表
		router.post("/list").blockingHandler(documentHandler::getDocumentList, false);
		// 5.新建分组
		router.post("/newgroup").blockingHandler(documentHandler::newGroup, false);
		// 6.删除分组
		router.post("/delgroup").blockingHandler(documentHandler::delGroup, false);
		// 7.分组列表
		router.post("/grouplist").blockingHandler(documentHandler::getGroupList, false);
		// 8.新建分类
		router.post("/newcategory").blockingHandler(documentHandler::newCategory, false);
		// 9.删除分类
		router.post("/delcategory").blockingHandler(documentHandler::delCategory, false);
		// 10.分类列表
		router.post("/categorylist").blockingHandler(documentHandler::getCategoryList, false);
		return router;
	}

	/**
	 * 用户维护路由
	 */
	public static Router userRouter(Vertx vertx, UserHandler userHandler, UploadHandler uploadHandler) {
		Router router = Router.router(vertx);
		// 1.用户列表
		router.post("/list").blockingHandler(userHandler::getUserList, false);
		// 2.用户权限
		router.post("/authority").blockingHandler(userHandler::updateAuthority, false);
		// 3.用户信息
		router.post("/content").blockingHandler(userHandler::updateContent, false);
		// 4.删除用户
		router.post("/delete").blockingHandler(userHandler::delUser, false);
		// 5.新用户
		router.post("/add").blockingHandler(userHandler::saveUser, false);
		// 6.用户头像
		router.post("/face").blockingHandler(uploadHandler::uploadUserFace, false);
		// 7.用户首页
		router.post("/firstpage").blockingHandler(userHandler::updateFirstPage, false);
		// 8.修改密码
		router.post("/password").blockingHandler(userHandler::updatePassword, false);
		// 9.在线用户
		router.post("/onlineusers").blockingHandler(userHandler::onlineUsers, false);
		//10.短信订阅
		router.post("/subscription").blockingHandler(userHandler::updateSubscription, false);
		//11.短信主题
		router.post("/topiclist").blockingHandler(userHandler::smsTopicList, false);
		return router;
	}

	/**
	 * 首页路由
	 */
	public static Router pageRouter(Vertx vertx, FirstPageHandler firstPageHandler) {
		Router router = Router.router(vertx);
		// 1.值班表
		router.post("/duty/list").blockingHandler(firstPageHandler::getDutyList, false);
		// 2.添加值班
		router.post("/duty/add").blockingHandler(firstPageHandler::addDuty, false);
		// 3.删除值班
		router.post("/duty/delete").blockingHandler(firstPageHandler::delDuty, false);
		// 4.日志
		router.post("/itoslog").blockingHandler(firstPageHandler::getItosLog, false);
		return router;
	}

	/**
	 * PDF路由
	 */
	public static Router pdfRouter(Vertx vertx, PdfHandler pdfHandler, UploadHandler uploadHandler) {
		Router router = Router.router(vertx);
		// 1.组合任务报告
		router.post("/compose").blockingHandler(pdfHandler::getComposePdfReport, false);

		return router;
	}

	/**
	 * 服务信息路由
	 */
	public static Router appInfoRouter(Vertx vertx, AppInfoHandler appInfoHandler) {
		Router router = Router.router(vertx);
		// 1.设置遗产服务信息
		router.post("/setlegacy").blockingHandler(appInfoHandler::setLegacyAppInfo, false);
		// 2.服务信息列表
		router.post("/list").blockingHandler(appInfoHandler::getAppInfoList, false);
		return router;
	}

	/**
	 * 比对路由
	 */
	public static Router compareRouter(Vertx vertx, CompareHandler compareHandler) {
		Router router = Router.router(vertx);
		// 1.页端访问列表
		router.post("/pagecomparelist").blockingHandler(compareHandler::pageCompareList, false);
		// 2.添加比对项
		router.post("/add").blockingHandler(compareHandler::addCompareItem, false);
		// 3.修改对比项
		router.post("/update").blockingHandler(compareHandler::updateCompareItem, false);
		// 4.删除对比项
		router.post("/del").blockingHandler(compareHandler::delCompareItem, false);
		// 5.客户端访问列表
		router.post("/clientcomparelist").blockingHandler(compareHandler::clientCompareList, false);
		// 6.客户端更新比对信息
		router.post("/refresh").blockingHandler(compareHandler::clientRefresh, false);
		return router;
	}

}
