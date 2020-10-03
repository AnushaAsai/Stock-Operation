/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo.service;

import java.util.stream.Stream;

/**
 *
 * @author Aunsha Asaithambi
 */
public interface FileOperationService {
    
    void storeStockTicker(String filename);
    
    Stream<String> queryByStockTicker(String stockTicker);
    
}
