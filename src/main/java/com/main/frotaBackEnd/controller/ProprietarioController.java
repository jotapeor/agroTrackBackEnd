package com.main.frotaBackEnd.controller;

import com.main.frotaBackEnd.model.PerfilUsuario;
import com.main.frotaBackEnd.model.Usuario;
import com.main.frotaBackEnd.model.UsuarioDTO;
import com.main.frotaBackEnd.repository.UserRepository;
import com.main.frotaBackEnd.service.ProprietarioService;
import com.main.frotaBackEnd.service.TokenService;
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
@RequestMapping("/api/proprietario")
public class ProprietarioController {
    @Autowired
    private ProprietarioService proprietarioService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private UserRepository userRepository;

    @PreAuthorize("hasRole('PROPRIETARIO')")
    @PostMapping("/registrar-colaborador")
    public String registrar(
            @RequestParam("nome") String nome,
            @RequestParam("email") String email,
            @RequestParam("senha") String senha,
            @RequestParam(value = "perfil", defaultValue = "OPERADOR") String perfil,
            @RequestParam(value = "foto", required = false) MultipartFile foto) {
        proprietarioService.registrarColaborador(nome, email, senha, perfil, foto);
        return "Novo colaborador cadastrado com sucesso!";
    }

    @PreAuthorize("hasAnyRole('PROPRIETARIO', 'SOCIO')")
    @GetMapping("/colaboradores")
    public ResponseEntity<List<Usuario>> listarColaboradores() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PreAuthorize("hasAnyRole('PROPRIETARIO', 'SOCIO')")
    @GetMapping("/colaboradores/{id}")
    public ResponseEntity<Usuario> buscarColaborador(@PathVariable Long id) {
        Usuario user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Colaborador não encontrado."));
        return ResponseEntity.ok(user);
    }

    @PreAuthorize("hasAnyRole('PROPRIETARIO', 'SOCIO')")
    @PutMapping("/colaboradores/{id}")
    public ResponseEntity<Map<String, String>> atualizarColaborador(
            @PathVariable Long id,
            @RequestBody Map<String, String> dados) {
        UsuarioDTO solicitante = (UsuarioDTO) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        proprietarioService.atualizarColaborador(id, dados, solicitante);
        return ResponseEntity.ok(Map.of("message", "Colaborador atualizado com sucesso!"));
    }

    @PreAuthorize("hasAnyRole('PROPRIETARIO', 'SOCIO')")
    @PutMapping("/colaboradores/{id}/vincular-maquinas")
    public ResponseEntity<Map<String, String>> vincularMaquinas(
            @PathVariable Long id,
            @RequestBody List<Long> idsMaquinas) {
        UsuarioDTO solicitante = (UsuarioDTO) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        proprietarioService.vincularMaquinas(id, idsMaquinas, solicitante);
        return ResponseEntity.ok(Map.of("message", "Vínculos atualizados com sucesso!"));
    }

    @PreAuthorize("hasAnyRole('PROPRIETARIO', 'SOCIO')")
    @GetMapping("/colaboradores/{id}/maquinas")
    public ResponseEntity<List<Long>> listarMaquinasVinculadas(
            @PathVariable Long id) {
        return ResponseEntity.ok(proprietarioService.listarIdsMaquinasVinculadas(id));
    }

    @PreAuthorize("hasRole('PROPRIETARIO')")
    @DeleteMapping("/colaboradores/{id}")
    public ResponseEntity<Map<String, String>> excluirColaborador(
            @PathVariable Long id) {
        proprietarioService.excluirColaborador(id);
        return ResponseEntity.ok(Map.of("message", "Colaborador desativado com sucesso!"));
    }

    @PreAuthorize("hasRole('PROPRIETARIO')")
    @PostMapping("/colaboradores/{id}/reativar")
    public ResponseEntity<Map<String, String>> reativarColaborador(
            @PathVariable Long id) {
        proprietarioService.reativarColaborador(id);
        return ResponseEntity.ok(Map.of("message", "Colaborador reativado com sucesso!"));
    }
}
