package com.appdirect.isv.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.appdirect.isv.model.Addon;

public interface AddonRepository extends PagingAndSortingRepository<Addon, Long> {
}
