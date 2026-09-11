package com.thanh.chat.repository.httpClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.thanh.chat.dto.ApiResponse;
import com.thanh.chat.dto.request.IntrospectRequest;
import com.thanh.chat.dto.response.IntrospectResponse;

@FeignClient(name = "identity-client", url = "${app.services.identity.url}")
public interface IdentityClient {
    @PostMapping(value = "/auth/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request);
}
