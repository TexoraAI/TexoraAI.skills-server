# -------- transcript-microservice (FastAPI + openai-whisper) --------
FROM python:3.11-slim

# whisper shells out to the ffmpeg binary internally to decode audio,
# even for .wav input -- same package video-service/chat-service install.
RUN apt-get update && \
    apt-get install -y --no-install-recommends ffmpeg && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY requirements.txt .

# openai-whisper's setup.py needs pkg_resources, which PyPI's setuptools
# removed entirely starting at v81. Two things are required together:
#   1. Pin setuptools below 81 so pkg_resources still exists.
#   2. --no-build-isolation, because pip otherwise builds openai-whisper
#      in a throwaway isolated env that fetches the LATEST setuptools
#      from PyPI regardless of what's installed globally -- so just
#      upgrading/pinning setuptools here does nothing without this flag.
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir "setuptools<81" wheel

# Install everything -- including torch -- in ONE pip resolve, with the
# CPU-only wheel index as the PRIMARY source and PyPI as fallback for
# packages that aren't on the PyTorch index (fastapi, tiktoken, etc).
#
# This MUST be a single command, not "install torch first, then -r
# requirements.txt" as two separate pip calls. openai-whisper's dependency
# spec just says "torch, any version" with no index pin -- so a second,
# separate pip invocation with no --index-url will happily re-resolve
# torch against plain PyPI to "satisfy" whisper, and PyPI's default Linux
# wheel for torch is the GPU/CUDA build. That silently re-drags in ~5GB of
# nvidia-*-cu12 packages and is what filled the disk both previous times
# ("no space left on device" on libcusparseLt, then again on triton).
#
# Keeping it one resolve means pip only ever sees the CPU build of torch
# as a candidate, so it can't "upgrade" its way back to CUDA.
RUN pip install --no-cache-dir --no-build-isolation \
    --index-url https://download.pytorch.org/whl/cpu \
    --extra-index-url https://pypi.org/simple \
    -r requirements.txt

# Model name used both to bake the model into the image at build time
# (so prod containers never pay a download cost on first request) and
# as the runtime default. Override at build time with:
#   docker build --build-arg WHISPER_MODEL=base -t ... .
ARG WHISPER_MODEL=tiny
ENV WHISPER_MODEL=${WHISPER_MODEL}

RUN python -c "import whisper; whisper.load_model('${WHISPER_MODEL}')"

COPY transcribe_server.py .

EXPOSE 5001

CMD ["python", "transcribe_server.py"]