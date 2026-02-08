package com.api.project.Ecomerce.controller;

import com.api.project.Ecomerce.dto.AddressRequest;
import com.api.project.Ecomerce.dto.AddressResponse;
import com.api.project.Ecomerce.response.ApiResponse;
import com.api.project.Ecomerce.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Slf4j
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ApiResponse<List<AddressResponse>> getAddresses() {
        log.info("AddressController - Getting all addresses");
        List<AddressResponse> addresses = addressService.getAddresses();
        return ApiResponse.<List<AddressResponse>>builder()
                .success(true)
                .message("Addresses retrieved")
                .data(addresses)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<AddressResponse> getAddress(@PathVariable Long id) {
        log.info("AddressController - Getting address id={}", id);
        AddressResponse address = addressService.getAddress(id);
        return ApiResponse.<AddressResponse>builder()
                .success(true)
                .message("Address retrieved")
                .data(address)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PostMapping
    public ApiResponse<AddressResponse> addAddress(@RequestBody AddressRequest request) {
        log.info("AddressController - Adding new address");
        AddressResponse address = addressService.addAddress(request);
        return ApiResponse.<AddressResponse>builder()
                .success(true)
                .message("Address added successfully")
                .data(address)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<AddressResponse> updateAddress(
            @PathVariable Long id,
            @RequestBody AddressRequest request) {
        log.info("AddressController - Updating address id={}", id);
        AddressResponse address = addressService.updateAddress(id, request);
        return ApiResponse.<AddressResponse>builder()
                .success(true)
                .message("Address updated successfully")
                .data(address)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAddress(@PathVariable Long id) {
        log.info("AddressController - Deleting address id={}", id);
        addressService.deleteAddress(id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Address deleted successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PutMapping("/{id}/default")
    public ApiResponse<AddressResponse> setDefaultAddress(@PathVariable Long id) {
        log.info("AddressController - Setting default address id={}", id);
        AddressResponse address = addressService.setDefaultAddress(id);
        return ApiResponse.<AddressResponse>builder()
                .success(true)
                .message("Default address updated")
                .data(address)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
