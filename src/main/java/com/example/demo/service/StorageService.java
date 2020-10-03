/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo.service;

import com.example.demo.model.File;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Aunsha Asaithambi
 */
public interface StorageService {

    void init();

    void store(MultipartFile file);

    Stream<File> loadAll();

    Path load(String filename);

    Resource loadAsResource(String filename);

    void deleteAll();

    void addARecord(Integer id, String record);
}
