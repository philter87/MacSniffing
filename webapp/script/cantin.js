function call_address() {
    var url = 'http://www.w3schools.com/json/myTutorials.txt';
    var xhttp = new XMLHttpRequest();

    xhttp.onreadystatechange = function() {
        alert('ReadyState: ' + xhttp.readyState + ',  Status: ' + xhttp.status);
        if (xhttp.readyState == 4 && xhttp.status == 200) {
            document.getElementById("count").innerHTML = xhttp.responseText;
            var myArr = JSON.parse(xhttp.responseText);
            alert(myArr);
            myFunction(myArr);
        }
    };
    xhttp.open("GET", url, true);
    xhttp.send(null);
}

function myFunction(arr) {
    var out = "";
    var i;
    for(i = 0; i < arr.length; i++) {
        out += '<a href="' + arr[i].url + '">' +
        arr[i].display + '</a><br>';
    }
    document.getElementById("count").innerHTML = out;
}
function httpGet(theUrl) {
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.open( "GET", theUrl, false ); // false for synchronous request
    xmlHttp.send( null );
    return xmlHttp.responseText;
}

function init() {
    var maxVisitor=100;
    var visitorCount=httpGet("http://172.20.10.3:8080/count");
    var value = 90;
    //if(visitorCount.error !== null){
    //    console.log(visitorCount);
    //    visitorCount=50;
    //}
    //console.log(visitorCount);
    //var value = parseInt(visitorCount/500);
    //console.log(value);
    var maxValue = 100;

    /*var opts = {
            lines: 12, // The number of lines to draw
            angle: 0.15, // The length of each line
            lineWidth: 0.44, // The line thickness
        pointer: {
            length: 0.9, // The radius of the inner circle
            strokeWidth: 0.035, // The rotation offset
            color: '#000000' // Fill color
        },
        limitMax: 'false',   // If true, the pointer will not go past the end of the gauge
        colorStart: '#6FADCF',   // Colors
        colorStop: '#8FC0DA',    // just experiment with them
        strokeColor: '#E0E0E0',   // to see which ones work best for you
        generateGradient: true
    };

    var target = document.getElementById('gaugeCanvas'); // your canvas element
    var gauge = new Gauge(target).setOptions(opts); // create sexy gauge!
    gauge.maxValue = maxValue; // set max gauge value
    gauge.animationSpeed = 97; // set animation speed (32 is default value)
    gauge.set(value); // set actual value*/

    var g = new JustGage({
    id: "gaugeDiv",
    value: value,
    min: 0,
    max: maxValue,
    title: "Visitors"
  });

    //var countField = document.getElementById('count');
    //countField.innerHTML = value + "/" + maxValue;

    //call_address();
}
