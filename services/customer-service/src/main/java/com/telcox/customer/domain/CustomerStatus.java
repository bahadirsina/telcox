package com.telcox.customer.domain;

/**
 * FR-02: KYC state flow.
 *
 * Gecisler:
 *   PROSPECT  -> ACTIVE     (KYC onaylandi)
 *   PROSPECT  -> CLOSED     (KYC reddedildi, terminal)
 *   ACTIVE    -> SUSPENDED  (gecici askiya alma)
 *   SUSPENDED -> ACTIVE     (askidan cikarma)
 *   ACTIVE/SUSPENDED -> CLOSED (hesap kapatma, terminal)
 *
 * Gecis kurallari Customer.java icindeki metotlarda uygulanir; bu enum
 * sadece durumlarin kendisini tanimlar.
 */
public enum CustomerStatus {
    PROSPECT,
    ACTIVE,
    SUSPENDED,
    CLOSED
}
