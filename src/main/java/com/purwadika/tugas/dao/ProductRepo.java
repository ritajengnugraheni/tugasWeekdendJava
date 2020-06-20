package com.purwadika.tugas.dao;

import org.springframework.data.jpa.repository.JpaRepository;


import com.purwadika.tugas.entity.Product;

public interface ProductRepo extends JpaRepository<Product, Integer> {
	public Product findByProductName(String productName);
}
