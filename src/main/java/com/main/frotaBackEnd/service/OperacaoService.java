package com.main.frotaBackEnd.service;

import com.main.frotaBackEnd.model.*;
import com.main.frotaBackEnd.repository.MaquinaRepository;
import com.main.frotaBackEnd.repository.RegistroOperacaoRepository;
import com.main.frotaBackEnd.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OperacaoService {

    @Autowired
    private MaquinaRepository maquinaRepository;

    @Autowired
    private RegistroOperacaoRepository registroOperacaoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClassificacaoRiscoService classificacaoRiscoService;

    @Autowired
    private AbastecimentoService abastecimentoService;

    @Transactional
    public RegistroOperacaoDTO trocarStatus(Long idMaquina, TrocaStatusDTO dto, Long idUsuarioLogado, String perfilUsuario) {
        Maquina maquina = maquinaRepository.findById(idMaquina)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Máquina não encontrada."));

        Usuario usuario = userRepository.findById(idUsuarioLogado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Usuário não encontrado."));

        if (!"PROPRIETARIO".equals(perfilUsuario) && !"SOCIO".equals(perfilUsuario)) {
            boolean vinculado = userRepository.verificaVinculo(idUsuarioLogado, idMaquina);
            if (!vinculado) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Você não tem permissão para operar esta máquina.");
            }
        }

        String statusAtual = maquina.getStatus();
        String novoStatus = dto.getNovoStatus();

        if (novoStatus.equals(statusAtual)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "A máquina já está neste status.");
        }

        RegistroOperacaoDTO resumo = null;

        if ("Em Operacao".equals(novoStatus)) {
            if (!dto.isConfirmacao()) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Confirmação é obrigatória para iniciar a operação.");
            }
            if (("Pulverizador".equals(maquina.getTipo()) || "Semeadeira".equals(maquina.getTipo())) && dto.getPesoCarregado() == null) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Peso/volume carregado é obrigatório para pulverizadores e semeadeiras.");
            }

            if ("Alto".equals(maquina.getNivelRisco())) {
                if (!maquina.isAutorizadaOperacaoRisco()) {
                    throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Máquina em nível de risco ALTO. É necessária autorização prévia do proprietário.");
                }
                maquina.setAutorizadaOperacaoRisco(false);
            }
            
            RegistroOperacao registro = new RegistroOperacao();
            registro.setMaquina(maquina);
            registro.setOperador(usuario);
            registro.setDataInicio(LocalDateTime.now());
            registro.setHodometroInicio(maquina.getHodometroInicial());
            registro.setPesoCarregado(dto.getPesoCarregado());
            
            registroOperacaoRepository.save(registro);

        } else if ("Em Operacao".equals(statusAtual)) {
            if (dto.getObservacoes() == null || dto.getObservacoes().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Observações são obrigatórias ao encerrar uma operação.");
            }
            if (dto.getHodometroFim() == null) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Hodômetro final é obrigatório ao encerrar uma operação.");
            }
            if (dto.getHodometroFim().compareTo(maquina.getHodometroInicial()) < 0) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Hodômetro final não pode ser menor que o inicial.");
            }

            List<RegistroOperacao> ativas = registroOperacaoRepository.buscarOperacoesAtivas(idMaquina);
            if (!ativas.isEmpty()) {
                RegistroOperacao registro = ativas.get(0);
                registro.setDataFim(LocalDateTime.now());
                registro.setHodometroFim(dto.getHodometroFim());
                registro.setObservacoes(dto.getObservacoes());
                
                registroOperacaoRepository.save(registro);
                
                maquina.setHodometroInicial(dto.getHodometroFim());
                
                resumo = toDTO(registro);
                
                classificacaoRiscoService.recalcularRisco(maquina);
                abastecimentoService.verificarAlertaPreventivo(maquina, dto.getHodometroFim());
            }
        }

        maquina.setStatus(novoStatus);
        maquinaRepository.save(maquina);

        return resumo;
    }

    public List<RegistroOperacaoDTO> listarHistoricoMaquina(Long idMaquina) {
        return registroOperacaoRepository.buscarPorMaquinaId(idMaquina).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private RegistroOperacaoDTO toDTO(RegistroOperacao r) {
        RegistroOperacaoDTO dto = new RegistroOperacaoDTO();
        dto.setId(r.getId());
        dto.setIdMaquina(r.getMaquina().getId());
        dto.setNomeMaquina(r.getMaquina().getNome());
        dto.setIdOperador(r.getOperador().getId_usuario());
        dto.setNomeOperador(r.getOperador().getNome());
        dto.setDataInicio(r.getDataInicio());
        dto.setDataFim(r.getDataFim());
        dto.setHodometroInicio(r.getHodometroInicio());
        dto.setHodometroFim(r.getHodometroFim());
        dto.setPesoCarregado(r.getPesoCarregado());
        dto.setObservacoes(r.getObservacoes());

        if (r.getDataFim() != null) {
            long minutes = Duration.between(r.getDataInicio(), r.getDataFim()).toMinutes();
            dto.setHorasOperadas(BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));
            if (r.getHodometroFim() != null) {
                dto.setKmRodados(r.getHodometroFim().subtract(r.getHodometroInicio()));
            }
        }
        return dto;
    }
}
