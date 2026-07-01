package com.telcox.productcatalog.cache;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("telcox.cache.product-catalog")
public class ProductCatalogCacheProperties {
    private Duration tariffTtl = Duration.ofMinutes(10);
    private Duration listTtl = Duration.ofMinutes(5);

    public Duration getTariffTtl() {
        return tariffTtl;
    }

    public void setTariffTtl(Duration tariffTtl) {
        this.tariffTtl = requirePositive(tariffTtl, "tariff-ttl");
    }

    public Duration getListTtl() {
        return listTtl;
    }

    public void setListTtl(Duration listTtl) {
        this.listTtl = requirePositive(listTtl, "list-ttl");
    }

    private Duration requirePositive(Duration value, String name) {
        if (value == null || value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }
}
