package com.example.hamkae.controller;

import com.example.hamkae.DTO.ApiResponse;
import com.example.hamkae.DTO.RewardRequestDTO;
import com.example.hamkae.DTO.RewardResponseDTO;
import com.example.hamkae.service.RewardService;
import com.example.hamkae.config.JwtUtil;
import com.example.hamkae.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 상품권 교환 관련 HTTP 요청을 처리하는 컨트롤러 클래스
 * 
 * @author 권오윤
 * @version 1.0
 * @since 2025-08-18
 */
@RestController
@RequestMapping("/rewards")
@RequiredArgsConstructor
@Slf4j
public class RewardController {

    private final RewardService rewardService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    /**
     * 사용자의 포인트를 상품권으로 교환합니다.
     * POST /rewards
     * 
     * @param requestDTO 교환 요청 정보
     * @return 교환 완료 응답
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RewardResponseDTO>> exchangePointsForReward(
            @RequestHeader("Authorization") String authorization,
            @RequestBody RewardRequestDTO requestDTO) {
        
        try {
            Long userId = extractUserIdFromToken(authorization);
            log.info("상품권 교환 요청: 사용자 {}, 타입 {}, 개수 {}", 
                    userId, requestDTO.getRewardType(), requestDTO.getQuantity());

            RewardResponseDTO response = rewardService.exchangePointsForReward(userId, requestDTO);

            ApiResponse<RewardResponseDTO> apiResponse = ApiResponse.<RewardResponseDTO>builder()
                    .success(true)
                    .message("교환 요청 완료")
                    .data(response)
                    .build();

            return ResponseEntity.ok(apiResponse);

        } catch (IllegalArgumentException e) {
            log.warn("상품권 교환 요청 검증 실패: {}", e.getMessage());
            
            ApiResponse<RewardResponseDTO> apiResponse = ApiResponse.<RewardResponseDTO>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(apiResponse);

        } catch (IllegalStateException e) {
            log.warn("상품권 교환 요청 처리 실패: {}", e.getMessage());
            
            ApiResponse<RewardResponseDTO> apiResponse = ApiResponse.<RewardResponseDTO>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(apiResponse);

        } catch (Exception e) {
            log.error("상품권 교환 요청 처리 중 오류 발생", e);
            
            ApiResponse<RewardResponseDTO> apiResponse = ApiResponse.<RewardResponseDTO>builder()
                    .success(false)
                    .message("서버 오류가 발생했습니다.")
                    .build();

            return ResponseEntity.internalServerError().body(apiResponse);
        }
    }

    /**
     * 사용자의 상품권 교환 요청 목록을 조회합니다.
     * GET /rewards?userId={userId}
     * 
     * @param userId 사용자 ID
     * @return 상품권 교환 요청 목록
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RewardResponseDTO>>> getUserRewards(
            @RequestHeader("Authorization") String authorization) {
        try {
            Long userId = extractUserIdFromToken(authorization);
            log.info("사용자 {}의 상품권 교환 요청 목록 조회", userId);

            List<RewardResponseDTO> rewards = rewardService.getUserRewards(userId);

            ApiResponse<List<RewardResponseDTO>> apiResponse = ApiResponse.<List<RewardResponseDTO>>builder()
                    .success(true)
                    .message("조회 완료")
                    .data(rewards)
                    .build();

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            log.error("상품권 교환 요청 목록 조회 중 오류 발생", e);
            
            ApiResponse<List<RewardResponseDTO>> apiResponse = ApiResponse.<List<RewardResponseDTO>>builder()
                    .success(false)
                    .message("서버 오류가 발생했습니다.")
                    .build();

            return ResponseEntity.internalServerError().body(apiResponse);
        }
    }

    /**
     * 특정 교환 내역 단건 조회
     * GET /rewards/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RewardResponseDTO>> getUserRewardById(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") Long id) {
        try {
            Long userId = extractUserIdFromToken(authorization);
            log.info("사용자 {}의 교환 내역 단건 조회: {}", userId, id);

            RewardResponseDTO reward = rewardService.getUserRewardById(userId, id);

            ApiResponse<RewardResponseDTO> apiResponse = ApiResponse.<RewardResponseDTO>builder()
                    .success(true)
                    .message("조회 완료")
                    .data(reward)
                    .build();
            return ResponseEntity.ok(apiResponse);
        } catch (SecurityException e) {
            ApiResponse<RewardResponseDTO> apiResponse = ApiResponse.<RewardResponseDTO>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.status(403).body(apiResponse);
        } catch (IllegalArgumentException e) {
            ApiResponse<RewardResponseDTO> apiResponse = ApiResponse.<RewardResponseDTO>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(apiResponse);
        } catch (Exception e) {
            log.error("교환 내역 단건 조회 중 오류 발생", e);
            ApiResponse<RewardResponseDTO> apiResponse = ApiResponse.<RewardResponseDTO>builder()
                    .success(false)
                    .message("서버 오류가 발생했습니다.")
                    .build();
            return ResponseEntity.internalServerError().body(apiResponse);
        }
    }

    private Long extractUserIdFromToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("유효하지 않은 토큰 형식입니다.");
        }
        String token = authorizationHeader.substring(7);
        String username = jwtUtil.validateAndGetUsername(token);
        if (username == null) {
            throw new IllegalArgumentException("토큰이 유효하지 않습니다.");
        }
        return userRepository.findByUsername(username)
                .map(user -> user.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}
