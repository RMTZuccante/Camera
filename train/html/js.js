var http = new XMLHttpRequest();
var kalive = new XMLHttpRequest();
var host = window.location.hostname;

window.addEventListener('load', function () {
    document.getElementById('video').setAttribute('src', "http://" + host + ":5000/");

    document.addEventListener('keypress', function (k) {
        http.open('GET', 'http://' + host + ':5000/key?k=' + k.key);
        http.send()
    });
});