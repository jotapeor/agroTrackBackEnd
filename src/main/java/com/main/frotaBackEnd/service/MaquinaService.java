package com.main.frotaBackEnd.service;

import com.main.frotaBackEnd.model.Fazenda;
import com.main.frotaBackEnd.model.Maquina;
import com.main.frotaBackEnd.model.MaquinaDTO;
import com.main.frotaBackEnd.model.Talhao;
import com.main.frotaBackEnd.repository.FazendaRepository;
import com.main.frotaBackEnd.repository.MaquinaRepository;
import com.main.frotaBackEnd.repository.TalhaoRepository;
import com.main.frotaBackEnd.repository.AutorizacaoRiscoRepository;
import com.main.frotaBackEnd.repository.UserRepository;
import com.main.frotaBackEnd.model.AutorizacaoRisco;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MaquinaService {

    private static final Logger log = LoggerFactory.getLogger(MaquinaService.class);

    @Autowired
    private MaquinaRepository maquinaRepository;

    @Autowired
    private FazendaRepository fazendaRepository;

    @Autowired
    private TalhaoRepository talhaoRepository;

    @Autowired
    private AutorizacaoRiscoRepository autorizacaoRiscoRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${app.upload.dir:uploads/maquinas}")
    private String uploadDir;

    public void cadastrar(MaquinaDTO dto, MultipartFile foto) {
        if (dto.getNome() == null || dto.getNome().trim().length() < 2)
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Insira um nome/apelido para a máquina (mínimo 2 caracteres).");
        if (dto.getTipo() == null || (!dto.getTipo().equals("Trator") && !dto.getTipo().equals("Colheitadeira") && !dto.getTipo().equals("Pulverizador") && !dto.getTipo().equals("Semeadeira")))
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Tipo de máquina inválido. Use: Trator, Colheitadeira, Pulverizador ou Semeadeira.");
        if (dto.getModelo() == null || dto.getModelo().trim().isEmpty())
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "O modelo é obrigatório.");
        if (dto.getAno() < 1900 || dto.getAno() > LocalDate.now().getYear() + 1)
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Ano de fabricação inválido.");
        if (dto.getHodometroInicial() == null || dto.getHodometroInicial().compareTo(java.math.BigDecimal.ZERO) < 0)
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Horímetro inicial inválido.");

        Maquina maquina = new Maquina();
        maquina.setNome(dto.getNome().trim());
        maquina.setTipo(dto.getTipo());
        maquina.setMarca(dto.getMarca() != null ? dto.getMarca().trim() : null);
        maquina.setModelo(dto.getModelo().trim());
        maquina.setAno(dto.getAno());
        maquina.setNumeroSerie(dto.getNumeroSerie() != null ? dto.getNumeroSerie().trim() : null);
        maquina.setPlaca(dto.getPlaca() != null ? dto.getPlaca().trim().toUpperCase() : null);
        maquina.setHodometroInicial(dto.getHodometroInicial());
        maquina.setCapacidadeTanque(dto.getCapacidadeTanque());
        maquina.setTipoCombustivel(dto.getTipoCombustivel());
        maquina.setIntervaloTrocaOleoHoras(dto.getIntervaloTrocaOleoHoras());
        maquina.setIntervaloInspecaoHoras(dto.getIntervaloInspecaoHoras());
        maquina.setConsumoMedio(dto.getConsumoMedio());
        maquina.setStatus(dto.getStatus() != null ? dto.getStatus() : "Disponivel");
        maquina.setNivelRisco("Baixo");

        if (dto.getDataAquisicao() != null && !dto.getDataAquisicao().isEmpty()) {
            try {
                maquina.setDataAquisicao(Date.valueOf(LocalDate.parse(dto.getDataAquisicao())));
            } catch (Exception ignored) {
            }
        }

        maquina.setValorAquisicao(dto.getValorAquisicao());
        maquina.setObservacoes(dto.getObservacoes());

        if (dto.getIdFazenda() != null) {
            Fazenda fazenda = fazendaRepository.findById(dto.getIdFazenda())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Fazenda não encontrada."));
            maquina.setFazenda(fazenda);
        }

        if (dto.getIdTalhao() != null) {
            Talhao talhao = talhaoRepository.findById(dto.getIdTalhao())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Talhão não encontrado."));
            maquina.setTalhao(talhao);
        }

        if (foto != null && !foto.isEmpty()) {
            maquina.setFotoPath(salvarFoto(foto));
        }

        try {
            maquinaRepository.save(maquina);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(500), "Erro interno ao salvar a máquina.");
        }
    }

    public List<MaquinaDTO> listarTodas() {
        return maquinaRepository.buscarTodosOrdenados().stream()
                .map(this::toDTO)
                .toList();
    }

    public List<MaquinaDTO> listarPorUsuario(Long usuarioId) {
        return maquinaRepository.buscarPorUsuarioId(usuarioId).stream()
                .map(this::toDTO)
                .toList();
    }

    public MaquinaDTO buscarPorId(Long id) {
        Maquina maquina = maquinaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Máquina não encontrada."));
        return toDTO(maquina);
    }

    public void atualizar(Long id, MaquinaDTO dto, MultipartFile foto) {
        Maquina maquina = maquinaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Máquina não encontrada."));

        if (dto.getNome() == null || dto.getNome().trim().length() < 2)
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Insira um nome/apelido para a máquina (mínimo 2 caracteres).");
        if (dto.getTipo() == null || (!dto.getTipo().equals("Trator") && !dto.getTipo().equals("Colheitadeira") && !dto.getTipo().equals("Pulverizador") && !dto.getTipo().equals("Semeadeira")))
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Tipo de máquina inválido.");

        maquina.setNome(dto.getNome().trim());
        maquina.setTipo(dto.getTipo());
        maquina.setMarca(dto.getMarca() != null ? dto.getMarca().trim() : null);
        maquina.setModelo(dto.getModelo().trim());
        maquina.setAno(dto.getAno());
        maquina.setNumeroSerie(dto.getNumeroSerie() != null ? dto.getNumeroSerie().trim() : null);
        maquina.setPlaca(dto.getPlaca() != null ? dto.getPlaca().trim().toUpperCase() : null);
        maquina.setHodometroInicial(dto.getHodometroInicial());
        maquina.setCapacidadeTanque(dto.getCapacidadeTanque());
        maquina.setTipoCombustivel(dto.getTipoCombustivel());
        maquina.setIntervaloTrocaOleoHoras(dto.getIntervaloTrocaOleoHoras());
        maquina.setIntervaloInspecaoHoras(dto.getIntervaloInspecaoHoras());
        maquina.setConsumoMedio(dto.getConsumoMedio());

        if (dto.getStatus() != null) maquina.setStatus(dto.getStatus());
        if (dto.getNivelRisco() != null) maquina.setNivelRisco(dto.getNivelRisco());

        if (dto.getDataAquisicao() != null && !dto.getDataAquisicao().isEmpty()) {
            try {
                maquina.setDataAquisicao(Date.valueOf(LocalDate.parse(dto.getDataAquisicao())));
            } catch (Exception ignored) {
            }
        }
        maquina.setValorAquisicao(dto.getValorAquisicao());
        maquina.setObservacoes(dto.getObservacoes());

        if (dto.getIdFazenda() != null) {
            maquina.setFazenda(fazendaRepository.findById(dto.getIdFazenda())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Fazenda não encontrada.")));
        } else {
            maquina.setFazenda(null);
        }

        if (dto.getIdTalhao() != null) {
            maquina.setTalhao(talhaoRepository.findById(dto.getIdTalhao())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Talhão não encontrado.")));
        } else {
            maquina.setTalhao(null);
        }

        if (foto != null && !foto.isEmpty()) {
            maquina.setFotoPath(salvarFoto(foto));
        }

        try {
            maquinaRepository.save(maquina);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(500), "Erro interno ao atualizar a máquina.");
        }
    }

    @Transactional
    public void excluir(Long id) {
        Maquina maquina = maquinaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Máquina não encontrada."));
        try {
            maquina.setAtivo(false);
            maquinaRepository.save(maquina);
        } catch (RuntimeException e) {
            log.error("Erro ao arquivar máquina id={}", id, e);
            throw new ResponseStatusException(HttpStatusCode.valueOf(500), "Não foi possível arquivar a máquina no momento. Tente novamente.");
        }
    }

    @Transactional
    public void autorizarRisco(Long idMaquina, String justificativa, Long idProprietario) {
        Maquina maquina = maquinaRepository.findById(idMaquina)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Máquina não encontrada."));
        if (!"Alto".equals(maquina.getNivelRisco())) {
             throw new ResponseStatusException(HttpStatusCode.valueOf(400), "A máquina não está em nível de risco Alto.");
        }
        maquina.setAutorizadaOperacaoRisco(true);
        maquinaRepository.save(maquina);

        AutorizacaoRisco auth = new AutorizacaoRisco();
        auth.setMaquina(maquina);
        auth.setAutorizadoPor(userRepository.findById(idProprietario).orElseThrow());
        auth.setJustificativa(justificativa);
        auth.setDataAutorizacao(LocalDateTime.now());
        autorizacaoRiscoRepository.save(auth);
    }

    private String salvarFoto(MultipartFile foto) {
        try {
            String nomeArquivo = UUID.randomUUID() + "_" + foto.getOriginalFilename();
            Path caminho = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(caminho);
            Path destino = caminho.resolve(nomeArquivo);
            Files.copy(foto.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            return nomeArquivo;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(500), "Erro ao salvar a foto.");
        }
    }

    public MaquinaDTO toDTO(Maquina m) {
        MaquinaDTO dto = new MaquinaDTO();
        dto.setId(m.getId());
        if (m.getFazenda() != null) {
            dto.setIdFazenda(m.getFazenda().getId());
            dto.setNomeFazenda(m.getFazenda().getNome());
        }
        if (m.getTalhao() != null) {
            dto.setIdTalhao(m.getTalhao().getId());
            dto.setNomeTalhao(m.getTalhao().getNome());
        }
        dto.setNome(m.getNome());
        dto.setTipo(m.getTipo());
        dto.setMarca(m.getMarca());
        dto.setModelo(m.getModelo());
        dto.setAno(m.getAno());
        dto.setNumeroSerie(m.getNumeroSerie());
        dto.setPlaca(m.getPlaca());
        dto.setHodometroInicial(m.getHodometroInicial());
        dto.setCapacidadeTanque(m.getCapacidadeTanque());
        dto.setTipoCombustivel(m.getTipoCombustivel());
        dto.setIntervaloTrocaOleoHoras(m.getIntervaloTrocaOleoHoras());
        dto.setIntervaloInspecaoHoras(m.getIntervaloInspecaoHoras());
        dto.setConsumoMedio(m.getConsumoMedio());
        dto.setStatus(m.getStatus());
        dto.setNivelRisco(m.getNivelRisco());
        if (m.getDataAquisicao() != null)
            dto.setDataAquisicao(m.getDataAquisicao().toString());
        dto.setValorAquisicao(m.getValorAquisicao());
        dto.setFotoPath(m.getFotoPath());
        dto.setObservacoes(m.getObservacoes());
        dto.setAutorizadaOperacaoRisco(m.isAutorizadaOperacaoRisco());
        return dto;
    }
}
