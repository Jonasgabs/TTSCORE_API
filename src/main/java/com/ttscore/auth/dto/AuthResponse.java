package com.ttscore.auth.dto;

import com.ttscore.user.dto.UserResponse;

public record AuthResponse(String token, UserResponse user) {}
