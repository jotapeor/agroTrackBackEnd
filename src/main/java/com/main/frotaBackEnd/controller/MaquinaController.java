package com.main.frotaBackEnd.controller;

import com.main.frotaBackEnd.model.Fazenda;
import com.main.frotaBackEnd.model.MaquinaDTO;
import com.main.frotaBackEnd.model.Talhao;
import com.main.frotaBackEnd.model.UsuarioDTO;
import com.main.frotaBackEnd.service.MaquinaService;
import com.main.frotaBackEnd.service.TokenService;
import com.main.frotaBackEnd.repository.FazendaRepository;
import com.main.frotaBackEnd.repository.TalhaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/proprietario/maquinas")
public class MaquinaController {

    @Autowired
    private MaquinaService maquinaService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private FazendaRepository fazendaRepository;

    @Autowired
    private TalhaoRepository talhaoRepository;

    @Autowired
    private com.main.frotaBackEnd.repository.MaquinaRepository maquinaRepository;

    @Autowired
    private com.main.frotaBackEnd.repository.UserRepository userRepository;

    @Autowired
    private com.main.frotaBackEnd.service.HistoricoMaquinaService historicoMaquinaService;

    @PreAuthorize("hasRole('PROPRIETARIO')")
    @PostMapping
    public ResponseEntity<Map<String, String>> cadastrar(
            @RequestParam("nome") String nome,
            @RequestParam("tipo") String tipo,
            @RequestParam("modelo") String modelo,
            @RequestParam("ano") int ano,
            @RequestParam("hodometro_inicial") String hodometroInicial,
            @RequestParam(value = "marca", required = false) String marca,
            @RequestParam(value = "numero_serie", required = false) String numeroSerie,
            @RequestParam(value = "placa", required = false) String placa,
            @RequestParam(value = "capacidade_tanque", required = false) String capacidadeTanque,
            @RequestParam(value = "tipo_combustivel", required = false) String tipoCombustivel,
            @RequestParam(value = "intervalo_troca_oleo_horas", required = false) String intervaloTrocaOleo,
            @RequestParam(value = "intervalo_inspecao_horas", required = false) String intervaloInspecao,
            @RequestParam(value = "consumo_medio", required = false) String consumoMedio,
            @RequestParam(value = "id_fazenda", required = false) String idFazenda,
            @RequestParam(value = "id_talhao", required = false) String idTalhao,
            @RequestParam(value = "data_aquisicao", required = false) String dataAquisicao,
            @RequestParam(value = "valor_aquisicao", required = false) String valorAquisicao,
            @RequestParam(value = "observacoes", required = false) String observacoes,
            @RequestParam(value = "foto", required = false) MultipartFile foto) {

        MaquinaDTO dto = new MaquinaDTO();
        dto.setNome(nome);
        dto.setTipo(tipo);
        dto.setMarca(marca);
        dto.setModelo(modelo);
        dto.setAno(ano);
        dto.setNumeroSerie(numeroSerie);
        dto.setPlaca(placa);

        try {
            dto.setHodometroInicial(new java.math.BigDecimal(hodometroInicial));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Horímetro inicial inválido.");
        }

        if (capacidadeTanque != null && !capacidadeTanque.isEmpty())
            dto.setCapacidadeTanque(new java.math.BigDecimal(capacidadeTanque));
        dto.setTipoCombustivel(tipoCombustivel);

        if (intervaloTrocaOleo != null && !intervaloTrocaOleo.isEmpty())
            dto.setIntervaloTrocaOleoHoras(Integer.parseInt(intervaloTrocaOleo));
        if (intervaloInspecao != null && !intervaloInspecao.isEmpty())
            dto.setIntervaloInspecaoHoras(Integer.parseInt(intervaloInspecao));
        if (consumoMedio != null && !consumoMedio.isEmpty())
            dto.setConsumoMedio(new java.math.BigDecimal(consumoMedio));

        if (idFazenda != null && !idFazenda.isEmpty())
            dto.setIdFazenda(Long.parseLong(idFazenda));
        if (idTalhao != null && !idTalhao.isEmpty())
            dto.setIdTalhao(Long.parseLong(idTalhao));

        dto.setDataAquisicao(dataAquisicao);
        if (valorAquisicao != null && !valorAquisicao.isEmpty())
            dto.setValorAquisicao(new java.math.BigDecimal(valorAquisicao));
        dto.setObservacoes(observacoes);

        maquinaService.cadastrar(dto, foto);
        return ResponseEntity.ok(Map.of("message", "Máquina cadastrada com sucesso!"));
    }

