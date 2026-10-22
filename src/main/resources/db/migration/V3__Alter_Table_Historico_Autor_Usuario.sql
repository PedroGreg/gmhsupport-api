ALTER TABLE historico_chamado ADD COLUMN id_usuario BIGINT NULL;
ALTER TABLE historico_chamado ADD CONSTRAINT fk_historico_usuario
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario);