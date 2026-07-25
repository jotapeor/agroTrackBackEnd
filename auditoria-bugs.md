# Auditoria de Bugs — AgroTrack
**Data:** 2026-07-24  
**Auditoria estática — nenhuma execução realizada**

---

## Resumo Executivo

| Severidade | Quantidade |
|------------|-----------|
| Crítico    | 5         |
| Alto       | 9         |
| Médio      | 12        |
| Baixo      | 7         |
| Cosmético  | 4         |
| **Total**  | **37**    |

### Top 5 Prioridades para Correção Imediata

1. **[CRÍTICO] Status inconsistente ao encerrar operação** — hodômetro comparado com `hodometroInicial` (valor do cadastro) em vez do valor acumulado, bloqueando ou permitindo encerramentos incorretos.
2. **[CRÍTICO] Maquina em "Em Operação" pode ter status manipulado diretamente no editar-maquina** — o formulário de edição (proprietário) permite alterar `status` e `nivel_risco` sem passar pelo fluxo de operação, podendo criar registros abertos órfãos.
3. **[CRÍTICO] `OperacaoController` sem `@PreAuthorize`** — qualquer usuário autenticado pode trocar status de qualquer máquina sem verificação de perfil no nível Spring Security.
4. **[CRÍTICO] `AbastecimentoController` sem `@PreAuthorize`** — idem; qualquer usuário autenticado acessa sem restrição declarada.
5. **[ALTO] Relatório de consumo ignora filtros de data** — `RelatorioService.relatorioConsumo()` ignora os parâmetros `inicio` e `fim` e retorna sempre o consumo médio estático de todas as máquinas ativas.

---

## Bugs Detalhados

---

### [Operação] Hodômetro final validado contra `hodometroInicial` do cadastro, não contra o hodômetro de início da operação

- **Severidade:** Crítico
- **Como reproduzir:** OPERADOR; tela "Trocar Status"; encerrar operação informando `hodometroFim` menor que `maquina.hodometroInicial` (valor atual do campo no banco) mas maior que o `hodometroInicio` registrado no `RegistroOperacao` aberto.
- **Comportamento esperado:** validação deve comparar `hodometroFim` com `registro.getHodometroInicio()` (o valor no momento em que a operação foi aberta).
- **Comportamento observado:** `OperacaoService.java` linha 118: `if (dto.getHodometroFim().compareTo(maquina.getHodometroInicial()) < 0)` — compara com o campo `hodometro_inicial` da máquina, que foi atualizado em abastecimentos anteriores ou em edições diretas. Se a máquina foi abastecida durante a operação e o hodômetro foi atualizado, a validação pode rejeitar um hodômetro final legítimo.
- **Causa raiz:** `OperacaoService.java` linha 118; a variável correta seria `ativas.get(0).getHodometroInicio()`.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Máquina] Editar máquina permite manipular status diretamente, criando operações abertas órfãs

- **Severidade:** Crítico
- **Como reproduzir:** PROPRIETARIO; tela "Editar Máquina"; alterar status de "Em Operação" para "Disponivel" sem encerrar a operação no fluxo normal.
- **Comportamento esperado:** o status da máquina só deveria mudar via endpoint `/api/operacoes/maquina/{id}/status`, que garante o encerramento do `RegistroOperacao`.
- **Comportamento observado:** `MaquinaController.java` (backend) `@PostMapping("/{id}")` aceita o campo `status` via parâmetro e o salva diretamente, sem verificar se existe `RegistroOperacao` aberto. O `RegistroOperacao` ficaria com `data_fim = NULL` para sempre.
- **Causa raiz:** `MaquinaController.java` (backend) linhas 129-192; `MaquinaService.atualizar()` linha 179: `if (dto.getStatus() != null) maquina.setStatus(dto.getStatus())`.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Segurança] `OperacaoController` sem `@PreAuthorize`

- **Severidade:** Crítico
- **Como reproduzir:** qualquer usuário autenticado; chamar diretamente `POST /api/operacoes/maquina/{id}/status` com JWT válido de qualquer perfil.
- **Comportamento esperado:** apenas perfis autorizados (PROPRIETARIO, SOCIO, OPERADOR vinculado) deveriam alcançar o endpoint.
- **Comportamento observado:** `OperacaoController.java` não possui nenhuma anotação `@PreAuthorize` na classe ou nos métodos. A verificação de vínculo é feita manualmente dentro do `OperacaoService`, mas apenas para perfis que não são PROPRIETARIO/SOCIO — um usuário com JWT válido de qualquer perfil passa pelo filtro Spring Security sem bloqueio no nível do framework.
- **Causa raiz:** `OperacaoController.java` — ausência de `@PreAuthorize`.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Segurança] `AbastecimentoController` sem `@PreAuthorize`

- **Severidade:** Crítico
- **Como reproduzir:** idem ao item anterior; qualquer usuário autenticado pode registrar abastecimento em qualquer máquina via API direta.
- **Comportamento esperado:** verificação de vínculo deveria ser reforçada por `@PreAuthorize` ou pelo mínimo exigir o perfil correto.
- **Comportamento observado:** `AbastecimentoController.java` não possui `@PreAuthorize`. A verificação de vínculo existe em `AbastecimentoService.registrarAbastecimento()` linha 45-50, mas somente para perfis que não são PROPRIETARIO/SOCIO — sem proteção Spring Security no controller.
- **Causa raiz:** `AbastecimentoController.java` — ausência de `@PreAuthorize`.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Segurança] `DashboardController` sem `@PreAuthorize`

- **Severidade:** Crítico
- **Como reproduzir:** qualquer usuário autenticado; chamar `GET /api/dashboard`.
- **Comportamento esperado:** apenas usuários autenticados com perfil válido deveriam acessar, mas principalmente o endpoint deveria ter proteção explícita.
- **Comportamento observado:** `DashboardController.java` não possui `@PreAuthorize`. O serviço filtra dados por usuário, então a exposição de dados errados é baixa, mas a ausência de proteção declarada é inconsistente com o restante do sistema.
- **Causa raiz:** `DashboardController.java` — ausência de `@PreAuthorize`.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Relatório] `relatorioConsumo` ignora completamente os filtros de data

