package co.wethinkcode.trafficflow;

import io.javalin.Javalin;
import java.util.*;
import com.opencsv.*;
import com.opencsv.exceptions.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class IngestionServiceApp {

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7020);

        List<Map<String,Object>> cleanedIntersections = cleanCsv("/intersections.csv")
        app.get("/health", ctx -> ctx.result("OK"));
        app.get("intersections" , ctx -> ctx.json(cleanedIntersections));

        // TODO: read and clean src/main/resources/intersections-legacy.csv (intersections, districts, signal types data —
        // trim whitespace, fix casing, normalize dates/booleans) and expose the
        // cleaned records here for the other services to consume.
    }

    private static List<Map<String,Object>> cleanCsv(String path){
        List<String[]> rawRecords;

        try(InputStream in = IngestionServiceApp.class.getResourcesAsStream(path));
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

    private static String cleanDistict(String district){}

    private static String cleanSignal(String signal){}

    private static Boolean cleanActive(String active){}
}
