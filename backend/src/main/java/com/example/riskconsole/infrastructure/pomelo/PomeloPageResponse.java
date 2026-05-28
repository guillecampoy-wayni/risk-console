package com.example.riskconsole.infrastructure.pomelo;

import java.util.List;

public record PomeloPageResponse<T>(List<T> data, Meta meta) {
    public record Meta(Pagination pagination) {}
    public record Pagination(Integer current_page, Integer total_pages, Integer page_size) {}
}