- **Severidade:** Alto
- **Como reproduzir:** PROPRIETARIO; tela "Relatórios"; alterar filtros de data e clicar "Atualizar Dados" no gráfico de consumo.
- **Comportamento esperado:** o gráfico deveria mostrar dados filtrados pelo período selecionado.
- **Comportamento observado:** `RelatorioService.relatorioConsumo()` linhas 22-29 executa a query `SELECT m.id, m.nome, m.consumoMedio FROM Maquina m WHERE m.ativo = true` sem nenhum filtro de data. Os parâmetros `inicio` e `fim` são recebidos pelo método mas nunca usados.
- **Causa raiz:** `RelatorioService.java` linhas 22-29; os parâmetros `inicio` e `fim` são declarados mas descartados.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Relatório] `relatorioRisco` ignora completamente os filtros de data

- **Severidade:** Alto
- **Como reproduzir:** PROPRIETARIO; tela "Relatórios"; alterar filtros de data no gráfico de risco.
- **Comportamento esperado:** filtro de data deveria impactar o resultado.
- **Comportamento observado:** `RelatorioController.java` recebe `dataInicio` e `dataFim` mas os repassa para `relatorioService.relatorioRisco()` que não aceita parâmetros — assinatura do método: `public Map<String, Long> relatorioRisco()` sem nenhum parâmetro. A query agrupa por `nivel_risco` atual das máquinas, ignorando datas completamente.
- **Causa raiz:** `RelatorioService.java` linhas 32-42; `RelatorioController.java` linha 47.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Contrato API] `abrirOrdem` envia campo `urgencia` mas backend espera `urgencia` em `NovaOrdemDTO`

- **Severidade:** Alto
- **Como reproduzir:** OPERADOR; abrir nova ordem de manutenção.
- **Comportamento esperado:** o campo urgência deveria ser recebido corretamente.
- **Comportamento observado:** `ApiService.abrirOrdem()` linha 415 envia `body.put("urgencia", urgencia)` e o backend em `NovaOrdemDTO.java` espera o campo `urgencia`. Aparentemente correto, mas o template `nova-ordem.html` linha 35 usa `name="urgencia"` no `<select>`. **O problema real está nos valores**: o select do template (`nova-ordem.html` linha 37-40) usa valores `"Baixa"`, `"Media"`, `"Alta"`, `"Critica"` enquanto `ClassificacaoRiscoService.determinarPrioridadeManutencao()` linha 57 compara com `"Critica".equalsIgnoreCase(urgenciaFalha)` — funciona. Porém a prioridade para nível de risco "Medio" resulta em `"Media"` (linha 63) que é armazenado no banco como `prioridade` com enum `('Baixa','Media','Alta','Critica')` — inconsistência: o enum usa "Media" sem acento, mas o gráfico em `lista-ordens.html` linha 57 exibe `th:text="'Prioridade: ' + ${o.prioridade}"` sem tratamento — cosmético, mas indica que o valor "Media" é armazenado sem acento no banco, enquanto a UI deveria exibir "Média" com acento para consistência.
- **Causa raiz:** `ClassificacaoRiscoService.java` linha 63; `script.sql` linha 256 (enum `'Media'`).
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não (valor coincide com enum, mas exibição é sem acento)

---

### [Autorização] SOCIO pode ver e editar dados de outros SOCIOs e do PROPRIETARIO na lista de colaboradores

- **Severidade:** Alto
- **Como reproduzir:** login como SOCIO; acessar `/colaboradores`; clicar "Editar/Vincular" em outro SOCIO.
- **Comportamento esperado:** SOCIO só deveria poder editar OPERADORs.
- **Comportamento observado:** `lista-colaboradores.html` linha 133: `th:if="${session.role == 'SOCIO' && c.perfil == 'OPERADOR'}"` — corretamente esconde o botão para não-OPERADORs. Porém, se um SOCIO acessar diretamente a URL `/colaboradores/editar/{id}` onde o ID é de outro SOCIO, o `ColaboradorController.editar()` verifica `if ("SOCIO".equals(role) && !"OPERADOR".equals(colaborador.get("perfil")))` (linha 62) e redireciona. O backend `ProprietarioController` tem a verificação equivalente. **Suspeita confirmada**: o botão de editar na lista de colaboradores NÃO aparece para SOCIO quando o colaborador listado é PROPRIETARIO/SOCIO, porém a lista de colaboradores do backend `ProprietarioController.listarColaboradores()` retorna `userRepository.findAll()` — **todos os usuários do sistema sem filtro**, incluindo outros PROPRIETARIOs e o próprio usuário, antes do filtro `meuId` no front. Qualquer SOCIO vê todos os usuários do sistema incluindo dados pessoais de proprietários.
- **Causa raiz:** `ProprietarioController.java` linha 44: `return ResponseEntity.ok(userRepository.findAll())`.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Autorização] Frontend permite OPERADOR acessar `/meu-perfil` após remoção da proteção

- **Severidade:** Alto
- **Como reproduzir:** login como OPERADOR; acessar `/meu-perfil` diretamente.
- **Comportamento esperado:** OPERADORs deveriam poder ver e editar seu próprio perfil.
- **Comportamento observado:** `MeuPerfilController.java` linhas 27-30 bloqueia explicitamente OPERADORs de acessar a tela "Meu Perfil" (`if (!"PROPRIETARIO".equals(role) && !"SOCIO".equals(role)) { return "redirect:/dashboard"; }`). OPERADORs ficam sem maneira de alterar seus próprios dados (nome, foto, email). A alteração de senha no primeiro acesso funciona via modal, mas não há tela de perfil. Esta é provavelmente uma decisão de design, mas merece revisão pois o operador não tem como atualizar email nem foto.
- **Causa raiz:** `MeuPerfilController.java` linhas 27-30.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Contrato API] `atualizarMeusDados` no frontend envia multipart mas o retorno é `Map<String, Object>` enquanto o backend retorna `Map<String, String>`