    @GetMapping
    public ResponseEntity<List<MaquinaDTO>> listar() {
        UsuarioDTO user = (UsuarioDTO) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if ("PROPRIETARIO".equals(user.getPerfil()) || "SOCIO".equals(user.getPerfil())) {
            return ResponseEntity.ok(maquinaService.listarTodas());
        }
        return ResponseEntity.ok(maquinaService.listarPorUsuario(user.getId_usuario()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaquinaDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(maquinaService.buscarPorId(id));
    }

    @PreAuthorize("hasRole('PROPRIETARIO')")
    @PostMapping("/{id}")
    public ResponseEntity<Map<String, String>> atualizar(
            @PathVariable Long id,
            @RequestParam("nome") String nome,
            @RequestParam("tipo") String tipo,
            @RequestParam("modelo") String modelo,
            @RequestParam("ano") int ano,
            @RequestParam("hodometro_inicial") String hodometroInicial,
            @RequestParam(value = "marca", required = false) String marca,
            @RequestParam(value = "numero_serie", required = false) String numeroSerie,
            @RequestParam(value = "placa", required = false) String placa,
            @RequestParam(value = "capacidade_tanque", required = false) String capacidadeTanque,
            @RequestParam(value = "tipo_combustivel", required = false) String tipoCombustivel,
            @RequestParam(value = "intervalo_troca_oleo_horas", required = false) String intervaloTrocaOleo,
            @RequestParam(value = "intervalo_inspecao_horas", required = false) String intervaloInspecao,
            @RequestParam(value = "consumo_medio", required = false) String consumoMedio,
            @RequestParam(value = "id_fazenda", required = false) String idFazenda,
            @RequestParam(value = "id_talhao", required = false) String idTalhao,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "nivel_risco", required = false) String nivelRisco,
            @RequestParam(value = "data_aquisicao", required = false) String dataAquisicao,
            @RequestParam(value = "valor_aquisicao", required = false) String valorAquisicao,
            @RequestParam(value = "observacoes", required = false) String observacoes,
            @RequestParam(value = "foto", required = false) MultipartFile foto) {

        MaquinaDTO dto = new MaquinaDTO();
        dto.setNome(nome);
        dto.setTipo(tipo);
        dto.setMarca(marca);
        dto.setModelo(modelo);
        dto.setAno(ano);
        dto.setNumeroSerie(numeroSerie);
        dto.setPlaca(placa);
        dto.setStatus(status);
        dto.setNivelRisco(nivelRisco);

        try {
            dto.setHodometroInicial(new java.math.BigDecimal(hodometroInicial));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Horímetro inicial inválido.");
        }

        if (capacidadeTanque != null && !capacidadeTanque.isEmpty())
            dto.setCapacidadeTanque(new java.math.BigDecimal(capacidadeTanque));
        dto.setTipoCombustivel(tipoCombustivel);
        if (intervaloTrocaOleo != null && !intervaloTrocaOleo.isEmpty())
            dto.setIntervaloTrocaOleoHoras(Integer.parseInt(intervaloTrocaOleo));
        if (intervaloInspecao != null && !intervaloInspecao.isEmpty())
            dto.setIntervaloInspecaoHoras(Integer.parseInt(intervaloInspecao));
        if (consumoMedio != null && !consumoMedio.isEmpty())
            dto.setConsumoMedio(new java.math.BigDecimal(consumoMedio));
        if (idFazenda != null && !idFazenda.isEmpty())
            dto.setIdFazenda(Long.parseLong(idFazenda));
        if (idTalhao != null && !idTalhao.isEmpty())
            dto.setIdTalhao(Long.parseLong(idTalhao));
        dto.setDataAquisicao(dataAquisicao);
        if (valorAquisicao != null && !valorAquisicao.isEmpty())
            dto.setValorAquisicao(new java.math.BigDecimal(valorAquisicao));
        dto.setObservacoes(observacoes);

        maquinaService.atualizar(id, dto, foto);
        return ResponseEntity.ok(Map.of("message", "Máquina atualizada com sucesso!"));
    }

    @PreAuthorize("hasRole('PROPRIETARIO')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> excluir(@PathVariable Long id) {
        maquinaService.excluir(id);
        return ResponseEntity.ok(Map.of("message", "Máquina arquivada com sucesso!"));
    }

    @GetMapping("/fazendas")
    public ResponseEntity<List<Fazenda>> listarFazendas() {
        return ResponseEntity.ok(fazendaRepository.findAllByAtivoTrueOrderByNome());
    }

    @GetMapping("/talhoes")
    public ResponseEntity<List<Talhao>> listarTalhoes(
            @RequestParam(value = "id_fazenda", required = false) Long idFazenda) {
        if (idFazenda != null) {
            return ResponseEntity.ok(talhaoRepository.buscarPorFazenda(idFazenda));
        }
        return ResponseEntity.ok(talhaoRepository.findAll());
    }

    @PreAuthorize("hasRole('PROPRIETARIO')")
    @PostMapping("/{id}/autorizar-risco")
    public ResponseEntity<Map<String, String>> autorizarRisco(
            @PathVariable Long id,
            @RequestBody com.main.frotaBackEnd.model.AutorizacaoRiscoDTO dto) {

        if (dto.getJustificativa() == null || dto.getJustificativa().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "A justificativa é obrigatória.");
        }

        UsuarioDTO usuario = (UsuarioDTO) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        maquinaService.autorizarRisco(id, dto.getJustificativa(), usuario.getId_usuario());

        return ResponseEntity.ok(Map.of("message", "Operação temporária autorizada com sucesso."));
    }

    @PreAuthorize("hasAnyRole('PROPRIETARIO', 'SOCIO', 'OPERADOR')")
    @GetMapping("/{idMaquina}/historico-completo")
    public ResponseEntity<List<com.main.frotaBackEnd.model.HistoricoEventoDTO>> obterHistoricoCompleto(
            @PathVariable Long idMaquina,
            @RequestHeader("Authorization") String tokenHeader) {

        UsuarioDTO userDTO = tokenService.extrairClaim(tokenHeader.substring(7));
        Long idUsuario = userDTO.getId_usuario();
        String role = userDTO.getPerfil();

        if ("OPERADOR".equals(role)) {
            boolean vinculado = userRepository.verificaVinculo(idUsuario, idMaquina);
            if (!vinculado) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Máquina não vinculada ao operador");
            }
        }

        List<com.main.frotaBackEnd.model.HistoricoEventoDTO> historico = historicoMaquinaService.obterHistoricoCompleto(idMaquina);
        return ResponseEntity.ok(historico);
    }
}
