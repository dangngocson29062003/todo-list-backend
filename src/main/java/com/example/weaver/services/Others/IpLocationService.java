package com.example.weaver.services.Others;

import com.example.weaver.dtos.others.results.LocationResult;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IpLocationService {
    private final RestTemplate restTemplate;
    private static final String API_URL = "http://ip-api.com/batch";

    public IpLocationService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(3000);
        restTemplate = new RestTemplate(factory);
    }


    public Map<String, LocationResult> resolve(List<String> ips) {
        if (ips == null || ips.isEmpty()) {
            return new ConcurrentHashMap<>();
        }
        ips = ips.stream().filter(this::isPublicIp).toList();

        Map<String, LocationResult> results = new ConcurrentHashMap<>();

        try {
            List<Map<String, String>> requestBody = new ArrayList<>();
            for (String ip : ips) {
                Map<String, String> entry = new HashMap<>();
                entry.put("query", ip);
                entry.put("fields", "city,country");
                requestBody.add(entry);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<List<Map<String, String>>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<List<Map<String, Object>>> response =
                    restTemplate.exchange(
                            API_URL,
                            HttpMethod.POST,
                            request,
                            new ParameterizedTypeReference<>() {
                            }
                    );

            List<Map<String, Object>> body = response.getBody();
            if (body == null) {
                return results;
            }

            for (int i = 0; i < body.size(); i++) {
                Map<String, Object> item = body.get(i);
                String city = (String) item.getOrDefault("city", "Unknown");
                String country = (String) item.getOrDefault("country", "Unknown");

                String ip = ips.get(i);

                results.put(ip, new LocationResult(city, country));
            }
        } catch (Exception e) {
            System.err.println("Error occurred while resolving ip locations: " + e.getMessage());
        }

        return results;
    }

    private boolean isPublicIp(String ip) {
        return !(ip.startsWith("192.168.")
                || ip.startsWith("10.")
                || ip.startsWith("127.")
                || ip.startsWith("172.16."));
    }
}