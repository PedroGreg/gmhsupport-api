CREATE TABLE usuarios (
                          id_usuario BIGINT PRIMARY KEY AUTO_INCREMENT,
                          nome VARCHAR(150) NOT NULL,
                          email VARCHAR(150) NOT NULL UNIQUE,
                          senha VARCHAR(255) NOT NULL,
                          telefone VARCHAR(20),
                          endereco VARCHAR(255),
                          data_cadastro DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tecnicos (
                          id_tecnico BIGINT PRIMARY KEY AUTO_INCREMENT,
                          nome VARCHAR(150) NOT NULL,
                          email VARCHAR(150) NOT NULL UNIQUE,
                          senha VARCHAR(255) NOT NULL,
                          especialidade VARCHAR(100),
                          disponivel BOOLEAN DEFAULT TRUE,
                          data_cadastro DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categorias_problema (
                                     id_categoria BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     nome VARCHAR(100) NOT NULL,
                                     descricao VARCHAR(255)
);

CREATE TABLE status_chamado (
                                id_status BIGINT PRIMARY KEY AUTO_INCREMENT,
                                descricao VARCHAR(50) NOT NULL
);

CREATE TABLE chamados (
                          id_chamado BIGINT PRIMARY KEY AUTO_INCREMENT,
                          id_usuario BIGINT NOT NULL,
                          id_tecnico BIGINT,
                          id_categoria BIGINT,
                          id_status BIGINT NOT NULL DEFAULT 1,
                          titulo VARCHAR(150) NOT NULL,
                          descricao TEXT NOT NULL,
                          prioridade ENUM('baixa','media','alta','urgente') DEFAULT 'media',
                          data_abertura DATETIME DEFAULT CURRENT_TIMESTAMP,
                          data_atendimento DATETIME,
                          data_fechamento DATETIME,
                          FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
                          FOREIGN KEY (id_tecnico) REFERENCES tecnicos(id_tecnico),
                          FOREIGN KEY (id_categoria) REFERENCES categorias_problema(id_categoria),
                          FOREIGN KEY (id_status) REFERENCES status_chamado(id_status)
);

CREATE TABLE historico_chamado (
                                   id_historico BIGINT PRIMARY KEY AUTO_INCREMENT,
                                   id_chamado BIGINT NOT NULL,
                                   id_tecnico BIGINT,
                                   descricao TEXT NOT NULL,
                                   data_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
                                   FOREIGN KEY (id_chamado) REFERENCES chamados(id_chamado),
                                   FOREIGN KEY (id_tecnico) REFERENCES tecnicos(id_tecnico)
);

CREATE TABLE avaliacoes (
                            id_avaliacao BIGINT PRIMARY KEY AUTO_INCREMENT,
                            id_chamado BIGINT NOT NULL UNIQUE,
                            nota INT NOT NULL CHECK (nota BETWEEN 0 AND 5),
                            comentario TEXT,
                            data_avaliacao DATETIME DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (id_chamado) REFERENCES chamados(id_chamado)
);

INSERT INTO status_chamado (descricao) VALUES
                                           ('Aberto'),
                                           ('Em andamento'),
                                           ('Aguardando usuário'),
                                           ('Resolvido'),
                                           ('Cancelado');

INSERT INTO categorias_problema (nome, descricao) VALUES
                                                      ('Hardware', 'Problemas com equipamentos físicos'),
                                                      ('Software', 'Problemas com sistemas e programas'),
                                                      ('Rede', 'Problemas de conexão e internet'),
                                                      ('Impressora', 'Problemas relacionados a impressão'),
                                                      ('Outros', 'Problemas diversos');