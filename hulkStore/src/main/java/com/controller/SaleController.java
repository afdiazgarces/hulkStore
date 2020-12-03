/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.controller;

import com.google.gson.Gson;
import com.model.Product;
import com.model.Sale;
import com.model.SaleMovements;
import com.persistence.SaleMovementsPersistence;
import com.service.ProductService;
import com.service.SaleService;
import com.service.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 *
 * @author Andres Felipe Diaz
 */
@Controller
public class SaleController {

    @Autowired
    SaleService saleService;

    @Autowired
    ProductService productService;

    @Autowired
    UserService userService;

    @Autowired
    SaleMovementsPersistence saleMovementsPersistence;

    @GetMapping("/listSale")
    public String listSale(Model model) {
        model.addAttribute("sales", saleService.listSale());
        return "listSale";
    }

    @GetMapping("/newSale")
    public String add(Model model) {
        model.addAttribute("sale", new Sale());
        model.addAttribute("sm", new SaleMovements());
        model.addAttribute("products", productService.listProduct());
        model.addAttribute("users", userService.listUser());
        return "formSale";
    }

    @PostMapping("/saveSale")
    public String save(@Valid Sale s, @Valid SaleMovements sm) {

        saleService.saveSale(s);

        s.setName("COT-" + s.getId());

        saleService.saveSale(s);

        saveSaleMovements(s, sm);

        deleteSaleMovements(sm);

        return "redirect:/listSale";
    }

    private void saveSaleMovements(Sale s, SaleMovements sm) {
        JSONArray jsonArray = new JSONArray(sm.getName_item());

        for (int i = 0; i < jsonArray.length(); i++) {

            Product pro = new Product();
            try {
                pro.setId(jsonArray.getJSONObject(i).getJSONObject("product").getInt("id"));
            } catch (Exception e) {
                pro.setId(jsonArray.getJSONObject(i).getInt("id"));
            }

            SaleMovements sale_m = new SaleMovements();

            sale_m.setSale(s);
            sale_m.setId(jsonArray.getJSONObject(i).getInt("id"));
            sale_m.setName_item(jsonArray.getJSONObject(i).getString("name"));
            sale_m.setPrice_item(jsonArray.getJSONObject(i).getDouble("price"));
            sale_m.setQuantity_item(jsonArray.getJSONObject(i).getInt("cant"));
            sale_m.setTotal_item(jsonArray.getJSONObject(i).getDouble("total"));
            sale_m.setProduct(pro);

            saleMovementsPersistence.save(sale_m);

            int cant_update = 0;

            try {
                cant_update = jsonArray.getJSONObject(i).getInt("quantity_item");
            } catch (Exception e) {
                cant_update = 0;
            }

            updateStock(pro, sale_m, cant_update);

        }
    }

    private void deleteSaleMovements(SaleMovements sm) {
        JSONArray jsonArray = new JSONArray(sm.getDesc_item());

        for (int i = 0; i < jsonArray.length(); i++) {

            Product pro = new Product();
            pro.setId(jsonArray.getJSONObject(i).getJSONObject("product").getInt("id"));

            SaleMovements sale_m = new SaleMovements();
            sale_m.setQuantity_item(0);

            int cant_update = jsonArray.getJSONObject(i).getInt("quantity_item");

            updateStock(pro, sale_m, cant_update);

            saleMovementsPersistence.deleteById(jsonArray.getJSONObject(i).getInt("id"));

        }
    }

    private void updateStock(Product pro, SaleMovements sale_m, int cant_update) {
        Optional<Product> producto = productService.listProductId(pro.getId());
        producto.get().setStock((producto.get().getStock() + cant_update) - sale_m.getQuantity_item());
        productService.saveProduct(producto.get());
    }

    @GetMapping("/updateSale/{id}")
    public String update(Model model, @PathVariable int id) {

        List<SaleMovements> sale_movements = new ArrayList<>();

        saleService.listSaleMovements(id).stream().filter((saleMovements) -> (saleMovements.getSale().getId() == id)).forEachOrdered((saleMovements) -> {
            sale_movements.add(saleMovements);
        });

        String json_sale_movements = new Gson().toJson(sale_movements);

        SaleMovements sm = new SaleMovements();
        sm.setName_item(json_sale_movements);

        model.addAttribute("sale", saleService.listSaleId(id));
        model.addAttribute("sm", sm);
        model.addAttribute("products", productService.listProduct());
        model.addAttribute("users", userService.listUser());

        return "formSale";

    }

    /*
    @GetMapping("/deleteProduct/{id}")
    public String delete(@PathVariable int id) {
        productService.deleteProduct(id);
        return "redirect:/listProduct";

    }
    

     */
    @GetMapping("/detailSale/{id}")
    public String detail(@PathVariable int id, Model model) {

        List<SaleMovements> sale_movements = new ArrayList<>();

        saleService.listSaleMovements(id).stream().filter((saleMovements) -> (saleMovements.getSale().getId() == id)).forEachOrdered((saleMovements) -> {
            sale_movements.add(saleMovements);
        });

        model.addAttribute("sale", saleService.listSaleId(id).get());
        model.addAttribute("sale_movements", sale_movements);
        return "detailSale";

    }
}
