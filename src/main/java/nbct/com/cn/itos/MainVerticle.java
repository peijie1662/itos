package nbct.com.cn.itos;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import static nbct.com.cn.itos.ConfigVerticle.CONFIG;
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
import nbct.com.cn.itos.handler.SysCodeHandler;
import nbct.com.cn.itos.handler.UploadHandler;
import nbct.com.cn.itos.handler.UserHandler;

public class MainVerticle extends AbstractVerticle {

	public static Logger log = LogManager.getLogger(MainVerticle.class);

	@Override
	public void start() throws Exception {
		Router router = Router.router(vertx);
		router.route().handler(CorsHandler.create("*")//
				.allowedMethod(HttpMethod.GET)//
				.allowedMethod(HttpMethod.OPTIONS)//
				.allowedMethod(HttpMethod.POST)//
				.allowedHeader("X-PINGARUNER")//
				.allowedHeader("Content-Type"));
		router.route().handler(BodyHandler.create());
		ModelHandler modelHandler = new ModelHandler();
		CommonTaskHandler commonTaskHandler = new CommonTaskHandler();
		ManualTaskHandler manualTaskHandler = new ManualTaskHandler();
		SettingsHandler settingsHandler = new SettingsHandler();
		UploadHandler uploadHandler = new UploadHandler();
		DispatchClientHandler dispatchClientHandler = new DispatchClientHandler();
		AssociateItopHandler associateItopHandler = new AssociateItopHandler();
		ComposeHandler composeHandler = new ComposeHandler();
		UserHandler userHandler = new UserHandler();
		FirstPageHandler firstPageHandler = new FirstPageHandler();
		PdfHandler pdfHandler = new PdfHandler();
		AppInfoHandler appInfoHandler = new AppInfoHandler();
		DocumentHandler documentHandler = new DocumentHandler();
		CompareHandler compareHandler = new CompareHandler();
		SysCodeHandler sysCodeHandler = new SysCodeHandler();
		//
		Future.future(promise -> {
			vertx.deployVerticle(new ConfigVerticle(), r -> {
				if (r.succeeded())
					promise.complete();
				else
					promise.fail("启动Confige组件失败");
			});
		}).compose(re -> {
			return Future.future(promise -> {
				vertx.deployVerticle(new TimerVerticle(), r -> {
					if (r.succeeded())
						promise.complete();
					else
						promise.fail("启动Timer组件失败");
				});
			});
		}).compose(re -> {
			return Future.future(promise -> {
				vertx.deployVerticle(new WebsocketVerticle(), r -> {
					if (r.succeeded())
						promise.complete();
					else
						promise.fail("启动Websocket组件失败");
				});
			});
		}).compose(re -> {
			return Future.future(promise -> {
				vertx.deployVerticle(new NotifyVerticle(), r -> {
					if (r.succeeded())
						promise.complete();
					else
						promise.fail("启动Notify组件失败");
				});
			});
		}).onSuccess(r -> {
			// 0.初始化DispatchClient数据
			dispatchClientHandler.loadData();
			// 1.静态文件
			router.route("/itosfile/*").handler(StaticHandler.create(CONFIG.getString("uploadDir")));
			// 2.登录
			router.post("/login").blockingHandler(userHandler::handleLogin, false);
			// 3.任务模版
			router.mountSubRouter("/model", ItosRouter.modelRouter(vertx, modelHandler, uploadHandler));
			// 4.组合任务
			router.mountSubRouter("/composetask", ItosRouter.composeRouter(vertx, composeHandler));
			// 5.人工任务
			router.mountSubRouter("/manualtask", ItosRouter.manualtaskRouter(vertx, manualTaskHandler));
			// 6.任务通用
			router.mountSubRouter("/task", ItosRouter.commonTaskRouter(vertx, commonTaskHandler, uploadHandler));
			// 7.智能提示
			router.mountSubRouter("/smarttips", ItosRouter.smarttipsRouter(vertx, settingsHandler));
			// 8.下发终端
			router.mountSubRouter("/dispatchclient", ItosRouter.dispatchclientRouter(vertx, dispatchClientHandler));
			// 9.关联信息
			router.mountSubRouter("/associate", ItosRouter.associateRouter(vertx, associateItopHandler));
			// 10.用户
			router.mountSubRouter("/user", ItosRouter.userRouter(vertx, userHandler, uploadHandler));
			// 11.首页
			router.mountSubRouter("/page", ItosRouter.pageRouter(vertx, firstPageHandler));
			// 12.PDF
			router.mountSubRouter("/pdf", ItosRouter.pdfRouter(vertx, pdfHandler, uploadHandler));
			// 13.APP
			router.mountSubRouter("/appinfo", ItosRouter.appInfoRouter(vertx, appInfoHandler));
			// 14.文档管理
			router.mountSubRouter("/document", ItosRouter.documentRouter(vertx, documentHandler));
			// 15.文件比对
			router.mountSubRouter("/compare", ItosRouter.compareRouter(vertx, compareHandler));
			// 16.系统代码
			router.mountSubRouter("/syscode", ItosRouter.sysCodeRouter(vertx, sysCodeHandler));
			//
			Integer port = CONFIG.getJsonObject("provider").getInteger("port");
			vertx.createHttpServer().requestHandler(router).listen(port);
			log.info("ITOS服务启动成功");
		}).onFailure(r -> {
			log.error("ITOS服务启动失败", r.getCause());
		});

	}
}
