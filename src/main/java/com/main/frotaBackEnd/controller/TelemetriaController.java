package com.main.frotaBackEnd.controller;

import com.main.frotaBackEnd.model.Maquina;
import com.main.frotaBackEnd.model.RegistroOperacao;
import com.main.frotaBackEnd.model.TelemetriaDTO;
import com.main.frotaBackEnd.model.TelemetriaMaquina;
import com.main.frotaBackEnd.repository.MaquinaRepository;
import com.main.frotaBackEnd.repository.RegistroOperacaoRepository;
import com.main.frotaBackEnd.repository.TelemetriaMaquinaRepository;
import com.main.frotaBackEnd.repository.UserRepository;
import com.main.frotaBackEnd.model.UsuarioDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/telemetria")
public class TelemetriaController {

    @Autowired
    private MaquinaRepository maquinaRepository;

    @Autowired
    private TelemetriaMaquinaRepository telemetriaMaquinaRepository;

    @Autowired
    private RegistroOperacaoRepository registroOperacaoRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/maquina/{idMaquina}")
    public ResponseEntity<TelemetriaDTO> obterTelemetria(@PathVariable Long idMaquina) {
        UsuarioDTO user = (UsuarioDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Maquina maquina = maquinaRepository.findById(idMaquina)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Máquina não encontrada."));

        if (!"PROPRIETARIO".equals(user.getPerfil()) && !"SOCIO".equals(user.getPerfil())) {
            boolean vinculado = userRepository.verificaVinculo(user.getId_usuario(), idMaquina);
            if (!vinculado) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Você não tem permissão para acessar esta máquina.");
            }
        }

        TelemetriaDTO dto = montarTelemetriaDTO(maquina);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/em-operacao")
    public ResponseEntity<List<TelemetriaDTO>> obterTelemetriaEmOperacao() {
        UsuarioDTO user = (UsuarioDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!"PROPRIETARIO".equals(user.getPerfil()) && !"SOCIO".equals(user.getPerfil())) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Acesso negado.");
        }

        List<Maquina> maquinasEmOperacao = maquinaRepository.buscarEmOperacao();
        List<TelemetriaDTO> telemetrias = maquinasEmOperacao.stream()
                .map(this::montarTelemetriaDTO)
                .toList();

        return ResponseEntity.ok(telemetrias);
    }

    private TelemetriaDTO montarTelemetriaDTO(Maquina maquina) {
        TelemetriaDTO dto = new TelemetriaDTO();
        dto.setIdMaquina(maquina.getId());
        dto.setNomeMaquina(maquina.getNome());
        dto.setStatus(maquina.getStatus());

        boolean emOperacao = "Em Operacao".equals(maquina.getStatus());
        dto.setEmOperacao(emOperacao);

        List<RegistroOperacao> ativas = registroOperacaoRepository.buscarOperacoesAtivas(maquina.getId());
        if (!ativas.isEmpty()) {
            dto.setOperadorAtual(ativas.get(0).getOperador().getNome());
        } else {
            dto.setOperadorAtual(null);
        }

        Optional<TelemetriaMaquina> ultima = telemetriaMaquinaRepository.findTopByMaquinaIdOrderByDataAtualizacaoDesc(maquina.getId());
        if (ultima.isPresent()) {
            TelemetriaMaquina t = ultima.get();
            dto.setVelocidadeAtual(t.getVelocidadeAtual());
            dto.setConsumoAtual(t.getConsumoAtual());
            dto.setLatitude(t.getLatitude());
            dto.setLongitude(t.getLongitude());
            dto.setDataAtualizacao(t.getDataAtualizacao());
        }

        return dto;
    }
}
