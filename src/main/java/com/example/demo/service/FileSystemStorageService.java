/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo.service;

import com.example.demo.model.File;
import com.example.demo.repository.DocumentRepository;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Aunsha Asaithambi
 */
@Service
public class FileSystemStorageService implements StorageService, FileOperationService {

    private final Path rootLocation;
    private final HashMap<String,List<String>> map;
    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
        this.map = new HashMap<>();
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }

    @Override
    public void store(MultipartFile file) {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + filename);
            }
            if (filename.contains("..")) {
                // This is a security check
                throw new StorageException(
                        "Cannot store file with relative path outside current directory "
                        + filename);
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, this.rootLocation.resolve(filename),
                        StandardCopyOption.REPLACE_EXISTING);
                this.uploadFile(filename);
                this.storeStockTicker(filename);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + filename, e);
        }
    }

    public File uploadFile(String filename) {
        Path file = load(filename);
        try {
            File document = new File(file.getFileName().toString(), file.toAbsolutePath().toString());
            return documentRepository.save(document);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
    
    public List<File> getFiles(){
	  return documentRepository.findAll();
    }

    @Override
    public Stream<File> loadAll() {
          return documentRepository.findAll().stream();
    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                System.out.println(resource);
                return resource;
            } else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + filename);

            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void storeStockTicker(String filename) {
        Path file = load(filename);
        if(Files.exists(file)){
            Stream<String> lines;
        try {
            lines = Files.lines(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        lines.skip(1).forEach(line -> {
            String[] field = line.split(",");
            List<String> list = map.getOrDefault(field[1],new ArrayList<>());
            if(!list.contains(file.toString())){
               list.add(file.toString()); 
            }
            map.put(field[1], list);
        });
        }
    }

    @Override
    public Stream<String> queryByStockTicker(String stockTicker) {
        Path p;
        Stream<String> lines = null;
        if (map.containsKey(stockTicker)) {
            System.out.println(stockTicker);
            List<String> l = map.get(stockTicker);
            for (String s : l) {
                p = Paths.get(s);
                if (Files.exists(p)) {
                    try {
                        lines = Files.lines(p).skip(1).filter(x->{
                            String[] field = x.split(",");
                            return field[1].equals(stockTicker);
                    });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return lines;
    }

    @Override
    public void addARecord(Integer id , String record) {
        Optional<File> document= documentRepository.findById(id);
        Path path = Paths.get(document.get().getLocation());
        if (Files.exists(path)) {
            try {
//                String textToAppend = "2,XOM,6/24/2011,$78.65,$81.12,$76.78,$76.78,118679791,-2.37762,18.06420424,100521400,$76.88,$82.01,6.67274,47,0.612139NEW";
                Files.write(path, record.getBytes(), StandardOpenOption.APPEND);
                        System.out.println(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
