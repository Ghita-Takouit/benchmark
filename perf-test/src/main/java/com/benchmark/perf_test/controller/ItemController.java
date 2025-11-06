package com.benchmark.perf_test.controller;

import com.benchmark.perf_test.entity.Item;
import com.benchmark.perf_test.repository.CategoryRepository;
import com.benchmark.perf_test.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/items")
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // GET /items?page=&size= : liste paginée
    @GetMapping
    public Page<Item> getAllItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "false") boolean joinFetch) {
        Pageable pageable = PageRequest.of(page, size);

        // GET /items?categoryId=&page=&size= : filtrage relationnel
        if (categoryId != null) {
            if (joinFetch) {
                // Utilise JOIN FETCH pour éviter le problème N+1
                return itemRepository.findByCategoryIdWithJoinFetch(categoryId, pageable);
            } else {
                // Requête normale qui peut causer le problème N+1
                return itemRepository.findByCategoryId(categoryId, pageable);
            }
        }

        return itemRepository.findAll(pageable);
    }

    // GET /items/{id} : détail
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        Optional<Item> item = itemRepository.findById(id);
        return item.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    // POST /items (JSON ~1–5 KB)
    @PostMapping
    public ResponseEntity<Item> createItem(@RequestBody Item item) {
        try {
            // Validate that the category exists
            if (item.getCategory() != null && item.getCategory().getId() != null) {
                if (!categoryRepository.existsById(item.getCategory().getId())) {
                    return ResponseEntity.badRequest().build();
                }
            }

            Item savedItem = itemRepository.save(item);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // PUT /items/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item itemDetails) {
        Optional<Item> itemOptional = itemRepository.findById(id);

        if (itemOptional.isPresent()) {
            Item item = itemOptional.get();
            item.setSku(itemDetails.getSku());
            item.setName(itemDetails.getName());
            item.setPrice(itemDetails.getPrice());
            item.setStock(itemDetails.getStock());
            item.setDescription(itemDetails.getDescription());

            // Update category if provided
            if (itemDetails.getCategory() != null && itemDetails.getCategory().getId() != null) {
                if (!categoryRepository.existsById(itemDetails.getCategory().getId())) {
                    return ResponseEntity.badRequest().build();
                }
                item.setCategory(itemDetails.getCategory());
            }

            Item updatedItem = itemRepository.save(item);
            return ResponseEntity.ok(updatedItem);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /items/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        if (itemRepository.existsById(id)) {
            itemRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Legacy endpoint for backward compatibility
    @GetMapping("/by-category")
    public Page<Item> byCategory(
            @RequestParam Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "false") boolean joinFetch) {
        if (joinFetch)
            return itemRepository.findByCategoryIdWithJoinFetch(categoryId, PageRequest.of(page, size));
        return itemRepository.findByCategoryId(categoryId, PageRequest.of(page, size));
    }
}