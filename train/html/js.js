var http = new XMLHttpRequest();


window.addEventListener('load', function () {
    var host = window.location.hostname;
    document.getElementById('video').setAttribute('src', "http://" + host + ":5000/");
    document.getElementById('video').addEventListener('load', function () {

    });
    document.addEventListener('keypress', function (k) {
        http.open('GET', 'http://' + host + ':5000/key?k=' + k.key);
        http.send()
    });
});