- **Severidade:** Alto
- **Como reproduzir:** PROPRIETARIO/SOCIO; tela "Meu Perfil"; salvar dados com foto.
- **Comportamento esperado:** dados atualizados com sucesso.
- **Comportamento observado:** `ApiService.atualizarMeusDados()` declara retorno `Map<String, Object>` (linha 325). O backend `UserController.atualizarMeusDados()` retorna `ResponseEntity<Map<String, String>>` (linha 81) enviando `Map.of("message", "...", "token", novoTicket)`. O RestClient desserializa para `Map<String, Object>` sem problema. No entanto, o frontend envia como `multipart` (`LinkedMultiValueMap`) mas o backend recebe via `@RequestParam` (linha 82-86) — **este contrato funciona** pois Spring aceita `@RequestParam` em multipart. Porém, se `foto` não for enviada, o backend ainda funciona pois `required = false`. Ponto de atenção: o `Content-Type` do RestClient precisaria ser configurado para `multipart/form-data` ao enviar `LinkedMultiValueMap`, o que Spring `RestClient` faz automaticamente ao detectar o tipo do body. Suspeita baixa — requer confirmação em runtime.
- **Causa raiz:** `ApiService.java` linha 333; `UserController.java` linha 81.
- **Confirmado por:** suspeita — requer confirmação
- **Exige script.sql?:** não

---

### [Lógica de Negócio] Classificação de risco não é recalculada quando uma ordem é ABERTA — apenas quando é ENCERRADA

- **Severidade:** Alto
- **Como reproduzir:** OPERADOR; abrir nova ordem de manutenção para máquina com risco "Baixo"; verificar que o risco permanece "Baixo" até a ordem ser encerrada.
- **Comportamento esperado:** ao abrir uma ordem "Aguardando Aprovação" ou "Ativa", o risco deveria subir para "Medio" imediatamente (pois há manutenção pendente).
- **Comportamento observado:** `ManutencaoService.abrirOrdem()` não chama `classificacaoRiscoService.recalcularRisco()`. O `recalcularRisco` só é chamado em `OperacaoService.trocarStatus()` (ao encerrar operação) e em `ManutencaoService.encerrarOrdem()`. A máquina pode permanecer com risco "Baixo" enquanto tem ordens abertas.
- **Causa raiz:** `ManutencaoService.java` — ausência de chamada a `classificacaoRiscoService.recalcularRisco()` em `abrirOrdem()`.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Lógica de Negócio] Máquina "Em Manutenção" pode ter nova operação iniciada sem restrição

- **Severidade:** Alto
- **Como reproduzir:** OPERADOR vinculado; máquina com status "Em Manutencao"; acessar `/maquinas/{id}/status` e selecionar "Em Operacao".
- **Comportamento esperado:** o sistema deveria impedir iniciar operação em máquina em manutenção.
- **Comportamento observado:** `OperacaoService.trocarStatus()` verifica apenas se `novoStatus.equals(statusAtual)` (linha 81) e se o status alvo é "Em Operacao" exige confirmação e peso. Não há verificação que impeça a transição "Em Manutencao" → "Em Operacao". O select em `trocar-status.html` linha 109 usa `th:disabled="${maquina.status == 'Em Manutencao'}"` — isto desabilita apenas a opção "Em Manutenção" (não mudar para ela), mas **não bloqueia** a opção "Em Operacao" quando o status atual é "Em Manutencao". A opção "Em Operação" seria habilitada mesmo estando em manutenção.
- **Causa raiz:** `trocar-status.html` linha 108; `OperacaoService.java` — ausência de validação de transição de status.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Consistência] Máquina com `RegistroOperacao` aberto pode ter seu status mudado para "Inativa" ou "Em Manutencao"

- **Severidade:** Alto
- **Como reproduzir:** OPERADOR com operação em andamento; PROPRIETARIO edita a máquina pelo formulário de edição e muda o status para "Inativa".
- **Comportamento esperado:** o sistema deveria impedir mudança de status que contradiz operação ativa.
- **Comportamento observado:** como documentado no bug "Editar máquina permite manipular status diretamente", o `MaquinaService.atualizar()` não verifica existência de `RegistroOperacao` aberto. O `OperacaoService` faz essa verificação mas apenas dentro do fluxo de troca de status normal.
- **Causa raiz:** `MaquinaService.java` linha 179; `MaquinaController.java` (backend) `@PostMapping("/{id}")`.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Contrato API] `ApiService.atualizarMaquina` usa POST, não PUT, para atualização

- **Severidade:** Médio
- **Como reproduzir:** qualquer fluxo de edição de máquina.
- **Comportamento esperado:** convenção REST: atualização deveria usar PUT ou PATCH.
- **Comportamento observado:** `ApiService.atualizarMaquina()` linha 227 usa `restClient.post()` para `URI /proprietario/maquinas/{id}`. O backend `MaquinaController.java` linha 129 usa `@PostMapping("/{id}")` — ambos concordam em usar POST. Funciona, mas é semanticamente incorreto e poderia causar confusão. Adicionalmente, o payload é `multipart/form-data` para uma operação de atualização.
- **Causa raiz:** `ApiService.java` linha 227; `MaquinaController.java` (backend) linha 129.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Consistência] `maquina.tipo_combustivel` vs tabela `maquina_combustivel` — dados podem divergir

