package com.example.ShoesShop.Controller.impl;

import com.example.ShoesShop.DTO.StoreDTO;
import com.example.ShoesShop.Entity.Store;
import com.example.ShoesShop.Services.impl.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store")
public class StoreController {
    
    @Autowired
    private StoreService storeService;
    
    @GetMapping
    public ResponseEntity<List<StoreDTO>> getAllStores() {
        List<StoreDTO> stores = storeService.getAllStores();
        return new ResponseEntity<>(stores, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<StoreDTO> getStoreById(@PathVariable Long id) {
        StoreDTO store = storeService.getStoreById(id);
        if (store != null) {
            return new ResponseEntity<>(store, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    
    @PostMapping
    public ResponseEntity<Store> createStore(@RequestBody StoreDTO storeDTO) {
        Store createdStore = storeService.createStore(storeDTO);
        return new ResponseEntity<>(createdStore, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Store> updateStore(@PathVariable Long id, @RequestBody StoreDTO storeDTO) {
        Store updatedStore = storeService.updateStore(id, storeDTO);
        if (updatedStore != null) {
            return new ResponseEntity<>(updatedStore, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStore(@PathVariable Long id) {
        boolean deleted = storeService.deleteStore(id);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
