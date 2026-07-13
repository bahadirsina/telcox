package com.telcox.bff.model;

public record UpstreamStatus(
        String service,
        boolean available,
        String detail
) {
    public static UpstreamStatus up(String service) {
        return new UpstreamStatus(service, true, "OK");
    }

    public static UpstreamStatus down(String service, String detail) {
        return new UpstreamStatus(service, false, detail);
    }
}
