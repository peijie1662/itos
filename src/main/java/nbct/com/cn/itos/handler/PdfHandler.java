package nbct.com.cn.itos.handler;

import java.io.FileOutputStream;
import java.util.List;

import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPRow;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import static nbct.com.cn.itos.model.CallResult.OK;
import static nbct.com.cn.itos.model.CallResult.Err;
import static nbct.com.cn.itos.ConfigVerticle.CONFIG;

/**
 * @author PJ
 * @version 创建时间：2020年4月14日 下午12:43:24
 */
public class PdfHandler {

	/**
	 * 组合任务报表<br>
	 * 报表数据由前台传入<br>
	 * 即时生成并下载
	 */
	public void getComposePdfReport(RoutingContext ctx) {
		HttpServerResponse res = ctx.response();
		JsonObject rp = ctx.getBodyAsJson();
		String composeId = rp.getString("composeId");
		Document doc = new Document();
		try {
			FileSystem fs = ctx.vertx().fileSystem();
			String path = CONFIG.getString("uploadDir") + "pdf/compose/";
			if (!fs.existsBlocking(path)) {
				fs.mkdirsBlocking(path);
			}
			if (!fs.existsBlocking(path + composeId + ".pdf")) {
				fs.createFileBlocking(path + composeId + ".pdf");
			}
			PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(path + composeId + ".pdf"));
			doc.open();
			BaseFont baseFont = BaseFont.createFont("simkai.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
			Font font = new Font(baseFont, 11, Font.NORMAL);
			Font tFont = new Font(baseFont, 16, Font.BOLD);
			JsonObject composeTask = rp.getJsonObject("composeTask");
			JsonArray details = rp.getJsonArray("details");
			// 1.组合任务简介
			Paragraph abs = new Paragraph("ITOS任务报告 ：" + composeTask.getString("abs"), tFont);
			abs.setAlignment(Paragraph.ALIGN_CENTER);
			doc.add(abs);
			// 2.起止时间
			Paragraph period = new Paragraph(
					"开始时间 ：" + composeTask.getString("bgDt") + "   -   结束时间：" + composeTask.getString("edDt"), font);
			period.setAlignment(Paragraph.ALIGN_LEFT);
			doc.add(period);
			// 3.组合任务内容
			String content = composeTask.getString("content");
			doc.add(new Paragraph("任务内容：" + content, font));
			// 4.子任务列表
			PdfPTable table = new PdfPTable(6);
			table.setSpacingBefore(10);
			table.setWidthPercentage(100);
			table.setTotalWidth(new float[] { 50, 180, 100, 120, 200, 100 });
			List<PdfPRow> rows = table.getRows();
			// 4.1标题栏
			PdfPCell title_cells[] = new PdfPCell[6];
			PdfPRow title_row = new PdfPRow(title_cells);
			title_cells[0] = new PdfPCell(new Paragraph("步骤", font));
			title_cells[1] = new PdfPCell(new Paragraph("任务简介", font));
			title_cells[2] = new PdfPCell(new Paragraph("执行终端", font));
			title_cells[3] = new PdfPCell(new Paragraph("IP", font));
			title_cells[4] = new PdfPCell(new Paragraph("起至时间", font));
			title_cells[5] = new PdfPCell(new Paragraph("执行结果", font));
			rows.add(title_row);
			// 4.2表格内容
			details.stream().forEach(item -> {
				PdfPCell row_cells[] = new PdfPCell[6];
				PdfPRow table_row = new PdfPRow(row_cells);
				JsonObject jo = JsonObject.mapFrom(item);
				JsonObject task = jo.getJsonObject("task");
				row_cells[0] = new PdfPCell(new Paragraph(jo.getInteger("composeLevel").toString(), font));
				row_cells[1] = new PdfPCell(new Paragraph(task.getString("abs"), font));
				row_cells[2] = new PdfPCell(new Paragraph(task.getString("terminal"), font));
				row_cells[3] = new PdfPCell(new Paragraph(task.getString("ip"), font));
				row_cells[4] = new PdfPCell(
						new Paragraph(task.getString("bgDtStr") + "  -  " + task.getString("edDtStr"), font));
				row_cells[5] = new PdfPCell(new Paragraph(task.getString("status"), font));
				rows.add(table_row);
			});
			// 5.表格尾部  //TODO
			doc.add(table);
			doc.close();
			writer.close();
			res.end(OK());
		} catch (Exception e) {
			e.printStackTrace();
			res.end(Err(e.getMessage()));
		}
	}
	
	/**
	 * 每日简报
	 */
	public void createDailyPdf() {
		
	}

}
