package com.takima.backskeleton.DAO;

import com.takima.backskeleton.DTO.SageDto;
import com.takima.backskeleton.models.Sage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SageDao extends JpaRepository<Sage, Integer> {
    List<Sage> findByCreatorIsNull();
    List<Sage> findByCreatorIsNotNull();
}
