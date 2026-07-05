# Whisper.cpp
[Whisper Github Project](https://github.com/ggml-org/whisper.cpp)

**Download models**
```
docker run -it --rm -v ./models:/models ghcr.io/ggml-org/whisper.cpp:main-cuda "./models/download-ggml-model.sh base /models"
```

**Transcribe using terminal**
```
docker run -it --rm -v ./models:/models -v /home/leonardo/Temp:/audios ghcr.io/ggml-org/whisper.cpp:main-cuda "whisper-cli -m /models/ggml-base.bin -f /audios/past\ history.mp3"
```

**Open Web Solution**
```
docker run -it --rm -p "8080:8080" -v ./models:/models -v /home/leonardo/Temp:/audios ghcr.io/ggml-org/whisper.cpp:main "whisper-server --host 0.0.0.0 -m /models/ggml-base.bin"
```

Call Inference API
```
curl 127.0.0.1:8080/inference \
-H "Content-Type: multipart/form-data" \
-F file="@/home/leonardo/Temp/past history.mp3" \
-F temperature="0.0" \
-F temperature_inc="0.2" \
-F response_format="json"
```

## How the backend uses Whisper

When the frontend sends a recording to `POST /api/v1/conversations`, the backend:

1. Stores the uploaded audio in `infra/audios/` (mounted at `/app/audios` in the backend container).
2. Calls the Whisper `/inference` endpoint to transcribe the audio.
3. Persists the transcript, topic, and feedback items in PostgreSQL.

The audio storage is behind an `AudioStoragePort` so a cloud object-storage backend (e.g., S3) can be swapped in later without changing the domain or application code.

## Run the full OpenConversation stack with Docker Compose

This starts PostgreSQL, Whisper, the Kotlin/Spring Boot backend, and the Vite/React webapp.

**1. Configure environment variables**
```
cp infra/.env.example infra/.env
```
Edit `infra/.env` and set a secure `POSTGRES_PASSWORD`.

**2. Build the images**
```
docker compose -f infra/docker-compose.yml build
```

**3. Start all services**
```
docker compose -f infra/docker-compose.yml up
```

**4. Open the webapp**
Open http://localhost:5173 in your browser.

**5. Stop the stack**
```
docker compose -f infra/docker-compose.yml down
```

**Notes**
- The backend is exposed on http://localhost:8081.
- The Whisper server is exposed on http://localhost:8080.
- PostgreSQL is exposed on port 5432.
- Uploaded audio recordings are persisted on the host under `infra/audios/`.
- On Linux/macOS, ensure the container user can write to `infra/audios/` before starting the stack:
  ```
  chmod 775 infra/audios
  ```
  If uploads fail with an internal server error, the directory is likely not writable by the backend container.
