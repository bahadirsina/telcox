package com.telcox.common.dto;

import lombok.Builder;

import java.util.List;

/**
 * Sayfalama icin standart cevap zarfi.
 * Spring Data Pageable cevaplarini istemciye sade bir formatta sunmak icin kullanilir.
 */
@Builder
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
}
