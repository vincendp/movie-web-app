function deleteAllMoviesFromCart(){
	console.log("deleting all movies");
	jQuery.ajax({
	    method: "GET", 
	    url: "api/cart?deleteAll=TRUE",
	});
}

jQuery.ajax({
	dataType: "json", // Setting return data type
	method: "GET", // Setting request method
	url: "api/checkout", // Setting request url, which is mapped by StarsServlet in Stars.java
	success: (json) => {
		console.log(json);
		deleteAllMoviesFromCart();
		var main = $(".main-container");
		
		for( let i = 0; i < json.length; ++i) {
			var saleId = json[i]["saleId"];
			var movie_name = json[i]["movie_name"];
			
			main.append("<div class=\"sales-container\"><p>Id: " + saleId + " </p><p>Title: " + movie_name + "</p><p>Quantity: 1</p></div>");
			
		}
	}
});