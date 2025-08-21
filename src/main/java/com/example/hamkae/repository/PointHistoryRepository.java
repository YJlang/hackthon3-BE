package com.example.hamkae.repository;

import com.example.hamkae.domain.PointHistory;
import com.example.hamkae.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    /**
     * 특정 사용자의 포인트 이력을 최신순으로 조회
     *
     * @param user 조회할 사용자
     * @return 포인트 이력 리스트 (최신순)
     */
    List<PointHistory> findByUserOrderByCreatedAtDesc(User user);

    /**
     * 특정 사용자의 총 적립 포인트 조회
     *
     * @param userId 사용자 ID
     * @return 총 적립 포인트
     */
    @Query("SELECT COALESCE(SUM(ph.points), 0) FROM PointHistory ph " +
            "WHERE ph.user.id = :userId AND ph.type = 'EARNED'")
    Integer getTotalEarnedPointsByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자의 총 사용 포인트 조회 (음수 값 반환)
     *
     * @param userId 사용자 ID
     * @return 총 사용 포인트 (음수)
     */
    @Query("SELECT COALESCE(SUM(ph.points), 0) FROM PointHistory ph " +
            "WHERE ph.user.id = :userId AND ph.type = 'USED'")
    Integer getTotalUsedPointsByUserId(@Param("userId") Long userId);
}