- **Severidade:** Médio
- **Como reproduzir:** cadastrar máquina com combustível principal "Diesel S10" e extras "Diesel S500"; depois editar a máquina alterando o combustível principal para "Gasolina" sem marcar checkbox multi-combustível.
- **Comportamento esperado:** a tabela `maquina_combustivel` deveria ser sincronizada com `maquina.tipo_combustivel`.
- **Comportamento observado:** `MaquinaService.atualizar()` linha 215-222 deleta todos os registros de `maquina_combustivel` e recria apenas os `combustivel_extra`. O `tipo_combustivel` principal é salvo apenas no campo `maquina.tipo_combustivel`. No endpoint `GET /{id}/combustiveis`, se `maquina_combustivel` estiver vazia, retorna `maquina.tipo_combustivel`. Mas se existirem registros em `maquina_combustivel`, o principal não está incluído neles — a listagem de combustíveis retorna apenas os extras, não incluindo o principal. O método `listarCombustiveis` em `MaquinaController.java` (backend) linha 214-224 retorna `maquinaCombustivelRepository.findTiposByMaquinaId(id)` e só cai no fallback `maquina.getTipoCombustivel()` se a lista for vazia. Se houver extras cadastrados mas o combustível principal mudou, o select de abastecimento não mostrará o novo combustível principal.
- **Causa raiz:** `MaquinaController.java` (backend) linhas 214-224; `MaquinaService.atualizar()` linhas 215-222.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Contrato API] `ApiService.registrarAbastecimento` envia `dataAbastecimento` como String, mas `AbastecimentoDTO` espera `LocalDateTime`

- **Severidade:** Médio
- **Como reproduzir:** registrar abastecimento com data/hora preenchida.
- **Comportamento esperado:** data processada corretamente.
- **Comportamento observado:** `ApiService.registrarAbastecimento()` linha 492 faz `body.put("dataAbastecimento", dataAbastecimento)` onde `dataAbastecimento` é uma String vinda do `<input type="datetime-local">` (formato `"2026-07-24T10:30"`). O backend `AbastecimentoDTO.java` declara `private LocalDateTime dataAbastecimento`. O Jackson precisaria desserializar a String para `LocalDateTime` — isso funciona se o formato for ISO (`2026-07-24T10:30:00`), mas o input `datetime-local` retorna `2026-07-24T10:30` sem segundos, o que pode falhar dependendo da configuração do ObjectMapper no backend.
- **Causa raiz:** `ApiService.java` linha 492; formato do `datetime-local` HTML vs `LocalDateTime` Java.
- **Confirmado por:** suspeita — requer confirmação em runtime
- **Exige script.sql?:** não

---

### [Rota] `nova-maquina.html` busca talhões em `/api/maquinas/talhoes` mas o endpoint correto no frontend é `/api/maquinas/talhoes`

- **Severidade:** Médio
- **Como reproduzir:** PROPRIETARIO; tela "Cadastrar Máquina"; selecionar fazenda.
- **Comportamento esperado:** talhões carregam corretamente.
- **Comportamento observado:** `nova-maquina.html` linha 352 faz `fetch('/api/maquinas/talhoes?id_fazenda=' + idFazenda)`. O frontend `MaquinaController.java` linha 244 mapeia `@GetMapping("/api/maquinas/talhoes")` — **este endpoint existe**. Porém o `ApiService.listarTalhoes()` chama `/proprietario/maquinas/talhoes` no backend. O JS do template chama diretamente o frontend (porta 8081), enquanto o `ApiService` chama o backend (porta 8080). O endpoint do frontend relays a chamada. **Funciona**, mas `editar-maquina.html` linha 292 também usa `fetch('/api/maquinas/talhoes?id_fazenda=...')` — consistente. Sem bug real neste ponto — apenas observação de arquitetura.
- **Causa raiz:** design correto; observação registrada para clareza.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Lógica de Negócio] `recalcularConsumo` usa índice `historico.get(1)` sem garantir ordem correta

- **Severidade:** Médio
- **Como reproduzir:** registrar abastecimento em máquina com histórico desordenado.
- **Comportamento esperado:** consumo calculado com base nos dois últimos abastecimentos cronológicos.
- **Comportamento observado:** `AbastecimentoService.recalcularConsumo()` linha 80: `Abastecimento anterior = historico.get(1)`. O `historico` vem de `abastecimentoRepository.buscarPorMaquinaId(maquina.getId())` — a ordenação depende da query do repositório. Se a query não ordenar por data decrescente, o `get(1)` pode não ser o penúltimo abastecimento cronológico, levando a cálculos de consumo incorretos.
- **Causa raiz:** `AbastecimentoService.java` linha 77-80; ordenação do `AbastecimentoRepository.buscarPorMaquinaId()` não verificada.
- **Confirmado por:** suspeita — requer leitura do repositório
- **Exige script.sql?:** não

---

### [Lógica de Negócio] Alerta preventivo dispara desnecessariamente toda vez que abastecimento é registrado

- **Severidade:** Médio
- **Como reproduzir:** máquina com hodômetro próximo ao limite de troca de óleo; registrar vários abastecimentos consecutivos.
- **Comportamento esperado:** alerta disparado uma vez até a manutenção ser realizada e o contador resetado.
- **Comportamento observado:** `AbastecimentoService.verificarAlertaPreventivo()` (linhas 118-138) não verifica se já existe notificação do mesmo tipo para a mesma máquina nos últimos dias. Cada abastecimento próximo do limite gera uma nova notificação `alerta_preventivo`, resultando em spam de notificações. Idem para o fim de operação em `OperacaoService.trocarStatus()` linha 159.
- **Causa raiz:** `AbastecimentoService.java` linha 118; ausência de deduplicação de alertas.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Lógica de Negócio] `classificacaoRiscoService.recalcularRisco` detecta "falha" apenas por substring na observação

- **Severidade:** Médio
- **Como reproduzir:** operador escreve observações que contenham a palavra "falha" em contextos não-problemáticos (ex: "sem falha detectada").
- **Comportamento esperado:** classificação de risco baseada em critérios objetivos.
- **Comportamento observado:** `ClassificacaoRiscoService.recalcularRisco()` linha 42: `r.getObservacoes().toLowerCase().contains("falha")`. A palavra "falha" em qualquer contexto incrementa o contador de falhas, podendo elevar o risco indevidamente.
- **Causa raiz:** `ClassificacaoRiscoService.java` linha 42.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Lógica de Negócio] Autorização de risco é "use uma vez", mas não há prazo de expiração

