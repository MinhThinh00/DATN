package com.example.ShoesShop.Services.impl;

import com.example.ShoesShop.DTO.StoreDTO;
import com.example.ShoesShop.Entity.Store;
import com.example.ShoesShop.Repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    public List<StoreDTO> getAllStores() {
        return storeRepository.findAll().stream().map(this::convertToDTO).toList();
    }

    public Store getStoreById(Long id) {
        return storeRepository.findById(id).orElse(null);
    }

    @Transactional
    public Store createStore(StoreDTO storeDTO) {
        validateStoreData(storeDTO, null);
        
        // Create new store
        Store store = new Store();
        updateStoreFromDTO(store, storeDTO);
        
        return storeRepository.save(store);
    }

    @Transactional
    public Store updateStore(Long id, StoreDTO storeDTO) {
        Optional<Store> storeOptional = storeRepository.findById(id);
        if (storeOptional.isPresent()) {
            // Validate store data
            validateStoreData(storeDTO, id);
            Store store = storeOptional.get();
            updateStoreFromDTO(store, storeDTO);
            
            return storeRepository.save(store);
        }
        return null;
    }

    @Transactional
    public boolean deleteStore(Long id) {
        if (storeRepository.existsById(id)) {
            storeRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    private void validateStoreData(StoreDTO storeDTO, Long storeId) {
        if (storeDTO.getName() == null || storeDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Store name cannot be empty");
        }
        if (storeDTO.getEmail() == null || storeDTO.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Store email cannot be empty");
        }

        if (storeRepository.existsByName(storeDTO.getName())) {
            if (storeId == null || !storeRepository.findById(storeId).get().getName().equals(storeDTO.getName())) {
                throw new IllegalArgumentException("Store name already exists");
            }
        }
        if (storeRepository.existsByEmail(storeDTO.getEmail())) {
            if (storeId == null || !storeRepository.findById(storeId).get().getEmail().equals(storeDTO.getEmail())) {
                throw new IllegalArgumentException("Store email already exists");
            }
        }
    }
    
    private void updateStoreFromDTO(Store store, StoreDTO storeDTO) {
        store.setName(storeDTO.getName());
        store.setAddress(storeDTO.getAddress());
        store.setPhone(storeDTO.getPhone());
        store.setEmail(storeDTO.getEmail());
    }

    public StoreDTO convertToDTO(Store store) {
        StoreDTO storeDTO = new StoreDTO();
        storeDTO.setId(store.getId());
        storeDTO.setName(store.getName());
        storeDTO.setAddress(store.getAddress());
        storeDTO.setPhone(store.getPhone());
        storeDTO.setEmail(store.getEmail());
        return storeDTO;
    }
}