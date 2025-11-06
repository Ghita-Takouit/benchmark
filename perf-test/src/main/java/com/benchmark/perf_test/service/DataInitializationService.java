package com.benchmark.perf_test.service;

import com.benchmark.perf_test.entity.Category;
import com.benchmark.perf_test.entity.Item;
import com.benchmark.perf_test.repository.CategoryRepository;
import com.benchmark.perf_test.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class DataInitializationService implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ItemRepository itemRepository;

    private final Random random = new Random();

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Check if data already exists
        if (categoryRepository.count() > 0) {
            System.out.println("Data already exists, skipping initialization");
            return;
        }

        System.out.println("Starting data initialization...");

        // Create 2,000 categories
        List<Category> categories = createCategories();
        System.out.println("Created " + categories.size() + " categories");

        // Create 100,000 items (~50 items per category)
        createItems(categories);
        System.out.println("Data initialization completed");
    }

    private List<Category> createCategories() {
        List<Category> categories = new ArrayList<>();

        for (int i = 1; i <= 2000; i++) {
            Category category = new Category();
            category.setName("Category " + String.format("CAT%04d", i));
            category.setDescription("Description for category " + i + " - " + generateRandomText(50));
            categories.add(category);
        }

        return categoryRepository.saveAll(categories);
    }

    private void createItems(List<Category> categories) {
        List<Item> items = new ArrayList<>();
        int itemCounter = 1;

        for (Category category : categories) {
            // Create approximately 50 items per category (with some variation)
            int itemsPerCategory = 45 + random.nextInt(11); // 45-55 items per category

            for (int j = 0; j < itemsPerCategory && itemCounter <= 100000; j++) {
                Item item = new Item();
                item.setSku("SKU" + String.format("%06d", itemCounter));
                item.setName("Item " + itemCounter + " - " + category.getName().substring(9));
                item.setPrice(BigDecimal.valueOf(10 + random.nextDouble() * 1000).setScale(2, BigDecimal.ROUND_HALF_UP));
                item.setStock(random.nextInt(1000));
                item.setCategory(category);

                // Add description for heavy payload testing (~5KB)
                item.setDescription(generateHeavyDescription());

                items.add(item);
                itemCounter++;

                // Save in batches to avoid memory issues
                if (items.size() >= 1000) {
                    itemRepository.saveAll(items);
                    items.clear();
                    System.out.println("Saved items up to: " + (itemCounter - 1));
                }
            }
        }

        // Save remaining items
        if (!items.isEmpty()) {
            itemRepository.saveAll(items);
        }
    }

    private String generateRandomText(int words) {
        String[] wordPool = {"product", "item", "category", "description", "quality", "premium", "standard",
                           "excellent", "good", "best", "top", "high", "low", "medium", "special", "unique",
                           "amazing", "wonderful", "fantastic", "great", "perfect", "ideal", "suitable"};

        StringBuilder text = new StringBuilder();
        for (int i = 0; i < words; i++) {
            if (i > 0) text.append(" ");
            text.append(wordPool[random.nextInt(wordPool.length)]);
        }
        return text.toString();
    }

    private String generateHeavyDescription() {
        // Generate approximately 4.8KB of text for heavy payload testing (safely under 5KB limit)
        StringBuilder description = new StringBuilder();

        String baseText = "This is a comprehensive product description designed for performance testing. " +
                         "It contains detailed information about the product features, specifications, " +
                         "usage instructions, warranty information, and customer reviews. ";

        // Calculate safe repetition count to stay under 5000 characters
        int maxLength = 4800; // Safe margin under 5000
        int baseLength = baseText.length();
        int sectionLength = 50; // Approximate length of section text
        int totalSectionLength = baseLength + sectionLength;
        int maxSections = maxLength / totalSectionLength;

        for (int i = 0; i < maxSections && description.length() < maxLength - 200; i++) {
            if (description.length() + totalSectionLength > maxLength) {
                break;
            }
            description.append(baseText)
                      .append("Section ").append(i + 1).append(": ")
                      .append(generateRandomText(5)).append(". "); // Reduced from 20 to 5 words
        }

        // Ensure we don't exceed the limit
        String result = description.toString();
        if (result.length() > 4900) {
            result = result.substring(0, 4900);
        }

        return result;
    }
}
