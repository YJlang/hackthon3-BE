package com.example.hamkae.repository;

import com.example.hamkae.domain.Reward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 상품권 교환 요청을 관리하는 Repository 인터페이스
 * 
 * @author 권오윤
 * @version 1.0
 * @since 2025-08-15
 */
@Repository
public interface RewardRepository extends JpaRepository<Reward, Long> {

    /**
     * 특정 사용자의 상품권 교환 요청 목록을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 해당 사용자의 상품권 교환 요청 목록
     */
    List<Reward> findByUserId(Long userId);

    /**
     * 특정 사용자의 특정 상태 상품권 교환 요청 목록을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @param status 요청 상태
     * @return 해당 사용자의 특정 상태 상품권 교환 요청 목록
     */
    List<Reward> findByUserIdAndStatus(Long userId, Reward.RewardStatus status);

    /**
     * 특정 사용자의 특정 교환 요청 단건을 조회합니다.
     *
     * @param id 교환 요청 ID
     * @param userId 사용자 ID
     * @return 해당 사용자의 교환 요청 (없으면 empty)
     */
    Optional<Reward> findByIdAndUserId(Long id, Long userId);
}

