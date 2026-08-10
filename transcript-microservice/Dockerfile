# -------- transcript-microservice (FastAPI + openai-whisper) --------
FROM python:3.11-slim

# whisper shells out to the ffmpeg binary internally to decode audio,
# even for .wav input -- same package video-service/chat-service install.
RUN apt-get update && \
    apt-get install -y --no-install-recommends ffmpeg && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY requirements.txt .
# Upgrade build tooling first -- openai-whisper's setup.py needs
# pkg_resources, which recent pip/setuptools no longer bundle by default
# in this base image. Without this line the build fails with:
#   ModuleNotFoundError: No module named 'pkg_resources'
RUN pip install --no-cache-dir --upgrade pip setuptools wheel
RUN pip install --no-cache-dir -r requirements.txt

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