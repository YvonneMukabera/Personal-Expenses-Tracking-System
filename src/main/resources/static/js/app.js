let deleteUrl = "";

function openDeleteModal(element) {
    deleteUrl = element.getAttribute("data-url");
    document.getElementById("deleteModal").style.display = "flex";
}

document.addEventListener("DOMContentLoaded", function () {

    document.getElementById("confirmBtn").addEventListener("click", function () {
        if (deleteUrl) {
            window.location.href = deleteUrl;
        }
    });

    document.getElementById("cancelBtn").addEventListener("click", function () {
        document.getElementById("deleteModal").style.display = "none";
    });

});