package com.example.hamkae.controller;

import com.example.hamkae.DTO.PointHistoryResponseDTO;
import com.example.hamkae.DTO.PointResponseDTO;
import com.example.hamkae.config.JwtUtil;
import com.example.hamkae.domain.User;
import com.example.hamkae.repository.UserRepository;
import com.example.hamkae.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
@Slf4j
public class PointController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PointService pointService;

    /**
     * 사용자 포인트 및 통계 조회 API
     * GET /points?userId={userId}
     *
     * @param userId 조회할 사용자 ID
     * @return 포인트 정보 응답
     */

    /**
     * JWT 토큰에서 유저 정보를 추출해 포인트를 조회합니다.
     *
     * @param authorization JWT 인증 토큰
     * @return 유저 포인트 정보
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getPoints(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        log.info("포인트 조회 API 호출");

        // 1. 인증 토큰 검사
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "인증 토큰이 필요합니다."
            ));
        }

        // 2. 토큰 파싱 및 유효성 검사
        String token = authorization.substring(7);
        String username = jwtUtil.validateAndGetUsername(token); // JWT에서 username 추출
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "유효하지 않은 토큰입니다."
            ));
        }

        // 3. 유저 조회
        Long userId = userRepository.findByUsername(username)
                .map(User::getId)
                .orElse(null);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "사용자를 찾을 수 없습니다."
            ));
        }

        // 4. 포인트 조회
        try {
            PointResponseDTO pointData = pointService.getUserPoints(userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", pointData
            ));

        } catch (IllegalArgumentException e) {
            log.error("포인트 조회 실패 - userId: {}, error: {}", userId, e.getMessage());

            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "포인트 조회 실패: " + e.getMessage()
            ));
        }
    }


    /**
     * 포인트 적립/사용 내역 조회 API
     * GET /points/history?userId={userId}
     *
     * @param userId 조회할 사용자 ID
     * @return 포인트 이력 응답
     */
    /**
     * JWT 토큰에서 사용자 정보를 추출하여 포인트 이력을 조회합니다.
     *
     * @param authorization JWT 인증 토큰
     * @return 포인트 이력 데이터
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getPointHistory(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        log.info("포인트 이력 조회 API 호출");

        // 1. 토큰 검사
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "인증 토큰이 필요합니다."
            ));
        }

        // 2. 토큰에서 username 추출
        String token = authorization.substring(7);
        String username = jwtUtil.validateAndGetUsername(token);
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "유효하지 않은 토큰입니다."
            ));
        }

        // 3. username으로 userId 조회
        Long userId = userRepository.findByUsername(username)
                .map(User::getId)
                .orElse(null);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "사용자를 찾을 수 없습니다."
            ));
        }

        // 4. 포인트 이력 조회
        try {
            List<PointHistoryResponseDTO> historyData = pointService.getPointHistory(userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", historyData
            ));

        } catch (IllegalArgumentException e) {
            log.error("포인트 이력 조회 실패 - userId: {}, error: {}", userId, e.getMessage());

            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "포인트 이력 조회 실패: " + e.getMessage()
            ));
        }
    }

}