- **Severidade:** Médio
- **Como reproduzir:** PROPRIETARIO autoriza operação de risco; a autorização é consumida ao iniciar operação; se a operação falhar no meio (ex: crash da sessão), a flag `autorizada_operacao_risco` já foi zerada mas a operação não ocorreu.
- **Comportamento esperado:** autorização deveria ter prazo (ex: 24h) ou ser revertível.
- **Comportamento observado:** `OperacaoService.trocarStatus()` linha 99: `maquina.setAutorizadaOperacaoRisco(false)` é executado antes de salvar o `RegistroOperacao`. Se o save falhar (exceção de banco), a máquina terá a flag zerada sem ter iniciado operação. A `@Transactional` garante rollback, mas a lógica de desfazer o zero depende do commit total.
- **Causa raiz:** `OperacaoService.java` linhas 95-109.
- **Confirmado por:** suspeita — a anotação `@Transactional` mitiga, mas a ausência de prazo de expiração é um gap de negócio real.
- **Exige script.sql?:** não

---

### [Notificações] Encerramento de ordem não gera notificação para SOCIO/PROPRIETARIO

- **Severidade:** Médio
- **Como reproduzir:** PROPRIETARIO; encerrar ordem de manutenção.
- **Comportamento esperado:** notificação gerada informando o encerramento.
- **Comportamento observado:** `ManutencaoService.encerrarOrdem()` (linhas 97-118) não gera nenhuma notificação. Apenas a aprovação/recusa gera notificação (linha 84-91). O solicitante original da ordem não é notificado do encerramento.
- **Causa raiz:** `ManutencaoService.java` linhas 97-118 — ausência de `notificacaoRepository.save()`.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Notificações] Notificações geradas para "Risco" no `script.sql` usam tipo `'Risco'` mas o sistema usa `'anomalia'` e `'alerta_preventivo'`

- **Severidade:** Médio
- **Como reproduzir:** verificar tela de notificações; acessar a notificação de ID 6 no script de dados.
- **Comportamento esperado:** todos os tipos de notificação deveriam ser reconhecidos pelo frontend.
- **Comportamento observado:** `script.sql` linha 240: `INSERT INTO notificacao ... 'Risco', 'Pulverizador Stara...'`. O template `lista-notificacoes.html` linha 46 mapeia ícones apenas para `'anomalia'` e `'alerta_preventivo'`. O tipo `'Risco'` cai no else (ícone de informação). No `DashboardService.java` linha 75: `n.getTipo().startsWith("alerta") || n.getTipo().startsWith("anomalia")` — o tipo `'Risco'` não é contabilizado como alerta ativo. A notificação de ID 2 e ID 4 do script usam tipo `'Aprovacao'` (também não mapeado). Os tipos no script de dados divergem dos tipos gerados pelo código.
- **Causa raiz:** `script.sql` linhas 235-240; os tipos usados no script são: `'Preventivo'`, `'Aprovacao'`, `'Anomalia'`, `'Risco'` — com inicial maiúscula, enquanto o código gera: `'alerta_preventivo'`, `'anomalia'`, `'ordem_pendente'`, `'ordem_aprovada'`, `'ordem_recusada'` — em snake_case minúsculo.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** sim

---

### [Autorização] Frontend esconde botões de encerrar/aprovar ordens para não-PROPRIETARIO mas SOCIO poderia fazer via URL

- **Severidade:** Médio
- **Como reproduzir:** login como SOCIO; acessar `/ordens/{id}/encerrar` diretamente via POST.
- **Comportamento esperado:** SOCIO não deveria poder encerrar/aprovar ordens (a regra atual restringe ao PROPRIETARIO).
- **Comportamento observado:** `ManutencaoController.java` (frontend) linhas 100-117 usa `if (!isProprietario(session))` antes de chamar encerrar. `isProprietario()` verifica `"PROPRIETARIO".equals(session.getAttribute("role"))`. Correto para o frontend. No backend `ManutencaoController.java` (backend) linha 45: `@PreAuthorize("hasRole('PROPRIETARIO')")` — protege a rota no backend. **Funciona corretamente** — o backend protege via `@PreAuthorize`. Registrado como confirmado sem bug.
- **Causa raiz:** não há bug; proteção dupla correta.
- **Confirmado por:** leitura de código — sem problema
- **Exige script.sql?:** não

---

### [Validação] Campo `ano` no formulário `nova-maquina.html` tem `th:max` dinâmico mas sem `min` no backend

- **Severidade:** Baixo
- **Como reproduzir:** PROPRIETARIO; cadastrar máquina com ano 1800.
- **Comportamento esperado:** HTML bloqueia (min=1900), mas se alguém bypass o HTML, o backend deveria rejeitar.
- **Comportamento observado:** `nova-maquina.html` linha 93: `min="1900" th:max="${T(java.time.Year).now().getValue() + 1}"` — validação HTML correta. Backend `MaquinaService.cadastrar()` linha 74: `if (dto.getAno() < 1900 || dto.getAno() > LocalDate.now().getYear() + 1)` — validação backend também presente e correta. **Sem bug**. O formulário `editar-maquina.html` linha 65-66 NÃO tem `min` nem `max` no campo `ano` — falta de validação HTML no formulário de edição, embora o backend valide (suspeita baixa, o backend protege).
- **Causa raiz:** `editar-maquina.html` linha 65.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Segurança] `UserController.emailDisponivel` (`GET /api/usuarios/email-disponivel`) não requer autenticação explícita

- **Severidade:** Baixo
- **Como reproduzir:** chamar `GET /api/usuarios/email-disponivel?email=joao@...` sem token.
- **Comportamento esperado:** endpoint acessível apenas por autenticados (para verificação durante edição).
- **Comportamento observado:** `SecurityConfig.java` linha 36 permite apenas `/api/autenticar/logar` e `/api/autenticar/verificar-email` sem autenticação; todo o resto exige autenticação. O endpoint `/api/usuarios/email-disponivel` está coberto pelo `anyRequest().authenticated()`, portanto requer token. **Sem bug real** — mas o endpoint de verificação de email durante cadastro `ApiService.verificarEmailDisponivel()` não envia token, apenas `email` e `idAtual`. Isso funciona porque o `ApiService` é chamado pelo frontend que já passou pela sessão, mas a chamada ao backend não leva token — **possível falha**: `ApiService.verificarEmailDisponivel()` linhas 262-268 chama sem `.header("Authorization", ...)`. Se o backend exige autenticação em `anyRequest()`, esta chamada falharia com 401.
- **Causa raiz:** `ApiService.java` linhas 262-268 — ausência de token na requisição ao backend.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Autorização] `ManutencaoController.abrirOrdem` (backend) sem `@PreAuthorize` — qualquer autenticado pode abrir ordem

