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
