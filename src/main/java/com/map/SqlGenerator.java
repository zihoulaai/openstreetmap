package com.map;

import com.uid.UUID;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SqlGenerator {

    public static String generateBatchInsertSQL(List<Position> records, Long cityId) {
        UUID uuid = new UUID();
        // Format the current date and time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder sqlBuilder = new StringBuilder();
        String createTime = LocalDateTime.now().format(formatter);

        sqlBuilder.append("insert into \n")
                .append("  g_vehicle_operation_position (\n")
                .append("    id, \n")
                .append("    vehicle_position_id, \n")
                .append("    endpoint_address, \n")
                .append("    endpoint_longitude, \n")
                .append("    endpoint_latitude, \n")
                .append("    create_time\n")
                .append("  )\n")
                .append("values\n");

        for (int i = 0; i < records.size(); i++) {
            String endpointAddress = records.get(i).getEndpointAddress().replace("'", "''");
            double endpointLongitude = records.get(i).getEndpointLongitude();
            double endpointLatitude = records.get(i).getEndpointLatitude();
            sqlBuilder.append(String.format(
                    "  ('%s', '%s', '%s', %.6f, %.6f, '%s')%s\n",
                    uuid.getUID(), cityId, endpointAddress, endpointLongitude, endpointLatitude, createTime,
                    (i < records.size() - 1) ? "," : ";"));
        }
        return sqlBuilder.toString();
    }

    public static String generateInsertSQL(Long id, Position position) {
        // INSERT INTO g_vehicle_position (id, name, city_code, address, lat, lng, deleted, status, order_num, create_by, create_time, update_by, update_time, remark, max_car_purchase_qty, coordinate_move_time) VALUES('1223340993866944512', 'Atlanta', 'GA', 'Atlanta', '33.7544657', '-84.3898151', 0, '1', 0, 'bfmadmin01', '2025-05-29 15:47:51', 'bfmadmin01', '2025-05-29 15:47:51', NULL, 10, 5);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String createTime = LocalDateTime.now().format(formatter);
        return "INSERT INTO g_vehicle_position (id, name, city_code, address, lat, lng, deleted, status, order_num, create_by, create_time,  max_car_purchase_qty, coordinate_move_time) VALUES (" +
                "'" + id + "', " +
                "'" + position.getName().replace("'", "''") + "', " +
                "'" + position.getName().replace("'", "''")  + "', " +
                "'" + position.getEndpointAddress().replace("'", "''") + "', " +
                "'" + position.getEndpointLatitude() + "', " +
                "'" + position.getEndpointLongitude() + "', " +
                "0, '1', 0, 'bfmadmin01', '" + createTime + "', 10, 5);";
    }

    public static void writeToFile(String content, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(content);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}
