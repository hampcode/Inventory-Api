package com.tambos.inventory.controller;


import com.tambos.inventory.exception.ResourceNotFoundException;
import com.tambos.inventory.model.Category;
import com.tambos.inventory.repository.CategoryRepository;
import com.tambos.inventory.util.CategoryExcelExporter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:4200"})
@RestController
@RequestMapping("/api/v1")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> searchCategories() {
        List<Category> categories=categoryRepository.findAll();
        return new ResponseEntity<List<Category>>(categories, HttpStatus.OK);
    }


    @Transactional (readOnly = true)
    @GetMapping("/categories/{id}")
    public ResponseEntity<Category> searchCategoriesById(@PathVariable Long id) {
        Category category= categoryRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Not found category with id="+id));
        return new ResponseEntity<Category>(category,HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/categories")
    public ResponseEntity<Category> save(@RequestBody Category category) {
        Category newCategory= Category.builder().name(category.getName()).description(category.getDescription()).build();
        categoryRepository.save(newCategory);
        return new ResponseEntity<Category>(newCategory,HttpStatus.CREATED);
    }

    @Transactional
    @PutMapping("/categories/{id}")
    public ResponseEntity<Category> update(@RequestBody Category category, @PathVariable Long id) {
        Category categoryUpdate= categoryRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Not found category with id="+id));

        categoryUpdate.setName(category.getName());
        categoryUpdate.setDescription(category.getDescription());

        return new ResponseEntity<Category>(categoryRepository.save(categoryUpdate),
                HttpStatus.OK);
    }

    @Transactional
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Category> delete(@PathVariable Long id) {
        categoryRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Transactional (readOnly = true)
    @GetMapping("/categories/export/excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {

        response.setContentType("application/octet-stream");

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=result_category";
        response.setHeader(headerKey, headerValue);

        List<Category> categoryResponse = categoryRepository.findAll();

        CategoryExcelExporter excelExporter = new CategoryExcelExporter(
                categoryResponse);

        excelExporter.export(response);
    }
}
