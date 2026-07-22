# Boas práticas de Git — MyCash / Financeiro

Guia prático do fluxo que usamos neste projeto.

---

## 1. Branches

| Branch | Para quê |
|--------|----------|
| `main` | Código estável / “produção” |
| `develop` | Integração do dia a dia |
| `feature/nome-curto` | Uma funcionalidade por vez |
| `fix/nome-curto` | Correção de bug |
| `chore/nome-curto` | Config, build, docs |

### Regras

- Nunca trabalhe direto na `main`.
- Features saem da `develop` e voltam para a `develop` (PR).
- Só promova `develop` → `main` quando estiver estável e testado.
- Nome da branch em minúsculas, com hífen: `feature/metas`, `fix/cor-dashboard`.

---

## 2. Fluxo do dia a dia

```text
main (estável)
  └── develop (integração)
        └── feature/minha-feature
              → PR para develop
              → merge + delete branch
```

### Comandos base

```bash
# Atualizar develop
git checkout develop
git pull origin develop

# Nova feature
git checkout -b feature/minha-feature

# ... alterar código ...

git status
git add caminho/dos/arquivos   # preferir paths específicos
git commit -m "feat: descrição curta"
git push -u origin feature/minha-feature
```

No GitHub:

1. **Pull Request**: base `develop` ← compare `feature/minha-feature`
2. Revisar → **Merge**
3. **Delete branch**

No PC, depois do merge:

```bash
git checkout develop
git pull origin develop
git branch -d feature/minha-feature
git fetch --prune
```

### Promover para main (release)

```bash
git checkout main
git pull origin main
# PR no GitHub: base main ← develop
# ou, se o time permitir merge local:
# git merge develop
# git push origin main
```

---

## 3. Commits

### Formato

```text
tipo: resumo em uma linha

(opcional) explicação do porquê em 1–2 frases
```

### Tipos comuns

| Prefixo | Uso |
|---------|-----|
| `feat:` | Nova funcionalidade |
| `fix:` | Correção de bug |
| `chore:` | Build, deps, config |
| `docs:` | Documentação |
| `refactor:` | Melhoria sem mudar comportamento |
| `test:` | Testes |

### Exemplos bons

```text
feat: CRUD de categorias com seletor de cor
fix: gerar despesa ao salvar conta recorrente
chore: estrutura base do sistema financeiro desktop
```

### Evite

- `update`, `ajustes`, `wip`, `commit`
- misturar várias features no mesmo commit
- commitar arquivos gerados (`target/`, `.idea/`, CSV de export)

---

## 4. O que adicionar no stage

```bash
# Bom — só o que é da feature
git add src/main/java/service/CategoryService.java
git add src/main/resources/fxml/categories-view.fxml

# Ruim na maior parte do tempo (entra tudo)
git add .
```

Use `git add .` só quando **todo** o working tree for daquela mudança e você já revisou com `git status`.

Sempre antes do commit:

```bash
git status
git diff --staged
```

---

## 5. Pull Request (PR)

Todo PR deve ter:

- **Título** claro (`feat: ...` / `fix: ...`)
- **Summary**: o que mudou (1–3 bullets)
- **Test plan**: como validar

Exemplo:

```markdown
## Summary
- CRUD de categorias
- ColorPicker na tela

## Test plan
- [ ] Criar categoria
- [ ] Editar e excluir
- [ ] Pesquisar por nome
```

Uma feature = um PR. Não misture tema + cartões + metas no mesmo PR.

---

## 6. Comandos essenciais

| Comando | Função |
|---------|--------|
| `git status` | Onde estou e o que mudou |
| `git log --oneline --graph -15` | Histórico visual |
| `git branch -vv` | Branches e tracking remoto |
| `git pull` | Buscar e integrar remoto |
| `git fetch --prune` | Atualizar refs e limpar branches apagadas no remoto |
| `git reset` | Tirar arquivos do stage (não apaga do disco) |
| `git branch -d nome` | Apagar branch local já mergeada |

---

## 7. Erros comuns

| Situação | O que fazer |
|----------|-------------|
| `nothing added to commit` | Faltou `git add` |
| `src refspec main does not match any` | Ainda não existe commit na branch |
| Stage com arquivos demais | `git reset` e `git add` só o necessário |
| Trabalhou na branch errada | `git stash` / commit na branch certa, ou peça ajuda antes de forçar push |
| Conflito no PR | Atualize a base (`pull` na develop), resolva, commit, push |

**Nunca** use `git push --force` na `main` ou `develop` sem combinar com o time.

---

## 8. Checklist rápido antes do push

- [ ] Estou na branch certa? (`git branch`)
- [ ] `git status` mostra só o que quero?
- [ ] Mensagem de commit com `feat:` / `fix:` / `chore:`?
- [ ] Não entrei `target/`, `.idea/`, backups, CSV gerado?
- [ ] PR aponta para a base certa (`develop` ou `main`)?

---

## 9. Mapa mental

```text
1. checkout develop + pull
2. checkout -b feature/...
3. codar
4. add seletivo + commit
5. push
6. PR → merge → delete branch
7. voltar develop + pull
```

Seguir isso mantém o histórico limpo e o GitHub fácil de entender.
