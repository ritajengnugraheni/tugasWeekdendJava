package com.purwadika.tugas.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.purwadika.tugas.dao.ProductRepo;
import com.purwadika.tugas.entity.Product;

@RestController
@RequestMapping("/products")
@CrossOrigin
public class ProductController {

	private String uploadPath = System.getProperty("user.dir") + "\\src\\main\\resources\\static\\images\\";
	
	@Autowired
	private ProductRepo productRepo;
	
	@PostMapping("/testCreateProduct")
	private Product testCreateProduct(@RequestBody Product product) {
		product.setId(0);
		return productRepo.save(product);
	}
	
	@GetMapping("/readProduct")
	private Iterable<Product> getAllProduct(){
		return productRepo.findAll();
	}
	
	@PostMapping("/createProduct")
	public String createProduct(@RequestParam("file") MultipartFile file, @RequestParam("productData") String productString) throws JsonMappingException, JsonProcessingException {
		Product product = new ObjectMapper().readValue(productString, Product.class);
		Date date = new Date();
		
		String fileExtension = file.getContentType().split("/")[1];
		String newFileName = "PRODUK-" + date.getTime() + "." + fileExtension;
		
		String fileName = StringUtils.cleanPath(newFileName);
		
		Path path = Paths.get(StringUtils.cleanPath(uploadPath) + fileName);
		
		try {
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
		

		String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/products/download/")
				.path(fileName).toUriString();
		
		product.setPhotoProduct(fileDownloadUri);
		
		productRepo.save(product);
		
		return fileDownloadUri;
		
		
	}
	@GetMapping("/download/{fileName:.+}")
	public ResponseEntity<Object> downloadFile(@PathVariable String fileName) {
		Path path = Paths.get(uploadPath + fileName);
		Resource resource = null;
		
		try {
			resource = new UrlResource(path.toUri());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/octet-stream"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}
	
	@DeleteMapping("/delete/{id}")
	public void deleteProductById(@PathVariable int id ) {
		Optional<Product> findProduct = productRepo.findById(id);
		
		if (findProduct.toString()=="Optional.empty")
			throw new RuntimeException("Product Not Found");
			
		productRepo.deleteById(id);
		
		
	}
	
	@PutMapping("/editProduct/{id}")
	public String editProduct(@RequestParam("file") MultipartFile file, @RequestParam("productData") String productString, @PathVariable int id) throws JsonMappingException, JsonProcessingException {
		
		Product findProduct = productRepo.findById(id).get();
		
		
		findProduct = new ObjectMapper().readValue(productString, Product.class);
		Date date = new Date();
		
		String fileExtension = file.getContentType().split("/")[1];
		String newFileName = "PRODUK-" + date.getTime() + "." + fileExtension;
		
		String fileName = StringUtils.cleanPath(newFileName);
		
		Path path = Paths.get(StringUtils.cleanPath(uploadPath) + fileName);
		
		try {
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
		

		String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/products/download/")
				.path(fileName).toUriString();
		
		
		findProduct.setPhotoProduct(fileDownloadUri);
		
		productRepo.save(findProduct);
		
		return fileDownloadUri;
	}
	
	
}
