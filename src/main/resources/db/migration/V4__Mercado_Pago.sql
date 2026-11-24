-- =====================================================================
-- V4: pagamentos reais pelo Mercado Pago
--  meio = como o cliente escolheu pagar: 'pix' (QR code gerado pela API) ou 'checkout'
--         (Checkout Pro: página do Mercado Pago, cartão de crédito/débito)
--  forma (já existia) = como foi pago de fato: pix | credito | debito (preenchida na aprovação)
-- =====================================================================
ALTER TABLE pagamentos ADD COLUMN meio VARCHAR(20) CHECK (meio IN ('pix', 'checkout'));
ALTER TABLE pagamentos ADD COLUMN mp_pagamento_id VARCHAR(40);
ALTER TABLE pagamentos ADD COLUMN mp_preferencia_id VARCHAR(100);
ALTER TABLE pagamentos ADD COLUMN link_pagamento VARCHAR(500);
ALTER TABLE pagamentos ADD COLUMN pix_expira_em TIMESTAMP;
ALTER TABLE pagamentos ADD COLUMN mp_status VARCHAR(40);
ALTER TABLE pagamentos ADD COLUMN mp_status_detalhe VARCHAR(100);
-- O "copia e cola" do Pix de verdade passa de 255 caracteres
ALTER TABLE pagamentos ALTER COLUMN codigo_pix SET DATA TYPE VARCHAR(1000);
ALTER TABLE pagamentos ALTER COLUMN bandeira SET DATA TYPE VARCHAR(40);

-- Pagamentos antigos (simulados) ficam marcados com o meio que usaram
UPDATE pagamentos SET meio = 'pix' WHERE forma = 'pix';
UPDATE pagamentos SET meio = 'checkout' WHERE forma IN ('credito', 'debito');

CREATE INDEX idx_pagamentos_mp ON pagamentos(mp_pagamento_id);
