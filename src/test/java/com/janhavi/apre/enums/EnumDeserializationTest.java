package com.janhavi.apre.enums;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.janhavi.apre.dto.LoginRequest;
import com.janhavi.apre.dto.RegisterRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EnumDeserializationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testRoleDeserialization() throws Exception {
        assertEquals(Role.ANALYST, mapper.readValue("\"ANALYST\"", Role.class));
        assertEquals(Role.ANALYST, mapper.readValue("\"analyst\"", Role.class));
        assertEquals(Role.ANALYST, mapper.readValue("\"Analyst\"", Role.class));
        assertEquals(Role.ANALYST, mapper.readValue("\"\"", Role.class));
        assertEquals(Role.ANALYST, mapper.readValue("\"UNKNOWN\"", Role.class));
        assertEquals(Role.ADMIN, mapper.readValue("\"ADMIN\"", Role.class));
        assertEquals(Role.ADMIN, mapper.readValue("\"admin\"", Role.class));
    }

    @Test
    public void testPaymentMethodDeserialization() throws Exception {
        assertEquals(PaymentMethod.CREDIT_CARD, mapper.readValue("\"CREDIT_CARD\"", PaymentMethod.class));
        assertEquals(PaymentMethod.CREDIT_CARD, mapper.readValue("\"credit_card\"", PaymentMethod.class));
        assertEquals(PaymentMethod.CREDIT_CARD, mapper.readValue("\"credit-card\"", PaymentMethod.class));
        assertEquals(PaymentMethod.UPI, mapper.readValue("\"UPI\"", PaymentMethod.class));
        assertEquals(PaymentMethod.UPI, mapper.readValue("\"upi\"", PaymentMethod.class));
        assertEquals(PaymentMethod.WALLET, mapper.readValue("\"WALLET\"", PaymentMethod.class));
        assertEquals(PaymentMethod.WALLET, mapper.readValue("\"wallet\"", PaymentMethod.class));
    }

    @Test
    public void testMerchantCategoryDeserialization() throws Exception {
        assertEquals(MerchantCategory.ECOMMERCE, mapper.readValue("\"ECOMMERCE\"", MerchantCategory.class));
        assertEquals(MerchantCategory.ECOMMERCE, mapper.readValue("\"ecommerce\"", MerchantCategory.class));
        assertEquals(MerchantCategory.TRAVEL, mapper.readValue("\"TRAVEL\"", MerchantCategory.class));
        assertEquals(MerchantCategory.TRAVEL, mapper.readValue("\"travel\"", MerchantCategory.class));
        assertEquals(MerchantCategory.OTHER, mapper.readValue("\"UNKNOWN_CAT\"", MerchantCategory.class));
    }

    @Test
    public void testRegisterRequestDeserialization() throws Exception {
        String json = "{\"name\":\" John Doe \",\"email\":\" JOHN@EXAMPLE.COM \",\"password\":\"pass123\",\"role\":\"analyst\"}";
        RegisterRequest req = mapper.readValue(json, RegisterRequest.class);
        assertEquals("John Doe", req.getName());
        assertEquals("JOHN@EXAMPLE.COM", req.getEmail());
        assertEquals("pass123", req.getPassword());
        assertEquals(Role.ANALYST, req.getRole());
    }

    @Test
    public void testLoginRequestDeserialization() throws Exception {
        String json = "{\"email\":\" TEST@EXAMPLE.COM \",\"password\":\"pass123\"}";
        LoginRequest req = mapper.readValue(json, LoginRequest.class);
        assertEquals("TEST@EXAMPLE.COM", req.getEmail());
        assertEquals("pass123", req.getPassword());
    }
}
