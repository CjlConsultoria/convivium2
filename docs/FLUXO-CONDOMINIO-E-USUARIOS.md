# Fluxo: Condomínio e Usuários (Convivium)

## Quem faz o quê

- **Condomínio** não “se cadastra” na tela de login. O condomínio é **criado pelo Admin** da plataforma.
- Na tela de **login** e **cadastro** entram **pessoas** (moradores, síndico, etc.): quem tem conta faz login; quem não tem pode se cadastrar como morador (com aprovação do síndico) ou ser cadastrado pelo síndico dentro do condomínio.

---

## 1. Admin cria o condomínio

1. Admin faz login e acessa **Painel Administrativo**.
2. Em **Gestão → Condomínios** clica em **Novo Condomínio**.
3. Preenche dados (nome, endereço, etc.) e **Criar Condomínio**.
4. O condomínio é criado com status **Ativo** e aparece na lista.

---

## 2. Configurar o condomínio (blocos e unidades)

1. Na lista de condomínios, admin clica no **condomínio** para abrir o detalhe.
2. Na aba **Blocos**: adiciona blocos (ex.: Bloco A, Bloco B).
3. Na aba **Unidades** (ou equivalente): cadastra as unidades de cada bloco (ex.: 101, 102, 201…).
4. Sem blocos e unidades, **moradores não conseguem se cadastrar** (precisam escolher uma unidade).

---

## 3. Entrar no condomínio e adicionar o primeiro usuário (Síndico)

1. Na tela de **detalhe do condomínio**, use o botão **“Acessar Gestão de Moradores”** (ou link para o painel do condomínio).
2. Isso leva para **`/c/{id}/users`** (painel do condomínio).
3. Admin (como plataforma) pode acessar qualquer condomínio; o menu lateral mostra o painel daquele condomínio.
4. Em **Moradores** (ou **Usuários**), clique em **Novo Cadastro**.
5. Cadastre o **primeiro usuário** (ex.: síndico): nome, e-mail, senha, **Cargo = Síndico**, unidade (se aplicável).
6. Esse usuário passa a poder fazer **login** e acessar o painel do condomínio normalmente.

---

## 4. Dentro do condomínio: editar, adicionar e excluir usuários

- **Editar condomínio (dados gerais):** no **Painel Admin** → Condomínios → clicar no condomínio → editar dados e status.
- **Editar / adicionar / excluir usuários:** dentro do **painel do condomínio** (`/c/{id}`):
  - **Moradores** (ou **Usuários**): lista de pessoas do condomínio.
  - **Novo Cadastro:** adicionar morador ou funcionário (cargo e unidade).
  - **Clicar em um usuário:** ver detalhe, **editar** dados, **aprovar/rejeitar** (se pendente), **excluir** (conforme permissões do cargo).

Resumo: **quem cria o condomínio é o Admin; quem edita dados do condomínio é o Admin; quem gerencia usuários (incluindo síndico) é feito dentro do painel do condomínio** (acessível pelo Admin ou pelo Síndico, conforme permissões).

---

## 5. Cadastro pela tela de login (morador)

1. Na tela de **login**, o link **“Cadastre-se”** leva para a **tela de cadastro**.
2. O **condomínio não se cadastra** ali; a pessoa escolhe **em qual condomínio** quer se cadastrar.
3. Ela seleciona:
   - **Condomínio** (lista dos condomínios ativos),
   - **Unidade** (lista das unidades daquele condomínio).
4. Preenche nome, e-mail, senha, etc. e envia.
5. O usuário é criado como **morador** com status **Pendente de aprovação**.
6. O **síndico** (ou quem tiver permissão) entra no painel do condomínio → **Moradores** → **Pendentes** e **aprova** o cadastro.
7. Depois da aprovação, o morador faz **login** normalmente e acessa o painel do condomínio.

---

## Resumo rápido

| Ação | Onde | Quem |
|------|------|------|
| Criar condomínio | Admin → Condomínios → Novo | Admin |
| Editar condomínio (dados/status) | Admin → Condomínios → [condomínio] | Admin |
| Cadastrar blocos/unidades | Admin → Condomínio → Blocos/Unidades | Admin |
| Adicionar primeiro síndico / usuários | Painel do condomínio → Moradores → Novo Cadastro | Admin ou Síndico |
| Editar / aprovar / excluir usuários | Painel do condomínio → Moradores | Síndico / Sub-síndico / Porteiro (conforme permissão) |
| Morador se cadastrar sozinho | Tela de cadastro (link “Cadastre-se” no login) | Qualquer pessoa |
| Condomínio “se cadastrar” | Não existe; condomínio é criado só pelo Admin | — |

---

## Observação sobre a tela de login

Na **tela de login** só existem **login** e **cadastro de pessoa** (morador). O **condomínio não faz cadastro** ali; ele já deve ter sido criado pelo Admin e ter blocos/unidades cadastrados para que os moradores possam se cadastrar escolhendo condomínio e unidade.
