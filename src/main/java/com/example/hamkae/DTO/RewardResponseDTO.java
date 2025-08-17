package com.example.hamkae.DTO;

import com.example.hamkae.domain.Reward;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상품권 교환 조회를 위한 응답 DTO
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardResponseDTO {

    /**
     * 상품권 교환 요청 고유 식별자
     */
    private Long id;

    /**
     * 교환에 사용된 총 포인트 수량
     */
    private Integer pointsUsed;

    /**
     * 교환한 상품권 금액 타입 (예: 5천원, 만원, 3만원)
     */
    private String rewardType;

    /**
     * 교환한 상품권 개수
     */
    private Integer quantity;

    /**
     * 교환 요청의 처리 상태
     */
    private String status;

    /**
     * 교환 요청일시
     */
    private LocalDateTime createdAt;

    /**
     * 교환 처리 완료일시
     */
    private LocalDateTime processedAt;

    /**
     * 발급된 핀번호 목록
     */
    private List<String> pinNumbers;

    /**
     * Reward 엔티티를 RewardResponseDTO로 변환하는 정적 팩토리 메서드
     * 
     * @param reward 변환할 Reward 엔티티
     * @return RewardResponseDTO 객체
     */
    public static RewardResponseDTO from(Reward reward) {
        return RewardResponseDTO.builder()
                .id(reward.getId())
                .pointsUsed(reward.getPointsUsed())
                .rewardType(reward.getRewardType() == null ? null : reward.getRewardType().name())
                .quantity(reward.getQuantity())
                .status(reward.getStatus().name())
                .createdAt(reward.getCreatedAt())
                .processedAt(reward.getProcessedAt())
                .pinNumbers(reward.getPins() == null ? List.of() : reward.getPins().stream()
                        .map(p -> p.getPinNumber())
                        .collect(Collectors.toList()))
                .build();
    }
}
