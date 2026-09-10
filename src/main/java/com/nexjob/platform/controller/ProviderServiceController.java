package com.nexjob.platform.controller;

import com.nexjob.platform.dto.ApiResponse;
import com.nexjob.platform.dto.PageResponse;
import com.nexjob.platform.dto.request.ServiceOfferingRequest;
import com.nexjob.platform.dto.response.ServiceOfferingResponse;
import com.nexjob.platform.service.ServiceOfferingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** El prestador administra aqui el catalogo de servicios que ofrece (titulo, precio, descripcion, fotos). */
@RestController
@RequestMapping("/api/provider/services")
@RequiredArgsConstructor
public class ProviderServiceController {

    private final ServiceOfferingService serviceOfferingService;

    @GetMapping
    public ApiResponse<PageResponse<ServiceOfferingResponse>> list(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(PageResponse.of(serviceOfferingService.listMine(PageRequest.of(page, size))));
    }

    @GetMapping("/{id}")
    public ApiResponse<ServiceOfferingResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(serviceOfferingService.getMineById(id));
    }

    @PostMapping
    public ApiResponse<ServiceOfferingResponse> create(@Valid @RequestBody ServiceOfferingRequest request) {
        return ApiResponse.ok(serviceOfferingService.create(request), "Servicio publicado");
    }

    @PutMapping("/{id}")
    public ApiResponse<ServiceOfferingResponse> update(@PathVariable Long id, @Valid @RequestBody ServiceOfferingRequest request) {
        return ApiResponse.ok(serviceOfferingService.update(id, request), "Servicio actualizado");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deactivate(@PathVariable Long id) {
        serviceOfferingService.deactivate(id);
        return ApiResponse.ok(null, "Servicio desactivado");
    }

    @PatchMapping("/{id}/activate")
    public ApiResponse<Void> reactivate(@PathVariable Long id) {
        serviceOfferingService.reactivate(id);
        return ApiResponse.ok(null, "Servicio activado");
    }

    @PostMapping(value = "/{id}/images", consumes = "multipart/form-data")
    public ApiResponse<ServiceOfferingResponse> addImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(serviceOfferingService.addImage(id, file), "Imagen agregada");
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public ApiResponse<Void> removeImage(@PathVariable Long id, @PathVariable Long imageId) {
        serviceOfferingService.removeImage(id, imageId);
        return ApiResponse.ok(null, "Imagen eliminada");
    }
}
