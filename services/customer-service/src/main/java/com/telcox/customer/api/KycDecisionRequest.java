package com.telcox.customer.api;

/**
 * FR-02: KYC onay/red istegi. reason opsiyoneldir (ozellikle red durumunda
 * agent aciklama girebilir).
 */
public record KycDecisionRequest(String reason) {
}
