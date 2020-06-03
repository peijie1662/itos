package util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.Objects;

import nbct.com.cn.itos.config.CycleEnum;
import nbct.com.cn.itos.model.TimerTaskModel;

/**
 * @author PJ
 * @version 创建时间：2020年5月27日 上午8:26:53
 */
public class ModelUtil {

	/**
	 * 判断是否可以生成新任务
	 */
	public static boolean couldCreateTask(TimerTaskModel model, LocalDateTime at) {
		// 1.未扫描过的有周期模版
		boolean valid = (!model.getCycle().eq("NONE")) && (model.getScanDate() == null);
		// 2.已有扫描时间，检查模版是否符合生成新任务条件
		if (model.getScanDate() != null) {
			LocalDate cd = at.toLocalDate();
			LocalDateTime mt = model.getScanDate();
			LocalDate md = mt.toLocalDate();
			CycleEnum mc = model.getCycle();
			// 2.1扫描每日任务,当前日期>标记时间的日期，就需要生成新任务。
			valid = valid || (mc == CycleEnum.PERDAY && md.isBefore(cd));
			// 2.2扫描每周任务，当前日期的年+第几周>标记时间的年+第几周，就需要生成新任务。
			int cw = cd.getYear() + cd.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
			int rw = md.getYear() + md.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
			valid = valid || (mc == CycleEnum.PERWEEK && cw > rw);
			// 2.3扫描每月任务，当前日期的年+第几月>标记时间的年+第几月，就需要生成新任务。
			int cm = cd.getYear() + cd.getMonthValue();
			int rm = md.getYear() + md.getMonthValue();
			valid = valid || (mc == CycleEnum.PERMONTH && cm > rm);
			// 2.4扫描循环任务，当前时间-间隔时间(秒)>=标记时间，就需要生成新任务。
			valid = valid || (mc == CycleEnum.CIRCULAR
					&& mt.isBefore(at.minusSeconds(Integer.parseInt(model.getPlanDates()) - 1)));
		}
		// 3.循环任务不能超过开始时间
		if (valid && model.getCycle() == CycleEnum.CIRCULAR) {
			valid = Objects.isNull(model.getStartDate()) || model.getStartDate().isBefore(LocalDateTime.now());
		}
		return valid;
	}

}
