package com.main.frotaBackEnd.controller;

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

    private UsuarioDTO validarProprietario(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "Token ausente.");
        String token = authHeader.replace("Bearer ", "");
        if (!tokenService.validarToken(token))
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "Token inválido ou expirado.");
        UsuarioDTO solicitante = tokenService.extrairClaim(token);
        if (!"PROPRIETARIO".equals(solicitante.getPerfil()))
            throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Acesso negado.");
        return solicitante;
    }

    @PostMapping("/registrar-colaborador")
    public String registrar(
            @RequestParam("nome") String nome,
            @RequestParam("email") String email,
            @RequestParam("senha") String senha,
            @RequestParam(value = "perfil", defaultValue = "OPERADOR") String perfil,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            @RequestHeader("Authorization") String authHeader) {
        validarProprietario(authHeader);
        proprietarioService.registrarColaborador(nome, email, senha, perfil, foto);
        return "Novo colaborador cadastrado com sucesso!";
    }

    @GetMapping("/colaboradores")
    public ResponseEntity<List<Usuario>> listarColaboradores(@RequestHeader("Authorization") String authHeader) {
        validarProprietario(authHeader);
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/colaboradores/{id}")
    public ResponseEntity<Usuario> buscarColaborador(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        validarProprietario(authHeader);
        Usuario user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Colaborador não encontrado."));
        return ResponseEntity.ok(user);
    }

    @PutMapping("/colaboradores/{id}")
    public ResponseEntity<Map<String, String>> atualizarColaborador(
            @PathVariable Long id,
            @RequestBody Map<String, String> dados,
            @RequestHeader("Authorization") String authHeader) {
        validarProprietario(authHeader);
        proprietarioService.atualizarColaborador(id, dados);
        return ResponseEntity.ok(Map.of("message", "Colaborador atualizado com sucesso!"));
    }

    @PutMapping("/colaboradores/{id}/vincular-maquinas")
    public ResponseEntity<Map<String, String>> vincularMaquinas(
            @PathVariable Long id,
            @RequestBody List<Long> idsMaquinas,
            @RequestHeader("Authorization") String authHeader) {
        validarProprietario(authHeader);
        proprietarioService.vincularMaquinas(id, idsMaquinas);
        return ResponseEntity.ok(Map.of("message", "Vínculos atualizados com sucesso!"));
    }

    @GetMapping("/colaboradores/{id}/maquinas")
    public ResponseEntity<List<Long>> listarMaquinasVinculadas(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        validarProprietario(authHeader);
        return ResponseEntity.ok(proprietarioService.listarIdsMaquinasVinculadas(id));
    }
}
