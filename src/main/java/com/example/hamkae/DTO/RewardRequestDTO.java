package com.example.hamkae.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 상품권 교환 요청을 위한 DTO
 *
 * @author 권오윤
 * @version 1.0
 * @since 2025-08-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardRequestDTO {

    /**
     * 교환할 상품권 개수 (1 이상)
     */
    private Integer quantity;

    /**
     * 교환할 상품권 금액 타입 (영문 열거형 값)
     * 허용값: FIVE_THOUSAND, TEN_THOUSAND, THIRTY_THOUSAND
     */
    private String rewardType;
}
