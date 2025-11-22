#!/bin/bash

# Script para criar tópicos Kafka com configurações específicas
# Aguarda o Kafka estar pronto e cria os tópicos se não existirem

set -e

echo "==========================================="
echo "Kafka Topics Initializer"
echo "==========================================="

# Aguarda o Kafka estar disponível
echo "Aguardando Kafka estar pronto..."
until kafka-broker-api-versions --bootstrap-server kafka:9092 > /dev/null 2>&1; do
    echo "Kafka não está pronto ainda... aguardando 3s"
    sleep 3
done

echo "✓ Kafka está pronto!"
echo ""

# Configurações dos tópicos
# Format: "topic_name:partitions:replication_factor:retention_hours"
TOPICS=(
    "notification.created:${NOTIFICATION_CREATED_PARTITIONS:-40}:1:${NOTIFICATION_CREATED_RETENTION:-168}"
    "notification.sent:${NOTIFICATION_SENT_PARTITIONS:-10}:1:${NOTIFICATION_SENT_RETENTION:-72}"
    "notification.failed:${NOTIFICATION_FAILED_PARTITIONS:-10}:1:${NOTIFICATION_FAILED_RETENTION:-168}"
)

echo "==========================================="
echo "Criando/Verificando Tópicos"
echo "==========================================="
echo ""

for topic_config in "${TOPICS[@]}"; do
    IFS=':' read -r topic partitions replication retention <<< "$topic_config"

    echo "Processando: $topic"
    echo "  - Partições: $partitions"
    echo "  - Replicação: $replication"
    echo "  - Retenção: ${retention}h"

    # Verifica se o tópico já existe
    if kafka-topics --bootstrap-server kafka:9092 --list 2>/dev/null | grep -q "^${topic}$"; then
        echo "  → Tópico já existe, verificando configuração..."

        # Pega número atual de partições
        current_partitions=$(kafka-topics --bootstrap-server kafka:9092 --describe --topic "$topic" 2>/dev/null | grep "PartitionCount" | awk '{print $4}' || echo "0")

        if [ "$current_partitions" -lt "$partitions" ]; then
            echo "  → Alterando partições de $current_partitions para $partitions..."
            kafka-topics --bootstrap-server kafka:9092 \
                --alter \
                --topic "$topic" \
                --partitions "$partitions" 2>/dev/null
            echo "  ✓ Partições atualizadas!"
        elif [ "$current_partitions" -gt "$partitions" ]; then
            echo "  ⚠ AVISO: Tópico tem $current_partitions partições (não é possível reduzir)"
        else
            echo "  ✓ Partições já configuradas corretamente"
        fi

        # Atualiza configurações de retenção
        kafka-configs --bootstrap-server kafka:9092 \
            --entity-type topics \
            --entity-name "$topic" \
            --alter \
            --add-config "retention.ms=$((retention * 3600000))" 2>/dev/null || true

    else
        echo "  → Criando tópico..."
        kafka-topics --bootstrap-server kafka:9092 \
            --create \
            --topic "$topic" \
            --partitions "$partitions" \
            --replication-factor "$replication" \
            --config "retention.ms=$((retention * 3600000))" 2>/dev/null
        echo "  ✓ Tópico criado com sucesso!"
    fi

    echo ""
done

echo "==========================================="
echo "Resumo dos Tópicos"
echo "==========================================="
echo ""

for topic_config in "${TOPICS[@]}"; do
    IFS=':' read -r topic _ _ _ <<< "$topic_config"
    kafka-topics --bootstrap-server kafka:9092 --describe --topic "$topic" 2>/dev/null | head -n 1
done

echo ""
echo "==========================================="
echo "✓ Inicialização dos tópicos concluída!"
echo "==========================================="

# Mantém o container rodando para logs (opcional - pode remover)
# tail -f /dev/null

