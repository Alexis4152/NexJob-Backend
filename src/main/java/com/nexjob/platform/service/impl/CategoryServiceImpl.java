package com.nexjob.platform.service.impl;

import com.nexjob.platform.dto.request.CategoryIntakeFieldRequest;
import com.nexjob.platform.dto.request.CategoryRequest;
import com.nexjob.platform.dto.response.CategoryResponse;
import com.nexjob.platform.entity.Category;
import com.nexjob.platform.entity.CategoryIntakeField;
import com.nexjob.platform.enums.IntakeFieldType;
import com.nexjob.platform.exception.BusinessException;
import com.nexjob.platform.exception.ResourceNotFoundException;
import com.nexjob.platform.mapper.CategoryMapper;
import com.nexjob.platform.repository.CategoryRepository;
import com.nexjob.platform.security.SecurityUtils;
import com.nexjob.platform.service.CategoryService;
import com.nexjob.platform.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponse> listActive() {
        return categoryRepository.findByIsActiveTrueOrderByNameAsc().stream()
                .map(categoryMapper::toResponse).toList();
    }

    @Override
    public Page<CategoryResponse> adminList(String q, Pageable pageable) {
        Page<Category> page = (q == null || q.isBlank())
                ? categoryRepository.findByIsActiveTrue(pageable)
                : categoryRepository.findByIsActiveTrueAndNameContainingIgnoreCase(q.trim(), pageable);
        return page.map(categoryMapper::toResponse);
    }

    @Override
    public CategoryResponse getById(Long id) {
        return categoryMapper.toResponse(findActive(id));
    }

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String slug = uniqueSlug(request.getName(), null);
        Category category = new Category();
        category.setName(request.getName());
        category.setSlug(slug);
        category.setDescription(request.getDescription());
        category.setIcon(request.getIcon());
        category.setCreatedBy(SecurityUtils.getCurrentUserOrNull());
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findActive(id);
        if (!category.getName().equalsIgnoreCase(request.getName())) {
            category.setSlug(uniqueSlug(request.getName(), id));
        }
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIcon(request.getIcon());
        category.setUpdatedBy(SecurityUtils.getCurrentUserOrNull());
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse updateIntakeFields(Long id, List<CategoryIntakeFieldRequest> fields) {
        Category category = findActive(id);

        // Ids de todos los campos CHECKBOX que se estan guardando: showIfChecked solo puede
        // apuntar a uno de estos (ver "Instalacion" en Carpinteria: una casilla que, al
        // marcarse, revela un grupo de preguntas en vez de mostrarlas todas de golpe).
        java.util.Set<String> checkboxIds = fields.stream()
                .filter(f -> f.getType() == IntakeFieldType.CHECKBOX && f.getId() != null && !f.getId().isBlank())
                .map(CategoryIntakeFieldRequest::getId)
                .collect(java.util.stream.Collectors.toSet());

        // Para validar showIfFieldId/showIfValue (version general: depende de la respuesta
        // exacta de otro campo, no solo de una casilla -- ej. "cuantas habitaciones" solo
        // aparece si "Tipo de servicio" = "Pintura de casa completa").
        java.util.Map<String, CategoryIntakeFieldRequest> fieldsById = fields.stream()
                .filter(f -> f.getId() != null && !f.getId().isBlank())
                .collect(java.util.stream.Collectors.toMap(CategoryIntakeFieldRequest::getId, f -> f, (a, b) -> a));

        List<CategoryIntakeField> entities = fields.stream().map(f -> {
            boolean hasOptions = f.getType() == IntakeFieldType.SELECT || f.getType() == IntakeFieldType.MULTISELECT;
            if (hasOptions && (f.getOptions() == null || f.getOptions().isEmpty())) {
                throw new BusinessException("La pregunta \"" + f.getLabel() + "\" es de seleccion pero no tiene opciones");
            }
            if (f.getShowIfChecked() != null && !f.getShowIfChecked().isBlank()) {
                if (!checkboxIds.contains(f.getShowIfChecked())) {
                    throw new BusinessException("La pregunta \"" + f.getLabel() + "\" depende de una casilla que ya no existe");
                }
                if (f.getShowIfFieldId() != null && !f.getShowIfFieldId().isBlank()) {
                    throw new BusinessException("La pregunta \"" + f.getLabel() + "\" no puede depender de una casilla y de una respuesta al mismo tiempo");
                }
            }
            if (f.getShowIfFieldId() != null && !f.getShowIfFieldId().isBlank()) {
                CategoryIntakeFieldRequest trigger = fieldsById.get(f.getShowIfFieldId());
                if (trigger == null) {
                    throw new BusinessException("La pregunta \"" + f.getLabel() + "\" depende de una pregunta que ya no existe");
                }
                if (f.getShowIfValue() == null || f.getShowIfValue().isBlank()) {
                    throw new BusinessException("La pregunta \"" + f.getLabel() + "\" necesita el valor que la activa");
                }
                boolean triggerHasOptions = trigger.getType() == IntakeFieldType.SELECT || trigger.getType() == IntakeFieldType.MULTISELECT;
                if (triggerHasOptions && (trigger.getOptions() == null || !trigger.getOptions().contains(f.getShowIfValue()))) {
                    throw new BusinessException("La pregunta \"" + f.getLabel() + "\" depende de un valor que ya no es una opcion valida");
                }
            }
            CategoryIntakeField entity = new CategoryIntakeField();
            entity.setId(f.getId() != null && !f.getId().isBlank() ? f.getId() : UUID.randomUUID().toString());
            entity.setLabel(f.getLabel());
            entity.setType(f.getType());
            entity.setOptions(hasOptions ? f.getOptions() : null);
            entity.setShowIfChecked(f.getShowIfChecked() != null && !f.getShowIfChecked().isBlank() ? f.getShowIfChecked() : null);
            entity.setShowIfFieldId(f.getShowIfFieldId() != null && !f.getShowIfFieldId().isBlank() ? f.getShowIfFieldId() : null);
            entity.setShowIfValue(entity.getShowIfFieldId() != null ? f.getShowIfValue() : null);
            entity.setUnit(f.getType() == IntakeFieldType.DIMENSIONS && f.getUnit() != null && !f.getUnit().isBlank() ? f.getUnit() : null);
            return entity;
        }).toList();
        category.setIntakeFields(entities);
        category.setUpdatedBy(SecurityUtils.getCurrentUserOrNull());
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Category category = findActive(id);
        category.setIsActive(false);
        category.setDeletedAt(LocalDateTime.now());
        category.setDeletedBy(SecurityUtils.getCurrentUserOrNull());
        categoryRepository.save(category);
    }

    private Category findActive(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada: " + id));
        if (!Boolean.TRUE.equals(category.getIsActive())) {
            throw new ResourceNotFoundException("Categoria no encontrada: " + id);
        }
        return category;
    }

    private String uniqueSlug(String name, Long excludeId) {
        String base = SlugUtils.slugify(name);
        String candidate = base;
        int suffix = 2;
        while (isSlugTaken(candidate, excludeId)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    private boolean isSlugTaken(String slug, Long excludeId) {
        return categoryRepository.findBySlug(slug)
                .filter(existing -> !existing.getId().equals(excludeId))
                .isPresent();
    }
}
