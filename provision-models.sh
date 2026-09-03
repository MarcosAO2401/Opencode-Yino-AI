#!/bin/bash
# Script de aprovisionamiento automático de modelos para Yino-AI

# Nueva ruta accesible
TARGET_DIR="/sdcard/Download/YinoAI"
mkdir -p "$TARGET_DIR"

echo "=== Jarvisificación de Yino-AI: Auto-Instalador de Cerebro ==="

# 1. Descargar Modelo GGUF (Phi-3 Mini Instruct)
MODEL_URL="https://huggingface.co/microsoft/Phi-3-mini-4k-instruct-gguf/resolve/main/Phi-3-mini-4k-instruct-q4.gguf"
echo "Descargando Cerebro (GGUF)..."
curl -L "$MODEL_URL" -o "$TARGET_DIR/gguf-model.gguf"

# 2. Descargar Modelo de Voz (Vosk Español - compacto)
VOSK_URL="https://alphacephei.com/vosk/models/vosk-model-small-es-0.42.zip"
echo "Descargando Voz (Vosk)..."
curl -L "$VOSK_URL" -o "$TARGET_DIR/vosk.zip"
unzip -o "$TARGET_DIR/vosk.zip" -d "$TARGET_DIR/"
mv "$TARGET_DIR/vosk-model-small-es-0.42" "$TARGET_DIR/vosk-model"
rm "$TARGET_DIR/vosk.zip"

echo "=== Instalación completa en $TARGET_DIR ==="
echo "Yino-AI ya está configurado para buscar los modelos en esta carpeta."
