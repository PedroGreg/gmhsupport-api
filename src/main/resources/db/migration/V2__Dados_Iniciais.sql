-- Dados iniciais: status, categorias/subcategorias com preços e contas de teste

INSERT INTO status_chamado (descricao) VALUES
    ('Aberto'), ('Em andamento'), ('Aguardando usuário'), ('Resolvido'), ('Cancelado'), ('Aguardando aprovação do adicional');

INSERT INTO categorias_problema (nome, descricao, valor_fixo) VALUES
    ('Hardware', 'Computadores e notebooks: defeitos físicos, upgrade e montagem', FALSE),
    ('Software', 'Sistema operacional, programas e arquivos', FALSE),
    ('Rede e internet', 'Conexão, Wi-Fi, roteadores e cabeamento', FALSE),
    ('Impressoras e scanners', 'Instalação, falhas de impressão e digitalização', FALSE),
    ('Celulares e tablets', 'Configuração, dados e aplicativos', FALSE),
    ('Periféricos e acessórios', 'Monitores, teclados, áudio, vídeo e energia', FALSE),
    ('Segurança e vírus', 'Vírus, invasões, golpes e proteção de dados', FALSE),
    ('E-mail, contas e nuvem', 'E-mail, acessos e armazenamento online', FALSE),
    ('Outros', 'O que não se encaixa nas outras categorias', FALSE),
    ('Testes', 'Somente para testar pagamentos (valor fixo, sem acréscimos)', TRUE);

INSERT INTO subcategorias_problema (id_categoria, nome, valor_base) VALUES
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Hardware'), 'Computador ou notebook não liga', 120.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Hardware'), 'Lento ou travando', 100.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Hardware'), 'Superaquecimento e limpeza interna', 110.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Hardware'), 'Tela de notebook quebrada ou com defeito', 150.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Hardware'), 'Teclado ou touchpad do notebook com defeito', 100.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Hardware'), 'Bateria ou carregador com defeito', 90.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Hardware'), 'Barulho estranho ou ventoinha', 90.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Hardware'), 'HD ou SSD com defeito ou cheio', 110.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Hardware'), 'Upgrade de memória RAM', 80.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Hardware'), 'Troca de HD por SSD', 120.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Hardware'), 'Montagem de computador', 200.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Hardware'), 'Sem imagem ou placa de vídeo com defeito', 130.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Hardware'), 'Fonte de energia com defeito', 110.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Hardware'), 'Entradas USB ou HDMI não funcionam', 90.00);

INSERT INTO subcategorias_problema (id_categoria, nome, valor_base) VALUES
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Software'), 'Formatação e reinstalação do Windows', 150.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Software'), 'Instalação de programas', 60.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Software'), 'Atualização do sistema', 70.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Software'), 'Erro ou travamento em programa', 80.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Software'), 'Tela azul', 120.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Software'), 'Ativação de licença (Windows ou Office)', 60.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Software'), 'Pacote Office com problema (Word, Excel...)', 70.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Software'), 'Driver ausente ou desatualizado', 70.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Software'), 'Backup e recuperação de arquivos', 130.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Software'), 'Migração de dados para computador novo', 140.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Software'), 'Configuração de sistema da empresa (ERP)', 120.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Software'), 'Inicialização lenta', 80.00);

INSERT INTO subcategorias_problema (id_categoria, nome, valor_base) VALUES
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Rede e internet'), 'Sem acesso à internet', 90.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Rede e internet'), 'Wi-Fi fraco ou caindo', 90.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Rede e internet'), 'Configuração de roteador', 100.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Rede e internet'), 'Instalação de repetidor ou rede mesh', 110.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Rede e internet'), 'Cabeamento de rede (por ponto)', 120.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Rede e internet'), 'Compartilhar arquivos e pastas na rede', 90.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Rede e internet'), 'VPN ou acesso remoto', 110.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Rede e internet'), 'Rede corporativa (switch, servidor)', 150.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Rede e internet'), 'Câmeras IP e CFTV em rede', 160.00);

INSERT INTO subcategorias_problema (id_categoria, nome, valor_base) VALUES
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Impressoras e scanners'), 'Impressora não imprime', 80.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Impressoras e scanners'), 'Instalação e configuração de impressora', 70.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Impressoras e scanners'), 'Impressora em rede ou Wi-Fi', 90.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Impressoras e scanners'), 'Papel atolando', 70.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Impressoras e scanners'), 'Impressão falhada ou manchada', 80.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Impressoras e scanners'), 'Troca de toner, cartucho ou tinta', 50.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Impressoras e scanners'), 'Scanner não funciona', 70.00);

