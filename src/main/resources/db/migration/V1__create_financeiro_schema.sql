CREATE TABLE IF NOT EXISTS usuario (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    email TEXT,
    senha_hash TEXT,
    ativo INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS categoria (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    tipo TEXT NOT NULL,
    cor_hex TEXT,
    ativa INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS movimentacao (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tipo TEXT NOT NULL,
    descricao TEXT NOT NULL,
    valor NUMERIC NOT NULL,
    data_movimento TEXT NOT NULL,
    status TEXT NOT NULL,
    categoria_id INTEGER,
    usuario_id INTEGER NOT NULL,
    observacoes TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (categoria_id) REFERENCES categoria(id),
    FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

CREATE TABLE IF NOT EXISTS configuracao (
    chave TEXT PRIMARY KEY,
    valor TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_categoria_nome ON categoria(nome);
CREATE INDEX IF NOT EXISTS idx_movimentacao_data ON movimentacao(data_movimento);
CREATE INDEX IF NOT EXISTS idx_movimentacao_categoria_id ON movimentacao(categoria_id);
CREATE INDEX IF NOT EXISTS idx_movimentacao_usuario_id ON movimentacao(usuario_id);