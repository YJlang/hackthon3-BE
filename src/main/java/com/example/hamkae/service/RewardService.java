package com.example.hamkae.service;

import com.example.hamkae.DTO.RewardRequestDTO;
import com.example.hamkae.DTO.RewardResponseDTO;
import com.example.hamkae.domain.Reward;
import com.example.hamkae.domain.RewardPin;
import com.example.hamkae.domain.User;
import com.example.hamkae.repository.RewardRepository;
import com.example.hamkae.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * 상품권 교환 관련 비즈니스 로직을 처리하는 서비스 클래스
 * 
 * @author 권오윤
 * @version 1.0
 * @since 2025-08-18
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RewardService {

    private final RewardRepository rewardRepository;
    private final UserRepository userRepository;
    

    /**
     * 사용자의 포인트를 상품권으로 교환합니다.
     * 
     * @param userId 사용자 ID
     * @param requestDTO 교환 요청 정보
     * @return 교환 완료 응답
     * @throws IllegalStateException 포인트가 부족하거나 잘못된 요청인 경우
     */
    public RewardResponseDTO exchangePointsForReward(Long userId, RewardRequestDTO requestDTO) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

        // 요청 검증 및 매핑
        Reward.RewardType rewardType = parseRewardType(requestDTO.getRewardType());
        int quantity = validateAndGetQuantity(requestDTO.getQuantity());

        int totalPointsToUse = rewardType.getAmount() * quantity;
        if (!user.hasEnoughPoints(totalPointsToUse)) {
            throw new IllegalStateException("보유 포인트가 부족합니다. 현재: " + user.getPoints() + ", 필요: " + totalPointsToUse);
        }

        // 포인트 차감
        user.usePoints(totalPointsToUse);

        // 상품권 교환 요청 생성
        Reward reward = Reward.builder()
                .user(user)
                .pointsUsed(totalPointsToUse)
                .rewardType(rewardType)
                .quantity(quantity)
                .build();

        // 핀번호 생성 및 연결
        List<RewardPin> pins = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            RewardPin pin = RewardPin.builder()
                    .pinNumber(generatePinNumber())
                    .build();
            pin.setReward(reward);
            pins.add(pin);
        }
        reward.setPins(pins);

        // 상태를 즉시 승인으로 설정 (자동 처리)
        reward.approve();

        // 저장
        Reward savedReward = rewardRepository.save(reward);
        userRepository.save(user);

        log.info("사용자 {}가 {} 타입 {}개를 교환했습니다. 총 차감 포인트: {}, 교환ID: {}", 
                userId, requestDTO.getRewardType(), quantity, totalPointsToUse, savedReward.getId());

        RewardResponseDTO response = RewardResponseDTO.from(savedReward);
        // 교환 응답에서는 상태/시간을 숨긴다
        response.setStatus(null);
        response.setCreatedAt(null);
        response.setProcessedAt(null);
        return response;
    }

    /**
     * 사용자의 상품권 교환 요청 목록을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 상품권 교환 요청 목록
     */
    @Transactional(readOnly = true)
    public List<RewardResponseDTO> getUserRewards(Long userId) {
        return rewardRepository.findByUserId(userId)
                .stream()
                .map(RewardResponseDTO::from)
                .toList();
    }

    /**
     * 사용자 소유의 특정 교환 내역 단건을 조회합니다.
     */
    @Transactional(readOnly = true)
    public RewardResponseDTO getUserRewardById(Long userId, Long rewardId) {
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new IllegalArgumentException("교환 내역을 찾을 수 없습니다."));
        if (reward.getUser() == null || reward.getUser().getId() == null || !reward.getUser().getId().equals(userId)) {
            throw new SecurityException("해당 교환 내역에 대한 권한이 없습니다.");
        }
        return RewardResponseDTO.from(reward);
    }

    /**
     * 상품권 타입을 검증합니다.
     * 
     * @param rewardType 검증할 상품권 타입
     * @throws IllegalArgumentException 잘못된 상품권 타입인 경우
     */
    private Reward.RewardType parseRewardType(String rewardType) {
        if (rewardType == null || rewardType.trim().isEmpty()) {
            throw new IllegalArgumentException("상품권 타입을 입력해주세요.");
        }
        String normalized = rewardType.trim().toUpperCase();
        switch (normalized) {
            case "FIVE_THOUSAND":
                return Reward.RewardType.FIVE_THOUSAND;
            case "TEN_THOUSAND":
                return Reward.RewardType.TEN_THOUSAND;
            case "THIRTY_THOUSAND":
                return Reward.RewardType.THIRTY_THOUSAND;
            default:
                throw new IllegalArgumentException("지원하지 않는 상품권 타입입니다. (FIVE_THOUSAND, TEN_THOUSAND, THIRTY_THOUSAND)");
        }
    }

    private int validateAndGetQuantity(Integer quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException("교환할 개수를 입력해주세요.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("교환할 상품권 개수는 1개 이상이어야 합니다.");
        }
        if (quantity > 50) {
            throw new IllegalArgumentException("한 번에 교환할 수 있는 최대 개수는 50개입니다.");
        }
        return quantity;
    }

    /**
     * 고유한 핀 번호를 생성합니다.
     * 형식: XXXX-XXXX-XXXX-XXXX (16자리 숫자)
     * 
     * @return 생성된 핀 번호
     */
    private String generatePinNumber() {
        SecureRandom random = new SecureRandom();
        StringBuilder pinBuilder = new StringBuilder();
        
        for (int i = 0; i < 4; i++) {
            if (i > 0) {
                pinBuilder.append("-");
            }
            // 4자리 숫자 생성
            int segment = 1000 + random.nextInt(9000);
            pinBuilder.append(segment);
        }
        
        return pinBuilder.toString();
    }
}
