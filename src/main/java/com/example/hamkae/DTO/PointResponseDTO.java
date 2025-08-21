package com.example.hamkae.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointResponseDTO {

    /**
     * 현재 보유 포인트
     */
    private Integer currentPoints;

    /**
     * 총 적립한 포인트
     */
    private Integer totalEarned;

    /**
     * 총 사용한 포인트
     */
    private Integer totalUsed;

    /**
     * User 엔티티와 포인트 통계로부터 PointResponseDTO를 생성하는 정적 팩토리 메서드
     *
     * @param currentPoints 현재 포인트
     * @param totalEarned 총 적립 포인트
     * @param totalUsed 총 사용 포인트
     * @return PointResponseDTO 객체
     */
    public static PointResponseDTO of(Integer currentPoints, Integer totalEarned, Integer totalUsed) {
        return PointResponseDTO.builder()
                .currentPoints(currentPoints)
                .totalEarned(totalEarned)
                .totalUsed(Math.abs(totalUsed)) // 음수를 양수로 변환
                .build();
    }
}
