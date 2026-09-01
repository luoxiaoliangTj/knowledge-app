# Knowledge App - 手机文件浏览器 + AI 知识库
# 安装依赖: pip install fastapi uvicorn python-multipart aiofiles
# 启动: uvicorn main:app --host 0.0.0.0 --port 8000

import os
import json
import sqlite3
import hashlib
from pathlib import Path
from datetime import datetime
from typing import Optional, List

from fastapi import FastAPI, HTTPException, Query
from fastapi.staticfiles import StaticFiles
from fastapi.responses import HTMLResponse, JSONResponse
from pydantic import BaseModel
import uvicorn

# ===== 配置 =====
BASE_DIR = Path(__file__).parent
DB_PATH = BASE_DIR / "knowledge.db"
STATIC_DIR = BASE_DIR.parent / "frontend"

# 手机存储根目录
STORAGE_ROOTS = [
    Path("/sdcard"),
    Path("/storage/emulated/0"),
    Path.home() / "storage" / "shared",
]

# 支持的文件类型
SUPPORTED_EXT = {
    ".pdf": "pdf", ".epub": "epub", ".mobi": "ebook", ".azw3": "ebook",
    ".txt": "text", ".md": "markdown", ".docx": "doc", ".doc": "doc",
    ".pptx": "ppt", ".ppt": "ppt", ".xlsx": "excel", ".xls": "excel",
    ".py": "code", ".js": "code", ".html": "code", ".css": "code",
    ".json": "code", ".xml": "code", ".csv": "data",
}

# ===== 数据库 =====
def get_db():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn

def init_db():
    conn = get_db()
    conn.executescript("""
        CREATE TABLE IF NOT EXISTS files (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            path TEXT UNIQUE NOT NULL,
            name TEXT NOT NULL,
            ext TEXT,
            size INTEGER,
            modified TEXT,
            category TEXT DEFAULT '未分类',
            tags TEXT DEFAULT '[]',
            summary TEXT DEFAULT '',
            content_hash TEXT,
            indexed_at TEXT,
            created_at TEXT DEFAULT CURRENT_TIMESTAMP
        );
        
        CREATE TABLE IF NOT EXISTS knowledge (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            file_id INTEGER,
            content TEXT,
            embedding TEXT,
            created_at TEXT DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY(file_id) REFERENCES files(id)
        );
        
        CREATE TABLE IF NOT EXISTS categories (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT UNIQUE NOT NULL,
            description TEXT DEFAULT '',
            created_at TEXT DEFAULT CURRENT_TIMESTAMP
        );
        
        CREATE VIRTUAL TABLE IF NOT EXISTS file_search USING fts5(
            path, name, summary, tags, content='files', content_rowid='id'
        );
    """)
    conn.commit()
    conn.close()

init_db()

# ===== FastAPI App =====
app = FastAPI(title="Knowledge App", version="1.0.0")

# ===== API: 文件浏览 =====

@app.get("/api/browse")
def browse(path: str = Query(default="/sdcard")):
    """浏览目录内容"""
    target = Path(path)
    if not target.exists():
        raise HTTPException(404, "路径不存在")
    if not target.is_dir():
        raise HTTPException(400, "不是目录")
    
    items = []
    try:
        for item in sorted(target.iterdir(), key=lambda x: (not x.is_dir(), x.name.lower())):
            try:
                stat = item.stat()
                is_dir = item.is_dir()
                ext = item.suffix.lower() if not is_dir else ""
                items.append({
                    "name": item.name,
                    "path": str(item),
                    "is_dir": is_dir,
                    "size": stat.st_size if not is_dir else 0,
                    "modified": datetime.fromtimestamp(stat.st_mtime).strftime("%Y-%m-%d %H:%M"),
                    "type": "folder" if is_dir else SUPPORTED_EXT.get(ext, "unknown"),
                    "ext": ext,
                })
            except (PermissionError, OSError):
                continue
    except PermissionError:
        raise HTTPException(403, "无权限访问该目录")
    
    return {"path": str(target), "parent": str(target.parent), "items": items}

@app.get("/api/file")
def read_file(path: str = Query(...)):
    """读取文件内容（文本类）"""
    target = Path(path)
    if not target.exists() or not target.is_file():
        raise HTTPException(404, "文件不存在")
    
    ext = target.suffix.lower()
    if ext not in [".txt", ".md", ".py", ".js", ".html", ".css", ".json", ".xml", ".csv", ".log"]:
        raise HTTPException(400, f"不支持的文件类型: {ext}")
    
    try:
        content = target.read_text(encoding="utf-8", errors="ignore")
        return {"path": str(target), "name": target.name, "content": content[:50000]}
    except Exception as e:
        raise HTTPException(500, str(e))