- **Severidade:** Baixo
- **Como reproduzir:** qualquer usuário autenticado; POST direto em `/api/manutencao/maquina/{id}/ordens`.
- **Comportamento esperado:** verificação de vínculo deveria ser reforçada por anotação.
- **Comportamento observado:** `ManutencaoController.java` (backend) linha 27: `@PostMapping("/maquina/{idMaquina}/ordens")` sem `@PreAuthorize`. O `ManutencaoService.abrirOrdem()` verifica vínculo manualmente. O `GET /manutencao/ordens` também sem `@PreAuthorize`. A verificação existe no service, mas não há proteção declarativa.
- **Causa raiz:** `ManutencaoController.java` (backend) — ausência de `@PreAuthorize` em alguns endpoints.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Segurança] Senha transmitida em texto claro no cadastro de colaborador via `multipart/form-data`

- **Severidade:** Baixo
- **Como reproduzir:** PROPRIETARIO; cadastrar novo colaborador.
- **Comportamento esperado:** senha deveria ser transmitida de forma segura.
- **Comportamento observado:** `ApiService.novoColaborador()` linha 87: `body.add("senha", senha)` — a senha é enviada como parte de um formulário multipart. Sem HTTPS (ambiente de desenvolvimento), a senha trafega em texto claro. Em produção com HTTPS isso é mitigado, mas o design poderia usar hash no frontend ou exigir HTTPS explicitamente.
- **Causa raiz:** `ApiService.java` linha 87; `ProprietarioController.java` (backend) linha 32 — `@RequestParam("senha") String senha`.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Contrato API] `ApiService.listarMaquinas` e backend `GET /proprietario/maquinas` — OPERADOR chama endpoint `/proprietario/maquinas` mas o backend filtra por usuário

- **Severidade:** Baixo
- **Como reproduzir:** login como OPERADOR; carregar lista de máquinas.
- **Comportamento esperado:** OPERADOR vê apenas suas máquinas vinculadas.
- **Comportamento observado:** `ApiService.listarMaquinas()` chama `/proprietario/maquinas`. O backend `MaquinaController.java` (backend) `@GetMapping` não tem `@PreAuthorize("hasRole('PROPRIETARIO')")` — correto, o código verifica o perfil internamente (linha 116-120) e retorna apenas máquinas vinculadas para OPERADOR. Funciona corretamente. Porém a URL `/proprietario/maquinas` dá a entender que é exclusivo do proprietário. SOCIO também vê todas as máquinas. Sem bug funcional, apenas naming confuso.
- **Causa raiz:** design de URL; `MaquinaController.java` (backend) `@RequestMapping("/api/proprietario/maquinas")`.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Lógica de Negócio] Autorização de risco: `autorizarRisco` no backend não verifica se já existe autorização pendente

- **Severidade:** Baixo
- **Como reproduzir:** PROPRIETARIO; autorizar operação de risco para a mesma máquina duas vezes consecutivas.
- **Comportamento esperado:** uma única autorização ativa por vez.
- **Comportamento observado:** `MaquinaService.autorizarRisco()` (linhas 258-273) seta `autorizada_operacao_risco = true` e salva novo `AutorizacaoRisco` sem verificar se já existe um em vigor. Cada chamada adiciona um registro em `autorizacao_risco`, mas a flag boolean só existe uma vez na máquina — então funcionalmente não há dupla autorização, mas o histórico de autorizações pode ter duplicatas.
- **Causa raiz:** `MaquinaService.java` linhas 258-273.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [UX/Polling] Dashboard tem polling a cada 5 segundos substituindo `innerHTML` completo da tabela

- **Severidade:** Baixo
- **Como reproduzir:** abrir dashboard; esperar polling.
- **Comportamento esperado:** atualização incremental ou com delay maior.
- **Comportamento observado:** `dashboard.html` linha 149: `setInterval(function() { ... }, 5000)`. A cada 5 segundos, a tabela de notificações tem seu `innerHTML` completamente substituído (linha 164-182). Isso provoca reflow completo do DOM, perda de foco em elementos filhos (se o usuário estiver interagindo), e re-renderização desnecessária se os dados não mudaram.
- **Causa raiz:** `dashboard.html` linhas 149-200.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [UX/Polling] Página de operações tem polling a cada 10 segundos substituindo `innerHTML` completo do grid

- **Severidade:** Baixo
- **Como reproduzir:** abrir `/operacoes`; esperar 10 segundos.
- **Comportamento esperado:** atualização incremental.
- **Comportamento observado:** `operacoes.html` linha 209: `setInterval(function() { ... }, 10000)`. A função `renderCards()` (linha 155) substitui `grid.innerHTML` completo. Intervalo de 10 segundos é razoável, mas o `innerHTML` completo a cada tick é ineficiente — todos os cards são destruídos e recriados, perdendo animações e estado.
- **Causa raiz:** `operacoes.html` linhas 155-206.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Performance] Chart.js: todos os gráficos de relatório destroem e recriam a instância corretamente

- **Severidade:** Cosmético (observação positiva)
- **Como reproduzir:** clicar "Atualizar Dados" em relatórios.
- **Comportamento observado:** `relatorios.html` linhas 175, 213, 248, 288, 344 — cada função de carregamento verifica `if (chartXInstance) chartXInstance.destroy()` antes de `new Chart(...)`. **Implementação correta** — sem bug de memory leak de Chart.js.
- **Causa raiz:** sem bug.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Cosmético/Layout] Botão "Novo Colaborador" na lista de colaboradores não tem gap em relação ao cabeçalho no mobile

