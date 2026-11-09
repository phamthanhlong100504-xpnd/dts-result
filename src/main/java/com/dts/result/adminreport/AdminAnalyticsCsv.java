package com.dts.result.adminreport;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/** Export aggregate source-owned values only. Excel formula injection is suppressed. */
public final class AdminAnalyticsCsv {
    private AdminAnalyticsCsv(){}
    public static String cell(Object item){
        String s=item==null?"":String.valueOf(item).replace("\r"," ").replace("\n"," ");
        String leading=s.stripLeading();
        if(!leading.isEmpty() && "=+-@".indexOf(leading.charAt(0))>=0)s="'"+s;
        return "\""+s.replace("\"","\"\"")+"\"";
    }
    public static String line(Object... cells){
        var entries=new String[cells.length];
        for(int i=0;i<cells.length;i++)entries[i]=cell(cells[i]);
        return String.join(",",entries)+"\r\n";
    }
    public static byte[] encode(String csv){return ("\ufeff"+csv).getBytes(StandardCharsets.UTF_8);}
    public static String overview(Map<String,Object> metadata, Map<String,Object> summary,
                                  List<Map<String,Object>> trend, List<Map<String,Object>> breakdown){
        var b=new StringBuilder();
        b.append(line("Báo cáo kết quả DTS","Nguồn",metadata.get("sourceService")));
        b.append(line("Từ",metadata.get("from"),"Đến",metadata.get("to")));
        b.append(line("Hạng GPLX",metadata.get("licenseClass"),"Hoạt động",metadata.get("module"),"Loại thi",metadata.get("examMode")));
        b.append(line("Trạng thái dữ liệu",metadata.get("dataStatus"),"Cập nhật",metadata.get("asOf")));
        b.append(line("Chỉ số","Giá trị"));
        for(String key:List.of("completedAttempts","passedAttempts","failedAttempts","passRate","averageNormalizedScore","activeLearners"))
            b.append(line(key,summary.get(key)));
        b.append(line("Xu hướng ngày","Phiên hoàn thành","Phiên đạt","Tỷ lệ đạt"));
        for(var row:trend)b.append(line(row.get("date"),row.get("completedAttempts"),row.get("passedAttempts"),row.get("passRate")));
        b.append(line("Nhóm nguồn","Phiên hoàn thành","Phiên đạt","Tỷ lệ đạt"));
        for(var row:breakdown)b.append(line(row.get("sourceGroup"),row.get("completedAttempts"),row.get("passedAttempts"),row.get("passRate")));
        return b.toString();
    }
    public static String exp(Map<String,Object> metadata,List<Map<String,Object>> sources){
        var b=new StringBuilder();b.append(line("Báo cáo EXP DTS","Nguồn",metadata.get("sourceService")));
        b.append(line("Từ",metadata.get("from"),"Đến",metadata.get("to")));
        b.append(line("Hạng GPLX",metadata.get("licenseClass"),"Hoạt động",metadata.get("module")));
        b.append(line("Trạng thái",metadata.get("dataStatus"),"Tổng EXP",metadata.get("totalCreditedExp")));
        b.append(line("Phân hệ","Loại hoạt động","Lượt ghi EXP","EXP đã ghi"));
        for(var row:sources)b.append(line(row.get("module"),row.get("activity_type"),row.get("credited_activities"),row.get("credited_exp")));
        return b.toString();
    }
}
