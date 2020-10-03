/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo.controller;

import com.example.demo.model.File;
import com.example.demo.model.FileOperation;
import com.example.demo.service.FileOperationService;
import com.example.demo.service.StorageFileNotFoundException;
import com.example.demo.service.StorageService;
import java.io.IOException;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author Aunsha Asaithambi
 */
@Controller
public class UploadController {

    private final StorageService storageService;
    private final FileOperationService fileOperationService;

    @Autowired
    public UploadController(StorageService storageService, FileOperationService fileOperationService) {
        this.storageService = storageService;
        this.fileOperationService = fileOperationService;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {
        model.addAttribute("files",storageService.loadAll().collect(Collectors.toList()));
        return "uploadForm";
    }
    
    @GetMapping("/stockTicker")
    public String queryFiles(@RequestParam (value = "stockTicker") String stockTicker, Model model) throws IOException {
        
        model.addAttribute("response", fileOperationService.queryByStockTicker(stockTicker).collect(Collectors.toList()));
        return "uploadForm";
    }
    
    @GetMapping("/addRecord")
    public String redirectToAddRecord(Model model) throws IOException {
        model.addAttribute("fileOperation", new FileOperation());
        model.addAttribute("files",storageService.loadAll().collect(Collectors.toList()));
        return "AddRecord";
    }
    
    @PostMapping("/addARecord")
    public String addARecordToAFile(@ModelAttribute FileOperation fileOperation,@RequestParam(value="fileId", required=false) Integer fileId){
        storageService.addARecord(fileId, fileOperation.getRecord());
        return "redirect:/";
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {

        storageService.store(file);
        
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