INSERT INTO subcategorias_problema (id_categoria, nome, valor_base) VALUES
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Celulares e tablets'), 'Configuração de celular novo', 60.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Celulares e tablets'), 'Transferência de dados entre aparelhos', 70.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Celulares e tablets'), 'E-mail corporativo no celular', 50.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Celulares e tablets'), 'Celular lento ou travando', 70.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Celulares e tablets'), 'Problema com WhatsApp ou aplicativos', 50.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Celulares e tablets'), 'Backup de fotos e contatos', 60.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Celulares e tablets'), 'Tablet com problema', 70.00);

INSERT INTO subcategorias_problema (id_categoria, nome, valor_base) VALUES
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Periféricos e acessórios'), 'Mouse ou teclado não funciona', 50.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Periféricos e acessórios'), 'Monitor sem imagem ou com falhas', 80.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Periféricos e acessórios'), 'Instalação de segundo monitor', 60.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Periféricos e acessórios'), 'Webcam, microfone ou áudio', 60.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Periféricos e acessórios'), 'Headset ou caixa de som', 50.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Periféricos e acessórios'), 'Nobreak ou estabilizador', 80.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Periféricos e acessórios'), 'Projetor ou TV para reuniões', 90.00);

INSERT INTO subcategorias_problema (id_categoria, nome, valor_base) VALUES
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Segurança e vírus'), 'Remoção de vírus ou malware', 120.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Segurança e vírus'), 'Instalação de antivírus', 60.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Segurança e vírus'), 'Conta invadida ou hackeada', 110.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Segurança e vírus'), 'Análise de golpe ou phishing', 90.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Segurança e vírus'), 'Pop-ups e propagandas indesejadas', 80.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Segurança e vírus'), 'Senha do Windows esquecida', 70.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Segurança e vírus'), 'Criptografia e proteção de dados', 120.00);

INSERT INTO subcategorias_problema (id_categoria, nome, valor_base) VALUES
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'E-mail, contas e nuvem'), 'Configurar e-mail (Outlook ou Gmail)', 60.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'E-mail, contas e nuvem'), 'E-mail não envia ou não recebe', 70.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'E-mail, contas e nuvem'), 'Caixa de e-mail cheia', 60.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'E-mail, contas e nuvem'), 'Recuperar acesso a conta', 70.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'E-mail, contas e nuvem'), 'Google Drive, OneDrive ou outra nuvem', 70.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'E-mail, contas e nuvem'), 'Microsoft 365 ou Google Workspace', 90.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'E-mail, contas e nuvem'), 'Assinatura de e-mail', 40.00);

INSERT INTO subcategorias_problema (id_categoria, nome, valor_base) VALUES
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Outros'), 'Consultoria ou orientação técnica', 80.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Outros'), 'Outro problema de hardware', 90.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Outros'), 'Outro problema de software', 80.00),
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Outros'), 'Treinamento de uso', 90.00);

INSERT INTO subcategorias_problema (id_categoria, nome, valor_base) VALUES
    ((SELECT id_categoria FROM categorias_problema WHERE nome = 'Testes'), 'Teste de pagamento (Mercado Pago)', 5.01);

-- Contas de teste (as mesmas de antes)
INSERT INTO usuarios (nome, email, senha, telefone, endereco) VALUES
                                                      ('Pedro Gregorio', 'pedrogregorio@email.com', '$2a$10$5zpYsJI1T25DaV0OgDRm3uq6uZPW7cqktOOCAA62NYI67dVXHQatK', '11911111111', 'rua pedro gregorio, 100'),
                                                      ('Arthur Cavalcante', 'arthurcavalcante@email.com', '$2a$10$Fj.tWVcnR4s3dinlW5bOqOPTwoBYb/aC69IBCC56g2nbVAqysV6ci', '11922222222', 'rua arthur cavalcante, 100'),
                                                      ('André Luiz', 'andreluiz@email.com', '$2a$10$1Ge/tl54FXCzfpFCYe2cL.zeDCRy4KMNE7cWb3cQyLRIgQ6HCQrJ.', '11933333333', 'rua andre luiz, 100');

INSERT INTO tecnicos (nome, email, senha, especialidade) VALUES
                                                                  ('Pedro Gregorio', 'pedrogregorio@tecnico.com', '$2a$10$5zpYsJI1T25DaV0OgDRm3uq6uZPW7cqktOOCAA62NYI67dVXHQatK', 'software, hardware'),
                                                                  ('Arthur Cavalcante', 'arthurcavalcante@tecnico.com', '$2a$10$Fj.tWVcnR4s3dinlW5bOqOPTwoBYb/aC69IBCC56g2nbVAqysV6ci', 'software'),
                                                                  ('André Luiz', 'andreluiz@tecnico.com', '$2a$10$1Ge/tl54FXCzfpFCYe2cL.zeDCRy4KMNE7cWb3cQyLRIgQ6HCQrJ.', 'hardware');
