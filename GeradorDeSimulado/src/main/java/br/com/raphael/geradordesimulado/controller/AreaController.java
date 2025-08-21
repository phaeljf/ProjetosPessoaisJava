package br.com.raphael.geradordesimulado.controller;

import br.com.raphael.geradordesimulado.domain.Area;
import br.com.raphael.geradordesimulado.repository.AreaRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/areas")
public class AreaController {

    private final AreaRepository repo;

    public AreaController(AreaRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("areas", repo.findAllOrderByName());
        return "admin/areas/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("area", new Area());
        model.addAttribute("title", "Nova Área");
        return "admin/areas/form";
    }

    @GetMapping("/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Area area = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Área não encontrada"));
        model.addAttribute("area", area);
        model.addAttribute("title", "Editar Área");
        return "admin/areas/form";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute("area") Area area,
                       BindingResult result,
                       RedirectAttributes ra) {

        // Se já existem erros de validação (ex.: @NotBlank), volta pro form
        if (result.hasErrors()) return "admin/areas/form";

        // Normaliza nome (tira espaços nas pontas)
        String name = area.getName().trim();
        area.setName(name);

        // 1) Verificação de unicidade no app (antes de ir ao banco)
        boolean nomeJaUsado = (area.getId() == null)
                ? repo.existsByNameIgnoreCase(name)                                  // criando
                : repo.existsByNameIgnoreCaseAndIdNot(name, area.getId());           // editando

        if (nomeJaUsado) {
            // Coloca erro no campo "name" com a chave definida no messages.properties
            result.rejectValue("name", "area.name.unique");
            return "admin/areas/form";
        }

        // 2) Salva. Se, por qualquer corrida/condição, o banco acusar UNIQUE, tratamos abaixo.
        try {
            if (area.getId() == null) area.setActive(true);
            repo.save(area);
            ra.addFlashAttribute("msg", "flash.saved"); // ou ra.addFlashAttribute("msgText", "Salvo com sucesso!");
            return "redirect:/admin/areas";
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Fallback se o banco lançar violação de UNIQUE (código 23505 no Postgres)
            result.rejectValue("name", "area.name.unique");
            return "admin/areas/form";
        }
    }


    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        repo.findById(id).ifPresent(a -> {
            a.setActive(!a.isActive());
            repo.save(a);
        });
        ra.addFlashAttribute("msg", "Status alterado.");
        return "redirect:/admin/areas";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        repo.deleteById(id);
        ra.addFlashAttribute("msg", "Área removida.");
        return "redirect:/admin/areas";
    }
}
