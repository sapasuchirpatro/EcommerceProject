package com.ecommerce.project.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FilesService {

    String uploadImage(String path, MultipartFile image) throws IOException;
}
