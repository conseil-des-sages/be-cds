package com.takima.backskeleton.DAO;

import com.takima.backskeleton.models.CouncilSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouncilSeatDao extends JpaRepository<CouncilSeat, CouncilSeat.Key> {
    @Query("""
            select c from CouncilSeat c join fetch c.sage s
            where c.id.userId = :userId and s.deletedAt is null
            order by c.seat
            """)
    List<CouncilSeat> findCouncil(@Param("userId") Integer userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from CouncilSeat c where c.id.userId = :userId")
    void deleteCouncil(@Param("userId") Integer userId);

    @Modifying
    @Query("delete from CouncilSeat c where c.id.sageId = :sageId")
    void deleteBySage(@Param("sageId") Integer sageId);
}
