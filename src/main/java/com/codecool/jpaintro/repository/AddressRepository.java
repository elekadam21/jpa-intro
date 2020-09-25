package com.codecool.jpaintro.repository;

import com.codecool.jpaintro.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AddressRepository extends JpaRepository<Address, Long> {

    @Query("update Address address set address.country = 'USA' where address.id in " +
            "(select student.address.id from Student student where student.name LIKE :name)")
    @Modifying(clearAutomatically = true)
    int updateAllToUSAByStudentName(@Param("name") String name);

}