# ===== API: 知识库索引 =====

@app.post("/api/index")
def index_directory(path: str = Query(...)):
    """扫描目录并建立索引"""
    target = Path(path)
    if not target.exists() or not target.is_dir():
        raise HTTPException(404, "路径不存在")
    
    conn = get_db()
    count = 0
    errors = []
    
    for root, dirs, files in os.walk(target):
        # 跳过隐藏目录
        dirs[:] = [d for d in dirs if not d.startswith('.')]
        
        for fname in files:
            fpath = Path(root) / fname
            ext = fpath.suffix.lower()
            
            if ext not in SUPPORTED_EXT:
                continue
            
            try:
                stat = fpath.stat()
                content_hash = hashlib.md5(fpath.read_bytes()[:8192]).hexdigest()
                
                # 检查是否已索引且未变化
                existing = conn.execute(
                    "SELECT id FROM files WHERE path=? AND content_hash=?",
                    (str(fpath), content_hash)
                ).fetchone()
                
                if existing:
                    continue
                
                # 提取文本内容（简单版）
                text_content = ""
                if ext in [".txt", ".md", ".py", ".js", ".html", ".css", ".json", ".xml", ".csv", ".log"]:
                    text_content = fpath.read_text(encoding="utf-8", errors="ignore")[:10000]
                
                conn.execute("""
                    INSERT OR REPLACE INTO files (path, name, ext, size, modified, content_hash, indexed_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """, (
                    str(fpath), fname, ext, stat.st_size,
                    datetime.fromtimestamp(stat.st_mtime).strftime("%Y-%m-%d %H:%M"),
                    content_hash, datetime.now().isoformat()
                ))
                count += 1
                
            except Exception as e:
                errors.append(f"{fname}: {str(e)[:100]}")
    
    conn.commit()
    conn.close()
    
    return {"indexed": count, "errors": errors[:10]}

@app.get("/api/files")
def list_files(
    category: Optional[str] = None,
    tag: Optional[str] = None,
    search: Optional[str] = None,
    limit: int = 50,
    offset: int = 0,
):
    """列出已索引的文件"""
    conn = get_db()
    query = "SELECT * FROM files WHERE 1=1"
    params = []
    
    if category:
        query += " AND category=?"
        params.append(category)
    if tag:
        query += " AND tags LIKE ?"
        params.append(f"%{tag}%")
    if search:
        query += " AND (name LIKE ? OR summary LIKE ? OR path LIKE ?)"
        params.extend([f"%{search}%"] * 3)
    
    query += " ORDER BY modified DESC LIMIT ? OFFSET ?"
    params.extend([limit, offset])
    
    rows = conn.execute(query, params).fetchall()
    total = conn.execute("SELECT COUNT(*) FROM files").fetchone()[0]
    
    files = [dict(r) for r in rows]
    conn.close()
    
    return {"files": files, "total": total, "limit": limit, "offset": offset}

# ===== API: AI 功能 =====

class AIConfig(BaseModel):
    provider: str = "remote"  # "remote" 或 "local"
    api_key: Optional[str] = None
    base_url: Optional[str] = None
    model: Optional[str] = None

# ===== API: 设置 =====

SETTINGS_FILE = BASE_DIR / "settings.json"

def load_settings():
    if SETTINGS_FILE.exists():
        return json.loads(SETTINGS_FILE.read_text(encoding="utf-8"))
    return {
        "ai_provider": "remote",
        "ai_api_key": "",
        "ai_base_url": "https://apihub.agnes-ai.com/v1",
        "ai_model": "agnes-2.0-flash",
    }

def save_settings(settings):
    SETTINGS_FILE.write_text(json.dumps(settings, ensure_ascii=False, indent=2), encoding="utf-8")

@app.get("/api/settings")
def get_settings():
    return load_settings()

@app.put("/api/settings")
def update_settings(settings: dict):
    save_settings(settings)
    # 更新全局配置
    global DEFAULT_REMOTE_CONFIG
    if settings.get("ai_api_key"):
        DEFAULT_REMOTE_CONFIG["api_key"] = settings["ai_api_key"]
        DEFAULT_REMOTE_CONFIG["base_url"] = settings.get("ai_base_url", "https://apihub.agnes-ai.com/v1")
        DEFAULT_REMOTE_CONFIG["model"] = settings.get("ai_model", "agnes-2.0-flash")
    return {"success": True}

class AIRequest(BaseModel):
    prompt: str
    context: Optional[str] = None
    model: Optional[str] = None
    api_key: Optional[str] = None
    base_url: Optional[str] = None

