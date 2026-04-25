let deleteUrl = "";

/* ================= DELETE MODAL ================= */
function openDeleteModal(element) {
    deleteUrl = element.getAttribute("data-url");
    document.getElementById("deleteModal").style.display = "flex";
}

document.addEventListener("DOMContentLoaded", function () {

    const confirmBtn = document.getElementById("confirmBtn");
    const cancelBtn = document.getElementById("cancelBtn");

    if (confirmBtn) {
        confirmBtn.addEventListener("click", function () {
            if (deleteUrl) {
                window.location.href = deleteUrl;
            }
        });
    }

    if (cancelBtn) {
        cancelBtn.addEventListener("click", function () {
            document.getElementById("deleteModal").style.display = "none";
        });
    }

    /* ================= CHART ================= */
    const canvas = document.getElementById("financeChart");

    if (canvas && window.income !== undefined && window.expenses !== undefined) {

        const ctx = canvas.getContext("2d");

        new Chart(ctx, {
            type: "bar",
            data: {
                labels: ["Income", "Expenses"],
                datasets: [{
                    data: [window.income, window.expenses],

                    backgroundColor: [
                        "rgba(34, 197, 94, 0.7)",   // green
                        "rgba(239, 68, 68, 0.7)"    // red
                    ],

                    borderRadius: 8
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { display: false }
                }
            }
        });
    }

});