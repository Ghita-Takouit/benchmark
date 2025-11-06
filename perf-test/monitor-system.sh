 #!/bin/bash

# Script de surveillance des métriques système pendant les tests de performance
# Collecte CPU, mémoire, GC, connexions Hikari, etc.

set -e

# Configuration
MONITOR_INTERVAL=5  # Intervalle en secondes
OUTPUT_DIR="./monitoring"
APP_PORT=8080
JMX_PORT=9999  # Port JMX de votre application Spring Boot

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() {
    echo -e "${BLUE}[MONITOR]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Fonction pour vérifier les dépendances
check_dependencies() {
    local missing_deps=()

    # Vérifier les outils système
    for tool in jstat jcmd curl ps; do
        if ! command -v $tool &> /dev/null; then
            missing_deps+=($tool)
        fi
    done

    if [ ${#missing_deps[@]} -ne 0 ]; then
        log_error "Outils manquants: ${missing_deps[*]}"
        log_error "Installez les outils Java (JDK) pour jstat et jcmd"
        exit 1
    fi
}

# Fonction pour détecter le PID de l'application
detect_app_pid() {
    local pid=$(ps aux | grep java | grep -v grep | grep "$APP_PORT\|spring-boot" | awk '{print $2}' | head -n1)

    if [ -z "$pid" ]; then
        log_error "Impossible de détecter le PID de l'application Java"
        log_error "Assurez-vous que votre application Spring Boot est démarrée"
        exit 1
    fi

    echo $pid
}

# Fonction pour collecter les métriques CPU et mémoire système
collect_system_metrics() {
    local timestamp=$(date +"%Y-%m-%d %H:%M:%S")
    local output_file="$OUTPUT_DIR/system_metrics.csv"

    # En-tête si fichier nouveau
    if [ ! -f "$output_file" ]; then
        echo "timestamp,cpu_percent,memory_used_mb,memory_free_mb,load_1min,load_5min,load_15min" > "$output_file"
    fi

    # Collecte des métriques via top et free
    local cpu_info=$(top -l 1 -n 0 | grep "CPU usage" | awk '{print $3}' | sed 's/%//')
    local memory_info=$(vm_stat | awk '
        /Pages free:/ { free = $3 * 4096 / 1024 / 1024 }
        /Pages active:/ { active = $3 * 4096 / 1024 / 1024 }
        /Pages inactive:/ { inactive = $3 * 4096 / 1024 / 1024 }
        /Pages wired down:/ { wired = $4 * 4096 / 1024 / 1024 }
        END {
            used = active + inactive + wired
            printf "%.0f,%.0f", used, free
        }
    ')
    local load_info=$(uptime | awk -F'load averages: ' '{print $2}' | sed 's/ /,/g')

    echo "$timestamp,$cpu_info,$memory_info,$load_info" >> "$output_file"
}

# Fonction pour collecter les métriques JVM
collect_jvm_metrics() {
    local pid=$1
    local timestamp=$(date +"%Y-%m-%d %H:%M:%S")
    local output_file="$OUTPUT_DIR/jvm_metrics.csv"

    # En-tête si fichier nouveau
    if [ ! -f "$output_file" ]; then
        echo "timestamp,heap_used_mb,heap_max_mb,heap_percent,non_heap_used_mb,gc_count,gc_time_ms,thread_count" > "$output_file"
    fi

    # Collecte des métriques JVM via jstat
    local gc_info=$(jstat -gc $pid | tail -n 1)
    local heap_info=$(jstat -gccapacity $pid | tail -n 1)

    if [ -n "$gc_info" ] && [ -n "$heap_info" ]; then
        # Parse jstat output (approximatif - ajustez selon votre version Java)
        local heap_used=$(echo $gc_info | awk '{print ($3+$4+$6+$8)/1024}')  # Eden + Survivor + Old Gen en MB
        local heap_max=$(echo $heap_info | awk '{print $10/1024}')  # Max heap en MB
        local heap_percent=$(echo "scale=2; $heap_used * 100 / $heap_max" | bc -l)

        # Informations sur les threads via jstack (compter les threads)
        local thread_count=$(jstack $pid 2>/dev/null | grep -c "^\".*\" " || echo "0")

        # GC info (simplifié)
        local gc_count=$(echo $gc_info | awk '{print $5+$7}')  # YGC + FGC
        local gc_time=$(echo $gc_info | awk '{print ($6+$8)*1000}')  # YGCT + FGCT en ms

        # Métaspace/Non-heap (approximatif)
        local non_heap_used=$(echo $gc_info | awk '{print $9/1024}')

        echo "$timestamp,$heap_used,$heap_max,$heap_percent,$non_heap_used,$gc_count,$gc_time,$thread_count" >> "$output_file"
    fi
}

# Fonction pour collecter les métriques de l'application via actuator
collect_app_metrics() {
    local timestamp=$(date +"%Y-%m-%d %H:%M:%S")
    local output_file="$OUTPUT_DIR/app_metrics.csv"

    # En-tête si fichier nouveau
    if [ ! -f "$output_file" ]; then
        echo "timestamp,hikari_active,hikari_idle,hikari_pending,hikari_max,http_requests_total,http_requests_per_sec" > "$output_file"
    fi

    # Collecte via Spring Boot Actuator
    local hikari_metrics=""
    local http_metrics=""

    # Tentative de récupération des métriques Hikari
    if curl -s "http://localhost:$APP_PORT/actuator/metrics/hikaricp.connections.active" &>/dev/null; then
        local hikari_active=$(curl -s "http://localhost:$APP_PORT/actuator/metrics/hikaricp.connections.active" | grep -o '"value":[0-9.]*' | cut -d: -f2 || echo "0")
        local hikari_idle=$(curl -s "http://localhost:$APP_PORT/actuator/metrics/hikaricp.connections.idle" | grep -o '"value":[0-9.]*' | cut -d: -f2 || echo "0")
        local hikari_pending=$(curl -s "http://localhost:$APP_PORT/actuator/metrics/hikaricp.connections.pending" | grep -o '"value":[0-9.]*' | cut -d: -f2 || echo "0")
        local hikari_max=$(curl -s "http://localhost:$APP_PORT/actuator/metrics/hikaricp.connections.max" | grep -o '"value":[0-9.]*' | cut -d: -f2 || echo "0")
        hikari_metrics="$hikari_active,$hikari_idle,$hikari_pending,$hikari_max"
    else
        hikari_metrics="0,0,0,0"
    fi

    # Métriques HTTP (si disponibles)
    if curl -s "http://localhost:$APP_PORT/actuator/metrics/http.server.requests" &>/dev/null; then
        local http_total=$(curl -s "http://localhost:$APP_PORT/actuator/metrics/http.server.requests" | grep -o '"value":[0-9.]*' | cut -d: -f2 || echo "0")
        # Calcul approximatif du taux (nécessiterait un historique)
        http_metrics="$http_total,0"
    else
        http_metrics="0,0"
    fi

    echo "$timestamp,$hikari_metrics,$http_metrics" >> "$output_file"
}

# Fonction principale de surveillance
monitor_system() {
    local duration=${1:-3600}  # Durée en secondes (par défaut 1h)
    local app_pid=$(detect_app_pid)

    log_info "Démarrage de la surveillance (PID: $app_pid, Durée: ${duration}s)"
    log_info "Intervalle: ${MONITOR_INTERVAL}s"
    log_info "Dossier de sortie: $OUTPUT_DIR"

    local end_time=$(($(date +%s) + duration))
    local iteration=0

    while [ $(date +%s) -lt $end_time ]; do
        iteration=$((iteration + 1))

        # Collecte des métriques
        collect_system_metrics
        collect_jvm_metrics $app_pid
        collect_app_metrics

        # Affichage périodique
        if [ $((iteration % 12)) -eq 0 ]; then  # Toutes les minutes (12 * 5s)
            log_info "Surveillance en cours... ($(date +"%H:%M:%S"))"
        fi

        sleep $MONITOR_INTERVAL
    done

    log_info "Surveillance terminée"
}

# Fonction pour générer un rapport de surveillance
generate_report() {
    local report_file="$OUTPUT_DIR/monitoring_report.html"

    log_info "Génération du rapport de surveillance..."

    cat > "$report_file" << 'EOF'
<!DOCTYPE html>
<html>
<head>
    <title>Rapport de Surveillance Performance</title>
    <script src="https://cdn.plot.ly/plotly-latest.min.js"></script>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .metric-section { margin: 30px 0; }
        .chart { width: 100%; height: 400px; }
    </style>
</head>
<body>
    <h1>Rapport de Surveillance Performance</h1>
    <p>Généré le: <span id="timestamp"></span></p>

    <div class="metric-section">
        <h2>Métriques Système</h2>
        <div id="system-chart" class="chart"></div>
    </div>

    <div class="metric-section">
        <h2>Métriques JVM</h2>
        <div id="jvm-chart" class="chart"></div>
    </div>

    <div class="metric-section">
        <h2>Connexions Hikari</h2>
        <div id="hikari-chart" class="chart"></div>
    </div>

    <script>
        document.getElementById('timestamp').textContent = new Date().toLocaleString();

        // Ici vous pouvez ajouter du JavaScript pour charger et afficher les données CSV
        // avec Plotly.js pour créer des graphiques interactifs

        console.log('Rapport généré - Ajoutez le code JavaScript pour les graphiques');
    </script>
</body>
</html>
EOF

    log_info "Rapport généré: $report_file"
}

# Fonction principale
main() {
    log_info "====== SURVEILLANCE PERFORMANCE ======"

    # Créer le dossier de sortie
    mkdir -p "$OUTPUT_DIR"

    # Vérifications
    check_dependencies

    case "${1:-monitor}" in
        "monitor")
            local duration=${2:-3600}
            monitor_system $duration
            ;;
        "report")
            generate_report
            ;;
        "help"|"-h"|"--help")
            echo "Usage: $0 [monitor|report|help] [duration_seconds]"
            echo "  monitor [duration] - Lance la surveillance (défaut: 3600s = 1h)"
            echo "  report            - Génère un rapport HTML"
            echo "  help              - Affiche cette aide"
            exit 0
            ;;
        *)
            log_error "Commande inconnue: $1"
            echo "Utilisez '$0 help' pour l'aide"
            exit 1
            ;;
    esac
}

# Gestion des signaux
trap 'log_info "Surveillance interrompue"; exit 0' INT TERM

# Exécution
main "$@"
