ALTER TABLE movimentacao ADD COLUMN data_vencimento TEXT;
ALTER TABLE movimentacao ADD COLUMN recorrente INTEGER NOT NULL DEFAULT 0;
ALTER TABLE movimentacao ADD COLUMN conta_recorrente_id INTEGER;
ALTER TABLE movimentacao ADD COLUMN cartao_id INTEGER;
ALTER TABLE movimentacao ADD COLUMN parcelamento_id INTEGER;
ALTER TABLE movimentacao ADD COLUMN numero_parcela INTEGER;

CREATE TABLE IF NOT EXISTS conta_recorrente (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    descricao TEXT NOT NULL,
    tipo TEXT NOT NULL,
    valor NUMERIC NOT NULL,
    categoria_id INTEGER,
    frequencia TEXT NOT NULL,
    intervalo_dias INTEGER,
    proxima_ocorrencia TEXT NOT NULL,
    ativa INTEGER NOT NULL DEFAULT 1,
    observacoes TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (categoria_id) REFERENCES categoria(id)
);

CREATE TABLE IF NOT EXISTS cartao (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    limite NUMERIC NOT NULL,
    dia_fechamento INTEGER NOT NULL,
    dia_vencimento INTEGER NOT NULL,
    ativo INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS parcelamento (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    cartao_id INTEGER,
    descricao TEXT NOT NULL,
    valor_total NUMERIC NOT NULL,
    quantidade_parcelas INTEGER NOT NULL,
    valor_parcela NUMERIC NOT NULL,
    parcelas_pagas INTEGER NOT NULL DEFAULT 0,
    data_inicio TEXT NOT NULL,
    categoria_id INTEGER,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (cartao_id) REFERENCES cartao(id),
    FOREIGN KEY (categoria_id) REFERENCES categoria(id)
);

CREATE TABLE IF NOT EXISTS meta (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    descricao TEXT,
    valor_alvo NUMERIC NOT NULL,
    valor_atual NUMERIC NOT NULL DEFAULT 0,
    data_limite TEXT,
    ativa INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS backup_historico (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    arquivo TEXT NOT NULL,
    origem TEXT NOT NULL,
    created_at TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_movimentacao_tipo ON movimentacao(tipo);
CREATE INDEX IF NOT EXISTS idx_movimentacao_status ON movimentacao(status);
CREATE INDEX IF NOT EXISTS idx_conta_recorrente_proxima ON conta_recorrente(proxima_ocorrencia);
CREATE INDEX IF NOT EXISTS idx_cartao_nome ON cartao(nome);
CREATE INDEX IF NOT EXISTS idx_meta_nome ON meta(nome);
