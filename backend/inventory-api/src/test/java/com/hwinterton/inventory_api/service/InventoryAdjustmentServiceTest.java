package com.hwinterton.inventory_api.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hwinterton.inventory_api.dto.inventory.InventoryAdjustmentRequest;
import com.hwinterton.inventory_api.dto.inventory.InventoryAdjustmentResponse;
import com.hwinterton.inventory_api.model.InventoryAdjustment;
import com.hwinterton.inventory_api.model.ProductVariant;
import com.hwinterton.inventory_api.model.User;
import com.hwinterton.inventory_api.repository.InventoryAdjustmentRepository;
import com.hwinterton.inventory_api.repository.ProductVariantRepository;
import com.hwinterton.inventory_api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class InventoryAdjustmentServiceTest {

    @Mock
    private InventoryAdjustmentRepository inventoryAdjustmentRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private UserRepository userRepository;

    private InventoryAdjustmentService inventoryAdjustmentService;

    @BeforeEach
    void setUp() {
        inventoryAdjustmentService = new InventoryAdjustmentService(
            inventoryAdjustmentRepository,
            productVariantRepository,
            userRepository
        );
    }

    // helper to build a base variant
    private ProductVariant buildVariant() {
        ProductVariant variant = new ProductVariant();
        variant.setId(1L);
        variant.setDisplayName("Chocolate Protein Bar");
        variant.setQuantityOnHand(50);
        variant.setLowStockThreshold(10);
        variant.setActive(true);
        return variant;
    }

    // helper to build a base user
    private User buildUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("owner");
        return user;
    }

    // helper to build a base adjustment
    private InventoryAdjustment buildAdjustment(ProductVariant variant, User user) {
        InventoryAdjustment adjustment = new InventoryAdjustment();
        adjustment.setId(1L);
        adjustment.setProductVariant(variant);
        adjustment.setChangeAmount(10);
        adjustment.setReason("Restock");
        adjustment.setPerformedByUser(user);
        return adjustment;
    }

    // testing recordAdjustment happy path
    @Test
    void recordAdjustment_validRequest_returnsInventoryAdjustmentResponse() {
        ProductVariant variant = buildVariant();
        User user = buildUser();
        InventoryAdjustment savedAdjustment = buildAdjustment(variant, user);

        InventoryAdjustmentRequest request = new InventoryAdjustmentRequest(1L, 10, "Restock");

        when(productVariantRepository.findById(1L))
            .thenReturn(Optional.of(variant));

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(productVariantRepository.save(any(ProductVariant.class)))
            .thenReturn(variant);

        when(inventoryAdjustmentRepository.save(any(InventoryAdjustment.class)))
            .thenReturn(savedAdjustment);

        InventoryAdjustmentResponse response = inventoryAdjustmentService.recordAdjustment(request, 1L);

        assertEquals(1L, response.variantId());
        assertEquals(10, response.changeAmount());
        assertEquals("Restock", response.reason());

        verify(productVariantRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(productVariantRepository).save(any(ProductVariant.class));
        verify(inventoryAdjustmentRepository).save(any(InventoryAdjustment.class));
    }

    // testing recordAdjustment fail path - variant not found
    @Test
    void recordAdjustment_invalidVariantId_throwsRuntimeException() {
        InventoryAdjustmentRequest request = new InventoryAdjustmentRequest(99L, 10, "Restock");

        when(productVariantRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> inventoryAdjustmentService.recordAdjustment(request, 1L));

        verify(productVariantRepository).findById(99L);
        verify(userRepository, never()).findById(any());
        verify(inventoryAdjustmentRepository, never()).save(any());
    }

    // testing recordAdjustment fail path - user not found
    @Test
    void recordAdjustment_invalidUserId_throwsRuntimeException() {
        ProductVariant variant = buildVariant();
        InventoryAdjustmentRequest request = new InventoryAdjustmentRequest(1L, 10, "Restock");

        when(productVariantRepository.findById(1L))
            .thenReturn(Optional.of(variant));

        when(userRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> inventoryAdjustmentService.recordAdjustment(request, 99L));

        verify(productVariantRepository).findById(1L);
        verify(userRepository).findById(99L);
        verify(inventoryAdjustmentRepository, never()).save(any());
    }

    // testing getAdjustmentsByVariant happy path
    @Test
    void getAdjustmentsByVariant_validVariantId_returnsAdjustmentList() {
        ProductVariant variant = buildVariant();
        User user = buildUser();
        InventoryAdjustment adjustment = buildAdjustment(variant, user);

        when(productVariantRepository.findById(1L))
            .thenReturn(Optional.of(variant));

        when(inventoryAdjustmentRepository.findByProductVariant(variant))
            .thenReturn(List.of(adjustment));

        List<InventoryAdjustmentResponse> response = inventoryAdjustmentService.getAdjustmentsByVariant(1L);

        assertEquals(1, response.size());
        assertEquals(10, response.get(0).changeAmount());
        assertEquals("Restock", response.get(0).reason());

        verify(productVariantRepository).findById(1L);
        verify(inventoryAdjustmentRepository).findByProductVariant(variant);
    }

    // testing getAdjustmentsByVariant fail path - variant not found
    @Test
    void getAdjustmentsByVariant_invalidVariantId_throwsRuntimeException() {
        when(productVariantRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> inventoryAdjustmentService.getAdjustmentsByVariant(99L));

        verify(productVariantRepository).findById(99L);
        verify(inventoryAdjustmentRepository, never()).findByProductVariant(any());
    }
}