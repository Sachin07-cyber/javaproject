package com.visitorpass.controller;

import com.visitorpass.entity.Resident;
import com.visitorpass.repository.FlatRepository;
import com.visitorpass.service.ResidentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ResidentPageController {

    private final ResidentService residentService;
    private final FlatRepository flatRepository;

    public ResidentPageController(ResidentService residentService, FlatRepository flatRepository) {
        this.residentService = residentService;
        this.flatRepository = flatRepository;
    }

    @GetMapping("/residents")
    public String showResidents(Model model) {
        model.addAttribute("residents", residentService.getAllResidents());
        return "resident-list";
    }

    @GetMapping("/residents/new")
    public String showForm(Model model) {
        model.addAttribute("resident", new Resident());
        model.addAttribute("flats", flatRepository.findAll());
        return "resident-form";
    }

    @PostMapping("/residents/register")
    public String registerResident(@ModelAttribute Resident resident, RedirectAttributes redirectAttributes) {
        try {
            residentService.createResident(resident);
            redirectAttributes.addFlashAttribute("successMessage", "Resident '" + resident.getName() + "' registered successfully!");
            return "redirect:/residents?success";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/residents/new?error";
        }
    }

    @PostMapping("/residents/delete/{id}")
    public String deleteResident(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            residentService.deleteResident(id);
            redirectAttributes.addFlashAttribute("successMessage", "Resident deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/residents";
    }
}