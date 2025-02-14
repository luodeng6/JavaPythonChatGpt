package com.chatgpt;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenAIChatSimple {
    private static final String API_KEY = "sk-QGP7ircB5lgIszv3vlq2ySuYXUg9XfImqSBhr5PDulnY3Azs";
    private static final String API_BASE = "https://api.chatanywhere.com.cn/v1";
    private static final Map<String, String> DATA_DIST = Map.of(
            "user", "用户",
            "assistant", "机器人"
    );

    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        List<Map<String, String>> messages = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);

        while (true) {

            // 问题的开始
            System.out.print("输入你的问题：");
            String question = scanner.nextLine();

            if ("1".equals(question)) {
                messages.forEach(item -> System.out.printf(
                        "%s: %s%n%n",
                        DATA_DIST.get(item.get("role")),
                        item.get("content")
                ));
                System.exit(0);
            }

            // 添加用户消息
            messages.add(Map.of("role", "user", "content", question));

            // 手动构建 JSON 请求体
            String requestBody = buildRequestBody(messages);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE + "/chat/completions"))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // 发送请求并获取流式响应
            HttpResponse<InputStream> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofInputStream()
            );

            // 处理助理响应
            Map<String, String> assistantMsg = new HashMap<>();
            assistantMsg.put("role", "assistant");
            assistantMsg.put("content", "");
            processStreamResponse(response.body(), assistantMsg);

            // 添加助理消息
            messages.add(assistantMsg);
            System.out.println();
        }
    }

    // 手动构建 JSON 请求体
    private static String buildRequestBody(List<Map<String, String>> messages) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"model\":\"gpt-3.5-turbo\",\"messages\":[");

        for (Map<String, String> msg : messages) {
            String content = msg.get("content")
                    .replace("\\", "\\\\")  // 处理反斜杠
                    .replace("\"", "\\\""); // 处理双引号
            sb.append(String.format(
                    "{\"role\":\"%s\",\"content\":\"%s\"},",
                    msg.get("role"),
                    content
            ));
        }

        if (!messages.isEmpty()) {
            sb.deleteCharAt(sb.length() - 1); // 删除最后一个逗号
        }
        sb.append("],\"stream\":true}");

        return sb.toString();
    }

    // 处理流式响应
    private static void processStreamResponse(InputStream inputStream, Map<String, String> assistantMsg) {
        Pattern contentPattern = Pattern.compile("\"content\":\"(.*?)\"");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    String json = line.substring(6).trim();
                    if ("[DONE]".equals(json)) break;

                    Matcher matcher = contentPattern.matcher(json);
                    if (matcher.find()) {
                        String content = matcher.group(1)
                                .replace("\\\"", "\"")  // 处理转义双引号
                                .replace("\\\\", "\\"); // 处理转义反斜杠

                        System.out.print(content);
                       /// System.out.println("\n");
                        assistantMsg.put("content",
                                assistantMsg.get("content") + content
                        );
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}