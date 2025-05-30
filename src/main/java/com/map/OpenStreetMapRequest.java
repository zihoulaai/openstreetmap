package com.map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class OpenStreetMapRequest {

    private static final String BASE_URL = "https://nominatim.openstreetmap.org/search";
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; OpenStreetMapRequest/1.0)";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(OpenStreetMapRequest.class);

    /**
     * 根据城市和州名称搜索位置，返回第一个匹配项
     */
    public static Position searchCity(String city, String state) {
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("format", "json"));
            params.add(new BasicNameValuePair("polygon_geojson", "1"));
            params.add(new BasicNameValuePair("city", city));
            params.add(new BasicNameValuePair("state", state));

            List<Position> positions = sendRequest(params);
            return positions.isEmpty() ? null : positions.get(0);

        } catch (Exception e) {
            logger.info("搜索城市失败: {}, {}，错误信息: {}", city, state, e.getMessage());
            return null;
        }
    }

    /**
     * 根据关键词搜索多个位置
     */
    public static List<Position> nearby(String query) {
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("format", "json"));
            params.add(new BasicNameValuePair("polygon_geojson", "1"));
            params.add(new BasicNameValuePair("q", query));
            return sendRequest(params);
        } catch (Exception e) {
            logger.error("搜索关键字失败: {}, 错误信息: {}", query, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 发送 GET 请求并返回解析的位置信息
     */
    private static List<Position> sendRequest(List<NameValuePair> params) throws Exception {
        // 强制使用系统 DNS 解析器
        System.setProperty("sun.net.spi.nameservice.provider.1", "dns,sun");
        System.setProperty("http.proxyHost", "127.0.0.1");
        System.setProperty("http.proxyPort", "7890");
        System.setProperty("https.proxyHost", "127.0.0.1");
        System.setProperty("https.proxyPort", "7890");

        List<Position> results = new ArrayList<>();

        HttpClient client = HttpClients.createDefault();

        URIBuilder uriBuilder = new URIBuilder(OpenStreetMapRequest.BASE_URL);
        if (params != null) {
            for (NameValuePair param : params) {
                uriBuilder.addParameter(param.getName(), param.getValue());
            }
        }
        HttpGet request = new HttpGet(uriBuilder.build());
        request.setHeader("User-Agent", USER_AGENT);

        try (ClassicHttpResponse response = client.executeOpen(null, request, null)) {
            int statusCode = response.getCode();
            if (statusCode != 200) {
                throw new RuntimeException("请求失败，状态码: " + statusCode);
            }
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            JsonNode jsonArray = objectMapper.readTree(responseBody);
            for (JsonNode node : jsonArray) {
                results.add(new Position(
                        node.get("name").asText(),
                        node.get("display_name").asText(),
                        node.get("lon").asDouble(),
                        node.get("lat").asDouble()
                ));
            }
        }
        return results;
    }

    public static void main(String[] args) {
        Position cityPosition = searchCity("San Francisco", "California");
        if (cityPosition != null) {
            System.out.println("City Position: " + cityPosition.getEndpointAddress() +
                    " (Lat: " + cityPosition.getEndpointLatitude() +
                    ", Lon: " + cityPosition.getEndpointLongitude() + ")");
        } else {
            System.out.println("未找到城市位置");
        }
    }
}