# Arquitetura

A primeira versão usa arquitetura em camadas para reduzir complexidade durante o aprendizado:

1. `controller`: recebe e devolve HTTP.
2. `service`: contém regras de negócio e transações.
3. `repository`: acessa o banco com Spring Data JPA.
4. `entity`: mapeia tabelas do PostgreSQL.
5. `dto`: define contratos da API.
6. `exception`: padroniza erros com ProblemDetail.

A estrutura poderá evoluir para módulos por domínio quando os módulos de documentos, carteira e relatórios crescerem.
