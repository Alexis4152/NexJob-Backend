package com.nexjob.platform.repository;

import com.nexjob.platform.entity.PostalCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostalCodeRepository extends JpaRepository<PostalCode, Long> {
    List<PostalCode> findByPostalCode(String postalCode);
}
