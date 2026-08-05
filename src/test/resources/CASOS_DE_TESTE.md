# Casos de Teste — AgroTrack BackEnd

## Infraestrutura

| Camada | Tecnologia |
|--------|------------|
| Unitário (serviços) | JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`) |
| Integração (controllers) | `@SpringBootTest` + MockMvc + H2 + `@ActiveProfiles("test")` |
| Banco de testes | H2 in-memory, `MODE=MySQL`, `create-drop` |
| Isolamento entre testes | `@Transactional` na classe (rollback automático após cada teste) |
| Autenticação nos testes | `TokenService.gerarToken()` com usuário salvo no H2 |

**Nota de adaptação — spec vs. código real:**
- `TC-MAQCTL-02`: OPERADOR em `GET /api/proprietario/maquinas` retorna 200 (não 403) — o controller não tem `@PreAuthorize` e chama `listarPorUsuario()` para OPERADOR.
- `TC-MAN-10/12`: `removerDaAba()` chama `save()` (não `delete()`) — apenas seta `removidaDaAba = true`.
- `TC-PRO-01`: usa `emailExiste()` (não `findByEmail()`) para verificar duplicidade.
- `ManutencaoServiceTest`: requer `@Mock ClassificacaoRiscoService` pois o service chama `determinarPrioridadeManutencao()`.

---

## Testes de Serviço (Unitários)

### OperacaoServiceTest (TC-OP-01 a TC-OP-24)

| ID | Método | Cenário | Resultado Esperado |
|----|--------|---------|-------------------|
| TC-OP-01 | `trocarStatus` | Máquina não encontrada | `ResponseStatusException` 404 |
| TC-OP-02 | `trocarStatus` | Usuário não encontrado | `ResponseStatusException` 404 |
| TC-OP-03 | `trocarStatus` | OPERADOR sem vínculo com a máquina | `ResponseStatusException` 403 |
| TC-OP-04 | `trocarStatus` | PROPRIETARIO inicia operação (pula verificação de vínculo) | Registro salvo, `verificaVinculo` não chamado |
| TC-OP-05 | `trocarStatus` | `novoStatus` igual ao status atual | `ResponseStatusException` 400 |
| TC-OP-06 | `trocarStatus` | Iniciar sem `confirmacao = true` | `ResponseStatusException` 400 |
| TC-OP-07 | `trocarStatus` | Pulverizador sem `pesoCarregado` | `ResponseStatusException` 400 |
| TC-OP-08 | `trocarStatus` | OPERADOR tenta inativar máquina | `ResponseStatusException` 403 |
| TC-OP-09 | `trocarStatus` | OPERADOR tenta enviar para manutenção | `ResponseStatusException` 403 |
| TC-OP-10 | `trocarStatus` | Máquina em manutenção → tentar iniciar operação | `ResponseStatusException` 400 |
| TC-OP-11 | `trocarStatus` | Máquina nível ALTO sem autorização prévia | `ResponseStatusException` 403 |
| TC-OP-12 | `trocarStatus` | Máquina nível ALTO com autorização | Registro salvo, `autorizadaOperacaoRisco` setado como `false` |
| TC-OP-13 | `trocarStatus` | Encerrar operação sem `hodometroFim` | `ResponseStatusException` 400 |
| TC-OP-14 | `trocarStatus` | `hodometroFim` menor que `hodometroInicio` | `ResponseStatusException` 400 |
| TC-OP-15 | `trocarStatus` | Encerrar operação com sucesso | `dataFim` preenchida, `save()` chamado |
| TC-OP-16 | `trocarStatus` | Inativar máquina sem motivo | `ResponseStatusException` 400 |
| TC-OP-17 | `trocarStatus` | Inativar máquina com motivo (PROPRIETARIO) | `HistoricoStatusMaquina` salvo |
| TC-OP-18 | `trocarStatus` | SOCIO inativa máquina → notifica proprietários | `notificacaoRepository.save()` chamado |
| TC-OP-19 | `trocarStatus` | Outro operador tenta encerrar operação ativa | `ResponseStatusException` 403 |
| TC-OP-20 | `obterOperacaoAtiva` | Nenhuma operação ativa | Retorna `null` |
| TC-OP-21 | `obterOperacaoAtiva` | Existe operação ativa | Retorna `RegistroOperacaoDTO` preenchido |
| TC-OP-22 | `trocarStatus` | Semeadeira sem `pesoCarregado` | `ResponseStatusException` 400 |
| TC-OP-23 | `listarHistoricoMaquina` | Sem registros | Retorna lista vazia |
| TC-OP-24 | `trocarStatus` | OPERADOR vinculado com confirmação | `RegistroOperacao` salvo, status da máquina = "Em Operacao" |

### MaquinaServiceTest (TC-MAQ-01 a TC-MAQ-05)

| ID | Método | Cenário | Resultado Esperado |
|----|--------|---------|-------------------|
| TC-MAQ-01 | `buscarPorId` | Máquina encontrada | Retorna `MaquinaDTO` |
| TC-MAQ-02 | `buscarPorId` | Máquina não encontrada | `ResponseStatusException` 404 |
| TC-MAQ-03 | `excluir` | Máquina em operação | `ResponseStatusException` 400 |
| TC-MAQ-04 | `excluir` | Máquina disponível | `ativo = false`, `save()` chamado |
| TC-MAQ-05 | `reativar` | Máquina arquivada | `ativo = true`, `status = "Disponivel"` |

### ManutencaoServiceTest (TC-MAN-01 a TC-MAN-12)

| ID | Método | Cenário | Resultado Esperado |
|----|--------|---------|-------------------|
| TC-MAN-01 | `abrirOrdem` | Máquina não encontrada | `ResponseStatusException` 404 |
| TC-MAN-02 | `abrirOrdem` | Usuário não encontrado | `ResponseStatusException` 404 |
| TC-MAN-03 | `abrirOrdem` | OPERADOR sem vínculo | `ResponseStatusException` 403 |
| TC-MAN-04 | `abrirOrdem` | PROPRIETARIO abre ordem | Status = "Ativa" |
| TC-MAN-05 | `abrirOrdem` | OPERADOR vinculado abre ordem | Status = "Aguardando Aprovação" |
| TC-MAN-06 | `aprovarOrdem` | Ordem não encontrada | `ResponseStatusException` 404 |
| TC-MAN-07 | `aprovarOrdem` | Ordem não está "Aguardando Aprovação" | `ResponseStatusException` 400 |
| TC-MAN-08 | `aprovarOrdem` | Aprovada = true | Status = "Ativa", notificação enviada |
| TC-MAN-09 | `aprovarOrdem` | Aprovada = false | Status = "Recusada" |
| TC-MAN-10 | `encerrarOrdem` | Observação nula | `ResponseStatusException` 400 **sem** chamar `findById` |
| TC-MAN-11 | `encerrarOrdem` | Ordem não está "Ativa" | `ResponseStatusException` 400 |
| TC-MAN-12 | `removerDaAba` | Ordem encerrada | `removidaDaAba = true`, `save()` chamado (nunca `delete()`) |

### ProprietarioServiceTest (TC-PRO-01 a TC-PRO-04)

| ID | Método | Cenário | Resultado Esperado |
|----|--------|---------|-------------------|
| TC-PRO-01 | `registrarColaborador` | E-mail já cadastrado (via `emailExiste()`) | `ResponseStatusException` 409 |
| TC-PRO-02 | `registrarColaborador` | Dados válidos, e-mail livre | `userRepository.save()` chamado |
| TC-PRO-03 | `excluirColaborador` | Colaborador encontrado | `ativo = false`, `save()` chamado |
| TC-PRO-04 | `reativarColaborador` | Colaborador encontrado | `ativo = true`, `save()` chamado |

---

## Testes de Controller (Integração)

Todos os testes de controller usam:
- `@SpringBootTest` + `@AutoConfigureMockMvc` + `@ActiveProfiles("test")` + `@Transactional`
- `@MockBean` para o(s) serviço(s) do controller testado
- `UserRepository` e `TokenService` reais (necessários para o `JwtAuthenticationFilter`)
- Usuários salvos em `@BeforeEach` dentro da transação do teste

### UserControllerTest (TC-AUTH-01 a TC-AUTH-08)

| ID | Endpoint | Cenário | Resultado Esperado |
|----|----------|---------|-------------------|
| TC-AUTH-01 | `GET /api/autenticar/verificar-email` | E-mail não cadastrado | 200, `disponivel = true` |
| TC-AUTH-02 | `POST /api/autenticar/alterar-senha` | Sem token | 4xx |
| TC-AUTH-03 | `POST /api/autenticar/alterar-senha` | Token válido, senha válida | 200 |
| TC-AUTH-04 | `POST /api/autenticar/alterar-senha` | Token válido, senha < 6 chars | 400 |
| TC-AUTH-05 | `GET /api/usuario/me` | Sem token | 4xx |
| TC-AUTH-06 | `GET /api/usuario/me` | Token válido | 200, email correto no JSON |
| TC-AUTH-07 | `GET /api/usuarios/email-disponivel` | Sem token | 4xx |
| TC-AUTH-08 | `GET /api/usuarios/email-disponivel` | Autenticado, email não usado | 200, `disponivel = true` |

### OperacaoControllerTest (TC-OPCTL-01 a TC-OPCTL-05)

| ID | Endpoint | Cenário | Resultado Esperado |
|----|----------|---------|-------------------|
| TC-OPCTL-01 | `POST /api/operacoes/maquina/1/status` | Sem token | 4xx |
| TC-OPCTL-02 | `POST /api/operacoes/maquina/1/status` | OPERADOR autenticado | 200 |
| TC-OPCTL-03 | `GET /api/operacoes/maquina/1/historico` | PROPRIETARIO autenticado | 200 |
| TC-OPCTL-04 | `GET /api/operacoes/maquina/1/operacao-ativa` | Serviço retorna null | 404 |
| TC-OPCTL-05 | `GET /api/operacoes/maquina/1/operacao-ativa` | Serviço retorna DTO | 200, dados corretos |

### MaquinaControllerTest (TC-MAQCTL-01 a TC-MAQCTL-11)

| ID | Endpoint | Cenário | Resultado Esperado |
|----|----------|---------|-------------------|
| TC-MAQCTL-01 | `GET /api/proprietario/maquinas` | Sem token | 4xx |
| TC-MAQCTL-02 | `GET /api/proprietario/maquinas` | OPERADOR (sem @PreAuthorize) | **200** — chama `listarPorUsuario()` |
| TC-MAQCTL-03 | `GET /api/proprietario/maquinas` | PROPRIETARIO | 200 — chama `listarTodas()` |
| TC-MAQCTL-04 | `POST /api/proprietario/maquinas` | PROPRIETARIO | 200 |
| TC-MAQCTL-05 | `POST /api/proprietario/maquinas` | OPERADOR | 403 |
| TC-MAQCTL-06 | `POST /api/proprietario/maquinas` | SOCIO | 403 |
| TC-MAQCTL-07 | `DELETE /api/proprietario/maquinas/1` | PROPRIETARIO | 200 |
| TC-MAQCTL-08 | `DELETE /api/proprietario/maquinas/1` | OPERADOR | 403 |
| TC-MAQCTL-09 | `GET /api/proprietario/maquinas/arquivadas` | PROPRIETARIO | 200 |
| TC-MAQCTL-10 | `GET /api/proprietario/maquinas/arquivadas` | OPERADOR | 403 |
| TC-MAQCTL-11 | `POST /api/proprietario/maquinas/1/reativar` | PROPRIETARIO | 200 |

### ManutencaoControllerTest (TC-MANCTL-01 a TC-MANCTL-10)

| ID | Endpoint | Cenário | Resultado Esperado |
|----|----------|---------|-------------------|
| TC-MANCTL-01 | `GET /api/manutencao/ordens` | Sem token | 4xx |
| TC-MANCTL-02 | `GET /api/manutencao/ordens` | PROPRIETARIO | 200 |
| TC-MANCTL-03 | `GET /api/manutencao/ordens` | OPERADOR | 200 |
| TC-MANCTL-04 | `POST /api/manutencao/maquina/1/ordens` | OPERADOR | 200 |
| TC-MANCTL-05 | `POST /api/manutencao/ordens/1/aprovar` | PROPRIETARIO, `aprovada = true` | 200 |
| TC-MANCTL-06 | `POST /api/manutencao/ordens/1/aprovar` | SOCIO | 403 |
| TC-MANCTL-07 | `POST /api/manutencao/ordens/1/encerrar` | PROPRIETARIO, com observação | 200 |
| TC-MANCTL-08 | `POST /api/manutencao/ordens/1/encerrar` | OPERADOR | 403 |
| TC-MANCTL-09 | `DELETE /api/manutencao/ordens/1` | PROPRIETARIO | 200 |
| TC-MANCTL-10 | `POST /api/manutencao/ordens/1/aprovar` | PROPRIETARIO, campo `aprovada` ausente | 400 |

### ProprietarioControllerTest (TC-PROCTL-01 a TC-PROCTL-08)

| ID | Endpoint | Cenário | Resultado Esperado |
|----|----------|---------|-------------------|
| TC-PROCTL-01 | `GET /api/proprietario/colaboradores` | Sem token | 4xx |
| TC-PROCTL-02 | `GET /api/proprietario/colaboradores` | PROPRIETARIO | 200 |
| TC-PROCTL-03 | `GET /api/proprietario/colaboradores` | SOCIO | 200 |
| TC-PROCTL-04 | `GET /api/proprietario/colaboradores` | OPERADOR | 403 |
| TC-PROCTL-05 | `POST /api/proprietario/registrar-colaborador` | PROPRIETARIO | 200 |
| TC-PROCTL-06 | `POST /api/proprietario/registrar-colaborador` | SOCIO | 403 |
| TC-PROCTL-07 | `DELETE /api/proprietario/colaboradores/1` | PROPRIETARIO | 200 |
| TC-PROCTL-08 | `DELETE /api/proprietario/colaboradores/1` | SOCIO | 403 |

### RelatorioControllerTest (TC-REL-01 a TC-REL-06)

`RelatorioController` tem `@PreAuthorize("hasAnyRole('PROPRIETARIO', 'SOCIO')")` a nível de classe.

| ID | Endpoint | Cenário | Resultado Esperado |
|----|----------|---------|-------------------|
| TC-REL-01 | `GET /api/relatorios/consumo-por-maquina` | Sem token | 4xx |
| TC-REL-02 | `GET /api/relatorios/consumo-por-maquina` | OPERADOR | 403 |
| TC-REL-03 | `GET /api/relatorios/consumo-por-maquina` | PROPRIETARIO | 200 |
| TC-REL-04 | `GET /api/relatorios/consumo-por-maquina` | SOCIO | 200 |
| TC-REL-05 | `GET /api/relatorios/risco-distribuicao` | PROPRIETARIO | 200 |
| TC-REL-06 | `GET /api/relatorios/ordens-por-status` | SOCIO | 200 |

### NotificacaoControllerTest (TC-NOT-01 a TC-NOT-04)

`NotificacaoController` sem `@PreAuthorize` (qualquer perfil autenticado).

| ID | Endpoint | Cenário | Resultado Esperado |
|----|----------|---------|-------------------|
| TC-NOT-01 | `GET /api/notificacoes` | Sem token | 4xx |
| TC-NOT-02 | `GET /api/notificacoes` | OPERADOR autenticado | 200 |
| TC-NOT-03 | `POST /api/notificacoes/1/lida` | PROPRIETARIO autenticado | 200 |
| TC-NOT-04 | `DELETE /api/notificacoes/1` | OPERADOR autenticado | 200 |

### AbastecimentoControllerTest (TC-ABA-01 a TC-ABA-03)

| ID | Endpoint | Cenário | Resultado Esperado |
|----|----------|---------|-------------------|
| TC-ABA-01 | `POST /api/abastecimentos/maquina/1` | Sem token | 4xx |
| TC-ABA-02 | `POST /api/abastecimentos/maquina/1` | OPERADOR autenticado | 200 |
| TC-ABA-03 | `POST /api/abastecimentos/maquina/1` | PROPRIETARIO autenticado | 200 |

### AuthLoginControllerTest (TC-LOGIN-01 a TC-LOGIN-04)

| ID | Endpoint | Cenário | Resultado Esperado |
|----|----------|---------|-------------------|
| TC-LOGIN-01 | `POST /api/autenticar/logar` | Credenciais válidas | 200, token retornado |
| TC-LOGIN-02 | `POST /api/autenticar/logar` | Senha errada | 401 |
| TC-LOGIN-03 | `POST /api/autenticar/logar` | E-mail inexistente | 401 |
| TC-LOGIN-04 | `POST /api/autenticar/logar` | Usuário inativo | 403 |

### DashboardControllerTest (TC-DASH-01 a TC-DASH-04)

`DashboardController` com `@PreAuthorize("hasAnyRole('PROPRIETARIO', 'SOCIO', 'OPERADOR')")`.

| ID | Endpoint | Cenário | Resultado Esperado |
|----|----------|---------|-------------------|
| TC-DASH-01 | `GET /api/dashboard` | PROPRIETARIO autenticado | 200 |
| TC-DASH-02 | `GET /api/dashboard` | SOCIO autenticado | 200 |
| TC-DASH-03 | `GET /api/dashboard` | OPERADOR autenticado | 200 |
| TC-DASH-04 | `GET /api/dashboard` | Sem token | 4xx |

### RelatorioControllerCompletoTest (TC-REL-COMPL-01 a TC-REL-COMPL-08)

| ID | Endpoint | Cenário | Resultado Esperado |
|----|----------|---------|-------------------|
| TC-REL-COMPL-01 | `GET /api/relatorios/horas-km` | PROPRIETARIO | 200 |
| TC-REL-COMPL-02 | `GET /api/relatorios/horas-km` | SOCIO | 200 |
| TC-REL-COMPL-03 | `GET /api/relatorios/horas-km` | OPERADOR | 403 |
| TC-REL-COMPL-04 | `GET /api/relatorios/horas-km` | Sem token | 4xx |
| TC-REL-COMPL-05 | `GET /api/relatorios/alertas-timeline` | PROPRIETARIO | 200 |
| TC-REL-COMPL-06 | `GET /api/relatorios/alertas-timeline` | SOCIO | 200 |
| TC-REL-COMPL-07 | `GET /api/relatorios/alertas-timeline` | OPERADOR | 403 |
| TC-REL-COMPL-08 | `GET /api/relatorios/alertas-timeline` | Sem token | 4xx |

### MaquinaControllerCompletoTest (TC-MAQ-COMPL-01 a TC-MAQ-COMPL-14)

| ID | Endpoint | Cenário | Resultado Esperado |
|----|----------|---------|-------------------|
| TC-MAQ-COMPL-01 | `GET /api/proprietario/maquinas/1` | Com token | 200 |
| TC-MAQ-COMPL-02 | `GET /api/proprietario/maquinas/1` | Sem token | 4xx |
| TC-MAQ-COMPL-03 | `GET /api/proprietario/maquinas/fazendas` | PROPRIETARIO autenticado | 200 |
| TC-MAQ-COMPL-04 | `GET /api/proprietario/maquinas/fazendas` | Sem token | 4xx |
| TC-MAQ-COMPL-05 | `GET /api/proprietario/maquinas/talhoes` | SOCIO autenticado | 200 |
| TC-MAQ-COMPL-06 | `GET /api/proprietario/maquinas/talhoes?id_fazenda=1` | SOCIO autenticado | 200 |
| TC-MAQ-COMPL-07 | `POST /api/proprietario/maquinas/1/autorizar-risco` | PROPRIETARIO com justificativa | 200 |
| TC-MAQ-COMPL-08 | `POST /api/proprietario/maquinas/1/autorizar-risco` | OPERADOR | 403 |
| TC-MAQ-COMPL-09 | `POST /api/proprietario/maquinas/1/autorizar-risco` | PROPRIETARIO sem justificativa | 400 |
| TC-MAQ-COMPL-10 | `GET /api/proprietario/maquinas/1/historico-completo` | PROPRIETARIO | 200 |
| TC-MAQ-COMPL-11 | `GET /api/proprietario/maquinas/1/historico-completo` | Sem token | 4xx |
| TC-MAQ-COMPL-12 | `POST /api/proprietario/maquinas/1` | PROPRIETARIO (editar) | 200 |
| TC-MAQ-COMPL-13 | `POST /api/proprietario/maquinas/1` | OPERADOR (editar) | 403 |
| TC-MAQ-COMPL-14 | `GET /api/proprietario/maquinas/999999/combustiveis` | Máquina inexistente | 404 |

### ColaboradorControllerCompletoTest (TC-COL-COMPL-01 a TC-COL-COMPL-12)

| ID | Endpoint | Cenário | Resultado Esperado |
|----|----------|---------|-------------------|
| TC-COL-COMPL-01 | `GET /api/proprietario/colaboradores/{id}` | PROPRIETARIO, colaborador existe | 200 |
| TC-COL-COMPL-02 | `GET /api/proprietario/colaboradores/{id}` | OPERADOR | 403 |
| TC-COL-COMPL-03 | `GET /api/proprietario/colaboradores/999999` | PROPRIETARIO, inexistente | 404 |
| TC-COL-COMPL-04 | `PUT /api/proprietario/colaboradores/{id}` | PROPRIETARIO | 200 |
| TC-COL-COMPL-05 | `PUT /api/proprietario/colaboradores/{id}` | OPERADOR | 403 |
| TC-COL-COMPL-06 | `PUT /api/proprietario/colaboradores/{id}/vincular-maquinas` | PROPRIETARIO | 200 |
| TC-COL-COMPL-07 | `PUT /api/proprietario/colaboradores/{id}/vincular-maquinas` | OPERADOR | 403 |
| TC-COL-COMPL-08 | `GET /api/proprietario/colaboradores/{id}/maquinas` | SOCIO | 200 |
| TC-COL-COMPL-09 | `GET /api/proprietario/colaboradores/{id}/maquinas` | Sem token | 4xx |
| TC-COL-COMPL-10 | `POST /api/proprietario/colaboradores/{id}/reativar` | PROPRIETARIO | 200 |
| TC-COL-COMPL-11 | `POST /api/proprietario/colaboradores/{id}/reativar` | SOCIO | 403 |
| TC-COL-COMPL-12 | `POST /api/proprietario/colaboradores/{id}/reativar` | Sem token | 4xx |

### TelemetriaControllerTest (TC-TEL-01 a TC-TEL-06)

`TelemetriaController` usa repositórios diretamente (sem services). `MaquinaRepository`, `TelemetriaMaquinaRepository` e `RegistroOperacaoRepository` são mockados via `@MockitoBean`.

| ID | Endpoint | Cenário | Resultado Esperado |
|----|----------|---------|-------------------|
| TC-TEL-01 | `GET /api/telemetria/maquina/1` | PROPRIETARIO (pula verificação de vínculo) | 200 |
| TC-TEL-02 | `GET /api/telemetria/maquina/1` | Sem token | 4xx |
| TC-TEL-03 | `GET /api/telemetria/maquina/999` | Máquina inexistente | 404 |
| TC-TEL-04 | `GET /api/telemetria/maquina/1` | OPERADOR sem vínculo | 403 |
| TC-TEL-05 | `GET /api/telemetria/em-operacao` | PROPRIETARIO | 200 |
| TC-TEL-06 | `GET /api/telemetria/em-operacao` | OPERADOR | 403 |

### UsuarioPerfilControllerTest (TC-PERFIL-01 a TC-PERFIL-02)

| ID | Endpoint | Cenário | Resultado Esperado |
|----|----------|---------|-------------------|
| TC-PERFIL-01 | `GET /api/usuario/me` | OPERADOR autenticado | 200 |
| TC-PERFIL-02 | `PUT /api/usuario/me` | Sem token | 4xx |

---

## Testes de Serviço — Novos (Fase 2)

### AbastecimentoServiceTest (TC-ABASVC-01 a TC-ABASVC-06)

| ID | Método | Cenário | Resultado Esperado |
|----|--------|---------|-------------------|
| TC-ABASVC-01 | `verificarAlertaPreventivo` | Hodômetro a ≤20h do intervalo de troca de óleo | `notificacaoRepository.save()` chamado |
| TC-ABASVC-02 | `verificarAlertaPreventivo` | Hodômetro distante do intervalo (diff > 20) | `notificacaoRepository.save()` NÃO chamado |
| TC-ABASVC-03 | `verificarAlertaPreventivo` | Máquina sem intervalo configurado | `notificacaoRepository.save()` NÃO chamado |
| TC-ABASVC-04 | `verificarAlertaPreventivo` | Inspeção próxima (intervaloInspecaoHoras próximo) | `notificacaoRepository.save()` chamado |
| TC-ABASVC-05 | `registrarAbastecimento` | Máquina não encontrada | `ResponseStatusException` 404 |
| TC-ABASVC-06 | `registrarAbastecimento` | `hodometroAtual` < `hodometroInicial` da máquina | `ResponseStatusException` 400 |

### ClassificacaoRiscoServiceTest (TC-RISCO-01 a TC-RISCO-08)

| ID | Método | Cenário | Resultado Esperado |
|----|--------|---------|-------------------|
| TC-RISCO-01 | `recalcularRisco` | Existe ordem com prioridade "Critica" | `nivelRisco = "Alto"` |
| TC-RISCO-02 | `recalcularRisco` | 3 ou mais falhas nos últimos 30 dias | `nivelRisco = "Alto"` |
| TC-RISCO-03 | `recalcularRisco` | 1 ordem pendente, sem falhas | `nivelRisco = "Medio"` |
| TC-RISCO-04 | `recalcularRisco` | 1 falha, sem ordens | `nivelRisco = "Medio"` |
| TC-RISCO-05 | `recalcularRisco` | Sem ordens e sem falhas | `nivelRisco = "Baixo"` |
| TC-RISCO-06 | `determinarPrioridadeManutencao` | `urgenciaFalha = "Critica"` | Retorna `"Critica"` |
| TC-RISCO-07 | `determinarPrioridadeManutencao` | `nivelRisco = "Alto"` | Retorna `"Alta"` |
| TC-RISCO-08 | `determinarPrioridadeManutencao` | `nivelRisco = "Medio"` | Retorna `"Media"` |

### TelemetriaSimuladorServiceTest (TC-TEL-SIM-01 a TC-TEL-SIM-03)

| ID | Método | Cenário | Resultado Esperado |
|----|--------|---------|-------------------|
| TC-TEL-SIM-01 | `simularTelemetria` | Nenhuma máquina no repositório | `telemetriaMaquinaRepository.save()` NÃO chamado |
| TC-TEL-SIM-02 | `simularTelemetria` | 1 máquina com status "Em Operacao" | `telemetriaMaquinaRepository.save()` chamado 1 vez |
| TC-TEL-SIM-03 | `simularTelemetria` | Verificar campos do objeto salvo | `latitude`, `longitude`, `velocidadeAtual`, `consumoAtual`, `dataAtualizacao` não nulos |

---

## Executar os Testes

```bash
mvn test -Dspring.profiles.active=test
```

Para rodar apenas os testes de serviço:
```bash
mvn test -Dspring.profiles.active=test -Dtest="*ServiceTest"
```

Para rodar apenas os testes de controller:
```bash
mvn test -Dspring.profiles.active=test -Dtest="*ControllerTest"
```
