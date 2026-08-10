"""
Persistent whisper transcription microservice.
"""

import os
import tempfile
import time
import traceback

import whisper
from fastapi import FastAPI, File, UploadFile
from fastapi.responses import JSONResponse

MODEL_NAME = os.environ.get("WHISPER_MODEL", "tiny")

app = FastAPI(title="Whisper Transcription Microservice")

print(f"[transcribe_server] Loading whisper model '{MODEL_NAME}' — this happens ONCE at startup...")
_load_start = time.time()
model = whisper.load_model(MODEL_NAME)
print(f"[transcribe_server] Model loaded in {time.time() - _load_start:.1f}s. Ready for requests.")


@app.get("/health")
def health():
    return {"status": "ok", "model": MODEL_NAME}


@app.post("/transcribe")
async def transcribe(file: UploadFile = File(...)):
    tmp_path = None
    try:
        suffix = os.path.splitext(file.filename or "")[1] or ".wav"
        with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as tmp:
            tmp.write(await file.read())
            tmp_path = tmp.name

        start = time.time()
        result = model.transcribe(tmp_path)
        elapsed = time.time() - start
        print(f"[transcribe_server] Transcribed '{file.filename}' in {elapsed:.1f}s")

        return JSONResponse(content={
            "language": result.get("language"),
            "segments": [
                {
                    "start": seg["start"],
                    "end": seg["end"],
                    "text": seg["text"],
                }
                for seg in result.get("segments", [])
            ],
        })
    except Exception as e:
        traceback.print_exc()
        return JSONResponse(status_code=500, content={"error": str(e)})
    finally:
        if tmp_path and os.path.exists(tmp_path):
            try:
                os.remove(tmp_path)
            except OSError:
                pass


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5001, workers=1)