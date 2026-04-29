package com.example.shop.controller;

import com.example.shop.entity.enums.DiscountType;
import com.example.shop.model.DiscountCode;
import com.example.shop.service.DiscountCodeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/discount-codes")
public class AdminDiscountCodeController {
    private final DiscountCodeService discountCodeService;

    public AdminDiscountCodeController(DiscountCodeService discountCodeService) {
        this.discountCodeService = discountCodeService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("discountCodes", discountCodeService.findAll());
        return "admin/discount-codes";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("discountCode", new DiscountCode());
        model.addAttribute("types", DiscountType.values());
        return "admin/discount-code-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        DiscountCode discountCode = discountCodeService.findById(id);
        if (discountCode == null) {
            redirectAttributes.addFlashAttribute("error", "Discount code not found.");
            return "redirect:/admin/discount-codes";
        }
        model.addAttribute("discountCode", discountCode);
        model.addAttribute("types", DiscountType.values());
        return "admin/discount-code-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute DiscountCode discountCode, RedirectAttributes redirectAttributes) {
        try {
            discountCodeService.save(discountCode);
            redirectAttributes.addFlashAttribute("success", "Saved discount code successfully.");
            return "redirect:/admin/discount-codes";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            if (discountCode.getId() == null) {
                return "redirect:/admin/discount-codes/add";
            }
            return "redirect:/admin/discount-codes/edit/" + discountCode.getId();
        }
    }
}
