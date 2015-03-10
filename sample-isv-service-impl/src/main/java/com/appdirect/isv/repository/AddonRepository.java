package com.appdirect.isv.repository;

import org.springframework.data.repository.CrudRepository;

import com.appdirect.isv.model.Addon;

public interface AddonRepository extends CrudRepository<Addon, Long> {
	Addon findByAddonIdentifier(String addonIdentifier);
}
