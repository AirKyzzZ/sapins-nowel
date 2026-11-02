package com.example.nowel.repository;

import org.springframework.data.repository.CrudRepository;

import com.example.nowel.entity.Client;

public interface ClientRepository extends CrudRepository<Client, Long> {
}
