package com.task.data_processor.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface DataProcessorService {

    ResponseEntity<?> loadAndProcessData(MultipartFile file1, MultipartFile file2);

}
