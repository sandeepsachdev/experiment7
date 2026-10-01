(function () {
    const btn = document.getElementById('lt-start');
    const statusEl = document.getElementById('lt-status');
    const elapsedEl = document.getElementById('lt-elapsed');
    const workersEl = document.getElementById('lt-workers');
    const totalEl = document.getElementById('lt-total');
    const errorsEl = document.getElementById('lt-errors');
    const avgEl = document.getElementById('lt-avg');
    const canvas = document.getElementById('lt-chart');
    if (!btn || !canvas) return;

    const ctx = canvas.getContext('2d');
    const chart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [
                {
                    label: 'avg ms',
                    data: [],
                    borderColor: '#38bdf8',
                    backgroundColor: 'rgba(56,189,248,0.15)',
                    tension: 0.25,
                    yAxisID: 'y',
                    fill: true,
                },
                {
                    label: 'max ms',
                    data: [],
                    borderColor: '#f59e0b',
                    backgroundColor: 'rgba(245,158,11,0.1)',
                    tension: 0.25,
                    yAxisID: 'y',
                    borderDash: [4, 4],
                    fill: false,
                },
                {
                    label: 'min ms',
                    data: [],
                    borderColor: '#22c55e',
                    backgroundColor: 'rgba(34,197,94,0.1)',
                    tension: 0.25,
                    yAxisID: 'y',
                    borderDash: [2, 4],
                    fill: false,
                },
                {
                    label: 'concurrency',
                    data: [],
                    borderColor: '#a78bfa',
                    backgroundColor: 'rgba(167,139,250,0.1)',
                    stepped: true,
                    yAxisID: 'y1',
                    fill: false,
                },
            ],
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            animation: false,
            interaction: { mode: 'index', intersect: false },
            scales: {
                x: {
                    title: { display: true, text: 'second', color: '#94a3b8' },
                    ticks: { color: '#94a3b8' },
                    grid: { color: 'rgba(148,163,184,0.1)' },
                },
                y: {
                    title: { display: true, text: 'response time (ms)', color: '#94a3b8' },
                    beginAtZero: true,
                    ticks: { color: '#94a3b8' },
                    grid: { color: 'rgba(148,163,184,0.1)' },
                },
                y1: {
                    title: { display: true, text: 'concurrency', color: '#94a3b8' },
                    beginAtZero: true,
                    position: 'right',
                    ticks: { color: '#94a3b8', precision: 0 },
                    grid: { drawOnChartArea: false },
                },
            },
            plugins: {
                legend: { labels: { color: '#e2e8f0' } },
            },
        },
    });

    let totalRequests = 0;
    let totalErrors = 0;
    let avgSum = 0;
    let avgCount = 0;
    let source = null;

    function resetState() {
        chart.data.labels = [];
        chart.data.datasets.forEach(d => (d.data = []));
        chart.update();
        totalRequests = 0;
        totalErrors = 0;
        avgSum = 0;
        avgCount = 0;
        elapsedEl.textContent = '0s';
        workersEl.textContent = '0';
        totalEl.textContent = '0';
        errorsEl.textContent = '0';
        avgEl.textContent = '0';
    }

    function finish(message) {
        btn.disabled = false;
        statusEl.textContent = message;
        if (source) {
            source.close();
            source = null;
        }
    }

    btn.addEventListener('click', () => {
        const url = btn.getAttribute('data-url');
        if (!url) return;
        resetState();
        btn.disabled = true;
        statusEl.textContent = 'Running...';

        source = new EventSource('/loadtest/stream?url=' + encodeURIComponent(url));

        source.addEventListener('start', () => {
            statusEl.textContent = 'Load test in progress...';
        });

        source.addEventListener('tick', (ev) => {
            const d = JSON.parse(ev.data);
            chart.data.labels.push(d.second);
            chart.data.datasets[0].data.push(d.avgMs);
            chart.data.datasets[1].data.push(d.maxMs);
            chart.data.datasets[2].data.push(d.minMs);
            chart.data.datasets[3].data.push(d.concurrency);
            chart.update();

            totalRequests += d.count;
            totalErrors += d.errors;
            if (d.count > 0) {
                avgSum += d.avgMs * d.count;
                avgCount += d.count;
            }
            elapsedEl.textContent = d.second + 's';
            workersEl.textContent = d.concurrency;
            totalEl.textContent = totalRequests;
            errorsEl.textContent = totalErrors;
            avgEl.textContent = avgCount > 0 ? Math.round(avgSum / avgCount) : 0;
        });

        source.addEventListener('done', () => {
            finish('Done.');
        });

        source.addEventListener('error', (ev) => {
            let msg = 'Load test failed.';
            if (ev && ev.data) {
                try {
                    const parsed = JSON.parse(ev.data);
                    if (parsed.message) msg = 'Load test failed: ' + parsed.message;
                } catch (e) {
                    // non-JSON — likely a connection drop
                }
            }
            finish(msg);
        });
    });
})();
