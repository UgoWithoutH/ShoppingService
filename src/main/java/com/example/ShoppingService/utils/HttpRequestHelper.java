package com.example.ShoppingService.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

public class HttpRequestHelper {

    @Autowired
    private WebClient webClient;

    public <T>ResponseEntity<T> get(String uri,Class<T> resultClass, HashMap<String, Object> queryParams){
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(uri);

        if(queryParams != null) {
            for (Map.Entry<String, Object> entrySet : queryParams.entrySet()) {
                uriBuilder.queryParam(entrySet.getKey(), entrySet.getValue());
            }
        }

        return webClient
                .get()
                .uri(uriBuilder.build().toUri())
                .retrieve()
                .toEntity(resultClass)
                .block();
    }

    public <T>ResponseEntity<T> post(String uri,Class<T> resultClass, HashMap<String, Object> queryParams){
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(uri);

        if(queryParams != null) {
            for (Map.Entry<String, Object> entrySet : queryParams.entrySet()) {
                uriBuilder.queryParam(entrySet.getKey(), entrySet.getValue());
            }
        }

        return webClient
                .post()
                .uri(uriBuilder.build().toUri())
                .retrieve()
                .toEntity(resultClass)
                .block();
    }

    public <T>ResponseEntity<T> patch(String uri,Class<T> resultClass, HashMap<String, Object> queryParams){
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(uri);

        if(queryParams != null) {
            for (Map.Entry<String, Object> entrySet : queryParams.entrySet()) {
                uriBuilder.queryParam(entrySet.getKey(), entrySet.getValue());
            }
        }

        return webClient
                .patch()
                .uri(uriBuilder.build().toUri())
                .retrieve()
                .toEntity(resultClass)
                .block();
    }
}
