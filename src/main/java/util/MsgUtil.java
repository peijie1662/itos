package util;

import java.util.List;
import java.util.Objects;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import nbct.com.cn.itos.config.SceneEnum;
import nbct.com.cn.itos.model.ItosMsg;

/**
 * @author PJ
 * @version 创建时间：2020年1月12日 下午5:21:22
 */
public class MsgUtil {
	
	/**
	 * 系统日志场景，如果同时是组合任务，那么也是控制中心场景 
	 */
	public static <T> void mixLC(Vertx vertx, T msg, String composeId) {
		MsgUtil.sysLog(vertx,msg);
		if (Objects.nonNull(composeId)) {
			MsgUtil.controlCenter(vertx, msg);
		}
	}

	/**
	 * 系统日志场景
	 */
	public static <T> void sysLog(Vertx vertx, T msg) {
		ItosMsg m = null;
		if (msg instanceof String) {
			m = ItosMsg.from(SceneEnum.SYSLOG.abs(), msg.toString());
		} else if (msg instanceof Integer) {
			m = ItosMsg.from(SceneEnum.SYSLOG.abs(), Integer.parseInt(msg.toString()));
		} else if (msg instanceof List) {
			m = ItosMsg.from(SceneEnum.SYSLOG.abs(), (List<?>) msg);
		} else {
			throw new RuntimeException("消息体类型不支持");
		}
		vertx.eventBus().send(SceneEnum.SYSLOG.addr(), m.json());
	}
	
	/**
	 * 控制中心场景
	 */
	public static <T> void controlCenter(Vertx vertx, T msg) {
		ItosMsg m = null;
		if (msg instanceof String) {
			m = ItosMsg.from(SceneEnum.CONTROLCENTER.abs(), msg.toString());
		} else if (msg instanceof Integer) {
			m = ItosMsg.from(SceneEnum.CONTROLCENTER.abs(), Integer.parseInt(msg.toString()));
		} else if (msg instanceof List) {
			m = ItosMsg.from(SceneEnum.CONTROLCENTER.abs(), (List<?>) msg);
		} else {
			throw new RuntimeException("消息体类型不支持");
		}
		vertx.eventBus().send(SceneEnum.CONTROLCENTER.addr(), m.json());
	}
	
	/**
	 * 在线用户场景
	 */
	public static <T> void onlineUser(Vertx vertx, T msg) {
		ItosMsg m = null;
		if (msg instanceof String) {
			m = ItosMsg.from(SceneEnum.ONLINEUSER.abs(), msg.toString());
		} else if (msg instanceof Integer) {
			m = ItosMsg.from(SceneEnum.ONLINEUSER.abs(), Integer.parseInt(msg.toString()));
		} else if (msg instanceof List) {
			m = ItosMsg.from(SceneEnum.ONLINEUSER.abs(), (List<?>) msg);
		} else {
			throw new RuntimeException("消息体类型不支持");
		}
		vertx.eventBus().send(SceneEnum.ONLINEUSER.addr(), m.json());
	}
	
	/**
	 * 系统日志场景，如果同时是组合任务，那么也是控制中心场景 
	 */
	public static <T> void mixLC(RoutingContext ctx, T msg, String composeId) {
		MsgUtil.sysLog(ctx,msg);
		if (Objects.nonNull(composeId)) {
			MsgUtil.controlCenter(ctx, msg);
		}
	}

	/**
	 * 系统日志场景
	 */
	public static <T> void sysLog(RoutingContext ctx, T msg) {
		ItosMsg m = null;
		if (msg instanceof String) {
			m = ItosMsg.from(SceneEnum.SYSLOG.abs(), msg.toString());
		} else if (msg instanceof Integer) {
			m = ItosMsg.from(SceneEnum.SYSLOG.abs(), Integer.parseInt(msg.toString()));
		} else if (msg instanceof List) {
			m = ItosMsg.from(SceneEnum.SYSLOG.abs(), (List<?>) msg);
		} else {
			throw new RuntimeException("消息体类型不支持");
		}
		ctx.vertx().eventBus().send(SceneEnum.SYSLOG.addr(), m.json());
	}
	
	/**
	 * 控制中心场景
	 */
	public static <T> void controlCenter(RoutingContext ctx, T msg) {
		ItosMsg m = null;
		if (msg instanceof String) {
			m = ItosMsg.from(SceneEnum.CONTROLCENTER.abs(), msg.toString());
		} else if (msg instanceof Integer) {
			m = ItosMsg.from(SceneEnum.CONTROLCENTER.abs(), Integer.parseInt(msg.toString()));
		} else if (msg instanceof List) {
			m = ItosMsg.from(SceneEnum.CONTROLCENTER.abs(), (List<?>) msg);
		} else {
			throw new RuntimeException("消息体类型不支持");
		}
		ctx.vertx().eventBus().send(SceneEnum.CONTROLCENTER.addr(), m.json());
	}
	
	/**
	 * 在线用户场景
	 */
	public static <T> void onlineUser(RoutingContext ctx, T msg) {
		ItosMsg m = null;
		if (msg instanceof String) {
			m = ItosMsg.from(SceneEnum.ONLINEUSER.abs(), msg.toString());
		} else if (msg instanceof Integer) {
			m = ItosMsg.from(SceneEnum.ONLINEUSER.abs(), Integer.parseInt(msg.toString()));
		} else if (msg instanceof List) {
			m = ItosMsg.from(SceneEnum.ONLINEUSER.abs(), (List<?>) msg);
		} else {
			throw new RuntimeException("消息体类型不支持");
		}
		ctx.vertx().eventBus().send(SceneEnum.ONLINEUSER.addr(), m.json());
	}

}
