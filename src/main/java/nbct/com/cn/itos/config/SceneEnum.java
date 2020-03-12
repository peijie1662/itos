package nbct.com.cn.itos.config;

import java.util.Arrays;
import java.util.Optional;

/**
* @author PJ 
* @version 创建时间：2019年12月31日 上午11:05:05
*/
public enum SceneEnum {
	
	SYSLOG("itos.sys.log","SYSLOG", "系统日志"),
	ONLINEUSER("itos.sys.users","ONLINEUSER","用户在线"),
	CONTROLCENTER("itos.controlcenter.msg","CONTROLCENTER","控制中心");

	private String addr;//地址
	
	private String abs;//简写

	private String desc;//描述

	private SceneEnum(String addr,String abs, String desc) {
		this.addr = addr;
		this.abs = abs;
		this.desc = desc;
	}

	public String addr() {
		return addr;
	}
	
	public String abs() {
		return abs;
	}

	public String desc() {
		return desc;
	}

	public boolean eq(String s) {
		return s.equals(this.addr());
	}

	public static Optional<SceneEnum> from(String s) {
		return Arrays.asList(SceneEnum.values()).stream().filter(item -> {
			return item.eq(s);
		}).findAny();
	}
	
	public boolean absEq(String s) {
		return s.equals(this.abs());
	}

	public static Optional<SceneEnum> absFrom(String s) {
		return Arrays.asList(SceneEnum.values()).stream().filter(item -> {
			return item.absEq(s);
		}).findAny();
	}

}
