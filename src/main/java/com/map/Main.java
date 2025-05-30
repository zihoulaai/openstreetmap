package com.map;


import com.uid.UUID;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(name = "Generate", mixinStandardHelpOptions = true, version = "1.0",
        description = "根据城市名抓取地点数据并写入 SQL 文件")
public class Main implements Runnable {


    private static final String[] PLACES_TYPES = {
            "natural_feature", "park", "beach", "campground", "restaurant", "cafe", "bar", "store", "supermarket",
            "shopping_mall", "hospital", "school", "university", "library", "police", "fire_station", "post_office",
            "bus_station", "train_station", "subway_station", "airport", "parking", "movie_theater", "museum",
            "stadium", "zoo", "aquarium", "church", "mosque", "synagogue", "hindu_temple", "buddhist_temple",
            "establishment", "premise", "street_address"
    };

    @CommandLine.Option(names = {"--city", "-c"}, description = "城市名，如 Phoenix", required = true)
    private String city;

    @CommandLine.Option(names = {"--state", "-s"}, description = "州/省/邦名，如 Arizona", required = true)
    private String state;

    @CommandLine.Option(names = {"--sqlFile", "-f"}, description = "SQL 文件路径，如 Phoenix.sql", required = true)
    private String sqlFile;

    @Override
    public void run() {
        try {
            sqlFile = normalizeSqlFileName(sqlFile);
            ensureFileExists(sqlFile);

            Long cityId = new UUID().getUID();
            Position position = OpenStreetMapRequest.searchCity(city, state);
            if (position == null) {
                throw new IllegalStateException("城市查找失败: " + city + ", " + state);
            }

            String citySql = SqlGenerator.generateInsertSQL(cityId, position);
            SqlGenerator.writeToFile(citySql, sqlFile);

            fetchNearbyPlacesAndWriteToSql(cityId, position.getEndpointAddress(), sqlFile);

            System.out.println("数据生成完成，文件路径: " + new File(sqlFile).getAbsolutePath());
        } catch (Exception e) {
            System.err.println("执行失败: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    // --- 私有辅助方法 ---
    private String normalizeSqlFileName(String filename) {
        return filename.endsWith(".sql") ? filename : filename + ".sql";
    }

    private void ensureFileExists(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException("无法创建 SQL 文件: " + filePath);
            }
        }
    }

    private void fetchNearbyPlacesAndWriteToSql(Long cityId, String address, String sqlFile) throws Exception {
        List<Position> allLocations = new ArrayList<>();
        int total = PLACES_TYPES.length;

        for (int i = 0; i < total; i++) {
            String placeType = PLACES_TYPES[i];
            int percent = (i + 1) * 100 / total;

            // 打印进度条（例如：[#####-----] 42%）
            printProgressBar(percent, placeType);

            String query = String.format("%s near %s", placeType, address);
            List<Position> results = OpenStreetMapRequest.nearby(query);
            allLocations.addAll(results);
            Thread.sleep(1000); // 避免请求频率过高
        }

        System.out.println("\n所有地点抓取完成，开始生成 SQL 文件...");
        String batchSql = SqlGenerator.generateBatchInsertSQL(allLocations, cityId);
        SqlGenerator.writeToFile(batchSql, sqlFile);
        System.out.println("SQL 文件写入完成。");
    }

    private void printProgressBar(int percent, String placeType) {
        final int width = 50; // 进度条宽度
        int filled = (percent * width) / 100;
        StringBuilder bar = new StringBuilder();
        bar.append('\r'); // 回到行首，覆盖之前输出
        bar.append(String.format("抓取地点类型: %-20s [", placeType.length() > 20 ? placeType.substring(0, 20) : placeType));
        for (int i = 0; i < width; i++) {
            if (i < filled) {
                bar.append('#');
            } else {
                bar.append('-');
            }
        }
        bar.append("] ");
        bar.append(percent);
        bar.append("%");
        System.out.print(bar);
    }

}