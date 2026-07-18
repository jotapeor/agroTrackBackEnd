package com.main.frotaBackEnd.service;

import com.main.frotaBackEnd.model.*;
import com.main.frotaBackEnd.repository.AbastecimentoRepository;
import com.main.frotaBackEnd.repository.MaquinaRepository;
import com.main.frotaBackEnd.repository.NotificacaoRepository;
import com.main.frotaBackEnd.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AbastecimentoService {

    @Autowired
    private AbastecimentoRepository abastecimentoRepository;

    @Autowired
    private MaquinaRepository maquinaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Transactional
    public Abastecimento registrarAbastecimento(Long idMaquina, AbastecimentoDTO dto, Long idUsuarioLogado, String perfilUsuario) {
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

        if (dto.getHodometroAtual().compareTo(maquina.getHodometroInicial()) < 0) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Hodômetro informado não pode ser menor que o último registrado.");
        }

        Abastecimento abastecimento = new Abastecimento();
        abastecimento.setMaquina(maquina);
        abastecimento.setOperador(usuario);
        abastecimento.setDataAbastecimento(dto.getDataAbastecimento() != null ? dto.getDataAbastecimento() : LocalDateTime.now());
        abastecimento.setLitros(dto.getLitros());
        abastecimento.setTipoCombustivel(dto.getTipoCombustivel());
        abastecimento.setHodometroAtual(dto.getHodometroAtual());

        abastecimento = abastecimentoRepository.save(abastecimento);

        recalcularConsumo(maquina, abastecimento);

        maquina.setHodometroInicial(dto.getHodometroAtual());
        verificarAlertaPreventivo(maquina, dto.getHodometroAtual());

        maquinaRepository.save(maquina);

        return abastecimento;
    }

    private void recalcularConsumo(Maquina maquina, Abastecimento abastecimentoAtual) {
        List<Abastecimento> historico = abastecimentoRepository.buscarPorMaquinaId(maquina.getId());
        if (historico.size() < 2) return; 

        Abastecimento anterior = historico.get(1);
        BigDecimal kmRodados = abastecimentoAtual.getHodometroAtual().subtract(anterior.getHodometroAtual());
        
        if (kmRodados.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal novoConsumo = abastecimentoAtual.getLitros().divide(kmRodados, 2, RoundingMode.HALF_UP);
            
            if (maquina.getConsumoMedio() != null && maquina.getConsumoMedio().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal mediaHistorica = maquina.getConsumoMedio();
                BigDecimal desvio = novoConsumo.subtract(mediaHistorica).abs().divide(mediaHistorica, 2, RoundingMode.HALF_UP);
                
                if (desvio.compareTo(new BigDecimal("0.30")) > 0) {
                    gerarNotificacaoProprietario("anomalia", "Anomalia de consumo detectada na máquina " + maquina.getNome() + ". Consumo atual: " + novoConsumo + ", Média histórica: " + mediaHistorica + ".");
                }
            }
            
            maquina.setConsumoMedio(novoConsumo);
        }
    }

    public void verificarAlertaPreventivo(Maquina maquina, BigDecimal hodometroAtual) {
        if (maquina.getIntervaloTrocaOleoHoras() != null && maquina.getIntervaloTrocaOleoHoras() > 0) {
            BigDecimal intervalo = new BigDecimal(maquina.getIntervaloTrocaOleoHoras());
            BigDecimal fator = hodometroAtual.divide(intervalo, 0, RoundingMode.FLOOR);
            BigDecimal ultimoPonto = fator.multiply(intervalo);
            BigDecimal proxima = ultimoPonto.add(intervalo);
            
            if (proxima.subtract(hodometroAtual).compareTo(new BigDecimal("20")) <= 0) {
                gerarNotificacaoProprietario("alerta_preventivo", "Máquina " + maquina.getNome() + " próxima da troca de óleo. Hodômetro: " + hodometroAtual);
            }
        }
        if (maquina.getIntervaloInspecaoHoras() != null && maquina.getIntervaloInspecaoHoras() > 0) {
            BigDecimal intervalo = new BigDecimal(maquina.getIntervaloInspecaoHoras());
            BigDecimal fator = hodometroAtual.divide(intervalo, 0, RoundingMode.FLOOR);
            BigDecimal ultimoPonto = fator.multiply(intervalo);
            BigDecimal proxima = ultimoPonto.add(intervalo);
            
            if (proxima.subtract(hodometroAtual).compareTo(new BigDecimal("20")) <= 0) {
                gerarNotificacaoProprietario("alerta_preventivo", "Máquina " + maquina.getNome() + " próxima da inspeção. Hodômetro: " + hodometroAtual);
            }
        }
    }

    private void gerarNotificacaoProprietario(String tipo, String mensagem) {
        List<Usuario> proprietarios = userRepository.findAll().stream()
                .filter(u -> "PROPRIETARIO".equals(u.getPerfil()))
                .toList();
        
        for (Usuario p : proprietarios) {
            Notificacao n = new Notificacao();
            n.setUsuarioDestinatario(p);
            n.setTipo(tipo);
            n.setMensagem(mensagem);
            n.setLida(false);
            n.setDataCriacao(LocalDateTime.now());
            notificacaoRepository.save(n);
        }
    }
}