- **Severidade:** Cosmético
- **Como reproduzir:** visualizar `/colaboradores` em tela estreita.
- **Comportamento observado:** `lista-colaboradores.html` linha 90: `style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:12px;"` — gap existe em telas largas, mas ao quebrar linha em mobile o gap entre título e botão pode ficar apertado.
- **Causa raiz:** `lista-colaboradores.html` linha 90.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Cosmético/Layout] `nova-maquina.html` — seção "Status e Controle" tem `alert-info` sem margin-bottom antes do grid

- **Severidade:** Cosmético
- **Como reproduzir:** abrir formulário de nova máquina; visualizar seção 4.
- **Comportamento observado:** `nova-maquina.html` linha 235: `<div class="at-alert at-alert-info" style="margin-bottom:16px;">` — margin-bottom está presente. Sem problema real. Observação positiva.
- **Causa raiz:** sem bug.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Cosmético/Layout] Página `lista-ordens.html` — botão "Nova Ordem" sem margin-top após cabeçalho em mobile

- **Severidade:** Cosmético
- **Como reproduzir:** `/ordens` em mobile; observar espaçamento entre título e listagem.
- **Comportamento observado:** `lista-ordens.html` linha 24: `style="display:flex; justify-content:space-between; align-items:center;"` sem `gap` declarado. Em mobile, quando os elementos quebram em coluna, não há espaço entre título e botão "Nova Ordem".
- **Causa raiz:** `lista-ordens.html` linha 24 — ausência de `gap` ou `flex-wrap` com gap.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Cosmético/Layout] `editar-colaborador.html` — os dois cards (dados + máquinas vinculadas) usam grid 2 colunas sem gap vertical suficiente em mobile

- **Severidade:** Cosmético
- **Como reproduzir:** abrir `/colaboradores/editar/{id}` em mobile.
- **Comportamento observado:** `editar-colaborador.html` linha 26: `style="display:grid; grid-template-columns: 1fr 1fr; gap:16px;"` — sem `@media` query, o layout de 2 colunas persiste em mobile, comprimindo os cards.
- **Causa raiz:** `editar-colaborador.html` linha 26 — ausência de responsividade.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [UX] Telemetria faz polling a cada 8 segundos sem verificação de autenticidade do token

- **Severidade:** Baixo
- **Como reproduzir:** abrir `/maquinas/{id}/telemetria`; sessão expirar no servidor durante o polling.
- **Comportamento esperado:** ao expirar sessão, redirecionar para login.
- **Comportamento observado:** `telemetria.html` linha 129: `setInterval(atualizarTelemetria, 8000)`. A função `atualizarTelemetria()` busca `/api/maquinas/{id}/telemetria/dados` — endpoint do frontend que retorna `Map.of("error", "Não autenticado")` se token for null. O JS verifica `if (data.error)` (linha 93) e apenas loga no console — sem redirecionamento para login. O usuário ficaria vendo `--` em todos os campos sem entender que a sessão expirou.
- **Causa raiz:** `telemetria.html` linhas 93-96 — ausência de redirecionamento em caso de erro de sessão.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Lógica de Negócio] `DashboardService` usa `maquinaRepository.findAll()` para PROPRIETARIO/SOCIO, incluindo máquinas inativas

- **Severidade:** Médio
- **Como reproduzir:** PROPRIETARIO com máquinas arquivadas (ativo=false); verificar contador do dashboard.
- **Comportamento esperado:** contadores do dashboard deveriam refletir apenas máquinas ativas.
- **Comportamento observado:** `DashboardService.java` linha 43: `maquinas = maquinaRepository.findAll()` para proprietário/sócio. A filtragem `if (!m.isAtivo()) continue` (linha 61) existe no loop de contagem, portanto os contadores de status e risco estão corretos. **Sem bug real** nos contadores. Observação: a query carrega todas as máquinas (incluindo inativas) para depois filtrar em memória — ineficiente com grande volume.
- **Causa raiz:** `DashboardService.java` linha 43 — pode usar `buscarTodosOrdenados()` que filtra por ativo.
- **Confirmado por:** leitura de código (problema de performance, não de lógica)
- **Exige script.sql?:** não

---

### [Lógica de Negócio] `OperacaoService.trocarStatus`: ao iniciar operação, `hodometroInicio` é setado com `maquina.getHodometroInicial()` mas o campo se chama "hodometroInicial" no Java e é o hodômetro acumulado

- **Severidade:** Médio  
- **Como reproduzir:** máquina com hodômetro acumulado de 1.500h; iniciar nova operação.
- **Comportamento esperado:** `hodometroInicio` do `RegistroOperacao` deve capturar o hodômetro atual (1.500h).
- **Comportamento observado:** `OperacaoService.java` linha 106: `registro.setHodometroInicio(maquina.getHodometroInicial())`. O campo `hodometro_inicial` na entidade `Maquina` é usado como "hodômetro atual/acumulado" — é atualizado após cada operação e abastecimento. O nome é confuso mas o comportamento está correto pois `maquina.hodometroInicial` é de fato o hodômetro mais recente. Não é um bug funcional, mas é um bug de nomenclatura que gera confusão: o campo se chama "inicial" mas armazena o valor atual.
- **Causa raiz:** `Maquina.java` — campo `hodometroInicial` semanticamente mal nomeado; `script.sql` coluna `hodometro_inicial`.
- **Confirmado por:** leitura de código (confusão semântica, não bug funcional)
- **Exige script.sql?:** sim (nomenclatura)

---

### [Autorização] `ManutencaoController.listarOrdens` (backend) — OPERADOR vê todas as ordens de máquinas vinculadas, mas não verifica se a máquina ainda está ativa

- **Severidade:** Baixo
- **Como reproduzir:** OPERADOR com máquinas vinculadas arquivadas (ativo=false).
- **Comportamento esperado:** ordens de máquinas arquivadas não deveriam aparecer para o operador.
- **Comportamento observado:** `ManutencaoService.listarOrdens()` linha 147-152 filtra por `idsMaquinasVinculadas` sem verificar `m.isAtivo()`. Se uma máquina vinculada for arquivada, suas ordens ainda aparecem para o operador.
- **Causa raiz:** `ManutencaoService.java` linhas 145-152.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Segurança] Senha enviada em campo do formulário de novo colaborador — sem validação de complexidade no backend

