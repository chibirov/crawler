var stompClient = null;

function connect() {
    var socket = new SockJS('/stock-exchange');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        stompClient.subscribe('/topic/stocks', function (data) {
            showCompany(JSON.parse(data.body));
        });
    });
}

  var formatter = new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
  });

function processStocks(quantity, company, price, action) {
	$.ajax ({
    	url: "/" + action,
    	type: "POST",
    	data: JSON.stringify({ quantity: quantity, company: company, price: price }),
    	dataType: "json",
    	contentType: "application/json; charset=utf-8",
    	success: function(data) {
    	    $("#owned").html('');
    	    $("#portfolio").html("Current portfolio - <span class='badge'>" + formatter.format(data.portfolio) + "</span>");
    	    let prices = new Map(Object.entries(data.prices));
    	    new Map(Object.entries(data.stocks)).forEach(function( key, value, map ) {
				$("#owned").append('<tr><td>' + value + '</td><td>' + key + '</td><td>' + formatter.format(prices.get(value)) + '</td><td><a href="#" onclick="lookup(\'' + value + '\');">View Stocks</a><td></tr>');
			});	        
    	}, error: function() {
    		alert('Failed to process the request. Please fix the order.');
    	}
	});
}

function disconnect() {
    if (stompClient != null) {
        stompClient.disconnect();
    }
}

function lookup(symbol) {
    stompClient.send("/app/lookup", {}, symbol);
}

function showCompany(company) {
    $("#companyTitle").html(company.name+"&nbsp<span class='label label-primary' id='companySymbol'>(" + company.symbol + ")</span>");
    $("#bid").text(company.bidPrice);
    $("#ask").text(company.askPrice);
}

$(function () {
    $("form").on('submit', function (e) { e.preventDefault(); });
    $("#btnBuy").unbind("click").click(function() { processStocks( $("#quantity").val(), $("#companySymbol").text(),  $("#bid").text(), 'buy'); });
    $("#btnSell").unbind("click").click(function() { processStocks( $("#quantity").val(), $("#companySymbol").text(),  $("#ask").text(), 'sell'); });
    $("#lookup").click(function() { lookup($("#symbol").val()); });
    if (!stompClient) connect();
    $.ajax ({
    	url: "/init",
    	type: "GET",
    	dataType: "json",
    	contentType: "application/json; charset=utf-8",
    	success: function(data){
    	    $("#owned").html('');
    	    $("#portfolio").html("Current portfolio - <span class='badge'>" + formatter.format(data.portfolio) + "</span>");
    	    let prices = new Map(Object.entries(data.prices));
    	    new Map(Object.entries(data.stocks)).forEach(function( key, value, map ) {
				$("#owned").append('<tr><td>' + value + '</td><td>' + key + '</td><td>' + formatter.format(prices.get(value)) + '</td><td><a href="#" onclick="lookup(\'' + value + '\');">View Stocks</a><td></tr>');
			});	        
    	}    	
	});
});