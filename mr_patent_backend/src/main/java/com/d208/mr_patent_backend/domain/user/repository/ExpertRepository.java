package com.d208.mr_patent_backend.domain.user.repository;

import com.d208.mr_patent_backend.domain.user.entity.Expert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpertRepository extends JpaRepository<Expert, Integer> {

}