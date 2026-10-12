package com.takima.backskeleton.services;

import com.takima.backskeleton.DAO.SageDao;
import com.takima.backskeleton.DTO.SageDto;
import com.takima.backskeleton.models.Sage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SageService {
    private final SageDao sageDao;
    private final SageMapper sageMapper;

    public SageService(SageDao sageDao, SageMapper sageMapper) {
        this.sageDao = sageDao;
        this.sageMapper = sageMapper;
    }

    public List<SageDto> findAll() {
        return sageDao.findAll().stream()
                .map((sageMapper::mapToDto))
                .toList();
    }

    public List<SageDto> findSageWithNoCreator(){
        return sageDao.findByCreatorIsNotNull().stream()
                .map(sageMapper::mapToDto)
                .toList();
    }
}
