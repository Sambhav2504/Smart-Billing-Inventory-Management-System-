package com.smartretail.backend.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {
    String uploadImage(MultipartFile file, String fileName) throws IOException;
    byte[] getImage(String imageId);
}