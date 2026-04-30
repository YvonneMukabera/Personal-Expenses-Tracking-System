document.addEventListener("DOMContentLoaded", function() {
    // 1. DASHBOARD CHART
    const canvas = document.getElementById('financeChart');
    if (canvas && typeof incomeData !== 'undefined') {
        const ctx = canvas.getContext('2d');
        const labels = Array.isArray(chartLabels) ? chartLabels : ['Selected Period'];
        const currency = typeof chartCurrency !== 'undefined' ? chartCurrency : 'FRW';
        const formatMoney = (value) => `${Number(value || 0).toLocaleString()} ${currency}`;

        new Chart(ctx, {
            type: 'bar',
            data: {
                labels,
                datasets: [
                    {
                        label: 'Income',
                        data: incomeData,
                        backgroundColor: '#38a169',
                        borderRadius: 6
                    },
                    {
                        label: 'Expenses',
                        data: expenseData,
                        backgroundColor: '#e53e3e',
                        borderRadius: 6
                    },
                    {
                        label: 'Balance',
                        data: balanceData,
                        backgroundColor: '#4facfe',
                        borderRadius: 6
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: (value) => formatMoney(value)
                        }
                    }
                },
                plugins: {
                    legend: { position: 'bottom' },
                    tooltip: {
                        callbacks: {
                            label: (context) => `${context.dataset.label}: ${formatMoney(context.raw)}`
                        }
                    }
                }
            }
        });
    }

    // 2. DELETE MODAL HANDLER
    let targetUrl = "";
    const modal = document.getElementById('deleteModal');

    window.openDeleteModal = function(element) {
        targetUrl = element.getAttribute('data-url');
        if(modal) modal.style.display = 'flex';
    };

    const cancelBtn = document.getElementById('cancelBtn');
    if (cancelBtn) {
        cancelBtn.onclick = () => modal.style.display = 'none';
    }

    const confirmBtn = document.getElementById('confirmBtn');
    if (confirmBtn) {
        confirmBtn.onclick = () => {
            if (targetUrl) window.location.href = targetUrl;
        };
    }

    window.onclick = (event) => {
        if (event.target == modal) modal.style.display = 'none';
    };
});
