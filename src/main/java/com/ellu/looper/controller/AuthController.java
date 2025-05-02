package com.ellu.looper.controller;

import com.ellu.looper.commons.ApiResponse;
import com.ellu.looper.commons.CurrentUser;
import com.ellu.looper.dto.AuthResponse;
import com.ellu.looper.dto.LogoutRequest;
import com.ellu.looper.dto.NicknameRequest;
import com.ellu.looper.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

  private final AuthService authService;

  private final String clientId = "44153da72adabc7e47959244ebe53bac"; // 카카오 REST API 키
  private final String redirectUri = "http://localhost:3000/auth/kakao/callback";

  @GetMapping("/auth/kakao/callback")
  public ResponseEntity<?> kakaoCallback(@RequestParam("code") String code, HttpServletResponse response) {
    String accessToken = requestAccessToken(code);

    // 로그인/회원가입 처리
    AuthResponse authResponse = authService.loginOrSignUp("kakao", accessToken);

    // 응답에 쿠키로 토큰 설정
    authService.setTokenCookies(response, authResponse.getRefreshToken());

    return ResponseEntity.ok(new ApiResponse("로그인 성공", null));
  }


  private String requestAccessToken(String code) {
    RestTemplate restTemplate = new RestTemplate();
    String tokenUrl = "https://kauth.kakao.com/oauth/token";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "authorization_code");
    params.add("client_id", clientId);
    params.add("redirect_uri", redirectUri);
    params.add("code", code);

    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

    try {
      ResponseEntity<String> response =
          restTemplate.exchange(tokenUrl, HttpMethod.POST, requestEntity, String.class);

      log.info("Response from Kakao token API: {}", response.getBody());

      if (response.getStatusCode() == HttpStatus.OK) {
        // JSON 파싱
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> resultMap = mapper.readValue(response.getBody(), Map.class);
        return (String) resultMap.get("access_token");
      } else {
        throw new RuntimeException(
            "Failed to get access token from Kakao: " + response.getStatusCode());
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error during Kakao access token request", e);
    }
  }

  @PostMapping("/auth/token")
  public ResponseEntity<ApiResponse<AuthResponse>> kakaoLogin(@RequestBody Map<String, String> request, HttpServletResponse response) {
    String code = request.get("code");
    if (code == null || code.isBlank()) {
      return ResponseEntity.badRequest().body(new ApiResponse("code가 필요합니다.", null));
    }

    // Kakao access token 요청
    String accessToken = requestAccessToken(code);

    // 로그인/회원가입 처리
    AuthResponse authResponse = authService.loginOrSignUp("kakao", accessToken);

    // RefreshToken을 쿠키로 설정
    authService.setTokenCookies(response, authResponse.getRefreshToken());

    ResponseEntity<ApiResponse<AuthResponse>> responseEntity= ResponseEntity.ok(ApiResponse.success("로그인 성공", authResponse));

    // ObjectMapper는 보통 Bean으로 등록되어 있으니 주입받거나 새로 생성할 수 있음
    ObjectMapper objectMapper = new ObjectMapper();

    try {
      String json = objectMapper.writeValueAsString(responseEntity.getBody());
      log.info("응답 내용: {}", json);
    } catch (Exception e) {
      log.error("응답 내용 로깅 중 오류 발생", e);
    }

    return responseEntity;
  }


  @DeleteMapping("/auth/token")
  public ResponseEntity<ApiResponse<Void>> logout(@RequestBody LogoutRequest request, HttpServletResponse response) {
    authService.logout(request.getRefreshToken());

    Cookie refreshCookie = new Cookie("refresh_token", null);
    refreshCookie.setPath("/");
    refreshCookie.setHttpOnly(true);
    refreshCookie.setMaxAge(0);

    response.addCookie(refreshCookie);
    return ResponseEntity.ok(new ApiResponse("로그아웃 성공", null));
  }

  @PostMapping("/auth/users/nickname")
  public ResponseEntity<?> registerNickname(
      @CurrentUser Long userId, @RequestBody NicknameRequest request) {
    authService.registerNickname(userId, request.getNickname());
    return ResponseEntity.ok(new ApiResponse("닉네임 등록 완료", null));
  }

  @PostMapping("/auth/token/refresh")
  public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String refreshTokenHeader) {
    String refreshToken = refreshTokenHeader.replace("Bearer ", "");
    return ResponseEntity.ok(authService.refreshAccessToken(refreshToken));
  }
}