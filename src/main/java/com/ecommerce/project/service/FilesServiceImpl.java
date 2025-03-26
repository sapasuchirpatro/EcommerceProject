package com.ecommerce.project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FilesServiceImpl implements FilesService{

    @Override
    public String uploadImage(String path, MultipartFile image) throws IOException {

        // File name of current / original file
        String originalFileName = image.getOriginalFilename();

        // Generate a unique file name to stop override if already a file exist with the same name
        String randomId = UUID.randomUUID().toString();
        String newFileName;
        if (originalFileName != null) {
            newFileName = randomId + originalFileName.substring(originalFileName.lastIndexOf("."));
        } else {
            newFileName = randomId + ".png";
        }

        // Used File.separator to make it OS independent
        String filePath = path + File.separator + newFileName;

        // Check if path exist and create
        File folder = new File(path);
        folder.mkdir();

        // Upload to server
        Files.copy(image.getInputStream(), Paths.get(filePath));

        // Return the file name
        return newFileName;
    }
}
