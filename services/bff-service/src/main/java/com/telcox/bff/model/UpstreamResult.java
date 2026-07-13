package com.telcox.bff.model;

public record UpstreamResult<T>(
        String service,
        boolean available,
        T body,
        String detail
) {
    public static <T> UpstreamResult<T> ok(String service, T body) {
        return new UpstreamResult<>(service, true, body, "OK");
    }

    public static <T> UpstreamResult<T> unavailable(String service, String detail, T fallback) {
        return new UpstreamResult<>(service, false, fallback, detail);
    }

    public UpstreamStatus status() {
        return available ? UpstreamStatus.up(service) : UpstreamStatus.down(service, detail);
    }
}
