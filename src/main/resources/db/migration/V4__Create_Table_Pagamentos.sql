-- Pagamento do serviço: criado quando o técnico finaliza o chamado informando um valor.
-- Um chamado tem no máximo um pagamento.
CREATE TABLE pagamentos (
                            id_pagamento BIGINT PRIMARY KEY AUTO_INCREMENT,
                            id_chamado BIGINT NOT NULL UNIQUE,
                            valor DECIMAL(10,2) NOT NULL,
                            status ENUM('pendente','aguardando_pix','pago') NOT NULL DEFAULT 'pendente',
                            forma ENUM('pix','credito','debito'),
                            parcelas INT,
                            cartao_final VARCHAR(4),
                            codigo_pix VARCHAR(255),
                            data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,
                            data_pagamento DATETIME,
                            FOREIGN KEY (id_chamado) REFERENCES chamados(id_chamado)
);