# 默认远端配置（Agnes AI 或 OpenAI 兼容）
DEFAULT_REMOTE_CONFIG = {
    "base_url": "https://apihub.agnes-ai.com/v1",
    "model": "agnes-2.0-flash",
    "api_key": "",  # 用户自行配置
}

# 默认本地配置
DEFAULT_LOCAL_CONFIG = {
    "base_url": "http://localhost:11434/v1",
    "model": "qwen2.5:1.5b",
    "api_key": "ollama-local",
}

def get_ai_config(req: AIRequest) -> dict:
    """获取 AI 配置：优先用请求中的，否则用默认远端"""
    if req.api_key and req.base_url:
        return {
            "base_url": req.base_url,
            "model": req.model or "agnes-2.0-flash",
            "api_key": req.api_key,
        }
    # 默认用远端
    return DEFAULT_REMOTE_CONFIG

@app.post("/api/ai/chat")
def ai_chat(req: AIRequest):
    """调用 AI（默认远端，可指定本地）"""
    try:
        import requests
        
        config = get_ai_config(req)
        system_prompt = """你是一个知识库助手。帮助用户整理、搜索、总结他们的知识资料。
回答要简洁、有条理。如果用户问的是关于他们资料的问题，先搜索再回答。"""
        
        messages = [{"role": "system", "content": system_prompt}]
        if req.context:
            messages.append({"role": "user", "content": f"相关资料：{req.context[:2000]}"})
        messages.append({"role": "user", "content": req.prompt})
        
        headers = {
            "Authorization": f"Bearer {config['api_key']}",
            "Content-Type": "application/json",
        }
        
        resp = requests.post(
            f"{config['base_url'].rstrip('/')}/chat/completions",
            json={
                "model": config["model"],
                "messages": messages,
                "temperature": 0.7,
                "max_tokens": 2048,
            },
            headers=headers,
            timeout=120,
            verify=False,
        )
        
        if resp.status_code == 200:
            data = resp.json()
            reply = data["choices"][0]["message"]["content"]
            return {"reply": reply, "model": config["model"]}
        else:
            return {"reply": f"API 错误: {resp.status_code} - {resp.text[:200]}", "model": config["model"]}
            
    except requests.ConnectionError:
        return {"reply": "无法连接 AI 服务，请检查网络或配置", "model": "error"}
    except Exception as e:
        return {"reply": f"错误: {str(e)}", "model": "error"}

@app.post("/api/ai/summarize")
def ai_summarize(req: AIRequest):
    """总结文件内容"""
    full_prompt = f"请用中文总结以下内容的核心要点（3-5条），语言简洁：\n\n{req.context[:5000] if req.context else req.prompt}"
    return ai_chat(AIRequest(prompt=full_prompt, model=req.model))

@app.post("/api/ai/classify")
def ai_classify(req: AIRequest):
    """AI 自动分类"""
    full_prompt = f"根据以下文件路径和名称，给出1-3个分类标签（中文，简短），用逗号分隔：\n\n{req.context or req.prompt}"
    result = ai_chat(AIRequest(prompt=full_prompt, model=req.model))
    if result.get("reply"):
        tags = [t.strip() for t in result["reply"].replace("，", ",").split(",") if t.strip()]
        result["tags"] = tags[:5]
    return result

# ===== API: 分类管理 =====

@app.get("/api/categories")
def list_categories():
    conn = get_db()
    cats = conn.execute("SELECT * FROM categories ORDER BY name").fetchall()
    conn.close()
    return {"categories": [dict(c) for c in cats]}

@app.post("/api/categories")
def create_category(name: str, description: str = ""):
    conn = get_db()
    try:
        conn.execute("INSERT INTO categories (name, description) VALUES (?, ?)", (name, description))
        conn.commit()
    except sqlite3.IntegrityError:
        pass
    conn.close()
    return {"success": True}

@app.put("/api/files/{file_id}/category")
def set_category(file_id: int, category: str):
    conn = get_db()
    conn.execute("UPDATE files SET category=? WHERE id=?", (category, file_id))
    conn.commit()
    conn.close()
    return {"success": True}

# ===== 静态文件服务 =====

@app.get("/", response_class=HTMLResponse)
def index():
    return (STATIC_DIR / "index.html").read_text(encoding="utf-8")

app.mount("/static", StaticFiles(directory=STATIC_DIR), name="static")

# ===== 启动 =====
if __name__ == "__main__":
    print("🚀 Knowledge App 启动中...")
    print("📱 手机浏览器访问: http://localhost:8000")
    uvicorn.run(app, host="0.0.0.0", port=8000)
