package br.com.raphael.simuladorparaconcurso.web;

import br.com.raphael.simuladorparaconcurso.modelo.*;
import br.com.raphael.simuladorparaconcurso.repositorio.AreaConhecimentoRepositorio;
import br.com.raphael.simuladorparaconcurso.servico.SimuladoServico;
import br.com.raphael.simuladorparaconcurso.servico.ServicoPdf;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.coyote.BadRequestException;
import org.thymeleaf.context.WebContext;

import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Controller
public class SimuladoControlador {

    private final AreaConhecimentoRepositorio areaRepo;
    private final SimuladoServico simuladoServico;

    // PDF
    private final SpringTemplateEngine templateEngine;
    private final ServicoPdf servicoPdf;

    public SimuladoControlador(AreaConhecimentoRepositorio areaRepo,
                               SimuladoServico simuladoServico,
                               SpringTemplateEngine templateEngine,
                               ServicoPdf servicoPdf) {
        this.areaRepo = areaRepo;
        this.simuladoServico = simuladoServico;
        this.templateEngine = templateEngine;
        this.servicoPdf = servicoPdf;
    }

    // Landing (lista de áreas para configurar)
    @GetMapping("/configurar")
    public String configurar(Model model) {
        model.addAttribute("areas", areaRepo.findByAtivoTrueOrderByNomeAsc());
        return "configurar";
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }



    @PostMapping("/iniciar")
    public String iniciar(@RequestParam String nome,
                          @RequestParam(defaultValue = "0") int horas,
                          @RequestParam(defaultValue = "0") int minutos,
                          @RequestParam List<Long> areaId,
                          @RequestParam List<Integer> quantidade,
                          HttpSession session) throws BadRequestException {

        // (area -> quantidade) preservando ordem
        LinkedHashMap<Long, Integer> mapa = new LinkedHashMap<>();
        for (int i = 0; i < areaId.size(); i++) {
            Integer q = quantidade.get(i);
            if (q != null && q > 0) mapa.put(areaId.get(i), q);
        }
        if (mapa.isEmpty()) return "redirect:/";

        // sorteia e monta
        List<QuestaoExibicao> questoes = simuladoServico.montarSimulado(mapa);

        // escolhas legíveis
        List<EscolhaArea> escolhas = new ArrayList<>();
        for (Map.Entry<Long, Integer> e : mapa.entrySet()) {
            String nomeArea = areaRepo.findById(e.getKey()).orElseThrow().getNome();
            escolhas.add(new EscolhaArea(e.getKey(), nomeArea, e.getValue()));
        }

        // sessão
        SessaoSimulado s = new SessaoSimulado();
        s.nomeCandidato = nome;
        s.escolhas = escolhas;
        s.questoes = questoes;
        s.indiceAtual = 0;

        long duracaoMin = (long) horas * 60 + minutos;
        s.fim = duracaoMin > 0 ? Instant.now().plusSeconds(duracaoMin * 60) : null;

        session.setAttribute("sessaoSimulado", s);
        return "redirect:/simulado";
    }

    @GetMapping("/simulado")
    public String simulado(@RequestParam(required = false) Integer q, Model model, HttpSession session) {
        SessaoSimulado s = (SessaoSimulado) session.getAttribute("sessaoSimulado");
        if (s == null) return "redirect:/";
        if (s.fim != null && Instant.now().isAfter(s.fim)) return "redirect:/resultado";

        if (q != null) s.indiceAtual = Math.max(0, Math.min(q, s.questoes.size() - 1));
        QuestaoExibicao atual = s.questoes.get(s.indiceAtual);

        Long restanteMs = null;
        if (s.fim != null) restanteMs = Duration.between(Instant.now(), s.fim).toMillis();

        model.addAttribute("q", atual);
        model.addAttribute("pos", s.indiceAtual);
        model.addAttribute("total", s.questoes.size());
        model.addAttribute("nome", s.nomeCandidato);
        model.addAttribute("restanteMs", restanteMs);
        return "exam";
    }

    @PostMapping("/responder")
    public String responder(@RequestParam int pos,
                            @RequestParam(required = false) String alternativa,
                            @RequestParam String acao,
                            HttpSession session) {
        SessaoSimulado s = (SessaoSimulado) session.getAttribute("sessaoSimulado");
        if (s == null) return "redirect:/";
        if (s.fim != null && Instant.now().isAfter(s.fim)) return "redirect:/resultado";

        if (alternativa != null && !alternativa.isBlank()) {
            s.questoes.get(pos).escolhida = alternativa.substring(0, 1).toUpperCase();
        }

        if ("ant".equals(acao)) s.indiceAtual = Math.max(pos - 1, 0);
        if ("prox".equals(acao)) s.indiceAtual = Math.min(pos + 1, s.questoes.size() - 1);
        if ("fim".equals(acao)) return "redirect:/resultado";

        return "redirect:/simulado?q=" + s.indiceAtual;
    }

    @PostMapping("/finalizar")
    public String finalizar() {
        return "redirect:/resultado";
    }

    @GetMapping("/resultado")
    public String resultado(Model model, HttpSession session) {
        SessaoSimulado s = (SessaoSimulado) session.getAttribute("sessaoSimulado");
        if (s == null) return "redirect:/";
        preencherResumoResultado(model, s);
        return "result";
    }

    // ---------- PDF ----------
    @GetMapping("/resultado/pdf")
    public ResponseEntity<byte[]> baixarPdf(HttpServletRequest req,
                                            HttpServletResponse res,
                                            HttpSession session,
                                            Model model) {
        SessaoSimulado s = (SessaoSimulado) session.getAttribute("sessaoSimulado");
        if (s == null) {
            return ResponseEntity.status(302).header(HttpHeaders.LOCATION, "/").build();
        }

        // Reaproveita a mesma montagem do resultado
        preencherResumoResultado(model, s);

        // Base URL para resolver CSS/IMG no PDF
        String baseUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath().build().toUriString() + "/";

        // Thymeleaf Context “puro” (compatível com todas as versões)
        Context ctx = new Context(req.getLocale());
        ctx.setVariables(model.asMap());
        ctx.setVariable("baseUrl", baseUrl); // vamos usar no template

        String html = templateEngine.process("resultado-pdf", ctx);

        byte[] pdf = servicoPdf.htmlParaPdf(html, baseUrl);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=resultado.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ---------- utilitário ----------
    private void preencherResumoResultado(Model model, SessaoSimulado s) {
        int acertos = simuladoServico.corrigir(s.questoes);
        int total = s.questoes.size();
        model.addAttribute("nome", s.nomeCandidato);
        model.addAttribute("acertos", acertos);
        model.addAttribute("erros", total - acertos);
        model.addAttribute("total", total);
        model.addAttribute("questoes", s.questoes);
    }
}
