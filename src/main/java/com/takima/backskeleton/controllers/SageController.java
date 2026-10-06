package com.takima.backskeleton.controllers;

import com.takima.backskeleton.DTO.SageDto;
import com.takima.backskeleton.models.Sage;
import com.takima.backskeleton.services.SageService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RequestMapping("sage")
@RestController
public class SageController {
    private final SageService sageService;

    public SageController(SageService sageService) {
        this.sageService = sageService;
    }

    @GetMapping("/")
    public List<SageDto> findAll() {
        return sageService.findAll();
    }

    @GetMapping("/no-creator")
    public List<SageDto> findNoCreator(){
        return sageService.findSageWithNoCreator();
    }
}
