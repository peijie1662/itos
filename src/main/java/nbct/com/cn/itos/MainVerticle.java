package nbct.com.cn.itos;

import org.apache.log4j.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import nbct.com.cn.itos.config.Configer;
import nbct.com.cn.itos.handler.LoginHandler;
import nbct.com.cn.itos.handler.ModelHandler;
import nbct.com.cn.itos.handler.TaskHandler;

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

		// 登录
		router.post("/login").blockingHandler(loginHandler::handleLogin, false);
		// 模版列表
		router.post("/modellist").blockingHandler(modelHandler::getTimerTaskModelList, false);
		// 修改模版
		router.post("/modelupdate").blockingHandler(modelHandler::updateTimerTaskModel, false);
		// 删除模版
		router.post("/modeldelete").blockingHandler(modelHandler::deleteTimerTaskModel, false);
		// 增加模版
		router.post("/modeladd").blockingHandler(modelHandler::addTimerTaskModel, false);
		//任务列表
		router.post("/tasklist").blockingHandler(taskHandler::getTaskList, false);
		//可执行任务列表
		router.post("/executetasklist").blockingHandler(taskHandler::getExecuteTaskList, false);
		
		Configer.initDbPool(vertx);
		vertx.deployVerticle(new TimerVerticle());
		vertx.deployVerticle(new WebsocketVerticle());
		vertx.createHttpServer().requestHandler(router).listen(Configer.getHttpPort());
		
		logger.info("ITOS server start");

	}
}
