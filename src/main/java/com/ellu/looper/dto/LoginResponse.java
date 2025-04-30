package com.ellu.looper.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LoginResponse {
  private String accessToken;
  private boolean isNewUser;
}