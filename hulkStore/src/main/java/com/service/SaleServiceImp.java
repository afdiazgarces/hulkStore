/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.service;

import com.model.Sale;
import com.model.SaleMovements;
import com.model.User;
import com.persistence.SaleMovementsPersistence;
import com.persistence.SalePersistence;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Andres Felipe Diaz
 */
@Service
public class SaleServiceImp implements SaleService {

    @Autowired
    SalePersistence salePersistence;

    @Autowired
    SaleMovementsPersistence saleMovementsPersistence;

    @Override
    public void saveSale(Sale u) {
        salePersistence.save(u);
    }

    @Override
    public void deleteSale(int id) {
        salePersistence.deleteById(id);
    }

    @Override
    public List<Sale> listSale() {
        return salePersistence.findAll();
    }

    @Override
    public Optional<Sale> listSaleId(int id) {
        return salePersistence.findById(id);
    }

    @Override
    public List<SaleMovements> listSaleMovements(int idSale) {
        return saleMovementsPersistence.findAll();
    }

}
