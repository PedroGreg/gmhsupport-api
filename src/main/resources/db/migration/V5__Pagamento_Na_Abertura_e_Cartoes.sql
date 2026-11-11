-- 1) Preço base de cada categoria (o valor final ainda recebe o acréscimo da prioridade)
ALTER TABLE categorias_problema ADD COLUMN valor_base DECIMAL(10,2) NOT NULL DEFAULT 60.00;

UPDATE categorias_problema SET valor_base = 100.00 WHERE nome = 'Hardware';
UPDATE categorias_problema SET valor_base = 80.00  WHERE nome = 'Software';
UPDATE categorias_problema SET valor_base = 90.00  WHERE nome = 'Rede';
UPDATE categorias_problema SET valor_base = 70.00  WHERE nome = 'Impressora';
UPDATE categorias_problema SET valor_base = 60.00  WHERE nome = 'Outros';

-- 2) Cartões salvos do usuário. Nunca guardamos o número completo nem o CVV.
CREATE TABLE cartoes (
                         id_cartao BIGINT PRIMARY KEY AUTO_INCREMENT,
                         id_usuario BIGINT NOT NULL,
                         apelido VARCHAR(40),
                         tipo ENUM('credito','debito') NOT NULL,
                         bandeira VARCHAR(20) NOT NULL,
                         ultimos_digitos VARCHAR(4) NOT NULL,
                         nome_titular VARCHAR(100) NOT NULL,
                         mes_validade INT NOT NULL,
                         ano_validade INT NOT NULL,
                         principal BOOLEAN NOT NULL DEFAULT FALSE,
                         data_cadastro DATETIME DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

-- 3) Pagamento agora é feito na abertura: guarda a bandeira e permite estorno (chamado cancelado)
ALTER TABLE pagamentos ADD COLUMN bandeira VARCHAR(20);
ALTER TABLE pagamentos MODIFY COLUMN status ENUM('pendente','aguardando_pix','pago','estornado') NOT NULL DEFAULT 'pendente';
