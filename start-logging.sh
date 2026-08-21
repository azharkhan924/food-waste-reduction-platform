#!/bin/bash
# ============================================
# Loki + Grafana Logging Stack — Start/Stop Script
# ============================================
# Usage:
#   ./start-logging.sh start    — Start all services
#   ./start-logging.sh stop     — Stop all services
#   ./start-logging.sh status   — Check if services are running

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
LOKI_CONFIG="$PROJECT_DIR/loki/loki-config.yml"
PROMTAIL_CONFIG="$PROJECT_DIR/promtail/promtail-config.yml"
LOKI_PID_FILE="/tmp/loki-foodwaste.pid"
PROMTAIL_PID_FILE="/tmp/promtail-foodwaste.pid"

start_services() {
    echo "🚀 Starting Loki + Grafana logging stack..."
    echo ""

    # Create logs directory if it doesn't exist
    mkdir -p "$PROJECT_DIR/logs"

    # Start Loki in background
    if [ -f "$LOKI_PID_FILE" ] && kill -0 $(cat "$LOKI_PID_FILE") 2>/dev/null; then
        echo "⚠️  Loki is already running (PID: $(cat $LOKI_PID_FILE))"
    else
        echo "📦 Starting Loki on http://localhost:3100 ..."
        loki -config.file="$LOKI_CONFIG" > /tmp/loki-foodwaste.log 2>&1 &
        echo $! > "$LOKI_PID_FILE"
        echo "✅ Loki started (PID: $(cat $LOKI_PID_FILE))"
    fi

    # Wait a moment for Loki to be ready
    sleep 2

    # Start Promtail in background
    if [ -f "$PROMTAIL_PID_FILE" ] && kill -0 $(cat "$PROMTAIL_PID_FILE") 2>/dev/null; then
        echo "⚠️  Promtail is already running (PID: $(cat $PROMTAIL_PID_FILE))"
    else
        echo "📡 Starting Promtail..."
        promtail -config.file="$PROMTAIL_CONFIG" > /tmp/promtail-foodwaste.log 2>&1 &
        echo $! > "$PROMTAIL_PID_FILE"
        echo "✅ Promtail started (PID: $(cat $PROMTAIL_PID_FILE))"
    fi

    # Start Grafana via brew services
    echo "📊 Starting Grafana on http://localhost:3000 ..."
    brew services start grafana 2>/dev/null
    echo "✅ Grafana started"

    echo ""
    echo "=========================================="
    echo "  All services are running!"
    echo "=========================================="
    echo ""
    echo "  📊 Grafana:  http://localhost:3000"
    echo "     Login:    admin / admin"
    echo ""
    echo "  📦 Loki:     http://localhost:3100"
    echo ""
    echo "  To view logs in Grafana:"
    echo "  1. Open http://localhost:3000"
    echo "  2. Go to Explore (compass icon)"
    echo "  3. Select 'Loki' data source"
    echo "  4. Enter: {job=\"foodwaste\"}"
    echo "  5. Click 'Run query'"
    echo "=========================================="
}

stop_services() {
    echo "🛑 Stopping Loki + Grafana logging stack..."
    echo ""

    # Stop Loki
    if [ -f "$LOKI_PID_FILE" ]; then
        PID=$(cat "$LOKI_PID_FILE")
        if kill -0 "$PID" 2>/dev/null; then
            kill "$PID"
            echo "✅ Loki stopped (PID: $PID)"
        else
            echo "⚠️  Loki was not running"
        fi
        rm -f "$LOKI_PID_FILE"
    else
        echo "⚠️  Loki PID file not found"
    fi

    # Stop Promtail
    if [ -f "$PROMTAIL_PID_FILE" ]; then
        PID=$(cat "$PROMTAIL_PID_FILE")
        if kill -0 "$PID" 2>/dev/null; then
            kill "$PID"
            echo "✅ Promtail stopped (PID: $PID)"
        else
            echo "⚠️  Promtail was not running"
        fi
        rm -f "$PROMTAIL_PID_FILE"
    else
        echo "⚠️  Promtail PID file not found"
    fi

    # Stop Grafana
    brew services stop grafana 2>/dev/null
    echo "✅ Grafana stopped"

    echo ""
    echo "All services stopped."
}

check_status() {
    echo "📋 Service Status:"
    echo ""

    # Check Loki
    if [ -f "$LOKI_PID_FILE" ] && kill -0 $(cat "$LOKI_PID_FILE") 2>/dev/null; then
        echo "  📦 Loki:     ✅ Running (PID: $(cat $LOKI_PID_FILE)) — http://localhost:3100"
    else
        echo "  📦 Loki:     ❌ Not running"
    fi

    # Check Promtail
    if [ -f "$PROMTAIL_PID_FILE" ] && kill -0 $(cat "$PROMTAIL_PID_FILE") 2>/dev/null; then
        echo "  📡 Promtail: ✅ Running (PID: $(cat $PROMTAIL_PID_FILE))"
    else
        echo "  📡 Promtail: ❌ Not running"
    fi

    # Check Grafana
    if brew services list 2>/dev/null | grep grafana | grep -q started; then
        echo "  📊 Grafana:  ✅ Running — http://localhost:3000"
    else
        echo "  📊 Grafana:  ❌ Not running"
    fi

    echo ""
}

# Handle command
case "$1" in
    start)
        start_services
        ;;
    stop)
        stop_services
        ;;
    status)
        check_status
        ;;
    *)
        echo "Usage: $0 {start|stop|status}"
        echo ""
        echo "  start   — Start Loki, Promtail, and Grafana"
        echo "  stop    — Stop all services"
        echo "  status  — Check if services are running"
        exit 1
        ;;
esac
