package com.tambos.inventory.controller;

import com.tambos.inventory.exception.ResourceNotFoundException;
import com.tambos.inventory.model.Category;
import com.tambos.inventory.model.Product;
import com.tambos.inventory.repository.CategoryRepository;
import com.tambos.inventory.repository.ProductRepository;
import com.tambos.inventory.util.Util;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@CrossOrigin(origins = {"http://localhost:4200"})
@RestController
@RequestMapping("/api/v1")
public class ProductController {

    private final CategoryRepository categoryRepository;

    private final ProductRepository productRepository;

    public ProductController(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @GetMapping("/products/{id}")
    @Transactional (readOnly = true)
    public ResponseEntity<Product> searchById(@PathVariable Long id){
        Product product=productRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Not found product with id="+id));

        if(product!=null){
            byte[] imageDescompressed = Util.decompressZLib(product.getPicture());
            product.setPicture(imageDescompressed);
        }
        return new ResponseEntity<Product>(product,HttpStatus.OK);
    }


    @PostMapping("/products")
    @Transactional
    //public ResponseEntity<Product> save(@RequestBody Product product) {
    public ResponseEntity<Product> save(@RequestParam("picture") MultipartFile picture,
                                        @RequestParam("name") String name,
                                        @RequestParam("price") int price,
                                        @RequestParam("account") int account,
                                        @RequestParam("categoryId") Long categoryID)throws IOException {

        Product product = new Product();
        product.setName(name);
        product.setAccount(account);
        product.setPrice(price);
        product.setPicture(Util.compressZLib(picture.getBytes()));

        //TODO: búsqueda de categoría para establecer en el objeto del producto
        Category category = categoryRepository.findById(categoryID)
                .orElseThrow(()-> new ResourceNotFoundException("Not found product with id="+categoryID));

        if( category!=null) {
            product.setCategory(category);
        }

        Product productSaved=productRepository.save(product);

        return new ResponseEntity<Product>(productSaved,HttpStatus.CREATED);
    }


    @GetMapping("/products/filter/{name}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Product>> searchByName(@PathVariable String name){
        List<Product> products=new ArrayList<>();
        List<Product> productsAux=new ArrayList<>();

        productsAux=productRepository.findByNameContainingIgnoreCase(name);

        if(productsAux.size()>0){
            productsAux.stream().forEach((p)->{
                byte[] imageDescompressed = Util.decompressZLib(p.getPicture());
                p.setPicture(imageDescompressed);
                products.add(p);
            });
        }

        return new ResponseEntity<List<Product>>(products, HttpStatus.OK);
    }


}
