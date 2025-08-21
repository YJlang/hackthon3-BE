package com.example.hamkae.service;

import com.example.hamkae.DTO.PointHistoryResponseDTO;
import com.example.hamkae.DTO.PointResponseDTO;
import com.example.hamkae.domain.User;
import com.example.hamkae.repository.PointHistoryRepository;
import com.example.hamkae.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PointService {

    private final PointHistoryRepository pointHistoryRepository;
    private final UserRepository userRepository;

    /**
     * 사용자의 포인트 정보 및 통계를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 포인트 응답 DTO
     * @throws IllegalArgumentException 사용자를 찾을 수 없는 경우
     */
    public PointResponseDTO getUserPoints(Long userId) {
        log.info("사용자 포인트 조회 요청 - userId: {}", userId);

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));

        // 포인트 통계 조회
        Integer totalEarned = pointHistoryRepository.getTotalEarnedPointsByUserId(userId);
        Integer totalUsed = pointHistoryRepository.getTotalUsedPointsByUserId(userId);

        // 현재 포인트는 User 엔티티에서 직접 조회
        Integer currentPoints = user.getPoints();

        log.info("포인트 조회 완료 - userId: {}, current: {}, earned: {}, used: {}",
                userId, currentPoints, totalEarned, totalUsed);

        return PointResponseDTO.of(currentPoints, totalEarned, totalUsed);
    }

    /**
     * 사용자의 포인트 이력을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 포인트 이력 응답 DTO 리스트 (최신순)
     * @throws IllegalArgumentException 사용자를 찾을 수 없는 경우
     */
    public List<PointHistoryResponseDTO> getPointHistory(Long userId) {
        log.info("포인트 이력 조회 요청 - userId: {}", userId);

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));

        // 포인트 이력 조회 및 DTO 변환
        List<PointHistoryResponseDTO> historyList = pointHistoryRepository
                .findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(PointHistoryResponseDTO::from)
                .collect(Collectors.toList());

        log.info("포인트 이력 조회 완료 - userId: {}, count: {}", userId, historyList.size());

        return historyList;
    }
}