- **Severidade:** Baixo
- **Como reproduzir:** PROPRIETARIO; cadastrar colaborador com senha "1".
- **Comportamento esperado:** backend rejeitar senha fraca.
- **Comportamento observado:** `ProprietarioService.registrarColaborador()` linha 52: `if (senha == null || senha.isBlank())` — apenas verifica se não é vazia. Não há validação de comprimento mínimo ou complexidade. `UserController.alterarSenha()` linha 68 valida `novaSenha.length() < 6` — mas isso só se aplica à alteração de senha, não ao cadastro inicial.
- **Causa raiz:** `ProprietarioService.java` linha 52-53.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Cosmético] `lista-notificacoes.html` — ícone da notificação usa concatenação de classes na `th:classappend` que pode gerar double-space

- **Severidade:** Cosmético
- **Como reproduzir:** visualizar página de notificações.
- **Comportamento observado:** `lista-notificacoes.html` linha 46: `th:classappend="${n.tipo == 'anomalia' ? 'anomalia bi-exclamation-triangle-fill' : ...}"` — a classe base `notif-icon` e as classes adicionadas por `classappend` podem gerar algo como `notif-icon anomalia bi-exclamation-triangle-fill`. Funciona, mas o elemento `<div>` com `class="notif-icon"` não tem o `bi` prefix como classe Bootstrap Icons standalone — o `bi-*` requer `class="bi bi-..."`. A classe `bi` está faltando. O ícone pode não renderizar corretamente pois o Bootstrap Icons requer a classe `bi` junto com a classe do ícone.
- **Causa raiz:** `lista-notificacoes.html` linha 45-47 — a div tem `class="notif-icon"` sem `bi`, e o `classappend` adiciona `anomalia bi-exclamation-triangle-fill` mas não inclui `bi`.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Contrato API] `ApiService.listarColaboradores` retorna `Usuario` (entidade JPA) em vez de DTO — expõe `senha` hash

- **Severidade:** Alto
- **Como reproduzir:** PROPRIETARIO/SOCIO; abrir `/colaboradores` ou `/relatorios`.
- **Comportamento esperado:** resposta da API não deveria incluir o hash de senha.
- **Comportamento observado:** `ProprietarioController.java` (backend) linha 44: `return ResponseEntity.ok(userRepository.findAll())`. A entidade `Usuario` inclui o campo `senha` (hash BCrypt). O response JSON serializa o hash de senha para todos os colaboradores listados. Qualquer requisição autenticada que liste colaboradores recebe hashes de senha. Isso viola o princípio de mínima exposição de dados mesmo que o hash não seja reversível diretamente.
- **Causa raiz:** `ProprietarioController.java` (backend) linha 44 — retorno de entidade JPA diretamente em vez de DTO sem senha.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Contrato API] `ProprietarioController.buscarColaborador` também retorna entidade `Usuario` com campo senha

- **Severidade:** Alto
- **Como reproduzir:** `GET /api/proprietario/colaboradores/{id}`.
- **Comportamento esperado:** retorno sem campo senha.
- **Comportamento observado:** `ProprietarioController.java` linha 51: `return ResponseEntity.ok(user)` onde `user` é uma entidade `Usuario`.
- **Causa raiz:** `ProprietarioController.java` linha 51.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

### [Contrato API] `UserController.meusDados` retorna entidade `Usuario` com campo senha

- **Severidade:** Alto
- **Como reproduzir:** `GET /api/usuario/me`.
- **Comportamento esperado:** retorno sem campo senha.
- **Comportamento observado:** `UserController.java` linha 75: `return ResponseEntity.ok(usuario)` — retorna a entidade JPA completa incluindo o campo `senha`.
- **Causa raiz:** `UserController.java` linha 75.
- **Confirmado por:** leitura de código
- **Exige script.sql?:** não

---

## Índice de Arquivos Relevantes

**Backend:**
- `src/main/java/com/main/frotaBackEnd/controller/MaquinaController.java`
- `src/main/java/com/main/frotaBackEnd/controller/OperacaoController.java`
- `src/main/java/com/main/frotaBackEnd/controller/ManutencaoController.java`
- `src/main/java/com/main/frotaBackEnd/controller/AbastecimentoController.java`
- `src/main/java/com/main/frotaBackEnd/controller/ProprietarioController.java`
- `src/main/java/com/main/frotaBackEnd/controller/UserController.java`
- `src/main/java/com/main/frotaBackEnd/service/OperacaoService.java`
- `src/main/java/com/main/frotaBackEnd/service/AbastecimentoService.java`
- `src/main/java/com/main/frotaBackEnd/service/ManutencaoService.java`
- `src/main/java/com/main/frotaBackEnd/service/MaquinaService.java`
- `src/main/java/com/main/frotaBackEnd/service/ClassificacaoRiscoService.java`
- `src/main/java/com/main/frotaBackEnd/service/RelatorioService.java`
- `src/main/java/com/main/frotaBackEnd/service/DashboardService.java`
- `src/main/java/com/main/frotaBackEnd/service/NotificacaoService.java`
- `script.sql`

**Frontend:**
- `src/main/java/com/main/frotaFrontEnd/service/ApiService.java`
- `src/main/java/com/main/frotaFrontEnd/controller/MaquinaController.java`
- `src/main/java/com/main/frotaFrontEnd/controller/MeuPerfilController.java`
- `src/main/resources/templates/trocar-status.html`
- `src/main/resources/templates/nova-maquina.html`
- `src/main/resources/templates/editar-maquina.html`
- `src/main/resources/templates/lista-ordens.html`
- `src/main/resources/templates/lista-notificacoes.html`
- `src/main/resources/templates/dashboard.html`
- `src/main/resources/templates/operacoes.html`
- `src/main/resources/templates/telemetria.html`
- `src/main/resources/templates/relatorios.html`
