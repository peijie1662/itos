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
import nbct.com.cn.itos.handler.FirstPageHandler;
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

		//1.静态文件
		router.route("/itosfile/*").handler(StaticHandler.create(Configer.uploadDir));
		//2.登录
		router.post("/login").blockingHandler(userHandler::handleLogin, false);
		//3.任务模版
		router.mountSubRouter("/model", ItosRouter.modelRouter(vertx,modelHandler,uploadHandler));
		//4.组合任务
		router.mountSubRouter("/composetask", ItosRouter.composeRouter(vertx, composeHandler));
		//5.人工任务
		router.mountSubRouter("/manualtask", ItosRouter.manualtaskRouter(vertx, manualTaskHandler));
		//6.任务通用
		router.mountSubRouter("/task", ItosRouter.commonTaskRouter(vertx, commonTaskHandler, uploadHandler));
		//7.智能提示
		router.mountSubRouter("/smarttips", ItosRouter.smarttipsRouter(vertx, settingsHandler));
		//8.下发终端
		router.mountSubRouter("/dispatchclient", ItosRouter.dispatchclientRouter(vertx, dispatchClientHandler));
		//9.关联信息
		router.mountSubRouter("/associate", ItosRouter.associateRouter(vertx, associateItopHandler));
		//10.用户
		router.mountSubRouter("/user", ItosRouter.userRouter(vertx, userHandler, uploadHandler));
		//11.首页
		router.mountSubRouter("/page", ItosRouter.pageRouter(vertx, firstPageHandler));		

		Configer.initDbPool(vertx);
		dispatchClientHandler.loadData();// 初始化DispatchClient数据
		vertx.deployVerticle(new TimerVerticle());
		vertx.deployVerticle(new WebsocketVerticle());
		vertx.createHttpServer().requestHandler(router).listen(Configer.getHttpPort());

		logger.info("ITOS server start");

	}
}
