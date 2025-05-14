package com.example.ShoesShop.Services.impl;

import com.example.ShoesShop.Entity.Option;
import com.example.ShoesShop.Repository.OptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OptionService {

    @Autowired
    private OptionRepository optionRepository;

    public List<Option> getAllOptions() {
        return optionRepository.findAll();
    }

    public Option getOptionById(Long id) {
        return optionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Option not found with id: " + id));
    }
    public Option createOption(Option option) {
        return optionRepository.save(option);
    }
    public Option updateOption(Long id, Option optionDetails) {
        Option option = getOptionById(id);
        option.setName(optionDetails.getName());
        return optionRepository.save(option);
    }
    public void deleteOption(Long id) {
        Option option = getOptionById(id);
        optionRepository.delete(option);
    }
}