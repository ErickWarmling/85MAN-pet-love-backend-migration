# Migração do Pet Love: PostgreSQL → MySQL

**Acadêmico:** Erick Augusto Warmling<br>
**Curso:** Engenharia de Software<br>
**Disciplina:** Manutenção de Software (85MAN)

---

## 1. Contextualização e Motivação

### Repositório escolhido

**Link:** https://github.com/lucas-gitirana/pet-love

Pet Love é uma aplicação Java Spring Boot voltada à gestão de clínicas veterinárias. A plataforma permite o cadastro e gerenciamento de usuários, pets, espécies, raças, consultas e solicitações de adoção, com autenticação de acesso e separação por perfis.

O projeto foi desenvolvido por [Erick Augusto Warmling](https://github.com/ErickWarmling), [Lucas Emanoel Gitirana](https://github.com/lucas-gitirana) e [Marco Antonio Garlini Possamai](https://github.com/MarcoPossamai) na disciplina de Projeto Integrador II, do curso de Engenharia de Software da Universidade do Estado de Santa Catarina (UDESC).

### Cenário Atual

Atualmente, o projeto usa PostgreSQL como banco de dados, integrado via Spring Data JPA/Hibernate. Isso pode ser confirmado em três lugares do código:

- No `pom.xml`, a dependência `org.postgresql:postgresql` é declarada como driver JDBC.
- No `application.properties`, a conexão aponta para `jdbc:postgresql://localhost:5432/petlove`, enquanto o dialeto do Hibernate está definido como `org.hibernate.dialect.PostgreSQLDialect`.
- Nos scripts auxiliares (pasta `scripts/`), o arquivo `limpa-banco.sql` usa blocos `DO $$ ... $$`, que são uma extensão específica do PostgreSQL (PL/pgSQL).

A aplicação roda em Java 17 com Spring Boot 3.4.4, o schema do banco é criado e atualizado automaticamente pelo Hibernate (`ddl-auto=update`), e as chaves primárias são geradas com `GenerationType.IDENTITY`.

### Cenário Alvo

A proposta é substituir o PostgreSQL por MySQL 8.0 (ou superior), mantendo toda a aplicação Java/Spring Boot como está. A tabela abaixo resume o que muda entre um cenário e outro:

| Item | Cenário Atual | Cenário Alvo |
|---|---|---|
| SGBD | PostgreSQL | MySQL 8.0+ |
| Driver JDBC | `org.postgresql:postgresql` | `com.mysql:mysql-connector-j` |
| Dialeto Hibernate | `PostgreSQLDialect` | `MySQLDialect` |
| String de conexão | `jdbc:postgresql://localhost:5432/petlove` | `jdbc:mysql://localhost:3306/petlove?useSSL=false&serverTimezone=UTC` |
| Charset | Padrão do Postgres (UTF-8) | `utf8mb4`, para preservar acentuação |
| Script `limpa-banco.sql` | PL/pgSQL (`DO $$ ... $$`) | SQL padrão MySQL (`TRUNCATE` + `FOREIGN_KEY_CHECKS`) |
| Entidades JPA / repositórios | Sem alteração | Sem alteração |

Ou seja: em resumo, muda-se o driver, a configuração de conexão, o dialeto do Hibernate e um dos scripts auxiliares. A aplicação em si permanece intacta.

### Justificativa e benefícios esperados

A escolha por essa migração se justifica por alguns motivos práticos:

- **Custo e disponibilidade de hospedagem:** MySQL é o banco padrão na maioria das hospedagens compartilhadas e planos gratuitos (cPanel, Railway, InfinityFree, entre outros), o que facilita bastante o deploy de um projeto acadêmico ou de portfólio.
- **Familiaridade da equipe:** é comum que times já tenham mais experiência com MySQL do que com PostgreSQL, o que reduz a curva de aprendizado e o tempo de manutenção.
- **Baixo risco técnico:** o Pet Love não usa nenhum recurso exclusivo do PostgreSQL (como tipos `JSONB`, arrays ou funções específicas). A camada de persistência é toda feita via JPA/Hibernate, então a maior parte do trabalho de migração se resume a trocar configuração e driver, sem precisar alterar as entidades ou os repositórios.

Ou seja, o benefício esperado não é ganho de performance ou de recursos, e sim de portabilidade e facilidade operacional. O projeto passa a rodar em qualquer ambiente que ofereça MySQL, que é mais amplamente disponível.

---

## 2. Ambiente e Pré-requisitos

Antes de iniciar a migração, é necessário ter disponível o seguinte:

| Categoria | Ferramenta | Finalidade |
|---|---|---|
| Desenvolvimento | JDK 17 | Mesma versão já usada pelo projeto |
| Desenvolvimento | Maven 3.9.x | O projeto já inclui o wrapper `mvnw`/`mvnw.cmd`, não é obrigatório ter o Maven instalado globalmente |
| Desenvolvimento | Git | Controle de versão / branch de migração |
| Banco de dados | Instância PostgreSQL acessível | Banco de origem, usado para o backup |
| Banco de dados | MySQL Server 8.0+ | Banco de destino, local ou via Docker |
| Banco de dados | Cliente `mysql` | Executar scripts e conferir dados no destino |
| Banco de dados | Cliente `psql` e `pg_dump` | Fazer o backup do banco de origem |
| Opcional | Docker e Docker Compose | Subir um MySQL descartável sem instalar nada na máquina |
| Opcional | `pgloader` | Automatizar a migração dos dados existentes do PostgreSQL para o MySQL |
| Opcional | MySQL Workbench ou DBeaver | Inspecionar visualmente o schema depois de migrado |

Além das ferramentas, duas dependências do próprio projeto precisam ser trocadas:

| Arquivo | O que muda |
|---|---|
| `demo/pom.xml` | Sai a dependência `org.postgresql:postgresql`, entra `com.mysql:mysql-connector-j` |
| `demo/src/main/resources/application.properties` | Mudam a URL de conexão, o `driver-class-name` e o `spring.jpa.database-platform` |

Nenhuma outra dependência do projeto (Spring Web, Spring Security, Spring Data JPA, etc.) precisa ser alterada. A migração é isolada na camada de acesso a dados.

---

## 3. Passo a Passo Executável (Roteiro de Migração)

O processo de migração está organizado nas seguintes etapas:

| Passo | O que é feito |
|---|---|
| 1 | Clonar o repositório e criar a branch de migração |
| 2 | Fazer backup do banco PostgreSQL atual |
| 3 | Subir uma instância MySQL (Docker ou instalação local) |
| 4 | Trocar o driver JDBC no `pom.xml` |
| 5 | Atualizar a conexão e o dialeto no `application.properties` |
| 6 | Reescrever o script `limpa-banco.sql` para sintaxe MySQL |
| 7 | Migrar os dados existentes (seed ou `pgloader`) |
| 8 | Subir a aplicação e conferir se está tudo funcionando |

### Passo 1 — Clonar o repositório e criar a branch de migração

Primeiramente, deve-se obter uma cópia local do repositório original e criar uma branch específica para as alterações da migração.

```bash
git clone https://github.com/lucas-gitirana/pet-love.git
cd pet-love
git checkout -b migracao/postgresql-para-mysql
```

A partir desse ponto, todas as alterações relacionadas à migração devem ser realizadas na branch `migracao/postgresql-para-mysql`, mantendo a versão original do projeto preservada na branch principal.

### Passo 2 — Fazer backup do banco PostgreSQL atual

Antes de qualquer alteração, é essencial ter um backup restaurável, tanto para segurança quanto para o plano de rollback.

```bash
pg_dump -h localhost -p 5432 -U postgres -d petlove -F c -f backup_petlove_postgres.dump
```

### Passo 3 — Subir uma instância MySQL

Usando Docker, por exemplo:

```bash
docker run --name petlove-mysql \
  -e MYSQL_ROOT_PASSWORD=admin \
  -e MYSQL_DATABASE=petlove \
  -e MYSQL_USER=petlove \
  -e MYSQL_PASSWORD=admin \
  -p 3306:3306 \
  -d mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_0900_ai_ci
```

Se preferir uma instalação local do MySQL em vez de Docker, o banco precisa ser criado manualmente com o charset correto:

```sql
CREATE DATABASE petlove
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
```

### Passo 4 — Trocar o driver no `demo/pom.xml`

Remover:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

Adicionar no mesmo lugar:

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

A versão não precisa ser especificada, pois já é controlada pelo `spring-boot-starter-parent`.

### Passo 5 — Atualizar o `demo/src/main/resources/application.properties`

De:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/petlove
spring.datasource.username=postgres
spring.datasource.password=admin
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

Para:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/petlove?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
spring.datasource.username=petlove
spring.datasource.password=admin
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

As linhas `spring.jpa.hibernate.ddl-auto=update` e `spring.jpa.show-sql=true` continuam iguais.

### Passo 6 — Reescrever o script `scripts/limpa-banco.sql`

O script atual usa PL/pgSQL, que não existe no MySQL. A versão para MySQL fica assim:

```sql
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE consulta;
TRUNCATE TABLE solicitacao_adocao;
TRUNCATE TABLE pes_pet;
TRUNCATE TABLE pet;
TRUNCATE TABLE raca;
TRUNCATE TABLE especie;
TRUNCATE TABLE usuario;
TRUNCATE TABLE funcionario;
TRUNCATE TABLE pessoa;

SET FOREIGN_KEY_CHECKS = 1;
```

O outro script, `scripts/povoamento-racas-especies.sql`, não precisa de nenhuma alteração de conteúdo. A sintaxe usada (`INSERT` com subselect) já é padrão ANSI e funciona igual no MySQL. Só é preciso executá-lo garantindo o charset UTF-8:

```bash
mysql --default-character-set=utf8mb4 -h 127.0.0.1 -u petlove -padmin petlove < scripts/povoamento-racas-especies.sql
```

### Passo 7 — Migrar os dados existentes

Se o banco atual tem apenas dados de exemplo (que é o caso deste projeto), basta rodar o script de povoamento do Passo 6 no banco novo.

Se houver dados reais que precisam ser preservados, o caminho recomendado é usar o `pgloader`, apontando da origem para o destino:

```bash
pgloader postgresql://postgres:admin@localhost:5432/petlove \
         mysql://petlove:admin@localhost:3306/petlove
```

Depois, vale conferir se a quantidade de registros bate nas duas bases, tabela por tabela, para garantir que nada ficou para trás.

### Passo 8 — Subir a aplicação

```bash
cd demo
./mvnw clean install
./mvnw spring-boot:run
```

Se tudo estiver certo, a aplicação sobe em `http://localhost:8080` sem erros de conexão nos logs.

---

## 4. Plano de Rollback e Testes de Validação

### Testes de Validação

Depois de subir a aplicação com o MySQL, o ideal é realizar algumas validações:

1. **Rodar a suíte de testes automatizados do projeto:**

   ```bash
   cd demo
   ./mvnw test
   ```

   Vale notar que os testes de integração do projeto (`@SpringBootTest`) usam a mesma configuração de banco da aplicação, ou seja, depois da migração eles vão rodar contra o MySQL. É importante que a instância de teste esteja no ar antes de rodar `mvn test`.

2. **Conferir se o schema foi criado corretamente**, com `SHOW TABLES;` no MySQL e verificando se as chaves estrangeiras e o charset (`utf8mb4`) das tabelas estão certos.

3. **Testar manualmente os principais endpoints da API**, garantindo que criação, listagem, atualização e remoção continuam funcionando normalmente:

   | Recurso | Endpoint | Ponto de atenção |
   |---|---|---|
   | Pessoas | `/pessoas` | CRUD básico |
   | Funcionários | `/funcionarios` | Herança JPA (tabelas `pessoa` + `funcionario`) |
   | Pets | `/pets` | Vínculo com espécie e raça |
   | Espécies / Raças | `/especies`, `/racas` | Nomes com acentuação exibidos corretamente |
   | Pessoa-Pet | (via `PetController`) | Campo booleano `pp_principal` salvo/lido corretamente |
   | Consultas | `/consultas` | Data/hora e valor numérico |
   | Solicitações de adoção | `/solicitacoes` | Fluxo completo pessoa → pet → solicitação |
   | Usuários | `/usuarios` | Vínculo `OneToOne` com pessoa |

   Os dois pontos de maior risco nessa migração são justamente os destacados na tabela: a acentuação nos nomes de espécies/raças e o campo booleano `pp_principal`, já que o MySQL representa booleanos como `TINYINT(1)` internamente.

Se todos esses pontos passarem, a migração pode ser considerada validada.

### Plano de Rollback

Caso alguma etapa falhe de forma crítica e não seja possível corrigir rapidamente, o processo de reversão é:

1. **Reverter o código para o estado anterior**, descartando as alterações feitas no `pom.xml`, no `application.properties` e nos scripts:

   ```bash
   git checkout main -- demo/pom.xml demo/src/main/resources/application.properties scripts/
   ```

   Ou, se a branch inteira de migração for descartada:

   ```bash
   git checkout main
   git branch -D migracao/postgresql-para-mysql
   ```

2. **Garantir que o PostgreSQL original ainda está disponível.** Se ele não foi alterado durante o processo, nenhuma ação extra é necessária além de reapontar a aplicação para ele. Se precisar ser recriado a partir do backup feito no Passo 2:

   ```bash
   psql -h localhost -U postgres -c "DROP DATABASE IF EXISTS petlove;"
   psql -h localhost -U postgres -c "CREATE DATABASE petlove;"
   pg_restore -h localhost -U postgres -d petlove backup_petlove_postgres.dump
   ```

3. **Remover o ambiente MySQL de teste**, se ele tiver sido criado só para essa tentativa de migração:

   ```bash
   docker stop petlove-mysql
   docker rm petlove-mysql
   ```

4. **Subir a aplicação novamente e repetir os testes básicos de validação**, para confirmar que tudo voltou a funcionar como antes da migração.
