package com.hwinterton.inventory_api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hwinterton.inventory_api.dto.inventory.InventoryAdjustmentRequest;
import com.hwinterton.inventory_api.dto.inventory.InventoryAdjustmentResponse;
import com.hwinterton.inventory_api.model.InventoryAdjustment;
import com.hwinterton.inventory_api.model.ProductVariant;
import com.hwinterton.inventory_api.model.User;
import com.hwinterton.inventory_api.repository.InventoryAdjustmentRepository;
import com.hwinterton.inventory_api.repository.ProductVariantRepository;
import com.hwinterton.inventory_api.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service class responsible for all InventoryAdjustment business logic.
 *
 * <p>Handles recording inventory changes and updating variant quantity on hand.
 * Enforces that variants and users exist before adjustments are applied.</p>
 */
@Slf4j // Lombok: logging feature helper, call replaced need for standard dependencies fields for Slf4j logging
@Service
public class InventoryAdjustmentService {

    private final InventoryAdjustmentRepository inventoryAdjustmentRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    public InventoryAdjustmentService(
        InventoryAdjustmentRepository inventoryAdjustmentRepository,
        ProductVariantRepository productVariantRepository,
        UserRepository userRepository
    ) {
        this.inventoryAdjustmentRepository = inventoryAdjustmentRepository;
        this.productVariantRepository = productVariantRepository;
        this.userRepository = userRepository;
    }

    /**
     * Method: records an inventory adjustment and updates the variant quantity on hand.
     *
     * @param request the adjustment data from the frontend
     * @param performedByUserId the id of the user making the adjustment
     * @return the recorded adjustment as a response DTO
     * @throws RuntimeException if the variant or user is not found
     */
    public InventoryAdjustmentResponse recordAdjustment(InventoryAdjustmentRequest request, String performedByUsername) {
        log.info("Redording inventory adjustment for variant id: {} by user id: {}", request.variantId(), performedByUsername);
        // find the variant
        ProductVariant variant = productVariantRepository.findById(request.variantId())
            .orElseThrow(() -> new RuntimeException("Product variant not found"));

        // find the user performing the adjustment
        User performedByUser = userRepository.findByUsername(performedByUsername)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // apply the change to quantity on hand
        variant.setQuantityOnHand(variant.getQuantityOnHand() + request.changeAmount());
        productVariantRepository.save(variant);
        log.info("Quantity on hand updated for variant id: {} New quantity: {}", variant.getId(), variant.getQuantityOnHand());

        // record the adjustment
        InventoryAdjustment adjustment = new InventoryAdjustment();
        adjustment.setProductVariant(variant);
        adjustment.setChangeAmount(request.changeAmount());
        adjustment.setReason(request.reason());
        adjustment.setPerformedByUser(performedByUser);

        InventoryAdjustment savedAdjustment = inventoryAdjustmentRepository.save(adjustment);
        log.info("Inventory adjustment recorded successfully with id: {}", savedAdjustment.getId());
        return toResponse(savedAdjustment);
    }

    /**
     * Method: retrieves all adjustments for a specific variant.
     *
     * @param variantId the variant ID
     * @return list of adjustments as response DTOs
     * @throws RuntimeException if the variant is not found
     */
    public List<InventoryAdjustmentResponse> getAdjustmentsByVariant(Long variantId) {
        log.info("Fetching inventory adjustments for variant id: {}", variantId);
        ProductVariant variant = productVariantRepository.findById(variantId)
            .orElseThrow(() -> new RuntimeException("Product variant not found"));

        return inventoryAdjustmentRepository.findByProductVariant(variant)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    /**
     * Method: converts an InventoryAdjustment entity to a response DTO.
     *
     * @param adjustment the adjustment entity
     * @return the converted InventoryAdjustmentResponse DTO
     */
    private InventoryAdjustmentResponse toResponse(InventoryAdjustment adjustment) {
        return new InventoryAdjustmentResponse(
            adjustment.getId(),
            adjustment.getProductVariant().getId(),
            adjustment.getProductVariant().getDisplayName(),
            adjustment.getChangeAmount(),
            adjustment.getReason(),
            adjustment.getCreatedAt()
        );
    }
}