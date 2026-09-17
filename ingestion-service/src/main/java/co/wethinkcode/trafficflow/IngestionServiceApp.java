package co.wethinkcode.trafficflow;

import io.javalin.Javalin;
import java.util.*;
import com.opencsv.*;
import com.opencsv.exceptions.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class IngestionServiceApp {

    public static void main(String[] args) throws IOException, CsvException{
        Javalin app = Javalin.create().start(7020);

        List<Map<String,Object>> cleanedIntersections = cleanCsv("/intersections-legacy.csv");
        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/intersections" , ctx -> ctx.json(cleanedIntersections));
    }

    private static List<Map<String,Object>> cleanCsv(String path) throws IOException, CsvException {
        List<String[]> rawRecords;

        try(InputStream in = IngestionServiceApp.class.getResourceAsStream(path) ;
            CSVReader reader = new CSVReader(new InputStreamReader(in,StandardCharsets.UTF_8))){
            List<String[]> rows = reader.readAll();
            rawRecords= rows.subList(1,rows.size());
        }

        List<Map<String,Object>> cleanedRows = new ArrayList<>();
        for(String[] row: rawRecords){
            String intersectionId = cleanId(row[0]);
            String district = cleanDistict(row[1]);
            String signal = cleanSignal(row[2]);
            Boolean active = cleanActive(row[3]);

            Map<String,Object> intersection = new LinkedHashMap<>();
            intersection.put("id",intersectionId);
            intersection.put("district",district);
            intersection.put("signal",signal);
            intersection.put("active",active);

            cleanedRows.add(intersection);
        }

        return deduplicate(cleanedRows);
    }

    private static String cleanId(String id){
        return id.trim().toUpperCase();
    }

    private static String cleanDistict(String district){
        if(district == null || district.isEmpty()){
            return null;
        }
        String trimmed = district.trim().replaceAll("\\s+", " ");
        return trimmed.substring(0,1).toUpperCase() + trimmed.substring(1).toLowerCase();
    }

    private static String cleanSignal(String signal){
        if(signal.toLowerCase().equals("unknown") || signal.isEmpty()) {
            return null;}

        return signal.trim().toLowerCase();
    }

    private static Boolean cleanActive(String active){
        String cleaned = active.trim().toUpperCase();
        return switch(cleaned){
            case "YES", "Y", "TRUE", "1" -> true;
            case "NO","N","FALSE","0" -> false;
            default -> null;
        };
    }

    private static List<Map<String,Object>> deduplicate(List<Map<String,Object>> intersections){
        Map<Object,Map<String, Object>> uniqueIntersections = new LinkedHashMap<>();

        for(Map<String,Object> section: intersections){
            String id = (String) section.get("id");
            if(id!=null){
                uniqueIntersections.put(id,section);
            }
        }
        return new ArrayList<>(uniqueIntersections.values());
    }
}
