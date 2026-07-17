package com.janhavi.apre.dto;

import com.janhavi.apre.enums.MerchantCategory;
import com.janhavi.apre.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class PaymentRequest {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Merchant name is required")
    private String merchantName;

    @NotNull(message = "Merchant category is required")
    private MerchantCategory merchantCategory;

    @NotBlank(message = "Country is required")
    private String country;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    public PaymentRequest() {
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public MerchantCategory getMerchantCategory() {
        return merchantCategory;
    }

    public void setMerchantCategory(MerchantCategory merchantCategory) {
        this.merchantCategory = merchantCategory;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}